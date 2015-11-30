/**
 * Contains status information about the current state of rendering.
 *
 *  events :
 *      change(portal.layer.filterer.Filterer this, String[] keys)
 *          Fired whenever the map changes, passed an array of all keys that have changed.
 */
Ext.define('portal.layer.renderer.RenderStatus', {
    extend: 'portal.util.ObservableMap',

    constructor : function(config) {
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Updates this render status with a new status for a particular key. if key DNE it will be created,
     * otherwise it will be overridden.
     *
     * key - string
     * responseStatus - string
     *
     * returns void
     */
    updateResponse : function(key, responseStatus) {
        this.setParameter(key, responseStatus);
    },

    /**
     * Batch sets all keys with a specified status.
     *
     * allKeys - array of strings
     * responseStatus - string
     */
    initialiseResponses : function(allKeys, responseStatus) {
        var params = {};
        for (var i = 0; i < allKeys.length; i++) {
            params[allKeys[i]] = responseStatus;
        }

        this.setParameters(params, true);
    },

    /**
     * Renders this status into a HTML string consisting of a table that represents all key/status pairs
     */
    renderHtml : function() {
        var parameterAddCount = 0;
        var htmlString = '<div class="auscope-servicestatus-grid"><table><tr><td>Request Status</td></tr>' ;

        for(i in this.parameters) {
            if(!this.parameters[i].toString().match('function')) {
                parameterAddCount++;
                if(i.length >= 1) {
                    htmlString += '<tr><td>'+ i + ' - ' + this.parameters[i] +'</td></tr>';
                } else {
                    htmlString += '<tr><td>'+ this.parameters[i] +'</td></tr>';
                }
            }
        }
        htmlString += '</table></div>' ;

        if (parameterAddCount === 0) {
            return 'No status has been recorded';
        } else {
            return htmlString;
        }

    }
});



