/**
 * @class PressureDbInfoWindow
 * <p>PressureDb marker info window pop-up</p>
 */

/**
 * @constructor
 * @param {GMap2}   iMap
 * @param {GMarker} iMarker
 * @param {wfsUrl}  wfsUrl
 */
function PressureDbInfoWindow(iMap, iMarker, wfsUrl) {

    this.Map = iMap;
    this.Marker = iMarker;
    this.boreholeId = iMarker.title || "";
    this.waitHtml =
        '<div>' +
            '<b>' + this.boreholeId + '</b>' +
            '<p style="text-align:center;">' +
                '<img src="img/wait.gif" style="padding-top:50px;" />' +
            '</p>' +
        '</div>';
    this.wfsServiceUrl = wfsUrl;

    /**
     * iMarker.wfsUrl comes from GeoNetwork and represents GeoServer's service
     * Url. From this Url we remove pathname and return only protocol with
     * hostname eg.: http://server.com.au/nvcl/wfs --> http://server.com.au
     */
    this.geoServerUrl = (function(url) {
        var str = url.slice( ("http://").length);
        return 'http://' + str.slice(0,str.indexOf("/"));
    })(wfsUrl);
}

PressureDbInfoWindow.prototype = {

    'show': function() {
    	var indexOfDes = this.Marker.description.indexOf('<');
    	var overlayDescription =this.Marker.description.substring(indexOfDes);
    	var summaryHtml ='';
    	summaryHtml += '<html>';
    	summaryHtml += '<body>';
    	if (Ext.isIE) {
    		summaryHtml += '<div style="';
    		summaryHtml += 'width: expression(!document.body ? &quot;auto&quot; : (document.body.clientWidth > 299 ? &quot;300px&quot; : &quot;auto&quot;) );';
    		summaryHtml += 'height: expression( this.scrollHeight > 549 ? &quot;550px&quot; : &quot;auto&quot; );';
    		summaryHtml += 'overflow: auto;">';
    	} else {
    		summaryHtml += '<div style="max-width: 300px; max-height: 550px; overflow: hidden;">';
    	}
    	summaryHtml += overlayDescription;
    	summaryHtml += '</div>';
    	
        var me = this;
        var serverAddr = this.geoServerUrl;
        
        summaryHtml += '<div align="right">' +
        						'<br/>' +
        			              '<input type="button" id="downloadDatasetBtn" value="Download Observations" name=butDownloadDataset onclick="openPressureDbDownloadWindow(\''+ this.boreholeId +'\',\''+ this.wfsServiceUrl +'\',\''+ this.geoServerUrl +'\')">';
        summaryHtml +=	'</div>';
        summaryHtml += '</body>';
        summaryHtml += '</html>';
    	
    	this.Marker.openInfoWindowHtml(summaryHtml);
    }

}; // End of PressureDbInfoWindow.prototype

var openPressureDbDownloadWindow = function(boreholeId, wfsServiceUrl, geoServerUrl)
{
	var keys = [];
    var values = [];
    
    request = wfsServiceUrl;
    request += '?request=getFeature';
    request += '&typeName=sa:SamplingFeatureCollection';
    request += '&featureId=' + 'sa.samplingfeaturecollection.' + boreholeId;
    
    keys.push("serviceUrls");
    values.push(request);
    
	keys.push("filename");
	values.push("PressureDb_Download");
    
    url = "downloadDataAsZip.do?";
    
    if (keys && values && (keys.length == values.length)) {
        for (var i = 0; i < keys.length; i++) {
            url += '&' + keys[i] + '=' + escape(values[i]);
        }
    }

    download(url);
};

//downloads given specified file.
download = function(url) {
    var body = Ext.getBody();
    var frame = body.createChild({
        tag:'iframe',
        cls:'x-hidden',
        id:'iframe',
        name:'iframe'
    });
    var form = body.createChild({
        tag:'form',
        cls:'x-hidden',
        id:'form',
        target:'iframe',
        method:'POST'
    });
    form.dom.action = url;
    form.dom.submit();
};

