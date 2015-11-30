Ext.define('portal.util.permalink.serializers.SerializerV3', {
    extend : 'portal.util.permalink.serializers.SerializerV2',

    getVersion : function() {
        return 3;
    },

    serialize : function(mapState, serializedLayers, callback) {
        //Serialise as per V2 Serializer but then feed the results through LZMA compression
        //Return the compressed LZMA byte array (as a string)
        this.callParent([mapState, serializedLayers, function(stateString) {
            LZMA.compress(stateString, "1", function (result) {
                callback(String.fromCharCode.apply(String, result));
            });
        }]);
    },

    /**
     * See parent interface for details.
     */
    deserialize : function(stateString, callback) {
        //Turn LZMA string into byte array
        var compressedByteArray = [];
        for (var i = 0; i < stateString.length; i++) {
            compressedByteArray.push(stateString.charCodeAt(i));
        }

        //Decompress LZMA String, pass decompressed string to the V2 deserialiser
        //Return the results of the V2 deserialiser
        var parentDeserialize = portal.util.permalink.serializers.SerializerV2.prototype.deserialize;
        LZMA.decompress(compressedByteArray, Ext.bind(function (result, parentDeserialize) {
            parentDeserialize.call(this, result, function(resultObj) {
                callback(resultObj);
            });
        }, this, [parentDeserialize], true));
    }
});