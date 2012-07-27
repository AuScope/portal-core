package org.auscope.portal.core.server.http.responses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.auscope.portal.core.util.FileIOUtil;

/**
 * A response handler that pipes it's response bytes into a predetermined OutputStream.
 *
 * Only the count of bytes copied is returned
 * @author Josh Vote
 *
 */
public class PipingResponseHandler extends BaseResponseHandler<Integer> {

    private OutputStream os;

    /**
     * Creates a new instance which will handle responses by writing to os
     * @param os The stream to receive raw response data
     */
    public PipingResponseHandler(OutputStream os) {
        this.os = os;
    }

    /**
     * See parent class.
     */
    @Override
    protected Integer generateResponse(HttpResponse response, HttpEntity entity)
            throws ClientProtocolException, IOException {

        InputStream contentStream = entity.getContent();

        try {
            return new Integer(IOUtils.copy(contentStream, os));
        } finally {
            FileIOUtil.closeQuietly(contentStream);
        }
    }

}
