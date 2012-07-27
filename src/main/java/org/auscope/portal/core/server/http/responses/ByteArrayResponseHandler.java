package org.auscope.portal.core.server.http.responses;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * A responseh handler for turning a HTTP response into an array of bytes
 * @author Josh Vote
 *
 */
public class ByteArrayResponseHandler extends BaseResponseHandler<byte[]> {

    /**
     * See parent class for documentation.
     */
    @Override
    protected byte[] generateResponse(HttpResponse response, HttpEntity entity)
            throws ClientProtocolException, IOException {
        return entity == null ? null : EntityUtils.toByteArray(entity);
    }



}
