
//Returns true if the click has originated from a generic parser layer
var genericParserClickHandler = function (map, overlay, latlng, activeLayersStore) {
	if (overlay == null || !overlay.description)
		return false;
	
	//The generic parser stamps the description with a specific string followed by the gml:id of the node
	var genericParserString = 'GENERIC_PARSER:';
	
	if (overlay.description.indexOf(genericParserString) == 0) {
		
		//Lets extract the ID and then lookup the parent record
		var gmlID = overlay.description.substring(genericParserString.length);
		var parentRecord = null;
		for (var i = 0; i < activeLayersStore.getCount(); i++) {
			var recordToCheck = activeLayersStore.getAt(i);
			if (recordToCheck == overlay.parentRecord) {
				parentRecord = recordToCheck;
				break;
			} 
		}
		
		//Parse the parameters to our iframe popup and get that to request the raw gml
		var html = '<iframe src="genericparser.html';
		html += '?serviceUrl=' + overlay.wfsUrl;
		html += '&typeName=' + parentRecord.get('typeName');
		html += '&featureId=' + gmlID;
		html += '" width="600" height="350"/>';
		
		if (overlay instanceof GMarker) {
			overlay.openInfoWindowHtml(html);
		} else {
			map.openInfoWindowHtml(overlay.getBounds().getCenter(), html);
		}
		
		return true;
	}
		
	return false;
};

/**
 * When someone clicks on the google maps we show popups specific to each 
 * feature type/marker that is clicked on
 * 
 * This event is fired when the user clicks on the map with the mouse. A
 * click event passes different arguments based on the context of the
 * click, and whether or not the click occured on a clickable overlay. If
 * the click does not occur on a clickable overlay, the overlay argument
 * is null and the latlng argument contains the geographical coordinates
 * of the point that was clicked. If the user clicks on an overlay that
 * is clickable (such as a GMarker, GPolygon, GPolyline, or GInfoWindow),
 * the overlay argument contains the overlay object, while the
 * overlaylatlng argument contains the coordinates of the clicked
 * overlay. In addition, a click event is then also fired on the overlay
 * itself. 
 * 
 * @param {GMap2}
 * @param {GOverlay} overlay object (such as a GMarker, GPolygon, GPolyline, or GInfoWindow)
 * @param {GLatLng}  geographical coordinates
 * @param {Ext.data.Store}
 * 
 * @version $Id$
 */
var gMapClickController = function(map, overlay, latlng, activeLayersStore) {
	
	//Try to handle a generic parser layer click
	if (genericParserClickHandler(map,overlay,latlng,activeLayersStore))
		return;
	
	//Try to see if its a WCS layer
	if (overlay && overlay.parentRecord && overlay.parentRecord.get('serviceType') == 'wcs') {
		var infoWindow = new GenericWCSInfoWindow(map, overlay, overlay.wcsUrl, overlay.layerName, overlay.parentRecord.get('openDapURLs'), overlay.parentRecord.get('wmsURLs'));
		infoWindow.showInfoWindow();
	//Otherwise it could be a WFS marker
	} if (overlay instanceof GMarker) {
        if (overlay.typeName == "gsml:Borehole") {
            new NvclInfoWindow(map,overlay).show();
        }
        else if (overlay.typeName == "ngcp:GnssStation") {
            new GeodesyMarker(overlay.wfsUrl, "geodesy:station_observations", overlay.title, overlay, overlay.description).getMarkerClickedFn()();
        }
        else if (overlay.description != null) {
            overlay.openInfoWindowHtml(overlay.description, {maxWidth:800, maxHeight:600, autoScroll:true});
        }
    //Otherwise it could be a WFS polygon
    } else if (overlay instanceof GPolygon) {
    	if (overlay.description != null) {
    		map.openInfoWindowHtml(overlay.getVertex(0),overlay.description);
    	}
    //Otherwise we test each of our WMS layers to see a click will affect them
    } else {
    	//If the user clicks on an info window, we will still get click events, lets ignore these
    	if (latlng == null || latlng == undefined)
    		return;

        for (var i = 0; i < activeLayersStore.getCount(); i++) {
            var record = activeLayersPanel.getStore().getAt(i);
            if (record.get('serviceType') == 'wms' && record.get('layerVisible')) {
            	           	               
                map.getDragObject().setDraggableCursor("pointer");
                
                var TileUtl = new Tile(map,latlng);

                var url = "wmsMarkerPopup.do";
                url += "?WMS_URL=" + record.get('serviceURLs');
                url += "&lat=" + latlng.lat();
                url += "&lng=" + latlng.lng();
                url += "&QUERY_LAYERS=" + record.get('typeName');
                url += "&x=" + TileUtl.getTilePoint().x; 
                url += "&y=" + TileUtl.getTilePoint().y;
                url += '&BBOX=' + TileUtl.getTileCoordinates();
                url += '&WIDTH=' + TileUtl.getTileWidth();
                url += '&HEIGHT=' + TileUtl.getTileHeight();
                
                var typeName = record.get('typeName');
                
                if(typeName.substring(0, typeName.indexOf(":")) == "gt") {
                	handleGeotransectWmsRecord(url, activeLayersStore, map, latlng);
                } else {    
                	handleGenericWmsRecord(url, map, latlng);
            	}
                
            }
        }
    }
};

/**
 * Request json data from url, process response and open a GeotransectsInfoWindow
 * to present the data.
 * 
 * @param url
 * @param activeLayersStore
 * @param map
 * @param latlng
 */
function handleGeotransectWmsRecord(url, activeLayersStore, map, latlng) {
	
	url += "&INFO_FORMAT=application/vnd.ogc.gml";
	
    GDownloadUrl(url, function(response, responseCode) {
        if (responseCode == 200) {
            if (isGmlDataThere(response)) {                                  
            	//
            	var geotransectRecord = null;
            	
            	//Parse the response
				var XmlDoc = GXml.parse(response);
				if (g_IsIE) {
				  XmlDoc.setProperty("SelectionLanguage", "XPath");
            	}
				var rootNode = XmlDoc.documentElement;
				if (!rootNode) {
				  return;
				}
				
				var schemaLoc = rootNode.getAttribute("xsi:schemaLocation");

				var reqTypeName = schemaLoc.substring(schemaLoc.indexOf("typeName")+9, 
            			schemaLoc.indexOf(' ', schemaLoc.indexOf("typeName")+9));
            	//Browser may have replaced certain characters
            	reqTypeName = reqTypeName.replace("%3A", ":");
            	
            	//Retrieve the matching record to pass to GeotransectsInfoWindow
            	//TODO: There may be a better way to do this, involving using Extjs AJAX
            	// rather than GDownloadUrl, that allows access to the iteration record
            	// when executing the callback.
            	var j = 0;
                while (geotransectRecord == null && 
                		j < activeLayersStore.getCount()) {
                    var rec = activeLayersPanel.getStore().getAt(j);
                    if(reqTypeName == rec.get('typeName')) {
                    	geotransectRecord = rec;
                    }
                    j++;
                }
            		                               
                if(geotransectRecord != null) {
                    //Extract the line Id from the XML
                	var line = rootNode.getElementsByTagName("gt:LINE");
                    var lineId = "";
                    if(line != null && line.length > 0) {
                	    if(document.all) { //IE
                	        lineId = line[0].text;
                	    } else {
                	    	lineId = line[0].textContent;
                	    }

                	    if(lineId.indexOf("cdp") == 0) {
                	    	lineId = lineId.substring(3, lineId.length);
                	    }
                	    
                    	new GeotransectsInfoWindow(latlng, map, lineId, geotransectRecord).show();                                
                    } else {
                    	//alert("Remote server returned an unsupported response.");
                    }
                } else {
                	alert("Remote server returned an unsupported response.");
                }                           
            }
        } else if(responseCode == -1) {
            alert("Data request timed out. Please try later.");
        } else if ((responseCode >= 400) & (responseCode < 500)){
            alert('Request not found, bad request or similar problem. Error code is: ' + responseCode);
        } else if ((responseCode >= 500) & (responseCode <= 506)){
            alert('Requested service not available, not implemented or internal service error. Error code is: ' + responseCode);
        } else {
            alert('Remote server returned error code: ' + responseCode);
        }
    });
}
/**
 * Request html data from the url and open an info window to present the data.
 * 
 * @param url
 * @param map
 * @param latlng
 */
function handleGenericWmsRecord(url, map, latlng) {

 	url += "&INFO_FORMAT=text/html";
 	
    GDownloadUrl(url, function(response, responseCode) {
        if (responseCode == 200) {
            if (isHtmlDataThere(response)) {
                if (isHtmlPage(response)) {                           	                  	
                    var openWindow = window.open('','mywindow'+i);
                    if (openWindow) {
                        openWindow.document.write(response);
                        openWindow.document.close();
                    } else {
                    	alert('Couldn\'t open popup window containing WMS information. Please disable any popup blockers and try again');
                    }                                                     	
                } else {                           	                        		
                	map.openInfoWindowHtml(latlng, response, {autoScroll:true});
                }
            }
        } else if(responseCode == -1) {
            alert("Data request timed out. Please try later.");
        } else if ((responseCode >= 400) & (responseCode < 500)){
            alert('Request not found, bad request or similar problem. Error code is: ' + responseCode);
        } else if ((responseCode >= 500) & (responseCode <= 506)){
            alert('Requested service not available, not implemented or internal service error. Error code is: ' + responseCode);
        } else {
            alert('Remote server returned error code: ' + responseCode);
        }
    });
}

/**
 * Returns true if the WMS GetFeatureInfo query returns valid gml data 
 * describing a feature. Verifies this by ensuring the gml contains at least 
 * 1 featureMember.
 * 
 * @param iStr GML string content to be verified
 * @return true if the WMS GetFeatureInfo query returns valid gml data.
 */
function isGmlDataThere(iStr) {
	var lowerCase = iStr.toLowerCase();
	return lowerCase.indexOf('<gml:featuremember>') > 0;
}

/**
 * Returns true if WMS GetFeatureInfo query returns data.
 * 
 * We need to hack a bit here as there is not much that we can check for.
 * For example the data does not have to come in tabular format.
 * In addition html does not have to be well formed.
 * In addition an "empty" click can still send style information
 * 
 * So ... we will assume that minimum html must be longer then 30 chars
 * eg. data string: <table border="1"></table>
 * 
 * For a bit of safety lets only count the bytes in the body tag
 * 
 * @param {iStr} HTML string content to be verified 
 * @return {Boolean} Status of the
 */
function isHtmlDataThere(iStr) {	
	//This isn't perfect and can technically fail
	//but it is "good enough" unless you want to start going mental with the checking
	var lowerCase = iStr.toLowerCase();
	
	//If we have something resembling well formed HTML,
	//We can test for the amount of data between the body tags
	var startIndex = lowerCase.indexOf('<body>');
	var endIndex = lowerCase.indexOf('</body>');
	if (startIndex >= 0 || endIndex >= 0) {
		return ((endIndex - startIndex) > 32);
	}
		
	//otherwise it's likely we've just been sent the contents of the body 
	return lowerCase.length > 32;
}

/**
 * Returns true if WMS GetFeatureInfo query returns content
 * within html page markup.
 *
 * @param {iStr} HTML string content to be verified
 * @return {Boolean}
 */
function isHtmlPage(iStr) {
	return (iStr.toLowerCase().indexOf('<body') !=-1);
}
