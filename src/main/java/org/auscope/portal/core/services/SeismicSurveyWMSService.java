package org.auscope.portal.core.services;

import java.io.InputStream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.http.client.methods.HttpGet;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformer;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformerFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class SeismicSurveyWMSService {

    private HttpServiceCaller serviceCaller;

    private CSWRecordTransformerFactory transformerFactory;

    // ----------------------------------------------------------- Constructors
    @Autowired
    public SeismicSurveyWMSService(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
        this.transformerFactory = new CSWRecordTransformerFactory();

    }

    public CSWRecord getCSWRecord(String httpUrl) throws Exception {
        HttpGet get = new HttpGet(httpUrl);
        InputStream responseString = this.serviceCaller.getMethodResponseAsStream(get);
        Document responseDoc = DOMUtil.buildDomFromStream(responseString);

        CSWNamespaceContext nc = new CSWNamespaceContext();
        XPathExpression exprRecordMetadata = DOMUtil.compileXPathExpr("/csw:GetRecordByIdResponse/gmd:MD_Metadata", nc);

        NodeList nodes = (NodeList) exprRecordMetadata.evaluate(responseDoc, XPathConstants.NODESET);

        Node metadataNode = nodes.item(0);
        CSWRecordTransformer transformer = transformerFactory.newCSWRecordTransformer(metadataNode);
        CSWRecord newRecord = transformer.transformToCSWRecord();

        return newRecord;

    }

}
