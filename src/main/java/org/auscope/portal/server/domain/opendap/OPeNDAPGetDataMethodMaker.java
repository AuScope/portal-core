package org.auscope.portal.server.domain.opendap;

import org.apache.commons.httpclient.HttpMethodBase;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * An interface for generating HttpMethods that will query an OPeNDAP Service for data in a given format
 * which is constrained by a list of constraints (which are a simplification of variables in ds).
 * @author vot002
 *
 */
public interface OPeNDAPGetDataMethodMaker {
    public enum OPeNDAPFormat {
        ASCII,
        DODS
    }
    
    /**
     * Gets the HTTP Method that will make a get data request
     * @param opendapUrl The url to make the request to
     * @param format The download format
     * @param ds The dataset that the constraint list is referencing
     * @param constraints [Optional] The constraints to apply to the request.
     * @return
     */
    public HttpMethodBase getMethod(String opendapUrl, OPeNDAPFormat format, NetcdfDataset ds, ViewVariable[] constraints) throws Exception;
}
