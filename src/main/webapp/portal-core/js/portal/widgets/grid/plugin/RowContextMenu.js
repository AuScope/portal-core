/**
 * A plugin for an Ext.grid.Panel class that a context menu that
 * shows whenever a row is right clicked.
 *
 * To use this plugin, assign the following field to the plugin constructor
 * {
 *  contextMenu : Ext.menu.Menu - will be shown/hidden according to user right clicks.
 * }
 */
Ext.define('portal.widgets.grid.plugin.RowContextMenu', {
    alias: 'plugin.rowcontextmenu',

    /**
     * The Ext.grid.Panel this plugin will be applied to.
     */
    _grid : null,

    _contextMenu : null,

    constructor : function(cfg) {
        this._contextMenu = cfg.contextMenu;
        this.callParent(arguments);
    },

    init: function(grid) {
        this._grid = grid;
        grid.getView().on('itemcontextmenu', this._onContextMenu, this);
    },

    _onContextMenu : function(view, record, el, index, e, eOpts) {
        e.stopEvent();

        var sm = this._grid.getSelectionModel();
        if (!sm.isSelected(record)) {
            this._grid.getSelectionModel().select([record]);
        }

        this._contextMenu.showAt(e.getXY());
        return false;
    }
});