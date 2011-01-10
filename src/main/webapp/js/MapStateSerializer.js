/**
 * MapStateSerializer
 * 
 * A 'class' for taking in various elements of the map state and serializing
 * them into a simple string that can be stored. Deserialization is also supported
 * 
 * Currently custom map layers are unsupported.
 */


MapStateSerializer = function() {
    this.mapState = {};
    this.activeLayers = [];
};

MapStateSerializer.prototype.mapState = null;
MapStateSerializer.prototype.activeLayers = null;


MapStateSerializer.prototype.addMapState = function(map) {
    this.mapState = {
            center : {
                lat : map.getCenter().lat(),
                lng : map.getCenter().lng()
            },
            zoom : map.getZoom()
    };
};

/**
 * Internal use only - returns an object representing a bare minimum activeLayerRecord
 */
MapStateSerializer.prototype.serializeActiveLayer = function(activeLayerRecord) {
    var source = activeLayerRecord.getSource();
    
    //Known layers have a persistable ID, CSWRecords do NOT
    //This means we have to do a 'best effort' to identify a CSWRecord
    if (source === 'KnownLayer') {
        return {
            id : activeLayerRecord.getId(), //This is only persistent for KnownLayers
            filter : activeLayerRecord.getLastFilterParameters(),
            opacity : activeLayerRecord.getOpacity(),
            source : source,
            visible : activeLayerRecord.getLayerVisible()
        };
    } else if (source === 'CSWRecord') {
        var cswRecord = activeLayerRecord.getCSWRecords()[0];
        return {
            opacity : activeLayerRecord.getOpacity(),
            source : source,
            visible : activeLayerRecord.getLayerVisible(),
            onlineResources : cswRecord.getOnlineResources()
        };
    }
};

/**
 * Extracts all active layers and adds them
 * to the stored map state
 * 
 * param activeLayersStore An instance of ActiveLayersRecord
 * 
 */
MapStateSerializer.prototype.addActiveLayers = function(activeLayersStore) {
    for (var i = 0; i < activeLayersStore.getCount(); i++) {
        activeLayerRecord = activeLayersStore.getActiveLayerAt(i);
        
        if (!activeLayerRecord) {
            continue;
        }
        
        var serializedActiveLayer = this.serializeActiveLayer(activeLayerRecord);
        this.activeLayers.push(serializedActiveLayer);
    }
};

/**
 * Returns a string that represents the entire state of this serializer
 */
MapStateSerializer.prototype.serialize = function() {
    var serializationObj = {
            mapState : this.mapState,
            activeLayers : this.activeLayers
    };
    
    var serializationString = Ext.util.JSON.encode(serializationObj);
    serializationString = Base64.encode( serializationString);
    return serializationString;
};

MapStateSerializer.prototype.deserialize = function(serializationString) {
    serializationString = Base64.decode(serializationString);
    var serializationObj = Ext.util.JSON.decode(serializationString);
    
    this.mapState = serializationObj.mapState;
    this.activeLayers = serializationObj.activeLayers;
};