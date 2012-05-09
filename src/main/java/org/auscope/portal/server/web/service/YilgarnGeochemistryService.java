package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.gsml.YilgarnLocatedSpecimenRecord;
import org.auscope.portal.gsml.YilgarnNamespaceContext;
import org.auscope.portal.gsml.YilgarnObservationRecord;
import org.auscope.portal.server.domain.ows.OWSExceptionParser;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Service class providing methods for interacting with a WFS that
 * implements the Yilgarn Laterite Geochemistry information model
 *
 * @author Josh Vote
 *
 */
@Service
public class YilgarnGeochemistryService {

    public final static String LOCATED_SPECIMEN_TYPENAME = "sa:LocatedSpecimen";

    private final Log log = LogFactory.getLog(getClass());
    private HttpServiceCaller httpServiceCaller;
    private WFSGetFeatureMethodMaker methodMaker;

    /**
     * Creates a new instance of this class with the specified dependencies
     * @param httpServiceCaller Will be used to make HTTP requests
     * @param methodMaker Will be used to generate HTTP methods
     */
    @Autowired
    public YilgarnGeochemistryService(HttpServiceCaller httpServiceCaller, WFSGetFeatureMethodMaker methodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.methodMaker = methodMaker;
    }

    /**
     * Evaluates the specified xPath and returns the result as a string. If null is evaluated the empty string will
     * instead be returned.
     * @param xPath The XPath to be evaluated
     * @param node The node that xPath should evaluated on
     * @return
     */
    private String xPathEvalString(String xPath, Node node, YilgarnNamespaceContext nc) throws Exception {
        String result = (String) DOMUtil.compileXPathExpr(xPath, nc).evaluate(node, XPathConstants.STRING);
        return result == null ? "" : result;
    }

    /**
     * Given a URL of a WFS and a ID for a sa:LocatedSpecimen, lookup the located specimen feature
     * and return a simple parsed response.
     *
     * Will return null if locSpecimenId returns no matching features
     *
     * @param serviceUrl A URL referencing a Yilgarn Laterite Geochemistry WFS
     * @param locSpecimenId a gml id for serviceUrl that references a sa:LocatedSpecimen
     * @return
     */
    public YilgarnLocatedSpecimenRecord getLocatedSpecimens(String serviceUrl, String locSpecimenId) throws Exception {
        //Make our request
        HttpMethodBase method = methodMaker.makeMethod(serviceUrl, LOCATED_SPECIMEN_TYPENAME, locSpecimenId);
        InputStream wfsResponse = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());

        //Make a response document
        Document wfsResponseDoc = DOMUtil.buildDomFromStream(wfsResponse);
        OWSExceptionParser.checkForExceptionResponse(wfsResponseDoc);

        //Parse our top level feature
        YilgarnNamespaceContext nc = new YilgarnNamespaceContext();
        XPathExpression xPathLocSpecs = DOMUtil.compileXPathExpr(String.format("/wfs:FeatureCollection/gml:featureMembers/%1$s | /wfs:FeatureCollection/gml:featureMember/%1$s", LOCATED_SPECIMEN_TYPENAME), nc);
        Node locSpecNode = (Node)xPathLocSpecs.evaluate(wfsResponseDoc, XPathConstants.NODE);
        if (locSpecNode == null) {
            log.debug("No matching results");
            return null;
        }
        String materialClass = xPathEvalString("sa:materialClass",locSpecNode,  nc);

        //Parse our the list of observations
        XPathExpression xPathObs = DOMUtil.compileXPathExpr("sa:relatedObservation/om:Observation", nc);
        NodeList observationNodes = (NodeList)xPathObs.evaluate(locSpecNode, XPathConstants.NODESET);
        List<YilgarnObservationRecord> observations = new ArrayList<YilgarnObservationRecord>();
        for (int i = 0; i < observationNodes.getLength(); i++) {
            Node observationNode = observationNodes.item(i);

            String serviceName = xPathEvalString("@gml:id",observationNode,  nc);
            String dateAndTime = xPathEvalString("om:samplingTime/gml:TimeInstant/gml:timePosition", observationNode,  nc);
            String date = dateAndTime.split(" ")[0];
            String observedMineralName = xPathEvalString("om:procedure/omx:ObservationProcess/@gml:id", observationNode,  nc);
            String preparationDetails = xPathEvalString("om:procedure/omx:ObservationProcess/gml:description", observationNode,  nc);
            String labDetails = xPathEvalString("om:procedure/omx:ObservationProcess/sml:contact/@xlink:title", observationNode,  nc);
            String analyticalMethod = xPathEvalString("om:procedure/omx:ObservationProcess/omx:method", observationNode,  nc);
            String observedProperty = xPathEvalString("om:observedProperty/@xlink:href", observationNode,  nc);
            String analyteName = xPathEvalString("om:result/swe:Quantity/gml:name", observationNode,  nc);
            String analyteValue = xPathEvalString("om:result/swe:Quantity/swe:value", observationNode,  nc);
            String urnUOM = xPathEvalString("om:result/swe:Quantity/swe:uom/@xlink:href", observationNode,  nc);
            String uom = "null";
            if (urnUOM.contains("ppm")) {
                uom = "ppm";
            } else if (urnUOM.contains("ppb")) {
                uom = "ppb";
            } else if (urnUOM.contains("%")) {
                uom = "%";
            }

            observations.add(new YilgarnObservationRecord(serviceName, date, observedMineralName, preparationDetails,
                        labDetails, analyticalMethod, observedProperty, analyteName, analyteValue, uom));
        }

        return new YilgarnLocatedSpecimenRecord(observations.toArray(new YilgarnObservationRecord[observations.size()]), materialClass);
    }
}
