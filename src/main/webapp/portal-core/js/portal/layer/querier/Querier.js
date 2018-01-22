/**
 * An abstract class for providing functionality that
 * looks for more information about a particular QueryTarget.
 *
 * Ie if the user selects a particular feature from a WFS for
 * more information, the querier will lookup that information
 * an return a portal.layer.querier.BaseComponent contain
 * said information
 *
 */
Ext.define('portal.layer.querier.Querier', {
    extend: 'Ext.util.Observable',

    map : null, //instance of portal.util.gmap.GMapWrapper for use by subclasses

    constructor: function(config){

        this.map = config.map;

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Utility function for generating a WMS GetFeatureInfo request that is proxied through the portal backend
     * for querying for information about the specific queryTarget.
     * VT: Now that it is possible for querier to handle both wfs with wms, this utility has been moved to the parent layer.
     *
     * The response will be returned in the specified infoFormat
     * @param queryTarget A portal.layer.querier.QueryTarget
     * @param infoFormat a String representing a MIME type
     */
    generateWmsProxyQuery : function(queryTarget, infoFormat) {
        
        var point = Ext.create('portal.map.Point', {latitude : queryTarget.get('lat'), longitude : queryTarget.get('lng')});
        var lonLat = new OpenLayers.LonLat(point.getLongitude(), point.getLatitude());
        lonLat = lonLat.transform('EPSG:4326','EPSG:3857');

        var tileInfo = this.map.getTileInformationForPoint(point);
        var layer = queryTarget.get('layer');
        var feature_count=0;//VT:0 is to default to server setting
        
        if(layer.get('sourceType')==portal.layer.Layer.KNOWN_LAYER){
            feature_count = layer.get('source').get('feature_count');
        }
        
        var wmsOnlineResource = queryTarget.get('onlineResource');

        var typeName = wmsOnlineResource.get('name');
        var serviceUrl = wmsOnlineResource.get('url');


        var bbox = tileInfo.getTileBounds();
        var bboxString = Ext.util.Format.format('{0},{1},{2},{3}',
                bbox.eastBoundLongitude,
                bbox.northBoundLatitude,
                bbox.westBoundLongitude,
                bbox.southBoundLatitude);
        
        var sldParams = this.generateSLDParams (queryTarget, infoFormat);
        
        var queryParams = Ext.Object.merge({
            serviceUrl : serviceUrl,
            lat : lonLat.lat,
            lng : lonLat.lon,
            QUERY_LAYERS : typeName,
            x : tileInfo.getOffset().x,
            y : tileInfo.getOffset().y,
            BBOX : bboxString,
            WIDTH : tileInfo.getWidth(),
            HEIGHT : tileInfo.getHeight(),
            INFO_FORMAT : infoFormat,
            version : wmsOnlineResource.get('version'),
            feature_count : feature_count
        }, sldParams)
        
        //Build our proxy URL
        var queryString = Ext.Object.toQueryString(queryParams);
        return Ext.urlAppend('wmsMarkerPopup.do', queryString);
    },
    
    /*
     * Determines the parameters to use when applying an SLD to a GetFeatureInfo query.
     * 
     */    
    generateSLDParams : function (queryTarget, infoFormat) {
        var postMethod = false;
        var sld_body = null;
        
        if(queryTarget.get('layer').get('filterer').getParameters().postMethod){
            postMethod = queryTarget.get('layer').get('filterer').getParameters().postMethod;
        }
        
        var applicationProfile = queryTarget.get('onlineResource').get('applicationProfile');
        
        if (applicationProfile && applicationProfile.indexOf("Esri:ArcGIS Server") > -1) {
             postMethod = false;
             infoFormat = infoFormat ? infoFormat : 'text/xml'
        }
        else if (queryTarget.get('layer').get('renderer').sld_body){
            sld_body=queryTarget.get('layer').get('renderer').sld_body;
            //VT: if post is undefined and we have a very long sld_body
            //VT: we are goign to take a best guess approach and use post instead of get
            if(sld_body.length > 1200){
                postMethod = true;
                console.log('You really should not be using this method if the query' +
                        'is going be long as it generates a GET spring request to and' +
                        ' there are limitation to the lenght of a URI in GET method');
            }
            infoFormat = infoFormat ? infoFormat : 'application/vnd.ogc.gml/3.1.1'
            
        }
        return {
            postMethod : postMethod,
            SLD_BODY : sld_body,
            INFO_FORMAT : infoFormat
        }

    },

    /**
     * An abstract function for querying for information
     * about a particular feature/location associated with a
     * data source.
     *
     * The result of the query will be returned via a callback mechanism
     * as a set of BaseComponent's ie. GUI widgets.
     *
     * function(portal.layer.querier.QueryTarget target,
     *          function(portal.layer.querier.Querier this, portal.layer.querier.BaseComponent[] baseComponents, portal.layer.querier.QueryTarget target) callback
     *
     * returns - void
     *
     * target - the instance that fired off the query
     * callback - will be called the specified parameters after the BaseComponent has been created. The baseComponents array may be null or empty
     */
    query : portal.util.UnimplementedFunction

});