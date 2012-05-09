/**
 * Concrete implementation for OpenLayers
 */
Ext.define('portal.map.openlayers.PrimitiveManager', {
    extend: 'portal.map.BasePrimitiveManager',

    vectorLayer : null,

    layers : null,
    vectors : null,

    /**
     * {
     *  baseMap : portal.map.BaseMap - The map instance that created this primitive manager,
     *  vectorLayer : OpenLayers.Layer.Vector - where vectors will be added
     * }
     */
    constructor : function(config) {
        this.callParent(arguments);

        this.vectorLayer = config.vectorLayer;

        this.layers = [];
        this.vectors = [];
    },

    /**
     * See parent class for info
     */
    clearPrimitives : function() {
        for (var i = 0; i < this.layers.length; i++) {
            this.layers[i].destroy();
        }
        this.layers = [];

        this.vectorLayer.removeFeatures(this.vectors);
        for (var i = 0; i < this.vectors.length; i++) {
            this.vectors[i].destroy();
        }
        this.vectors = [];

        this.fireEvent('clearprimitives', this);
    },

    /**
     * See parent class for info
     */
    addPrimitives : function(primitives) {
        var markers = [];
        var vectors = [];

        //sort our primitives into vectors and markers
        for (var i = 0; i < primitives.length; i++) {
            var prim = primitives[i];

            if (prim instanceof portal.map.openlayers.primitives.Marker ||
                    prim instanceof portal.map.openlayers.primitives.Polygon ||
                    prim instanceof portal.map.openlayers.primitives.Polyline) {
                vectors.push(prim.getVector())
            } else if (prim instanceof portal.map.openlayers.primitives.WMSOverlay) {
                var layer = prim.getWmsLayer();
                this.layers.push(layer);
                this.baseMap.map.addLayer(layer);
            }
        }

        if (vectors.length > 0) {
            this.vectorLayer.addFeatures(vectors);
            for (var i = 0; i < vectors.length; i++) {
                this.vectors.push(vectors[i]);
            }
        }

        this.fireEvent('addprimitives', this, primitives);
    }
});