package org.auscope.portal.core.services.methodmakers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.namespaces.IterableNamespace;
import org.auscope.portal.core.services.namespaces.WFSNamespaceContext;
/**
 * A class for generating Web Feature Service requests.
 *
 * @author Josh Vote
 */
public class WFSGetFeatureMethodMaker extends AbstractMethodMaker {
    public static final String WFS_VERSION = "1.1.0";


    /**
     * An enumeration of the values that can be used for the 'resultType' parameter
     *
     */
    public enum ResultType {
        /**
         * Requests the full set of results be returned
         */
        Results,
        /**
         * Requests that only the count of the results be returned
         */
        Hits
    }

    /** Log object for this class. */
    private final Log log = LogFactory.getLog(getClass());
    /** Namespaces which form the basis of WFS requests */
    protected IterableNamespace namespaces;

    /**
     * Creates a new instance of this class configured with org.auscope.portal.core.services.namespaces.WFSNamespaceContext
     */
    public WFSGetFeatureMethodMaker() {
        this.namespaces = new WFSNamespaceContext();
    }

    /**
     * Gets the list of namespaces that are used when generating WFS requests
     * @return
     */
    public IterableNamespace getNamespaces() {
        return namespaces;
    }

    /**
     * Sets the list of namespaces that are used when generating WFS requests
     * @param namespaces
     */
    public void setNamespaces(IterableNamespace namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Creates a PostMethod given the following parameters.
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @param maxFeatures - Set to non zero to specify a cap on the number of features to fetch
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public HttpMethodBase makePostMethod(String serviceURL, String featureType, String filterString, int maxFeatures) throws Exception {
        return makePostMethod(serviceURL, featureType, filterString, maxFeatures, null, null, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @param resultType - Can be null - The type of response set you wish to request (default is Results)
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public HttpMethodBase makePostMethod(String serviceURL, String featureType, String filterString, ResultType resultType) throws Exception {
        return makePostMethod(serviceURL, featureType, filterString, 0, null, resultType, null);
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
    public HttpMethodBase makePostMethod(String serviceURL, String featureType, String filterString, int maxFeatures, String srsName) {
        return makePostMethod(serviceURL, featureType, filterString, 0, srsName, null, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional - an OGC Filter String
     * @param maxFeatures - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName - Can be null or empty
     * @param resultType - Can be null - The type of response set you wish to request (default is Results)
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public HttpMethodBase makePostMethod(String serviceURL, String featureType, String filterString, int maxFeatures, String srsName, ResultType resultType) {
        return makePostMethod(serviceURL, featureType, filterString, maxFeatures, srsName, resultType, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional - an OGC Filter String
     * @param maxFeatures - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName - Can be null or empty
     * @param resultType - Can be null - The type of response set you wish to request (default is Results)
     * @param outputFormat - Can be null - The format you wish the response to take
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public HttpMethodBase makePostMethod(String serviceURL, String featureType, String filterString, int maxFeatures, String srsName, ResultType resultType, String outputFormat) {

        // Make sure the required parameters are given
        if (featureType == null || featureType.equals("")) {
            throw new IllegalArgumentException("featureType parameter can not be null or empty.");
        }

        if (serviceURL == null || serviceURL.equals("")) {
            throw new IllegalArgumentException("serviceURL parameter can not be null or empty.");
        }

        PostMethod httpMethod = new PostMethod(serviceURL);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(String.format("<wfs:GetFeature service=\"WFS\" version=\"%1$s\"\n", WFS_VERSION));

        Iterator<String> prefixes = namespaces.getPrefixIterator();
        while (prefixes.hasNext()) {
            String prefix = prefixes.next();
            String namespace = namespaces.getNamespaceURI(prefix);

            sb.append(String.format("                xmlns:%1$s=\"%2$s\"\n", prefix, namespace));
        }
        if (maxFeatures > 0) {
            sb.append("                maxFeatures=\"" + Integer.toString(maxFeatures) + "\"");
        }
        if (resultType != null) {
            switch (resultType) {
            case Hits:
                sb.append("                resultType=\"hits\"\n");
                break;
            case Results:
                sb.append("                resultType=\"results\"\n");
                break;
            default:
                throw new IllegalArgumentException("Unknown resultType " + resultType);
            }
        }
        if (outputFormat != null && !outputFormat.isEmpty()) {
            sb.append("                outputFormat=\"" + outputFormat + "\"");
        }

        sb.append(">\n");
        sb.append("  <wfs:Query typeName=\"" + featureType + "\"");
        if (srsName != null && !srsName.isEmpty()) {
            sb.append(" srsName=\"" + srsName + "\"");
        } else if (featureType.equals("gsml:Borehole")) {
            sb.append(" srsName=\"" + "EPSG:4326" + "\"");
        }
        sb.append(">\n");
        if (filterString != null) {
            sb.append(filterString);
        }
        sb.append("  </wfs:Query>\n");
        sb.append("</wfs:GetFeature>");

        log.debug("Service URL:\n\t" + serviceURL);
        log.debug("Get Feature Query:\n" + sb.toString());

        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        try {
            httpMethod.setRequestEntity(new StringRequestEntity(sb.toString(), null, null));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding", e);
        }


        return httpMethod;
    }

    /**
     * Generates a method for requesting a specific feature for a specific typeName.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param featureId [Optional] The ID of typeName to request
     * @param maxFeatures [Optional] The maximum number of features to request
     * @param srs [Optional] The spatial reference system the response should conform to
     * @param outputFormat [Optional] The format you wish the response to take
     * @return
     */
    protected HttpMethodBase makeGetMethod(String serviceUrl, String typeName, String featureId, String cqlFilter, Integer maxFeatures, ResultType resultType, String srs, String outputFormat) {
        GetMethod method = new GetMethod(serviceUrl);

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("service", "WFS"));
        valuePairs.add(new NameValuePair("request", "GetFeature"));
        valuePairs.add(new NameValuePair("typeName", typeName));
        if (featureId != null) {
            valuePairs.add(new NameValuePair("featureId", featureId));
        }
        if (cqlFilter != null && !cqlFilter.isEmpty()) {
            valuePairs.add(new NameValuePair("cql_filter", cqlFilter));
        }
        if (maxFeatures != null) {
            valuePairs.add(new NameValuePair("maxFeatures", maxFeatures.toString()));
        }
        if (srs != null) {
            valuePairs.add(new NameValuePair("srsName", srs));
        }
        if (resultType != null) {
            switch (resultType) {
            case Hits:
                valuePairs.add(new NameValuePair("resultType", "hits"));
                break;
            case Results:
                valuePairs.add(new NameValuePair("resultType", "results"));
                break;
            default:
                throw new IllegalArgumentException("Unknown resultType " + resultType);
            }
        }
        if (outputFormat != null && !outputFormat.isEmpty()) {
            valuePairs.add(new NameValuePair("outputFormat", outputFormat));
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
     * @param srs [Optional] The spatial reference system the response should conform to
     * @return
     */
    public HttpMethodBase makeGetMethod(String serviceUrl, String typeName, String featureId, String srs) {
        return makeGetMethod(serviceUrl, typeName, featureId, null, (Integer) null, null, srs, null);
    }

    /**
     * Generates a method for requesting a specific feature for a specific typeName.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param featureId [Optional] The ID of typeName to request
     * @param srs [Optional] The spatial reference system the response should conform to
     * @return
     */
    public HttpMethodBase makeGetMethod(String serviceUrl, String typeName, String featureId, String srs, String outputFormat) {
        return makeGetMethod(serviceUrl, typeName, featureId, null, (Integer) null, null, srs, outputFormat);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param maxFeatures [Optional] The maximum number of features to request
     * @param srs [Optional] The spatial reference system the response should conform to
     * @return
     */
    public HttpMethodBase makeGetMethod(String serviceUrl, String typeName, Integer maxFeatures, String srs) {
        return makeGetMethod(serviceUrl, typeName, null, null, (Integer) maxFeatures, null, srs, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param maxFeatures [Optional] The maximum number of features to request
     * @param srs [Optional] The spatial reference system the response should conform to
     * @return
     */
    public HttpMethodBase makeGetMethod(String serviceUrl, String typeName, Integer maxFeatures, ResultType resultType, String srs) {
        return makeGetMethod(serviceUrl, typeName, null, null, (Integer) maxFeatures, resultType, srs, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type that pass a CQL filter.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param maxFeatures [Optional] The maximum number of features to request
     * @param cqlFilter A CQL filter string (not an OGC filter).
     * @param srs [Optional] The spatial reference system the response should conform to
     * @return
     */
    public HttpMethodBase makeGetMethod(String serviceUrl, String typeName, String cqlFilter, Integer maxFeatures, String srs) {
        return makeGetMethod(serviceUrl, typeName, null, cqlFilter, (Integer) maxFeatures, null, srs, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type that pass a CQL filter.
     * @param serviceUrl The WFS endpoint
     * @param typeName The typeName to query
     * @param maxFeatures [Optional] The maximum number of features to request
     * @param cqlFilter A CQL filter string (not an OGC filter).
     * @param srs [Optional] The spatial reference system the response should conform to
     * @return
     */
    public HttpMethodBase makeGetMethod(String serviceUrl, String typeName, String cqlFilter, Integer maxFeatures, ResultType resultType, String srs) {
        return makeGetMethod(serviceUrl, typeName, null, cqlFilter, (Integer) maxFeatures, resultType, srs, null);
    }
}
