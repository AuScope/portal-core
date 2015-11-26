/**
 * A column (similar to the ExtJs Action Column) that allows
 * a grid to subscribe to specific click events for a column.
 *
 * What it also offers is the ability organise its own
 * custom renderers which makes it distinct from the ExtJS
 * Action Column.
 *
 * It is expected that classes define their own renderer when
 * using this column
 */
Ext.define('portal.widgets.grid.column.ClickColumn', {
    extend: 'Ext.grid.column.Column',
    alias: 'widget.clickcolumn',

    constructor: function() {
        this.callParent(arguments);
    },

    /**
     * @private
     * Process and refire events routed from the GridView's processEvent method.
     */
    processEvent: function(type, view, cell, recordIndex, cellIndex, e) {
        var record = e.record;
        var storeRecordIndex = view.store.indexOf(record);        
        if (type == 'click' || (type == 'keydown' && (e.getKey() === e.ENTER || e.getKey() === e.SPACE))) {
            this.fireEvent('columnclick', this, record, storeRecordIndex, cellIndex,e);
            return this.callParent(arguments);
        } else if (type === 'dblclick') {
            return this.fireEvent('columndblclick', this, record, storeRecordIndex, cellIndex);
        } else {
            return this.callParent(arguments);
        }
    }
});

