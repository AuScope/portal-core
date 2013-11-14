CSWSelectionWindow = Ext.extend(Ext.Window, {
    //this is the store of the personal panal.
    store : null,
    pageSize: 20,

    constructor : function(cfg) {

        this.store=cfg.panelStore;
        var me = this;
        //this is the store after filtering the registery.
        var recordStore = cfg.filterStore

        Ext.apply(cfg, {
            title: cfg.title,
            height: 500,
            width: 650,
            layout: 'fit',
            items: [{
                id : 'pagingRecordPanel',
                xtype: 'cswrecordpagingpanel',
                layout : 'fit',
                store : recordStore
            }],

            buttonAlign : 'right',
            buttons : [{
                xtype : 'button',
                text : 'Add Selected Records',
                iconCls : 'add',
                handler : function(button, e) {
                    var cswPagingPanel = button.findParentByType('window').getComponent('pagingRecordPanel');
                    var csw = cswPagingPanel.getSelectionModel().getSelection();
                    me.store.add(csw);
                    //me.store.load();
                 }
            },{
                xtype : 'button',
                text : 'Add All Current Page Records',
                iconCls : 'addall',
                handler : function(button, e) {
                    var cswPagingPanel = button.findParentByType('window').getComponent('pagingRecordPanel');
                    var allStore = cswPagingPanel.getStore();
                    var cswRecords = allStore.getRange();
                    me.store.add(csw);
                    me.store.load();
                 }

            },{
                xtype : 'button',
                text : 'Add All Records',
                iconCls : 'addall',
                handler : function(button, e) {
                    var cswPagingPanel = button.findParentByType('window').getComponent('pagingRecordPanel');
                    var allStore = cswPagingPanel.getStore();
                    var cswRecords = allStore.getRange();
                    me.store.add(csw);
                    me.store.load();
                }

            }]
        });

        //Call parent constructor
        CSWSelectionWindow.superclass.constructor.call(this, cfg);

    }

});

