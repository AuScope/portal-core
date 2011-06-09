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

/**
 * An object with the schema {
 * 		center : {
 * 			lng : Number
 * 			lat : Number
 *  	},
 *  	zoom : Number
 * }
 */
MapStateSerializer.prototype.mapState = null;

/**
 * An array of objects which have one of the following schemas 
 * 
 * 	{
 * 		source : String //Always set to 'KnownLayer'
 * 		id : String,
 * 		filter : Object
 * 		opacity : Number,
 * 		visible : Boolean			
 * 	}
 * 
 * 	{
 * 		source : String //Always set to 'CSWRecord'
 * 		opacity : Number,
 * 		visible : Boolean,
 * 		onlineResources : [{
 * 			url					: String
 *  		onlineResourceType 	: String
 *  		name				: String
 *  		description			: String
 * 		}] 			
 * 	}
 */
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
	
	//We strip out a lot of overhead by only limiting the size of our field definitions
    var serializationObj = {
            m : {
    			a : this.mapState.center.lat,
    			n : this.mapState.center.lng,
    			z : this.mapState.zoom
    		},
            a : [],
            v : 1 //we are using version 1 serialization, increment this if you make changes to the serialization objects
    };
    
    //We also do the same by serializing our objects into 'shorter' variants of themselves that the deserialization step will take care of
    for (var i = 0; i < this.activeLayers.length; i++) {
    	var onlineResources = undefined;
    	if (Ext.isArray(this.activeLayers[i].onlineResources)) {
    		onlineResources = [];
    		for (var j = 0; j < this.activeLayers[i].onlineResources.length; j++) {
    			onlineResources.push({
    				u : this.activeLayers[i].onlineResources[j].url,
    				o : this.activeLayers[i].onlineResources[j].onlineResourceType,
    				n : this.activeLayers[i].onlineResources[j].name,
    				d : this.activeLayers[i].onlineResources[j].description
    			});
    		}
    	}
    	
    	serializationObj.a.push({
    		s : this.activeLayers[i].source,
    		f : this.activeLayers[i].filter,
    		o : this.activeLayers[i].opacity,
    		v : this.activeLayers[i].visible,
    		i : this.activeLayers[i].id,
    		r : onlineResources
    	});
    }
    
    var serializationString = Ext.util.JSON.encode(serializationObj);
    serializationString = Base64.encode( serializationString);
    return serializationString;
};

/**
 * Attemps to deserialize the specified serializationString and apply its state to this object.
 * 
 * Throws an exception if the serialization string is in a format that cannot be recognised.
 */
MapStateSerializer.prototype.deserialize = function(serializationString) {
	serializationString = Base64.decode(serializationString);
	var serializationObj = Ext.util.JSON.decode(serializationString);
	
	switch (this.internalGuessSerializationObjVersion(serializationObj)) {
	case 0:
		this.internalDeserializeV0(serializationObj);
		break;
	case 1:
		this.internalDeserializeV1(serializationObj);
		break;
	default:
		throw 'Unsupported serialization version';
	}
}

//Deserialisation function for version 0 
MapStateSerializer.prototype.internalDeserializeV0 = function(serializationObj) {
    this.mapState = serializationObj.mapState;
    this.activeLayers = serializationObj.activeLayers;
};

//Deserialisation function for version 1
MapStateSerializer.prototype.internalDeserializeV1 = function(serializationObj) {
    var mapState = serializationObj.m;
    var activeLayers = serializationObj.a;
    
    //We need to take our minified map state and explode it into a meaningful object
    this.mapState = {
    	center : {
    		lng : mapState.n,
    		lat : mapState.a
    	},
    	zoom : mapState.z
    };
    
    //We need to take our minified active layer list and explode it into a meaningful object
    this.activeLayers = [];
    for (var i = 0; i < activeLayers.length; i++) {
    	var onlineResources = undefined;
    	if (Ext.isArray(activeLayers[i].r)) {
    		onlineResources = [];
    		for (var j = 0; j < activeLayers[i].r.length; j++) {
    			onlineResources.push({
    				url : activeLayers[i].r[j].u,
    				onlineResourceType : activeLayers[i].r[j].o,
    				name : activeLayers[i].r[j].n,
    				description : activeLayers[i].r[j].d
    			});
    		}
    	}
    	
    	this.activeLayers.push({
    		source : activeLayers[i].s,
    		filter : activeLayers[i].f,
    		opacity : activeLayers[i].o,
    		visible : activeLayers[i].v,
    		id : activeLayers[i].i,
    		onlineResources : onlineResources
    	});
    }
};

//Given a serializationObject this function attempts to calculate the 'schema' of the serialization object for backwards compatiblity
MapStateSerializer.prototype.internalGuessSerializationObjVersion = function(serializationObj) {
	if (Ext.isNumber(serializationObj.v)) {
		return serializationObj.v;
	}
	
	return 0;
};