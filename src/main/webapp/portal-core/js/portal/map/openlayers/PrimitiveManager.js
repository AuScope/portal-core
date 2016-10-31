/**
 * Concrete implementation for OpenLayers
 */
Ext.define('portal.map.openlayers.PrimitiveManager', {
    extend: 'portal.map.BasePrimitiveManager',

    vectorLayer : null,
    vectorLayerGenerator : null,
    layers : null,
    vectors : null,

    /**
     * {
     *  baseMap : portal.map.BaseMap - The map instance that created this primitive manager,
     *  vectorLayerGenerator : function(this) - Called once on demand, should return a OpenLayers.Layer.Vector where vectors will be added by this class
     *  noLazyGeneration: Boolean - If true, this will force the vectorLayerGenerator to be fired immediately
     * }
     */
    constructor : function(config) {
        this.callParent(arguments);

        this.vectorLayer = null;
        this.vectorLayerGenerator = config.vectorLayerGenerator;

        this.layers = [];
        this.vectors = [];
        
        if (config.noLazyGeneration) {
            this.getVectorLayer();
        }
    },

    getVectorLayer: function() {
        if (this.vectorLayer) {
            return this.vectorLayer;
        } else {
            return this.vectorLayer = this.vectorLayerGenerator(this);
        }
    },

    setVisibility : function(visibility){
        this.getVectorLayer().setVisibility(visibility);
        
        for (var i = 0; i < this.layers.length; i++) {
            this.layers[i].setVisibility(visibility);
        }
    },

    /**
     * See parent class for info
     */
    clearPrimitives : function() {
        for (var i = 0; i < this.layers.length; i++) {
            this.layers[i].destroy();
        }
        this.layers = [];

        this.getVectorLayer().removeFeatures(this.vectors);
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
                //VT: add wms primitive in the store order if exist
                var layerId=prim.getLayer().data.id;
                var layerStore = this.baseMap.layerStore.data.items;
                var position = 0;
                //layerStore provides the ordering
                for(position=0;position < layerStore.length; position++){
                    if(layerId==layerStore[position].data.id){
                        break;
                    }
                }

                var layer = prim.getWmsLayer();
                this.layers.push(layer);
                this.baseMap.map.addLayer(layer);

                //VT: this will give us the order where we should be slotting into the map layer
                position = this.baseMap.map.layers.length - 1 - position;
                this.baseMap.map.setLayerIndex(layer,position);
            }
        }

        if (vectors.length > 0) {
            this.getVectorLayer().addFeatures(vectors);
            for (var i = 0; i < vectors.length; i++) {
                this.vectors.push(vectors[i]);
            }
        }

        this.fireEvent('addprimitives', this, primitives);
    }
});