CSWFilterSelectionWindow = Ext.extend(Ext.Window, {

    constructor : function(cfg) {
        var me = this;
        Ext.apply(cfg, {
            title: cfg.title,
            height: 500,
            width: 650,
            layout: 'fit',
            items: [{
                id : 'pagingPanel',
                xtype: 'cswfilterformpanel',
                layout : 'fit'
            }],

            buttonAlign : 'right',
            buttons : [{
                xtype : 'button',
                text : 'Search',
                iconCls : 'add',
                handler : function(button, e) {
                    var cswSelectionWindow = new CSWSelectionWindow({
                        title : 'CSW Record Selection',
                        store : cfg.store
                    });
                    cswSelectionWindow.show();
                    me.close();
                }
            }]
        });

        //Call parent constructor
        CSWFilterSelectionWindow.superclass.constructor.call(this, cfg);

    }

});
