/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

package org.auscope.gridtools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;

/**
 * @author Gary Lai
 *
 */
public class MyProxyManager {

    private static final Log logger =
        LogFactory.getLog(MyProxyManager.class.getName());

    /**
     * The default lifetime of a delegated proxy (12 hours).
     */
    public static int PROXY_LIFETIME_DEFAULT = 12*60*60;
    
    /**
     * Retrieves a {@link GSSCredential} from a myproxy server using username
     * and password. 
     * This method is used if you want to retrieve a proxy that requires
     * authentication.
     * 
     * @param server the hostname of the myproxy server
     * @param port the port of the myproxy server (default is 7512)
     * @param credential the credential that is used to authenticate against
     *                   the MyProxy server
     * @param username the username the user used when uploading the proxy
     * @param passphrase the passphrase the user used when uploading the proxy
     * @param lifetimeInSeconds how long you want the proxy to be valid
     * @return the delegated credential
     * @throws Exception 
     */
    public static GSSCredential getDelegation(String server, int port,
            GSSCredential credential, String username, char[] passphrase,
            int lifetimeInSeconds) throws Exception {
        MyProxy myproxy = new MyProxy(server, port);
        GSSCredential proxyCredential = null;
        try {
            logger.debug("username is " + username);
            proxyCredential = myproxy.get(credential, username,
                    new String(passphrase), lifetimeInSeconds);
        } catch (MyProxyException e) {
            logger.error("Could not get delegated proxy from server \"" +
                    server + ":" + port + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Could not get delegated proxy from server: " +
                    e.getMessage());
            throw e;
        }
        return proxyCredential;
    }
    
    /**
     * Retrieves a {@link GSSCredential} from a myproxy server using username
     * and password. 
     * This method is used when you want to retrieve a proxy that has got the
     * "allow anonymous retriever" flag enabled.
     * 
     * @param server the hostname of the myproxy server
     * @param port the port of the myproxy server (default is 7512)
     * @param username the username the user used when uploading the proxy
     * @param passphrase the passphrase the user used when uploading the proxy
     * @param lifetimeInSeconds how long you want the proxy to be valid
     * @return the delegated credential 
     * @throws Exception 
     */
    public static GSSCredential getDelegation(String server, int port,
            String username, char[] passphrase, int lifetimeInSeconds)
            throws Exception {
        MyProxy myproxy = new MyProxy(server, port);
        GSSCredential credential = null;
        try {
            credential = myproxy.get(username, new String(passphrase),
                    lifetimeInSeconds);
        } catch (MyProxyException e) {
            logger.error("Could not get delegated proxy from server \"" +
                    server + ":" + port + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Could not get delegated proxy from server: " +
                    e.getMessage());
            throw e;
        }
        return credential;
    }
}

