/*
 * This file is part of the AuScope GeodesyWorkflow project.
 * Copyright (c) 2009 AuScope
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
// reference local blank image
Ext.BLANK_IMAGE_URL = 'js/external/ext-2.2/resources/images/default/s.gif';

Ext.namespace('DataService');

var xmlText = '<?xml version="1.0" encoding="ISO-8859-1"?>';
var xmlRootOpenTag = "<data>";
var xmlRootCloseTag = "</data>";
var fileUrl = "fileUrl";
var fileUrlOpenTag = "<fileUrl>";
var fileUrlCloseTag = "</fileUrl>";

//
//Called when the user tries to navigate away from this site
//
DataService.onWindowUnloading = function(e) {
   e.browserEvent.returnValue = "All entered details will be lost!";
}

//callback for sendToGrid action
DataService.onSendToGridResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.error != null) {
        JobList.showError(resp.error);
    } else {
        Ext.Msg.alert("Success", "The selected files are marked as input for Job submission.");
    }
}

//called when an Ajax request fails
DataService.onRequestFailure = function(response, request) {
	Ext.Msg.alert("Error", 'Could not execute last request. Status: '+
        response.status+' ('+response.statusText+')');
}

//
//Send selected files to the server for zip and download
//
DataService.zip4Download = function() {
      var myStore = Ext.getCmp('selection-grid').getStore();
	  var myStore = Ext.getCmp('selection-grid').getStore();
	  xmlText += xmlRootOpenTag;
	  myStore.each(function(url_date){
		  url_date.fields.each(function(field){
			  if((field.name == 'select_item') && (url_date.get(field.name).toString() == 'true')){
			   	xmlText += fileUrlOpenTag + url_date.get(fileUrl) + fileUrlCloseTag;
			  }
		  });
	  },this); 
	  xmlText += xmlRootCloseTag;

	  Ext.Ajax.request({
	      url: 'zip4Download.do' ,
	      success: DataService.onSendToGridResponse,
	      failure: DataService.onRequestFailure,
	      params: { 'myFiles': xmlText }
	  });
}


//
//Send selected files as an input for Grid job
//
DataService.send2Grid = function() {
	  var myStore = Ext.getCmp('selection-grid').getStore();
	  xmlText += xmlRootOpenTag;
	  myStore.each(function(url_date){
		  url_date.fields.each(function(field){
			  if((field.name == 'select_item') && (url_date.get(field.name).toString() == 'true')){
			   	xmlText += fileUrlOpenTag + url_date.get(fileUrl) + fileUrlCloseTag;
			  }
		  });
	  },this); 
	  xmlText += xmlRootCloseTag;

	  Ext.Ajax.request({
	      url: 'sendToGrid.do' ,
	      success: DataService.onSendToGridResponse,
	      failure: DataService.onRequestFailure,
	      params: { 'myFiles': xmlText }
	  });
}



DataService.getSelectedFiles = function(record){
	  record.fields.each(function(field)
		  {
		    if(record.get(select_item)){
		    	
		    }
	  });
}

//
//This is the main layout definition.
//
DataService.initialize = function(){

    // shorthand alias
    var fm = Ext.form;

    // the check column is created using a custom plugin
    var checkColumn = new Ext.grid.CheckColumn({
       header: 'Select',
       dataIndex: 'select_item',
       width: 55
    });

    // the column model has information about grid columns
    // dataIndex maps the column to the specific data field in
    // the data store (created below)
    var cm = new Ext.grid.ColumnModel({
        // specify any defaults for each column
        defaults: {
            sortable: true // columns are not sortable by default           
        },
        columns: [
            {
                 header: 'File Date',
                 dataIndex: 'fileDate',
                 width: 95,
                 editor: new fm.TextField({
                	 allowNegative: false,
                	 allowBlank: false,
                     disabled : true
                })
            }, {
                 id: 'fileUrl',
            	 header: 'File URL',
                 dataIndex: 'fileUrl',
                 width: 250,
                 editor: new fm.TextField({
                	 allowNegative: false,
                	 allowBlank: false,
                     disabled : true
                 })
            },
            checkColumn // the plugin instance
        ]
    });
    


    var store = new Ext.data.Store({
    	url: '/getSelection.do',
    	reader: new Ext.data.XmlReader({
    		record: 'url_date',
    		fields: [
    	          {name: 'fileDate', type: 'string'},                
    	          {name: 'fileUrl', type: 'string'},            
    	          {name: 'select_item', type: 'bool'}
    	    ]
    	})
    });
   
    // create the editor grid
    var grid = new Ext.grid.EditorGridPanel({
    	id: 'selection-grid',
    	store: store,
        cm: cm,
        renderTo: 'body',
        split: true,
        //region: 'south',
        width: 800,
        height: 300,
        autoExpandColumn: 'fileUrl', // column with this id will be expanded
        title: 'Data Service Tool',
        frame: true,
        // specify the check column plugin on the grid so the plugin is initialized
        plugins: checkColumn,
        clicksToEdit: 1,
        buttons: [{
            text: 'Zip for Download',
            tooltip: 'Zip selected files for download',
            handler: DataService.zip4Download
        },
        {
            text: 'Send to Grid',
            tooltip: 'Send selected files as an input of a Grid job',
            handler: DataService.send2Grid
        }]
    });

    //Function that allows user to select all or clear all
    Select_or_Clear = function(checkbox, checked) {
  	  var myStore = Ext.getCmp('selection-grid').getStore();
	  var myGrid = Ext.getCmp('selection-grid');
  	  if (checked) {
	      myStore.each(function(url_date){
	    	  url_date.set("select_item",true);
	      },myStore);
	  }else{	  	  
	  	  myStore.each(function(url_date){
			  url_date.set("select_item",false);
		  },myStore);
	  }
    }
    var clearAll =  new Ext.form.Checkbox({
    	region: 'west',
    	boxLabel: 'SelectAll / ClearAll',
    	handler : Select_or_Clear
    })

    var statusLabel =  new Ext.form.Label({
    	region: 'center',
    	text: ''
    })
       
    new Ext.Viewport({
	   layout: 'border',
       items:  [{
            xtype: 'box',
            region: 'north',
            applyTo: 'body',
            height: 100
        },{
            id: 'dst-panel',
            region: 'center',
            margins: '2 2 2 0',
            split: true,
            layout: 'anchor',
            bodyStyle: 'padding:20px 200px;',
            items: [grid, clearAll]
        },
        {
            id: 'status-panel',
            region: 'south',
            margins: '2 2 2 0',
            height: 100,
            layout: 'border',
            items: [statusLabel]
        }]
    });
    
    // manually trigger the data store load
    store.load({
        // store loading is asynchronous, use a load listener or callback to handle results
        callback: function(){
            Ext.Msg.show({
                title: 'Data Service Tool Info',
                msg: 'Data is loaded and available for processing',
                modal: false,
                icon: Ext.Msg.INFO,
                buttons: Ext.Msg.OK
            });
        }
    });

}

Ext.onReady(DataService.initialize);