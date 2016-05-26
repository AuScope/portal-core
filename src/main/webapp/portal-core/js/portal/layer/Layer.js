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
        //{ name: 'loading', type: 'boolean', defaultValue: false }, //Whether this layer is currently loading data or not
        { name: 'filterForm', type: 'auto'}, //The portal.layer.filterer.BaseFilterForm that houses the GUI for editing this layer's filterer
        { name: 'renderOnAdd', type: 'boolean', defaultValue: false }, //If true then this layer should be rendered the moment it is added as a layer. Currently used by KML
        { name: 'deserialized', type: 'boolean', defaultValue: false }, //If true then this layer has been deserialized from a permanent link
        { name: 'singleTile', type: 'boolean', defaultValue: false},    // Whether the layer should be requested as a single image (ie not tiled)
        { name: 'staticLegendUrl', type: 'string'} // A URL to use to grab a canned legend graphic for the layer (optional).
    ],

    /**
     * Utility function for concatenating all online resources stored in all
     * CSWRecords and returning the result as an Array.
     *
     * returns an Array of portal.csw.OnlineResource objects
     */
    getAllOnlineResources : function() {
        var resources = [];
        var cswRecords = this.get('cswRecords');
        for (var i = 0; i < cswRecords.length; i++) {
            resources = resources.concat(cswRecords[i].get('onlineResources'));
        }
        return resources;
    },
    
    setLayerVisibility : function(visibility){
        this.get('renderer').setVisibility(visibility);
        this.visible = visibility;
    },                

    onRenderStarted : function(renderer, onlineResources, filterer) {
        //this.set('loading', true);
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

        var map = renderer.map;
        var layerStore = map.layerStore;

        var l = 0;
        for (var i = layerStore.data.items.length-1; i >= 0; --i) {
            var onlineResourcesForLayer = [];
            var cswRecords = layerStore.data.items[i].data.cswRecords;
            for (var j = 0; j < cswRecords.length; j++) {
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
                        var zIndex = (layerStore.data.items.length - i) * (j*100) + k + l;
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

        // float the vector root containers to the top of the map so that they can be clicked on
        for (var i = 0; i < map.map.layers.length; i++) {
            var layer = map.map.layers[i];
            if (layer.id.indexOf('OpenLayers_Layer_Vector_RootContainer') != -1) {
                map.map.setLayerZIndex(layer, 10000 + i);
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
        
        var group = 'group';
        if(this.get('sourceType')=='CSWRecord'){
            group='contactOrg';
        }
        
        //VT: Custom layer doesn't contain group
        if(this.get('source').get(group)){
            this._expandGridGroup(this.get('source').get(group));
        }
                       
    },
    
    _expandGridGroup : function(groupname){
        var activeTab = Ext.getCmp('auscope-tabs-panel').getActiveTab();
        for (var i = 0; i < activeTab.features.length; i++) {
            if (activeTab.features[i] instanceof Ext.grid.feature.Grouping) {
                // try to expand the group but fail gracefully if not possible
                // for example because the group may be obtained from the "contactOrg" field
                // GA Portal thinks the group name of a Custom layer is "Geoscience Australia"
                try {
                    activeTab.features[i].expand(groupname,true);
                }
                catch(e) {}
            }
        }        
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



