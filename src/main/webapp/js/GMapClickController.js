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
	
    if (overlay instanceof GMarker) {
        
        if (overlay.typeName == "gsml:Borehole") {
            new NvclInfoWindow(map,overlay).show();
        }
        else if (overlay.typeName == "ngcp:GnssStation") {
            new GeodesyMarker(overlay.wfsUrl, "geodesy:station_observations", overlay.title, overlay, overlay.description).getMarkerClickedFn()();
        }
        else if (overlay.description != null) {
            overlay.openInfoWindowHtml(overlay.description, {maxWidth:800, maxHeight:600, autoScroll:true});
        }
        
    } else {
    	//If the user clicks on an info window, we will still get click events, lets ignore these
    	if (latlng == null || latlng == undefined)
    		return;

        for (var i = 0; i < activeLayersStore.getCount(); i++) {
            
            var record = activeLayersPanel.getStore().getAt(i);
            
            if (record.get('serviceType') == 'wms') {
                var TileUtl = new Tile(map,latlng);

                var url = "/wmsMarkerPopup.do";
                url += "?WMS_URL=" + record.get('serviceURLs');
                url += "&lat=" + latlng.lat();
                url += "&lng=" + latlng.lng();
                url += "&QUERY_LAYERS=" + record.get('typeName');
                url += "&x=" + TileUtl.getTilePoint().x; 
                url += "&y=" + TileUtl.getTilePoint().y;
                url += '&BBOX=' + TileUtl.getTileCoordinates();
                url += '&WIDTH=' + TileUtl.getTileWidth();
                url += '&HEIGHT=' + TileUtl.getTileHeight();
                //alert(url);
                
                map.getDragObject().setDraggableCursor("pointer");
                GDownloadUrl(url, function(response, responseCode) {
                    if (responseCode == 200) {
                        if (isDataThere(response)) {
                            if (isHtmlPage(response)) {
                                var openWindow = window.open('','mywindow'+i);
                                openWindow.document.write(response);
                                openWindow.document.close();
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
        }
    }
};

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
 * @param {String} HTML string content to be verified 
 * @return {Boolean} Status of the
 */
function isDataThere(iStr) {	
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
 * @param {String} HTML string content to be verified
 * @return {Boolean}
 */
function isHtmlPage(iStr) {
	return (iStr.toLowerCase().indexOf('<body') !=-1);
}
