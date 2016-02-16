/**
 * MapStateSerializer
 *
 * A 'class' for taking in various elements of the map state and serializing
 * them into a simple string that can be stored. Deserialization is also supported
 *
 * Currently custom map layers are unsupported.
 */
Ext.define('portal.util.permalink.MapStateSerializer', {

    /**
     * An object with the schema {
     *      center : {
     *          lng : Number
     *          lat : Number
     *      },
     *      zoom : Number
     * }
     */
    mapState : null,

    /**
     * An array of objects which have one of the following schemas
     *
     *  {
     *      source : String //Always set to 'KnownLayer'
     *      id : String,
     *      filter : Object,
     *      visible : Boolean
     *  }
     *
     *  {
     *      source : String //Always set to 'CSWRecord'
     *      filter : Object,
     *      visible : Boolean,
     *      onlineResources : [{
     *          url                 : String
     *          onlineResourceType  : String
     *          name                : String
     *          description         : String
     *      }]
     *  }
     */
    serializedLayers : null,

    /**
     * Instance of portal.util.permalink.serializers.BaseSerializer that
     * will be used for SERIALIZATION.
     *
     * Deserialization will still occur from baseSerializers list
     */
    serializer : null,

    /**
     * An object containing portal.util.permalink.serializers.BaseSerializer keyed
     * by their reported versions
     */
    baseSerializers : null,

    constructor : function() {
        this.mapState = {};
        this.serializedLayers = [];

        //Build our list of serializers
        this.baseSerializers = {};
        var tmpSerializer = Ext.create('portal.util.permalink.serializers.SerializerV0');
        this.baseSerializers[tmpSerializer.getVersion()] = tmpSerializer;
        tmpSerializer = Ext.create('portal.util.permalink.serializers.SerializerV1');
        this.baseSerializers[tmpSerializer.getVersion()] = tmpSerializer;
        tmpSerializer = Ext.create('portal.util.permalink.serializers.SerializerV2');
        this.baseSerializers[tmpSerializer.getVersion()] = tmpSerializer;
        tmpSerializer = Ext.create('portal.util.permalink.serializers.SerializerV3');
        this.baseSerializers[tmpSerializer.getVersion()] = tmpSerializer;
        tmpSerializer = Ext.create('portal.util.permalink.serializers.SerializerV4');
        this.baseSerializers[tmpSerializer.getVersion()] = tmpSerializer;
        this.serializer = tmpSerializer; //we serialize using the latest version

        this.callParent(arguments);
    },

    /**
     * Adds the specified portal.util.gmap.GMapWrapper state to this serializer
     */
    addMapState : function(map) {
        this.mapState = {
            center : {
                lat : map.getCenter().getLatitude(),
                lng : map.getCenter().getLongitude()
            },
            zoom : map.getZoom()
        };
    },

    /**
     * Internal use only - returns an array of objects representing a bare minimum OnlineResource
     */
    _serializeOnlineResources : function(orArray) {
        var serialized = [];
        for (var i = 0; i < orArray.length; i++) {
            serialized.push({
                url : orArray[i].get('url'),
                type : orArray[i].get('type'),
                name : orArray[i].get('name'),
                description : orArray[i].get('description')
            });
        }
        return serialized;
    },

    /**
     * Internal use only - returns an object representing a bare minimum activeLayerRecord
     */
    _serializeLayer : function(layer) {
        var source = layer.get('sourceType');
        var filterer = layer.get('filterer');
        var renderer = layer.get('renderer');

        //Known layers have a persistable ID, CSWRecords do NOT
        //This means we have to do a 'best effort' to identify a CSWRecord
        if (source === portal.layer.Layer.KNOWN_LAYER) {
            return {
                id : layer.get('id'), //This is only persistent for KnownLayers
                filter : filterer.getParameters(),
                source : source               
            };            
        } else if (source === portal.layer.Layer.CSW_RECORD || source === 'search') {
            var cswRecord = layer.get('cswRecords')[0];

            return {
                filter : filterer.getParameters(),
                source : source,   
                customlayer : cswRecord.get('customlayer'),
                onlineResources : this._serializeOnlineResources(cswRecord.get('onlineResources'))
            };
        }

        return null;
    },

    /**
     * Extracts all active layers from the map and adds them
     * to the stored map state.
     *
     * @param map the OL map wrapper. Contains a portal.layer.LayerStore.
     */
    addLayers : function(map) {
        // get the map's active layer store
        var activeLayerStore = ActiveLayerManager.getActiveLayerStore(map);
        if (activeLayerStore) {
            for (var i = 0; i < activeLayerStore.getCount(); i++) {
                var layer = activeLayerStore.getAt(i);

                //VT: Unable to support KML perm link at this stage because of the source and size of the kml file.
                if (!layer || layer.get('sourceType') === portal.layer.Layer.KML_RECORD) {
                    continue;
                }

                var serializedLayer = this._serializeLayer(layer);
                this.serializedLayers.push(serializedLayer);
            }
        }
    },

    /**
     * Generates a string that represents the entire state of this serializer
     * 
     * callback - function(string state, string version) will be passed the serialisation string asynchronously and the identifiable version of the serialiser used in the encoding
     */
    serialize : function(callback) {      
        this.serializer.serialize(this.mapState, this.serializedLayers, Ext.bind(function(stateStr) {            
            callback(portal.util.Base64.encode(stateStr), this.serializer.getVersion());
        }, this));
    },

    //Given a serialization string this function attempts to calculate the 'schema' of the serialization object for backwards compatiblity
    _guessSerializationVersion : function(serializationStr) {
        //Does this even remotely resemble JSON??
        if (serializationStr &&
                serializationStr.charAt(0) === '{' &&
                serializationStr.charAt(serializationStr.length - 1) === '}') {
            var serializationObj = Ext.JSON.decode(serializationStr);
            
            if (Ext.isNumber(serializationObj.v)) {
                return serializationObj.v;
            }
            
            //This is to support the original serialization object which had no version stamp
            return 0;
        } else {
            //Then it's probably LZMA encoded
            return 3;
        }
    },

    /**
     * Attempts to deserialize (asynchronously) the specified serializationString and 
     * apply its state to this object.
     *
     * serializationString - String - A serialization string generated by a prior call to serialize
     * serializationVersion - String - The serializer version used to encode serializationString. If null, this will be guessed  
     * callback - function(boolean, String) - Passed a boolean with success/failure AFTER deserialisation has finished
     */
    deserialize : function(serializationString, serializationVersion, callback) {
        var b64Decoded = portal.util.Base64.decode(serializationString);
        
        if (!serializationVersion) {
            serializationVersion = this._guessSerializationVersion(b64Decoded);
        }
        
        var deserializer = this.baseSerializers[serializationVersion];
        if (!deserializer) {
            callback(false, 'Unsupported serialization version');
            return;
        }

        deserializer.deserialize(b64Decoded, Ext.bind(function(deserializedState) {
            this.mapState = deserializedState.mapState;
            this.serializedLayers = deserializedState.serializedLayers;
            
            callback(true);
        }, this));
    }
});
