/**
 * PrimitiveManager is a class for managing a set of primitives as a distinct group
 * of primitives that can cleared/modified seperately from other primitives that may
 * already be on the map
 *
 * It supports the following events
 * clear - function(portal.map.BasePrimitiveManager manager) - raised after clearPrimitives is called
 * addprimitive - function(portal.map.BasePrimitiveManager manager, portal.map.primitives.BasePrimitive[] primitives) -
 *                raised whenever addPrimitive is called (after the primitive has been added to the map)
 */
Ext.define('portal.map.BasePrimitiveManager', {
    extend: 'Ext.util.Observable',

    /**
     * portal.map.BaseMap - The map instance that created this primitive manager
     */
    baseMap : null,

    /**
     * {
     *  baseMap : portal.map.BaseMap - The map instance that created this primitive manager
     * }
     */
    constructor : function(config) {
     
        this.baseMap = config.baseMap;
        this.callParent(arguments);
    },

    /**
     * function()
     *
     * Removes all primitives (that are managed by this instance) from the map
     */
    clearPrimitives : Ext.util.UnimplementedFunction,

    /**
     * Adds an array of primitive to the map and this instance
     *
     * function(primitive)
     *
     * @param primitive portal.map.primitives.BasePrimitive[] all primitives to add to the map
     */
    addPrimitives : Ext.util.UnimplementedFunction
});