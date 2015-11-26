/**
 * Contains debug information about the final state of rendering. ie what requests were made
 * to where for data.
 *
 *  events :
 *      change(portal.layer.filterer.Filterer this, String[] keys)
 *          Fired whenever the map changes, passed an array of all keys that have changed.
 */
Ext.define('portal.layer.renderer.RenderDebuggerData', {
    extend: 'portal.util.ObservableMap',

    constructor : function(config) {
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Updates this render debug info with a new debug info for a particular key. if key DNE it will be created,
     * otherwise it will be overridden.
     *
     * key - string
     * debugDetail - string
     *
     * returns void
     */
    updateResponse : function(key, debugDetail) {
        this.setParameter(key, debugDetail);
    },

    /**
     * Renders this debug data into a HTML string that represents all key/status pairs
     */
    renderHtml : function() {
        var htmlString = '<br/>' ;
        var parametersAdded = 0;

        for(i in this.parameters) {
            var unescapedXml = this.parameters[i];
            if (!Ext.isString(unescapedXml)) {
                unescapedXml = '';
            }
            var escapedXml = unescapedXml.replace(/</g, '&lt;');

            parametersAdded = parametersAdded + 1;
            htmlString += '<b>'+ i + '</b>' + '<br/> ' + escapedXml +'<br/><br/>';
        }
        if (parametersAdded === 0) {
            htmlString += 'No information has yet been recorded...';
        }
        htmlString += '<br/>' ;
        return htmlString;
    }
});



