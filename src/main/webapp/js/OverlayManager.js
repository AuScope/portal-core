

/*
 * Is a combination of a MarkerManager with the added extension for generic overlays too 
 */
OverlayManager = function(map) {
	this.overlayList = [];
	this.markerManager = new MarkerManager(map);
	this.map = map;
};



/**
 * Removes all overlays and markers (that are managed by this instance) from the map
 * @return
 */
OverlayManager.prototype.clearOverlays = function() {
	for (var i = 0; i < this.overlayList.length; i++) {
		this.map.removeOverlay(this.overlayList[i]);
	}
	this.overlayList = [];
	this.markerManager.clearMarkers();
}

/**
 * Adds a single overlay to the map and this instance
 * @param overlay
 * @return
 */
OverlayManager.prototype.addOverlay = function(overlay) {
	this.map.addOverlay(overlay);
	this.overlayList.push(overlay);
}


