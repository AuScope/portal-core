Ext.define('portal.util.permalink.serializers.SerializerV1', {
    extend : 'portal.util.permalink.serializers.BaseSerializer',

    getVersion : function() {
        return 1;
    },

    //This class is only for legacy deserialization therefore there is no serialize implementation

    /**
     * See parent interface for details.
     */
    deserialize : function(stateStr, callback) {
        var state = Ext.JSON.decode(stateStr);
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

            var filter = minifiedLayers[i].f;
            if (!filter) {
                filter = {};
            }

            if (Ext.isDefined(minifiedLayers[i].o)) {
                filter.opacity = minifiedLayers[i].o;
            }

            this.serializedLayers.push({
                source : minifiedLayers[i].s,
                filter : filter,
                visible : minifiedLayers[i].v,
                id : minifiedLayers[i].i,
                onlineResources : onlineResources
            });
        }

        callback({
            mapState : mapState,
            serializedLayers : serializedLayers
        });
    }
});