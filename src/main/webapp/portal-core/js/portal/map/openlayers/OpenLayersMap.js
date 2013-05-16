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
    makePolyline : function(id, sourceCswRecord,sourceOnlineResource, sourceLayer, points, strokeColor, strokeWeight, strokeOpacity) {
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
        lonLat=lonLat.transform('EPSG:4326','EPSG:3857');

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
                if (!containsPoint || layer.visible==false) {
                    continue;
                }

                //Finally we don't include WMS query targets if we
                //have WCS queries for the same record
                var allResources = cswRecord.get('onlineResources');
                var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(allResources, portal.csw.OnlineResource.WMS);
                var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(allResources, portal.csw.OnlineResource.WCS);

                //VT: if layerswitcher layer visibility is set to false, then do not query that layer as well.
                if(wmsResources[0]){
                    var layerSwitcherVisible=true;
                    var layerName=wmsResources[0].get('name');
                    var layerSwitcherState=this.map.controls[7].layerStates;
                    for(var z=0; z < layerSwitcherState.length; z++){
                        if(layerSwitcherState[z].name === layerName){
                            layerSwitcherVisible=layerSwitcherState[z].visibility;
                            break;
                        }
                    }
                    if(!layerSwitcherVisible){
                        continue;
                    }
                }


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
        lonlat = lonlat.transform('EPSG:3857','EPSG:4326');
        var longitude = lonlat.lon;
        var latitude = lonlat.lat;
        var layer = primitive ? primitive.getLayer() : null;

        var queryTargets = [];
        if (primitive && layer && primitive instanceof portal.map.openlayers.primitives.Polygon) {
            queryTargets = this._makeQueryTargetsPolygon(primitive, this.layerStore, longitude, latitude);
        } else if (primitive && layer) {
            queryTargets = this._makeQueryTargetsVector(primitive, longitude, latitude);
        } else {
            queryTargets = this._makeQueryTargetsMap(this.layerStore, longitude, latitude);
        }

        this.fireEvent('query', this, queryTargets);
    },

    /**
     * If the removal of a layer has the same ID has a info window opened, close it.
     */
    closeInfoWindow: function(layerid){
        if(layerid === this.openedInfoLayerId && this.map.popups[0]){
            this.map.removePopup(this.map.popups[0]);
        }
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
            projection: 'EPSG:3857',
            controls : [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.PanZoomBar({zoomStopHeight:8}),
                //new OpenLayers.Control.LayerSwitcher({'ascending':false}), //useful for debug
                new OpenLayers.Control.MousePosition(),
                new OpenLayers.Control.KeyboardDefaults()
            ],
            layers: [
                     new OpenLayers.Layer.Google(
                             "Google Hybrid",
                             {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
                         ),
                     new OpenLayers.Layer.Google(
                         "Google Physical",
                         {type: google.maps.MapTypeId.TERRAIN}
                     ),
                     new OpenLayers.Layer.Google(
                         "Google Streets", // the default
                         {numZoomLevels: 20}
                     ),
                     new OpenLayers.Layer.Google(
                         "Google Satellite",
                         {type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22}
                     )
                 ],
                 center: new OpenLayers.LonLat(133.3, -26)
                     // Google.v3 uses web mercator as projection, so we have to
                     // transform our coordinates
                     .transform('EPSG:4326', 'EPSG:3857'),
                 zoom: 4
        });

        var ls = new OpenLayers.Control.LayerSwitcher({'ascending':false});

        this.map.addControl(ls);
        ls.maximizeControl();

        this.vectorLayer = new OpenLayers.Layer.Vector("Vectors", {
            preFeatureInsert: function(feature) {
                // Google.v3 uses web mercator as projection, so we have to
                // transform our coordinates

                var bounds = feature.geometry.getBounds();

                //JJV - Here be dragons... this is a horrible, horrible workaround. I am so very sorry :(
                //Because we want to let portal core *think* its in EPSG:4326 and because our base map is in EPSG:3857
                //we automagically transform the geometry on the fly. That isn't a problem until you come across
                //various openlayers controls that add to the map in the native projection (EPSG:3857). To workaround this
                //we simply don't transform geometry that's already EPSG:3857. The scary part is how we go about testing for that...
                //The below should work except for tiny bounding boxes off the west coast of Africa
                if (bounds.top <= 90 && bounds.top >= -90) {
                    feature.geometry.transform('EPSG:4326','EPSG:3857');
                }
            }
        });
        this.map.addLayer(this.vectorLayer);

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
        
        //VT: adds a customZoomBox which fires a afterZoom event.
        var zoomBoxCtrl = new OpenLayers.Control.ZoomBox({alwaysZoom:true,zoomOnClick:false});
        var panCtrl = new OpenLayers.Control.Navigation();
        var customNavToolBar = OpenLayers.Class(OpenLayers.Control.NavToolbar, {
            initialize: function(options) {
                OpenLayers.Control.Panel.prototype.initialize.apply(this, [options]);
                this.addControls([panCtrl, zoomBoxCtrl])
            }
        });

        //VT: once we catch the afterZoom event, reset the control to panning.
        var customNavTb=new customNavToolBar();
        customNavTb.controls[1].events.on({
            "afterZoom": function() {
               customNavTb.defaultControl=customNavTb.controls[0];
               customNavTb.activateControl(customNavTb.controls[0]);
               clickControl.activate();
            }
        });

        this.map.addControl(customNavTb);

        //If we are allowing data selection, add an extra control to the map
        if (this.allowDataSelection) {
            //This panel will hold our Draw Control. It will also custom style it
            var panel = new OpenLayers.Control.Panel({
                createControlMarkup: function(control) {
                    var button = document.createElement('button'),
                        iconSpan = document.createElement('span'),
                        activeTextSpan = document.createElement('span');
                        inactiveTextSpan = document.createElement('span');

                    iconSpan.innerHTML = '&nbsp;';
                    button.appendChild(iconSpan);

                    activeTextSpan.innerHTML = control.activeText;
                    Ext.get(activeTextSpan).addCls('active-text');
                    button.appendChild(activeTextSpan);

                    inactiveTextSpan.innerHTML = control.inactiveText;
                    Ext.get(inactiveTextSpan).addCls('inactive-text');
                    button.appendChild(inactiveTextSpan);

                    button.setAttribute('id', control.buttonId);

                    return button;
                }
            });

            var drawFeatureCtrl = new OpenLayers.Control.DrawFeature(this.vectorLayer, OpenLayers.Handler.RegularPolygon, {
                handlerOptions: {
                    sides: 4,
                    irregular: true
                },
                title:'Draw a bounding box to select data in a region.',
                activeText: 'Click and drag a region of interest',
                buttonId : 'gmap-subset-control',
                inactiveText : 'Select Data'
            });
            panel.addControls([drawFeatureCtrl]);

            //We need to ensure the click controller and other controls aren't active at the same time
            drawFeatureCtrl.events.register('activate', {}, function() {
                clickControl.deactivate();
                Ext.each(customNavTb.controls, function(ctrl) {
                   ctrl.deactivate();
                });
            });
            drawFeatureCtrl.events.register('deactivate', {}, function() {
                clickControl.activate();
            });
            Ext.each(customNavTb.controls, function(ctrl) {
                ctrl.events.register('activate', {}, function() {
                    drawFeatureCtrl.deactivate();
                });
            });

            //We need to listen for when a feature is drawn and act accordingly
            drawFeatureCtrl.events.register('featureadded', {}, Ext.bind(function(e){
                var ctrl = e.object;
                var feature = e.feature;

                //Remove box after it's added (delayed by 3 seconds so the user can see it)
                var task = new Ext.util.DelayedTask(Ext.bind(function(feature){
                    this.vectorLayer.removeFeatures([feature]);
                }, this, [feature]));
                task.delay(3000);

                //raise the data selection event
                var originalBounds = feature.geometry.getBounds();
                var bounds = originalBounds.transform('EPSG:3857','EPSG:4326').toArray();
                var bbox = Ext.create('portal.util.BBox', {
                    northBoundLatitude : bounds[3],
                    southBoundLatitude : bounds[1],
                    eastBoundLongitude : bounds[2],
                    westBoundLongitude : bounds[0]
                });

                //Iterate all active layers looking for data sources (csw records) that intersect the selection
                var intersectedRecords = this.getLayersInBBox(bbox);
                this.fireEvent('dataSelect', this, bbox, intersectedRecords);

                //Because click events are still 'caught' even if the click control is deactive, the click event
                //still gets fired. To work around this, add a tiny delay to when we reactivate click events
                var task = new Ext.util.DelayedTask(Ext.bind(function(ctrl){
                    ctrl.deactivate();
                }, this, [ctrl]));
                task.delay(50);
            }, this));


            this.map.addControl(panel);
        }
        
        //Finally listen for resize events on the parent container so we can pass the details
        //on to Openlayers.
        container.on('resize', function() {
            this.map.updateSize();
        }, this);
    },

    /**
     * Returns the currently visible map bounds as a portal.util.BBox object.
     *
     * function()
     */
    getVisibleMapBounds : function() {
        var bounds = this.map.getExtent().transform('EPSG:3857','EPSG:4326').toArray();
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
    openInfoWindow : function(windowLocation, width, height, content,layer) {
        //Firstly create a popup with a chunk of placeholder HTML - we will render an ExtJS container inside that
        var popupId = Ext.id();
        var location = new OpenLayers.LonLat(windowLocation.getLongitude(), windowLocation.getLatitude());
        location = location.transform('EPSG:4326','EPSG:3857');
        var verticalPadding = content.length <= 1 ? 0 : 32; //If we are opening a padded popup, we need to pad for the header
        var horizontalPadding = 0;
        var paddedSize = new OpenLayers.Size(width + horizontalPadding, height + verticalPadding);
        var divId = Ext.id();
        var divHtml = Ext.util.Format.format('<html><body><div id="{0}" style="width: {1}px; height: {2}px;"></div></body></html>', divId, paddedSize.w, paddedSize.h);
        var popup = new OpenLayers.Popup.FramedCloud(popupId, location, paddedSize, divHtml, null, true, null);

        this.map.addPopup(popup, true);
        this.openedInfoLayerId=layer.get('id');
        //next create an Ext.Container to house our content, render it to the HTML created above
        if (!Ext.isArray(content)) {
            content = [content];
        }

        //We need a parent control to house the components, a regular panel works fine for one component
        //A tab panel will be required for many components
        if (content.length === 1) {
            Ext.create('Ext.panel.Panel', {
                width : paddedSize.w,
                height : paddedSize.h,
                autoScroll : true,
                renderTo : divId,
                border : false,
                items : content
            });
        } else {
            var tabPanelItems = [];
            for (var i = 0; i < content.length; i++) {
                if (Ext.isString(content[i])) {
                    tabPanelItems.push({
                        title : '',
                        border : false,
                        autoScroll : true,
                        html : content[i]
                    });
                } else {
                    tabPanelItems.push({
                        title : content[i].tabTitle,
                        border : false,
                        autoScroll : true,
                        items : [content[i]]
                    });
                }
            }

            Ext.create('Ext.tab.Panel', {
                width : paddedSize.w,
                height : paddedSize.h,
                renderTo : divId,
                plain : true,
                border : false,
                activeTab: 0,
                items : tabPanelItems
            });
        }
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
        bounds.transform('EPSG:4326','EPSG:3857');
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
     * @param crs - the crs of the point
     */
    setCenter : function(point,crs) {
        if(crs && crs=='EPSG:3857'){
            this.map.panTo(new OpenLayers.LonLat(point.getLongitude(), point.getLatitude()))
        }else{
            this.map.panTo((new OpenLayers.LonLat(point.getLongitude(), point.getLatitude()))
                .transform('EPSG:4326','EPSG:3857'));
        }
    },

    /**
     * Gets the location of the center point on the map as a portal.map.Point
     *
     * function()
     */
    getCenter : function() {
        var center = this.map.getCenter();
        center = center.transform('EPSG:3857','EPSG:4326');
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
    getTileInformationForPoint : function(point) {
        var layer = this.map.baseLayer;
        var tileSize = this.map.getTileSize();
        //Get the bounds of the tile that encases point
        var lonLat = new OpenLayers.LonLat(point.getLongitude(), point.getLatitude());
            lonLat = lonLat.transform('EPSG:4326','EPSG:3857');
        var viewPortPixel = this.map.getViewPortPxFromLonLat(lonLat);

        var tileBounds = this.map.getExtent();//.transform('EPSG:3857','EPSG:4326');

        return Ext.create('portal.map.TileInformation', {
            width : this.map.size.w,
            height : this.map.size.h,
            offset : {  //Object - The point location within the tile being queried
                x : viewPortPixel.x ,
                y : viewPortPixel.y
            },
            tileBounds : Ext.create('portal.util.BBox', {
                eastBoundLongitude : tileBounds.right,
                westBoundLongitude : tileBounds.left,
                northBoundLatitude : tileBounds.top,
                southBoundLatitude : tileBounds.bottom
            })
        });
    },

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
        var lonlat=new OpenLayers.LonLat(point.getLongitude(), point.getLatitude());
        lonlat = lonlat.transform('EPSG:4326','EPSG:3857');
        var layerPixel = this.map.getLayerPxFromLonLat(lonlat);
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
            this.closeInfoWindow(layer.get('id'));
        }
    }
});