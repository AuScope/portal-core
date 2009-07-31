package org.auscope.portal.csw;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * User: Mathew Wyatt
 * Date: 11/02/2009
 * Time: 11:56:00 AM
 */
public class CSWGetRecordResponse {
    private CSWRecord[] records; 
    private Document recordResponse;

    public CSWGetRecordResponse(Document getRecordResponseText) {
        this.recordResponse = getRecordResponseText;
    }

    public CSWRecord[] getCSWRecords() throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());
        String serviceTitleExpression = "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata";
        NodeList nodes = (NodeList) xPath.evaluate(serviceTitleExpression, this.recordResponse, XPathConstants.NODESET);

        System.out.println(nodes.getLength());

        records = new CSWRecord[nodes.getLength()];
        for(int i=0; i<nodes.getLength(); i++ ) {
            records[i] = new CSWRecord(nodes.item(i));
        }

        return records;
    }

}
