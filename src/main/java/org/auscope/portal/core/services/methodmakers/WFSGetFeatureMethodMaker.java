package org.auscope.portal.core.services.methodmakers;

import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
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
     *
     * @return
     */
    public IterableNamespace getNamespaces() {
        return namespaces;
    }

    /**
     * Sets the list of namespaces that are used when generating WFS requests
     *
     * @param namespaces
     */
    public void setNamespaces(IterableNamespace namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Creates a PostMethod given the following parameters.
     *
     * @param serviceUrl
     *            - required, exception thrown if not provided
     * @param featureType
     *            - required, exception thrown if not provided
     * @param filterString
     *            - optional
     * @param maxFeatures
     *            - Set to non zero to specify a cap on the number of features to fetch
     * @return
     */
    public HttpRequestBase makePostMethod(String serviceUrl, String featureType, String filterString, int maxFeatures) {
        return makePostMethod(serviceUrl, featureType, filterString, maxFeatures, null, null, null, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     *
     * @param serviceUrl
     *            - required, exception thrown if not provided
     * @param featureType
     *            - required, exception thrown if not provided
     * @param filterString
     *            - optional
     * @param resultType
     *            - Can be null - The type of response set you wish to request (default is Results)
     * @return
     */
    public HttpRequestBase makePostMethod(String serviceUrl, String featureType, String filterString,
            ResultType resultType) {
        return makePostMethod(serviceUrl, featureType, filterString, 0, null, resultType, null, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     *
     * @param serviceUrl
     *            - required, exception thrown if not provided
     * @param featureType
     *            - required, exception thrown if not provided
     * @param filterString
     *            - optional
     * @param maxFeatures
     *            - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName
     *            - Can be null or empty
     * @return
     */
    public HttpRequestBase makePostMethod(String serviceUrl, String featureType, String filterString, int maxFeatures,
            String srsName) {
        return makePostMethod(serviceUrl, featureType, filterString, 0, srsName, null, null, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     *
     * @param serviceUrl
     *            - required, exception thrown if not provided
     * @param featureType
     *            - required, exception thrown if not provided
     * @param filterString
     *            - optional - an OGC Filter String
     * @param maxFeatures
     *            - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName
     *            - Can be null or empty
     * @param resultType
     *            - Can be null - The type of response set you wish to request (default is Results)
     * @return
     */
    public HttpRequestBase makePostMethod(String serviceUrl, String featureType, String filterString, int maxFeatures,
            String srsName, ResultType resultType) {
        return makePostMethod(serviceUrl, featureType, filterString, maxFeatures, srsName, resultType, null, null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     *
     * @param serviceUrl
     *            - required, exception thrown if not provided
     * @param featureType
     *            - required, exception thrown if not provided
     * @param filterString
     *            - optional - an OGC Filter String
     * @param maxFeatures
     *            - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName
     *            - Can be null or empty
     * @param resultType
     *            - Can be null - The type of response set you wish to request (default is Results)
     * @param outputFormat
     *            - Can be null - The format you wish the response to take
     * @return
     */
    public HttpRequestBase makePostMethod(String serviceUrl, String featureType, String filterString, int maxFeatures,
            String srsName, ResultType resultType, String outputFormat) {
        return makePostMethod(serviceUrl, featureType, filterString, maxFeatures, srsName, resultType, outputFormat,
                null);
    }

    /**
     * Creates a PostMethod given the following parameters.
     *
     * @param serviceUrl
     *            - required, exception thrown if not provided
     * @param featureType
     *            - required, exception thrown if not provided
     * @param filterString
     *            - optional - an OGC Filter String
     * @param maxFeatures
     *            - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName
     *            - Can be null or empty
     * @param resultType
     *            - Can be null - The type of response set you wish to request (default is Results)
     * @param outputFormat
     *            - Can be null - The format you wish the response to take
     * @param startIndex
     *            - This is for services that supports paging.
     * @return
     */
    public HttpRequestBase makePostMethod(String serviceUrl, String featureType, String filterString, int maxFeatures,
            String srsName, ResultType resultType, String outputFormat, String startIndex) {

        // Make sure the required parameters are given
        if (featureType == null || featureType.equals("")) {
            throw new IllegalArgumentException("featureType parameter can not be null or empty.");
        }

        if (serviceUrl == null || serviceUrl.equals("")) {
            throw new IllegalArgumentException("serviceUrl parameter can not be null or empty.");
        }

        HttpPost httpMethod = new HttpPost(serviceUrl);

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

        if (startIndex != null) {
            sb.append("                startIndex=\"" + startIndex + "\"");
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

        if (srsName != null && ! srsName.isEmpty()) {
            sb.append(" srsName=\""+srsName+"\"");
        }
        sb.append(">");
        if (filterString != null) {
            sb.append(filterString);
        }
        sb.append("  </wfs:Query>\n");
        sb.append("</wfs:GetFeature>");

        log.debug("Service URL:\n\t" + serviceUrl);
        log.debug("Get Feature Query:\n" + sb.toString());

        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        httpMethod.setEntity(new StringEntity(sb.toString(), "UTF-8"));

        return httpMethod;
    }

    /**
     * Generates a method for requesting a specific feature for a specific typeName.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param featureId
     *            [Optional] The ID of typeName to request
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @param outputFormat
     *            [Optional] The format you wish the response to take
     * @param bbox
     *            [Optional] Bounding box used to constrain request
     * @return
     * @throws URISyntaxException
     */
    protected HttpRequestBase makeGetMethod(String serviceUrl, String typeName, String featureId, String cqlFilter,
            Integer maxFeatures, ResultType resultType, String srs, String outputFormat, FilterBoundingBox bbox)
                    throws URISyntaxException {
        HttpGet method = new HttpGet();

        URIBuilder builder = new URIBuilder(serviceUrl);
        builder.setParameter("service", "WFS"); //The access token I am getting after the Login
        builder.setParameter("request", "GetFeature");
        builder.setParameter("typeName", typeName);

        if (featureId != null) {
            builder.setParameter("featureId", featureId);
        }
        if (cqlFilter != null && !cqlFilter.isEmpty()) {
            builder.setParameter("cql_filter", cqlFilter);
        }
        if (maxFeatures != null) {
            builder.setParameter("maxFeatures", maxFeatures.toString());
        }
        if (srs != null) {
            builder.setParameter("srsName", srs);
        }
        if (bbox != null) {
            StringBuilder sb = new StringBuilder();

            //Add lower corner points
            for (double d : bbox.getLowerCornerPoints()) {
                sb.append(d);
                sb.append(',');
            }

            //Add upper corner points
            for (double d : bbox.getUpperCornerPoints()) {
                sb.append(d);
                sb.append(',');
            }

            //And finally add the SRS (if there) otherwise just delete the superfluous ','
            if (sb.length() > 0) {
                if (bbox.getBboxSrs() != null && !bbox.getBboxSrs().isEmpty()) {
                    sb.append(bbox.getBboxSrs());
                } else {
                    sb.deleteCharAt(sb.length() - 1);
                }
            }

            builder.setParameter("bbox", sb.toString());
        }
        if (resultType != null) {
            switch (resultType) {
            case Hits:
                builder.setParameter("resultType", "hits");
                break;
            case Results:
                builder.setParameter("resultType", "results");
                break;
            default:
                throw new IllegalArgumentException("Unknown resultType " + resultType);
            }
        }
        if (outputFormat != null && !outputFormat.isEmpty()) {
            builder.setParameter("outputFormat", outputFormat);
        }

        builder.setParameter("version", WFS_VERSION);

        //attach them to the method
        method.setURI(builder.build());

        return method;
    }

    /**
     * Generates a method for requesting a specific feature for a specific typeName.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param featureId
     *            [Optional] The ID of typeName to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, String featureId, String srs)
            throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, featureId, null, (Integer) null, null, srs, null, null);
    }

    /**
     * Generates a method for requesting a specific feature for a specific typeName.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param featureId
     *            [Optional] The ID of typeName to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @param outputFormat
     *            The output format the response to be parsed in.
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, String featureId, String srs,
            String outputFormat) throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, featureId, null, (Integer) null, null, srs, outputFormat, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, Integer maxFeatures, String srs)
            throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, null, null, maxFeatures, null, srs, null, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @param bbox
     *            [Optional] Bounding box used to constrain request
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, Integer maxFeatures, String srs,
            FilterBoundingBox bbox) throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, null, null, maxFeatures, null, srs, null, bbox);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, Integer maxFeatures,
            ResultType resultType, String srs) throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, null, null, maxFeatures, resultType, srs, null, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @param bbox
     *            [Optional] Bounding box used to constrain request
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, Integer maxFeatures,
            ResultType resultType, String srs, FilterBoundingBox bbox) throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, null, null, maxFeatures, resultType, srs, null, bbox);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @param bbox
     *            [Optional] Bounding box used to constrain request
     * @param outputFormat
     *            [Optional] The output format the response should takew
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, Integer maxFeatures,
            ResultType resultType, String srs, FilterBoundingBox bbox, String outputFormat) throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, null, null, maxFeatures, resultType, srs, outputFormat,
                bbox);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type that pass a CQL filter.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param cqlFilter
     *            A CQL filter string (not an OGC filter).
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, String cqlFilter, Integer maxFeatures,
            String srs) throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, null, cqlFilter, maxFeatures, null, srs, null, null);
    }

    /**
     * Generates a method for requesting all instances of a specific feature type that pass a CQL filter.
     *
     * @param serviceUrl
     *            The WFS endpoint
     * @param typeName
     *            The typeName to query
     * @param maxFeatures
     *            [Optional] The maximum number of features to request
     * @param cqlFilter
     *            A CQL filter string (not an OGC filter).
     * @param srs
     *            [Optional] The spatial reference system the response should conform to
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, String typeName, String cqlFilter, Integer maxFeatures,
            ResultType resultType, String srs) throws URISyntaxException {
        return makeGetMethod(serviceUrl, typeName, null, cqlFilter, maxFeatures, resultType, srs, null, null);
    }

    /**
     * Generates a method for requesting a WFS GetCapabilities response
     *
     * @param serviceUrl
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetCapabilitiesMethod(String serviceUrl) throws URISyntaxException {
        HttpGet method = new HttpGet();

        URIBuilder builder = new URIBuilder(serviceUrl);
        builder.setParameter("service", "WFS");
        builder.setParameter("request", "GetCapabilities");
        builder.setParameter("version", WFS_VERSION);

        //attach them to the method
        method.setURI(builder.build());

        return method;
    }

    /**
     * Generates a method for requesting a WFS DescribeFeatureType response
     *
     * @param serviceUrl
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeDescribeFeatureTypeMethod(String serviceUrl, String featureType)
            throws URISyntaxException {
        HttpGet method = new HttpGet();

        URIBuilder builder = new URIBuilder(serviceUrl);
        builder.setParameter("service", "WFS");
        builder.setParameter("request", "DescribeFeatureType");
        builder.setParameter("version", WFS_VERSION);
        builder.setParameter("typeName", featureType);

        //attach them to the method
        method.setURI(builder.build());

        return method;
    }
}
