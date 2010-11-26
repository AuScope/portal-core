

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
};

/**
 * Adds a single overlay to the map and this instance
 * @param overlay
 * @return
 */
OverlayManager.prototype.addOverlay = function(overlay) {
	this.map.addOverlay(overlay);
	this.overlayList.push(overlay);
};

/**
 * Iterates through every layer in this manager and updates the overlay zOrder
 * @param newZOrder
 * @return
 */
OverlayManager.prototype.updateZOrder = function(newZOrder) {
	for (var i = 0; i < this.overlayList.length; i++) {
		this.overlayList[i].zPriority = newZOrder;
        this.map.removeOverlay(this.overlayList[i]);
        this.map.addOverlay(this.overlayList[i]);
	}
};

/**
 * Iterates through every WMS layer sets the opacity to the specified value
 * @param newOpacity
 * @return
 */
OverlayManager.prototype.updateOpacity = function(newOpacity) {
	for (var i = 0; i < this.overlayList.length; i++) {
		if (this.overlayList[i] instanceof GTileLayerOverlay) {
			this.overlayList[i].getTileLayer().opacity = newOpacity;
	        this.map.removeOverlay(this.overlayList[i]);
	        this.map.addOverlay(this.overlayList[i]);
		}
	}
};


