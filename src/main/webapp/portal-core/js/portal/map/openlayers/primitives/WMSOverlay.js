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

        var bbox = Ext.JSON.decode(cfg.layer.data.filterer.getMercatorCompatibleParameters()[portal.layer.filterer.Filterer.BBOX_FIELD]);
        
        // bounds
        var bounds = bbox ? new OpenLayers.Bounds(bbox.westBoundLongitude, bbox.southBoundLatitude, bbox.eastBoundLongitude, bbox.northBoundLatitude) : null;
        
        var srs = bbox ? bbox.crs : null;
        
        if(this.getSld_body() && this.getSld_body().length > 0){
             wmsLayer = new OpenLayers.Layer.WMS( this.getWmsLayer(),
                    this.getWmsUrl(),
                    {
                         layers: this.getWmsLayer(), 
                         version:wmsVersion ,
                         transparent : true,
                         //VT: Geoserver wms 1.1.1 doesn't support blank currently
                         //exceptions : 'application/vnd.ogc.se_blank',
                         exceptions : 'application/vnd.ogc.se_inimage',
                         sld_body : this.getSld_body()
                    },{
                         tileOptions: {maxGetUrlLength: 1500}, 
                         isBaseLayer : false, 
                         projection: srs, 
                         maxExtent: bounds
                    });
        }else{
            wmsLayer = new OpenLayers.Layer.WMS( this.getWmsLayer(),
                    this.getWmsUrl(),
                    {
                        layers: this.getWmsLayer(), 
                        version:wmsVersion ,
                        //exceptions : 'application/vnd.ogc.se_blank',
                        exceptions : 'application/vnd.ogc.se_inimage',
                        transparent : true
                    },{
                        tileOptions: {maxGetUrlLength: 1500}, 
                        isBaseLayer : false, 
                        projection: srs, 
                        maxExtent: bounds
                    });
        }
        
        wmsLayer.setOpacity(this.getOpacity());
        wmsLayer._portalBasePrimitive = this;

        this.setWmsLayer(wmsLayer);
    }
});