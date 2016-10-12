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
    getLegendComponent : function(resources, filterer,sld_body, isSld_body, callback, staticLegendUrl, tryGetCapabilitiesFirst) {
        // GPT-80 - Legend - This is called from BARP / _getLegendAction().  I think I want to change WMSLegendForm ... (see there)
        var form = Ext.create('portal.layer.legend.wms.WMSLegendForm',{resources:resources, filterer:filterer, sld_body:sld_body, staticLegendUrl:staticLegendUrl});
        callback(this, resources, filterer, true, form); //this layer cannot generate a GUI popup
        // GPT-80 - the Legend data now comes from async service calls and needs to added separately (prev. was done in constructor)
        form.addLegends({resources : resources, form : form, sld_body: sld_body, isSld_body: isSld_body, tryGetCapabilitiesFirst:tryGetCapabilitiesFirst});
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

        generateImageUrl : function(wmsURL,wmsName,wmsVersion,applicationProfile,width,sld_body,isSld_body,styles) {
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
            url += '&HEIGHT=25';
            url += '&BGCOLOR=0xFFFFFF';
            url += '&LAYER=' + escape(wmsName);
            url += '&LAYERS=' + escape(wmsName);
            if (width) {
                url += '&WIDTH=' + width;  
            }
            
            //vt: The sld for legend does not require any filter therefore it should be
            // able to accomadate all sld length.
            if (sld_body && sld_body.length < 2000) {
                if (isSld_body === true) {
                    url += '&SLD_BODY=' + escape(sld_body);
                } else {
                    url += '&SLD=' + encodeURIComponent(sld_body);
                }
                url += '&LEGEND_OPTIONS=forceLabels:on;minSymbolSize:16';
            }
            
            // GPT-MS -- I don't believe the below works. GetLegendGraphic takes a STYLE parameter, not a STYLES parameter. Have left it as is. 
            if (this.styles) {
                url += '&STYLES=' + escape(this.styles);
            } else if (applicationProfile && applicationProfile.indexOf("Esri:ArcGIS Server") > -1) {
            	var sld = portal.util.xml.SimpleDOM.parseStringToDOM(sld_body);
            	// GPT-MS : This would be better as an XPath '/StyledLayerDescriptor/UserStyle/Name" but I couldn't get it to work.  
            	url += '&STYLE=' + sld.getElementsByTagName("UserStyle")[0].getElementsByTagName("Name")[0].textContent;
            }

            return url;
        },
    
        /* Hits the WMS controller to doa  getCapabilties call on the layer and retrieve the LegendGraphicURL element */
        generateLegendGraphicFromGetCapabilities : function(wmsURL,wmsName,wmsVersion,applicationProfile,width,sld_body,isSld_body,styles, callback) {
            portal.util.Ajax.request({
                url: "getLegendURL.do",
                timeout : 30000,    // Yes this seems a long time but was necessary
                params : {
                    serviceUrl : wmsURL ,
                    wmsVersion : wmsVersion,
                    layerName : wmsName
                },
                scope : this,
                success: function(data, message){
                    callback(data);
                },
                failure: function(message) {
                    var getLegendGraphicUrl = portal.layer.legend.wms.WMSLegend.generateImageUrl(wmsURL,wmsName,wmsVersion,applicationProfile,width,sld_body,isSld_body,styles);
                    callback(getLegendGraphicUrl);
                }
            });
        },

        // WMS Can specify a <legendUrl> image - retrieve from the service
        generateLegendUrl : function(wmsURL,wmsName,wmsVersion,applicationProfile,width,sld_body,isSld_body,styles, tryGetCapabilitiesFirst, callback) {
            
            // first check the getCapabilities if configured to do so
            if (tryGetCapabilitiesFirst) {
                this.generateLegendGraphicFromGetCapabilities(wmsURL,wmsName,wmsVersion,applicationProfile,width,sld_body,isSld_body,styles, callback);
            } else {
                // the default behaviour: try a getLegendGraphic call and use the getCapabilties iff it fails
                var getLegendGraphicUrl = portal.layer.legend.wms.WMSLegend.generateImageUrl(wmsURL,wmsName,wmsVersion,applicationProfile,width,sld_body,isSld_body,styles);
                if (getLegendGraphicUrl) {
                    callback(getLegendGraphicUrl);
                } else {
                    this.generateLegendGraphicFromGetCapabilities(wmsURL,wmsName,wmsVersion,applicationProfile,width,sld_body,isSld_body,styles, callback);
                }
            }
        }
    }    
});