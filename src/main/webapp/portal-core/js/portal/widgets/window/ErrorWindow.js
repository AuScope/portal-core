/**
 * The ErrorWindow is a class that specialises Ext.Window into displaying
 * an error message along with its details if presented in errorObj.
 */
Ext.define('portal.widgets.window.ErrorWindow', {
    extend : 'Ext.window.Window',

    statics : {
        /**
         * Static utility for showing a window with the specified values.
         *
         * Creates a new instance on every call
         */
        showText : function(title, message, info) {
            Ext.create('portal.widgets.window.ErrorWindow', {
                title : title,
                message : message,
                info : info
            }).show();
        },

        /**
         * Static utility for showing a window with the specified values.
         *
         * Creates a new instance on every call
         */
        show : function(errorObj) {
            Ext.create('portal.widgets.window.ErrorWindow', {
                errorObj : errorObj
            }).show();
        }
    },

    /**
     * Accepts Ext.window.Window parameters along with the following additions:
     * {
     *  errorObj : Object - a response object from a portal controller function
     *  title : String - The title of the error (used if errorObj is undefined)
     *  message : String - The message of the error (used if errorObj is undefined)
     *  info : String - additional error information (used if errorObj is undefined)
     * }
     */
    constructor : function(cfg) {
        var errorObj = cfg.errorObj;
        var title = errorObj ? errorObj.title : cfg.title;
        var message = errorObj ? errorObj.message : cfg.message;
        var info = errorObj ? errorObj.info : cfg.info;

        // get a reference to our class
        var win = this;

        if (!title || !title.length) {
            title = 'Error';
        }

        Ext.apply(cfg, {
            title: title,
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
                html: Ext.htmlEncode(message)
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
        });

        if (info) {
            cfg.items.push({
                xtype: 'panel',
                title: 'Details',
                collapsible: true,
                collapsed: false,
                titleCollapse: true,
                layout: 'fit',
                listeners: {
                    collapse: function() { win.doLayout(); },
                    expand: function() { win.doLayout(); }
                },
                items: {
                    xtype: 'container',
                    height: 50,
                    html: info
                }
            });
        }

        this.callParent(arguments);
    }
});