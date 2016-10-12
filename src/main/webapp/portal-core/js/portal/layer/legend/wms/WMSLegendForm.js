/**
 * An implementation of portal.layer.legend.BaseComponent for providing
 * simple GUI details for a WMS Legend
 */
Ext.define('portal.layer.legend.wms.WMSLegendForm', {
    extend: 'portal.layer.legend.BaseComponent',

    staticLegendUrl: null,
    tryGetCapabilitiesFirst : false,

    constructor : function(config) {
 
        this.staticLegendUrl = config.staticLegendUrl;
        this.tryGetCapabilitiesFirst = config.tryGetCapabilitiesFirst;
        Ext.apply(config, {
            html    : '<p> Waiting for Legend data ...</p>'
        });
        this.callParent(arguments);
    },
    
    addLegends : function(config) {   
        var me = this;

        var dimensions = {maxWidth:330,height:30}; // Of all graphics so can resize window - accumulative height, max width (allow for title)

        // if a url to a static image was provided then just use that and
        if (this.staticLegendUrl) {
            var html = '<a target="_blank" href="' + this.staticLegendUrl + '">';
            html += '<img onerror="this.alt=\'There was an error loading this legend. Click here to try again in a new window or contact the data supplier.\'" alt="Loading legend..." src="' + this.staticLegendUrl + '"/>';
            html += '</a>';
            html += '<br/>\n';
            config.form.setData(html);
            me._setFormHeight(config.form, this.staticLegendUrl, dimensions);
            return;
        }

        if (config.sld_body && config.sld_body.length > 0 && config.sld_body.length < 2000) {
            this.addStyledLegend(config);           
        } else {
            var wmsOnlineResources = portal.csw.OnlineResource.getFilteredFromArray(config.resources, portal.csw.OnlineResource.WMS);
            var urls={};
            var dimensions = {maxWidth:330,height:30}; // Of all graphics so can resize window - accumulative height, max width (allow for title)

            for (var j = 0; j < wmsOnlineResources.length; j++) {
                var applicationProfile = wmsOnlineResources[j].get('applicationProfile');
                var width = this._determineWidth(applicationProfile, config.sld_body);

                portal.layer.legend.wms.WMSLegend.generateLegendUrl(
                    wmsOnlineResources[j].get('url'), 
                    wmsOnlineResources[j].get('name'),
                    wmsOnlineResources[j].get('version'),
                    applicationProfile,
                    width,
                    config.sld_body,
                    config.isSld_body,
                    undefined,
                    config.tryGetCapabilitiesFirst,
                    function(url) {
                        if (! urls.hasOwnProperty(url)) {
                            // Add a WIDTH attribute to ?requests (but not to normal resource GETs ie. that don't contain a '?')
                            if (!url.match(/width/i) === null && url.match(/\?/) !== null) {
                                // Force a width or else the gis server seems to return it truncated
                                url += "&WIDTH=100";
                            }
                            var html='';

                            urls[url] = 1;
                            Object.getOwnPropertyNames(urls).sort().forEach(function (url, index, array) {
                                html += '<a target="_blank" href="' + url + '">';
                                html += '<img onerror="this.alt=\'There was an error loading this legend. Click here to try again in a new window or contact the data supplier.\'" alt="Loading legend..." src="' + url + '"/>';
                                html += '</a>';
                                html += '<br/>\n';
                            });
                            config.form.setData(html);
                            me._setFormHeight(config.form, url, dimensions);
                        }
                    }
                );
            } 
        }
    },
    
    /*
     * Adds a legend image using SLD styles. If the sld style if being used for the layer then 
     * we will only have one legend so we need slightly different processing to the addLegends method
     */
    addStyledLegend : function(config) {
        var me = this;
        var wmsOnlineResources = portal.csw.OnlineResource.getFilteredFromArray(config.resources, portal.csw.OnlineResource.WMS);
        var sourceUrls = [];
        var loopIndex = 0;
        
        // do some AJAX stuff to populate the list of image URLS
        for (loopIndex; loopIndex < wmsOnlineResources.length; loopIndex++) {
            var applicationProfile = wmsOnlineResources[loopIndex].get('applicationProfile');
            var width = this._determineWidth(applicationProfile, config.sld_body);
            
           
            var handler = portal.layer.legend.wms.WMSLegend.generateLegendUrl(
                    
                wmsOnlineResources[loopIndex].get('url'), 
                wmsOnlineResources[loopIndex].get('name'),
                wmsOnlineResources[loopIndex].get('version'),
                applicationProfile,
                width,
                config.sld_body,
                config.isSld_body,
                undefined,
                me.tryGetCapabilitiesFirst,
                // callback function. Populates the array of legend urls
                function(url) {                    
                    if (sourceUrls.indexOf(url) == -1) {                                                    
                        sourceUrls.push(url);                           
                    }
                }
            );
        };    
        
    	var useableImage = false;
    	
    	// now loop through looking for a useable image
        for (loopIndex = 0; loopIndex < sourceUrls.length; loopIndex++) {                            
            if (useableImage)
             	break;
                
            var url = sourceUrls[loopIndex];
            var image = new Image();    
            image.onload = function() {                    
                if (this.height > 0) {               
                    var html='';
                    html += '<a target="_blank" href="' + this.src + '">';
                    html += '<img onerror="this.alt=\'There was an error loading this legend. Click here to try again in a new window or contact the data supplier.\'" alt="Loading legend..." src="' + this.src + '"/>';
                    html += '</a>';                        
                    config.form.setData(html);                    
                    me._setStyledFormHeight(config.form, this);
                    
                    useableImage = true;
                }   
            }; 
            image.src=url;            
        }               
    },
    
    _setStyledFormHeight : function(form, image) {
    	var dimensions = {maxWidth:330,height:30};
        dimensions.height += image.height;
        // Add extra to allow for spacing
        dimensions.height += (dimensions.height * 0.02);
        dimensions.maxWidth = Math.max(dimensions.maxWidth,image.width);
        form.setHeight(dimensions.height);
        form.setWidth(dimensions.maxWidth);        
    },
    
    _setFormHeight : function(form, url, dimensions) {
        var image = new Image(); 
        image.onload = function(){
            dimensions.height += image.height;
            // Add extra to allow for spacing
            dimensions.height += (dimensions.height * 0.02);
            dimensions.maxWidth = Math.max(dimensions.maxWidth,image.width);
            form.setHeight(dimensions.height);
            form.setWidth(dimensions.maxWidth);
        };
        image.src=url;
    },
    
    _determineWidth : function(applicationProfile, sld_body) {
        if (applicationProfile && applicationProfile.indexOf("Esri:ArcGIS Server") > -1) {
            return 300;
        } else if (sld_body) {
            return null;
        } else {
            return this.getWidth();
        }
    }

});