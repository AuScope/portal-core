package org.auscope.portal.server.web.controllers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.text.SimpleDateFormat;

import java.util.Date;

import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.methods.GetMethod;

import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.service.HttpServiceCaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Given a service URL,
 */
@Controller
public class XSLTRestProxy {
    protected final Log logger = LogFactory.getLog(getClass().getName());
    
    @Autowired private GmlToKml gmlToKml;
    @Autowired private HttpServiceCaller httpServiceCaller;
    
    @RequestMapping("/xsltRestProxy.do")
    public void xsltRestProxy(@RequestParam("serviceUrl") String serviceUrl,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            String result = httpServiceCaller.callMethod(new GetMethod(serviceUrl), httpServiceCaller.getHttpClient());

            // Send response back to client
            response.getWriter().println(gmlToKml.convert(result, request));
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
