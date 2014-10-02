/**
 * A plugin for an Ext.grid.Panel class that a context menu that
 * shows whenever a row is selected. The menu will render horizontally
 * below the selected row, seemingly "inline"
 *
 * To use this plugin, assign the following field to the plugin constructor
 * {
 *  actions : Ext.Action[] - Actions to be shown/hidden according to row selection.
 * }
 *
 * Contains two events:
 *  contexthide, contextshow
 *
 */
Ext.define('portal.widgets.grid.plugin.InlineContextMenu', {
    extend: 'portal.widgets.grid.plugin.RowExpanderContainer',

    alias: 'plugin.inlinecontextmenu',

    actions : null,

    /**
     * Supported config options
     * {
     *  actions : Ext.Action[] - *required* Actions to be shown/hidden according to row selection.
     * }
     */
    constructor : function(cfg) {

        cfg.generateContainer = this.generateToolbar;
        cfg.allowMultipleOpen = false;
        this.callParent(arguments);
    },

    generateToolbar : function(record, renderTo) {
        var items = [];
        Ext.each(this.actions, function(action) {
            items.push(Ext.create('Ext.button.Button', action));
        });

        return Ext.create('Ext.container.Container', {
            renderTo : renderTo,
            items : items,
            defaults : {
                margins : '0 0 0 10'
            },
            padding : '5 10 5 0',
            layout : {
                type : 'hbox',
                pack: 'end'
            }
         });
    }
});