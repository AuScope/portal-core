/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.PKCS8Key;

import org.auscope.portal.server.gridjob.GridAccessController;

// see http://www.hpc.jcu.edu.au/projects/archer-data-activities/browser/security/current/slcs-common/src/
// for source code of these classes
import org.glite.slcs.pki.CertificateExtension;
import org.glite.slcs.pki.CertificateExtensionFactory;
import org.glite.slcs.pki.CertificateKeys;
import org.glite.slcs.pki.CertificateRequest;

import org.globus.gsi.CertUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.mvc.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.view.RedirectView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Controller that forwards shibboleth token to SLCS to retrieve a certificate
 * which can subsequently be used to access grid resources.
 *
 * @author Cihan Altinay
 */
@Controller
public class GridLoginController {

    protected final Log logger = LogFactory.getLog(getClass());

    private static final String SLCS_URL = "https://slcs1.arcs.org.au/SLCS/";
    private static final String HOST_KEY_FILE = "/etc/shibboleth/hostkey.pem";
    private static final int PROXY_LIFETIME = 10*24*60*60; // 10 days
    @Autowired
    private GridAccessController gridAccess;

    private class RequestData {
        public String authToken;
        public String certDN;
        public List certExtensions;
    }

    /**
     * Sets the <code>GridAccessController</code> to be used for proxy checking
     * and initialisation.
     *
     * @param gridAccess the GridAccessController to use
     */
    /*public void setGridAccess(GridAccessController gridAccess) {
        this.gridAccess = gridAccess;
    }*/

    /**
     * Main entry point which decides where to redirect to.
     * If this is a GET request and the current grid proxy is not valid then
     * a redirect to the SLCS server is performed. A POST request is handled
     * as being a response from the SLCS server so the certificate is extracted
     * and a grid proxy is generated.
     */
    @RequestMapping("/gridLogin.do")
    public ModelAndView gridLogin(HttpServletRequest request,
                                      HttpServletResponse response) {

        final String serviceUrl = "https://" + request.getServerName() +
            "/gridLogin.do";
        logger.debug("SessionID:"+request.getSession().getId());
        String sharedToken = (String)request.getSession().getAttribute("Shib-Shared-Token");//request.getHeader("Shib-Shared-Token");
        if (sharedToken == null) {
            logger.info("No shared token, redirecting to login.");
            return new ModelAndView(new RedirectView(
                    "/login.html", true, false, false));
        }

        if (request.getMethod().equalsIgnoreCase("GET")) {
            logger.debug("Handling GET request.");
            Object credential = request.getSession().getAttribute("userCred");
            if (gridAccess.isProxyValid(credential)) {
                logger.debug("Valid proxy found.");
                return redirectToTarget(request);
            }
            return redirectToSlcs(serviceUrl);

        } else if (request.getMethod().equalsIgnoreCase("POST")) {
            logger.debug("Handling POST request.");
            try {
                processSlcsResponse(request);
                return redirectToTarget(request);

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        // proxy is not valid so redirect to a page showing what
        // happened, maybe giving option of MyProxy details entry.
        // Simple solution: logout
        return new ModelAndView(new RedirectView(
                    "/Shibboleth.sso/Logout", false, false, false));
    }

    /**
     * Returns a <code>ModelAndView</code> object for a redirect to the
     * SLCS server.
     *
     * @return A prepared <code>ModelAndView</code> to redirect to SLCS.
     */
    private ModelAndView redirectToSlcs(final String serviceUrl) {
        logger.info("Redirecting to SLCS. ServiceUrl= "+serviceUrl);
        return new ModelAndView(
                new RedirectView(SLCS_URL+"token?service="+serviceUrl));
    }

    /**
     * Returns a <code>ModelAndView</code> object which is a redirect either
     * to a page requested prior to login or the gmap.html view by default.
     * 
     * @return The <code>ModelAndView</code> of the proper destination page.
     */
    private ModelAndView redirectToTarget(HttpServletRequest request) {
        Object target = request.getSession().getAttribute("redirectAfterLogin");
        if (target != null) {
            logger.debug("Redirecting to "+target.toString());
            request.getSession().removeAttribute("redirectAfterLogin");
            return new ModelAndView(new RedirectView(
                        target.toString(), true, false, false));
        }
        logger.debug("Redirecting to joblist.");
        return new ModelAndView(new RedirectView(
                    "/gmap.html", true, false, false));
    }

    /**
     * Parses the request data and sets attributes accordingly.
     *
     * @param requestData the data to parse
     */
    private RequestData parseRequestData(final String requestData) {
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(
                    URLDecoder.decode(requestData).trim()));

        RequestData rd = new RequestData();
        try {
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            rd.authToken = doc.getElementsByTagName("AuthorizationToken")
                .item(0).getFirstChild().getNodeValue();
            rd.certDN = doc.getElementsByTagName("Subject")
                .item(0).getFirstChild().getNodeValue();

            // parse and add extensions
            rd.certExtensions = new ArrayList<CertificateExtension>();
            NodeList certExt = doc.getElementsByTagName("CertificateExtension");
            for (int i=0; i < certExt.getLength(); i++) {
                String name = ((Element) certExt.item(i)).getAttribute("name");
                String value = certExt.item(i).getFirstChild().getNodeValue();
                CertificateExtension ext = CertificateExtensionFactory
                    .createCertificateExtension(name, value);
                rd.certExtensions.add(ext);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return rd;
    }

    /**
     * Converts a hex string into a byte array.
     */
    private byte[] unhexlify(final String hexString) {
        byte[] array = new byte[hexString.length()/2];
        
        for (int i=0; i<hexString.length()/2; i++) {
            String s = hexString.substring(i*2, i*2+2);
            array[i] = (byte) Integer.parseInt(s, 16);
        }

        return array;
    }

    /**
     * Uses a cipher to decrypt data from an input stream.
     */
    private String decryptString(InputStream in, Cipher cipher)
            throws GeneralSecurityException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int blockSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(blockSize);
        byte[] inBlock = new byte[blockSize];
        byte[] outBlock = new byte[outputSize];
        int bytesRead;
        do {
            bytesRead = in.read(inBlock);
            if (bytesRead == blockSize) {
                int len = cipher.update(inBlock, 0, blockSize, outBlock);
                output.write(outBlock, 0, len);
            }
        } while (bytesRead == blockSize);

        if (bytesRead > 0) {
            outBlock = cipher.doFinal(inBlock, 0, bytesRead);
        } else {
            outBlock = cipher.doFinal();
        }
        output.write(outBlock);
        return output.toString();
    }

    /**
     * Extracts and decrypts the XML response received from the SLCS server
     */
    private String extractSlcsResponse(HttpServletRequest request)
            throws GeneralSecurityException, IOException {
        String responseXML = null;
        String certReqDataHex = request.getParameter("CertificateRequestData");
        String sessionKeyHex = request.getParameter("SessionKey");
        if (certReqDataHex == null || sessionKeyHex == null) {
            logger.error("CertificateRequestData or SessionKey empty!");
        } else {
            // load host key
            FileInputStream in = new FileInputStream(HOST_KEY_FILE);
            PKCS8Key pem = new PKCS8Key(in, null);
            Key privateKey = pem.getPrivateKey();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.UNWRAP_MODE, privateKey);

            // unwrap session key and decrypt request data
            byte[] wrappedKey = unhexlify(sessionKeyHex);
            ByteArrayInputStream certReqDataEnc =
               new ByteArrayInputStream(unhexlify(certReqDataHex));
            Key key = cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            responseXML = decryptString(certReqDataEnc, cipher);
        }
        return responseXML;
    }
 
    /**
     * Processes the SLCS response and tries to generate a grid proxy from
     * the extracted certificate and key.
     */
    private void processSlcsResponse(HttpServletRequest request)
            throws GeneralSecurityException, Exception {
            
        String slcsResponse = extractSlcsResponse(request);
        logger.debug("SLCSResponse:\n"+slcsResponse);
        RequestData rd = parseRequestData(slcsResponse);

        String certCN = rd.certDN.split("CN=")[1];
        String shibCN = (String)request.getSession().getAttribute("Shib-Person-commonName") + " "
                + (String)request.getSession().getAttribute("Shib-Shared-Token");
        logger.debug("SessionID:"+request.getSession().getId());
        logger.debug("shibCN: "+shibCN);
        if (!certCN.equals(shibCN)) {
            logger.error(certCN+" != "+shibCN);
            throw new GeneralSecurityException(
                    "Certificate is not for current user!");
        }
 
        CertificateKeys certKeys = new CertificateKeys(2048, new char[0]);
        CertificateRequest req = new CertificateRequest(
                certKeys, rd.certDN, rd.certExtensions);

        logger.info("Requesting signed certificate...");
        URL certRespURL = new URL(SLCS_URL +
                "certificate?AuthorizationToken=" + rd.authToken +
                "&CertificateSigningRequest=" +
                URLEncoder.encode(req.getPEMEncoded(), "UTF-8"));
        BufferedReader certRespReader = new BufferedReader(
                new InputStreamReader(certRespURL.openStream()));
        StringBuffer certResp = new StringBuffer();

        String inputLine;
        while ((inputLine = certRespReader.readLine()) != null) {
            certResp.append(inputLine);
            certResp.append('\n');
        }
        certRespReader.close();

        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(certResp.toString().trim()));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = builder.parse(is);
        String status = doc.getElementsByTagName("Status")
            .item(0).getFirstChild().getNodeValue();

        logger.info("Response status: "+status);
        if (!status.equals("Error")) {
            String certStr = doc.getElementsByTagName("Certificate")
                .item(0).getFirstChild().getNodeValue();
            InputStream in = new ByteArrayInputStream(certStr.getBytes());
            X509Certificate certificate = CertUtil.loadCertificate(in);

            Object credential = gridAccess.initProxy(
                    certKeys.getPrivate(), certificate, PROXY_LIFETIME);
            if (credential == null) {
                throw new Exception("Proxy generation failed");
            } else {
                logger.info("Storing credentials in session.");
                request.getSession().setAttribute("userCred", credential);
            }
        }
    }
}

