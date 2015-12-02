/**
 * Utility functions for downloading files
 */
Ext.define('portal.util.GoogleAnalytic', {
    singleton: true
}, function() {


    portal.util.GoogleAnalytic.trackevent = function(catagory,action,label,value) {
        if(typeof _gaq != 'undefined' ){
            if(value){
                _gaq.push(['_trackEvent', catagory, action, label,value]);
            } else {
                _gaq.push(['_trackEvent', catagory, action, label]);
            }
        }

    };


    portal.util.GoogleAnalytic.trackpage = function(page) {
        if(typeof _gaq != 'undefined' ){
            _gaq.push(['_trackPageview', page]);
        }

    };
});