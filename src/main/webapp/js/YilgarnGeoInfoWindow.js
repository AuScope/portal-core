function YilgarnGeoInfoWindow(iMap,iOverlay,iWfsUrl,iFeatureId,iWfsTypeName) {
    this.map = iMap;
    this.overlay = iOverlay;
    this.wfsUrl = iWfsUrl;
    this.featureId = iFeatureId;
    this.wfsTypeName = iWfsTypeName;
    this.overlayDes = iOverlay.description;
    this.geoFeaturePart = 'geologicUnit_';
    this.locFeaturePart = 'locatedSpecimen_';
    this.locSpecTypeName = 'sa:LocatedSpecimen';
    this.locSpecimenSubstring = this.featureId.substring(this.geoFeaturePart.length);
    this.locSpecimenFeatureId = this.locFeaturePart + this.locSpecimenSubstring;
    this.tabsArray = [];

   }

function showLocSpecDetails(wfsUrl ,typename, locSpecimenFeatureId){
	var myMask = new Ext.LoadMask(Ext.get('center_region'), {msg:"Please wait..." ,	removeMask: true});
    myMask.show();


	Ext.Ajax.request( {
	    url : 'doLocatedSpecimenFeature.do',
	    params : {
	        serviceUrl : wfsUrl,
	        typeName : typename,
	        featureId : locSpecimenFeatureId
	    },
	    callingInstance : this,

	    failure: function (response, options){
	    	myMask.hide();
	    	Ext.Msg.alert('Error Describing LocSpecimen Records', 'Error (' + response.status + '): ' + response.statusText);
	    },
	    success: function (response, options){
	    	myMask.hide();
	    	var jsonData = Ext.util.JSON.decode(response.responseText);
	    	if (!jsonData.success) {
    			Ext.Msg.alert('Error Describing LocSpecimen Records', 'There was an error whilst communicating with ' + wfsUrl);
    			return;
    		} else if (jsonData.records.length === 0) {
    			Ext.Msg.alert('Error Describing LocSpecimen Records', 'The URL ' + wfsUrl + ' returned no parsable LocatedSpecimen records');
    			return;
    		}
	    	var resultMessage = jsonData.result;
	    	var locSpecName = jsonData.uniqueSpecName;
	    	var materialDesc = jsonData.materialDesc;
	    	var records = jsonData.records;
	    	var recordItems = [];
	    	for (var i = 0; i < records.length ; i++) {
	    		recordItems.push([
	    		        records[i].analyteName,
	    			    records[i].analyteValue,
	    			    records[i].uom,
	    			    records[i].analyticalMethod,
	    			    records[i].labDetails,
	    			    records[i].date,
	    			    records[i].preparationDetails,
	    			    i
	    			]);
	    		}

	    	var groupStore = new Ext.data.GroupingStore({
	    		autoDestroy		: true,
	    		groupField		: 'analyteName',
	    		sortInfo		: {
	    			field			: 'analyteName',
	    			direction		: 'ASC'
	    		},
	    		reader : new Ext.data.ArrayReader({
	    			fields : [
	    			    'analyteName',
	    			    'analyteValue',
	    			    'uom',
	    			    'analyticalMethod',
	    			    'labDetails',
	    			    'analysisDate',
	    			    'preparationDetails',
	    			    'recordIndex'
	    			]
	    		}),
	    		data : recordItems
	    	});

	    	var grid = new Ext.grid.GridPanel({
	    		    store : groupStore,
		    	colModel:new Ext.grid.ColumnModel({
		    		 defaults: {
		             	sortable: true // columns are not sortable by default
		         },
		         columns: [{
		        	 id: 'analyteName',
		             header: 'Analyte',
		             dataIndex: 'analyteName',
		             width: 100
		         },{
		             header: 'Value',
		             dataIndex: 'analyteValue',
		             width: 100
		         },{
		             header: 'Unit Of Measure',
		             dataIndex: 'uom',
		             width: 100
		         },{		             
		             header: 'Analytical Method',
		             dataIndex: 'analyticalMethod',
		             width: 100
		         },{
		             header: 'Lab Details',
		             dataIndex: 'labDetails',
		             width: 100
		         },{
		             header: 'Analysis Date',
		             dataIndex: 'analysisDate',
		             width: 100
		         },{
		             header: 'Preparation Details',
		             dataIndex: 'preparationDetails',
		             width: 200
		         }]

		    	 }),
		    	 view: new Ext.grid.GroupingView({
		    	        forceFit: true,
		    	        // custom grouping text template to display the number of items per group
		    	        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
		    	    }),
		    	    tbar: [
			               'Select Analyte: ',
			               new Ext.ux.form.ClearableComboBox({
			                   store: locSpecName,
			                   width:200,
			                   typeAhead: true,
			                   forceSelection: true,
			                   listeners:{
			            	   		select : function(combo, record, index){

			            	   			//This will occur if our field is cleared
			            	   			if (record == null) {
			            	   				groupStore.filter('analyteName', '', false, false);
			            	   			} else {
			            	   				var selectedMineral = record.get('field1'); //TODO change to a proper field name
			            	   				groupStore.filter('analyteName', selectedMineral, false, false);
			            	   			}
			               			}
			               		}
			               }),
			               {xtype: 'tbfill'},
			               'Material Description: ',
			               materialDesc
			           ],
		    	    frame:true,
		    	    columnLines: true,
		    	    iconCls:'icon-grid',
		    	    width: 1000,
		    	    height: 450

	    	});


	    	 var win = new Ext.Window({

			        	title: 'Specimen Chemical Analyses',
		               	layout:'fit',
		                width:1000,
		                height:500,

	                    items: [grid]

	    	 });
	    	 win.show(this);
	    }
	});

}

function locSpecDownload(wfsUrl,locSpecimenFeatureId,locSpecTypeName, geoFeatureId, geoTypeName){

	var key = 'serviceUrls';
	var locSpecLink=window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/" + "doLocatedSpecimenFeature.do" + "?" +
	"serviceUrl=" + wfsUrl + "&typeName=" + "sa:LocatedSpecimen" +
	"&featureId=" + locSpecimenFeatureId;

	var geoLink = window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/" + "doYilgarnGeochemistryDownload.do" + "?" +
		"serviceUrl=" + wfsUrl + "&typeName=" + geoTypeName +
		"&featureId=" + geoFeatureId;

	var url = 'downloadLocSpecAsZip.do?';
    url += '&' + key + '=' + escape(locSpecLink);
    url += '&' + key + '=' + escape(geoLink);

	downloadFile(url);
}

downloadFile = function(url) {
    var body = Ext.getBody();
    var frame = body.createChild({
        tag:'iframe',
        id:'iframe',
        name:'iframe'
    });
    var form = body.createChild({
        tag:'form',
        id:'form',
        target:'iframe',
        method:'POST'
    });
    form.dom.action = url;
    form.dom.submit();
};


YilgarnGeoInfoWindow.prototype =  {
	'show': function(){

		var indexOfDes = this.overlayDes.indexOf('<');
		var overlayDescription =this.overlayDes.substring(indexOfDes);
		var htmlFragment ='';
		htmlFragment += '<html>';
		htmlFragment += '<body>';
		if (Ext.isIE) {
			htmlFragment += '<div style="';
			htmlFragment += 'width: expression(!document.body ? &quot;auto&quot; : (document.body.clientWidth > 599 ? &quot;600px&quot; : &quot;auto&quot;) );';
			htmlFragment += 'height: expression( this.scrollHeight > 549 ? &quot;550px&quot; : &quot;auto&quot; );';
			htmlFragment += 'overflow: auto;">';
		} else {
			htmlFragment += '<div style="max-width: 600px; max-height: 550px; overflow: hidden;">';
		}
		htmlFragment += overlayDescription;
		htmlFragment += '</div>';

		htmlFragment += '<div align="right">' +
        						'<br/>' +
        						'<input type="button" id="downloadLocBtn" style = "visibility:visible;" value="DownloadChemistry" onclick="locSpecDownload('+
        						'\'' + this.wfsUrl +'\',' +
        						'\'' + this.locSpecimenFeatureId.trim()+'\',' +
        						'\'' + this.locSpecTypeName +'\',' +
        						'\'' + this.featureId.trim()+'\',' +
        						'\'' + this.wfsTypeName+'\');"/>';

		htmlFragment += '<input type="button" id="LocSpecDetailsBtn"  value="ChemistryDetails" onclick="showLocSpecDetails('+
							'\'' + this.wfsUrl +'\',' +
							'\'' + this.locSpecTypeName +'\',' +
							'\'' + this.locSpecimenFeatureId.trim()+'\');"/>';
		htmlFragment +=	'</div>';
		htmlFragment += '</body>';
		htmlFragment += '</html>';
		this.overlay.openInfoWindowHtml(htmlFragment);
	}
};







