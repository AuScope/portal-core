package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker.OPeNDAPFormat;
import org.auscope.portal.core.services.responses.opendap.AbstractViewVariable;
import org.auscope.portal.core.services.responses.opendap.SimpleAxis;
import org.auscope.portal.core.services.responses.opendap.SimpleBounds;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class TestOpendapService extends PortalTestClass {
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private OPeNDAPGetDataMethodMaker mockMethodMaker = context.mock(OPeNDAPGetDataMethodMaker.class);
    private NetcdfDataset mockDataset = context.mock(NetcdfDataset.class);
    private OpendapService service = null;

    /**
     * OpendapService that uses an injected mock NetcdfDataset
     */
    private class TestableOpendapService extends OpendapService {
        // NetcdfDataset dataset;

        public TestableOpendapService(HttpServiceCaller serviceCaller,
                OPeNDAPGetDataMethodMaker getDataMethodMaker,
                NetcdfDataset dataset) {
            super(serviceCaller, getDataMethodMaker);
            // this.dataset = dataset;
        }

        @Override
        protected NetcdfDataset fetchDataset(String serviceUrl) {
            return mockDataset;
        }
    }

    /**
     * Initialise service variable
     */
    @Before
    public void init() {
        service = new TestableOpendapService(mockServiceCaller, mockMethodMaker, mockDataset);
    }

    /**
     * Tests simple axis parsing
     * @throws PortalServiceException 
     * @throws InvalidRangeException 
     * @throws IOException 
     */
    @Test
    public void testGetSimpleAxisVariables() throws PortalServiceException, IOException, InvalidRangeException {
        final String serviceUrl = "http://example.org/opendap";
        final String variableName = "foo";
        final String variableUnits = "ms/s";

        final Variable mockVariable1 = context.mock(Variable.class, "mockVariable1");
        final Variable mockVariable2 = context.mock(Variable.class, "mockVariable2"); //will not be included

        final Dimension mockDimension1 = context.mock(Dimension.class); //belongs to mockVariable1
        final double[] mockDimension1Data = new double[] {1, 1.5, 2, 2.6, 3};

        final Array mockArray1 = context.mock(Array.class, "mockArray1"); //belongs to mockDimension1
        final Array mockArray2 = context.mock(Array.class, "mockArray2"); //belongs to mockDimension1

        final DataType dataType1 = DataType.DOUBLE; //belongs to mockVariable1

        context.checking(new Expectations() {
            {
                oneOf(mockDataset).getVariables();
                will(returnValue(Arrays.asList(mockVariable1, mockVariable2)));

                allowing(mockVariable1).getName();
                will(returnValue(variableName));
                oneOf(mockVariable1).getDimensions();
                will(returnValue(Arrays.asList(mockDimension1)));
                oneOf(mockVariable1).getDataType();
                will(returnValue(dataType1));
                oneOf(mockVariable1).getUnitsString();
                will(returnValue(variableUnits));
                oneOf(mockVariable1).read(new int[] {0}, new int[] {1});
                will(returnValue(mockArray1));
                oneOf(mockVariable1).read(new int[] {mockDimension1Data.length - 1}, new int[] {1});
                will(returnValue(mockArray2));

                allowing(mockDimension1).getLength();
                will(returnValue(mockDimension1Data.length));

                oneOf(mockArray1).getDouble(0);
                will(returnValue(mockDimension1Data[0]));
                oneOf(mockArray2).getDouble(0);
                will(returnValue(mockDimension1Data[mockDimension1Data.length - 1]));

                allowing(mockVariable2).getName();
                will(returnValue("different" + variableName));
            }
        });

        AbstractViewVariable[] variables = service.getVariables(serviceUrl, variableName);
        Assert.assertNotNull(variables);
        Assert.assertEquals(1, variables.length);
        Assert.assertTrue(variables[0] instanceof SimpleAxis);

        SimpleAxis variable = (SimpleAxis) variables[0];
        Assert.assertEquals(variableName, variable.getName());
        Assert.assertEquals(dataType1.name(), variable.getDataType());
        Assert.assertEquals(variableUnits, variable.getUnits());
        Assert.assertEquals(0, variable.getDimensionBounds().getFrom(), 0.001);
        Assert.assertEquals(mockDimension1Data.length, variable.getDimensionBounds().getTo(), 0.001);

        Assert.assertEquals(mockDimension1Data[0], variable.getValueBounds().getFrom(), 0.001);
        Assert.assertEquals(mockDimension1Data[mockDimension1Data.length - 1], variable.getValueBounds().getTo(), 0.001);
    }

    @Test
    public void testGetData() throws IOException, PortalServiceException {
        final String serviceUrl = "http://example.org/opendap";
        final AbstractViewVariable[] constraints = new AbstractViewVariable[] {new SimpleAxis("foo", "DOUBLE", "ms/s",
                null, new SimpleBounds(1.1, 1.3))};
        final OPeNDAPFormat format = OPeNDAPFormat.ASCII;

        final HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);

        try (final HttpClientInputStream mockResponse = context.mock(HttpClientInputStream.class)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getMethod(serviceUrl, format, mockDataset, constraints);
                    will(returnValue(mockMethod));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                    will(returnValue(mockResponse));
                    allowing(mockResponse).close();
                }
            });

            try (InputStream response = service.getData(serviceUrl, format, constraints)) {
                Assert.assertSame(mockResponse, response);
            }
        }
    }

    @Test(expected = PortalServiceException.class)
    public void testGetDataErrorRequest() throws IOException, PortalServiceException {
        final String serviceUrl = "http://example.org/opendap";
        final AbstractViewVariable[] constraints = new AbstractViewVariable[] {new SimpleAxis("foo", "DOUBLE", "ms/s",
                null, new SimpleBounds(1.1, 1.3))};
        final OPeNDAPFormat format = OPeNDAPFormat.ASCII;

        final HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getMethod(serviceUrl, format, mockDataset, constraints);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new IOException()));
            }
        });

        service.getData(serviceUrl, format, constraints);
    }
}
