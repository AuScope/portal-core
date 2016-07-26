/**
 * This class exposes static methods for manipulating the map's layerStore.
 * We call this store the "Active" layer store to help differentiate it from the other layer stores
 * in the application, for example the stores backing the Known Layers and Custom Layers panels. 
 * 
 * ActiveLayerManager does not store any state and isn't meant to be instantiated, it is just 
 * a Singleton with static utility methods.
 * 
 * Because a lot of the components have the activeLayerStore wired in to their config
 * they automatically update their UI when the store is changed. 
 * 
 * It is recommended that this class be the ONLY mechanism for changing the active layer store
 * purely because it makes it easy to maintain and debug if changes are localised.
 */
Ext.define('portal.map.openlayers.ActiveLayerManager', {
    singleton : true,
    alternateClassName: ['ActiveLayerManager'],
    constructor : function(config) {
        this.initConfig(config);        
    },    

    /** adds the given layer to the active layer store */
    addLayer : function(layer) {        
        if (layer) {
            var map = layer.get('renderer').map;
            var activeLayerStore = ActiveLayerManager.getActiveLayerStore(map);
            activeLayerStore.suspendEvents(true);
            // The insert() used below will trigger removal events and an event driven clean up operation.
            // So we set a flag to block it.
            if (activeLayerStore.getCount()>0) {
                activeLayerStore.addingLayer=true;
            }
            activeLayerStore.insert(0,layer);
            activeLayerStore.resumeEvents();
            this.saveApplicationState(map);
        }
    },
    
    /** adds an array of layers to the active layer store */
    addLayers : function(layers) {
        if (layers && layers.length > 0) {
            var map = layers[0].get('renderer').map;
            var activeLayerStore = ActiveLayerManager.getActiveLayerStore(map);
            for (var i = 0; i < layers.length; i++) {
                activeLayerStore.suspendEvents(true);
                activeLayerStore.add(layers[i]);    
                activeLayerStore.resumeEvents();
            }
        }
    },
    
    /** removes the given layer from the active layer store */
    removeLayer : function(layer) {
        if (layer) {
            var map = layer.get('renderer').map;
            var activeLayerStore = ActiveLayerManager.getActiveLayerStore(map);
            activeLayerStore.suspendEvents(true);
            activeLayerStore.remove(layer);
            layer.removeDataFromMap();
            activeLayerStore.resumeEvents();
            this.saveApplicationState(map);
        }
    },    
    
    /** removes all layers from the active layer store */
    removeAllLayers : function(map) {
        var activeLayerStore = ActiveLayerManager.getActiveLayerStore(map);
        activeLayerStore.suspendEvents(true);
        activeLayerStore.each (function (layer) {        
            layer.removeDataFromMap();
        });
        activeLayerStore.removeAll();
        activeLayerStore.resumeEvents();
        this.saveApplicationState(map);
    },
    
    /** updates the order of the layers in the store and in the map */
    updateLayerOrder : function(map, layer) {
        layer.reRenderLayerDisplay();
        this.saveApplicationState(map);
        
    },   
    
    /** Gets the active layer store. 
     * If configured with a storeId of 'activeLayerStore' we can look it up from the ExtJS store manager. 
     * If not we should be able to get it from the optional map parameter. 
     * The store manager method is faster although this is normally a one-off interaction
     * coming from the UI so it is really just a few nanoseconds difference.
     * 
     * This method could return null or undefined if the storeId is not configured and the map passed in is null
     * (or the map doesn't have a configured layerStore in which case we have bigger issues - check your Main-UI).
     */
    getActiveLayerStore : function(map) {
        var activeLayerStore = Ext.StoreMgr.lookup("activeLayerStore");
        if (!activeLayerStore && map) {
            activeLayerStore = map.layerStore;
        }
        return activeLayerStore;
    },
    
    /** Saves the map's state using a MapStateSerializer.
     * 
     *  The interesting state includes:
     *   - the map zoom level
     *   - the current map bounds (or rather the center point)
     *   - the currently active layers
     */
    saveApplicationState : function(map) {
        if (map) {
            
            if(typeof(Storage) !== "undefined") {
                var mss = Ext.create('portal.util.permalink.MapStateSerializer');
                
                mss.addMapState(map);
                mss.addLayers(map);                
                
                mss.serialize(function(state, version) {
                    localStorage.setItem("portalStorageApplicationState", state);
                    localStorage.setItem("portalStorageDefaultBaseLayer", map.map.baseLayer.name);
                });
            }
        }
    }
});
