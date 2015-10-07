/**
 * An Ext.data.Store specialisation for storing
 * portal.layer.Layer objects.
 */
Ext.define('portal.layer.LayerStore', {
    extend: 'Ext.data.Store',

    /**
     * Creates an empty store - no configuration options
     */
    constructor : function() {
        this.callParent([{
            model : 'portal.layer.Layer',
            data : []
        }]);
    }, 
    listeners : {
        add : function(store, records, index, eOpts) {
            console.log("LayerStore - Records added to layerstore: ", records);
            // Let the listeners know about the new active layer
            AppEvents.broadcast('addactivelayer', {layer:records});
        },
        remove : function( store, records, index, isMove, eOpts ) {
            console.log("LayerStore - Records removed from layerstore: ", records);
            // Let the listeners know about the removed active layer
            AppEvents.broadcast('removeactivelayer', {layer:records});
        }
    
    }
});
