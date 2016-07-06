
/**
 * Collapsed accordian can be applied to any container using the accordian layout.
 * 
 * This will allow all accordian panels to be closed (default behaviour is that one is always open)
 */
Ext.define('portal.widgets.plugins.CollapsedAccordian', {
    extend : 'Ext.plugin.Abstract',
    alias : 'plugin.collapsedaccordian',

    statics : {
        HIDDEN_ID : 'collapsedtarget'
    },

    init : function(cmp) {
        cmp.insert(0, {
            xtype : 'panel',
            hidden : true,
            itemId : portal.widgets.plugins.CollapsedAccordian.HIDDEN_ID,
            collapsed : false
        });

        var cfg = Ext.apply({}, cmp.initialConfig);
        var layoutCfg = cfg.layout;
        if (Ext.isString(layoutCfg)) {
            layoutCfg = {};
        }

        layoutCfg.type = 'accordiondefault';
        layoutCfg.defaultId = portal.widgets.plugins.CollapsedAccordian.HIDDEN_ID;
        cmp.setLayout(layoutCfg)
    }
});
