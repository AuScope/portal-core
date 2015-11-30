/**
 * Concrete implementation of portal.map.BasePrimitiveManager
 * which is specialised for managing google map primitives.
 */
Ext.define('portal.map.gmap.PrimitiveManager', {
    extend: 'portal.map.BasePrimitiveManager',

    constructor : function(config) {
        this.callParent(arguments);
        this.markerManager = new MarkerManager(this.baseMap.map);
        this.primitiveList = [];
    },

    /**
     * See parent for definition.
     */
    clearPrimitives : function() {
        for (var i = 0; i < this.primitiveList.length; i++) {
            this.baseMap.map.removeOverlay(this.primitiveList[i]);
        }
        this.primitiveList = [];
        this.markerManager.clearMarkers();

        this.fireEvent('clearprimitives', this);
    },

    /**
     * See parent for definition.
     */
    addPrimitives : function(primitives) {
        var markers = [];
        for (var i = 0; i < primitives.length; i++) {
            var prim = primitives[i];

            if (prim instanceof portal.map.gmap.primitives.Marker) {
                markers.push(prim.getMarker());
            } else if (prim instanceof portal.map.gmap.primitives.Polygon) {
                var overlay = prim.getPolygon();
                this.baseMap.map.addOverlay(overlay);
                this.primitiveList.push(overlay);
            } else if (prim instanceof portal.map.gmap.primitives.Polyline) {
                var overlay = prim.getPolyline();
                this.baseMap.map.addOverlay(overlay);
                this.primitiveList.push(overlay);
            } else if (prim instanceof portal.map.gmap.primitives.WMSOverlay) {
                var overlay = prim.getTileLayerOverlay();
                this.baseMap.map.addOverlay(overlay);
                this.primitiveList.push(overlay);
            }
        }

        if (markers.length > 0) {
            this.markerManager.addMarkers(markers, 0);
            this.markerManager.refresh();
        }

        this.fireEvent('addprimitives', this, primitives);
    }
});






