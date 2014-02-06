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

        var wmsLayer = null;
        if(this.getSld_body() && this.getSld_body().length > 0){
             wmsLayer = new OpenLayers.Layer.WMS( this.getWmsLayer(),
                    this.getWmsUrl(),
                    {layers: this.getWmsLayer(), version:wmsVersion ,transparent : true, sld_body : this.getSld_body()},
                    {tileOptions: {maxGetUrlLength: 2048}, isBaseLayer : false});
        }else{
            wmsLayer = new OpenLayers.Layer.WMS( this.getWmsLayer(),
                    this.getWmsUrl(),
                    {layers: this.getWmsLayer(), version:wmsVersion ,transparent : true},
                    {tileOptions: {maxGetUrlLength: 2048}, isBaseLayer : false});
        }
        wmsLayer.setOpacity(this.getOpacity());
        wmsLayer._portalBasePrimitive = this;

        this.setWmsLayer(wmsLayer);
    }
});