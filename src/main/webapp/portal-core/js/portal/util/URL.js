/**
 * Utility functions for manipulating URLs
 */
Ext.define('portal.util.URL', {
    singleton: true
}, function() {
    if (!Ext.isString(window.WEB_CONTEXT)) {
        window.WEB_CONTEXT = '/' + window.location.pathname.substring(1).split("/")[0];
    }

    /**
     * The base URL for this page (with trailing '/') as a String variable
     *
     * eg - http://your.website/context/
     */
    portal.util.URL.base = window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/";
    
    /**
     * Given a URL in the form http://example.com:80/path?k=v
     * 
     * Return just the host (sans port number) i.e. 'example.com' or an empty string
     * if the host couldn't be parsed
     * 
     * @param url The URL as a String
     */
    portal.util.URL.extractHost = function(url) {
        var urlRegexp = /^(?:ftp|https?):\/\/(?:[^@:\/]*@)?([^:\/]+)/;
        var match = urlRegexp.exec(url);
        if (!match || match.length < 2) {
            return '';
        }
        return match[1];
    },
    
    
    /**
     * Given a URL in the form http://example.com:80/a/b/c
     * 
     * Return just the host (and port number) i.e. 'example.com' and subdirectory
     * 
     * 
     * @param url The URL as a String
     * @param OPTIONAL number of subDir to return
     */
    portal.util.URL.extractHostNSubDir = function(url,numberOfSubDir) {
        var a = document.createElement('a');
        a.href = url;
        var pathname = (a.pathname.charAt(0) == "/") ? a.pathname : "/" + a.pathname;
        var pathArray=pathname.split("/");
        
        var hostname = a.hostname;
        
        if(numberOfSubDir && pathArray.length > 1){
            
            for(var i=1; i <= numberOfSubDir && i < pathArray.length ; i++){
                hostname =  hostname + "/" + pathArray[i];
            }            
            
            return hostname;
        }
        
        
        
        return a.hostname;
    }
    
});