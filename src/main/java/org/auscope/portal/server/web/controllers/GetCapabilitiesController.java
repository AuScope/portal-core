package org.auscope.portal.server.web.controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.record.CSWGeographicBoundingBox;
import org.auscope.portal.csw.record.CSWGeographicElement;
import org.auscope.portal.csw.record.CSWOnlineResource;
import org.auscope.portal.csw.record.CSWOnlineResourceImpl;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.csw.record.CSWResponsibleParty;
import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;
import org.auscope.portal.server.domain.ows.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.server.web.service.GetCapabilitiesService;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles GetCapabilites (WFS)WMS queries.
 *
 * @author Jarek Sanders
 * @version $Id$
 */
@Controller
public class GetCapabilitiesController extends BaseCSWController {


    // ----------------------------------------------------- Instance variables

    private GetCapabilitiesService capabilitiesService;
    protected final Log log = LogFactory.getLog(getClass());

    // ----------------------------------------------------------- Constructors

    @Autowired
    public GetCapabilitiesController( GetCapabilitiesService capService, ViewCSWRecordFactory viewCSWRecordFactory) {
        super(viewCSWRecordFactory);
        this.capabilitiesService = capService;
    }


    // ------------------------------------------- Property Setters and Getters

    /**
     * Gets all WMS data records from a discovery service, and then
     * creates JSON response for the WMS layers list in the portal
     *
     * @return a JSON representation of the CSWRecord equivalent records
     *
     * @throws Exception
     */
    @RequestMapping("/getCustomLayers.do")
    public ModelAndView getCustomLayers( @RequestParam("service_URL") String service_url) throws Exception {

        CSWRecord[] records;
        int invalidLayerCount =0;
        try {
            GetCapabilitiesRecord capabilitiesRec
                = capabilitiesService.getWmsCapabilities(service_url);

            List<CSWRecord> cswRecords = new ArrayList<CSWRecord>();

            if (capabilitiesRec != null) {
                //Make a best effort of parsing a WMS into a CSWRecord
                for (GetCapabilitiesWMSLayerRecord rec : capabilitiesRec.getLayers()) {
                    //to check if layers are EPSG: 4326 SRS
                    String[] uniqueSRSList = getSRSList(capabilitiesRec.getLayerSRS() , rec.getChildLayerSRS());
                    if(!((Arrays.binarySearch(uniqueSRSList, "EPSG:4326"))>=0 || (Arrays.binarySearch(uniqueSRSList, "epsg:4326"))>=0)){
                        invalidLayerCount +=1;
                        continue;
                    }

                    String serviceName = rec.getTitle();
                    String fileId = "unique-id-" + rec.getName();
                    String recordInfoUrl = null;
                    String dataAbstract = rec.getAbstract();
                    CSWResponsibleParty responsibleParty = new CSWResponsibleParty();
                    responsibleParty.setOrganisationName(capabilitiesRec.getOrganisation());

                    CSWGeographicElement[] geoEls = null;
                    CSWGeographicBoundingBox bbox = rec.getBoundingBox();
                    if (bbox != null) {
                        geoEls = new CSWGeographicElement[] {bbox};
                    }

                    CSWOnlineResource[] onlineResources = new CSWOnlineResource[1];
                    onlineResources[0] = new CSWOnlineResourceImpl(new URL(capabilitiesRec.getMapUrl()),
                            "OGC:WMS-1.1.1-http-get-map",
                            rec.getName(),
                            rec.getTitle());

                    CSWRecord newRecord = new CSWRecord(serviceName, fileId, recordInfoUrl, dataAbstract, onlineResources, geoEls );
                    newRecord.setContact(responsibleParty);
                    cswRecords.add(newRecord);
                }
            }
            //generate the same response from a getCSWRecords call
            records = cswRecords.toArray(new CSWRecord[cswRecords.size()]);
        }
        catch (MalformedURLException e) {
            log.debug(e.getMessage());
            return generateJSONResponseMAV(false, "URL not well formed", null);
        }
        catch (Exception e) {
            log.debug(e.getMessage());
            return generateJSONResponseMAV(false, "Unable to process request", null);
        }

        ModelAndView mav = generateJSONResponseMAV(records);
        mav.addObject("invalidLayerCount", invalidLayerCount);
        return mav;
    }

    public String[] getSRSList(String[] layerSRS, String[] childLayerSRS){
        try{
            int totalLength = layerSRS.length;
            totalLength += childLayerSRS.length ;
            String[] totalSRS = new String[totalLength];
            System.arraycopy(layerSRS, 0, totalSRS, 0, layerSRS.length);
            System.arraycopy(childLayerSRS, 0, totalSRS, layerSRS.length, childLayerSRS.length);
            Arrays.sort(totalSRS);

            int k = 0;
            for(int i = 0; i < totalSRS.length; i++){
                if(i>0 && totalSRS[i].equals(totalSRS[i-1]))
                    continue;
                totalSRS[k++] = totalSRS[i];
            }
            String[] uniqueSRS = new String[k];
            System.arraycopy(totalSRS, 0, uniqueSRS, 0, k);
            return uniqueSRS;
        }catch (Exception e) {
            log.debug(e.getMessage());
            return null;
        }

    }
}
