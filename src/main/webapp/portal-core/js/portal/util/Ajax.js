/**
 * This is a portal core specialisation/alternative to Ext.Ajax.request that specialises
 * in receiving JSON responses in the form:
 * 
 *  {
 *    success : Boolean - Whether the request was successful (as reported by the server)
 *    data : (Optional) Object/Array - Any response information from the server
 *    message : (Optional) String - Any extra human readable information reported by the server
 *    debugInfo : (Optional) Object - Any extra debug information pertaining to the request and its processing
 *  }
 *  
 *  Any other types of response formats should be made through Ext.Ajax.request as per normal
 */
Ext.define('portal.util.Ajax', {
    singleton: true,
    
    /**
     * Handles all parsing logic and error conditions for a boolean success flag and a XMLHttpRequest object
     * 
     * @param success Boolean - whether the remote connection was successful
     * @param response XMLHttpRequest - response object
     * @param callback function(success, data, message, debugInfo)
     *               success - Boolean - true if the connection succeeded AND the server reported success. false otherwise
     *               data - Array/Object - contents of the data response (if any)
     *               message - String - String message returned by server if connection succeeded, otherwise a generic HTTP error message
     *               debugInfo - Object - debug object returned by server (if any)
     *               response - Object - Ajax response object
     */
    parseResponse: function(success, response, callback) {
        if (!success) {
            var message = response.status ? 
                            response.status + ': ' + response.statusText :
                            'Network Error: Cannot connect to server.';

            callback(false, undefined, message, undefined, response);
            return;
        }
        
        var responseObj = null;
        try {
            responseObj = Ext.JSON.decode(response.responseText);
        } catch(err) {
            console.log('ERROR parsing Ajax response:', err);
            callback(false, undefined, undefined, undefined, response);
            return;
        }
        
        try {
            callback(responseObj.success === true, responseObj.data, responseObj.msg, responseObj.debugInfo, response);
        } catch(err) {
            console.log('ERROR calling user callback:', err);
            return;
        }
    },
    
    /**
     * Exactly the same as Ext.Ajax.request with the following changes/additions:
     * 
     *  callback - function(success, data, message, debugInfo)
     *               success - Boolean - true if the connection succeeded AND the server reported success. false otherwise
     *               data - Array/Object - contents of the data response (if any)
     *               message - String - String message returned by server if connection succeeded, otherwise a generic HTTP error message
     *               debugInfo - Object - debug object returned by server (if any)
     *               response - Object - Ajax response object
     *               
     *  success - function(data, message, debugInfo) - This will be called IFF the connection succeeds AND the response object indicates success
     *               data - Array/Object - contents of the data response (if any)
     *               message - String - String message returned by server if connection succeeded, otherwise a generic HTTP error message
     *               debugInfo - Object - debug object returned by server (if any)
     *               response - Object - Ajax response object
     *               
     *  failure - function(message, debugInfo) - This will be called if the connection fail OR the response object indicates failure
     *               message - String - String message returned by server if connection succeeded, otherwise a generic HTTP error message
     *               debugInfo - Object - debug object returned by server (if any)
     *               response - Object - Ajax response object
     */
    request: function(cfg) {
        //We do all injection via callback and then offload back 
        //to the user defined success/failure/callback functions
        var userCallbacks = {
            success: cfg.success ? Ext.bind(cfg.success, cfg.scope) : undefined,
            failure: cfg.failure ? Ext.bind(cfg.failure, cfg.scope) : undefined,
            callback: cfg.callback ? Ext.bind(cfg.callback, cfg.scope) : undefined
        };
        delete cfg.failure;
        delete cfg.success;
        delete cfg.scope;
        
        var scope = cfg.scope;
        cfg.callback = Ext.bind(function(options, success, response, userCallbacks) {
            //Because we need to parse the response before we can workout whether to call success/failure callbacks
            //We need to go another level deeper with our wrapping functions. This final callback will decide what user callbacks
            //to fire and in what order depending on the results of the parseResponse function.
            portal.util.Ajax.parseResponse(success, response, Ext.bind(function(success, data, message, debugInfo, response, userCallbacks) {
                if (userCallbacks.callback) {
                    userCallbacks.callback(success, data, message, debugInfo, response);
                }
                
                if (success) {
                    if (userCallbacks.success) {
                        userCallbacks.success(data, message, debugInfo, response);
                    }
                } else {
                    if (userCallbacks.failure) {
                        userCallbacks.failure(message, debugInfo, response);
                    }
                }
            }, undefined, [userCallbacks], true));
        }, undefined, [userCallbacks], true);
        
        return Ext.Ajax.request(cfg);
    }
});