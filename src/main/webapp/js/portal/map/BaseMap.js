/**
 * Abstract class for representing the core functionality that the
 * portal requires from a mapping API.
 */
Ext.define('portal.map.BaseMap', {
    extend : 'Ext.util.Observable',

    /**
     * An instance of portal.map.BasePrimitiveManager for managing any temporary highlights
     * on the map
     */
    highlightPrimitiveManager : null,

    /**
     * Boolean - Whether this map wrapper has been rendered to a container
     */
    rendered : false,

    /**
     * Accepts a config in the form {
     *  container - [Optional] Ext.util.Container that will house the google map instance. If omitted then a call to renderToContainer must be made before this wrapper can be used
     *  layerStore - An instance of portal.layer.LayerStore
     * }
     *
     * Adds the following events
     *
     * query : function(portal.map.BaseMap this, portal.layer.querier.QueryTarget[] queryTargets)
     *         Fired whenever the underlying map is clicked and the user is requesting information about one or more layers
     */
    constructor : function(cfg) {
        this.container = cfg.container;
        this.layerStore = cfg.layerStore;

        this.addEvents('query');

        this.callParent(arguments);

        if (this.container) {
            this.renderToContainer(this.container);
        }

        console.warn('Artificial delay to overcome Ext JS 4.1 rc1 issues');
        this.layerStore.on('add', this._onLayerStoreAdd, this, {
            delay : 1   //There is an issue with grids adding responding to
                        //data store change events before the constructed row is
                        //ready for use (hence this artificial delay)
        });

        this.layerStore.on('remove', this._onLayerStoreRemove, this);
    },

    /////////////// Unimplemented functions

    /**
     * Utility function for creating an instance of portal.map.primitives.Marker
     *
     * function(id, tooltip, sourceCswRecord, sourceOnlineResource, sourceLayer, point, icon)
     *
     * @param id [Optional] A string based ID that will be used as the gml:id for this marker
     * @param tooltip The text to display when this marker is moused over
     * @param sourceOnlineResource portal.csw.OnlineResource representing where this marker was generated from
     * @param sourceCswRecord portal.csw.CSWRecord representing the owner of sourceOnlineResource
     * @param sourceLayer portal.layer.Layer representing the owner of sourceOnlineResource
     * @param point a portal.map.Point indicating where this marker should be shown
     * @param icon a portal.map.Icon containing display information about how the marker should look
     */
    makeMarker : Ext.util.UnimplementedFunction,

    /**
     * Utility function for creating an instance of portal.map.primitives.Polygon
     *
     * function(id, sourceCswRecord, sourceOnlineResource, sourceLayer, points, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity)
     *
     * @param id A string based ID that will be used as the gml:id for this polygon
     * @param sourceCswRecord portal.csw.CSWRecord representing the owner of sourceOnlineResource
     * @param sourceOnlineResource portal.csw.OnlineResource representing where this polygon was generated from
     * @param sourceLayer portal.layer.Layer representing the owner of sourceOnlineResource
     * @param points an array portal.map.Point objects indicating the bounds of this polygon
     * @param strokeColor [Optional] HTML Style color string '#RRGGBB' for the vertices of the polygon
     * @param strokeWeight [Optional] Width of the stroke in pixels
     * @param strokeOpacity [Optional] A number from 0-1 indicating the opacity of the vertices
     * @param fillColor [Optional] HTML Style color string '#RRGGBB' for the filling of the polygon
     * @param fillOpacity [Optional] A number from 0-1 indicating the opacity of the fill
     *
     */
    makePolygon : Ext.util.UnimplementedFunction,

    /**
     * Utility function for creating an instance of portal.map.primitives.Polyline
     *
     * function(id, sourceCswRecord,sourceOnlineResource, sourceLayer, points, color, weight, opacity)
     *
     * @param id A string based ID that will be used as the gml:id for this line
     * @param sourceCswRecord portal.csw.CSWRecord representing the owner of sourceOnlineResource
     * @param sourceOnlineResource portal.csw.OnlineResource representing where this line was generated from
     * @param sourceLayer portal.layer.Layer representing the owner of sourceOnlineResource
     * @param points an array portal.map.Point objects indicating the bounds of this polygon
     * @param color [Optional] HTML Style color string '#RRGGBB' for the line
     * @param weight [Optional] Width of the stroke in pixels
     * @param opacity [Optional] A number from 0-1 indicating the opacity of the line
     */
    makePolyline : Ext.util.UnimplementedFunction,

    /**
     * Utility function for creating an instance of portal.map.primitives.BasWMSPrimitive
     *
     * function(id, sourceCswRecord, sourceOnlineResource, sourceLayer, wmsUrl, wmsLayer, opacity)
     *
     * @param id A string based ID that will be used as the gml:id for this line
     * @param sourceCswRecord portal.csw.CSWRecord representing the owner of sourceOnlineResource
     * @param sourceOnlineResource portal.csw.OnlineResource representing where this line was generated from
     * @param sourceLayer portal.layer.Layer representing the owner of sourceOnlineResource
     * @param wmsUrl String - URL of the WMS to query
     * @param wmsLayer String - Name of the WMS layer to query
     * @param opacity Number - opacity/transparency in the range [0, 1]
     */
    makeWms : Ext.util.UnimplementedFunction,

    /**
     * Renders this map to the specified Ext.container.Container.
     *
     * Also sets the rendered property.
     *
     * function(container)
     *
     * @param container The container to receive the map
     */
    renderToContainer : Ext.util.UnimplementedFunction,

    /**
     * Returns the currently visible map bounds as a portal.util.BBox object.
     *
     * function()
     */
    getVisibleMapBounds : Ext.util.UnimplementedFunction,

    /**
     * Creates a new empty instance of the portal.map.PrimitiveManager class for use
     * with this map
     *
     * function()
     */
    makePrimitiveManager : Ext.util.UnimplementedFunction,

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
    scrollToBounds : Ext.util.UnimplementedFunction,

    /**
     * Causes the map to highlight the specified bounding box by drawing an overlay
     * over it. The highlight will disappear after a short period of time
     *
     * function(bboxes, delay)
     *
     * @param bboxes an instance of portal.util.BBox or an array of portal.util.BBox objects
     * @param delay [Optional] a delay in ms before the highlight is hidden. Defaults to 2000
     */
    highlightBounds : Ext.util.UnimplementedFunction,

    /**
     * Gets the numerical zoom level of the current map as a Number
     *
     * function()
     */
    getZoom : Ext.util.UnimplementedFunction,

    /**
     * Sets the numerical zoom level of the current map
     *
     * function(zoom)
     *
     * @param zoom Number based zoom level
     */
    setZoom : Ext.util.UnimplementedFunction,

    /**
     * Pans the map until the specified point is in the center
     *
     * function(point)
     *
     * @param point portal.map.Point to be centered on
     */
    setCenter : Ext.util.UnimplementedFunction,

    /**
     * Gets the location of the center point on the map as a portal.map.Point
     *
     * function()
     */
    getCenter : Ext.util.UnimplementedFunction,

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