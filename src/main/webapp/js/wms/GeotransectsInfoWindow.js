/**
 * @class GeotransectsInfoWindow
 *
 * @author jac24m
 */

/**
 * cswRecord - CSWRecord - the CSW Record that is a parent to onlineResource
 * onlineResource - Object - The online resource that was used to render this geotransect
 * @constructor
 */
function GeotransectsInfoWindow(iLatlng, iMap, iLineId, iCSWRecord, iOnlineResource) {
    this.map = iMap;
    this.latlng = iLatlng;
    this.cswRecord = iCSWRecord;
    this.onlineResource = iOnlineResource;
    this.tabsArray = [];

    this.lineId = iLineId;
    this.description = this.cswRecord.getDataIdentificationAbstract();

    //Get the GeoServer's service Url, removing pathname and return only protocol with
    //hostname eg.: http://server.com.au/nvcl/wfs --> http://server.com.au
    this.geoServerUrl = (function(url) {
        var str = url.slice( ("http://").length);
        return 'http://' + str.slice(0,str.indexOf("/"));
    })(this.onlineResource.url);

    this.mask = new Ext.LoadMask(Ext.get('center_region'), {msg:"Please wait..."});
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
				"<div style='min-width:400; min-height:300;'>" +
				"<table border=\"1\" cellspacing=\"1\" width=\"100%\" bgcolor=\"#EAF0F8\">"
				+"<tr><td><pre style=\"white-space:pre-wrap;white-space:-moz-pre-wrap;" +
						"white-space:-pre-wrap;white-space:-o-pre-wrap;word-wrap:break-word;" +
						"width:99%;overflow:auto;\">"
				+ this.description
				+ "</pre></td></tr>"
				+"</table>" +
			    "</div>");

		//The following initialisation is required as the tabs later added asynchronously
		//may end up being added in the wrong order (2 before 1 exists) resulting in errors
		//such as: contextElem is null or not an object.
		this.tabsArray[1] = new GInfoWindowTab(this.TAB_2, "");
		this.tabsArray[2] = new GInfoWindowTab(this.TAB_3, "");

		var me = this;

        this.map.openInfoWindowTabs(this.latlng, this.tabsArray,
        		{maxWidth:800, maxHeight:300, autoScroll:true,
        	onOpenFn:function(){
                me.retrieveDatasets();
        	}});

	},

	'retrieveDatasets' : function() {

		this.map.getInfoWindow().hide();
        this.mask.show();

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
//              	this.mask.hide();
        			Ext.Msg.alert('Error downloading data', 'There was an error whilst communicating with ' + url);
        			return;
        		}
//        		} else if (responseObj.items.length == 0) {
//        	        this.mask.hide();
//        			Ext.Msg.alert('Error downloading data', 'The URL ' + url + ' returned no parsable records');
//        			return;
//        		}

        		//Parse records and download the data
                var values = [responseObj.items.length];
            	for (var i = 0; i < responseObj.items.length; i++) {
            		values[i] = responseObj.items[i].url;
            	}

            	me.addDataTab(values, tabI, label);

        	},
        	failure: function(response, options) {
//                this.mask.hide();
        		Ext.Msg.alert('Error requesting data', 'Error (' + response.status + '): ' + response.statusText);
        	}
        });
	},

	'addDataTab' : function(values, tabI, label) {
		//Parse records and build html string
    	var sHtml = "";

		if(values.length > 1) {
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
	        this.mask.hide();
		}
		else {
			sHtml += '<div style="padding-bottom:10px;" >' +
			'No DataSets available.' +
			'</div>'
	        this.mask.hide();
		}

		//Create new tab
        this.tabsArray[tabI] = new GInfoWindowTab(label, sHtml);

        //Add new tab to pop-up window
        this.map.updateInfoWindow(this.tabsArray);



//        if(values.length > 1) {
//        	this.map.getInfoWindow().show();
//        }
	}

};
