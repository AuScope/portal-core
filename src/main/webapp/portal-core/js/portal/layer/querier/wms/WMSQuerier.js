/**
 * Class for making and then parsing a WMS request/response
 */
Ext.define('portal.layer.querier.wms.WMSQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Creates a BaseComponent rendered with an error message
     *
     * @param message The message string to put in the body of the generated component
     * @param tabTitle The title of the tab (defaults to 'Error')
     */
    generateErrorComponent : function(message, tabTitle) {
        return Ext.create('portal.layer.querier.BaseComponent', {
            html: Ext.util.Format.format('<p class="centeredlabel">{0}</p>', message),
            tabTitle : tabTitle ? tabTitle : 'Error'
        });
    },

    /**
     * Returns true if WMS GetFeatureInfo query returns data.
     *
     * We need to hack a bit here as there is not much that we can check for.
     * For example the data does not have to come in tabular format.
     * In addition html does not have to be well formed.
     * In addition an "empty" click can still send style information
     *
     * So ... we will assume that minimum html must be longer then 30 chars
     * eg. data string: <table border="1"></table>
     *
     * For a bit of safety lets only count the bytes in the body tag
     *
     * @param {iStr} HTML string content to be verified
     * @return {Boolean} Status of the
     */
    isHtmlDataThere : function(iStr) {
        //This isn't perfect and can technically fail
        //but it is "good enough" unless you want to start going mental with the checking
        var lowerCase = iStr.toLowerCase();

        //If we have something resembling well formed HTML,
        //We can test for the amount of data between the body tags
        var startIndex = lowerCase.indexOf('<body>');
        var endIndex = lowerCase.indexOf('</body>');
        if (startIndex >= 0 || endIndex >= 0) {
            return ((endIndex - startIndex) > 32);
        }

        //otherwise it's likely we've just been sent the contents of the body
        return lowerCase.length > 32;
    },

    /**
     * See parent class for definition
     *
     * Makes a WMS request, waits for the response and then parses it passing the results to callback
     */
    query : function(queryTarget, callback) {
        var proxyUrl = this.generateWmsProxyQuery(queryTarget, 'text/html');
        Ext.Ajax.request({
            url: proxyUrl,
            timeout : 180000,
            scope : this,
            callback : function(options, success, response) {
                var cmp = null;

                if (!success) {
                    cmp = this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.');
                } else if (this.isHtmlDataThere(response.responseText)) {
                    cmp = Ext.create('portal.layer.querier.BaseComponent', {
                        autoScroll : true,
                        html: response.responseText
                    });
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