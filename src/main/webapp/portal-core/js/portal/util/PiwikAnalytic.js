/**
 * Utility functions for downloading files
 */
Ext.define('portal.util.PiwikAnalytic', {
    singleton: true
}, function() {


    portal.util.PiwikAnalytic.trackevent = function(catagory,action,label,value) {
        if(typeof _paq != 'undefined' ){
            if(value){
                _paq.push(['trackEvent', catagory, action, label,value]);
            } else {
                _paq.push(['trackEvent', catagory, action, label]);
            }
        }

    };


    portal.util.PiwikAnalytic.siteSearch = function(keyword,category,searchCount) {
        if(typeof _paq != 'undefined' ){                     
            _paq.push(['trackSiteSearch',keyword,category,searchCount]);
        }

    };
});