/**
 * An extension to Ext.ux.CheckColumn which allows
 * the ability for a custom value renderer.
 *
 * This is essentially the same as rendering this column
 * based on a 'view' of the underlying boolean data.
 *
 * Instances of this class must specify the functions:
 *  getCustomValueBool
 *  setCustomValueBool
 */
Ext.define('portal.widgets.grid.column.RenderableCheckColumn', {
    extend: 'Ext.ux.CheckColumn',
    alias: 'widget.renderablecheckcolumn',
    
    
    /**
     * A function that will be called when the column needs
     * to get the custom value for this column for the particular value
     *
     * function(portal.widgets.grid.column.RenderableCheckColumn, value, Ext.data.Model)
     */
    getCustomValueBool : portal.util.UnimplementedFunction,

    
    /**
     * A function that will be called to update the custom value of field
     *
     * function(portal.widgets.grid.column.RenderableCheckColumn, checked, Ext.data.Model)
     */
    setCustomValueBool : portal.util.UnimplementedFunction,

    constructor: function(config) {
        if (config.getCustomValueBool) {
            this.getCustomValueBool = config.getCustomValueBool;
        }
        if (config.setCustomValueBool) {
            this.setCustomValueBool = config.setCustomValueBool;
        }
        this.callParent(arguments);
    },

    /**
     * @private
     * Process and refire events routed from the GridView's processEvent method.
     * VT:I don't think this is needed anymore
     */
    processEvent: function(type, view, cell, recordIndex, cellIndex, e) {
        if (type === 'mousedown' || (type === 'keydown' && (e.getKey() === e.ENTER || e.getKey() === e.SPACE))) {
            var record = view.panel.store.getAt(recordIndex),
                dataIndex = this.dataIndex,
                value = record.get(dataIndex);
                checked = !this.getCustomValueBool(this, value,record);

            this.setCustomValueBool(this, checked, record);
            record.afterEdit(dataIndex);//trick record into triggering an update
            this.fireEvent('checkchange', this, recordIndex, checked);
            // cancel selection.
            return false;
        } else {
            return this.callParent(arguments);
        }
    },

    renderer : function(value, metadata, record, rowIndex, colIndex, store, view) {
        var header = view.getHeaderAtIndex(colIndex);
        return this.callParent([header.getCustomValueBool(header, value,record)]);

        
    }
});

