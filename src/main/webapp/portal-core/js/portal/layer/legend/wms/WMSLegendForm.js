/**
 * An implementation of portal.layer.legend.BaseComponent for providing
 * simple GUI details for a WMS Legend
 */
Ext.define('portal.layer.legend.wms.WMSLegendForm', {
    extend: 'portal.layer.legend.BaseComponent',

    constructor : function(config) {
 
        Ext.apply(config, {
            html    : '<p> Waiting for Legend data ...</p>'
        });
        this.callParent(arguments);
    },
    
    addLegends : function(config) {
        var wmsOnlineResources = portal.csw.OnlineResource.getFilteredFromArray(config.resources, portal.csw.OnlineResource.WMS);
        var urlsFound=wmsOnlineResources.length;
        var urls={};

        for (var j = 0; j < wmsOnlineResources.length; j++) {
            portal.layer.legend.wms.WMSLegend.generateLegendUrl(
                    wmsOnlineResources[j].get('url'), 
                    wmsOnlineResources[j].get('name'),
                    wmsOnlineResources[j].get('version'),
                    this.getWidth(), // 100,    // getWidth() ? - Won't work as don't know width until this constructor is finished
                    config.sld_body, undefined, function(url) {
                        if (! urls.hasOwnProperty(url)) {
                            // Add a WIDTH attribute to ?requests (but not to normal resource GETs ie. that don't contain a '?')
                            if (url.match(/width/i) === null && url.match(/\?/) !== null) {
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
                        }
                    });
            if(config.sld_body && config.sld_body.length > 0 && config.sld_body.length < 2000){
                //VT: if we are supplying the SLD, we are controlling the legends. Therefore we only need to
                // loop once
                break;
            }
        }

    }

});