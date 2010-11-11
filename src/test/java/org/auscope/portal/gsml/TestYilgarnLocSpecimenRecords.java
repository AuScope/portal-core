package org.auscope.portal.gsml;

import org.junit.Test;
import org.junit.Assert;

public class TestYilgarnLocSpecimenRecords {
	
	@Test
	public void parseRecordsTest() throws Exception{
		final String xmlString = org.auscope.portal.Util.loadXML("src/test/resources/YilgarnLocSpecimenResponse.xml");
		
		YilgarnLocSpecimenRecords[] records = YilgarnLocSpecimenRecords.parseRecords(xmlString);
		
		Assert.assertNotNull(records);
        Assert.assertEquals(1, records.length);
        Assert.assertNotNull(records[0]);
        
        YilgarnLocSpecimenRecords record = records[0];
        
        Assert.assertEquals("serviceName1", record.getServiceName());
        Assert.assertEquals("2006-09-28 00:00:00.0", record.getDateAndTime());
        Assert.assertEquals("quantityName1", record.getObservedMineralName());
        Assert.assertEquals("MineralDescription1", record.getObservedMineralDescription());
        Assert.assertEquals("obsProcessContact1", record.getObsProcessContact());
        Assert.assertEquals("obsProcessMethod1", record.getObsProcessMethod());
        Assert.assertEquals("observedProperty", record.getObservedProperty());
        Assert.assertEquals("quantityName1", record.getQuantityName());
        Assert.assertEquals("21.4", record.getQuantityValue());
        Assert.assertEquals("urn:ogc:def:uom:UCUM::%25", record.getUom());
        
	}

}
