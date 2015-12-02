Ext.define('portal.util.permalink.serializers.SerializerV0', {
    extend : 'portal.util.permalink.serializers.BaseSerializer',

    getVersion : function() {
        return 0;
    },

    //This class is only for legacy deserialization therefore there is no serialize implementation

    /**
     * See parent interface for details.
     */
    deserialize : function(stateStr, callback) {
        var state = Ext.JSON.decode(stateStr);
        var mapState = serializationObj.mapState;
        var layers = serializationObj.activeLayers;

        //The above is ALMOST all the work - the schema has changed slightly
        for (var i = 0; i < layers.length; i++) {
            if (Ext.isDefined(layers[i].opacity)) {
                if (!layers[i].filter) {
                    layers[i].filter = {};
                }

                layers[i].filter.opacity = layers[i].opacity;
                layers[i].opacity = undefined;
            }

            if (Ext.isArray(layers[i].onlineResources)) {
                for (var j = 0; j < layers[i].onlineResources.length; j++) {
                    layers[i].onlineResources[j].type = layers[i].onlineResources[j].onlineResourceType;
                    layers[i].onlineResources[j].onlineResourceType = undefined;
                }
            }
        }

        callback({
            mapState : mapState,
            serializedLayers : layers
        });
    }
});