package org.auscope.portal.csw;

import org.apache.xmlbeans.*;
import org.isotc211.x2005.gmd.MDMetadataDocument;
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

    public CSWRecord[] getCSWRecords() throws XPathExpressionException, XmlException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());
        String serviceTitleExpression = "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata";
        NodeList nodes = (NodeList) xPath.evaluate(serviceTitleExpression, this.recordResponse, XPathConstants.NODESET);

        records = new CSWRecord[nodes.getLength()];
        for(int i=0; i<nodes.getLength(); i++ ) {
        	MDMetadataDocument doc =
        		MDMetadataDocument.Factory.parse(nodes.item(i));
            records[i] = new CSWRecord(doc);
        }

        return records;
    }

}
