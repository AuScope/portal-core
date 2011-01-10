package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;


import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.auscope.portal.gsml.GSMLResponseHandler;
import org.auscope.portal.gsml.YilgarnGeochemistryFilter;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.csw.ICSWMethodMaker;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


/**
 * Acts as a proxy to WFS's
 *
 * User: Mathew Wyatt
 * @version $Id$
 */

@Controller
public class GSMLController {
	/** Log object for this class. */
    protected final Log logger = LogFactory.getLog(getClass().getName());
    private HttpServiceCaller serviceCaller;
    private GmlToKml gmlToKml;
    private IWFSGetFeatureMethodMaker methodMaker;
    private IFilter filter;
    private GSMLResponseHandler gsmlResponseHandler;
    
    @Autowired
    public GSMLController(HttpServiceCaller serviceCaller,
                          GmlToKml gmlToKml,
                          IWFSGetFeatureMethodMaker methodMaker,
                          IFilter filter,
                          GSMLResponseHandler gsmlResponseHandler
                          ) {
        this.serviceCaller = serviceCaller;
        this.gmlToKml = gmlToKml;
        this.methodMaker = methodMaker;
        this.filter = filter;
        this.gsmlResponseHandler = gsmlResponseHandler;
    }
    

    /**
     * Given a service Url and a feature type this will query for all of the features, then convert them into KML,
     * to be displayed, assuming that the response will be complex feature GeoSciML
     *
     * @param serviceUrl
     * @param featureType
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getAllFeatures.do")
    public ModelAndView requestAllFeatures(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           @RequestParam(required=false, value="bbox") final String bboxJSONString,
                                           @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
                                           HttpServletRequest request) throws Exception {

        
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJSONString);
     
        JSONArray requestInfo = new JSONArray();
        String filterString;
        
        if (bbox == null) {
            filterString = filter.getFilterStringAllRecords();
        } else {
            filterString = filter.getFilterStringBoundingBox(bbox);
        }
        HttpMethodBase method = methodMaker.makeMethod(serviceUrl, featureType, filterString, maxFeatures);
        RequestEntity ent;
        String body = null;
        if (method instanceof PostMethod) {
        	ent = ((PostMethod) method).getRequestEntity();
            body = ((StringRequestEntity) ent).getContent(); 
        }
        requestInfo.add(serviceUrl);
        requestInfo.add(body);
        
        String gmlResponse = serviceCaller.getMethodResponseAsString(method, 
                                                                     serviceCaller.getHttpClient());

        return makeModelAndViewKML(convertToKml(gmlResponse, request, serviceUrl), gmlResponse, requestInfo);
    }
    
    @RequestMapping("/doYilgarnGeochemistry.do")
    public ModelAndView doYilgarnGeochemistryFilter(
    		@RequestParam(required=false,	value="serviceUrl") String serviceUrl,
    		@RequestParam(required=false,	value="geologicName") String geologicName,
            @RequestParam(required=false,	value="rockLithology") String rockLithology,
            @RequestParam(required=false,	value="weatherLithology") String weatherLithology,            
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
            HttpServletRequest request) throws Exception  {

        
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
     
        JSONArray requestInfo = new JSONArray();
        try{
        	String filterString;
	        YilgarnGeochemistryFilter yilgarnGeochemistryFilter = new YilgarnGeochemistryFilter(geologicName, rockLithology, weatherLithology);
	        if (bbox == null) {
	            filterString = yilgarnGeochemistryFilter.getFilterStringAllRecords();
	        } else {
	            filterString = yilgarnGeochemistryFilter.getFilterStringBoundingBox(bbox);
	        }
	      
	        HttpMethodBase method = methodMaker.makeMethod(serviceUrl, "gsml:GeologicUnit", filterString, maxFeatures);
	        RequestEntity ent;
	        String body = null;
	        if (method instanceof PostMethod) {
	        	ent = ((PostMethod) method).getRequestEntity();
	            body = ((StringRequestEntity) ent).getContent(); 
	        }
	        requestInfo.add(serviceUrl);
	        requestInfo.add(body);
	        
	        String yilgarnGeochemResponse = serviceCaller.getMethodResponseAsString(method,serviceCaller.getHttpClient());
	        
	        String kmlBlob =  convertToKml(yilgarnGeochemResponse, request, serviceUrl);
	        
	        if (kmlBlob == null || kmlBlob.length() == 0) {
	        	logger.error(String.format("Transform failed serviceUrl='%1$s' gmlBlob='%2$s'",serviceUrl, yilgarnGeochemResponse));
            	return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED ,requestInfo);
            } else {
            	return makeModelAndViewKML(kmlBlob, yilgarnGeochemResponse, requestInfo);
            }
	        
        } catch (Exception e) {
            return this.handleExceptionResponse(e, serviceUrl, requestInfo);
        }
    }
    
    
    
    /**
     * Given a service Url, a feature type and a specific feature ID, this function will fetch the specific feature and 
     * then convert it into KML to be displayed, assuming that the response will be complex feature GeoSciML
     * @param serviceUrl
     * @param featureType
     * @param featureId
     * @param request
     * @return
     */
    @RequestMapping("/requestFeature.do")
    public ModelAndView requestFeature(@RequestParam("serviceUrl") final String serviceUrl,
            						   @RequestParam("typeName") final String featureType,
            						   @RequestParam("featureId") final String featureId,
            						   HttpServletRequest request) throws Exception {
    	String gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
            public HttpMethodBase makeMethod() {
                GetMethod method = new GetMethod(serviceUrl);

                ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
                
                //set all of the parameters
                valuePairs.add(new NameValuePair("request", "GetFeature"));
                valuePairs.add(new NameValuePair("typeName", featureType));
                valuePairs.add(new NameValuePair("featureId", featureId));
                

                //attach them to the method
                method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

                return method;
            }
        }.makeMethod(), serviceCaller.getHttpClient());

        return makeModelAndViewKML(convertToKml(gmlResponse, request, serviceUrl), gmlResponse);
    }

    @RequestMapping("/xsltRestProxy.do")
    public void xsltRestProxy(@RequestParam("serviceUrl") String serviceUrl,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            String result = serviceCaller.getMethodResponseAsString(new GetMethod(serviceUrl), serviceCaller.getHttpClient());

            // Send response back to client
            response.getWriter().println(convertToKml(result, request, serviceUrl));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Insert a kml block into a successful JSON response
     * @param kmlBlob
     * @return
     */
    private ModelAndView makeModelAndViewKML(final String kmlBlob, final String gmlBlob) {

        final Map<String,String> data = new HashMap<String,String>();
        data.put("kml", kmlBlob);
        data.put("gml", gmlBlob);

        
        ModelMap model = new ModelMap();
        model.put("success", true);
        model.put("data", data);

        return new JSONModelAndView(model);
    }
    
    
    public ModelAndView handleExceptionResponse(Exception e, String serviceUrl, JSONArray requestInfo) {

    	logger.error(String.format("Exception! serviceUrl='%1$s'", serviceUrl),e);

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
    //for debugger:
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
