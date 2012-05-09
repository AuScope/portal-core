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
     * This function takes the state of the map/layers and generates a Object that can be used to
     * regenerate the specified state at a later date
     *
     * function(Object mapState, Object serializedLayers)
     *
     * mapState - Object conforming to the mapState schema defined in portal.util.permalink.MapStateSerializer
     * serializedLayers - Array of Objects conforming to the serializedLayers schema defined in portal.util.permalink.MapStateSerializer
     *
     * returns - Object encoded state
     */
    serialize : portal.util.UnimplementedFunction,

    /**
     * This function takes a serialized string representing the state of the map/layers and generates an
     * object representation of that state
     *
     * function(Object state)
     *
     * state - A Object response from the serializeFunction
     *
     * returns Object in the form
     * {
     *  mapState - Object conforming to the mapState schema defined in portal.util.permalink.MapStateSerializer
     *  serializedLayers - Array of Objects conforming to the serializedLayers schema defined in portal.util.permalink.MapStateSerializer
     * }
     */
    deserialize : portal.util.UnimplementedFunction
});