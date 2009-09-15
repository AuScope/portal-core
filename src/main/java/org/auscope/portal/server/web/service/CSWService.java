package org.auscope.portal.server.web.service;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.util.Util;
import org.auscope.portal.csw.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * Provides some utility methods for accessing data from a CSW service
 *
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
    private Util util;
    private long lastUpdated = 0;
    private static final int UPDATE_INTERVAL = 300000;

    @Autowired
    public CSWService(CSWThreadExecutor executor,
                      HttpServiceCaller serviceCaller,
                      Util util) throws Exception {

        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.util = util;
        
    }

    /**
     * ServiceURL setter
     * @param serviceUrl
     */
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Checks to see if the time interval from the last update has passed. If so, then update the cached records.
     * @throws Exception
     */
    public void updateRecordsInBackground() throws Exception {
        if(System.currentTimeMillis() - lastUpdated > UPDATE_INTERVAL) { //if older that 5 mins then do the update
            executor.execute(new Runnable() {
                public void run() {
                    updateCSWRecords();
                }
            });
            lastUpdated = System.currentTimeMillis();
        }
    }

    /**
     * Updates the cached data records from the CSW service.
     */
    public void updateCSWRecords() {
        try {
            ICSWMethodMaker getRecordsMethod = new CSWMethodMakerGetDataRecords(serviceUrl);

            Document document = util.buildDomFromString(serviceCaller.getMethodResponseAsString(getRecordsMethod.makeMethod(), serviceCaller.getHttpClient()));

            CSWRecord[] tempRecords = new CSWGetRecordResponse(document).getCSWRecords();

            setDatarecords(tempRecords);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Assigns the cached data records.
     * @param records
     */
    private synchronized void setDatarecords(CSWRecord[] records) {
        this.dataRecords = records;
    }

    /**
     * Returns the entire cached record set
     * @return
     * @throws Exception
     */
    public CSWRecord[] getDataRecords() throws Exception {
        return dataRecords;
    }

    /**
     * Returns on WMS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWMSRecords() throws Exception {
        CSWRecord[] records = getDataRecords();

         ArrayList<CSWRecord> wfsRecords = new ArrayList<CSWRecord>();

        for(CSWRecord rec : records) {
            if(rec.getOnlineResourceProtocol() != null)
                if(rec.getOnlineResourceProtocol().contains("WMS") && !rec.getServiceUrl().equals("")) {
                    wfsRecords.add(rec);
                }
        }

        return wfsRecords.toArray(new CSWRecord[wfsRecords.size()]);
    }

    /**
     * Returns only WFS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWFSRecords() throws Exception {
         CSWRecord[] records = getDataRecords();

         ArrayList<CSWRecord> wfsRecords = new ArrayList<CSWRecord>();

        for(CSWRecord rec : records) {
            if(rec.getOnlineResourceProtocol() != null)
                if(rec.getOnlineResourceProtocol().contains("WFS") && !rec.getServiceUrl().equals("")) {
                    wfsRecords.add(rec);
                }
        }

        return wfsRecords.toArray(new CSWRecord[wfsRecords.size()]);
    }

    /**
     * Returns only WFS data records for a given feature typeName
     * @param featureTypeName
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWFSRecordsForTypename(String featureTypeName) throws Exception {
         CSWRecord[] records = getDataRecords();
         ArrayList<CSWRecord> wfsRecords = new ArrayList<CSWRecord>();

        for(CSWRecord rec : records) {
            if(rec.getOnlineResourceProtocol() != null)
                if(rec.getOnlineResourceProtocol().contains("WFS") && !rec.getServiceUrl().equals("") && featureTypeName.equals(rec.getOnlineResourceName())) {
                    wfsRecords.add(rec);
                }
        }

        return wfsRecords.toArray(new CSWRecord[wfsRecords.size()]);
    }
}
