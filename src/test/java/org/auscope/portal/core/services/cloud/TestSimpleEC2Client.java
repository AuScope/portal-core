package org.auscope.portal.core.services.cloud;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.SimpleEC2Client.InstanceInitiatedShutdownBehaviour;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Simple EC2 client
 * @author Josh Vote (CSIRO)
 *
 */
public class TestSimpleEC2Client extends PortalTestClass {
    private SimpleEC2Client client;
    private HttpServiceCaller httpService = context.mock(HttpServiceCaller.class);
    private static final String ACCESS_KEY_EXAMPLE = "AKIDEXAMPLE";
    private static final String SECRET_KEY_EXAMPLE = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
    private static final String REGION = "us-east-1";
    private Date date;

    @Before
    public void setup() throws ParseException {
        client = new SimpleEC2Client(httpService, ACCESS_KEY_EXAMPLE, SECRET_KEY_EXAMPLE, REGION);
        client.setService("iam"); //All of Amazon's examples are based around IAM so this is what we validate against

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = df.parse("20150830T123600Z");
    }

    @Test
    public void testCanonicalRequest() throws Exception {
        URIBuilder builder = new URIBuilder("http://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08");
        String actualCanonicalRequest = client.generateCanonicalRequest(builder.build(), date, "");
        String expectedCanoncialRequest =
                "GET\n" +
                "/\n" +
                "Action=ListUsers&Version=2010-05-08\n" +
                "content-type:application/x-www-form-urlencoded; charset=utf-8\n" +
                "host:iam.amazonaws.com\n" +
                "x-amz-date:20150830T123600Z\n" +
                "\n" +
                "content-type;host;x-amz-date\n" +
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        Assert.assertEquals(expectedCanoncialRequest, actualCanonicalRequest);
    }

    @Test
    public void testCanonicalRequestOrderedParams() throws Exception {
        URIBuilder builder = new URIBuilder("http://iam.amazonaws.com/?Version=2010-05-08&Action=ListUsers&Foo=bar%2Fbaz");
        String actualCanonicalRequest = client.generateCanonicalRequest(builder.build(), date, "");
        String expectedCanoncialRequest =
                "GET\n" +
                "/\n" +
                "Action=ListUsers&Foo=bar%2Fbaz&Version=2010-05-08\n" +
                "content-type:application/x-www-form-urlencoded; charset=utf-8\n" +
                "host:iam.amazonaws.com\n" +
                "x-amz-date:20150830T123600Z\n" +
                "\n" +
                "content-type;host;x-amz-date\n" +
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        Assert.assertEquals(expectedCanoncialRequest, actualCanonicalRequest);
    }

    @Test
    public void testStringToSign() throws Exception {
        String stringToSign = client.generateStringToSign(date, "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59");
        String expectedString =
                "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "20150830/us-east-1/iam/aws4_request\n" +
                "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";
        Assert.assertEquals(expectedString, stringToSign);
    }

    @Test
    public void testGenerateSignature() throws Exception {
        String stringToSign =
                "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "20150830/us-east-1/iam/aws4_request\n" +
                "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";
        String signature = client.generateSignature(stringToSign, date);
        String expectedSignature = "5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";
        Assert.assertEquals(expectedSignature, signature);
    }

    @Test
    public void testGenerateAuthHeader() throws Exception {
        String signature = client.generateAuthorizationHeader("5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7", date);
        String expectedSignature = "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";
        Assert.assertEquals(expectedSignature, signature);
    }

    @Test
    public void testSetInstanceInitiatedShutdown() throws Exception {
        final InputStream response = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/aws/modifyinstanceattribute-response-success.xml");

        context.checking(new Expectations() {{
            oneOf(httpService).getMethodResponseAsStream(with(any(HttpRequestBase.class)));will(returnValue(response));
        }});

        client.setInstanceInitiatedShutdownBehaviour("region/foo", InstanceInitiatedShutdownBehaviour.Terminate);
    }

    @Test(expected=PortalServiceException.class)
    public void testSetInstanceInitiatedShutdownFailure() throws Exception {
        final InputStream response = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/aws/modifyinstanceattribute-response-failure.xml");

        context.checking(new Expectations() {{
            oneOf(httpService).getMethodResponseAsStream(with(any(HttpRequestBase.class)));will(returnValue(response));
        }});

        client.setInstanceInitiatedShutdownBehaviour("region/foo", InstanceInitiatedShutdownBehaviour.Terminate);
    }

    @Test
    public void testGetConsoleOutput() throws Exception {
        final InputStream response = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/aws/getconsoleoutput-response-success.xml");

        context.checking(new Expectations() {{
            oneOf(httpService).getMethodResponseAsStream(with(any(HttpRequestBase.class)));will(returnValue(response));
        }});

        String console = client.getConsoleOutput("region/foo");
        String expected = "Linux version 2.6.16-xenU (builder@patchbat.amazonsa) (gcc version 4.0.1 20050727 (Red Hat 4.0.1-5)) #1 SMP Thu Oct 26 08:41:26 SAST 2006\n" +
                            "BIOS-provided physical RAM map:\n" +
                            "Xen: 0000000000000000 - 000000006a400000 (usable)\n" +
                            "980MB HIGHMEM available.\n" +
                            "727MB LOWMEM available.\n" +
                            "NX (Execute Disable) protection: active\n" +
                            "IRQ lockup detection disabled\n" +
                            "Built 1 zonelists\n" +
                            "Kernel command line: root=/dev/sda1 ro 4\n" +
                            "Enabling fast FPU save and restore... done.\n";

        Assert.assertEquals(expected, console);
    }
}
