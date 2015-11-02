/**
 * This is the window used to display the results from the csw filtering use by master catalogue.
 */

CSWSelectionWindow = Ext.extend(Ext.Window, {
    //this is the store of the personal panal.
    store : null,
    pageSize: 20,

	buttons : [{
	    xtype : 'button',
	    text : 'Back to Search',
	    iconCls : 'magglass',
	    scope : this,
	    handler : function(button, e) {
	        var cswFilterWindow = Ext.getCmp('cswFilterWindow');
	    	var searchresultsWindow = Ext.getCmp('cswSelectionWindow');
	    	if (searchresultsWindow) {
	    		searchresultsWindow.destroy();
	    	}
	        cswFilterWindow.show();
	     }
	}, {
	    xtype : 'button',
	    text : 'Add Selected Records',
	    iconCls : 'add',
	    scope : this,
	    handler : function(button, e) {
	        var cswPagingPanel = button.findParentByType('window').getComponent('pagingRecordtabPanel').getActiveTab();
	        var csw = cswPagingPanel.getSelectionModel().getSelection();
	        this.fireEvent('selectioncomplete',csw);
	
	
	     }
	},{
	    xtype : 'button',
	    text : 'Add All Current Page Records',
	    iconCls : 'addall',
	    scope : this,
	    handler : function(button, e) {
	        var cswPagingPanel = button.findParentByType('window').getComponent('pagingRecordtabPanel').getActiveTab();
	        var allStore = cswPagingPanel.getStore();
	        var csw = allStore.getRange();
	        this.fireEvent('selectioncomplete',csw);
	
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

        });

        if (cfg.showControlButtons === false) {
        	this.buttons = null;
        }
        
        //Call parent constructor
        CSWSelectionWindow.superclass.constructor.call(this, cfg);

    },
    
    // overridden close method to obtain a reference to the search window and close it as well
    close: function() {      
    	var advancedSearchWindow = Ext.getCmp('cswFilterWindow');
    	if (advancedSearchWindow) {
    		advancedSearchWindow.close();
    	}
    	var searchresultsWindow = Ext.getCmp('cswSelectionWindow');
    	if (searchresultsWindow) {
    		searchresultsWindow.destroy();
    	}
    }

});

