package org.auscope.portal;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

/**
 * A JUnit matcher for matching HttpMethodBase objects based on a few simplified terms
 * @author Josh Vote
 *
 */
public class HttpMethodBaseMatcher extends TypeSafeMatcher<HttpMethodBase> {
    /**
     * What different types of HttpMethods we can match for
     */
    public enum HttpMethodType {
        GET,
        POST
    }

    private HttpMethodType type;
    private String url;
    private String postBody;


    /**
     * Creates a new matcher looking for the specified elements
     * @param type If not null, the type of method to match for
     * @param url If not null the URL to match for
     * @param postBody If not null (and a PostMethod) the body of the post to match for
     */
    public HttpMethodBaseMatcher(HttpMethodType type, String url,
            String postBody) {
        super();
        this.type = type;
        this.url = url;
        this.postBody = postBody;
    }
    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("a HttpMethodBase with type='%1$s' url='%2$s' postBody='%3$s'", type, url, postBody));
    }
    @Override
    public boolean matchesSafely(HttpMethodBase method) {
        boolean matches = true;
        if (type != null) {
            switch (type) {
            case GET:
                matches &= method instanceof GetMethod;
                break;
            case POST:
                matches &= method instanceof PostMethod;
                break;
            }
        }

        if (url != null) {
            try {
                matches &= url.equals(method.getURI().toString());
            } catch (URIException e) {
                Assert.fail();
            }
        }

        if (postBody != null && method instanceof PostMethod) {
            PostMethod postMethod = (PostMethod) method;
            RequestEntity entity = postMethod.getRequestEntity();
            if (entity instanceof StringRequestEntity) {
                matches &= postBody.equals(((StringRequestEntity) entity).getContent());
            }
        }

        return matches;
    }


}
