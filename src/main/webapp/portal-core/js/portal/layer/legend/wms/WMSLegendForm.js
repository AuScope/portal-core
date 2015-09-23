/**
 * An implementation of portal.layer.legend.BaseComponent for providing
 * simple GUI details for a WMS Legend
 */
Ext.define('portal.layer.legend.wms.WMSLegendForm', {
    extend: 'portal.layer.legend.BaseComponent',

    constructor : function(config) {
        var html='';
        var wmsOnlineResources = portal.csw.OnlineResource.getFilteredFromArray(config.resources, portal.csw.OnlineResource.WMS);

        for (var j = 0; j < wmsOnlineResources.length; j++) {
            var url = portal.layer.legend.wfs.WMSLegend.generateImageUrl(
                    wmsOnlineResources[j].get('url'), 
                    wmsOnlineResources[j].get('name'),
                    wmsOnlineResources[j].get('version'),
                    config.sld_body);
            html += '<a target="_blank" href="' + url + '">';
            html += '<img onerror="this.alt=\'There was an error loading this legend. Click here to try again in a new window or contact the data supplier.\'" alt="Loading legend..." src="' + url + '"/>';
            html += '</a>';
            html += '<br/>';
            if(config.sld_body && config.sld_body.length > 0 && config.sld_body.length < 2000){
                //VT: if we are supplying the SLD, we are controlling the legends. Therefore we only need to
                // loop once
                break;
            }
        }

        Ext.apply(config, {
            html    : html
        });
        this.callParent(arguments);
    }

});