/**
 * Abstract base class for serializers to extend and implement
 */
Ext.define('portal.util.permalink.serializers.BaseSerializer', {
    constructor : function() {
        this.callParent(arguments);
    },

    /**
     * This function should return the unique version string describing this serializer
     *
     * function()
     *
     * returns - String which should be unique across all serializer implementations
     */
    getVersion : function() {
        return this.$className;
    },

    /**
     * This function takes the state of the map/layers and generates a String that can be used to
     * regenerate the specified state at a later date
     *
     * function(Object mapState, Object serializedLayers, function(string) callback)
     *
     * mapState - Object conforming to the mapState schema defined in portal.util.permalink.MapStateSerializer
     * serializedLayers - Array of Objects conforming to the serializedLayers schema defined in portal.util.permalink.MapStateSerializer
     * callback - The string encoded state will passed to this function after it has been calculated asynchronously
     *
     * returns - Nothing - Use the callback parameter
     */
    serialize : portal.util.UnimplementedFunction,

    /**
     * This function takes a serialized string representing the state of the map/layers and generates an
     * object representation of that state
     *
     * function(String state, function(Object) callback)
     *
     * state - A Object response from the serializeFunction
     * callback - Will be passed an Object in the form below when deserialisation finishes
     *              {
     *                  mapState - Object conforming to the mapState schema defined in portal.util.permalink.MapStateSerializer
     *                  serializedLayers - Array of Objects conforming to the serializedLayers schema defined in portal.util.permalink.MapStateSerializer
     *              }
     *
     * returns Nothing - Use the callback parameter 
     */
    deserialize : portal.util.UnimplementedFunction,
    
    
    /**
     * This function allows us to override and control what parameters we serialize in each version of the Serializer.
     */
    createSerializedObject : portal.util.UnimplementedFunction,
    
    
    /**
     * This function allows us to override and control what parameters we deserialize in each version of the Serializer.
     */
    createDeSerializedObject : portal.util.UnimplementedFunction
});