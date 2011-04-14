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
function GeotransectsInfoWindow(iLatlng, iMap, iLineId, iCSWRecord, iOnlineResource, iUrl) {
    this.map = iMap;
    this.latlng = iLatlng;
    this.cswRecord = iCSWRecord;
    this.onlineResource = iOnlineResource;
    this.tabsArray = [];
    this.lineId = iLineId;
	this.linedesc = "";
	this.layername = "";
	this.actualRegistryItemURL = "";
	this.linkingLine = "";
	this.url = iUrl;
	this.serviceurl = "";
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

    'TAB_1' : "Layer",
    'TAB_2' : "Line",
    'TAB_3' : "Seismic Data",
    'TAB_4' : "SegY Data",

    'GETSEISMICSECTIONS' : "/geotransect-dataservices/getSeismicSections.json?",

    'GETSEGYDATASETS' : "/geotransect-dataservices/getSEGYDatasets.json?",

	'show': function() {
		//Open our window with the basic info displayed
		//Stock WMS Response

        this.mask.show();

	    // Hack to find the line's descrition from CSW
		var linecsw = cswRecordStore.getCSWRecordsByKeywords([this.lineId]);

	    try {
	    	this.linedesc = linecsw[0].getDataIdentificationAbstract();
	    	this.serviceurl = linecsw[0].getOnlineResources()[0].url;
	    	this.layername = linecsw[0].getOnlineResources()[0].name;
	    	this.linkingLine = "Specific Line information from <a target=_blank href=" + linecsw[0].getRecordInfoUrl() + ">registry</a>:";
	    }
	    catch (err) {
	    	// Line is not found in the CSW as a HighRes line
	    	this.linedesc = this.description;
	    	this.serviceurl = "No HighRes Layer URL Found in Registry";
	    	this.layername = "No HighRes Layer Name Found in Registry";
	    	this.linkingLine = "No Specific Line information found in registy";

		}


	    //TODO: use of the CSS styles causes issues with the layout in IE especially
	    // (the pre style also has issues in FF). This is the best I can do for now
	    // to make slightly less ugly in IE. Will need to look at it again later.
		this.tabsArray[0] = new GInfoWindowTab(this.TAB_2,
				'<div style="padding-bottom:10px;" >' +
				this.linkingLine + '</div>' +
				'<div style="min-width:400; min-height:300;">' +
				'<table border="1" cellspacing="1" cellpadding="4" class="auscopeTable">' +
				'<tr><td id="headings">ID</td><td id="data">' + this.lineId + '</td></tr>' +
				'<tr><td id="headings">Description</td><td id="data">' +
				'<pre id="auscopePre">' +
				this.linedesc + '</pre></td></tr>' +
				'<tr><td id="headings">HighRes Service URL</td><td id="data">' + this.serviceurl + '</td></tr>' +
				'<tr><td id="headings">HighRes Layer Name</td><td id="data">' + this.layername + '</td></tr>' +
				'</table>' +
				'</div>');

		//The following initialisation is required as the tabs later added asynchronously
		//may end up being added in the wrong order (2 before 1 exists) resulting in errors
		//such as: contextElem is null or not an object.
		this.tabsArray[1] = new GInfoWindowTab(this.TAB_3, "");
		this.tabsArray[2] = new GInfoWindowTab(this.TAB_4, "");

		var me = this;

        this.map.openInfoWindowTabs(this.latlng, this.tabsArray,
        		{maxWidth:800, maxHeight:300, autoScroll:true,
        	onOpenFn:function(){
                me.retrieveDatasets();
        	}});
	},

	'retrieveDatasets' : function() {

		this.map.getInfoWindow().hide();

		//request data urls - seismic section data
	    var url = this.geoServerUrl;
        url += this.GETSEISMICSECTIONS;
        url += "lineId="+this.lineId;
        this.requestData(url, 1, this.TAB_3);

		//request data urls - segy data
	    url = this.geoServerUrl;
    	url += this.GETSEGYDATASETS;
        url += "lineId="+this.lineId;
        this.requestData(url, 2, this.TAB_4);
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
        		var responseObj;
        		try {
	        		responseObj = Ext.util.JSON.decode(Ext.util.JSON.decode(response.responseText).json);
        		}
	        	catch (err) {
	        		me.mask.hide();
        			Ext.Msg.alert('Error downloading data', 'There was an error whilst communicating with the geotransects data server');
                    return;
	        	}

        		//Generate an error / success fragment to display to the user
        		if (!responseObj.result.success) {
        			me.mask.hide();
        			Ext.Msg.alert('Error downloading data', 'The service returned a failure result status ' + url);
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
                me.mask.hide();
        	}
        });
	},

	'addDataTab' : function(values, tabI, label) {
		//Parse records and build html string
    	var sHtml = "";

		if(values.length > 1) {
			sHtml += '<div class="niceDiv">' +
				'<div style="padding-bottom:10px;" >' +
				'Available data sets:' +
				'</div>' +
				'<div>' +
				'Note: Browsers may not be able to display the following data due to the '+
				'format or large size. It is advisable to download the data before viewing '+
				'(right click > Save Target / Link As).' +
				'</div>' +
				'<div>' +
				'<table border="1" cellspacing="1" cellpadding="4" class="auscopeTable">';

			for (var i = 0; i < values.length; i++) {
				sHtml += "<tr><td>";
				sHtml += "<a href='" + values[i] + "' target='_blank'>"+ values[i] +"</a><br/>";
				sHtml += "</tr></td>";
			}

			sHtml += "</table></div></div>";
	        this.mask.hide();
		}
		else {
			sHtml += '<div class="niceDiv">' +
			         'No DataSets available.' +
			          '</div>';
	        this.mask.hide();
		}

		//Create new tab
        this.tabsArray[tabI] = new GInfoWindowTab(label, sHtml);

        //Add new tab to pop-up window
        this.map.updateInfoWindow(this.tabsArray);
        this.mask.hide();
	}
};