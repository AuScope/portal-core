/**
 * Plugin to apply to Ext.Image components. Adds a "click" event
 * to the image by binding to the rendered DOM click event
 */
Ext.define('portal.widgets.plugins.ClickableImage', {
    extend : 'Ext.plugin.Abstract',
    alias : 'plugin.clickableimage',

    stopEvent : false,

    /**
     * stopEvent - Boolean - if true, the event will be stopped from propogating upwards. Defaults to false
     */
    constructor : function(config) {
        this.stopEvent = !!config.stopEvent;
    },

    init : function(cmp) {
        if (cmp.rendered) {
            this.installEvents(cmp);
        } else {
            cmp.on('afterrender', this.installEvents, this);
        }
    },

    installEvents : function(cmp) {
        var el = cmp.getEl();
        el.on('click', function(e, t, eOpts) {
            if (this.stopEvent) {
                e.stopEvent();
            }
            cmp.fireEvent('click', cmp, e);
        }, this);
        
        el.on('dblclick', function(e, t, eOpts) {
            if (this.stopEvent) {
                e.stopEvent();
            }
            cmp.fireEvent('dblclick', cmp, e);
        }, this);
    }
});
