package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.mineraloccurrence.Commodity;
import org.auscope.portal.mineraloccurrence.Mine;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.KnownFeatureTypeDefinition;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.auscope.portal.server.web.service.NvclService;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.web.service.CommodityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.xml.sax.SAXException;

import com.sun.xml.internal.fastinfoset.util.StringArray;

/**
 * Controller that handles all Earth Resource related requests
 * <p>
 * It handles the following WFS features:
 * <ul>
 * <li>Mine</li>
 * <li>Mineral Occurrence</li>
 * <li>Mininig Activity</li>
 * </ul>
 * </p>
 * 
 * @author Jarek Sanders
 * @version $Id$
 */
@Controller
public class EarthResourcesFilterController {
    
    // -------------------------------------------------------------- Constants
    
    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());
    
    /** Query all mines command */
    private static String ALL_MINES = "";
    
    // ----------------------------------------------------- Instance variables
    
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrenceService mineralOccurrenceService;
    private NvclService nvclService;
    private GmlToKml gmlToKml;
    private CommodityService commodityService;
    private HttpServiceCaller httpServiceCaller;
    private CSWService cswService;
   
    
    
    // ----------------------------------------------------------- Constructors
    
    @Autowired
    public EarthResourcesFilterController
        ( MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler,
          MineralOccurrenceService mineralOccurrenceService,
          NvclService nvclService,
          GmlToKml gmlToKml,
          CommodityService commodityService,
          HttpServiceCaller httpServiceCaller
          ) {
        
        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
        this.mineralOccurrenceService = mineralOccurrenceService;
        this.nvclService = nvclService;
        this.gmlToKml = gmlToKml;
        this.commodityService = commodityService;
        this.httpServiceCaller = httpServiceCaller;
        
        
        
       
    }

    // ------------------------------------------- Property Setters and Getters   
    
    
    /**
     * Handles the Earth Resource Mine filter queries.
     * (If the bbox elements are specified, they will limit the output response to 200 records implicitly)
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception 
     */
    @RequestMapping("/doMineFilter.do")
    public ModelAndView doMineFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures, 
            HttpServletRequest request) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        
        JSONArray requestInfo = new JSONArray();
        try {
            String gmlBlob;
            HttpMethodBase method;
            if (mineName.equals(ALL_MINES)) {//get all mines 
                if (bbox == null){
                	method = this.mineralOccurrenceService.getAllMinesGML(serviceUrl, maxFeatures);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
                else{
                	method = this.mineralOccurrenceService.getAllVisibleMinesGML(serviceUrl, bbox, maxFeatures);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
            } else {
                if (bbox == null){
                	method = this.mineralOccurrenceService.getMineWithSpecifiedNameGML(serviceUrl, mineName, maxFeatures);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
                else{
                	method = this.mineralOccurrenceService.getVisibleMineWithSpecifiedNameGML(serviceUrl, mineName, bbox, maxFeatures);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
            }

            
            /* getting the request string for debug window*/
            RequestEntity ent;
            String body = null;
            if (method instanceof PostMethod) {
            	ent = ((PostMethod) method).getRequestEntity();
                body = ((StringRequestEntity) ent).getContent(); 
            }
            requestInfo.add(serviceUrl);
            requestInfo.add(body); 
            
            String kmlBlob =  convertToKml(gmlBlob, request, serviceUrl);
            
            //log.debug(kmlBlob);
            log.info("i m in doMineFilter"); 
            //This failure test should be made a little bit more robust
            //And should probably try to extract an error message
            if (kmlBlob == null || kmlBlob.length() == 0) {
                log.error(String.format("Transform failed serviceUrl='%1$s' gmlBlob='%2$s'",serviceUrl, gmlBlob));
            	return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED ,requestInfo);
            } else {            	
            	return makeModelAndViewKML(kmlBlob, gmlBlob, requestInfo);
            }
        } catch (Exception e) {
            return this.handleExceptionResponse(e, serviceUrl, requestInfo);
        }
        
    }

    
    /**
     * Handles the Earth Resource MineralOccerrence filter queries.
     *
     * @param serviceUrl
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param request                the HTTP client request
     * 
     * @return a WFS response converted into KML
     * @throws Exception 
     */
    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
        @RequestParam(value="serviceUrl",            required=false) String serviceUrl,
        @RequestParam(value="commodityName",         required=false) String commodityName,
        @RequestParam(value="measureType",           required=false) String measureType,
        @RequestParam(value="minOreAmount",          required=false) String minOreAmount,
        @RequestParam(value="minOreAmountUOM",       required=false) String minOreAmountUOM,
        @RequestParam(value="minCommodityAmount",    required=false) String minCommodityAmount,
        @RequestParam(value="minCommodityAmountUOM", required=false) String minCommodityAmountUOM,
        @RequestParam(required=false, value="bbox") String bboxJson,
        @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
        HttpServletRequest request) throws Exception 
    {
        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        
        JSONArray requestInfo = new JSONArray();
        
        try {

            //get the mineral occurrences
            String mineralOccurrenceResponse = null;
            HttpMethodBase method;
            if (bbox == null) { 
            	method = this.mineralOccurrenceService.getMineralOccurrenceGML ( 
                        serviceUrl,
                        commodityName, 
                        measureType,
                        minOreAmount,
                        minOreAmountUOM,
                        minCommodityAmount,
                        minCommodityAmountUOM,
                        maxFeatures);            	           	
            	mineralOccurrenceResponse = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                
            } else {
                method = this.mineralOccurrenceService.getVisibleMineralOccurrenceGML ( 
                            serviceUrl,
                            commodityName,
                            measureType,
                            minOreAmount,
                            minOreAmountUOM,
                            minCommodityAmount,
                            minCommodityAmountUOM,
                            bbox,
                            maxFeatures);                
                mineralOccurrenceResponse = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());                
            }    
            RequestEntity ent;
            String body = null;
            if (method instanceof PostMethod) {
            	ent = ((PostMethod) method).getRequestEntity();
                body = ((StringRequestEntity) ent).getContent(); 
            }
            requestInfo.add(serviceUrl);
            requestInfo.add(body);
            
            // If there are 0 features then send NO_RESULTS message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS, requestInfo);
            
            //if everything is good then return the KML
            return makeModelAndViewKML(convertToKml(mineralOccurrenceResponse, request, serviceUrl), mineralOccurrenceResponse, requestInfo);

        } catch (Exception e) {
            return this.handleExceptionResponse(e, serviceUrl, requestInfo);
            
        }
    }
    
    
    /**
     * Handles Mining Activity filter queries
     * Returns WFS response converted into KML.
     *
     * @param serviceUrl
     * @param mineName
     * @param startDate 
     * @param endDate 
     * @param oreProcessed 
     * @param producedMaterial 
     * @param cutOffGrade 
     * @param production 
     * @param request
     * @return the KML response
     * @throws Exception 
     */    
    @RequestMapping("/doMiningActivityFilter.do")
    public ModelAndView doMiningActivityFilter(
            @RequestParam("serviceUrl")       String serviceUrl,
            @RequestParam("mineName")         String mineName,
            @RequestParam("startDate")        String startDate,
            @RequestParam("endDate")          String endDate,
            @RequestParam("oreProcessed")     String oreProcessed,
            @RequestParam("producedMaterial") String producedMaterial,
            @RequestParam("cutOffGrade")      String cutOffGrade,
            @RequestParam("production")       String production,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
            HttpServletRequest request) 
    throws Exception 
    {
        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        
        JSONArray requestInfo = new JSONArray();
        try {
        	
            List<Mine> mines = new ArrayList<Mine>();   
           
            if (!mineName.equals("")) {
                //We intentionally do not bbox filter here, the bbox will happen during the main response 
                mines = this.mineralOccurrenceService.getMineWithSpecifiedName
                                                                    ( serviceUrl
                                                                    , mineName
                                                                    , maxFeatures);
                
                // If there are 0 features then send nice message to the user
                if (mines.size() == 0)
                    return makeModelAndViewFailure(ErrorMessages.NO_RESULTS);                    
            }
           
            // Get the mining activities
            HttpMethodBase method = null;;
            String miningActivityResponse = null;
            if (bbox == null) {
            	method = this.mineralOccurrenceService.getMiningActivityGML( serviceUrl
                        											, mines
                        											, startDate
                        											, endDate
                        											, oreProcessed
                        											, producedMaterial
                        											, cutOffGrade
                        											, production
                        											, maxFeatures);
                miningActivityResponse = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                    
            } else {
            	method = this.mineralOccurrenceService.getVisibleMiningActivityGML( serviceUrl
            																, mines
            																, startDate
            																, endDate
            																, oreProcessed
            																, producedMaterial
            																, cutOffGrade
            																, production
            																, bbox
            																, maxFeatures);
                miningActivityResponse = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                    
            }
            log.info("i m in doMiningActivityFilter"); 
            /* getting the request string for debug window*/
            RequestEntity ent;
            String body = null;
            if (method instanceof PostMethod) {
            	ent = ((PostMethod) method).getRequestEntity();
                body = ((StringRequestEntity) ent).getContent(); 
            }
            requestInfo.add(serviceUrl);
            requestInfo.add(body);
            // If there are 0 features then send NO_RESULTS message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(miningActivityResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS, requestInfo);
            
            return makeModelAndViewKML(convertToKml(miningActivityResponse, request, serviceUrl), miningActivityResponse, requestInfo);

        } catch (Exception e) {
            return this.handleExceptionResponse(e, serviceUrl, requestInfo);
        }
    }

    
    /**
     * Handles the NVCL filter queries.
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception 
     */
    @RequestMapping("/doNvclFilter.do")
    public ModelAndView doNvclFilter( @RequestParam("serviceUrl") String serviceUrl,
                                      @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
                                      @RequestParam(required=false, value="bbox") String bboxJson,
                                      HttpServletRequest request) throws Exception {
        
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);  
        
        JSONArray requestInfo = new JSONArray();
        
        
        try {
        	HttpMethodBase method = this.nvclService.getAllBoreholes(serviceUrl, maxFeatures, bbox);
            String gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

            String kmlBlob = convertToKml(gmlBlob, request, serviceUrl);
            RequestEntity ent;
            String body = null;
            if (method instanceof PostMethod) {
            	ent = ((PostMethod) method).getRequestEntity();
                body = ((StringRequestEntity) ent).getContent(); 
            }
            requestInfo.add(serviceUrl);
            requestInfo.add(body);
            
            //log.debug(kmlBlob);
            log.info("i m in doNVCLFilter");    
            // This failure test should be more robust,
            // it should try to extract an error message
            if (kmlBlob == null || kmlBlob.length() == 0) {
                log.error(String.format("Transform failed serviceUrl='%1$s' gmlBlob='%2$s'",serviceUrl, gmlBlob));
                return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED, requestInfo);
            } else {
                return makeModelAndViewKML(kmlBlob, gmlBlob, requestInfo);
            }
        } catch (Exception e) {
            return this.handleExceptionResponse(e, serviceUrl, requestInfo);
        }
    }    
    
    /**
     * Exception resolver that maps exceptions to views presented to the user
     * @param exception
     * @return ModelAndView object with error message 
     */
    public ModelAndView handleExceptionResponse(Exception e, String serviceUrl) {

        log.error(String.format("Exception! serviceUrl='%1$s'", serviceUrl),e);

        // Service down or host down
        if(e instanceof ConnectException || e instanceof UnknownHostException) {
            return this.makeModelAndViewFailure(ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION);
        }

        // Timouts
        if(e instanceof ConnectTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT);
        }
        
        if(e instanceof SocketTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT);
        }        

        // An error we don't specifically handle or expect
        return makeModelAndViewFailure(ErrorMessages.FILTER_FAILED);
    } 
    //copy of above
    public ModelAndView handleExceptionResponse(Exception e, String serviceUrl, JSONArray requestInfo) {

        log.error(String.format("Exception! serviceUrl='%1$s'", serviceUrl),e);

        // Service down or host down
        if(e instanceof ConnectException || e instanceof UnknownHostException) {
            return this.makeModelAndViewFailure(ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION, requestInfo);
        }

        // Timouts
        if(e instanceof ConnectTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT, requestInfo);
        }
        
        if(e instanceof SocketTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT, requestInfo);
        }        

        // An error we don't specifically handle or expect
        return makeModelAndViewFailure(ErrorMessages.FILTER_FAILED, requestInfo);
    } 
   

    // ------------------------------------------------------ Protected Methods

    // ------------------------------------------------------ Private Methods
    /**
     * Create a new ModelAndView given a kml block and serialised xml document.
     * @param kmlBlob
     * @param gmlBlob
     * @return ModelAndView JSON response object
     */
   /* private ModelAndView makeModelAndViewKML(final String kmlBlob, final String gmlBlob) {
        final Map<String,String> data = new HashMap<String,String>();
        data.put("kml", kmlBlob);
        data.put("gml", gmlBlob);
        
        ModelMap model = new ModelMap();
        model.put("success", true);
        model.put("data", data);

        return new JSONModelAndView(model);
    }*/
    
    /**
     * Create a failure response
     *
     * @param message
     * @return
     */
  
    
    private ModelAndView makeModelAndViewFailure(final String message) {    	
        ModelMap model = new ModelMap(); 
        
        model.put("success", false);
        model.put("msg", message); 
        
        return new JSONModelAndView(model);
    }
    
    private ModelAndView makeModelAndViewKML(final String kmlBlob, final String gmlBlob, JSONArray requestInfo) {
    	
    	    	
    	final Map<String,String> data = new HashMap<String,String>();
        data.put("kml", kmlBlob);
        data.put("gml", gmlBlob);
        
        final Map<String,String> debugInfo = new HashMap<String,String>();
        debugInfo.put("url",requestInfo.getString(0) );
        debugInfo.put("info",requestInfo.getString(1) );
        
        ModelMap model = new ModelMap();
        model.put("success", true);
        model.put("data", data);
        model.put("debugInfo", debugInfo);

        return new JSONModelAndView(model);
    }
    
    private ModelAndView makeModelAndViewFailure(final String message, JSONArray requestInfo) {    	
    	final Map<String,String> debugInfo = new HashMap<String,String>();
        debugInfo.put("url",requestInfo.getString(0) );
        debugInfo.put("info",requestInfo.getString(1) );
    	
    	ModelMap model = new ModelMap(); 
        
        model.put("success", false);
        model.put("msg", message); 
        model.put("debugInfo", debugInfo);
        
        return new JSONModelAndView(model);
    }
    
    
    /**
     * Assemble a call to convert GeoSciML into kml format 
     * @param geoXML
     * @param httpRequest
     * @param serviceUrl
     */
    private String convertToKml(String geoXML, HttpServletRequest httpRequest, String serviceUrl) {
        InputStream inXSLT = httpRequest.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/kml.xsl");
        
        return gmlToKml.convert(geoXML, inXSLT, serviceUrl);
    }
}
