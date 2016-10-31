/**
 * An Ext.data.Store specialisation for storing
 * portal.layer.Layer objects.
 */
Ext.define('portal.layer.LayerStore', {
    extend: 'Ext.data.Store',
    
    addingLayer: false, // Set to true when adding a layer
    
    /**
     * Creates an empty store - no configuration options
     */
    constructor : function(config) {        
        this.callParent([{
            model : 'portal.layer.Layer',
            data : []
        }]);
    }
});
