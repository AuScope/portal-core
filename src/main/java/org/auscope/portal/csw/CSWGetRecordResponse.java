package org.auscope.portal.csw;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * User: Mathew Wyatt
 * Date: 11/02/2009
 * Time: 11:56:00 AM
 */
public class CSWGetRecordResponse {
    private CSWRecord[] records;
    private Document recordResponse;

    // -------------------------------------------------------------- Constants

    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());

    // --------------------------------------------------------- Public Methods


    public CSWGetRecordResponse(Document getRecordResponseText) {
        this.recordResponse = getRecordResponseText;
    }


    public CSWRecord[] getCSWRecords() throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());

        String serviceTitleExpression
                = "/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata";

        NodeList nodes = (NodeList) xPath.evaluate( serviceTitleExpression
                                                  , this.recordResponse
                                                  , XPathConstants.NODESET );

        log.info("Number of records retrieved from GeoNetwork: " + nodes.getLength());

        records = new CSWRecord[nodes.getLength()];

        for(int i=0; i<nodes.getLength(); i++ ) {
        	records[i] =new CSWRecord(nodes.item(i));
            log.debug("GN layer " + (i+1) + " : " + records[i].toString());
        }

        return records;
    }

}
