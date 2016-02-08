/**
 * An implementation of portal.layer.legend.Legend for providing
 * simple GUI details on a WFS layer added to the map
 */
Ext.define('portal.layer.legend.wms.WMSLegend', {
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
    getLegendComponent : function(resources, filterer,sld_body, callback) {
        // GPT-80 - Legend - This is called from BARP / _getLegendAction().  I think I want to change WMSLegendForm ... (see there)
        var form = Ext.create('portal.layer.legend.wms.WMSLegendForm',{resources : resources,filterer : filterer,sld_body:sld_body});
        callback(this, resources, filterer, true, form); //this layer cannot generate a GUI popup
        // GPT-80 - the Legend data now comes from async service calls and needs to added separately (prev. was done in constructor)
        form.addLegends({resources : resources, form : form, sld_body: sld_body});
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

        generateImageUrl : function(wmsURL,wmsName,wmsVersion,width,sld_body,styles) {
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
            url += '&VERSION='+ wmsVersion;
            url += '&FORMAT=image/png';
            url += '&BGCOLOR=0xFFFFFF';
            url += '&LAYER=' + escape(wmsName);
            url += '&LAYERS=' + escape(wmsName);
            if (width) {
                url += '&WIDTH=' + width;
            }
            //vt: The sld for legend does not require any filter therefore it should be
            // able to accomadate all sld length.
            if(sld_body && sld_body.length< 2000){
                url += '&SLD_BODY=' + escape(sld_body);
                url += '&LEGEND_OPTIONS=forceLabels:on';
            }
            if (this.styles) {
                url += '&STYLES=' + escape(this.styles);
            }

            return url;
        },
    
        // WMS Can specify a <legendUrl> image - retrieve from the service
        generateLegendUrl : function(wmsURL,wmsName,wmsVersion,width,sld_body,styles, callback) {
            
            var url = portal.layer.legend.wms.WMSLegend.generateImageUrl(wmsURL,wmsName,wmsVersion,width,sld_body,styles);
            
            if (url) {
                callback(url);
            } else {            
                Ext.Ajax.request({
                    url: "getLegendURL.do",
                    timeout : 30000,    // Yes this seems a long time but was necessary  
                    params : {
                        serviceUrl : wmsURL ,
                        wmsVersion : wmsVersion,
                        layerName : wmsName
                    },
                    scope : this,
                    success: function(response, options){
                        var text = response.responseText;
                        
                        console.log("getLegendURL.do call success - response text: ",text, "options: ", options);
                        callback(JSON.parse(response.responseText)["data"]);
                    },
                    failure: function(response, opts) {
                        var status = response.status;
                        var statusMsg = response.statusText;
                        
                        console.log("getLegendURL.do call failure - layerName: ", opts.params.layerName, ", url: ", wmsURL, ", status: ", status, ", status text: ",statusMsg, ".  Try alternate method.");
                        var url = portal.layer.legend.wms.WMSLegend.generateImageUrl(wmsURL,wmsName,wmsVersion,width,sld_body,styles);
                        callback(url);
                    }
                });
            }
        }
    }    
});