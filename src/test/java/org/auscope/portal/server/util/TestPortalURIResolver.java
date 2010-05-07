package org.auscope.portal.server.util;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * User: Mathew Wyatt
 * Date: 28/08/2009
 * @version $Id$
 */
public class TestPortalURIResolver {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Portal URI resolver to test
     */
    private PortalURIResolver portalURIResolver;

    /**
     * Mick servlet context
     */
    private ServletContext servletContext = context.mock(ServletContext.class);

    @Before
    public void setup() {
        portalURIResolver = new PortalURIResolver(servletContext);
    }

    /**
     * Test the caching mechanism
     * @throws TransformerException
     */
    @Test
    public void testCached() throws TransformerException {
        final InputStream mockStream = context.mock(InputStream.class);

        //we test the caching by seeing if the getResourceAsStream is called only once
        context.checking(new Expectations() {{
            oneOf(servletContext).getResourceAsStream(with(any(String.class)));will(returnValue(mockStream));
        }});

        Source source = portalURIResolver.resolve("href", "");
        Source source2 = portalURIResolver.resolve("href", "");

        //the two should be the same
        if(source == source2)
            Assert.assertTrue(true);
    }

    /**
     * Test when things happen smoothly
     * @throws TransformerException
     */
    @Test
    public void testNotCached() throws TransformerException {
        final InputStream mockStream = context.mock(InputStream.class);

        context.checking(new Expectations() {{
            oneOf(servletContext).getResourceAsStream(with(any(String.class)));will(returnValue(mockStream));
        }});

        Source source = portalURIResolver.resolve("", "");

        //we should get a valid source back
        Assert.assertNotNull(source);
    }

    //todo: implement exception test
    /**
     * Test the cae of an exception occureing during the loading of the Source
     * @throws TransformerException
     */
    /*@Test
    public void testException() throws TransformerException {

        context.checking(new Expectations() {{
            oneOf(servletContext).getResourceAsStream(with(any(String.class)));will(throwException(new Throwable()));
        }});

        Source source = portalURIResolver.resolve("", "");

        //expect a null source
        if(source == null)
            Assert.assertTrue(true);

    }*/
}

