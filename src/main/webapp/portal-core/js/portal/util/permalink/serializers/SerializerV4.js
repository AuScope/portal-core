Ext.define('portal.util.permalink.serializers.SerializerV4', {
    extend : 'portal.util.permalink.serializers.SerializerV3',

    getVersion : function() {
        return 4;
    },

        
    createDeSerializedObject : function(value, onlineResources){
        var result = {
            source : value.s,
            filter : value.f,
            visible : value.v,
            id : value.i,  
            customlayer : value.c,
            onlineResources : onlineResources
        };
        
        return result;
    },
    
    createSerializedObject : function(value, onlineResources){
        var result = {
            s : value.source,
            f : value.filter,
            v : value.visible,
            i : value.id,   
            c : value.customlayer,
            r : onlineResources
        };
        
        return result;
    }
});