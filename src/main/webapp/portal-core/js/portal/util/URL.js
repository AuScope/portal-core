/**
 * Utility functions for manipulating URLs
 */
Ext.define('portal.util.URL', {
    singleton: true
}, function() {
    if (!window.WEB_CONTEXT) {
        window.WEB_CONTEXT = '';
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
    }
});