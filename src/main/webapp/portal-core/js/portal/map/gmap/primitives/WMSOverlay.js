/**
 * Represents a simple Polyline (series of straight line segments) as implemented by the Gmap API
 */
Ext.define('portal.map.gmap.primitives.WMSOverlay', {

    extend : 'portal.map.primitives.BaseWMSPrimitive',

    config : {
        /**
         * GTileLayer instance
         */
        tileLayer : null,
        tileLayerOverlay : null
    },

    /**
     * Accepts portal.map.primitives.BaseWMSPrimitive constructor args as well as
     *
     * map : Instance of GMap2
     */
    constructor : function(cfg) {
        this.callParent(arguments);

        var tileLayer = new GWMSTileLayer(cfg.map, new GCopyrightCollection(""), 1, 17);
        tileLayer._portalBasePrimitive = this;
        tileLayer.baseURL = this.getWmsUrl();
        tileLayer.layers = this.getWmsLayer();
        tileLayer.opacity = this.getOpacity();

        var overlay = new GTileLayerOverlay(tileLayer);
        overlay._portalBasePrimitive = this;

        this.setTileLayer(tileLayer);
        this.setTileLayerOverlay(overlay);
    }
});