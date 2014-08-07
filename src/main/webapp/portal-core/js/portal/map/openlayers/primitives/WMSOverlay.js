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

        //var bbox = Ext.JSON.decode(cfg.layer.data.filterer.getMercatorCompatibleParameters()[portal.layer.filterer.Filterer.BBOX_FIELD]);

        // bounds must be in EPSG:3857
        var bounds = this.getBbox() ? this.getBbox().transform(this.getBbox_crs(),"EPSG:3857") : null;

        if(this.getSld_body() && this.getSld_body().length > 0){
             wmsLayer = new OpenLayers.Layer.WMS( this.getWmsLayer(),
                    this.getWmsUrl(),
                    {
                         layers: this.getWmsLayer(),
                         version:wmsVersion ,
                         transparent : true,
                         exceptions : 'BLANK',
                         sld_body : this.getSld_body(),
                         tiled:true
                    },{
                         tileOptions: {maxGetUrlLength: 1500},
                         isBaseLayer : false,
                         projection: 'EPSG:3857',
                         maxExtent: bounds,
                         tileOrigin: new OpenLayers.LonLat(-20037508.34, -20037508.34)
                    });
        }else{
            wmsLayer = new OpenLayers.Layer.WMS( this.getWmsLayer(),
                    this.getWmsUrl(),
                    {
                        layers: this.getWmsLayer(),
                        version:wmsVersion ,
                        exceptions : 'BLANK',
                        transparent : true,
                        tiled:true
                    },{
                        tileOptions: {maxGetUrlLength: 1500},
                        isBaseLayer : false,
                        projection: 'EPSG:3857',
                        maxExtent: bounds,
                        tileOrigin: new OpenLayers.LonLat(-20037508.34, -20037508.34)
                    });
        }

        wmsLayer.setOpacity(this.getOpacity());
        wmsLayer._portalBasePrimitive = this;

        this.setWmsLayer(wmsLayer);
    }
});