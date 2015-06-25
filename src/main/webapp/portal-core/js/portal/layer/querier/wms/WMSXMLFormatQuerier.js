/**
 * Class for making and then parsing a WMS request/response
 */
Ext.define('portal.layer.querier.wms.WMSXMLFormatQuerier', {
    extend: 'portal.layer.querier.wms.WMSQuerier',

    constructor: function(config){
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },



    /**
     * @Override
     * See parent class for definition
     *
     * Makes a WMS request, waits for the response and then parses it passing the results to callback
     */
    query : function(queryTarget, callback) {
        var proxyUrl = this.generateWmsProxyQuery(queryTarget, 'text/xml');
        Ext.Ajax.request({
            url: proxyUrl,
            timeout : 180000,
            scope : this,
            callback : function(options, success, response) {
                var cmp = null;

                if (!success) {
                    cmp = this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.');
                } else {
                    //VT: Hopefully this is a once off event. When we serve up wms using GeoServer,
                    //VT: we can process the it normally using wmsquerier because of it supports info_format=text/html
                    //VT: However we should be able to reuse this in the future when we are required to parese xml instead of html.
                    cmp = Ext.create('portal.layer.querier.BaseComponent', this._parseStringXMLtoTreePanel(response.responseText));
                }

                if (cmp !== null) {
                    callback(this, [cmp], queryTarget);
                } else {
                    callback(this, [], queryTarget);
                }
            }
        });
    }

});