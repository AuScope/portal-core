/**
 * OverlayManager Is a combination of a MarkerManager with the added extension for
 * generic overlays too
 *
 * It supports the following events
 * clear - function(OverlayManager manager) - raised after clearOverlays is called
 * addoverlay - function(OverlayManager manager, GOverlay overlay) - raised whenever addOverlay is called
 *                                                                   (after the overlay has been added to the map)
 */
OverlayManager = Ext.extend(Ext.util.Observable, {
    overlayList : null,
    markerManager : null,
    map : null,

    /**
     * Expects an instance of a GMap2 object
     */
    constructor : function(map) {
        this.overlayList = [];
        this.markerManager = new MarkerManager(map);
        this.map = map;
        OverlayManager.superclass.constructor.call(this, {});
        this.addEvents('clear', 'addoverlay');
    },

    /**
     * Removes all overlays and markers (that are managed by this instance) from the map
     * @return
     */
    clearOverlays : function() {
        for (var i = 0; i < this.overlayList.length; i++) {
            this.map.removeOverlay(this.overlayList[i]);
        }
        this.overlayList = [];
        this.markerManager.clearMarkers();

        this.fireEvent('clear', this);
    },

    /**
     * Adds a single overlay to the map and this instance
     * @param overlay
     * @return
     */
    addOverlay : function(overlay) {
        this.map.addOverlay(overlay);
        this.overlayList.push(overlay);

        this.fireEvent('addoverlay', this, overlay);
    },

    /**
     * Iterates through every layer in this manager and updates the overlay zOrder
     * @param newZOrder
     * @return
     */
    updateZOrder : function(newZOrder) {
        for (var i = 0; i < this.overlayList.length; i++) {
            this.overlayList[i].zPriority = newZOrder;
            this.map.removeOverlay(this.overlayList[i]);
            this.map.addOverlay(this.overlayList[i]);
        }
    },

    /**
     * Iterates through every WMS layer sets the opacity to the specified value
     * @param newOpacity
     * @return
     */
    updateOpacity : function(newOpacity) {
        for (var i = 0; i < this.overlayList.length; i++) {
            if (this.overlayList[i] instanceof GTileLayerOverlay) {
                this.overlayList[i].getTileLayer().opacity = newOpacity;
                this.map.removeOverlay(this.overlayList[i]);
                this.map.addOverlay(this.overlayList[i]);
            }
        }
    }
});






