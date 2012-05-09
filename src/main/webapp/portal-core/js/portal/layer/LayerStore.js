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
    }
});