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
});