package org.auscope.portal.core.server.http.responses;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.FileIOUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Handles turning a Http response (containing XML) into a W3C DOM Document.
 *
 * Unparsable XML will generate IOExceptions
 *
 * @author Josh Vote
 *
 */
public class DomResponseHandler extends BaseResponseHandler<Document> {

    @Override
    protected Document generateResponse(HttpResponse response, HttpEntity entity)
            throws ClientProtocolException, IOException {
        if (entity == null) {
            return null;
        }

        InputStream contentStream = entity.getContent();
        try {
            return DOMUtil.buildDomFromStream(contentStream);
        } catch (ParserConfigurationException e) {
            throw new IOException("Error building DOM from response content", e);
        } catch (SAXException e) {
            throw new IOException("Error building DOM from response content", e);
        } finally {
            FileIOUtil.closeQuietly(contentStream);
        }
    }

}
