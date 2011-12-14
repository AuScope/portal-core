package org.auscope.portal.server.web.controllers;


import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.gsml.YilgarnGeochemistryFilter;
import org.auscope.portal.gsml.YilgarnLocatedSpecimenRecord;
import org.auscope.portal.gsml.YilgarnObservationRecord;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.web.service.WFSService;
import org.auscope.portal.server.web.service.YilgarnGeochemistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller for controlling access to the Yilgarn Laterite Geochemistry
 * Web Feature Services.
 *
 * @author Tannu Gupta
 * @author Joshua Vote
 *
 */
@Controller
public class YilgarnGeochemistryController extends BasePortalController {

    /** Log object for this class. */
    private final Log logger = LogFactory.getLog(getClass().getName());
    /** Used for making Yilgarn geochemistry specific requests*/
    private YilgarnGeochemistryService geochemService;
    /** Used for making general WFS requests*/
    private WFSService wfsService;


    @Autowired
    public YilgarnGeochemistryController(WFSService wfsService, YilgarnGeochemistryService geochemService) {
        this.wfsService = wfsService;
        this.geochemService = geochemService;
    }

    /**
     * Given a located specimen ID, lookup its details and return a simplified response
     * @param serviceUrl The WFS url containing a sa:LocatedSpecimen type
     * @param featureId The sa:LocatedSpecimen gml:id to lookup
     * @return
     * @throws Exception
     */
    @RequestMapping("/doLocatedSpecimenFeature.do")
    public ModelAndView doLocatedSpecimenFeature(@RequestParam("serviceUrl") final String serviceUrl,
                                       @RequestParam("featureId") final String featureId) throws Exception {

        //Let the underlying service do all the heavy lifting
        YilgarnLocatedSpecimenRecord locSpecimenRecord = null;
        try {
            locSpecimenRecord = geochemService.getLocatedSpecimens(serviceUrl, featureId);
        } catch (Exception ex) {
            log.warn(String.format("error requesting loc spec records for '%1$s' from '%2$s': %3$s",featureId, serviceUrl, ex));
            log.debug("Exception: ", ex);
            return generateJSONResponseMAV(false);
        }
        if (locSpecimenRecord == null) {
            log.info("null response record. Likely due to bad featureId");
            return generateJSONResponseMAV(false);
        }

        //Transform our response for the view
        String[] specName = null;
        String[] uniqueSpecName = null;
        YilgarnObservationRecord[] observations = locSpecimenRecord.getRelatedObservations();
        try {
            specName = new String[observations.length];
            for (int j = 0; j < observations.length; j++) {
                specName[j] = observations[j].getAnalyteName();
            }
            //specName has duplicate values so this is to get Unique values.
            Arrays.sort(specName);
            int k = 0;
            for (int i = 0; i < specName.length; i++) {
                if (i > 0 && specName[i].equals(specName[i-1])) {
                    continue;
                }
                specName[k++] = specName[i];
            }
            uniqueSpecName = new String[k];
            System.arraycopy(specName, 0, uniqueSpecName, 0, k);
        } catch (Exception ex) {
            logger.warn("Error parsing request", ex);
            return generateJSONResponseMAV(false, null, "Error occured whilst parsing response: " + ex.getMessage());
        }
        return generateJSONResponseMAV(true, generateYilgarnModel(observations, locSpecimenRecord.getMaterialClass(), uniqueSpecName), "");

    }

    /**
     * Generates a Model object to send to the view.
     * @param records
     * @param materialDesc
     * @param uniqueSpecName
     * @return
     */
    protected ModelMap generateYilgarnModel(YilgarnObservationRecord[] records, String materialDesc, String[] uniqueSpecName) {
        ModelMap response = new ModelMap();
        response.put("records", records);
        response.put("materialDesc", materialDesc);
        response.put("uniqueSpecName", uniqueSpecName);

        return response;
    }

    /**
     * Utility function for generating an OGC filter for a geologicUnit based on the specified params
     * @return
     */
    private String generateGeologicUnitFilter(String name, String bboxString) {
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxString);
        YilgarnGeochemistryFilter yilgarnGeochemistryFilter = new YilgarnGeochemistryFilter(name);
        if (bbox == null) {
            return yilgarnGeochemistryFilter.getFilterStringAllRecords();
        } else {
            return yilgarnGeochemistryFilter.getFilterStringBoundingBox(bbox);
        }
    }

    /**
     * This method returns the GMl/KML output from a Yilgarn Geochemistry WFS
     * @param serviceUrl A WFS endpoint
     * @param geologicName A name filter for the geologic unit
     * @param bboxJson A FilterBoundingBox encoded in JSON
     * @param maxFeatures The maximum number of features to request (or 0 for unbounded)
     * @return
     * @throws Exception
     */
    @RequestMapping("/doYilgarnGeochemistry.do")
    public ModelAndView doYilgarnGeochemistryFilter(
            @RequestParam(required=false, value="serviceUrl") String serviceUrl,
            @RequestParam(required=false, value="geologicName") String geologicName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception  {

        //Build our filter details
        String filterString = generateGeologicUnitFilter(geologicName, bboxJson);

        //Make our request and get it transformed
        WFSKMLResponse response = null;
        try {
            response = wfsService.getWfsResponseAsKml(serviceUrl, "gsml:GeologicUnit", filterString, maxFeatures, null);
        } catch (Exception ex) {
            log.warn(String.format("Unable to request/transform WFS response for '%1$s' from '%2$s': %3$s",geologicName, serviceUrl, ex));
            log.debug("Exception: ", ex);
            return generateExceptionResponse(ex, serviceUrl);
        }

        return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
    }

    /**
     * Similar to doYilgarnGeochemistryFilter, this method returns the count of the matched features
     * @param serviceUrl A WFS endpoint
     * @param geologicName A name filter for the geologic unit
     * @param bboxJson A FilterBoundingBox encoded in JSON
     * @param maxFeatures The maximum number of features to request (or 0 for unbounded)
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/doYilgarnGeochemistryCount.do")
    public ModelAndView doYilgarnGeochemistryCount(
            @RequestParam(required=false, value="serviceUrl") String serviceUrl,
            @RequestParam(required=false, value="geologicName") String geologicName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception  {

        //Build our filter details
        String filterString = generateGeologicUnitFilter(geologicName, bboxJson);

        //Make our request and get it transformed
        WFSCountResponse response = null;
        try {
            response = wfsService.getWfsFeatureCount(serviceUrl, "gsml:GeologicUnit", filterString, maxFeatures);
        } catch (Exception ex) {
            log.warn(String.format("Unable to request/transform WFS response for '%1$s' from '%2$s': %3$s",geologicName, serviceUrl, ex));
            log.debug("Exception: ", ex);
            return generateExceptionResponse(ex, serviceUrl);
        }

        return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
    }

}
