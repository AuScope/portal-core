package org.auscope.portal.server.web;

import java.util.ArrayList;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
/**
 * A class for generating Web Feature Service requests.
 *
 * @author Josh Vote
 */
@Repository
public class WFSGetFeatureMethodMaker {
    public static final String WFS_VERSION = "1.1.0";

    /** Log object for this class. */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Creates a PostMethod given the following parameters.
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @param maxFeatures - Set to non zero to specify a cap on the number of features to fetch
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString, int maxFeatures) throws Exception {
        return makeMethod(serviceURL, featureType, filterString, maxFeatures, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @param maxFeatures - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName - Can be null or empty
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString, int maxFeatures, String srsName) throws Exception {

        // Make sure the required parameters are given
        if (featureType == null || featureType.equals("")) {
            throw new Exception("featureType parameter can not be null or empty.");
        }

        if (serviceURL == null || serviceURL.equals("")) {
            throw new Exception("serviceURL parameter can not be null or empty.");
        }

        PostMethod httpMethod = new PostMethod(serviceURL);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(String.format("<wfs:GetFeature service=\"WFS\" version=\"%1$s\"\n", WFS_VERSION));
        sb.append("                xmlns:wfs=\"http://www.opengis.net/wfs\"\n");
        sb.append("                xmlns:ogc=\"http://www.opengis.net/ogc\"\n");
        sb.append("                xmlns:gml=\"http://www.opengis.net/gml\"\n");
        sb.append("                xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\"\n");
        sb.append("                xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"\n");
        if (maxFeatures > 0) {
            sb.append("                maxFeatures=\"" + Integer.toString(maxFeatures) + "\"");
        }

        sb.append(">\n");
        sb.append("  <wfs:Query typeName=\"" + featureType + "\"");
        if (srsName != null && !srsName.isEmpty()) {
            sb.append(" srsName=\"" + srsName + "\"");
        } else if (featureType.equals("gsml:Borehole")) {
            sb.append(" srsName=\"" + "EPSG:4326" + "\"");
        }
        sb.append(">\n");
        sb.append(filterString);
        sb.append("  </wfs:Query>\n");
        sb.append("</wfs:GetFeature>");

        log.debug("Service URL:\n\t" + serviceURL);
        log.debug("Get Feature Query:\n" + sb.toString());

        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        httpMethod.setRequestEntity(new StringRequestEntity(sb.toString(), null, null));


        return httpMethod;
    }

    /**
     * Generates a method for requesting a specific feature for a specific typeName.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param featureId [Optional] The ID of typeName to request
     * @param maxFeatures [Optional] The maximum number of features to request
     * @return
     */
    private HttpMethodBase makeMethod(String serviceUrl, String typeName, String featureId, Integer maxFeatures) {
        GetMethod method = new GetMethod(serviceUrl);

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("request", "GetFeature"));
        valuePairs.add(new NameValuePair("typeName", typeName));
        if (featureId != null) {
            valuePairs.add(new NameValuePair("featureId", featureId));
        }
        if (maxFeatures != null) {
            valuePairs.add(new NameValuePair("maxFeatures", maxFeatures.toString()));
        }
        valuePairs.add(new NameValuePair("version", WFS_VERSION));

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }

    /**
     * Generates a method for requesting a specific feature for a specific typeName.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param featureId [Optional] The ID of typeName to request
     * @return
     */
    public HttpMethodBase makeMethod(String serviceUrl, String typeName, String featureId) {
        return makeMethod(serviceUrl, typeName, featureId, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param maxFeatures [Optional] The maximum number of features to request
     * @return
     */
    public HttpMethodBase makeMethod(String serviceUrl, String typeName, Integer maxFeatures) {
        return makeMethod(serviceUrl, typeName, null, maxFeatures);
    }
}
