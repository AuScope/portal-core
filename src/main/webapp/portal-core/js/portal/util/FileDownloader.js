/**
 * Utility functions for downloading files
 */
Ext.define('portal.util.FileDownloader', {
    singleton: true
}, function() {
    /**
     * Given a URL this function will create a hidden form and post its (empty) contents
     * to the specified URL. If the response contains an appropriate header the user will
     * be prompted for a file download.
     *
     * @param url The URL to download to
     * @param parameters [Optional] a javascript object containing parameter key value pairs to be posted. The values may consist of Javascript primitives or Arrays
     */
    portal.util.FileDownloader.downloadFile = function(url, parameters, method) {
        //build our list of input children first
        var inputs = [];

        if (typeof method === "undefined") {
        	method = "POST"; 
        }
        if (method == "GET") {
        	// Populate inputs from the url
        	Ext.apply(parameters, Ext.urlDecode(url.split('?')[1]));
        }
       
        
        if (parameters) {
            for (var key in parameters) {
                if (!key) {
                    continue;
                }

                //We need to treat arrays and objects
                var value = parameters[key];
                if (!Ext.isArray(value)) {
                    value = [value];
                }

                //Build a number of Ext.DomHelper config objects representing the inputs
                for (var i = 0; i < value.length; i++) {
                    inputs.push({
                        tag : 'input',
                        id : Ext.util.Format.format('portal-input-{0}-{1}', key, i),
                        type : 'hidden',
                        name : key,
                        value : value[i]
                    });
                }
            }
        }

        //This will leak iframes but it shouldnt be an issue under "normal" usage
        var frameId = Ext.id();
        var body = Ext.getBody();
        var frame = body.createChild({
            tag : 'iframe',
            id : frameId,
            name : 'iframe'
        });
        var form = body.createChild({
            tag : 'form',
            target : frameId,
            method : method,
            children : inputs
        });

        portal.util.GoogleAnalytic.trackevent('FileDownloader','Download', url);
        portal.util.PiwikAnalytic.trackevent('FileDownloader', 'Url:' + url,'parameters:' + Ext.encode(parameters));

        form.dom.action = url;	
        form.dom.submit();
    };
});