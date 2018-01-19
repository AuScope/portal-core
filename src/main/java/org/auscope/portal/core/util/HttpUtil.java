package org.auscope.portal.core.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

public class HttpUtil {

    public static URI parseURI(String hostUrl, List<NameValuePair> params) throws URISyntaxException {
        //        URI host=new URI(hostUrl);
        //
        //        URIBuilder builder=new URIBuilder(URIUtils.extractHost(host).getHostName());
        URIBuilder builder = new URIBuilder(hostUrl);
        for (NameValuePair param : params) {
            builder.setParameter(param.getName(), param.getValue());
        }

        //We don't want spaces encoded as "+"
        //URIBuilder has no option to avoid this
        //This is our workaround
        URI uri = builder.build();
        try {
            String decodedQuery = URLDecoder.decode(uri.getQuery(), "UTF-8");
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), decodedQuery, uri.getFragment());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("No support for UTF-8", ex);
        }
    }
    
    /**
     * comparing url with a list of filter urls
     *
     * @param url
     *            - the url of the service to query
     * @param filterUrls
     *            - the filter urls
     * @throws MalformedURLException
     */
    public static boolean containHost(String url, String[] filterUrls)
            throws MalformedURLException {
        String urlHost = new URL(url).getHost();
        for (String filterUrl : filterUrls) {
            String filterHost = new URL(filterUrl).getHost();
            if (urlHost.equalsIgnoreCase(filterHost)) {
                return true;
            }
        }
        return false;
    }

}
