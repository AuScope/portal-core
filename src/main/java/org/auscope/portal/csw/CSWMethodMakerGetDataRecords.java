package org.auscope.portal.csw;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * User: Mathew Wyatt
 * Date: 02/07/2009
 * Time: 12:12:34 PM
 */
public class CSWMethodMakerGetDataRecords implements ICSWMethodMaker {
    private String serviceURL;

    public CSWMethodMakerGetDataRecords(String serviceURL) throws Exception {
        this.serviceURL = serviceURL;

        //pretty hard to do updateCSWRecords GetFeature query with updateCSWRecords serviceURL, so we had better check that we have one
        if(serviceURL == null || serviceURL.equals(""))
            throw new Exception("serviceURL parameter can not be null or empty.");
    }

    public HttpMethodBase makeMethod() {
        GetMethod method = new GetMethod(this.serviceURL);

        //set all of the parameters
        NameValuePair service = new NameValuePair("service", "CSW");
        NameValuePair version = new NameValuePair("constraint_language_version", "1.1.0");
        NameValuePair request = new NameValuePair("request", "GetRecords");
        NameValuePair outputSchema = new NameValuePair("outputSchema", "csw:IsoRecord");
        NameValuePair constraintLanguage = new NameValuePair("constraintLanguage", "FILTER");
        NameValuePair maxRecords = new NameValuePair("maxRecords", "1000");
        NameValuePair typeNames = new NameValuePair("typeNames", "csw:Record");
        NameValuePair resultType = new NameValuePair("resultType", "results");
        NameValuePair namespace = new NameValuePair("namespace", "csw:http://www.opengis.net/cat/csw");
        NameValuePair elementSet = new NameValuePair("elementSetName", "full");
        //NameValuePair constraint = new NameValuePair("constraint", filterString);

        //attach them to the method
        method.setQueryString(new NameValuePair[]{service, version, request, outputSchema, constraintLanguage, maxRecords, typeNames, resultType, namespace, elementSet});

        return method;
    }
}
