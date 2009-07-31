/**
 * When someone clicks on the google maps we show popups specific to each feature type/marker that is clicked on
 * @param overlay
 * @param latlng
 * @param statusBar
 * @param viewport
 */
var gMapClickController = function(map, overlay, latlng, statusBar, viewport, treePanel) {
    statusBar.showBusy();
    statusBar.setVisible(true);
    viewport.doLayout();

    if (overlay instanceof GMarker) {
        if (overlay.featureType == "gsml:Borehole") {
            new NVCLMarker(overlay.title, overlay, overlay.description).getMarkerClickedFn()();
        }
        else if (overlay.featureType == "geodesy:stations") {
            new GeodesyMarker(overlay.wfsUrl, "geodesy:station_observations", overlay.getTitle(), overlay, overlay.description).getMarkerClickedFn()();
        }
        else if (overlay.description != null) {
            overlay.openInfoWindowHtml(overlay.description, {maxWidth:800, maxHeight:600, autoScroll:true});
               // overlay.openInfoWindowHtml(overlay.description);
        }
    }
    else if(latlng != null && treePanel.getSelectionModel().getSelectedNode() != null) { //geologic unit layer
        //var queryString = ProxyURL+"http://www.gsv-tb.dpi.vic.gov.au/AuScope-GeoSciML/services?service=WFS%26version=1.1.0%26request=GetFeature%26typeName=gsml:GeologicUnit%26outputFormat=text/xml;%20subtype=geoscimlhtml%26filter=%3Cogc:Filter%20xmlns:wfs=%22http://www.opengis.net/wfs%22%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:gml=%22http://www.opengis.net/gml%22%20xmlns:gsml=%22urn:cgi:xmlns:CGI:GeoSciML:2.0%22%3E%3Cogc:BBOX%3E%3Cogc:PropertyName%3Egsml:occurrence/gsml:MappedFeature/gsml:shape%3C/ogc:PropertyName%3E%3Cgml:Envelope%20srsName=%22EPSG:4326%22%3E%3Cgml:lowerCorner%3E"+latlng.lng()+"%20"+latlng.lat()+"%3C/gml:lowerCorner%3E%3Cgml:upperCorner%3E"+latlng.lng()+"%20"+latlng.lat()+"%3C/gml:upperCorner%3E%3C/gml:Envelope%3E%3C/ogc:BBOX%3E%3C/ogc:Filter%3E";
        //var queryString = ProxyURL+'http://www.gsv-tb.dpi.vic.gov.au/AuScope-GeoSciML/services?service=WFS&version=1.1.0&request=GetFeature&typeName=gsml:GeologicUnit&outputFormat=text/xml;subtype=geoscimlhtml&filter=<ogc:Filter xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0"><ogc:BBOX><ogc:PropertyName>gsml:occurrence/gsml:MappedFeature/gsml:shape</ogc:PropertyName><gml:Envelope srsName="EPSG:4326"><gml:lowerCorner>'+latlng.lng()+' '+latlng.lat()+'</gml:lowerCorner><gml:upperCorner>'+latlng.lng()+' '+latlng.lat()+'</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>';
        if(treePanel.getSelectionModel().getSelectedNode().text == "Geologic Units") {
            var url = "/geologicUnitPopup.do?lat=" + latlng.lat() + "&lng=" + latlng.lng();
            GDownloadUrl(url, function(response, pResponseCode) {
                if(pResponseCode == 200) {
                    map.openInfoWindowHtml(latlng, response, {autoScroll:true});
                    //map.openInfoWindowHtml(latlng, response);
                }
            });
        }
    }


    statusBar.clearStatus();
    statusBar.setVisible(false);
    viewport.doLayout();

};