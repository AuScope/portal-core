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
 * abstract implementation of org.apache.http.client.ResponseHandler that provides
 * basic checking of responses so that derivative classes can focus on generating their responses
 * @author Josh Vote
 *
 * @param <T>
 */
public abstract class BaseResponseHandler<T> implements ResponseHandler<T> {

    /**
     * Expected to just generate an appropriate response object. response will have
     * already been checked for HttpErrors and the like.
     *
     * There is no need to close entity before returning this function, it will be handled at a higher level.
     *
     * @param response The response to check
     * @param entity The http entity (extracted during prior testing)
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected abstract T generateResponse(HttpResponse response, HttpEntity entity) throws ClientProtocolException, IOException;

    @Override
    public T handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {

        //Do our response checking common to all requests
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }

        try {
            //Derivative classes will handle the actual response
            return generateResponse(response, entity);
        } finally {
            //Always ensure we are done with the response
            EntityUtils.consume(entity);
        }
    }

}
