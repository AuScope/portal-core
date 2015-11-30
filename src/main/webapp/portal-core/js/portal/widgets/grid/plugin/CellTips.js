/**
 * A plugin for an Ext.grid.Panel class that adds dynamic
 * tooltips to individual cells.
 *
 * To use this plugin, assign the following fields to each of the grid's columns
 * {
 *  hasTip : Boolean - whether this column has a tip associated with it (default value false)
 *  tipRenderer : function(Object value, Ext.data.Model record, Ext.grid.Column column, Ext.tip.ToolTip tip) - should return a value to be rendered into a tool tip
 * }
 *
 * Original idea adapted from http://stackoverflow.com/questions/7539006/extjs4-set-tooltip-on-each-column-hover-in-gridpanel
 */
Ext.define('portal.widgets.grid.plugin.CellTips', {
    alias: 'plugin.celltips',

    /**
     * The Ext.grid.Panel this plugin will be applied to.
     */
    _grid : null,

    /**
     * The simple selector string used for discovering the row
     * that triggers a tooltip opening.
     */
    _rowSelector : 'tr.' + Ext.baseCSSPrefix + 'grid-row',

    constructor : function(cfg) {
        this.callParent(arguments);
    },

    init: function(grid) {
        this._grid = grid;
        grid.getView().on('render', this._registerTips, this);
    },

    /**
     * Registers a tooltip to show based on a grid view. The shown tooltip will
     * be generated using _tipRenderer
     */
    _registerTips : function(view) {
        view.tip = Ext.create('Ext.tip.ToolTip', {
            // The overall target element.
            target: view.el,
            // Each grid row causes its own seperate show and hide.
            delegate: view.cellSelector,
            // Moving within the row should not hide the tip.
            trackMouse: true,
            // Render immediately so that tip.body can be referenced prior to the first show.
            renderTo: Ext.getBody(),
            // Allow tooltips to grow to their maximum
            maxWidth: 500,
            listeners: {
                // Change content dynamically depending on which element triggered the show.
                beforeshow: Ext.bind(this._tipRenderer, this, [view], true)
            }
        });
    },

    /**
     * Function for building the contents of a tooltip
     */
    _tipRenderer : function(tip, opt, view) {
        //Firstly we lookup the parent column
        var gridColums = view.getGridColumns();
        var colIndex = tip.triggerElement.cellIndex;
        var column = gridColums[tip.triggerElement.cellIndex];

        if (!column || !column.hasTip) {
            return false;
        }

        //Next we iterate through our parent nodes until we hit the containing tr
        var triggerElement = Ext.fly(tip.triggerElement);
        var parent = triggerElement.findParentNode(this._rowSelector, 20, true);
        if (!parent) {
            return false;
        }

        //We use the parent node to lookup the record and we
        //finally we pass along the 'useful' information to the tipRenderer
        var record = view.getRecord(parent);
        var value = record.get(column.dataIndex);
        if (column.tipRenderer) {
            tip.update(column.tipRenderer(value, record, column, tip));
        } else {
            tip.update(value);
        }

        return true;
     }
});