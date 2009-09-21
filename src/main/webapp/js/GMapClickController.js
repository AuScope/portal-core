/**
 * When someone clicks on the google maps we show popups specific to each feature type/marker that is clicked on
 * @param overlay
 * @param latlng
 * @param statusBar
 * @param viewport
 */
var gMapClickController = function(map, overlay, latlng, statusBar, viewport, activeLayersStore) {

    if (overlay instanceof GMarker) {
        if (overlay.typeName == "gsml:Borehole") {
            new NVCLMarker(overlay.title, overlay, overlay.description).getMarkerClickedFn()();
        }
        else if (overlay.typeName == "ngcp:GnssStation") {
            new GeodesyMarker(overlay.wfsUrl, "geodesy:station_observations", overlay.title, overlay, overlay.description).getMarkerClickedFn()();
        }
        else if (overlay.description != null) {
            overlay.openInfoWindowHtml(overlay.description, {maxWidth:800, maxHeight:600, autoScroll:true});
               // overlay.openInfoWindowHtml(overlay.description);
        }
    } else {        
        for (i = 0; i < activeLayersStore.getCount(); i++) {
            var record = activeLayersPanel.getStore().getAt(i);
            if (record.get('serviceType') == 'wms') {
                var TileUtl = new Tile(map,latlng);

                var url = "/wmsMarkerPopup.do"
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
                GDownloadUrl(url, function(response, pResponseCode) {
                    if (pResponseCode == 200) {
                        if (isDataThere(response)) {
                            if (isHtmlPage(response)) {
                                var openWindow = window.open('','mywindow'+i);
                                openWindow.document.write(response);
                                openWindow.document.close();
                            } else {
                                map.openInfoWindowHtml(latlng, response, {autoScroll:true});
                            }
                        }
                    } else {
                        alert(pResponseCode);
                    }
                });
            }        	    			
    	}
    }
};

/**
 * Attempts to find out if WMS GetFeatureInfo query returns data.
 * 
 * We need to hack a bit here as there is not much that we can check  
 * for. For example the data does not have to come in tabular format.
 * In addition html does not have to be well formed.
 * 
 * So ... we assume that minimum html must be longer then 30 chars
 * eg. data string: <table border="1"></table>
 * 
 * @param iStr HTML string content to be verified 
 * @return Boolean. 
 */
function isDataThere(iStr) {
	return (iStr.length > 30) ? true : false;
}

/**
 * Attempts to find out if WMS GetFeatureInfo query returns content
 * with page markup.
 *
 * @param iStr HTML string content to be verified 
 * @return Boolean. 
 */
function isHtmlPage(iStr) {
	return (iStr.toLowerCase().indexOf('<body') !=-1);
}
