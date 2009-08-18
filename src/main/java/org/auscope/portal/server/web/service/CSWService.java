package org.auscope.portal.server.web.service;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.csw.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * User: Mathew Wyatt
 * Date: 02/07/2009
 * Time: 2:33:49 PM
 */
@Service
public class CSWService {
    private Logger logger = Logger.getLogger(getClass());

    private CSWRecord[] dataRecords = new CSWRecord[0];
    private HttpServiceCaller serviceCaller;
    private String serviceUrl;
    private CSWThreadExecutor executor;
    private long lastUpdated = 0;
    private static final int UPDATE_INTERVAL = 300000;

    @Autowired
    public CSWService(CSWThreadExecutor executor, HttpServiceCaller serviceCaller) throws Exception {
        this.executor = executor;
        this.serviceCaller = serviceCaller;

        this.serviceUrl = "http://auscope-portal-test.arrc.csiro.au/geonetwork/srv/en/csw";

        this.updateCSWRecords();
    }

    public void updateCSWRecords() throws Exception {
        if(System.currentTimeMillis() - lastUpdated > UPDATE_INTERVAL) { //if older that 5 mins then do the update
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        CSWRecord[] tempRecords = getRecordResponse(new CSWMethodMakerGetDataRecords(serviceUrl)).getCSWRecords();
                        setDatarecords(tempRecords);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            lastUpdated = System.currentTimeMillis();
        }
    }

    private synchronized void setDatarecords(CSWRecord[] records) {
        this.dataRecords = records;
    }

    /*public CSWService(HttpServiceCaller serviceCaller, String serviceUrl) {
        this.serviceCaller = serviceCaller;
        this.serviceUrl = serviceUrl;
    }*/

    public CSWGetRecordResponse getRecordResponse(ICSWMethodMaker methodMaker) throws Exception, ParserConfigurationException, SAXException {
        return new CSWGetRecordResponse(this.getDocumentResponse(methodMaker));
    }

    public Document getDocumentResponse(ICSWMethodMaker methodMaker) throws Exception, SAXException, ParserConfigurationException {
        return buildDom(serviceCaller.callMethod(methodMaker.makeMethod(), serviceCaller.getHttpClient()));
    }

    private Document buildDom(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString.toString()));
        Document doc = builder.parse(inputSource);

        return doc;
    }

    private CSWRecord[] getAllDataRecords() throws Exception {
        this.updateCSWRecords();
        return dataRecords;
    }

    public CSWRecord[] getWMSRecords() throws Exception {
        CSWRecord[] records = getAllDataRecords();

         ArrayList<CSWRecord> wfsRecords = new ArrayList<CSWRecord>();

        for(CSWRecord rec : records) {
            if(rec.getOnlineResourceProtocol() != null)
                if(rec.getOnlineResourceProtocol().contains("WMS") && !rec.getServiceUrl().equals("")) {
                    wfsRecords.add(rec);
                }
        }

        return wfsRecords.toArray(new CSWRecord[wfsRecords.size()]);
    }

    public CSWRecord[] getWFSRecords() throws Exception {
         CSWRecord[] records = getAllDataRecords();

         ArrayList<CSWRecord> wfsRecords = new ArrayList<CSWRecord>();

        for(CSWRecord rec : records) {
            if(rec.getOnlineResourceProtocol() != null)
                if(rec.getOnlineResourceProtocol().contains("WFS") && !rec.getServiceUrl().equals("")) {
                    wfsRecords.add(rec);
                }
        }

        return wfsRecords.toArray(new CSWRecord[wfsRecords.size()]);
    }

    public CSWRecord[] getWFSRecordsKnownType(String featureTypeName) throws Exception {
         CSWRecord[] records = getAllDataRecords();
         ArrayList<CSWRecord> wfsRecords = new ArrayList<CSWRecord>();

        for(CSWRecord rec : records) {
            if(rec.getOnlineResourceProtocol() != null)
                if(rec.getOnlineResourceProtocol().contains("WFS") && !rec.getServiceUrl().equals("") && featureTypeName.equals(rec.getOnlineResourceName())) {
                    wfsRecords.add(rec);
                }
        }
        return wfsRecords.toArray(new CSWRecord[wfsRecords.size()]);
    }

    /*public CSWRecord[] getWFSRecordsKnownTypes() throws Exception {
         CSWRecord[] records = new CSWGetRecordResponse(this.getDocumentResponse(new CSWMethodMakerGetDataRecords(serviceUrl))).getCSWRecords();

         ArrayList<CSWRecord> wfsRecords = new ArrayList<CSWRecord>();

        for(CSWRecord rec : records) {
            if(rec.getOnlineResourceProtocol() != null)
                if(rec.getOnlineResourceProtocol().contains("WFS") && !rec.getServiceUrl().equals("") && knownTypes.contains(rec.getOnlineResourceName())) {
                    wfsRecords.add(rec);
                }
        }

        return wfsRecords.toArray(new CSWRecord[wfsRecords.size()]);
    }*/

    

}
