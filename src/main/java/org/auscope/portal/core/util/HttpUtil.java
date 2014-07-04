package org.auscope.portal.core.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;

public class HttpUtil {

    public static URI parseURI(String hostUrl,List<NameValuePair> params) throws URISyntaxException{
//        URI host=new URI(hostUrl);
//
//        URIBuilder builder=new URIBuilder(URIUtils.extractHost(host).getHostName());
        URIBuilder builder=new URIBuilder(hostUrl);
        for(NameValuePair param:params){
            builder.setParameter(param.getName(), param.getValue());
        }
        return builder.build();
    }


    /**
     * comparing url with a list of filter urls
     *
     * @param url - the url of the service to query
     * @param filterUrls - the filter urls
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
