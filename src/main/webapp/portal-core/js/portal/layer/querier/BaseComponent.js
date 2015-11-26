/**
 * An abstract Ext.Panel extension forming the base
 * for all Querier components to derive from
 */
Ext.define('portal.layer.querier.BaseComponent', {
    extend: 'Ext.panel.Panel',
    alias : 'widget.querierbasecomponent',

    /**
     * The title to be used in the event of this base component being rendered
     * inside some form of tab control (or any other parent container requiring a title).
     */
    tabTitle : 'Summary',

    constructor : function(cfg) {
        if (cfg.tabTitle) {
            this.tabTitle = cfg.tabTitle;
        }
        this.callParent(arguments);
    }
});