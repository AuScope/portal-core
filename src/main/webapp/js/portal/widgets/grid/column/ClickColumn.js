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
        this.addEvents(
            /**
             * @event columnclick
             * Fires when a row is clicked and that click lies within this column
             * @param {portal.widgets.grid.column.ClickColumn} this
             * @param {Ext.data.Model} The selected data record
             * @param {Number} rowIndex The row index clicked
             * @param {Number} colIndex The column index clicked
             */
            'columnclick',
            /**
             * @event columndblclick
             * Fires when a row is double clicked and that click lies within this column
             * @param {portal.widgets.grid.column.ClickColumn} this
             * @param {Ext.data.Model} The selected data record
             * @param {Number} rowIndex The row index clicked
             * @param {Number} colIndex The column index clicked
             */
            'columndblclick'
        );
        this.callParent(arguments);
    },

    /**
     * @private
     * Process and refire events routed from the GridView's processEvent method.
     */
    processEvent: function(type, view, cell, recordIndex, cellIndex, e) {
        if (type == 'mousedown' || (type == 'keydown' && (e.getKey() == e.ENTER || e.getKey() == e.SPACE))) {
            var record = view.panel.store.getAt(recordIndex);

            this.fireEvent('columnclick', this, record, recordIndex, cellIndex);
            // cancel selection.
            return false;
        } else if (type == 'dblclick') {
            var record = view.panel.store.getAt(recordIndex);
            this.fireEvent('columndblclick', this, record, recordIndex, cellIndex);
            return false;
        } else {
            return this.callParent(arguments);
        }
    }
});

