package org.auscope.portal.core.services.csw;

import javax.xml.xpath.XPathExpressionException;

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

    public GriddedCSWRecordTransformer() throws PortalServiceException {
        super();
    }
    
    public GriddedCSWRecordTransformer(Node mdMetadataNode,OgcServiceProviderType serverType)  {
        super(mdMetadataNode, serverType);
    }
    
    @Override
    public CSWRecord transformToCSWRecord() throws XPathExpressionException {
        //Parse basic information
        GriddedCSWRecord cswRecord = new GriddedCSWRecord(null);
        super.transformToCSWRecord(cswRecord);
        
        //Extract Date as a string
        cswRecord.setDateStamp(evalXPathString(this.mdMetadataNode, "gmd:dateStamp/gco:DateTime"));
        
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
