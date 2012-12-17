Ext.define('portal.util.permalink.serializers.SerializerV2', {
    extend : 'portal.util.permalink.serializers.BaseSerializer',

    getVersion : function() {
        return 2;
    },

    serialize : function(mapState, serializedLayers) {
        //We strip out a lot of overhead by only limiting the size of our field definitions
        var serializationObj = {
            m : {
                a : mapState.center.lat,
                n : mapState.center.lng,
                z : mapState.zoom
            },
            a : []
        };

        //We also do the same by serializing our objects into 'shorter' variants of themselves that the deserialization step will take care of
        for (var i = 0; i < serializedLayers.length; i++) {
            var minifiedOnlineResources = undefined;
            if (Ext.isArray(serializedLayers[i].onlineResources)) {
                var serializedOrs = serializedLayers[i].onlineResources;
                minifiedOnlineResources = [];
                for (var j = 0; j < serializedOrs.length; j++) {
                    minifiedOnlineResources.push({
                        u : serializedOrs[j].url,
                        o : serializedOrs[j].type,
                        n : serializedOrs[j].name,
                        d : serializedOrs[j].description
                    });
                }
            }

            serializationObj.a.push({
                s : serializedLayers[i].source,
                f : serializedLayers[i].filter,
                v : serializedLayers[i].visible,
                i : serializedLayers[i].id,
                r : minifiedOnlineResources
            });
        }

        return serializationObj;
    },

    /**
     * See parent interface for details.
     */
    deserialize : function(state) {
        var minifiedMapState = state.m;
        var minifiedLayers = state.a;

        //We need to take our minified map state and explode it into a meaningful object
        var mapState = {
            center : {
                lng : minifiedMapState.n,
                lat : minifiedMapState.a
            },
            zoom : minifiedMapState.z
        };

        //We need to take our minified active layer list and explode it into a meaningful object
        var serializedLayers = [];
        for (var i = 0; i < minifiedLayers.length; i++) {
            var onlineResources = undefined;
            if (Ext.isArray(minifiedLayers[i].r)) {
                onlineResources = [];
                for (var j = 0; j < minifiedLayers[i].r.length; j++) {
                    onlineResources.push({
                        url : minifiedLayers[i].r[j].u,
                        type : minifiedLayers[i].r[j].o,
                        name : minifiedLayers[i].r[j].n,
                        description : minifiedLayers[i].r[j].d
                    });
                }
            }

            serializedLayers.push({
                source : minifiedLayers[i].s,
                filter : minifiedLayers[i].f,
                visible : minifiedLayers[i].v,
                id : minifiedLayers[i].i,
                onlineResources : onlineResources
            });
        }

        return {
            mapState : mapState,
            serializedLayers : serializedLayers
        };
    }
});