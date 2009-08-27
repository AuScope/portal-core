package org.auscope.portal.server.web;

import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.HttpMethodBase;

/**
 * User: Mathew Wyatt
 * Date: 01/07/2009
 * Time: 3:11:28 PM
 */
public class WFSGetFeatureMethodMakerPOST implements IWFSGetFeatureMethodMaker {
    /**
     * Creates a PostMethod given the following parameters
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @return
     * @throws Exception
     */
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString) throws Exception {
        //pretty hard to do updateCSWRecords GetFeature query with updateCSWRecords featureType, so we had better check that we have one
        if(featureType == null || featureType.equals(""))
            throw new Exception("featureType parameter can not be null or empty.");

        //pretty hard to do updateCSWRecords GetFeature query with updateCSWRecords serviceURL, so we had better check that we have one
        if(serviceURL == null || serviceURL.equals(""))
            throw new Exception("serviceURL parameter can not be null or empty.");

        //create updateCSWRecords method
        PostMethod method = new PostMethod(serviceURL);

        //TODO: remove the mo namespace and have it passed in as updateCSWRecords parameter
        String postString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wfs:GetFeature version=\"1.1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" maxFeatures=\"200\">\n" +
                "    <wfs:Query typeName=\""+featureType+"\">" +
                        filterString +
                "    </wfs:Query>" +
                "</wfs:GetFeature>";

        method.setRequestEntity(new StringRequestEntity(postString));

        return method;
    }
}
