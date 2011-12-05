package org.auscope.portal.server.web.controllers;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.server.domain.wfs.WFSHTMLResponse;
import org.auscope.portal.server.util.ByteBufferedServletOutputStream;
import org.auscope.portal.server.web.service.WFSService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for WFSPopupController
 * @author Josh Vote
 *
 */
public class TestWFSPopupController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private WFSService mockWfsService = context.mock(WFSService.class);
    private HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
    private HttpSession mockSession = context.mock(HttpSession.class);
    private ServletContext mockServletContext = context.mock(ServletContext.class);;
    private InputStream mockInputStream = context.mock(InputStream.class);
    private WFSPopupController popupController;


    @Before
    public void init() {
        popupController = new WFSPopupController(mockWfsService);
    }

    /**
     * Test wfsFeaturePopup will all optional parameters specified
     * @throws Exception
     */
    @Test
    public void testWFSFeaturePopup() throws Exception {
        final String serviceUrl = "http://example.com/wfs";
        final String typeName = "wfs:typeName";
        final String featureId = "idString";
        final String wfsToHtmlUrl = "/WEB-INF/xsl/WfsToHtml.xsl";
        final String convertedData = "gmlToKMLResult";
        final String wfsResponse = "wfsResponseString";
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(convertedData.getBytes().length);

        context.checking(new Expectations() {{

            oneOf(mockMethod).getURI();will(returnValue(new URI(serviceUrl, false)));

            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(wfsToHtmlUrl);will(returnValue(mockInputStream));

            allowing(mockResponse).setContentType(with(any(String.class)));

            oneOf(mockWfsService).getWfsResponseAsHtml(serviceUrl, typeName, featureId);will(returnValue(new WFSHTMLResponse(wfsResponse, convertedData, mockMethod)));

            oneOf(mockResponse).getOutputStream();will(returnValue(outputStream));
        }});

        popupController.wfsFeaturePopup(mockResponse, serviceUrl, typeName, featureId);

        Assert.assertArrayEquals(convertedData.getBytes(), outputStream.toByteArray());
    }

    /**
     * Tests wfsFeaturePopup with only a URL
     * @throws Exception
     */
    @Test
    public void testWFSFeaturePopupUrlOnly() throws Exception {
        final String serviceUrl = "http://example.com/wfs";
        final String typeName = null;
        final String featureId = null;
        final String wfsToHtmlUrl = "/WEB-INF/xsl/WfsToHtml.xsl";
        final String convertedData = "gmlToKMLResult";
        final String wfsResponse = "wfsResponseString";
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(convertedData.getBytes().length);

        context.checking(new Expectations() {{

            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(wfsToHtmlUrl);will(returnValue(mockInputStream));

            allowing(mockResponse).setContentType(with(any(String.class)));

            oneOf(mockWfsService).getWfsResponseAsHtml(serviceUrl);will(returnValue(new WFSHTMLResponse(wfsResponse, convertedData, mockMethod)));

            oneOf(mockResponse).getOutputStream();will(returnValue(outputStream));
        }});

        popupController.wfsFeaturePopup(mockResponse, serviceUrl, typeName, featureId);

        Assert.assertArrayEquals(convertedData.getBytes(), outputStream.toByteArray());
    }
}
