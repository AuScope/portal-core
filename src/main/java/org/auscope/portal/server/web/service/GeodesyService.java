package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.domain.geodesy.GeodesyNamespaceContext;
import org.auscope.portal.server.domain.geodesy.GeodesyObservation;
import org.auscope.portal.server.domain.geodesy.GeodesyObservationsFilter;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Service class for interacting with the Geodesy services
 * @author Josh Vote
 */
@Service
public class GeodesyService {
    private HttpServiceCaller httpServiceCaller;
    private WFSGetFeatureMethodMaker wfsMethodMaker;

    /**
     * Creates a new instance of this class
     * @param httpServiceCaller Used for making requests
     * @param wfsMethodMaker Used for generating requests
     */
    @Autowired
    public GeodesyService(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.wfsMethodMaker = wfsMethodMaker;
    }

    /**
     * Gets all the observations for a particular station
     * @param stationId The station ID to limit the filter to
     * @param startDate The start date in the form 'YYYY-mm-DD'
     * @param endDate The end date in the form 'YYYY-mm-DD'
     * @return
     */
    public List<GeodesyObservation> getObservationsForStation(String wfsUrl, String stationId, String startDate, String endDate) throws PortalServiceException {
        GeodesyObservationsFilter filter = new GeodesyObservationsFilter(stationId, startDate, endDate);
        HttpMethodBase method = null;
        List<GeodesyObservation> allObservations = new ArrayList<GeodesyObservation>();
        try {
            //Make our request
            method = wfsMethodMaker.makeMethod(wfsUrl, "geodesy:station_observations", filter.getFilterStringAllRecords(), null);
            InputStream response = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());

            //Parse it into a DOM doc
            GeodesyNamespaceContext nc = new GeodesyNamespaceContext();
            Document domDoc = DOMUtil.buildDomFromStream(response);

            //Parse the dom doc into observation POJOs
            XPathExpression exprStationId = DOMUtil.compileXPathExpr("geodesy:station_id", nc);
            XPathExpression exprDate = DOMUtil.compileXPathExpr("geodesy:ob_date", nc);
            XPathExpression exprUrl = DOMUtil.compileXPathExpr("geodesy:url", nc);
            NodeList observationNodes = (NodeList) DOMUtil.compileXPathExpr("wfs:FeatureCollection/gml:featureMembers/geodesy:station_observations", nc).evaluate(domDoc, XPathConstants.NODESET);
            for (int i = 0; i < observationNodes.getLength(); i++) {
                Node observationNode = observationNodes.item(i);

                String parsedId = (String) exprStationId.evaluate(observationNode, XPathConstants.STRING);
                String parsedDate = (String) exprDate.evaluate(observationNode, XPathConstants.STRING);
                String parsedUrl = (String) exprUrl.evaluate(observationNode, XPathConstants.STRING);

                allObservations.add(new GeodesyObservation(parsedId, parsedDate, parsedUrl));
            }

        } catch (Exception ex) {
            throw new PortalServiceException(method);
        }

        return allObservations;
    }
}
