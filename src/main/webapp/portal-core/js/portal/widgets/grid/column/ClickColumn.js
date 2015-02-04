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
        if (type == 'mousedown' || (type == 'keydown' && (e.getKey() === e.ENTER || e.getKey() === e.SPACE))) {
            var record = view.panel.store.getAt(recordIndex);                      
            this.fireEvent('columnclick', this, record, recordIndex, cellIndex,e);
            return this.callParent(arguments);
        } else if (type === 'dblclick') {
            var record = view.panel.store.getAt(recordIndex);
            return this.fireEvent('columndblclick', this, record, recordIndex, cellIndex);
        } else {
            return this.callParent(arguments);
        }
    }
});

