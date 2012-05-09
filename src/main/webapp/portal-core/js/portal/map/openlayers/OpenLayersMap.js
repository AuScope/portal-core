/**
 * Concrete implementation for OpenLayers
 */
Ext.define('portal.map.openlayers.OpenLayersMap', {
    extend : 'portal.map.BaseMap',

    map : null, //Instance of OpenLayers.Map
    vectorLayer : null,
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

    _makeQueryTargetsPolygon : function(polygon, layerStore, longitude, latitude) {
        var queryTargets = [];
        var lonLat = new OpenLayers.LonLat(longitude, latitude);

        //Iterate all features on the map, those that intersect the given lat/lon should
        //have query targets generated for them as it isn't clear which one the user meant
        //to click
        for (var i = 0; i < this.vectorLayer.features.length; i++) {
            var featureToTest = this.vectorLayer.features[i];
            if (featureToTest.geometry.atPoint(lonLat)) {
                var primitiveToTest = featureToTest.attributes['portalBasePrimitive'];
                if (primitiveToTest) {
                    var id = primitiveToTest.getId();
                    var onlineResource = primitiveToTest.getOnlineResource();
                    var layer = primitiveToTest.getLayer();
                    var cswRecord = primitiveToTest.getCswRecord();

                    queryTargets.push(Ext.create('portal.layer.querier.QueryTarget', {
                        id : id,
                        lat : latitude,
                        lng : longitude,
                        onlineResource : onlineResource,
                        layer : layer,
                        cswRecord : cswRecord,
                        explicit : true
                    }));
                }
            }
        }

        return queryTargets;
    },

    _makeQueryTargetsVector : function(primitive, longitude, latitude) {
        var id = primitive.getId();
        var onlineResource = primitive.getOnlineResource();
        var layer = primitive.getLayer();
        var cswRecord = primitive.getCswRecord();

        return [Ext.create('portal.layer.querier.QueryTarget', {
            id : id,
            lat : latitude,
            lng : longitude,
            onlineResource : onlineResource,
            layer : layer,
            cswRecord : cswRecord,
            explicit : true
        })];
    },

    _makeQueryTargetsMap : function(layerStore, longitude, latitude) {
        var queryTargets = [];
        //Iterate everything with WMS/WCS - no way around this :(
        for (var i = 0; i < layerStore.getCount(); i++) {
            var layer = layerStore.getAt(i);

            var cswRecords = layer.get('cswRecords');
            for(var j = 0; j < cswRecords.length; j++){
                var cswRecord = cswRecords[j];

                //ensure this click lies within this CSW record
                var containsPoint = false;
                var geoEls = cswRecord.get('geographicElements');
                for (var k = 0; k < geoEls.length; k++) {
                    if (geoEls[k] instanceof portal.util.BBox &&
                        geoEls[k].contains(latitude, longitude)) {
                        containsPoint = true;
                        break;
                    }
                }

                //If it doesn't, don't consider this point for examination
                if (!containsPoint) {
                    continue;
                }

                //Finally we don't include WMS query targets if we
                //have WCS queries for the same record
                var allResources = cswRecord.get('onlineResources');
                var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(allResources, portal.csw.OnlineResource.WMS);
                var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(allResources, portal.csw.OnlineResource.WCS);
                var resourcesToIterate = [];
                if (wcsResources.length > 0) {
                    resourcesToIterate = wcsResources;
                } else {
                    resourcesToIterate = wmsResources;
                }

                //Generate our query targets for WMS/WCS layers
                for (var k = 0; k < resourcesToIterate.length; k++) {
                    var type = resourcesToIterate[k].get('type');
                    if (type === portal.csw.OnlineResource.WMS ||
                        type === portal.csw.OnlineResource.WCS) {
                        queryTargets.push(Ext.create('portal.layer.querier.QueryTarget', {
                            id : '',
                            lat : latitude,
                            lng : longitude,
                            cswRecord   : cswRecord,
                            onlineResource : resourcesToIterate[k],
                            layer : layer,
                            explicit : true
                        }));
                    }
                }
            }
        }

        return queryTargets;
    },

    /**
     * Handler for click events
     *
     * @param vector [Optional] OpenLayers.Feature.Vector the clicked feature (if any)
     * @param e Event The click event that caused this handler to fire
     */
    _onClick : function(vector, e) {
        var primitive = vector ? vector.attributes['portalBasePrimitive'] : null;
        var lonlat = this.map.getLonLatFromViewPortPx(e.xy);
        var longitude = lonlat.lon;
        var latitude = lonlat.lat;
        console.log("You clicked primitive: ", primitive, " near " + lonlat.lat + " N, " +
                                  + lonlat.lon + " E and the arguments are: ", arguments);

        var queryTargets = [];
        if (primitive && primitive instanceof portal.map.openlayers.primitives.Polygon) {
            queryTargets = this._makeQueryTargetsPolygon(primitive, this.layerStore, longitude, latitude);
        } else if (primitive) {
            queryTargets = this._makeQueryTargetsVector(primitive, longitude, latitude);
        } else {
            queryTargets = this._makeQueryTargetsMap(this.layerStore, longitude, latitude);
        }

        this.fireEvent('query', this, queryTargets);
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
        this.map.addLayer(this.vectorLayer);

        this.map.zoomTo(4);
        this.map.panTo(new OpenLayers.LonLat(133.3, -26));

        this.highlightPrimitiveManager = this.makePrimitiveManager();
        this.container = container;
        this.rendered = true;

        //Control for handling click events on the map
        var clickableLayers = [this.vectorLayer];
        var clickControl = new portal.map.openlayers.ClickControl(clickableLayers, {
            map : this.map,
            trigger : Ext.bind(this._onClick, this)
        });

        this.map.addControl(clickControl);
        clickControl.activate();
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
            vectorLayer : this.vectorLayer
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
    openInfoWindow : function(windowLocation, width, height, content, initFunction) {
        //Firstly create a popup with a chunk of placeholder HTML - we will render an ExtJS container inside that
        var popupId = Ext.id();
        var location = new OpenLayers.LonLat(windowLocation.getLongitude(), windowLocation.getLatitude());
        var size = new OpenLayers.Size(width, height);
        var divId = Ext.id();
        var divHtml = Ext.util.Format.format('<html><body><div id="{0}" style="width: {1}px; height: {2}px;"></div></body></html>', divId, width, height);
        var popup = new OpenLayers.Popup.Anchored(popupId, location, size, divHtml, null, true, null);
        this.map.addPopup(popup, true);

        //next create an Ext.Container to house our content, render it to the HTML created above
        if (!Ext.isArray(content)) {
            content = [content];
        }

        var tabPanelItems = [];
        for (var i = 0; i < content.length; i++) {
            if (Ext.isString(content[i])) {
                tabPanelItems.push({
                    title : '',
                    html : content[i]
                });
            } else {
                tabPanelItems.push({
                    title : content[i].tabTitle,
                    items : [content[i]]
                });
            }
        }

        Ext.create('Ext.tab.Panel', {
            width : width,
            height : height,
            renderTo : divId,
            activeTab: 0,
            items : tabPanelItems
        });
    },

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
    getTileInformationForPoint : portal.util.UnimplementedFunction,

    /**
     * Returns an portal.map.Size object representing the map size in pixels in the form
     *
     * function()
     */
    getMapSizeInPixels : function() {
        var size = this.map.getCurrentSize();
        return Ext.create('portal.map.Size', {
            width : size.w,
            height : size.h
        });
    },

    /**
     * See parent class for information
     */
    getPixelFromLatLng : function(point) {
        var layerPixel = this.map.getLayerPxFromLonLat(new OpenLayers.LonLat(point.getLongitude(), point.getLatitude()));
        var viewportPixel = this.map.getViewPortPxFromLayerPx(layerPixel);

        return {
            x : viewportPixel.x,
            y : viewportPixel.y
        }
    },

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