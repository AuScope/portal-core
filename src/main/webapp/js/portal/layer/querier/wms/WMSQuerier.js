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
     * Utility function for generating a WMS GetFeatureInfo request that is proxied through the portal backend
     * for querying for information about the specific queryTarget.
     *
     * The response will be returned in the specified infoFormat
     * @param queryTarget A portal.layer.querier.QueryTarget
     * @param infoFormat a String representing a MIME type
     */
    generateWmsProxyQuery : function(queryTarget, infoFormat) {
        var point = Ext.create('portal.map.Point', {latitude : queryTarget.get('lat'), longitude : queryTarget.get('lng')});
        var tileInfo = this.map.getTileInformationForPoint(point);
        var layer = queryTarget.get('layer');
        var wmsOnlineResource = queryTarget.get('onlineResource');

        var typeName = wmsOnlineResource.get('name');
        var serviceUrl = wmsOnlineResource.get('url');

        var bbox = tileInfo.getTileBounds();
        var bboxString = Ext.util.Format.format('{0},{1},{2},{3}',
                bbox.eastBoundLongitude,
                bbox.northBoundLatitude,
                bbox.westBoundLongitude,
                bbox.southBoundLatitude);


        //Build our proxy URL
        var queryString = Ext.Object.toQueryString({
            WMS_URL : serviceUrl,
            lat : point.getLatitude(),
            lng : point.getLongitude(),
            QUERY_LAYERS : typeName,
            x : tileInfo.getOffset().x,
            y : tileInfo.getOffset().y,
            BBOX : bboxString,
            WIDTH : tileInfo.getWidth(),
            HEIGHT : tileInfo.getHeight(),
            INFO_FORMAT : infoFormat
        });
        return Ext.urlAppend('wmsMarkerPopup.do', queryString);
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