/**
 * Represents a simple Polyline (series of straight line segments) as implemented by the Gmap API
 */
Ext.define('portal.map.openlayers.primitives.WMSOverlay', {

    extend : 'portal.map.primitives.BaseWMSPrimitive',

    config : {
        wmsLayer : null
    },

    /**
     * Accepts portal.map.primitives.BaseWMSPrimitive constructor args as well as
     *
     * map : Instance of GMap2
     */
    constructor : function(cfg) {
        this.callParent(arguments);
        var wmsVersion='1.1.1';
        if(this.getVersion() && this.getVersion().length > 0){
            wmsVersion=this.getVersion();
        }

        var wmsLayer = new OpenLayers.Layer.WMS( this.getWmsLayer(),
                this.getWmsUrl(),
                {layers: this.getWmsLayer(), version:wmsVersion ,transparent : true},
                {isBaseLayer : false});
        wmsLayer.setOpacity(this.getOpacity());
        wmsLayer._portalBasePrimitive = this;

        this.setWmsLayer(wmsLayer);
    }
});