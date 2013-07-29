package org.auscope.portal.core.services.responses.csw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents the response from a CSW GetRecord request
 */
public class CSWGetRecordResponse {

    /** Log object for this class. */
    private final Log log = LogFactory.getLog(getClass());

    private List<CSWRecord> records = null;
    private int recordsReturned = 0;
    private int recordsMatched = 0;
    private int nextRecord = 0;
    
    /** A map object for looking up a particular CSWRecord object by its file identifier */
    private Map<String, CSWRecord> cswRecordLookupMap = new HashMap<String, CSWRecord>();
    /** A list object that stores orphan CSWRecord objects */
    private List<CSWRecord> stagingOrphanRecords = new ArrayList<CSWRecord>();
    
    /**
     * Creates a new instance from the specified record response by parsing its contents
     * 
     * The contents will be parsed according to a normal CSWRecordTransformerFactory
     * 
     * @param getRecordResponse an XML CSW GetRecords response parsed into a DOM tree
     * @param origin Where the getRecordResponse has originated from
     * @throws XPathExpressionException
     */
    public CSWGetRecordResponse(CSWServiceItem origin, Document getRecordResponse) throws XPathExpressionException {
        this(origin, getRecordResponse, new CSWRecordTransformerFactory());
    }
    
    /**
     * Creates a new instance from the specified record response by parsing its contents
     * 
     * The contents will be parsed according to the rules set out by the CSWRecordTransformerFactory
     * 
     * @param getRecordResponse an XML CSW GetRecords response parsed into a DOM tree
     * @param origin Where the getRecordResponse has originated from
     * @throws XPathExpressionException
     */
    public CSWGetRecordResponse(CSWServiceItem origin, Document getRecordResponse, CSWRecordTransformerFactory cswRecordTransformerFactory) throws XPathExpressionException {
        //These cannot be static pre-compiled expressions as they are NOT threadsafe
        CSWNamespaceContext nc = new CSWNamespaceContext();
        XPathExpression exprRecordsMatched = DOMUtil.compileXPathExpr("/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched", nc);
        XPathExpression exprRecordsReturned = DOMUtil.compileXPathExpr("/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned", nc);
        XPathExpression exprNextRecord = DOMUtil.compileXPathExpr("/csw:GetRecordsResponse/csw:SearchResults/@nextRecord", nc);
        XPathExpression exprRecordMetadata = DOMUtil.compileXPathExpr("/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata", nc);

        Node node = (Node) exprRecordsMatched.evaluate(getRecordResponse, XPathConstants.NODE);
        if (node != null) {
            recordsMatched = Integer.parseInt(node.getTextContent());
        }

        node = (Node) exprRecordsReturned.evaluate(getRecordResponse, XPathConstants.NODE);
        if (node != null) {
            recordsReturned = Integer.parseInt(node.getTextContent());
        }

        node = (Node) exprNextRecord.evaluate(getRecordResponse, XPathConstants.NODE);
        if (node != null) {
            nextRecord = Integer.parseInt(node.getTextContent());
        }

        NodeList nodes = (NodeList) exprRecordMetadata.evaluate(getRecordResponse, XPathConstants.NODESET);
        records = new ArrayList<CSWRecord>(nodes.getLength());

        for (int i=0; i<nodes.getLength(); i++ ) {
            Node metadataNode = nodes.item(i);
            CSWRecordTransformer transformer = cswRecordTransformerFactory.newCSWRecordTransformer(metadataNode);
            CSWRecord newRecord = transformer.transformToCSWRecord();
            newRecord.setRecordInfoUrl(String.format(origin.getRecordInformationUrl(), newRecord.getFileIdentifier()));
            records.add(newRecord);
            log.trace("GN layer " + (i + 1) + " : " + newRecord.toString());
            cswRecordLookupMap.put(newRecord.getFileIdentifier(), newRecord);
            //Links a child record to its parent and stage it as it is possible
            //that its parent record hasn't been parsed yet.
            linkChildToParent(newRecord, true);
        }
        
        //Final check to ensure each staged child record links to its parent record.
        //There is no need to stage a child record at this point if it doesn't have
        //a parent - as there is a possibility the record is wrong in the registry.
        for (CSWRecord record : stagingOrphanRecords) {
            linkChildToParent(record, false);
        }
    }
    
    /**
     * Links a child CSWRecord object to its parent object.
     * @param record a CSWRecord object (can be a child or parent object)
     * @param stageOrphanChild flag to indicate whether or not an orphan CSWRecord should be staged.
     */
    private void linkChildToParent(CSWRecord record, boolean stageOrphanChild) {
        String parentId = record.getParentIdentifier();
        if (parentId != null && !parentId.isEmpty()) {
            CSWRecord parent = cswRecordLookupMap.get(parentId);
            if (parent != null) {
                parent.addChildRecord(record);
            } else {
                if (stageOrphanChild) {
                    stagingOrphanRecords.add(record);
                } else {
                    log.trace("An orphan CSWRecord object detected. Child id=" + record.getFileIdentifier());
                }
            }
        }
    }

    /**
     * Returns an unmodifiable list of CSWRecords that were parsed from the response
     * that built this instance.
     * @return
     */
    public List<CSWRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }


    /**
     * Gets the number of records returned (as identified by the response).
     * @return
     */
    public int getRecordsReturned() {
        return recordsReturned;
    }

    /**
     * Gets the number of records that match the original GetRecords query.
     *
     * This can be greater than records returned indicating that only the first
     * X records were returned and subsequent queries are required to get the rest
     * @return
     */
    public int getRecordsMatched() {
        return recordsMatched;
    }

    /**
     * Gets the index of the next record (if there are more following) or 0 otherwise
     * @return
     */
    public int getNextRecord() {
        return nextRecord;
    }
}