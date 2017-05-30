/**
 * A Layer is what a portal.csw.CSWRecord or portal.knownlayer.KnownLayer becomes
 * when the user wishes to add it to the map.
 *
 * i.e. What a collection of service URL's becomes so that the GUI can render and
 * make the resulting data interactive
 */
Ext.define('portal.layer.Layer', {
    extend: 'Ext.data.Model',

    statics : {
        KNOWN_LAYER : 'KnownLayer', //A value for 'sourceType'
        CSW_RECORD : 'CSWRecord', //A value for 'sourceType'
        KML_RECORD : 'KMLRecord'
    },

    
    // TODO: Deprecate?
    visible : true,

    fields: [
        { name: 'id', type: 'string' }, //A unique ID of this layer - sourced from the original KnownLayer/CSWRecord
        { name: 'sourceType', type: 'string' }, //an 'enum' representing whether this Layer was constructed from a KnownLayer or CSWRecord
        { name: 'source', type: 'auto' }, //a reference to an instance of portal.knownlayer.KnownLayer or portal.csw.CSWRecord that was used to create this layer
        { name: 'name', type: 'string' }, //A human readable name/title of this layer
        { name: 'description', type: 'string' }, //A human readable description/abstract of this layer
        { name: 'renderer', type: 'auto' }, //A concrete implementation of a portal.layer.renderer.Renderer
        { name: 'filterer', type: 'auto' }, //A concrete implementation of a portal.layer.filterer.Filterer
        { name: 'downloader', type: 'auto' }, //A concrete implementation of a portal.layer.downloader.Downloader
        { name: 'querier', type: 'auto' }, //A concrete implementation of a portal.layer.querier.Querier
        { name: 'cswRecords', type: 'auto'}, //The source of all underlying data is an array of portal.csw.CSWRecord objects
        { name: 'loading', type: 'boolean', defaultValue: false },//Whether this layer is currently loading data or not
        { name: 'active', type: 'boolean', defaultValue: false },//Whether this layer is current active on the map.
        { name: 'visible', type: 'boolean', defaultValue: true }, // Whether this layer is visible
        { name: 'filterForm', type: 'auto'}, //The portal.layer.filterer.BaseFilterForm that houses the GUI for editing this layer's filterer
        { name: 'renderOnAdd', type: 'boolean', defaultValue: false }, //If true then this layer should be rendered the moment it is added as a layer. Currently used by KML
        { name: 'deserialized', type: 'boolean', defaultValue: false }, //If true then this layer has been deserialized from a permanent link
        { name: 'singleTile', type: 'boolean', defaultValue: false},    // Whether the layer should be requested as a single image (ie not tiled)
        { name: 'staticLegendUrl', type: 'string'} // A URL to use to grab a canned legend graphic for the layer (optional).
    ],

    /**
     * Utility function for concatenating all online resources stored in all
     * CSWRecords and returning the result as an Array. Online resources
     * that are sourced from a known layer with defined nagiosFailingHosts
     * that are "known failing" will be omitted. To force the inclusion of
     * all online resources set includeFailingHosts to true.
     *
     * returns an Array of portal.csw.OnlineResource objects
     */
    getAllOnlineResources : function(includeFailingHosts) {
        
        var failingHosts = null;
        if (!includeFailingHosts) {
            if (this.get('sourceType') === portal.layer.Layer.KNOWN_LAYER) {
                failingHosts = this.get('source').get('nagiosFailingHosts');
            }
        }
        
        var resources = [];
        var cswRecords = this.get('cswRecords');
        for (var i = 0; i < cswRecords.length; i++) {
            if (includeFailingHosts || Ext.isEmpty(failingHosts)) {
                resources = resources.concat(cswRecords[i].get('onlineResources'));
            } else {
                Ext.each(cswRecords[i].get('onlineResources'), function(or) {
                    var isFailing = false;
                    Ext.each(failingHosts, function(host) {
                        if (or.get('url').indexOf(host) >= 0) {
                            isFailing = true;
                            return false;
                        }
                    });
                    
                    if (!isFailing) {
                        resources.push(or);
                    }
                });
            }
            
        }
        return resources;
    },
    
    setLayerVisibility : function(visibility){
        this.get('renderer').setVisibility(visibility);
        // TODO: Deprecate?
        this.visible=visibility;
        this.set('visible',visibility);
    },                

    onRenderStarted : function(renderer, onlineResources, filterer) {
        this.set('loading', true);
        this.set('active', true);
        this.get('source').set('loading', true);
        this.get('source').set('active', true);
    },

    /** Called when this layer is completely rendered.
     * Each renderer is responsible for firing the renderfinished
     * event when all of its resources have been rendered to the map.
     * 
     * @param renderer the layer renderer that just fired the renderfinished event
     */
    onRenderFinished : function(renderer) {
        //this.set('loading', false);
        this.get('source').set('loading', false);
        this.set('loading', false);

        var map = renderer.map;
        var layerStore = map.layerStore;

        var l = 0;
        var zIndex = 0;
        for (var i = layerStore.data.items.length-1; i >= 0; --i) {
            var onlineResourcesForLayer = [];
            var cswRecords = layerStore.data.items[i].data.cswRecords;
            for (var j = 0; j < cswRecords.length; j++) {
			    if (cswRecords[j].data.onlineResources)
                    onlineResourcesForLayer = onlineResourcesForLayer.concat(cswRecords[j].data.onlineResources);
            }

            var layerNameArray = [];
            for (var j = 0; j < onlineResourcesForLayer.length; j++) {
                var layerName = onlineResourcesForLayer[j].data.name;
                var mapLayers = map.map.getLayersByName(layerName);

                if (layerNameArray.indexOf(layerName) < 0)
                {
                    layerNameArray.push(layerName);
                    if (mapLayers && mapLayers.length > 0) {
                        for (var k = 0; k < mapLayers.length; k++) {
                        // construct a useable z-index for the layer on the map
                        var zIndex = zIndex + 1;
                        map.map.setLayerZIndex(mapLayers[k], zIndex);
                        }
                    }
                }
            }
            l = l + 100;
        }

        // float the vector root containers to the top of the map so that they can be clicked on
        for (var i = 0; i < map.map.layers.length; i++) {
            var layer = map.map.layers[i];
            if (layer.id.indexOf('OpenLayers_Layer_Vector_RootContainer') != -1) {
                map.map.setLayerZIndex(layer, 20000 + i);
            }
        }
    },


    /**
     * Whenever our filter changes, update the rendered page
     */
    onFilterChanged : function(filterer, keys) {
        this.reRenderLayerDisplay(filterer, keys);
    },
    
    reRenderLayerDisplay : function(filterer, keys) {
        var renderer = this.get('renderer');      
        this.removeDataFromMap();                  
        renderer.displayData(this.getAllOnlineResources(), this.get('filterer'), Ext.emptyFn);
    },
    
   removeDataFromMap:function(){
       var renderer = this.get('renderer');       
       renderer.removeData();
       renderer.map.closeInfoWindow(this.get('id')); 
       this.get('source').set('active', false);
   },

    getCSWRecordsByKeywords : function(keyword){
        //Filter our results
        var results = [];
        var cswRecords=this.get('cswRecords');
        for(var i=0; i < cswRecords.length;i++){
            if(cswRecords[i].containsKeywords(keyword)){
                results.push(cswRecords[i]);
            }
        }
        return results;
    },
    
    getCSWRecordsByResourceURL : function(resourceURL){
        //Filter our results
        var results = [];
        var cswRecords=this.get('cswRecords');
        for(var i=0; i < cswRecords.length;i++){
            if(cswRecords[i].containsOnlineResourceUrl(resourceURL)){
                results.push(cswRecords[i]);
            }            
        }
        return results;
    },

    containsCSWService : function() {
        // If the layer is a known layer, then ask the KnownLayer
        // object if it contains a CSW service endpoint:
        if (this.get('sourceType') == portal.layer.Layer.KNOWN_LAYER) {
            return this.get('source').containsCSWService();
        }

        return false;
    }
});



