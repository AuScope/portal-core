package org.auscope.portal.core.server.http.responses;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

/**
 * Similar to BasicResponseHandler but designed to take advantage of BaseResponseHandler
 * functionality. This class turns a HTTP response into a string
 *
 * @author Josh Vote
 */
public class StringResponseHandler extends BaseResponseHandler<String> {
    @Override
    protected String generateResponse(HttpResponse response, HttpEntity entity)
            throws ClientProtocolException, IOException {
        return entity == null ? null : EntityUtils.toString(entity);
    }
}
