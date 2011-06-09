package org.auscope.portal.server.web.controllers;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.auscope.portal.gsml.YilgarnLocSpecimenRecords;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class YilgarnLocSpecimenController {
	
	/** Log object for this class. */
    
    protected final Log logger = LogFactory.getLog(getClass().getName());
    private HttpServiceCaller serviceCaller;
    
    @Autowired
    public YilgarnLocSpecimenController(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller; 
    }
	
	
	@RequestMapping("/doLocatedSpecimenFeature.do")
    public ModelAndView doLocatedSpecimenFeature
									  (@RequestParam("serviceUrl") final String serviceUrl,
            						   @RequestParam("typeName") final String featureType,
            						   @RequestParam("featureId") final String featureId,
            						   HttpServletRequest request) throws Exception {
		
		String gmlResponse = null;
		
		try{
	    	gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
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
		}catch (Exception e){
			logger.error("Error occured whilst communicating to remote service", e);
			return generateJSONResponse(false, "Error occured whilst communicating to remote service: " + e.getMessage(), null, null,null, null);
		}
		YilgarnLocSpecimenRecords[] records = null;
		String materialDesc = null;
		String[] specName = null;
		String[] uniqueSpecName = null;
		try{
			records = YilgarnLocSpecimenRecords.parseRecords(gmlResponse);
			materialDesc = YilgarnLocSpecimenRecords.YilgarnLocSpecMaterialDesc(gmlResponse);
	        specName = new String[records.length];
	        for (int j=0; j<records.length; j++){
	        	specName[j] = records[j].getAnalyteName();
	        }
	        //specName has duplicate values so this is to get Unique values.
	        Arrays.sort(specName);
	        int k = 0;
	        for(int i = 0; i < specName.length; i++){
	        	if(i>0 && specName[i].equals(specName[i-1]))
	        		continue;
	        	specName[k++] = specName[i];
	        }
	        uniqueSpecName = new String[k];
	        System.arraycopy(specName, 0, uniqueSpecName, 0, k);
	        

		}
		catch (Exception ex) {
            logger.warn("Error parsing request", ex);
            return generateJSONResponse(false, "Error occured whilst parsing response: " + ex.getMessage(), null, null,null, null);
		}
		return generateJSONResponse(true, "No errors found", gmlResponse, records, materialDesc, uniqueSpecName);
    
    }
	
	@RequestMapping("/doYilgarnGeochemistryDownload.do")
    public ModelAndView doYilgarnGeochemistryDownload(
								    		@RequestParam(required=false,	value="serviceUrl") final String serviceUrl,
								    		@RequestParam("typeName") final String featureType,
											@RequestParam("featureId") final String featureId,
								            HttpServletRequest request) throws Exception  {

        
		String gmlResponse = null;
        try{
	    	gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
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
		}catch (Exception e){
			logger.error("Error occured whilst communicating to remote service", e);
			return generateJSONResponse(false, "Error occured whilst communicating to remote service: " + e.getMessage(), null, null,null,null);
		}
		return generateJSONResponse(true, "No errors found",gmlResponse, null,null,null);
    }
    

    protected JSONModelAndView generateJSONResponse(boolean success, String errorMessage, final String gmlResponse, YilgarnLocSpecimenRecords[] records, String materialDesc, String[] uniqueSpecName){
    	final Map<String,String> data = new HashMap<String,String>();
        data.put("gml", gmlResponse);
    	
    	ModelMap response = new ModelMap();    	
        response.put("success", success);
        response.put("msg", errorMessage);
        response.put("data", data);
        response.put("records", records);
        response.put("materialDesc", materialDesc);
        response.put("uniqueSpecName", uniqueSpecName);
        
        return new JSONModelAndView(response);
    }
    
    
    /**
     * Given a list of URls, this function will collate the responses 
     * into a zip file and send the response back to the browser.
     *
     * @param serviceUrls
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadLocSpecAsZip.do")
    public void downloadLocSpecAsZip( @RequestParam("serviceUrls") final String[] serviceUrls,
                                  HttpServletResponse response) throws Exception {

        //set the content type for zip files
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=Chemistry.zip;");

        //create the output stream
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        
        for(int i=0; i<serviceUrls.length; i++) {
            GetMethod method = new GetMethod(serviceUrls[i]);
            HttpClient client = serviceCaller.getHttpClient();

            logger.trace("Calling service: " + serviceUrls[i]);
            //Our request may fail (due to timeout or otherwise)
            String responseString = null;
            JSONObject jsonObject = null;
            try {
                responseString = serviceCaller.getMethodResponseAsString(method, client);
                
                logger.trace("Response: " + responseString);

                jsonObject = JSONObject.fromObject( responseString );
            } catch (Exception ex) {
                //Replace a failure exception with a JSONObject representing that exception
                logger.error(ex, ex);
                jsonObject = new JSONObject();
                jsonObject.put("msg", ex.getMessage());
                jsonObject.put("success", false);
                responseString = ex.toString();
            }

            //Extract our data (if it exists)
            byte[] gmlBytes = new byte[] {}; //The error response is an empty array
            Object dataObject = jsonObject.get("data");
            Object messageObject = jsonObject.get("msg"); //This will be used as an error string
            if (messageObject == null) {
            	messageObject = ""; 
            }
            
            if (dataObject != null) {
            	Object gmlResponseObject = JSONObject.fromObject(dataObject).get("gml");
            	
            	if (gmlResponseObject != null) {
            		gmlBytes = gmlResponseObject.toString().getBytes();
            	}
            }
        	
            logger.trace(gmlBytes.length);

            if(jsonObject.get("success").toString().equals("false")) {
            	//The server may have returned an error message, if so, lets include it in the filename
            	String messageString = messageObject.toString();
            	if (messageString.length() == 0)
            		messageString = "operation-failed";
            	
            	//"Tidy" up the message
            	messageString = messageString.replace(' ', '_').replace(".", "");
            	
            	zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i+1) + "yyyyMMdd_HHmmss").format(new Date()) + "-" + messageString + ".xml"));
            } else {
                //create a new entry in the zip file with a timestamped name 
            	zout.putNextEntry(new ZipEntry(new SimpleDateFormat((i+1) + "yyyyMMdd_HHmmss").format(new Date()) + ".xml"));               
            }

            zout.write(gmlBytes);
            zout.closeEntry();
        }

        zout.finish();
        zout.flush();
        zout.close();
    }
	

}
