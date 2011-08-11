package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.gsml.GSMLResponseHandler;
import org.auscope.portal.gsml.YilgarnGeochemistryFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


/**
 * Acts as a proxy to WFS's
 *
 * User: Mathew Wyatt
 * @version $Id$
 */

@Controller
public class GSMLController extends BaseWFSToKMLController {
    private WFSGetFeatureMethodMaker methodMaker;
    private IFilter filter;
    private GSMLResponseHandler gsmlResponseHandler;

    @Autowired
    public GSMLController(HttpServiceCaller httpServiceCaller,
                          GmlToKml gmlToKml,
                          WFSGetFeatureMethodMaker methodMaker,
                          IFilter filter,
                          GSMLResponseHandler gsmlResponseHandler
                          ) {
        this.httpServiceCaller = httpServiceCaller;
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

        String filterString;

        if (bbox == null) {
            filterString = filter.getFilterStringAllRecords();
        } else {
            filterString = filter.getFilterStringBoundingBox(bbox);
        }
        HttpMethodBase method = methodMaker.makeMethod(serviceUrl, featureType, filterString, maxFeatures, "http://www.opengis.net/gml/srs/epsg.xml#4326");

        String gmlResponse = httpServiceCaller.getMethodResponseAsString(method,
                                                                     httpServiceCaller.getHttpClient());

        return makeModelAndViewKML(convertToKml(gmlResponse, request, serviceUrl), gmlResponse, method);
    }

    @RequestMapping("/doYilgarnGeochemistry.do")
    public ModelAndView doYilgarnGeochemistryFilter(
            @RequestParam(required=false,	value="serviceUrl") String serviceUrl,
            @RequestParam(required=false,	value="geologicName") String geologicName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
            HttpServletRequest request) throws Exception  {


        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        HttpMethodBase method = null;
        try{
            String filterString;
            YilgarnGeochemistryFilter yilgarnGeochemistryFilter = new YilgarnGeochemistryFilter(geologicName);
            if (bbox == null) {
                filterString = yilgarnGeochemistryFilter.getFilterStringAllRecords();
            } else {
                filterString = yilgarnGeochemistryFilter.getFilterStringBoundingBox(bbox);
            }

            method = methodMaker.makeMethod(serviceUrl, "gsml:GeologicUnit", filterString, maxFeatures);
            String yilgarnGeochemResponse = httpServiceCaller.getMethodResponseAsString(method,httpServiceCaller.getHttpClient());

            String kmlBlob =  convertToKml(yilgarnGeochemResponse, request, serviceUrl);

            if (kmlBlob == null || kmlBlob.length() == 0) {
                log.error(String.format("Transform failed serviceUrl='%1$s' gmlBlob='%2$s'",serviceUrl, yilgarnGeochemResponse));
                return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED ,method);
            } else {
                return makeModelAndViewKML(kmlBlob, yilgarnGeochemResponse, method);
            }

        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl, method);
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
        HttpMethodBase method = methodMaker.makeMethod(serviceUrl, featureType, featureId);
        String gmlResponse = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

        return makeModelAndViewKML(convertToKml(gmlResponse, request, serviceUrl), gmlResponse);
    }

    @RequestMapping("/xsltRestProxy.do")
    public void xsltRestProxy(@RequestParam("serviceUrl") String serviceUrl,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            String result = httpServiceCaller.getMethodResponseAsString(new GetMethod(serviceUrl), httpServiceCaller.getHttpClient());

            // Send response back to client
            response.getWriter().println(convertToKml(result, request, serviceUrl));
        } catch (Exception e) {
            log.error(e);
        }
    }
}
