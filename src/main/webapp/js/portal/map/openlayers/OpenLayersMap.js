/**
 * Concrete implementation for OpenLayers
 */
Ext.define('portal.map.openlayers.OpenLayersMap', {
    extend : 'portal.map.BaseMap',

    map : null, //Instance of OpenLayers.Map
    vectorLayer : null,
    markerLayer : null,
    selectControl : null,

    constructor : function(cfg) {
        this.callParent(arguments);
    },

    /////////////// Unimplemented functions

    /**
     * See parent class for information.
     */
    makeMarker : function(id, tooltip, sourceCswRecord, sourceOnlineResource, sourceLayer, point, icon) {
        return Ext.create('portal.map.openlayers.primitives.Marker', {
            id : id,
            tooltip : tooltip,
            layer : sourceLayer,
            onlineResource : sourceOnlineResource,
            cswRecord : sourceCswRecord,
            point : point,
            icon : icon
        });
    },

    /**
     * See parent class for information.
     */
    makePolygon : function(id, sourceCswRecord, sourceOnlineResource, sourceLayer, points, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity) {
        return Ext.create('portal.map.openlayers.primitives.Polygon', {
            id : id,
            layer : sourceLayer,
            onlineResource : sourceOnlineResource,
            cswRecord : sourceCswRecord,
            points : points,
            strokeColor : strokeColor,
            strokeWeight : strokeWeight,
            strokeOpacity : strokeOpacity,
            fillColor : fillColor,
            fillOpacity : fillOpacity
        });
    },

    /**
     * See parent class for information.
     */
    makePolyline : function(id, sourceCswRecord,sourceOnlineResource, sourceLayer, points, color, weight, opacity) {
        return Ext.create('portal.map.openlayers.primitives.Polyline', {
            id : id,
            layer : sourceLayer,
            onlineResource : sourceOnlineResource,
            cswRecord : sourceCswRecord,
            points : points,
            strokeColor : strokeColor,
            strokeWeight : strokeWeight,
            strokeOpacity : strokeOpacity
        });
    },

    /**
     * See parent class for information.
     */
    makeWms : function(id, sourceCswRecord, sourceOnlineResource, sourceLayer, wmsUrl, wmsLayer, opacity) {
        return Ext.create('portal.map.openlayers.primitives.WMSOverlay', {
            id : id,
            layer : sourceLayer,
            onlineResource : sourceOnlineResource,
            cswRecord : sourceCswRecord,
            wmsUrl : wmsUrl,
            wmsLayer : wmsLayer,
            opacity : opacity,
            map : this.map
        });
    },

    _onClick : function(e) {
        var lonlat = this.map.getLonLatFromViewPortPx(e.xy);
        console.log("You clicked near " + lonlat.lat + " N, " +
                                  + lonlat.lon + " E and the arguments are: ", arguments);
    },

    _onPrimitivesAdded : function(primManager) {
        this.selectControl.setLayer(primManager.allLayers);
    },

    /**
     * Renders this map to the specified Ext.container.Container.
     *
     * Also sets the rendered property.
     *
     * function(container)
     *
     * @param container The container to receive the map
     */
    renderToContainer : function(container) {
        var containerId = container.body.dom.id;

        this.map = new OpenLayers.Map(containerId, {
            controls : [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.PanZoomBar(),
                //new OpenLayers.Control.LayerSwitcher({'ascending':false}), //useful for debug
                new OpenLayers.Control.MousePosition(),
                new OpenLayers.Control.KeyboardDefaults()
            ]
        });

        var baseLayer = new OpenLayers.Layer.WMS( "OpenLayers WMS",
                "http://vmap0.tiles.osgeo.org/wms/vmap0",
                {layers: 'basic'},
                {wrapDateLine : true, isBaseLayer : true});
        this.map.addLayer(baseLayer);

        this.vectorLayer = new OpenLayers.Layer.Vector("Vectors", {});
        this.markerLayer = new OpenLayers.Layer.Markers("Markers", {});
        this.map.addLayer(this.vectorLayer);
        this.map.addLayer(this.markerLayer);

        this.map.zoomTo(4);
        this.map.panTo(new OpenLayers.LonLat(133.3, -26));

        this.highlightPrimitiveManager = this.makePrimitiveManager();
        this.rendered = true;

        //Control for handling click events at an X/Y location
        var clickControl = new portal.map.openlayers.ClickControl({trigger : this._onClick});
        this.map.addControl(clickControl);
        clickControl.activate();

        //Control for handling click events on vectors
        /*this.selectControl = new OpenLayers.Control.SelectFeature(
            [this.vectorLayer],
            {
                clickout: true,
                toggle: false,
                box : false,
                multiple: true,
                hover: false,
                toggleKey: "ctrlKey", // ctrl key removes from selection
                multipleKey: "shiftKey", // shift key adds to selection
                box: true,
                onSelect : function() {
                    console.log('on select: ', arguments);
                }
        });
        this.map.addControl(this.selectControl);
        this.selectControl.activate();*/
    },

    /**
     * Returns the currently visible map bounds as a portal.util.BBox object.
     *
     * function()
     */
    getVisibleMapBounds : function() {
        var bounds = this.map.getExtent().toArray();

        return Ext.create('portal.util.BBox', {
            westBoundLongitude : bounds[0],
            southBoundLatitude : bounds[1],
            eastBoundLongitude : bounds[2],
            northBoundLatitude : bounds[3]
        });
    },

    /**
     * Creates a new empty instance of the portal.map.PrimitiveManager class for use
     * with this map
     *
     * function()
     */
    makePrimitiveManager : function() {
        return Ext.create('portal.map.openlayers.PrimitiveManager', {
            baseMap : this,
            vectorLayer : this.vectorLayer,
            markerLayer : this.markerLayer
        });
    },

    /**
     * Opens an info window at a location with the specified content. When the window loads initFunction will be called
     *
     * function(windowLocation, width, height, content, initFunction)
     *
     * width - Number - width of the info window in pixels
     * height - Number - height of the info window in pixels
     * windowLocation - portal.map.Point - where the window will be opened from
     * content - Mixed - A HTML string representing the content of the window OR a Ext.container.Container object OR an Array of the previous types
     * initFunction - [Optional] function(portal.map.BaseMap map, Mixed content) a function that will be called when the info window actually opens
     */
    openInfoWindow : Ext.util.UnimplementedFunction,

    /**
     * Causes the map to scroll/zoom so that the specified bounding box is visible
     *
     * function(bbox)
     *
     * @param bbox an instance of portal.util.BBox
     */
    scrollToBounds : function(bbox) {
        var bounds = new OpenLayers.Bounds(bbox.westBoundLongitude, bbox.southBoundLatitude, bbox.eastBoundLongitude, bbox.northBoundLatitude);
        this.map.zoomToExtent(bounds);
    },

    /**
     * Gets the numerical zoom level of the current map as a Number
     *
     * function()
     */
    getZoom : function() {
        return this.map.getZoom();
    },

    /**
     * Sets the numerical zoom level of the current map
     *
     * function(zoom)
     *
     * @param zoom Number based zoom level
     */
    setZoom : function(zoom) {
        this.map.zoomTo(zoom);
    },

    /**
     * Pans the map until the specified point is in the center
     *
     * function(point)
     *
     * @param point portal.map.Point to be centered on
     */
    setCenter : function(point) {
        this.map.panTo(new OpenLayers.LonLat(point.getLongitude(), point.getLatitude()));
    },

    /**
     * Gets the location of the center point on the map as a portal.map.Point
     *
     * function()
     */
    getCenter : function() {
        var center = this.map.getCenter();

        return Ext.create('portal.map.Point', {
            longitude : center.lon,
            latitude : center.lat
        });
    },

    /**
     * Gets a portal.map.TileInformation describing a specified spatial point
     *
     * function(point)
     *
     * @param point portal.map.Point to get tile information
     */
    getTileInformationForPoint : Ext.util.UnimplementedFunction,

    /**
     * Returns an portal.map.Size object representing the map size in pixels in the form
     *
     * function()
     */
    getMapSizeInPixels : Ext.util.UnimplementedFunction,

    /**
     * Converts a latitude/longitude into a pixel coordinate based on the
     * on the current viewport
     *
     * returns an object in the form
     * {
     *  x : number - offset in x direction
     *  y : number - offset in y direction
     * }
     *
     * function(point)
     *
     * @param point portal.map.Point location to query
     */
    getPixelFromLatLng : Ext.util.UnimplementedFunction,

    ////////////////// Base functionality

    /**
     * Opens a context menu on the map at the specified coordinates
     *
     * @param point portal.map.Point location to open menu
     * @param menu Ext.menu.Menu that will be shown
     */
    showContextMenuAtLatLng : function(point, menu) {
        var pixel = this.getPixelFromLatLng(point);
        menu.showAt(this.container.x + pixel.x, this.container.y + pixel.y);
    },

    /**
     * Figure out whether we should automatically render this layer or not
     */
    _onLayerStoreAdd : function(store, layers) {
        for (var i = 0; i < layers.length; i++) {
            var newLayer = layers[i];

            //Some layer types should be rendered immediately, others will require the 'Apply Filter' button
            //We trigger the rendering by forcing a write to the filterer object
            if (newLayer.get('deserialized')) {
                //Deserialized layers (read from permalink) will have their
                //filterer already fully configured.
                var filterer = newLayer.get('filterer');
                filterer.setParameters({}); //Trigger an update without chang
            } else if (newLayer.get('renderOnAdd')) {
                //Otherwise we will need to append the filterer with the current visible bounds
                var filterForm = newLayer.get('filterForm');
                var filterer = newLayer.get('filterer');

                //Update the filter with the current map bounds
                filterer.setSpatialParam(this.getVisibleMapBounds(), true);

                filterForm.writeToFilterer(filterer);
            }
        }
    },

    /**
     * Remove any rendered data from the map
     */
    _onLayerStoreRemove : function(store, layer) {
        var renderer = layer.get('renderer');
        if (renderer) {
            renderer.abortDisplay();
            renderer.removeData();
        }
    }
});