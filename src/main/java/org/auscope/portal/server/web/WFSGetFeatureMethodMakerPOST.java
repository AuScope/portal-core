package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: Mathew Wyatt
 * 
 * @version $Id$
 */
public class WFSGetFeatureMethodMakerPOST implements IWFSGetFeatureMethodMaker {
    
    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());

    
    /**
     * Creates a PostMethod given the following parameters
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString) throws Exception {

        // Make sure the required parameters are given
        if (featureType == null || featureType.equals(""))
            throw new Exception("featureType parameter can not be null or empty.");

        if (serviceURL == null || serviceURL.equals(""))
            throw new Exception("serviceURL parameter can not be null or empty.");

        PostMethod httpMethod = new PostMethod(serviceURL);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\"\n");
        sb.append("                xmlns:ogc=\"http://www.opengis.net/ogc\"\n");
        sb.append("                xmlns:gml=\"http://www.opengis.net/gml\"\n");        
        sb.append("                version=\"1.1.0\" maxFeatures=\"200\">\n");
        sb.append("    <wfs:Query typeName=\""+featureType+"\">\n");
        sb.append(filterString);
        sb.append("    </wfs:Query>\n");
        sb.append("</wfs:GetFeature>");
        
        // TODO: remove the er namespace and have it passed in as a parameter
        String postString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wfs:GetFeature version=\"1.1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" maxFeatures=\"200\">\n" +
                "    <wfs:Query typeName=\""+featureType+"\">" +
                        filterString +
                "    </wfs:Query>" +
                "</wfs:GetFeature>";
 
        log.debug("Get Feature Query:\n" + sb.toString());
        
        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        httpMethod.setRequestEntity(new StringRequestEntity(sb.toString(),null,null));

        return httpMethod;
    }
}
