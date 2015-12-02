package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker.OPeNDAPFormat;
import org.auscope.portal.core.services.responses.opendap.AbstractViewVariable;
import org.auscope.portal.core.services.responses.opendap.ViewVariableFactory;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * Service class for interacting with an OPeNDAP endpoint
 * 
 * @author Josh Vote
 *
 */
public class OpendapService {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());
    /** The service caller. */
    private HttpServiceCaller serviceCaller;
    /** The get data method maker. */
    private OPeNDAPGetDataMethodMaker getDataMethodMaker;

    public OpendapService(HttpServiceCaller serviceCaller, OPeNDAPGetDataMethodMaker getDataMethodMaker) {
        this.serviceCaller = serviceCaller;
        this.getDataMethodMaker = getDataMethodMaker;
    }

    /**
     * Fetches the object representing the dataset at serviceUrl
     * 
     * @param serviceUrl
     *            The OPeNDAP endpoint
     * @return
     * @throws PortalServiceException
     */
    protected NetcdfDataset fetchDataset(String serviceUrl) throws PortalServiceException {
        try {
            return NetcdfDataset.openDataset(serviceUrl);
        } catch (IOException ex) {
            log.info(String.format("Error connecting to '%1$s'", serviceUrl));
            log.debug("Exception...", ex);
            throw new PortalServiceException(String.format("Error connecting to '%1$s'", serviceUrl), ex);
        }
    }

    /**
     * Gets the exposed variables from an OPeNDAP endpoint.
     * 
     * @param serviceUrl
     *            OPeNDAP endpoint to query
     * @param variableFilter
     *            if not null, all ViewVariables in the response will have the name variableFilter
     * @return
     * @throws PortalServiceException
     */
    public AbstractViewVariable[] getVariables(String serviceUrl, String variableFilter) throws PortalServiceException {
        NetcdfDataset ds = fetchDataset(serviceUrl);

        //Attempt to parse our response
        try {
            return ViewVariableFactory.fromNetCDFDataset(ds, variableFilter);
        } catch (IOException ex) {
            log.error(String.format("Error parsing from '%1$s'", serviceUrl), ex);
            throw new PortalServiceException(String.format("Error parsing to '%1$s'", serviceUrl), ex);
        }
    }

    /**
     * Makes a request for the data at an OPeNDAP endpoint
     * 
     * @param serviceUrl
     *            OPeNDAP endpoint to query
     * @param downloadFormat
     *            What format should the data be downloaded in
     * @param constraints
     *            [Optional] Any constraints to apply to the download
     * @return
     * @throws PortalServiceException
     */
    public InputStream getData(String serviceUrl, OPeNDAPFormat downloadFormat, AbstractViewVariable[] constraints)
            throws PortalServiceException {
        NetcdfDataset ds = fetchDataset(serviceUrl);
        HttpRequestBase method = null;

        try {
            method = getDataMethodMaker.getMethod(serviceUrl, downloadFormat, ds, constraints);
            return serviceCaller.getMethodResponseAsStream(method);
        } catch (Exception ex) {
            log.error(String.format("Error requesting data from '%1$s'", serviceUrl), ex);
            throw new PortalServiceException(method, String.format("Error requesting data from '%1$s'", serviceUrl), ex);
        }
    }

    /**
     * Provide a string representation of the url request
     * 
     * @param serviceUrl
     *            OPeNDAP endpoint to query
     * @param downloadFormat
     *            What format should the data be downloaded in
     * @param constraints
     *            [Optional] Any constraints to apply to the download
     * @return
     * @throws PortalServiceException
     */
    public String getQueryDetails(String serviceUrl, OPeNDAPFormat downloadFormat, AbstractViewVariable[] constraints)
            throws PortalServiceException {
        NetcdfDataset ds = fetchDataset(serviceUrl);
        HttpRequestBase method = null;

        try {
            method = getDataMethodMaker.getMethod(serviceUrl, downloadFormat, ds, constraints);
            String details = "ServiceUrl: " + method.getURI().toString();
            details += "\n" + "DownloadFormat: " + downloadFormat;
            details += "\n" + "DataSet: " + ds;

            return details;
        } catch (Exception ex) {
            throw new PortalServiceException(method, String.format(
                    "Error parsing query URI", serviceUrl), ex);
        }
    }

}
