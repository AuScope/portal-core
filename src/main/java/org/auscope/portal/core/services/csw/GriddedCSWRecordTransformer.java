package org.auscope.portal.core.services.csw;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.xpath.XPathException;

import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * CSWRecord parser that also considers
 * @author Josh Vote
 *
 */
public class GriddedCSWRecordTransformer extends CSWRecordTransformer {
	
	protected static final String DATETIMEFORMATSTRING = "yyyy-MM-dd'T'HH:mm:ss";
	protected static final String NULL_DATETIME_STRING = "1900-01-01T12:00:00";
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIMEFORMATSTRING);

    public GriddedCSWRecordTransformer() throws PortalServiceException {
        super();
    }
    
    public GriddedCSWRecordTransformer(Node mdMetadataNode)  {
        this(mdMetadataNode, OgcServiceProviderType.Default);
    }
    
    /**
     * constructor with specified serverType
     * @param mdMetadataNode
     * @param serverType
     */
    public GriddedCSWRecordTransformer(Node mdMetadataNode, OgcServiceProviderType serverType)  {
        super(mdMetadataNode, serverType);
    }
    
    @Override
    public CSWRecord transformToCSWRecord() throws XPathException {
        //Parse basic information
        GriddedCSWRecord cswRecord = new GriddedCSWRecord(null);
        super.transformToCSWRecord(cswRecord);
        
        //Extract Date as a string
        String dateStamp = evalXPathString(this.mdMetadataNode, "gmd:dateStamp/gco:DateTime");
        String dateTimeString = null;
        // Note: ElasticSEarch indexes the String field as a Date for some reason, so it can't be empty
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEFORMATSTRING);
        try {
        	dateTimeString = LocalDateTime.parse(dateStamp).format(dateTimeFormatter); 
        } catch(DateTimeParseException e) {
        	dateTimeString = LocalDateTime.parse(NULL_DATETIME_STRING).format(dateTimeFormatter);
        }
        cswRecord.setDateStamp(dateTimeString);
        
        //Extract gridded positional data
        NodeList posAccuracyEls = evalXPathNodeList(this.mdMetadataNode, "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy");
        GriddedDataPositionalAccuracy[] posAccuracyObjs = new GriddedDataPositionalAccuracy[posAccuracyEls.getLength()];
        for (int i = 0; i < posAccuracyEls.getLength(); i++) {
            posAccuracyObjs[i] = new GriddedDataPositionalAccuracy();
            
            posAccuracyObjs[i].setNameOfMeasure(evalXPathString(posAccuracyEls.item(i), "gmd:nameOfMeasure/gco:CharacterString"));
            posAccuracyObjs[i].setUnitOfMeasure(evalXPathString(posAccuracyEls.item(i), "gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/gml:UnitDefinition/gml:identifier"));
            posAccuracyObjs[i].setValue(evalXPathString(posAccuracyEls.item(i), "gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record"));
        }
        cswRecord.setGriddedInfo(posAccuracyObjs);
        
        return cswRecord;
    }

}
