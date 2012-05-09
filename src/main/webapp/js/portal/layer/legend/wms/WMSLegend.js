/**
 * An implementation of portal.layer.legend.Legend for providing
 * simple GUI details on a WFS layer added to the map
 */
Ext.define('portal.layer.legend.wfs.WMSLegend', {
    extend: 'portal.layer.legend.Legend',

    iconUrl : '',

    /**
     * @param cfg an object in the form {
     *  iconUrl : String The URL of the marker icon for this layer
     * }
     */
    constructor : function(cfg) {
        this.iconUrl = cfg.iconUrl;
        this.callParent(arguments);
    },

    /**
     * Implemented function, see parent class
     */
    getLegendComponent : function(resources, filterer, callback) {
        var form = Ext.create('portal.layer.legend.wms.WMSLegendForm',{resources : resources,filterer : filterer});
        callback(this, resources, filterer, true, form); //this layer cannot generate a GUI popup
    },

    /**
     * Implemented function, see parent class
     */
    getLegendIconHtml : function(resources, filterer) {
        if (this.iconUrl && this.iconUrl.length > 0) {
            return Ext.DomHelper.markup({
                tag : 'div',
                style : 'text-align:center;',
                children : [{
                    tag : 'img',
                    width : 16,
                    height : 16,
                    align: 'CENTER',
                    src: this.iconUrl
                }]
            });
        } else {
            return '';
        }
    },

    statics : {

        generateImageUrl : function(wmsURL,wmsName,styles) {
            var url = wmsURL;
            var last_char = url.charAt(url.length - 1);
            if ((last_char !== "?") && (last_char !== "&")) {
              if (url.indexOf('?') == -1) {
                 url += "?";
              } else {
                 url += "&";
              }
            }
            url += 'REQUEST=GetLegendGraphic';
            url += '&SERVICE=WMS';
            url += '&VERSION=1.1.1';
            url += '&FORMAT=image/png';
            url += '&BGCOLOR=0xFFFFFF';
            url += '&LAYER=' + escape(wmsName);
            url += '&LAYERS=' + escape(wmsName);
            if (this.styles) {
                url += '&STYLES=' + escape(this.styles);
            }

            return url;
        }
    }
});