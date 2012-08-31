/**
 * The ErrorWindow is a class that specialises Ext.Window into displaying
 * an error message along with its details if presented in errorObj.
 */
Ext.define('portal.widgets.window.ErrorWindow', {
    extend : 'Ext.window.Window',

    constructor : function(cfg) {
        var errorObj = cfg.errorObj;
        // get a reference to our class
        var win = this;

        Ext.apply(cfg, {
            title: errorObj.title,
            layout: 'anchor',
            defaults: { anchor: '100%' },
            modal: true,
            resizable: false,
            width: 400,
            buttonAlign: 'center',
            items: [{
                xtype: 'container',
                autoScroll: true,
                border: false,
                height: 50,
                html: Ext.htmlEncode(errorObj.message)
            }], // eo items
            buttons: [{
                text: 'OK',
                handler: function() {
                    win.destroy();
                }
            }], // eo buttons
            listeners: {
                afterrender: { // make sure layout sized properly
                    single: true,
                    buffer: 10,
                    fn: function() {
                        return;
                        win.doLayout();
                    }
                } // eo afterrender
            } // eo listeners
        })

        if ( errorObj.info ) {
            cfg.items.push({
                xtype: 'panel',
                title: 'Details',
                collapsible: true,
                collapsed: true,
                titleCollapse: true,
                layout: 'fit',
                listeners: {
                    collapse: function() { win.doLayout(); },
                    expand: function() { win.doLayout(); }
                },
                items: {
                    xtype: 'textarea',
                    readOnly: true,
                    height: 50,
                    value: errorObj.info
                }
            });
        }

        this.callParent(arguments);
    }
});