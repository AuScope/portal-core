/**
 * Represents an primitive that sources its imagery directly from
 *
 */
Ext.define('portal.map.primitives.BaseWMSPrimitive', {

    extend : 'portal.map.primitives.BasePrimitive',

    statics : {
        /**
         * Utility function for generating a WMS GetMap request URL
         *
         * @param serviceUrl String - The WMS URL
         * @param layer String - the WMS layer name
         * @param bbox portal.util.BBox - the bounding box to request imagery for
         * @param width Number - The width of the response image in pixels
         * @param height Number - The height of the response image in pixels
         * @param imageFormat [Optional] String - the image MIME type to request - default 'image/png'
         */
        getWmsUrl : function(serviceUrl, layer, bbox, width, height, imageFormat) {

        	console.log("------- BasePrimitive - getWmsUrl - layer =" + layer);
        	
            serviceUrl = this.replaceUrlParamCaseInsensitive(serviceUrl, 'REQUEST' , 'GetMap');
            serviceUrl = this.replaceUrlParamCaseInsensitive(serviceUrl, 'SERVICE' , 'WMS');
            serviceUrl = this.replaceUrlParamCaseInsensitive(serviceUrl, 'VERSION' , '1.1.1');
        	
            var bbox_3857=bbox.transform(bbox,'EPSG:3857');

            var bboxString = Ext.util.Format.format('{0},{1},{2},{3}',
                    bbox_3857.westBoundLongitude,
                    bbox_3857.southBoundLatitude,
                    bbox_3857.eastBoundLongitude,
                    bbox_3857.northBoundLatitude);

            var params = {
                'FORMAT' : imageFormat ? imageFormat : 'image/png',
                'BGCOLOR' : '0xFFFFFF',
                'TRANSPARENT' : 'TRUE',
                'LAYERS' : layer, //"qtot_avg",// 
                'SRS' :bbox_3857.crs,
                'BBOX' : bboxString,
                'WIDTH' : width,
                'HEIGHT' : height,
                'STYLES' : '' //Some WMS implementations require this
            };

            var queryString = Ext.Object.toQueryString(params);
            return Ext.urlAppend(serviceUrl, queryString);
        },

        /**
         * Utility function for generating a WMS 1.3 GetMap request URL
         *
         * @param serviceUrl String - The WMS URL
         * @param layer String - the WMS layer name
         * @param bbox portal.util.BBox - the bounding box to request imagery for
         * @param width Number - The width of the response image in pixels
         * @param height Number - The height of the response image in pixels
         * @param imageFormat [Optional] String - the image MIME type to request - default 'image/png'
         */
        getWms_130_Url : function(serviceUrl, layer, bbox, width, height, imageFormat) {

        	console.log("------- BasePrimitive - getWmsUrl - 130");
            var bbox_3857=bbox.transform(bbox,'EPSG:3857');

            var bboxString = Ext.util.Format.format('{0},{1},{2},{3}',
                    bbox_3857.southBoundLatitude,
                    bbox_3857.westBoundLongitude,
                    bbox_3857.northBoundLatitude,
                    bbox_3857.eastBoundLongitude
                    );

            var params = {
                'REQUEST' : 'GetMap',
                'SERVICE' : 'WMS',
                'VERSION' : '1.3.0',
                'FORMAT' : imageFormat ? imageFormat : 'image/png',
                'BGCOLOR' : '0xFFFFFF',
                'TRANSPARENT' : 'TRUE',
                'LAYERS' : layer,
                'CRS' :bbox_3857.crs,
                'BBOX' : bboxString,
                'WIDTH' : width,
                'HEIGHT' : height,
                'STYLES' : '' //Some WMS implementations require this
            };

            var queryString = Ext.Object.toQueryString(params);
            return Ext.urlAppend(serviceUrl, queryString);
        },
        
        replaceUrlParamCaseInsensitive: function (url, paramName, paramValue) 
        {
        	var lower = paramName.toLowerCase();
        	if (url.includes(lower)) {
        		return this.replaceUrlParam(url, lower, paramValue);
        	}
        	return this.replaceUrlParam(url, paramName, paramValue);
        },
        
        replaceUrlParam: function (url, paramName, paramValue)
        {
            if (paramValue == null) {
                paramValue = '';
            }
            var pattern = new RegExp('\\b('+paramName+'=).*?(&|#|$)');
            if (url.search(pattern)>=0) {
                return url.replace(pattern,'$1' + paramValue + '$2');
            }
            url = url.replace(/[?#]$/,'');
            return url + (url.indexOf('?')>0 ? '&' : '?') + paramName + '=' + paramValue;
        }
    },

    config : {
        /**
         * String - The WMS URL endpoint to query
         */
        wmsUrl : '',
        /**
         * String - the WMS layer name to query
         */
        wmsLayer : '',
        /**
         * Number - the opacity of the layer in the range [0, 1]
         */
        opacity : 1.0,       

        sld_body : ''

    },

    /**
     * Accepts the following in addition to portal.map.primitives.BasePrimitive's constructor options
     *
     * wmsUrl : String - The WMS URL endpoint to query
     * layer : String - the WMS layer name to query
     * opacity : Number - the opacity of the layer in the range [0, 1]
     */
    constructor : function(cfg) {
        this.callParent(arguments);

        this.setWmsUrl(cfg.wmsUrl);
        this.setWmsLayer(cfg.wmsLayer);
        this.setOpacity(cfg.opacity);
        this.setSld_body(cfg.sld_body);
       
    }

});