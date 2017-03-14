package org.auscope.portal.core.services;

import java.io.IOException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Test;

public class TestCSWService extends PortalTestClass {

    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);

    /**
     * Test that queryCSWEndpoint does multiple attempts to connect.
     * @throws IOException 
     * @throws OWSException 
     */
    @Test(expected = java.io.IOException.class)
    public void testQueryCSWEndpoint() throws IOException, OWSException {
        CSWService cs = new CSWService(new CSWServiceItem("test", "http://example.org"), httpServiceCaller, true);
        final java.io.IOException e = new java.io.IOException("test exception");
        context.checking(new Expectations() {
            {
                exactly(2).of(httpServiceCaller).getMethodResponseAsStream(with(any(HttpRequestBase.class)));
                will(throwException(e));

            }
        });

        cs.queryCSWEndpoint(0, 100, 2, 1000);
    }

}
