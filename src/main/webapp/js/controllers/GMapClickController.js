var gMapClickController = function(overlay, latlng, statusBar, viewport) {
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
                overlay.openInfoWindowHtml(overlay.description);
            }

    }

    statusBar.clearStatus();
    statusBar.setVisible(false);
    viewport.doLayout();
};