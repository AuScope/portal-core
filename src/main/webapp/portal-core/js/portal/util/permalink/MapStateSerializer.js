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
        this.serializer = tmpSerializer; //we serialize using the latest version

        this.callParent(arguments);
    },

    /**
     * Adds the specified portal.util.gmap.GMapWrapper state to this serializer
     */
    addMapState : function(map) {
        this.mapState = {
            center : {
                lat : map.getCenter().latitude,
                lng : map.getCenter().longitude
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
                source : source,
                visible : renderer.getVisible()
            };
        } else if (source === portal.layer.Layer.CSW_RECORD) {
            var cswRecord = layer.get('cswRecords')[0];

            return {
                filter : filterer.getParameters(),
                source : source,
                visible : renderer.getVisible(),
                onlineResources : this._serializeOnlineResources(cswRecord.get('onlineResources'))
            };
        }

        return null;
    },

    /**
     * Extracts all active layers and adds them
     * to the stored map state
     *
     * @param layerStore an instance of portal.layer.LayerStore
     */
    addLayers : function(layerStore) {
        for (var i = 0; i < layerStore.getCount(); i++) {
            var layer = layerStore.getAt(i);

            if (!layer) {
                continue;
            }

            var serializedLayer = this._serializeLayer(layer);
            this.serializedLayers.push(serializedLayer);
        }
    },

    /**
     * Returns a string that represents the entire state of this serializer
     */
    serialize : function() {
        var serializationObj = this.serializer.serialize(this.mapState, this.serializedLayers);
        serializationObj.v = this.serializer.getVersion();

        var serializationString = Ext.JSON.encode(serializationObj);
        serializationString = portal.util.Base64.encode(serializationString);
        return serializationString;
    },

    //Given a serializationObject this function attempts to calculate the 'schema' of the serialization object for backwards compatiblity
    _guessSerializationObjVersion : function(serializationObj) {
        if (Ext.isNumber(serializationObj.v)) {
            return serializationObj.v;
        }

        //This is to support the original serialization object which had no version stamp
        return 0;
    },

    /**
     * Attemps to deserialize the specified serializationString and apply its state to this object.
     *
     * Throws an exception if the serialization string is in a format that cannot be recognised.
     */
    deserialize : function(serializationString) {
        serializationString = portal.util.Base64.decode(serializationString);
        var serializationObj = Ext.JSON.decode(serializationString);

        var deserializer = this.baseSerializers[this._guessSerializationObjVersion(serializationObj)];
        if (!deserializer) {
            throw 'Unsupported serialization version';
        }

        var deserializedState = deserializer.deserialize(serializationObj);
        this.mapState = deserializedState.mapState;
        this.serializedLayers = deserializedState.serializedLayers;
    }
});
