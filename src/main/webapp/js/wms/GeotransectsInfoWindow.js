/**
 * @class GeotransectsInfoWindow
 * 
 * @author jac24m
 */

/**
 * @constructor
 */
function GeotransectsInfoWindow(iLatlng, iMap, iLineId, iRecord) {
    this.map = iMap; 
    this.latlng = iLatlng;
    this.record = iRecord;
    this.tabsArray = [];   
    
    this.lineId = iLineId;
    this.description = this.record.get('description');
    
    //Get the GeoServer's service Url, removing pathname and return only protocol with 
    //hostname eg.: http://server.com.au/nvcl/wfs --> http://server.com.au      
    this.geoServerUrl = (function(url) {        
        var str = url.slice( ("http://").length);   
        return 'http://' + str.slice(0,str.indexOf("/"));
    })(this.record.get('serviceURLs')[0]);  
}

GeotransectsInfoWindow.prototype = {
		
    'TAB_1' : "Summary",
    'TAB_2' : "Seismic Data",
    'TAB_3' : "SegY Data",
    
    'GETSEISMICSECTIONS' : "/geotransect-dataservices/getSeismicSections.json?",
    
    'GETSEGYDATASETS' : "/geotransect-dataservices/getSEGYDatasets.json?",
	    
	'show': function() {
		//Open our window with the basic info displayed
		this.tabsArray[0] = new GInfoWindowTab(this.TAB_1, 
				"<table border=\"1\" cellspacing=\"1\" width=\"100%\" bgcolor=\"#EAF0F8\">" 
				+"<tr><td><pre style=\"white-space:pre-wrap;white-space:-moz-pre-wrap;" +
						"white-space:-pre-wrap;white-space:-o-pre-wrap;word-wrap:break-word;" +
						"overflow:auto;\">" 
				+ this.description 
				+ "</pre></td></tr>" 
				+"</table>");
		
		//The following initialisation is required as the tabs later added asynchronously 
		//may end up being added in the wrong order (2 before 1 exists) resulting in errors
		//such as: contextElem is null or not an object.
		this.tabsArray[1] = new GInfoWindowTab(this.TAB_2, "");
		this.tabsArray[2] = new GInfoWindowTab(this.TAB_3, "");
		
        this.map.openInfoWindowTabs(this.latlng, this.tabsArray, 
        		{maxWidth:800, maxHeight:500, autoScroll:true});

        //And update it with the downloaded data as it arrives
        this.retrieveDatasets(); 
	},

	'retrieveDatasets' : function() {
	
		//request data urls - seismic section data
	    var url = this.geoServerUrl;    
        url += this.GETSEISMICSECTIONS;
        url += "lineId="+this.lineId;
        this.requestData(url, 1, this.TAB_2);

		//request data urls - segy data  
	    url = this.geoServerUrl;
    	url += this.GETSEGYDATASETS;
        url += "lineId="+this.lineId;
        this.requestData(url, 2, this.TAB_3);
                
	},
	
	'requestData' : function(url, tabI, label) {
		var me = this;
		
        Ext.Ajax.request({
        	url: 'requestGeotransectsData.do',
        	timeout		: 180000,
        	params		: {
        		serviceUrl 		: url
        	},
        	success: function(response, options) {
        		var responseObj = Ext.util.JSON.decode(Ext.util.JSON.decode(response.responseText).json);
       			
        		//Generate an error / success fragment to display to the user
                if (!responseObj.result.success) {
        			Ext.Msg.alert('Error downloading data', 'There was an error whilst communicating with ' + url);
        			return;
        		} else if (responseObj.items.length == 0) {
        			Ext.Msg.alert('Error downloading data', 'The URL ' + url + ' returned no parsable records');
        			return;
        		}
                        		              
        		//Parse records and download the data
                var values = [responseObj.items.length];               
            	for (var i = 0; i < responseObj.items.length; i++) {
            		values[i] = responseObj.items[i].url;
            	}
            	
            	me.addDataTab(values, tabI, label);       		      
        	},
        	failure: function(response, options) {
        		Ext.Msg.alert('Error requesting data', 'Error (' + response.status + '): ' + response.statusText);
        	}
        });
	},
	
	'addDataTab' : function(values, tabI, label) {
		//Parse records and build html string
    	var sHtml = "";
    	
		if(values.length > 0) {             
			sHtml += '<div style="padding-bottom:10px;" >' + 
				'Available data sets:' + 
				'</div>' +
				'<div>' +
				'Note: Browsers may not be able to display the following data due to the '+
				'format or large size. It is advisable to download the data before viewing '+
				'(right click > Save Target / Link As).' +
				'</div>' +
				'<div>' +
				"<table border=\"1\" cellspacing=\"1\" width=\"100%\" bgcolor=\"#EAF0F8\">";
        
			for (var i = 0; i < values.length; i++) {
				sHtml += "<tr><td>";
				sHtml += "<a href='" + values[i] + "' target='_blank'>"+ values[i] +"</a><br/>";	
				sHtml += "</tr></td>";
			}                          	

			sHtml += "</table>";
			sHtml += '</div>'; 
		}
    	
    	//Create new tab
        this.tabsArray[tabI] = new GInfoWindowTab(label, sHtml);
		
        // Add new tab to pop-up window
        this.map.updateInfoWindow(this.tabsArray);  
	}
	
};
