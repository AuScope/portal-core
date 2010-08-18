package org.auscope.portal.server.domain.opendap;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.auscope.portal.server.web.view.JSONModelAndView;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Unit tests for ViewVariableFactory (and various ViewVariable implementations)
 * @author vot002
 *
 */
public class TestViewVariableFactory {
    
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);
    
    private NetcdfDataset mockNetCdfDataset = context.mock(NetcdfDataset.class);
    private Variable mockVariable1 = context.mock(Variable.class, "Variable1");
    private Variable mockVariable2 = context.mock(Variable.class, "Variable2");
    private Variable mockVariable3 = context.mock(Variable.class, "Variable3");
    private Dimension mockDimension1 = context.mock(Dimension.class, "Dimension1");
    private Dimension mockDimension2 = context.mock(Dimension.class, "Dimension2");
    private Array mockArray1 = context.mock(Array.class, "Array1");
    private Array mockArray2 = context.mock(Array.class, "Array2");
    private Array mockArray3 = context.mock(Array.class, "Array3");
    private Array mockArray4 = context.mock(Array.class, "Array4");
    private Group mockGroup = context.mock(Group.class);
    
    private void assertViewVariableEquals(SimpleAxis a1, SimpleAxis a2) {
        Assert.assertEquals(a1.getName(), a2.getName());
        Assert.assertEquals(a1.getDataType(), a2.getDataType());
        Assert.assertEquals(a1.getType(), a2.getType());
        Assert.assertEquals(a1.getUnits(), a2.getUnits());
        
        if (a1.getDimensionBounds() == null || a2.getDimensionBounds() == null) {
            Assert.assertNull(a1.getDimensionBounds());
            Assert.assertNull(a2.getDimensionBounds());
        } else {
            Assert.assertEquals(a1.getDimensionBounds().getFrom(), a2.getDimensionBounds().getFrom(), 0.001);
            Assert.assertEquals(a1.getDimensionBounds().getTo(), a2.getDimensionBounds().getTo(), 0.001);
        }
        
        if (a1.getValueBounds() == null || a2.getValueBounds() == null) {
            Assert.assertNull(a1.getValueBounds());
            Assert.assertNull(a2.getValueBounds());
        } else {
            Assert.assertEquals(a1.getValueBounds().getFrom(), a2.getValueBounds().getFrom(), 0.001);
            Assert.assertEquals(a1.getValueBounds().getTo(), a2.getValueBounds().getTo(), 0.001);            
        }
    }
    
    private void assertViewVariableEquals(SimpleGrid g1, SimpleGrid g2) {
        Assert.assertEquals(g1.getName(), g2.getName());
        Assert.assertEquals(g1.getDataType(), g2.getDataType());
        Assert.assertEquals(g1.getType(), g2.getType());
        Assert.assertEquals(g1.getUnits(), g2.getUnits());
        
        if (g1.getAxes() == null || g2.getAxes() == null) {
            Assert.assertNull(g1.getAxes());
            Assert.assertNull(g2.getAxes());
        } else {
            Assert.assertEquals(g1.getAxes().length, g2.getAxes().length);
            
            for (int i = 0; i < g1.getAxes().length; i++) {
                assertViewVariableEquals(g1.getAxes()[i], g2.getAxes()[i]);
            }
        }
    }
    
    private void assertViewVariableEquals(ViewVariable v1, ViewVariable v2) {
        Assert.assertEquals(v1.getClass(), v2.getClass());
        
        if (v1 instanceof SimpleGrid) {
            assertViewVariableEquals((SimpleGrid) v1, (SimpleGrid) v2);
        } else if (v1 instanceof SimpleAxis) {
            assertViewVariableEquals((SimpleAxis) v1, (SimpleAxis) v2);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private void assertViewVariableEquals(ViewVariable[] actual, ViewVariable... expected) {
        
        if (expected == null || actual == null) {
            Assert.assertNull(expected);
            Assert.assertNull(actual);
        } else {
            Assert.assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertViewVariableEquals(expected[i], actual[i]);
            }
        }
    }
    
    private void testJSONEquality(ViewVariable... vars) throws Exception {
        //This is required as its the only way to get the spring framework to correctly "render" our objects into JSON
        final StringWriter actualJSONResponse = new StringWriter();
        context.checking(new Expectations() {{
            allowing(mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
            allowing(mockHttpResponse).setContentType(with(any(String.class)));
        }});
        
        JSONModelAndView mav = new JSONModelAndView(JSONArray.fromObject(vars));
        mav.getView().render(mav.getModel(), null, mockHttpResponse);
        
        String jsonText = String.format("{\"vars\":%1$s}", actualJSONResponse.toString());
        ViewVariable[] result = ViewVariableFactory.fromJSONArray(JSONObject.fromObject(jsonText).getJSONArray("vars"));
        assertViewVariableEquals(result, vars);
    }
    
    /**
     * Create a number of ViewVariables, convert to JSONText, parse JSONText and then test equality
     * 
     * Assumption - The net.sf JSON libraries can parse/write JSON text
     */
    @Test
    public void testParseJSONFull() throws Exception{
        SimpleAxis a1 = new SimpleAxis("a1", "FLOAT", "a1Units", new SimpleBounds(1, 100), new SimpleBounds(11, 1100));
        SimpleAxis a2 = new SimpleAxis("a2", "FLOAT", "a2Units", new SimpleBounds(2, 200), new SimpleBounds(22, 2200));
        SimpleAxis a3 = new SimpleAxis("a3", "FLOAT", "a3Units", new SimpleBounds(3, 300), new SimpleBounds(33, 3300));
        SimpleAxis a4 = new SimpleAxis("a4", "FLOAT", "a4Units", new SimpleBounds(4, 400), new SimpleBounds(44, 4400));
        SimpleAxis a5 = new SimpleAxis("a5", "FLOAT", "a5Units", new SimpleBounds(5, 500), new SimpleBounds(55, 5500));
        SimpleAxis a6 = new SimpleAxis("a6", "FLOAT", "a6Units", new SimpleBounds(6, 600), new SimpleBounds(66, 6600));
        
        SimpleGrid g1 = new SimpleGrid("grid1", "FLOAT", "gunits1", new ViewVariable[] {a1, a2});
        SimpleGrid g2 = new SimpleGrid("grid2", "FLOAT", "gunits2", new ViewVariable[] {a3});
        SimpleGrid g3 = new SimpleGrid("grid3", "FLOAT", "gunits3", new ViewVariable[] {g2, a4});
        SimpleGrid g4 = new SimpleGrid("grid4", "FLOAT", "gunits4", new ViewVariable[] {a5, a6});
        SimpleGrid g5 = new SimpleGrid("grid5", "FLOAT", "gunits5", new ViewVariable[] {g3, g4});
        
        //Run our parsing tests
        testJSONEquality(a1, a2, a3, a4, a5, a6, g1, g2, g3, g4, g5);
    }
    
    /**
     * Tests parsing from a JSON String where only SOME fields are specified
     * @throws Exception
     */
    @Test
    public void testParseJSONPartial1() throws Exception {
        SimpleAxis a1 = new SimpleAxis("time", null, null, null, new SimpleBounds(0, 3));
        SimpleAxis a2 = new SimpleAxis("isobaric", null, null, null, new SimpleBounds(5, 100));
        SimpleAxis a3 = new SimpleAxis("y", null, null, null, new SimpleBounds(-5.5, 22.33));
        SimpleAxis a4 = new SimpleAxis("x", null, null, null, new SimpleBounds(-40, 50));
        
        SimpleGrid g1 = new SimpleGrid("Geopotential_height", null, null, new ViewVariable[] {a1, a2,a3,a4});
        
        String jsonString1 = "{\"constraints\":[{\"type\":\"grid\",\"name\":\"Geopotential_height\",\"axes\":[{\"type\":\"axis\",\"name\":\"time\",\"valueBounds\":{\"from\":0,\"to\":3}},{\"type\":\"axis\",\"name\":\"isobaric\",\"valueBounds\":{\"from\":5,\"to\":100}},{\"type\":\"axis\",\"name\":\"y\",\"valueBounds\":{\"from\":-5.5,\"to\":22.33}},{\"type\":\"axis\",\"name\":\"x\",\"valueBounds\":{\"from\":-40,\"to\":50}}]}]}";
        JSONArray jsonArr1 = JSONObject.fromObject(jsonString1).getJSONArray("constraints");
        ViewVariable[] result = ViewVariableFactory.fromJSONArray(jsonArr1);
        
        assertViewVariableEquals(result, g1);
    }
    
    /**
     * Tests parsing from a JSON String where only SOME fields are specified
     * @throws Exception
     */
    @Test
    public void testParseJSONPartial2() throws Exception {
        SimpleAxis a1 = new SimpleAxis("lat", null, null, new SimpleBounds(-2, 3),null);
        
        String jsonString2 = "{\"constraints\":[{\"type\":\"axis\",\"name\":\"lat\",\"dimensionBounds\":{\"from\":-2,\"to\":3}}]}";
        JSONArray jsonArr2 = JSONObject.fromObject(jsonString2).getJSONArray("constraints");
        ViewVariable[] result = ViewVariableFactory.fromJSONArray(jsonArr2);
        
        assertViewVariableEquals(result, a1);
    }
    
    /**
     * A test of reading a mock NetCDF dataset - reading a non gridded variable with a single axis
     * @throws Exception
     */
    @Test
    public void testParseNetCDFNonGridded() throws Exception {
        
        final DataType dataType = DataType.FLOAT;
        final SimpleAxis expectation = new SimpleAxis("axis1", dataType.name(), "units1", new SimpleBounds(0, 237566), new SimpleBounds(-3995.345, 21531.3));
        
        final List<Variable> variableList = Arrays.asList(mockVariable1);
        final List<Dimension> dimensionList = Arrays.asList(mockDimension1);
        
        //Build up our "mock" dataset
        context.checking(new Expectations() {{
            //Our mock array (will be referenced twice as the variable should make 2 requests for a single variable each)
            //(Instead of fetching the entire variable range and getting the first/last values
            oneOf(mockArray1).getDouble(0);will(returnValue(expectation.getValueBounds().getFrom()));
            oneOf(mockArray2).getDouble(0);will(returnValue(expectation.getValueBounds().getTo()));
            
            //Our dimension is configured here
            allowing(mockDimension1).getLength();will(returnValue((int) expectation.getDimensionBounds().getTo()));
            
            //Our variable is configured here
            allowing(mockVariable1).getDimensions();will(returnValue(dimensionList));
            allowing(mockVariable1).getName();will(returnValue(expectation.getName()));
            allowing(mockVariable1).getDataType();will(returnValue(dataType));
            allowing(mockVariable1).getUnitsString();will(returnValue(expectation.getUnits()));
            oneOf(mockVariable1).read(new int[] {0}, new int[] {1});will(returnValue(mockArray1));
            oneOf(mockVariable1).read(new int[] {(int) expectation.getDimensionBounds().getTo() - 1}, new int[] {1});will(returnValue(mockArray2));
            
            //Configure the mock dataset
            allowing(mockNetCdfDataset).getVariables();will(returnValue(variableList));
        }});
        
        ViewVariable[] result = ViewVariableFactory.fromNetCDFDataset(mockNetCdfDataset);
        
        assertViewVariableEquals(result, expectation);
    }
    
    /**
     * A test of reading a mock NetCDF dataset - reading a gridded variable with a two axes
     * @throws Exception
     */
    @Test
    public void testParseNetCDFGridded() throws Exception {
        final DataType dataType = DataType.FLOAT;
        final SimpleAxis axis1 = new SimpleAxis("axis1", dataType.name(), "units1", new SimpleBounds(0, 23566), new SimpleBounds(-3435.345, 25235.3));
        final SimpleAxis axis2 = new SimpleAxis("axis2", dataType.name(), "units2", new SimpleBounds(0, 3215), new SimpleBounds(-835.225, 278.123));
        final SimpleGrid expectation = new SimpleGrid("grid1", dataType.name(), "myunits", new ViewVariable[] {axis1, axis2});
        
        final List<Variable> variableList = Arrays.asList(mockVariable1, mockVariable2, mockVariable3);
        final List<Dimension> dimensionList1 = Arrays.asList(mockDimension1, mockDimension2);
        final List<Dimension> dimensionList2 = Arrays.asList(mockDimension1);
        final List<Dimension> dimensionList3 = Arrays.asList(mockDimension2);
        
        //Build up our "mock" dataset
        context.checking(new Expectations() {{
            //Our mock array will be fetched twice for each axis
            //(Instead of fetching the entire variable range and getting the first/last values
            allowing(mockArray1).getDouble(0);will(returnValue(axis1.getValueBounds().getFrom()));
            allowing(mockArray2).getDouble(0);will(returnValue(axis1.getValueBounds().getTo()));
            allowing(mockArray3).getDouble(0);will(returnValue(axis2.getValueBounds().getFrom()));
            allowing(mockArray4).getDouble(0);will(returnValue(axis2.getValueBounds().getTo()));
            
            //Our group
            allowing(mockGroup).findVariable(axis1.getName());will(returnValue(mockVariable2));
            allowing(mockGroup).findVariable(axis2.getName());will(returnValue(mockVariable3));
            
            //Our dimensions are configured here
            allowing(mockDimension1).getLength();will(returnValue((int)axis1.getDimensionBounds().getTo()));
            allowing(mockDimension2).getLength();will(returnValue((int)axis2.getDimensionBounds().getTo()));
            allowing(mockDimension1).getGroup();will(returnValue(mockGroup));
            allowing(mockDimension2).getGroup();will(returnValue(mockGroup));
            allowing(mockDimension1).getName();will(returnValue(axis1.getName()));
            allowing(mockDimension2).getName();will(returnValue(axis2.getName()));
            
            //Our variables are configured here
            allowing(mockVariable1).getDimensions();will(returnValue(dimensionList1));
            allowing(mockVariable2).getDimensions();will(returnValue(dimensionList2));
            allowing(mockVariable3).getDimensions();will(returnValue(dimensionList3));
            allowing(mockVariable1).getName();will(returnValue(expectation.getName()));
            allowing(mockVariable2).getName();will(returnValue(axis1.getName()));
            allowing(mockVariable3).getName();will(returnValue(axis2.getName()));
            allowing(mockVariable1).getDataType();will(returnValue(dataType));
            allowing(mockVariable2).getDataType();will(returnValue(dataType));
            allowing(mockVariable3).getDataType();will(returnValue(dataType));
            allowing(mockVariable1).getUnitsString();will(returnValue(expectation.getUnits()));
            allowing(mockVariable2).getUnitsString();will(returnValue(axis1.getUnits()));
            allowing(mockVariable3).getUnitsString();will(returnValue(axis2.getUnits()));
            
            //We want to ensure we only download the minimum/maximum of a range ONCE
            //(Once for the gridded variable and once for the plain axis)
            exactly(2).of(mockVariable2).read(new int[] {0}, new int[] {1});will(returnValue(mockArray1));
            exactly(2).of(mockVariable2).read(new int[] {(int)axis1.getDimensionBounds().getTo() - 1}, new int[] {1});will(returnValue(mockArray2));
            exactly(2).of(mockVariable3).read(new int[] {0}, new int[] {1});will(returnValue(mockArray3));
            exactly(2).of(mockVariable3).read(new int[] {(int)axis2.getDimensionBounds().getTo() - 1}, new int[] {1});will(returnValue(mockArray4));
            
            //Configure the mock dataset
            allowing(mockNetCdfDataset).getVariables();will(returnValue(variableList));
        }});
        
        ViewVariable[] result = ViewVariableFactory.fromNetCDFDataset(mockNetCdfDataset);
        
        assertViewVariableEquals(result, expectation, axis1, axis2);
    }
    
    /**
     * A test of reading a mock NetCDF dataset that fails when requesting the variable range
     * @throws Exception
     */
    @Test(expected=IOException.class)
    public void testParseNetCDFGriddedWithError() throws Exception {
        final DataType dataType = DataType.FLOAT;
        final SimpleAxis expectation = new SimpleAxis("axis1", dataType.name(), "units1", new SimpleBounds(0, 237566), new SimpleBounds(-3995.345, 21531.3));
        
        final List<Variable> variableList = Arrays.asList(mockVariable1);
        final List<Dimension> dimensionList = Arrays.asList(mockDimension1);
        
        //Build up our "mock" dataset
        context.checking(new Expectations() {{
            
            //Our dimension is configured here
            allowing(mockDimension1).getLength();will(returnValue((int) expectation.getDimensionBounds().getTo()));
            
            //Our variable is configured here
            allowing(mockVariable1).getDimensions();will(returnValue(dimensionList));
            allowing(mockVariable1).getName();will(returnValue(expectation.getName()));
            allowing(mockVariable1).getDataType();will(returnValue(dataType));
            allowing(mockVariable1).getUnitsString();will(returnValue(expectation.getUnits()));
            oneOf(mockVariable1).read(new int[] {0}, new int[] {1});will(returnValue(mockArray1));
            oneOf(mockVariable1).read(new int[] {(int) expectation.getDimensionBounds().getTo() - 1}, new int[] {1});will(throwException(new IOException()));
            
            //Configure the mock dataset
            allowing(mockNetCdfDataset).getVariables();will(returnValue(variableList));
        }});
        
        //This should throw an IOException
        ViewVariableFactory.fromNetCDFDataset(mockNetCdfDataset);
    }
    
    /**
     * A test of reading a mock NetCDF dataset - reading a gridded variable with a single axis
     * One of the dimensions parsed will NOT map to an existing variable
     * @throws Exception
     */
    @Test
    public void testParseNetCDFGridded_UnmappedDimension() throws Exception {
        final DataType dataType = DataType.FLOAT;
        final SimpleAxis axis1 = new SimpleAxis("axis1", dataType.name(), "units1", new SimpleBounds(0, 23566), new SimpleBounds(-3435.345, 25235.3));
        final SimpleAxis axis2 = new SimpleAxis("axis2", dataType.name(), "????", new SimpleBounds(0, 9), null);
        final SimpleGrid expectation = new SimpleGrid("grid1", dataType.name(), "myunits", new ViewVariable[] {axis1, axis2});
        
        final List<Variable> variableList = Arrays.asList(mockVariable1, mockVariable2);
        final List<Dimension> dimensionList1 = Arrays.asList(mockDimension1, mockDimension2);
        final List<Dimension> dimensionList2 = Arrays.asList(mockDimension1);
        final List<Dimension> dimensionList3 = Arrays.asList(mockDimension2);
        
        //Build up our "mock" dataset
        context.checking(new Expectations() {{
            //Our mock array will be fetched twice for each axis
            //(Instead of fetching the entire variable range and getting the first/last values
            allowing(mockArray1).getDouble(0);will(returnValue(axis1.getValueBounds().getFrom()));
            allowing(mockArray2).getDouble(0);will(returnValue(axis1.getValueBounds().getTo()));
            
            //Our group
            allowing(mockGroup).findVariable(axis1.getName());will(returnValue(mockVariable2));
            allowing(mockGroup).findVariable(axis2.getName());will(returnValue(null));
            
            //Our dimensions are configured here
            allowing(mockDimension1).getLength();will(returnValue((int)axis1.getDimensionBounds().getTo()));
            allowing(mockDimension2).getLength();will(returnValue(10));
            allowing(mockDimension1).getGroup();will(returnValue(mockGroup));
            allowing(mockDimension2).getGroup();will(returnValue(mockGroup));
            allowing(mockDimension1).getName();will(returnValue(axis1.getName()));
            allowing(mockDimension2).getName();will(returnValue(axis2.getName()));
            
            //Our variables are configured here
            allowing(mockVariable1).getDimensions();will(returnValue(dimensionList1));
            allowing(mockVariable2).getDimensions();will(returnValue(dimensionList2));
            allowing(mockVariable1).getName();will(returnValue(expectation.getName()));
            allowing(mockVariable2).getName();will(returnValue(axis1.getName()));
            allowing(mockVariable1).getDataType();will(returnValue(dataType));
            allowing(mockVariable2).getDataType();will(returnValue(dataType));
            allowing(mockVariable1).getUnitsString();will(returnValue(expectation.getUnits()));
            allowing(mockVariable2).getUnitsString();will(returnValue(axis1.getUnits()));
            
            //We want to ensure we only download the minimum/maximum of a range ONCE
            //(Once for the gridded variable and once for the plain axis)
            exactly(2).of(mockVariable2).read(new int[] {0}, new int[] {1});will(returnValue(mockArray1));
            exactly(2).of(mockVariable2).read(new int[] {(int)axis1.getDimensionBounds().getTo() - 1}, new int[] {1});will(returnValue(mockArray2));
            
            //Configure the mock dataset
            allowing(mockNetCdfDataset).getVariables();will(returnValue(variableList));
        }});
        
        ViewVariable[] result = ViewVariableFactory.fromNetCDFDataset(mockNetCdfDataset);
        
        assertViewVariableEquals(result, expectation, axis1);
    }
    
    /**
     * A test of reading a mock NetCDF dataset - reading a non gridded variable with a single axis
     * @throws Exception
     */
    @Test
    public void testParseVariableFilter() throws Exception {
    	final DataType dataType = DataType.FLOAT;
        final SimpleAxis axis1 = new SimpleAxis("axis1", dataType.name(), "units1", new SimpleBounds(0, 23566), new SimpleBounds(-3435.345, 25235.3));
        final SimpleAxis axis2 = new SimpleAxis("axis2", dataType.name(), "units2", new SimpleBounds(0, 3215), new SimpleBounds(-835.225, 278.123));
        final SimpleGrid grid1 = new SimpleGrid("grid1", dataType.name(), "myunits", new ViewVariable[] {axis1, axis2});
        
        final List<Variable> variableList = Arrays.asList(mockVariable1, mockVariable2, mockVariable3);
        final List<Dimension> dimensionList1 = Arrays.asList(mockDimension1, mockDimension2);
        final List<Dimension> dimensionList2 = Arrays.asList(mockDimension1);
        final List<Dimension> dimensionList3 = Arrays.asList(mockDimension2);
        
        //Build up our "mock" dataset
        context.checking(new Expectations() {{
            //Our mock array will be fetched twice for each axis
            //(Instead of fetching the entire variable range and getting the first/last values
            allowing(mockArray1).getDouble(0);will(returnValue(axis1.getValueBounds().getFrom()));
            allowing(mockArray2).getDouble(0);will(returnValue(axis1.getValueBounds().getTo()));
            allowing(mockArray3).getDouble(0);will(returnValue(axis2.getValueBounds().getFrom()));
            allowing(mockArray4).getDouble(0);will(returnValue(axis2.getValueBounds().getTo()));
            
            //Our group
            allowing(mockGroup).findVariable(axis1.getName());will(returnValue(mockVariable2));
            allowing(mockGroup).findVariable(axis2.getName());will(returnValue(mockVariable3));
            
            //Our dimensions are configured here
            allowing(mockDimension1).getLength();will(returnValue((int)axis1.getDimensionBounds().getTo()));
            allowing(mockDimension2).getLength();will(returnValue((int)axis2.getDimensionBounds().getTo()));
            allowing(mockDimension1).getGroup();will(returnValue(mockGroup));
            allowing(mockDimension2).getGroup();will(returnValue(mockGroup));
            allowing(mockDimension1).getName();will(returnValue(axis1.getName()));
            allowing(mockDimension2).getName();will(returnValue(axis2.getName()));
            
            //Our variables are configured here
            allowing(mockVariable1).getDimensions();will(returnValue(dimensionList1));
            allowing(mockVariable2).getDimensions();will(returnValue(dimensionList2));
            allowing(mockVariable3).getDimensions();will(returnValue(dimensionList3));
            allowing(mockVariable1).getName();will(returnValue(grid1.getName()));
            allowing(mockVariable2).getName();will(returnValue(axis1.getName()));
            allowing(mockVariable3).getName();will(returnValue(axis2.getName()));
            allowing(mockVariable1).getDataType();will(returnValue(dataType));
            allowing(mockVariable2).getDataType();will(returnValue(dataType));
            allowing(mockVariable3).getDataType();will(returnValue(dataType));
            allowing(mockVariable1).getUnitsString();will(returnValue(grid1.getUnits()));
            allowing(mockVariable2).getUnitsString();will(returnValue(axis1.getUnits()));
            allowing(mockVariable3).getUnitsString();will(returnValue(axis2.getUnits()));
            
            //We want to ensure we only download the minimum/maximum of a range ONCE
            //(Once for the gridded variable and once for the plain axis)
            oneOf(mockVariable3).read(new int[] {0}, new int[] {1});will(returnValue(mockArray3));
            oneOf(mockVariable3).read(new int[] {(int)axis2.getDimensionBounds().getTo() - 1}, new int[] {1});will(returnValue(mockArray4));
            
            //Configure the mock dataset
            allowing(mockNetCdfDataset).getVariables();will(returnValue(variableList));
        }});
        
        ViewVariable[] result = ViewVariableFactory.fromNetCDFDataset(mockNetCdfDataset, axis2.getName());
        
        assertViewVariableEquals(result, axis2);
    }
}
