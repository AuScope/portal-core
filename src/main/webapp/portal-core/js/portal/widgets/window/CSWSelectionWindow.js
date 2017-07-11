/**
 * This is the window used to display the results from the csw filtering use by master catalogue.
 */

CSWSelectionWindow = Ext.extend(Ext.Window, {
    //this is the store of the personal panel.
    store : null,
    pageSize: 20,

	buttons : [{
	    xtype : 'button',
	    text : 'Add Selected Records',
	    iconCls : 'add',
	    scope : this,
	    handler : function(button, e) {
            var myParent = button.findParentByType('window');
	        var cswPagingPanel = myParent.getComponent('pagingRecordtabPanel').getActiveTab();
	        var csw = cswPagingPanel.getSelectionModel().getSelection();
	        myParent.fireEvent('selectioncomplete',csw);
	     }
	},{
	    xtype : 'button',
	    text : 'Add All Current Page Records',
	    iconCls : 'addall',
	    scope : this,
	    handler : function(button, e) {
            var myParent = button.findParentByType('window');
	        var cswPagingPanel = myParent.getComponent('pagingRecordtabPanel').getActiveTab();
	        var allStore = cswPagingPanel.getStore();
	        var csw = allStore.getRange();
	        myParent.fireEvent('selectioncomplete',csw);
	     }
	}],

    constructor : function(cfg) {      

        var me = this;
        
        //this is the store after filtering the registery.
        var recordStore = cfg.filterStore;
                        
        Ext.apply(cfg, {
            title: cfg.title,
            height: 500,
            width: 650,
            layout: 'fit',
            items: [{
                xtype:'tabpanel',
                itemId: 'pagingRecordtabPanel',
                layout: 'fit',
                items : cfg.resultpanels //VT: CSWRecordPagingPanel
            }],

            buttonAlign : 'right',
            modal : true

        });
        
        //Call parent constructor
        CSWSelectionWindow.superclass.constructor.call(this, cfg);

    }

});

