package org.auscope.portal;

import java.util.regex.Pattern;

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
    private Pattern urlPattern;
    private Pattern postBodyPattern;
    private String url;
    private String postBody;

    /**
     * Creates a new matcher looking for the specified elements
     * @param type If not null, the type of method to match for
     * @param url If not null the URL pattern to match for
     * @param postBody If not null (and a PostMethod) the pattern of the body of the post to match for
     */
    public HttpMethodBaseMatcher(HttpMethodType type, Pattern url, Pattern postBody) {
        super();
        this.type = type;
        this.urlPattern = url;
        this.postBodyPattern = postBody;
    }

    /**
     * Creates a new matcher looking for the specified elements
     * @param type If not null, the type of method to match for
     * @param url If not null the URL to match for
     * @param postBody If not null (and a PostMethod) the body of the post to match for
     */
    public HttpMethodBaseMatcher(HttpMethodType type, String url, String postBody) {
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
            default:
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

        if (urlPattern != null) {
            try {
                matches &= urlPattern.matcher(method.getURI().toString()).matches();
            } catch (URIException e) {
                Assert.fail();
            }
        }

        if (method instanceof PostMethod) {
            PostMethod postMethod = (PostMethod) method;
            RequestEntity entity = postMethod.getRequestEntity();
            if (entity instanceof StringRequestEntity) {
                String content = ((StringRequestEntity) entity).getContent();
                if (postBody != null) {
                    matches &= postBody.equals(content);
                }

                if (postBodyPattern != null) {
                    matches &= postBodyPattern.matcher(content).matches();
                }
            }
        }
        return matches;
    }
}
