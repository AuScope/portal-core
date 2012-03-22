/**
 * OverlayManager Is a combination of a MarkerManager with the added extension for
 * generic overlays too
 *
 * It supports the following events
 * clear - function(OverlayManager manager) - raised after clearOverlays is called
 * addoverlay - function(OverlayManager manager, GOverlay[] overlays) - raised whenever addOverlay is called
 *                                                                   (after the overlay has been added to the map)
 * addmarker - function(OverlayManager manager, GMarker[] markers) - raised whenever a marker is added to the map
 */
Ext.define('portal.util.gmap.OverlayManager', {
    extend: 'Ext.util.Observable',

    /**
     * {
     *  map : an instance of a GMap2 object (see google map API v2)
     * }
     */
    constructor : function(config) {
        this.addEvents({
            'clear' : true,
            'addoverlay' : true
        });

        this.listeners = config.listeners;
        this.map = config.map;
        this.markerManager = new MarkerManager(this.map); //this is a non Ext/Portal managed class and thus requires 'new' keyword
        this.overlayList = [];

        this.callParent(arguments);
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
     * @param overlay instance of GOverlay
     * @return
     */
    addOverlay : function(overlay) {
        this.map.addOverlay(overlay);
        this.overlayList.push(overlay);

        this.fireEvent('addoverlay', this, [overlay]);
    },

    /**
     * Adds an array of overlays to the map and this instance
     * @param overlay an array of GOverlay objects
     * @return
     */
    addOverlays : function(overlays) {
        //Add layers to map
        for (var i = 0; i < overlays.length; i++) {
            this.map.addOverlay(overlays[i]);
        }
        this.overlayList = this.overlayList.concat(overlays);

        this.fireEvent('addoverlay', this, overlays);
    },

    /**
     * Adds an array of GMarker objects to this OverlayManager and internal map
     * @param markers and array of GMarker objects
     */
    addMarkers : function(markers) {
        this.markerManager.addMarkers(markers, 0);
        this.markerManager.refresh();

        this.fireEvent('addmarker', this, markers);
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






