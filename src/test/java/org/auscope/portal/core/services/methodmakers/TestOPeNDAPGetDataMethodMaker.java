package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker.OPeNDAPFormat;
import org.auscope.portal.core.services.responses.opendap.AbstractViewVariable;
import org.auscope.portal.core.services.responses.opendap.SimpleAxis;
import org.auscope.portal.core.services.responses.opendap.SimpleBounds;
import org.auscope.portal.core.services.responses.opendap.SimpleGrid;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;

import ucar.ma2.Array;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Unit tests for OPeNDAPGetDataMethodMaker
 * 
 * @author vot002
 *
 */
public class TestOPeNDAPGetDataMethodMaker extends PortalTestClass {

    private NetcdfDataset mockNetCdfDataset = context.mock(NetcdfDataset.class);
    private Variable mockVariable1 = context.mock(Variable.class, "Variable1");
    private Variable mockVariable2 = context.mock(Variable.class, "Variable2");
    private Array mockArray1 = context.mock(Array.class, "Array1");
    private Array mockArray2 = context.mock(Array.class, "Array2");

    /**
     * Tests the url for each download format
     * @throws IOException 
     */
    @Test()
    public void testExtension() throws IOException {
        final String opendapUrl = "http://fake.com/blah.nc";
        OPeNDAPGetDataMethodMaker methodMaker = new OPeNDAPGetDataMethodMaker();

        HttpRequestBase method = methodMaker.getMethod(opendapUrl, OPeNDAPFormat.ASCII, mockNetCdfDataset, null);
        Assert.assertNotNull(method);
        Assert.assertEquals(opendapUrl + ".ascii", method.getURI().toString());

        method = methodMaker.getMethod(opendapUrl, OPeNDAPFormat.DODS, mockNetCdfDataset, null);
        Assert.assertNotNull(method);
        Assert.assertEquals(opendapUrl + ".dods", method.getURI().toString());
    }

    private static List <NameValuePair> parseQuery(final String query) {
        if (query != null && query.length() > 0) {
            return URLEncodedUtils.parse(query, Consts.UTF_8);
        }
        return null;
    }

    /**
     * Tests a single constraint when it does NOT have to calculate the minimum bounding box
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test()
    public void testSingleAxisConstraintNoBBox() throws IOException, URISyntaxException {
        final String opendapUrl = "http://fake.com/blah.nc";
        final OPeNDAPFormat format = OPeNDAPFormat.ASCII;
        final SimpleAxis a1 = new SimpleAxis("name1", "FLOAT", "km/h", new SimpleBounds(43, 56), null);

        OPeNDAPGetDataMethodMaker methodMaker = new OPeNDAPGetDataMethodMaker();

        HttpRequestBase method = methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset,
                new AbstractViewVariable[] {a1});

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().toString().startsWith(opendapUrl));
        URIBuilder builder = new URIBuilder();
        builder.setParameters(parseQuery(String.format("%1$s[%2$d:%3$d]", a1.getName(), (int) a1.getDimensionBounds().getFrom(),
                (int) a1.getDimensionBounds().getTo())));

        Assert.assertEquals(builder.build().getQuery(),
                method.getURI().getQuery());

        //This will ensure that the dimensions bounds are used EVEN if the value bounds are specified
        final SimpleAxis a2 = new SimpleAxis("name2", "FLOAT", "km/h", new SimpleBounds(41, 99), new SimpleBounds(-995,
                787));
        method = methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset, new AbstractViewVariable[] {a2});

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().toString().startsWith(opendapUrl));

        builder = new URIBuilder();
        builder.setParameters(parseQuery(String.format("%1$s[%2$d:%3$d]", a2.getName(), (int) a2.getDimensionBounds().getFrom(),
                (int) a2.getDimensionBounds().getTo())));
        Assert.assertEquals(builder.build().getQuery(),
                method.getURI().getQuery());
    }

    /**
     * Tests a single constraint when it MUST calculate minimum bounding box
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test()
    public void testSingleAxisConstraint() throws IOException, URISyntaxException {
        OPeNDAPGetDataMethodMaker methodMaker = new OPeNDAPGetDataMethodMaker();
        final String opendapUrl = "http://fake.com/blah.nc";
        final OPeNDAPFormat format = OPeNDAPFormat.ASCII;
        final double[] dimensionVals = {0, 1, 2, 3};

        //Test minimum bounding box
        final SimpleAxis a1 = new SimpleAxis("name1", "FLOAT", "km/h", null, new SimpleBounds(0.7, 2.1));
        context.checking(new Expectations() {
            {
                allowing(mockArray1).getDouble(0);
                will(returnValue(dimensionVals[0]));
                allowing(mockArray1).getDouble(1);
                will(returnValue(dimensionVals[1]));
                allowing(mockArray1).getDouble(2);
                will(returnValue(dimensionVals[2]));
                allowing(mockArray1).getDouble(3);
                will(returnValue(dimensionVals[3]));
                allowing(mockArray1).getSize();
                will(returnValue((long) dimensionVals.length));

                //We don't want our variables to be downloaded more than once
                oneOf(mockVariable1).read();
                will(returnValue(mockArray1));

                allowing(mockNetCdfDataset).findVariable("/" + a1.getName());
                will(returnValue(mockVariable1));
            }
        });

        HttpRequestBase method = methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset,
                new AbstractViewVariable[] {a1});

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().toString().startsWith(opendapUrl));
        URIBuilder builder = new URIBuilder();
        builder.setParameters(parseQuery(String.format("%1$s[1:2]", a1.getName())));

        Assert.assertEquals(builder.build().getQuery(),
                method.getURI().getQuery());

        //Test an exact bounding box
        final SimpleAxis a2 = new SimpleAxis("name2", "FLOAT", "km/h", null, new SimpleBounds(1, 3));
        context.checking(new Expectations() {
            {
                allowing(mockArray1).getDouble(0);
                will(returnValue(dimensionVals[0]));
                allowing(mockArray1).getDouble(1);
                will(returnValue(dimensionVals[1]));
                allowing(mockArray1).getDouble(2);
                will(returnValue(dimensionVals[2]));
                allowing(mockArray1).getDouble(3);
                will(returnValue(dimensionVals[3]));
                allowing(mockArray1).getSize();
                will(returnValue((long) dimensionVals.length));

                //We don't want our variables to be downloaded more than once
                oneOf(mockVariable1).read();
                will(returnValue(mockArray1));

                allowing(mockNetCdfDataset).findVariable("/" + a2.getName());
                will(returnValue(mockVariable1));
            }
        });

        method = methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset, new AbstractViewVariable[] {a2});

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().toString().startsWith(opendapUrl));
        builder = new URIBuilder();
        builder.setParameters(parseQuery(String.format("%1$s[1:3]", a2.getName())));

        Assert.assertEquals(builder.build().getQuery(),
                method.getURI().getQuery());

        //Test a bounding box that runs off the end
        final SimpleAxis a3 = new SimpleAxis("name3", "FLOAT", "km/h", null, new SimpleBounds(-100, 500));
        context.checking(new Expectations() {
            {
                allowing(mockArray1).getDouble(0);
                will(returnValue(dimensionVals[0]));
                allowing(mockArray1).getDouble(1);
                will(returnValue(dimensionVals[1]));
                allowing(mockArray1).getDouble(2);
                will(returnValue(dimensionVals[2]));
                allowing(mockArray1).getDouble(3);
                will(returnValue(dimensionVals[3]));
                allowing(mockArray1).getSize();
                will(returnValue((long) dimensionVals.length));

                //We don't want our variables to be downloaded more than once
                oneOf(mockVariable1).read();
                will(returnValue(mockArray1));

                allowing(mockNetCdfDataset).findVariable("/" + a3.getName());
                will(returnValue(mockVariable1));
            }
        });

        method = methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset, new AbstractViewVariable[] {a3});

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().toString().startsWith(opendapUrl));
        builder = new URIBuilder();
        builder.setParameters(parseQuery(String.format("%1$s[0:3]", a3.getName())));
        Assert.assertEquals(builder.build().getQuery(),
                method.getURI().getQuery());
    }

    /**
     * Tests a single grid constraint
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test
    public void testSingleGridConstraint() throws IOException, URISyntaxException {
        OPeNDAPGetDataMethodMaker methodMaker = new OPeNDAPGetDataMethodMaker();
        final String opendapUrl = "http://fake.com/blah.nc";
        final OPeNDAPFormat format = OPeNDAPFormat.ASCII;
        final double[] dimensionVals1 = {0, 1, 2, 3};
        final double[] dimensionVals2 = {-5, -4, -3, -2};

        final SimpleAxis a1 = new SimpleAxis("name1", "FLOAT", "km/h", null, new SimpleBounds(0.7, 2.1));
        final SimpleAxis a2 = new SimpleAxis("name2", "FLOAT", "m^2", null, new SimpleBounds(-3.3, -2));
        final SimpleGrid g1 = new SimpleGrid("grid1", "FLOAT", "nm", new AbstractViewVariable[] {a1, a2});

        context.checking(new Expectations() {
            {
                allowing(mockArray1).getDouble(0);
                will(returnValue(dimensionVals1[0]));
                allowing(mockArray1).getDouble(1);
                will(returnValue(dimensionVals1[1]));
                allowing(mockArray1).getDouble(2);
                will(returnValue(dimensionVals1[2]));
                allowing(mockArray1).getDouble(3);
                will(returnValue(dimensionVals1[3]));
                allowing(mockArray1).getSize();
                will(returnValue((long) dimensionVals1.length));

                allowing(mockArray2).getDouble(0);
                will(returnValue(dimensionVals2[0]));
                allowing(mockArray2).getDouble(1);
                will(returnValue(dimensionVals2[1]));
                allowing(mockArray2).getDouble(2);
                will(returnValue(dimensionVals2[2]));
                allowing(mockArray2).getDouble(3);
                will(returnValue(dimensionVals2[3]));
                allowing(mockArray2).getSize();
                will(returnValue((long) dimensionVals2.length));

                //We don't want our variables to be downloaded more than once
                oneOf(mockVariable1).read();
                will(returnValue(mockArray1));
                oneOf(mockVariable2).read();
                will(returnValue(mockArray2));

                allowing(mockNetCdfDataset).findVariable("/" + a1.getName());
                will(returnValue(mockVariable1));
                allowing(mockNetCdfDataset).findVariable("/" + a2.getName());
                will(returnValue(mockVariable2));
            }
        });

        HttpRequestBase method = methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset,
                new AbstractViewVariable[] {g1});

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().toString().startsWith(opendapUrl));
        URIBuilder builder = new URIBuilder();
        builder.setParameters(parseQuery(String.format("%1$s[1:2][2:3]", g1.getName())));

        Assert.assertEquals(builder.build().getQuery(),
                method.getURI().getQuery());
    }

    /**
     * Tests a multi constraint request
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test
    public void testMultiConstraint() throws IOException, URISyntaxException {
        OPeNDAPGetDataMethodMaker methodMaker = new OPeNDAPGetDataMethodMaker();
        final String opendapUrl = "http://fake.com/blah.nc";
        final OPeNDAPFormat format = OPeNDAPFormat.ASCII;
        final double[] dimensionVals1 = {0, 1, 2, 3};
        final double[] dimensionVals2 = {-5, -4, -3, -2};

        final SimpleAxis a1 = new SimpleAxis("name1", "FLOAT", "km/h", null, new SimpleBounds(0.7, 2.1));
        final SimpleAxis a2 = new SimpleAxis("name2", "FLOAT", "m^2", null, new SimpleBounds(-3.3, -2));

        context.checking(new Expectations() {
            {
                allowing(mockArray1).getDouble(0);
                will(returnValue(dimensionVals1[0]));
                allowing(mockArray1).getDouble(1);
                will(returnValue(dimensionVals1[1]));
                allowing(mockArray1).getDouble(2);
                will(returnValue(dimensionVals1[2]));
                allowing(mockArray1).getDouble(3);
                will(returnValue(dimensionVals1[3]));
                allowing(mockArray1).getSize();
                will(returnValue((long) dimensionVals1.length));

                allowing(mockArray2).getDouble(0);
                will(returnValue(dimensionVals2[0]));
                allowing(mockArray2).getDouble(1);
                will(returnValue(dimensionVals2[1]));
                allowing(mockArray2).getDouble(2);
                will(returnValue(dimensionVals2[2]));
                allowing(mockArray2).getDouble(3);
                will(returnValue(dimensionVals2[3]));
                allowing(mockArray2).getSize();
                will(returnValue((long) dimensionVals2.length));

                //We don't want our variables to be downloaded more than once
                oneOf(mockVariable1).read();
                will(returnValue(mockArray1));
                oneOf(mockVariable2).read();
                will(returnValue(mockArray2));

                allowing(mockNetCdfDataset).findVariable("/" + a1.getName());
                will(returnValue(mockVariable1));
                allowing(mockNetCdfDataset).findVariable("/" + a2.getName());
                will(returnValue(mockVariable2));
            }
        });

        HttpRequestBase method = methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset,
                new AbstractViewVariable[] {a1, a2});

        Assert.assertNotNull(method);
        Assert.assertTrue(method.getURI().toString().startsWith(opendapUrl));
        URIBuilder builder = new URIBuilder();
        builder.setParameters(parseQuery(String.format("%1$s[1:2],%2$s[2:3]", a1.getName(), a2.getName())));
        Assert.assertEquals((builder.build()).getQuery(),
                method.getURI().getQuery());
    }

    /**
     * Tests that a read error is NOT ignored
     * @throws IOException 
     */
    @Test(expected = IOException.class)
    public void testReadError() throws IOException {
        OPeNDAPGetDataMethodMaker methodMaker = new OPeNDAPGetDataMethodMaker();
        final String opendapUrl = "http://fake.com/blah.nc";
        final OPeNDAPFormat format = OPeNDAPFormat.ASCII;

        //Test minimum bounding box
        final SimpleAxis a1 = new SimpleAxis("name1", "FLOAT", "km/h", null, new SimpleBounds(0.7, 2.1));
        context.checking(new Expectations() {
            {

                //We don't want our variables to be downloaded more than once
                oneOf(mockVariable1).read();
                will(throwException(new IOException()));

                allowing(mockNetCdfDataset).findVariable("/" + a1.getName());
                will(returnValue(mockVariable1));
            }
        });

        methodMaker.getMethod(opendapUrl, format, mockNetCdfDataset, new AbstractViewVariable[] {a1});

    }
}
