/**
 * Utility class for encapsulating the required
 * interface for the portal to interact with the
 * Google Map API v2
 */
Ext.define('portal.map.gmap.GoogleMap', {

    extend : 'portal.map.BaseMap',

    /**
     * See parent definition
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    /**
     * See parent class for information
     */
    makeMarker : function(id, tooltip, sourceCswRecord, sourceOnlineResource, sourceLayer, point, icon) {
        return Ext.create('portal.map.gmap.primitives.Marker', {
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
    makePolygon : function(id, cswRecord, sourceOnlineResource, sourceLayer, points, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity) {
        return Ext.create('portal.map.gmap.primitives.Polygon', {
            id : id,
            layer : sourceLayer,
            onlineResource : sourceOnlineResource,
            cswRecord : cswRecord,
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
    makePolyline : function(id, cswRecord, sourceOnlineResource, sourceLayer, points, strokeColor, strokeWeight, strokeOpacity) {
        return Ext.create('portal.map.gmap.primitives.Polyline', {
            id : id,
            layer : sourceLayer,
            onlineResource : sourceOnlineResource,
            cswRecord : cswRecord,
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
        return Ext.create('portal.map.gmap.primitives.WMSOverlay', {
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

        this.highlightPrimitiveManager = this.makePrimitiveManager();

        //Add data selection box (if required)
        if (this.allowDataSelection) {
            this.map.addControl(new GmapSubsetControl(Ext.bind(function(nw, ne, se, sw) {
                var bbox = Ext.create('portal.util.BBox', {
                  northBoundLatitude : nw.lat(),
                      southBoundLatitude : sw.lat(),
                      eastBoundLongitude : ne.lng(),
                      westBoundLongitude : sw.lng()
                });

                //Iterate all active layers looking for data sources (csw records) that intersect the selection
                var intersectedRecords = this.getLayersInBBox(bbox);

                this.fireEvent('dataSelect', this, bbox, intersectedRecords);
              }, this)), new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(405, 7)));
        }

        this.rendered = true;
    },

    /**
     * See parent for definition.
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
     * See parent for definition.
     */
    makePrimitiveManager : function() {
        return Ext.create('portal.map.gmap.PrimitiveManager', {
            baseMap : this
        });
    },

    /**
     * See Parent class.
     */
    //VT: initFunction doesn't seem to be in use.
    //openInfoWindow : function(windowLocation, width, height, content, initFunction,layer) {
    openInfoWindow : function(windowLocation, width, height, content,layer) {
        if (!Ext.isArray(content)) {
            content = [content];
        }

        //We need to open an info window with a number of tabs for each of the content
        //Each tab will need to have an appropriately sized parent container rendered into it
        //AND once they are all rendered, we need to then add each element of baseComponents
        //to each of the tabs

        //Build our info window content (sans parent containers)
        var infoWindowIds = []; //this holds the unique ID's to bind to
        var infoWindowTabs = []; //this holds GInfoWindowTab instances
        for (var i = 0; i < content.length; i++) {
            var html = null;
            var title = 'HTML';

            infoWindowIds.push(Ext.id());
            if (Ext.isString(content[i])) {
                html = content[i];
            } else {
                title = content[i].tabTitle;
                html = Ext.util.Format.format('<html><body><div id="{0}" style="width: {1}px; height: {2}px;"></div></body></html>', infoWindowIds[i], width, height);
            }
            infoWindowTabs.push(new GInfoWindowTab(title, html));
        }
        var initFunctionParams = { //this will be passed to the info window manager callback
            width : width,
            height : height,
            infoWindowIds : infoWindowIds,
            content : content
        };
        var infoWindowParams = undefined; //we don't dictate any extra info window options

        //Show our info window - create our parent components
        this._openInfoWindowTabs(windowLocation, infoWindowTabs, infoWindowParams, initFunctionParams, function(map, location, params) {
            for (var i = 0; i < params.content.length; i++) {
                if (Ext.isString(params.content[i])) {
                    continue; //HTML tabs need no special treatment
                }

                Ext.create('Ext.container.Container', {
                    renderTo : params.infoWindowIds[i],
                    border : 0,
                    width : params.width,
                    height : params.height,
                    layout : 'fit',
                    items : [params.content[i]],
                    listeners : {
                        //To workaround some display issues with ext JS under Google maps
                        //We need to force a layout of the ExtJS container when the GMap tab
                        //changes. GMap doesn't offer anyway of doing that so we instead monitor
                        //the underlying DOM for style changes referencing the 'display' CSS attribute.
                        //See: http://www.sencha.com/forum/showthread.php?186027-Ext-4.1-beta-3-Strange-layout-on-grids-rendered-into-elements-with-display-none&p=752916#post752916
                        afterrender : function(container) {
                            //Find the parent info window DOM
                            var el = container.getEl();
                            var tabParentDiv = el.findParentNode('div.gmnoprint', 10, true);
                            var headerParentDiv = tabParentDiv.findParentNode('div.gmnoprint', 10, true);

                            //Firstly get all child div's (these are our tabs).
                            //This tells us how many headers there should be (one for each tab)
                            var tabElements = tabParentDiv.select('> div');
                            var tabElementsArr = [];
                            tabElements.each(function(div) {
                                tabElementsArr.push(div.dom);   //don't store a reference to div, it's the Ext.flyWeight el. Use div.dom
                            });

                            //Now there are a lot of divs under the header parent, we are interested
                            //in the last N (which represent the N headers of the above tabs)
                            var allParentDivs = headerParentDiv.select('> div');
                            var allParentDivsArr = [];
                            allParentDivs.each(function(div) {
                                allParentDivsArr.push(div.dom); //don't store a reference to div, it's the Ext.flyWeight el. Use div.dom
                            });
                            var headerDivsArr = allParentDivsArr.slice(allParentDivsArr.length - tabElementsArr.length);

                            //Start iterating from the second index - the first tab will never need a forced layout
                            for (var i = 1; i < headerDivsArr.length; i++) {
                                var headerDiv = new Ext.Element(headerDivsArr[i]);
                                var tabDiv = new Ext.Element(tabElementsArr[i]);

                                headerDiv.on('click', Ext.bind(function(e, t, eOpts, headerElement, tabElement) {
                                    //Find the container which belongs to t
                                    for (var i = 0; i < params.content.length; i++) {
                                        var container = params.content[i];
                                        var containerElId = container.getEl().id;

                                        //Only layout the child of the element firing the event (i.e. the tab
                                        //which is visible)
                                        var matchingElements = tabElement.select(Ext.util.Format.format(':has(#{0})', containerElId));
                                        if (matchingElements.getCount() > 0) {
                                            //Only perform the layout once for performance reasons
                                            if (!container._portalTabLayout) {
                                                container._portalTabLayout = true;
                                                container.doLayout();
                                            }
                                        }
                                    }
                                }, this, [headerDiv, tabDiv], true));
                            }
                        }
                    }
                });
            }
        });
        this.openedInfoLayerId=layer.get('id');
    },

    /**
     * See parent class
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
     * See parent class
     */
    getZoom : function() {
        return this.map.getZoom();
    },

    /**
     * See parent class
     */
    setZoom : function(zoom) {
        return this.map.setZoom(zoom);
    },

    /**
     * See parent class.
     */
    setCenter : function(point) {
        this.map.panTo(new GLatLng(point.getLatitude(), point.getLongitude()));
    },

    /**
     * See parent class.
     */
    getCenter : function() {
        var c = this.map.getCenter();
        return Ext.create('portal.map.Point', {
            latitude : c.lat(),
            longitude : c.lng()
        });
    },

    /**
     * See parent class.
     */
    getTileInformationForPoint : function(point) {
        var latitude = point.getLatitude();
        var longitude = point.getLongitude();
        var info = Ext.create('portal.map.TileInformation', {});

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
     * Returns an portal.map.Size object representing the map size in pixels in the form
     *
     * function()
     */
    getMapSizeInPixels : function() {
        var size = this.map.getSize();
        return Ext.create('portal.map.Size', {
            width : size.width,
            height : size.height
        });
    },

    /**
     * See parent
     */
    getPixelFromLatLng : function(point) {
        var latLng = new GLatLng(point.getLatitude(), point.getLongitude());
        return this.map.fromLatLngToContainerPixel(latLng);
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
        var queryTargets = portal.map.gmap.ClickController.generateQueryTargets(overlay, latlng, overlayLatlng, this.layerStore);
        this.fireEvent('query', this, queryTargets);
    },

    /**
     * If the removal of a layer has the same ID has a info window opened, close it.
     */
    closeInfoWindow: function(layerid){
        if(layerid === this.openedInfoLayerId){
          this.map.closeInfoWindow();
        }
    },


    /**
     * Opens an info window at a location with the specified content. When the window loads initFunction will be called
     *
     * windowLocation - either a portal.map.gmap.primitives.Marker or a portal.map.Point which is where the window will be opened from
     * content - A HTML string representing the content of the window OR a array of GInfoWindowTab
     * infoWindowOpts - [Optional] an instance of GInfoWindowOptions that will be passed to the new window
     * initFunction - [Optional] function(map, location, initFunctionParam) a function that will be called with initFunctionParam when the window opens
     * initFunctionParam - [Optional] will be passed as a parameter to initFunction
     */
    _openInfoWindowTabs : function(windowLocation, content, infoWindowOpts, initFunctionParam, initFunction) {
        //We listen for the open event once
        var scope = this;
        var listenerHandler = null;
        var listenerFunction = function() {
            GEvent.removeListener(listenerHandler);

            if (initFunction) {
                initFunction(scope, content, initFunctionParam);
            }
        };

        //Figure out which function to call based upon our parameters
        if (windowLocation instanceof portal.map.Point) {
            var latLng = new GLatLng(windowLocation.getLatitude(), windowLocation.getLongitude());
            listenerHandler = GEvent.addListener(this.map, "infowindowopen", listenerFunction);

            if (content instanceof Array) {
                this.map.openInfoWindowTabs(latLng, content, infoWindowOpts);
            } else if (typeof(content) === "string") {
                this.map.openInfoWindowHtml(latLng, content, infoWindowOpts);
            }
        } else if (windowLocation instanceof portal.map.gmap.primitives.Marker) {
            var gMarker = windowLocation.getMarker();
            listenerHandler = GEvent.addListener(gMarker, "infowindowopen", listenerFunction);

            if (content instanceof Array) {
                gMarker.openInfoWindowTabs(content, infoWindowOpts);
            } else if (typeof(content) === "string") {
                gMarker.openInfoWindowHtml(content, infoWindowOpts);
            }
        }
    }
});