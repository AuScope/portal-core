/**
 * Utility class for encapsulating the required
 * interface for the portal to interact with the
 * Google Map API v2
 */
Ext.define('portal.util.gmap.GMapWrapper', {
    extend : 'Ext.util.Observable',

    map : null, //an instance of a GMap2 (google map api).
    container : null, //the container that houses map,
    layerStore : null, //An instance of portal.layer.LayerStore,
    rendered : false, //whether this map wrapper has been rendered to a container
    highlightOverlayManager : null,

    statics : {
        /**
         * Given a GOverlay created by this class, return the representative gml:id of the feature (can be null/empty)
         */
        getOverlayId : function(overlay) {
            return overlay._id;
        },

        /**
         * Given a GOverlay created by this class, return the portal.layer.Layer used in it's creation
         */
        getOverlayLayer : function(overlay) {
            return overlay._layer;
        },

        /**
         * Given a GOverlay created by this class, return the portal.csw.OnlineResource used in it's creation
         */
        getOverlayOnlineResource : function(overlay) {
            return overlay._onlineResource;
        },

        /**
         * Given a GOverlay created by this class, return the portal.csw.OnlineResource used in it's creation
         */
        getOverlayCSWRecord : function(overlay) {
            return overlay._cswRecord;
        },

        /**
         * Utility function for creating an instance of the google map GMarker class
         *
         * @param id [Optional] A string based ID that will be used as the gml:id for this marker
         * @param tooltip The text to display when this marker is moused over
         * @param sourceOnlineResource portal.csw.OnlineResource representing where this marker was generated from
         * @param sourceLayer portal.layer.Layer representing the owner of sourceOnlineResource
         * @param point a GLatLng (gmap api) indicating where this marker should be shown
         * @param icon a GIcon (gmap api) containing display information about how the marker should look
         */
        makeMarker : function(id, tooltip, cswRecord, sourceOnlineResource, sourceLayer, point, icon) {
            var marker = new GMarker(point, {icon: icon, title: tooltip});

            //Overload marker with useful info
            marker._id = id;
            marker._layer = sourceLayer;
            marker._onlineResource = sourceOnlineResource;
            marker._cswRecord=cswRecord;

            return marker;
        },

        /**
         * Utility function for creating an instance of the google map GPolygon class
         * @param id A string based ID that will be used as the gml:id for this polygon
         * @param sourceOnlineResource portal.csw.OnlineResource representing where this polygon was generated from
         * @param sourceLayer portal.layer.Layer representing the owner of sourceOnlineResource
         * @param points an array GLatLng (gmap api) objects indicating the bounds of this polygon
         * @param strokeColor [Optional] HTML Style color string '#RRGGBB' for the vertices of the polygon
         * @param strokeWeight [Optional] Width of the stroke in pixels
         * @param strokeOpacity [Optional] A number from 0-1 indicating the opacity of the vertices
         * @param fillColor [Optional] HTML Style color string '#RRGGBB' for the filling of the polygon
         * @param fillOpacity [Optional] A number from 0-1 indicating the opacity of the fill
         *
         */
        makePolygon : function(id, cswRecord, sourceOnlineResource, sourceLayer, points,
                strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity) {
            var polygon = new GPolygon(points,strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity);

            polygon._id = id;
            polygon._layer = sourceLayer;
            polygon._onlineResource = sourceOnlineResource;
            polygon._cswRecord=cswRecord;

            return polygon;
        },

        /**
         * Utility function for creating an instance of the google map GPolyLine class
         * @param id A string based ID that will be used as the gml:id for this line
         * @param sourceOnlineResource portal.csw.OnlineResource representing where this line was generated from
         * @param sourceLayer portal.layer.Layer representing the owner of sourceOnlineResource
         * @param points an array GLatLng (gmap api) objects indicating the line location
         * @param color [Optional] HTML Style color string '#RRGGBB' for the line
         * @param weight [Optional] Width of the stroke in pixels
         * @param opacity [Optional] A number from 0-1 indicating the opacity of the line
         */
        makePolyLine : function(id, cswRecord,sourceOnlineResource, sourceLayer, points, color, weight, opacity) {
            var line = new GPolyline(points, color, weight, opacity);

            line._id = id;
            line._layer = sourceLayer;
            line._onlineResource = sourceOnlineResource;
            line._cswRecord=cswRecord;

            return line;
        }
    },


    /**
     * Accepts a config in the form {
     *  container - [Optional] Ext.util.Container that will house the google map instance. If omitted then a call to renderToContainer must be made before this wrapper can be used
     *  layerStore - An instance of portal.layer.LayerStore
     * }
     *
     * Adds the following events
     *
     * query : function(portal.util.gmap.GMapWrapper this, portal.layer.querier.QueryTarget[] queryTargets)
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
    },

    /**
     * Update the Lat/Long text in the HTML
     */
    _onMouseMove : function(latlng) {
        var latStr = "<b>Long:</b> " + latlng.lng().toFixed(6) +
                     "&nbsp&nbsp&nbsp&nbsp" +
                     "<b>Lat:</b> " + latlng.lat().toFixed(6);
        document.getElementById("latlng").innerHTML = latStr;
    },

    /**
     * Remove the lat/long text from the HTML
     */
    _onMouseOut : function(latlng) {
        document.getElementById("latlng").innerHTML = "";
    },

    /**
     * Ensure our map fills the parent container at all times
     */
    _onContainerResize : function() {
        this.map.checkResize();
    },

    /**
     * When the map is clicked, convert that into a click event the portal can understand and offload it
     */
    _onClick : function(overlay, latlng, overlayLatlng) {
        var queryTargets = portal.util.gmap.ClickController.generateQueryTargets(overlay, latlng, overlayLatlng, this.layerStore);
        this.fireEvent('query', this, queryTargets);
    },

    /**
     * Renders this map to the specified container.
     */
    renderToContainer : function(container) {
        //Is user's browser suppported by Google Maps?
        if (!GBrowserIsCompatible()) {
            alert('Your browser isn\'t compatible with the Google Map API V2. This portal will not be functional as a result.');
            throw 'IncompatibleBrowser';
        }

        this.container = container;
        this.map = new GMap2(this.container.body.dom);

        /* AUS-1526 search bar. */
        this.map.enableGoogleBar();
        /*
        // Problems, find out how to
        1. turn out advertising
        2. Narrow down location seraches to the current map view
                        (or Australia). Search for Albany retruns Albany, US
        */
        this.map.setUIToDefault();

        //add google earth
        this.map.addMapType(G_SATELLITE_3D_MAP);

        // Toggle between Map, Satellite, and Hybrid types
        this.map.addControl(new GMapTypeControl());

        var startZoom = 4;
        this.map.setCenter(new google.maps.LatLng(-26, 133.3), startZoom);
        this.map.setMapType(G_SATELLITE_MAP);

        //Thumbnail map
        var Tsize = new GSize(150, 150);
        this.map.addControl(new GOverviewMapControl(Tsize));

        this.map.addControl(new DragZoomControl(), new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(345, 7)));

        // Fix for IE/Firefox resize problem (See issue AUS-1364 and AUS-1565 for more info)
        this.map.checkResize();
        container.on('resize', this._onContainerResize, this);

        GEvent.addListener(this.map, "mousemove", Ext.bind(this._onMouseMove, this));
        GEvent.addListener(this.map, "mouseout", Ext.bind(this._onMouseOut, this));
        GEvent.addListener(this.map, "click", Ext.bind(this._onClick, this));

        this.highlightOverlayManager = this.createOverlayManager();

        this.rendered = true;
    },

    /**
     * Returns the currently visible map bounds as a portal.util.BBox object.
     */
    getVisibleMapBounds : function() {
        var mapBounds = this.map.getBounds();
        var sw = mapBounds.getSouthWest();
        var ne = mapBounds.getNorthEast();

        return Ext.create('portal.util.BBox', {
            eastBoundLongitude : ne.lng(),
            westBoundLongitude : sw.lng(),
            southBoundLatitude : sw.lat(),
            northBoundLatitude : ne.lat()
        });
    },

    /**
     * Creates a new empty instance of the portal.util.gmap.OverlayManager class
     */
    createOverlayManager : function() {
        return Ext.create('portal.util.gmap.OverlayManager', {map : this.map});
    },

    /**
     * Opens an info window at a location with the specified content. When the window loads initFunction will be called
     *
     * windowLocation - either a GMarker or a GLatLng which is where the window will be opened from
     * content - A HTML string representing the content of the window OR a array of GInfoWindowTab
     * infoWindowOpts - [Optional] an instance of GInfoWindowOptions that will be passed to the new window
     * initFunction - [Optional] function(map, location, initFunctionParam) a function that will be called with initFunctionParam when the window opens
     * initFunctionParam - [Optional] will be passed as a parameter to initFunction
     */
    openInfoWindow : function(windowLocation, content, infoWindowOpts, initFunctionParam, initFunction) {
        //We listen for the open event once
        var scope = this;
        var listenerHandler = null;
        var listenerFunction = function() {
            GEvent.removeListener(listenerHandler);

            if (initFunction) {
                initFunction(scope.map, windowLocation, initFunctionParam);
            }
        };

        //Figure out which function to call based upon our parameters
        if (windowLocation instanceof GLatLng) {
            listenerHandler = GEvent.addListener(this.map, "infowindowopen", listenerFunction);

            if (content instanceof Array) {
                this.map.openInfoWindowTabs(windowLocation, content, infoWindowOpts);
            } else if (typeof(content) === "string") {
                this.map.openInfoWindowHtml(windowLocation, content, infoWindowOpts);
            }
        } else if (windowLocation instanceof GMarker) {
            listenerHandler = GEvent.addListener(windowLocation, "infowindowopen", listenerFunction);

            if (content instanceof Array) {
                windowLocation.openInfoWindowTabs(content, infoWindowOpts);
            } else if (typeof(content) === "string") {
                windowLocation.openInfoWindowHtml(content, infoWindowOpts);
            }
        }
    },

    /**
     * Causes the map to scroll/zoom so that the specified bounding box is visible
     * @param bbox an instance of portal.util.BBox
     */
    scrollToBounds : function(bbox) {
        var sw = new GLatLng(bbox.southBoundLatitude, bbox.westBoundLongitude);
        var ne = new GLatLng(bbox.northBoundLatitude, bbox.eastBoundLongitude);
        var layerBounds = new GLatLngBounds(sw,ne);

        //Adjust zoom if required
        var visibleBounds = this.map.getBounds();
        this.map.setZoom(this.map.getBoundsZoomLevel(layerBounds));

        //Pan to position
        var layerCenter = layerBounds.getCenter();
        this.map.panTo(layerCenter);
    },

    /**
     * Causes the map to highlight the specified bounding box by drawing an overlay
     * over it. The highlight will disappear after a short period of time
     *
     * @param bboxes an instance of portal.util.BBox or an array of portal.util.BBox objects
     * @param delay [Optional] a delay in ms before the highlight is hidden. Defaults to 2000
     */
    highlightBounds : function(bboxes, delay) {
        //Setup our inputs
        delay = delay ? delay : 2000;
        if (!Ext.isArray(bboxes)) {
            bboxes = [bboxes];
        }

        for (var i = 0; i < bboxes.length; i++) {
            var polygonList = bboxes[i].toGMapPolygon('00FF00', 0, 0.7,'#00FF00', 0.6);
            for (var j = 0; j < polygonList.length; j++) {
                polygonList[j].title = 'bbox';
                this.highlightOverlayManager.addOverlay(polygonList[j]);
            }
        }

        //Make the bbox disappear after a short while
        var clearTask = new Ext.util.DelayedTask(Ext.bind(function(){
            this.highlightOverlayManager.clearOverlays();
        }, this));

        clearTask.delay(delay);
    },

    //delegator function to the original map function
    getCurrentMapType : function(){
        return this.map.getCurrentMapType();
    },

    //delegator function to the original map function
    getZoom : function() {
        return this.map.getZoom();
    },

    //delegator function to the original map function
    setZoom : function(zoom) {
        return this.map.setZoom(zoom);
    },

    /**
     * Pans the map until the specified point is in the center
     */
    setCenter : function(latitude, longitude) {
        this.map.panTo(new GLatLng(latitude, longitude));
    },

    /**
     * Function for returning the center of the map as an Object
     * {
     *  latitude : Number,
     *  longitude : Number
     * }
     */
    getCenter : function() {
        var c = this.map.getCenter();
        return {
            latitude : c.lat(),
            longitude : c.lng()
        };
    },

    /**
     * Returns an object representing the map size in pixels in the form
     * {
     *  width : Number
     *  height : Number
     * }
     */
    getMapSizeInPixels : function() {
        return this.map.getSize();
    },

    /**
     * Returns a portal.util.gmap.TileInformation representing information about a particular
     * point on the map (based on the current map port)
     *
     * @param latitude Number
     * @param longitude Number
     */
    getTileInformation : function(latitude, longitude) {
        var info = Ext.create('portal.util.gmap.TileInformation');

        var zoom = this.map.getZoom();
        var mapType = this.map.getCurrentMapType();

        var tileSize = mapType.getTileSize();
        info.setWidth(tileSize);
        info.setHeight(tileSize);

        var currentProj = mapType.getProjection();
        var point = currentProj.fromLatLngToPixel(new GLatLng(latitude, longitude),zoom);
        var tile = {
            x : Math.floor(point.x / tileSize),
            y : Math.floor(point.y / tileSize)
        };

        var sw = currentProj.fromPixelToLatLng(new GPoint(tile.x*tileSize, (tile.y+1)*tileSize), zoom);
        var ne = currentProj.fromPixelToLatLng(new GPoint((tile.x+1)*tileSize, tile.y*tileSize), zoom);

        info.setTileBounds(Ext.create('portal.util.BBox', {
            northBoundLatitude : ne.lat(),
            southBoundLatitude : sw.lat(),
            eastBoundLongitude : ne.lng(),
            westBoundLongitude : sw.lng()
        }));

        info.setOffset({
            x : point.x % tileSize,
            y : point.y % tileSize
        });

        return info;
    },

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
     * @param latitude Number
     * @param longitude Number
     */
    getPixelFromLatLng : function(latitude, longitude) {
        var latLng = new GLatLng(latitude, longitude);
        return this.map.fromLatLngToContainerPixel(latLng);
    },

    /**
     * Opens a context menu on the map at the specified coordinates
     *
     * @param latitude Number
     * @param longitude Number
     * @param menu Ext.menu.Menu that will be shown
     */
    showContextMenuAtLatLng : function(latitude, longitude, menu) {
        var pixel = this.getPixelFromLatLng(latitude, longitude);
        menu.showAt(this.container.x + pixel.x, this.container.y + pixel.y);
    }
});