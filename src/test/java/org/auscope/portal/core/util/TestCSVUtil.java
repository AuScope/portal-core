package org.auscope.portal.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for FileIOUtil
 * 
 * @author VictorTey
 *
 */
public class TestCSVUtil extends PortalTestClass {

    CSVUtil csvUtil;

    @Before
    public void setup() throws IOException {
        String csv = "FID,sample_id,project,station,name,dt,desc_1,desc_2,desc_3,desc_4,desc_5,desc_6,desc_7,elev,wt,sd,bh,temperature,ec,ph,eh,notes,geom\n"
                +
                "hydrogeochem.fid-2a6b33a5_14cb60c5677_3fe3,Cap0001,Abra,Mulgul,JHP8,2014-06-23T15:24:00,SB,P,C,NP,V,,,513,14.05000019,19.04999924,,,0.83999997,7.67999983,111,,POINT (-24.63415 118.6415)\n"
                +
                "hydrogeochem.fid-2a6b33a5_14cb60c5677_3fe4,Cap0002,Abra,Mulgul,JHP9,2014-06-23T16:00:00,SB,P,C,NP,V,,,532,17.89999962,22.89999962,,23.79999924,,8.10999966,125,,POINT (-24.64951 118.65105)\n"
                +
                "hydrogeochem.fid-2a6b33a5_14cb60c5677_3fe5,Cap0003,Abra,Mulgul,JHP7,2014-06-23T16:29:00,SB,P,C,NP,V,,,520,14.5,18.5,,25.20000076,0.164,7.53999996,-2,,POINT (-24.62249 118.62614)\n"
                +
                "hydrogeochem.fid-2a6b33a5_14cb60c5677_3fe6,Cap0004,Abra,Mulgul,JHP10,2014-06-23T16:48:00,SB,P,C,NP,V,,,525,17.70000076,22.70000076,,25.20000076,0.57999998,7.8499999,90,,POINT (-24.62589 118.61684)\n"
                +
                "hydrogeochem.fid-2a6b33a5_14cb60c5677_3fe7,Cap0005,,Mulgul,Quartzite Well,2014-06-24T12:27:00,SW,W,O,PP,V,,,537,20.14999962,24.14999962,,24.20000076,3.33999991,8.19999981,117,,POINT (-24.64268 118.31869)";

        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        csvUtil = new CSVUtil(is);
    }

    /**
     * Tests closeQuietely works as intended when a null reference is passed
     */
    @Test
    public void testCSVHeader() {
        String[] headers = csvUtil.getHeaders();
        Assert.assertNotNull(headers);
        Assert.assertEquals(23, headers.length);
    }

    /**
     * Tests closeQuietely works as intended when a null reference is passed
     * @throws IOException 
     */
    @Test
    public void testColumnOfInterest() throws IOException {
        String[] interest = {"FID", "elev", "wt", "sd"};
        HashMap<String, ArrayList<String>> columns = csvUtil.getColumnOfInterest(interest);
        Assert.assertNotNull(columns);
        Assert.assertEquals(5, columns.get("FID").size());
        Assert.assertEquals(5, columns.get("elev").size());
        Assert.assertEquals(5, columns.get("wt").size());
        Assert.assertEquals(5, columns.get("sd").size());

        Assert.assertEquals("hydrogeochem.fid-2a6b33a5_14cb60c5677_3fe3", columns.get("FID").get(0));
        Assert.assertEquals("hydrogeochem.fid-2a6b33a5_14cb60c5677_3fe4", columns.get("FID").get(1));

    }

    /**
     * Tests closeQuietely works as intended when a null reference is passed
     * @throws IOException 
     */
    @Test
    public void testName() throws IOException {
        String[] interest = {"name"};
        HashMap<String, ArrayList<String>> columns = csvUtil.getColumnOfInterest(interest);
        Assert.assertNotNull(columns);
        Assert.assertEquals(5, columns.get("name").size());

        Assert.assertEquals("JHP8", columns.get("name").get(0));
        Assert.assertEquals("JHP9", columns.get("name").get(1));

    }
    
    /** 
     * Tests that if you input duplicate columns to 'getColumnOfInterest', it returns the correct number of elements
     * @throws IOException 
     */
    @Test
    public void testDuplicateCols() throws IOException {
        String[] dup_names = {"name","name","FID"};
        HashMap<String, ArrayList<String>> columns = csvUtil.getColumnOfInterest(dup_names);
        Assert.assertEquals(5, columns.get("name").size());
        Assert.assertEquals(5, columns.get("FID").size());
    }
}
