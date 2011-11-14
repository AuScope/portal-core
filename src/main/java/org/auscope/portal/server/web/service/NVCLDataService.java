package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.PlotScalarResponse;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker.PlotScalarGraphType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathResult;

/**
 * Service class for accessing an instance of a NVCLDataService web service.
 *
 * See https://twiki.auscope.org/wiki/CoreLibrary/WebServicesDevelopment for the full API this service
 * class attempts to provide
 *
 * @author Josh Vote
 */
@Service
public class NVCLDataService {

    private final Log log = LogFactory.getLog(getClass());

    private HttpServiceCaller httpServiceCaller;
    private NVCLDataServiceMethodMaker methodMaker;

    /**
     * Creates a new NVCLDataService with the specified dependencies
     */
    @Autowired
    public NVCLDataService(HttpServiceCaller httpServiceCaller, NVCLDataServiceMethodMaker methodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.methodMaker = methodMaker;
    }

    /**
     * Makes and parses a getDatasetCollection request to a NVCLDataService
     * @param serviceUrl The NVCLDataService url
     * @param holeIdentifier The unique borehole ID to query
     * @throws Exception
     */
    public List<GetDatasetCollectionResponse> getDatasetCollection(String serviceUrl, String holeIdentifier) throws Exception {
        HttpMethodBase method = methodMaker.getDatasetCollectionMethod(serviceUrl, holeIdentifier);

        //Make our request, parse it into a DOM document
        InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());
        Document responseDoc = DOMUtil.buildDomFromStream(responseStream);

        //Get our dataset nodes
        XPathExpression expr = DOMUtil.compileXPathExpr("DatasetCollection/Dataset");
        NodeList nodeList = (NodeList) expr.evaluate(responseDoc, XPathConstants.NODESET);

        //Parse our response objects
        List<GetDatasetCollectionResponse> responseObjs = new ArrayList<GetDatasetCollectionResponse>();
        XPathExpression exprDatasetId = DOMUtil.compileXPathExpr("DatasetID");
        XPathExpression exprDatasetName = DOMUtil.compileXPathExpr("DatasetName");
        XPathExpression exprOmUrl = DOMUtil.compileXPathExpr("OmUrl");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            String datasetId = (String) exprDatasetId.evaluate(node, XPathConstants.STRING);
            String datasetName = (String) exprDatasetName.evaluate(node, XPathConstants.STRING);
            String omUrl = (String) exprOmUrl.evaluate(node, XPathConstants.STRING);
            responseObjs.add(new GetDatasetCollectionResponse(datasetId, datasetName, omUrl));
        }

        return responseObjs;
    }

    /**
     * Makes and parses a getLogCollection request to a NVCLDataService
     * @param serviceUrl The NVCLDataService url
     * @param datasetId The unique dataset ID to query
     * @param forMosaicService indicates if the getLogCollection service should generate a result specifically for the use of a Mosaic Service
     * @throws Exception
     */
    public List<GetLogCollectionResponse> getLogCollection(String serviceUrl, String datasetId, boolean forMosaicService) throws Exception {
        HttpMethodBase method = methodMaker.getLogCollectionMethod(serviceUrl, datasetId, forMosaicService);

        //Make our request, parse it into a DOM document
        InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());
        Document responseDoc = DOMUtil.buildDomFromStream(responseStream);

        //Get our dataset nodes
        XPathExpression expr = DOMUtil.compileXPathExpr("LogCollection/Log");
        NodeList nodeList = (NodeList) expr.evaluate(responseDoc, XPathConstants.NODESET);

        //Parse our response objects
        List<GetLogCollectionResponse> responseObjs = new ArrayList<GetLogCollectionResponse>();
        XPathExpression exprLogId = DOMUtil.compileXPathExpr("LogID");
        XPathExpression exprLogName = DOMUtil.compileXPathExpr("logName");
        XPathExpression exprSampleCount = DOMUtil.compileXPathExpr("SampleCount");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            String logId = (String) exprLogId.evaluate(node, XPathConstants.STRING);
            String logName = (String) exprLogName.evaluate(node, XPathConstants.STRING);
            String sampleCountString = (String) exprSampleCount.evaluate(node, XPathConstants.STRING);
            int sampleCount = 0;
            if (sampleCountString != null && !sampleCountString.isEmpty()) {
                sampleCount = Integer.parseInt(sampleCountString);
            }

            responseObjs.add(new GetLogCollectionResponse(logId, logName, sampleCount));
        }

        return responseObjs;
    }

    /**
     * Makes a mosaic request and returns the resulting data in a MosaicResponse object.
     *
     * @param serviceUrl The URL of the NVCLDataService
     * @param logId The logID (from a getLogCollection request) to query
     * @param width [Optional] specify the number of column the images are to be displayed
     * @param startSampleNo [Optional] the first sample image to be displayed
     * @param endSampleNo [Optional] the last sample image to be displayed
     * @return
     */
    public MosaicResponse getMosaic(String serviceUrl, String logId, Integer width, Integer startSampleNo, Integer endSampleNo) throws Exception {
        HttpMethodBase method = methodMaker.getMosaicMethod(serviceUrl, logId, width, startSampleNo, endSampleNo);

        InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());
        Header contentHeader = method.getResponseHeader("Content-Type");

        return new MosaicResponse(responseStream, contentHeader == null ? null : contentHeader.getValue());
    }

    /**
     * Makes a plot scalar request and returns the resulting data in a PlotScalarResponse object.
     *
     * @param serviceUrl The URL of the NVCLDataService
     * @param logId The logID (from a getLogCollection request) to query
     * @param width [Optional] the width of the image in pixel
     * @param height [Optional] the height of the image in pixel
     * @param startDepth [Optional] the start depth of a borehole collar
     * @param endDepth [Optional] the end depth of a borehole collar
     * @param samplingInterval [Optional] the interval of the sampling
     * @param graphType [Optional] The type of graph to plot
     * @return
     */
    public PlotScalarResponse getPlotScalar(String serviceUrl, String logId, Integer startDepth, Integer endDepth, Integer width, Integer height, Double samplingInterval, PlotScalarGraphType graphType) throws Exception {
        HttpMethodBase method = methodMaker.getPlotScalarMethod(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);

        InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method, httpServiceCaller.getHttpClient());
        Header contentHeader = method.getResponseHeader("Content-Type");

        return new PlotScalarResponse(responseStream, contentHeader == null ? null : contentHeader.getValue());
    }
}
