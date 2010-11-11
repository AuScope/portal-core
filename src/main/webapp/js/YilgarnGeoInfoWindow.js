function YilgarnGeoInfoWindow(iMap,iOverlay,iWfsUrl,iFeatureId,iWfsTypeName) {
    this.map = iMap; 
    this.overlay = iOverlay;
    this.wfsUrl = iWfsUrl;
    this.featureId = iFeatureId;
    this.wfsTypeName = iWfsTypeName;
    this.overlayDes = iOverlay.description;
    this.geoServiceURL = this.wfsUrl;
    this.geoServiceURL += '&typeName=' + this.wfsTypeName;
    this.geoServiceURL += '&featureId=' + this.featureId;
    	
    this.geoFeaturePart = 'geologicUnit_';
    this.locFeaturePart = 'locatedSpecimen_';
    this.locSpecTypeName = 'sa:LocatedSpecimen';
    this.locSpecimenSubstring = this.featureId.substring(this.geoFeaturePart.length);
    this.locSpecimenFeatureId = this.locFeaturePart + this.locSpecimenSubstring;
    this.tabsArray = []; 
    
   }

YilgarnGeoInfoWindow.prototype =  {
		'TAB_1' : "gswa Details",
	    
	    'TAB_2' : "sa Details",
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
			htmlFragment += 'overflow: scroll;">';
		} else {
			htmlFragment += '<div style="max-width: 600px; max-height: 550px; overflow: scroll;">';
		}
		htmlFragment += overlayDescription;
		htmlFragment += '</div>';
								
		htmlFragment += '<div align="right">' +
        						'<br/>' +
        						'<input type="button" id="downloadLocBtn"  value="DownloadLocSpec" onclick="locSpecDownload('+
        						'\'' + this.wfsUrl +'\',' +
        						'\'' + this.locSpecimenFeatureId.trim()+'\');"/>';
		
		htmlFragment += '<input type="button" id="LocSpecDetailsBtn"  value="LocSpecDetails" onclick="showLocSpecDetails('+
							'\'' + this.wfsUrl +'\',' +
							'\'' + this.locSpecTypeName +'\',' +
							'\'' + this.locSpecimenFeatureId.trim()+'\');"/>';
		htmlFragment +=	'</div>';
		htmlFragment += '</body>';
		htmlFragment += '</html>';
		this.tabsArray.push(new GInfoWindowTab(this.TAB_1, htmlFragment));
		this.overlay.openInfoWindowTabs(this.tabsArray);
	}
};

function locSpecDownload(wfsUrl,locSpecimenFeatureId){
	var key = 'serviceUrls';
	var value=window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/" + "doLocatedSpecimenFeature.do" + "?" + "serviceUrl=" + wfsUrl + "&typeName=" + "sa:LocatedSpecimen"
	+"&featureId=" + locSpecimenFeatureId;
	
	var url = 'downloadLocSpecAsZip.do?';
    url += '&' + key + '=' + escape(value);
    
	downloadFile(url);
};
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
function showLocSpecDetails(wfsUrl ,typename, locSpecimenFeatureId){
	Ext.Ajax.request( {
	    url : 'doLocatedSpecimenFeature.do',
	    params : {
	        serviceUrl : wfsUrl,
	        typeName : typename,
	        featureId : locSpecimenFeatureId
	    },
	    callingInstance : this,
	    success: function (response, options){
	    	var jsonData = Ext.util.JSON.decode(response.responseText);
	    	var resultMessage = jsonData.result;
	    	var locSpecName = jsonData.uniqueSpecName;
	    	var records = jsonData.records;
	    	
	    	/*var locSpecNameStore = new Ext.data.ArrayStore({
		    	fields : ['loc'],
		        data   : locSpecName
		    });*/
	    	
	    	var recordItems = [];
	    	for (var i = 0; i < records.length ; i++) {	    		
	    		recordItems.push([
	    			    records[i].serviceName,
	    			    records[i].dateAndTime,
	    			    records[i].observedMineralName,
	    			    records[i].observedMineralDescription,
	    			    records[i].obsProcessContact,
	    			    records[i].obsProcessMethod,
	    			    records[i].observedProperty,
	    			    records[i].quantityName,
	    			    records[i].quantityValue,
	    			    records[i].uom,
	    			    i
	    			]);
	    		}
	    	

	    	
	    	var grid = new Ext.grid.GridPanel({
	    		    store : new Ext.data.GroupingStore({
		    		autoDestroy		: true,
		    		groupField		: 'quantityName',
		    		sortInfo		: {
		    			field			: 'quantityName',
		    			direction		: 'ASC'
		    		},
		    		reader : new Ext.data.ArrayReader({
		    			fields : [
		    			    'name',
		    			    'date&time',
		    			    'MineralName',
		    			    'MineralDescription',
		    			    'ProcessContact',
		    			    'ProcessMethod',
		    			    'Property',
		    			    'quantityName',
		    			    'quantityValue',
		    			    'uom',
		    			    'RecordIndex'
		    			]
		    		}),
		    		data : recordItems
		    	}),
		    	colModel:new Ext.grid.ColumnModel({
		    		 defaults: {
		             	sortable: true // columns are not sortable by default           
		         },
		         columns: [{
		             id: 'name',
		             header: 'Common Name',
		             dataIndex: 'name',
		             width: 200
		         },{
		             header: 'date&time',
		             dataIndex: 'date&time',
		             width: 100
		         },{
		             header: 'MineralDescription',
		             dataIndex: 'MineralDescription',
		             width: 200
		         },{
		             header: 'ProcessContact',
		             dataIndex: 'ProcessContact',
		             width: 100
		         },{
		             header: 'quantityName',
		             dataIndex: 'quantityName',
		             width: 100
		         },{
		             header: 'quantityValue',
		             dataIndex: 'quantityValue',
		             width: 100
		         },{
		             header: 'uom',
		             dataIndex: 'uom',
		             width: 100
		         }]
	
		    	 }),
		    	 view: new Ext.grid.GroupingView({
		    	        forceFit: true,
		    	        // custom grouping text template to display the number of items per group
		    	        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
		    	    }),
		    	    frame:true,
		    	    columnLines: true,
		    	    iconCls:'icon-grid',
		    	    width: 1000,
		    	    height: 450
		    	   
	    	});
	    	
	    	 var win = new Ext.Window({
	    		
			        	title: 'Located Specimen Details',
		               	layout:'fit',
		                width:1000,
		                height:500,

	                    items: [grid]
	    		 
	    	 });
	    	 win.show(this);
	    },
	    
	    failure: function (result, request){
	    }
	});
	
}

YilgarnGeoInfoWindow.prototype.someFunction = function(){
	
	var mineralTypeStore = new Ext.data.SimpleStore({
        fields : ['type'],
        data   : mineralTypes
    });
	
};



