/**
 * A plugin for an Ext.grid.Panel class that allows the grid's
 * contents to be selected by a user
 */
Ext.define('portal.widgets.grid.plugin.SelectableGrid', {
    alias: 'plugin.selectablegrid',

    /**
     * The Ext.grid.Panel this plugin will be applied to.
     */
    _grid : null,

    constructor : function(cfg) {
        this.callParent(arguments);
    },

    init: function(grid) {
        this._grid = grid;

        var view = grid.getView();
        var store = grid.getStore();
        view.on('itemadd', this._makeRecordsSelectable, this);
        view.on('viewready', function(view) {
            this._makeNodesSelectable(view.getNodes(), true);
        }, this);
        grid.on('afterlayout', function(grid) {
            if (grid.getView().viewReady) {
                this._makeRecordsSelectable(grid.getStore().getRange());
            }
        }, this);
    },

    /**
     * Makes added rows selectable
     */
    _makeRecordsSelectable : function(records) {
        var view = this._grid.getView();

        //For every record that was just added
        for (var i = 0; i < records.length; i++) {
            var node = view.getNode(records[i]);
            this._makeNodesSelectable(node, true);
        }
    },

    /**
     * Makes DOM nodes selectable (makes use of the Ext flyweight element)
     */
    _makeNodesSelectable : function(nodes, selectable) {
        if (!Ext.isArray(nodes)) {
            nodes = [nodes];
        }

        for (var i = 0; i < nodes.length; i++) {
            if (selectable) {
                Ext.fly(nodes[i]).selectable();
            } else {
                Ext.fly(nodes[i]).unselectable();
            }
        }
    }
});