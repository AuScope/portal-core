/**
 * Concrete implementation for OpenLayers
 */
Ext.define('portal.map.openlayers.OpenLayersMap', {
    extend : 'portal.map.BaseMap',

    map : null, //Instance of OpenLayers.Map
    vectorLayers : [],    
    selectControl : null,
//    mapCreatedEventListeners : [],  // Listeners (functions) to call once the map has been created
    layerSwitcher : null,           // Keep as a global so can check if has been created

    constructor : function(cfg) {
        this.callParent(arguments);
        
        // If the portal (eg. Geoscience Portal but NOT it as GP already handles this) doesn't call 
        // renderBaseMap() then the OpenLayers LayerSwitcher won't appear so we set a timeout to 
        // create it if it hasn't
        if (!cfg.portalIsHandlingLayerSwitcher) {
            Ext.defer(this._callDrawOpenLayerSwitcher, 2000, this);
        }
    },
    
    _callDrawOpenLayerSwitcher : function() {
        var meMethod=this._callDrawOpenLayerSwitcher;
        if (this.map) {
            this._drawOpenLayerSwitcher();
        } else {
            // Wait until it is ready
            Ext.defer(meMethod, 250, this);
        }
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
    makeWms : function(id, sourceCswRecord, sourceOnlineResource, sourceLayer, wmsUrl, wmsLayer, opacity,sld_body) {
        return Ext.create('portal.map.openlayers.primitives.WMSOverlay', {
            id : id,
            layer : sourceLayer,
            onlineResource : sourceOnlineResource,
            cswRecord : sourceCswRecord,
            wmsUrl : wmsUrl,
            wmsLayer : wmsLayer,
            opacity : opacity,
            map : this.map,
            sld_body : sld_body
        });
    },

    _makeQueryTargetsPolygon : function(polygon, layerStore, longitude, latitude) {
        var queryTargets = [];
        var lonLat = new OpenLayers.LonLat(longitude, latitude);
        lonLat=lonLat.transform('EPSG:4326','EPSG:3857');

        //Iterate all features on the map, those that intersect the given lat/lon should
        //have query targets generated for them as it isn't clear which one the user meant
        //to click
        for(var j = 0; j < this.vectorLayers.length;j++){
            var vectorLayer = this.vectorLayers[j];
            for (var i = 0; i < vectorLayer.features.length; i++) {
                var featureToTest = vectorLayer.features[i];
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
        if (!layerStore) {
            return queryTargets;
        }
        
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
                if (wmsResources[0]) {
                    var layerSwitcherVisible=true;
                    var layerName=wmsResources[0].get('name');

                    // We loop over the available to controls to find the layer switcher:
                    var layerSwitcher = null;
                    for (var y=0; y < this.map.controls.length; y++) {
                        if (this.map.controls[y] instanceof OpenLayers.Control.LayerSwitcher) {
                            layerSwitcher = this.map.controls[y];
                            break;
                        }
                    }

                    var layerSwitcherState = layerSwitcher.layerStates;
                    for (var z = 0; z < layerSwitcherState.length; z++) {
                        if (layerSwitcherState[z].name === layerName) {
                            layerSwitcherVisible=layerSwitcherState[z].visibility;
                            break;
                        }
                    }

                    if (!layerSwitcherVisible) {
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

                        var serviceFilter = layer.get('filterer').getParameters().serviceFilter;
                        if (serviceFilter) {
                            if (Ext.isArray(serviceFilter)) {
                                serviceFilter = serviceFilter[0];
                            }
                            // layers get filtered based on the service provider
                            // or from a single provider and based on the layer name
                            if (resourcesToIterate[k].get('name') != serviceFilter &&
                                    this._getDomain(resourcesToIterate[k].get('url')) != this._getDomain(serviceFilter)) {
                                continue;
                            }
                        }

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
    
    _handleDrawCtrlAddFeature : function(e) {
        var ctrl = e.object;
        var feature = e.feature;

        //Remove box after it's added (delayed by 3 seconds so the user can see it)
        var task = new Ext.util.DelayedTask(Ext.bind(function(feature){
            this._drawCtrlVectorLayer.removeFeatures([feature]);
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
    renderToContainer : function(container,divId) {
        //VT: manually set the id.
        var containerId = divId;
        var me = this;

        this.map = new OpenLayers.Map({
            div: containerId,
            projection: 'EPSG:3857',
            controls : [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.PanZoomBar({zoomStopHeight:8}),
                new OpenLayers.Control.MousePosition({
                    "numDigits": 2,
                    displayProjection: new OpenLayers.Projection("EPSG:4326"),
                    prefix: '<a target="_blank" href="http://spatialreference.org/ref/epsg/4326/">Map coordinates (WGS84 decimal degrees)</a>: ' ,
                    suffix : ' / lat lng',
                    emptyString : '<a target="_blank" href="http://spatialreference.org/ref/epsg/4326/">Map coordinates (WGS84 decimal degrees): </a> Out of bound',
                    element : Ext.get('latlng').dom,
                    formatOutput: function(lonLat) {
                        var digits = parseInt(this.numDigits);
                        var newHtml =
                            this.prefix +
                            lonLat.lat.toFixed(digits) +
                            this.separator +
                            lonLat.lon.toFixed(digits) +
                            this.suffix;
                        return newHtml;
                     }
                })
            ],
            layers: [
                     new OpenLayers.Layer.WMS (
                         "World Political Boundaries",
                         "http://services.ga.gov.au/site_1/services/World_Political_Boundaries_WM/MapServer/WMSServer",
                         {layers: 'Countries'}
                     ),
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
                    		 // Name of the layer that will be set as default. If removed or changed, also
                    		 // change code below which sets as default
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

        var selLayer = this.map.getLayersByName("Google Satellite");
        if (selLayer !== null) {        
            this.map.setBaseLayer(selLayer[0]);
        }
        
        // Creation and rendering of LayerSwitcher moved to renderBaseMap()

        this.highlightPrimitiveManager = this.makePrimitiveManager(true);
        this.container = container;
        this.rendered = true;
        
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
                me.fireEvent('afterZoom', this);                             

               // reset the control to panning.
               customNavTb.defaultControl=customNavTb.controls[0];
               customNavTb.activateControl(customNavTb.controls[0]);
               me._activateClickControl();
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
            
            this._drawCtrlVectorLayer = this._getNewVectorLayer();
                       
            var drawFeatureCtrl = new OpenLayers.Control.DrawFeature(this._drawCtrlVectorLayer, OpenLayers.Handler.RegularPolygon, {
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
                me._deactivateClickControl();
                Ext.each(customNavTb.controls, function(ctrl) {
                   ctrl.deactivate();
                });
            });
            drawFeatureCtrl.events.register('deactivate', {}, function() {
                me._activateClickControl();
            });
            Ext.each(customNavTb.controls, function(ctrl) {
                ctrl.events.register('activate', {}, function() {
                    drawFeatureCtrl.deactivate();
                });
            });

            //We need to listen for when a feature is drawn and act accordingly
            drawFeatureCtrl.events.register('featureadded', {}, Ext.bind(function(e) {this._handleDrawCtrlAddFeature(e);}, this));


            this.map.addControl(panel);
        }

        //Finally listen for resize events on the parent container so we can pass the details
        //on to Openlayers.
        container.on('resize', function() {
            this.map.updateSize();
        }, this);
        
        //Finally listen for boxready events on the parent container so we can pass the details
        //on to Openlayers.
        container.on('boxready', function() {
            this.map.updateSize();
        }, this);        
    },

    // Draw the OpenLayers layers (eg. "Google Street View"/"Google Satellite") Controls (GPT-40 Active Layers)
    renderBaseMap : function(divId) {
        var me = this;
//        console.log("renderBaseMap - LAYERS")
        // Setup if map is defined else let map know to send this an event once created
        if (this.map) {
//            console.log("renderBaseMap - LAYERS - map NOT null: " + this.map + " - create LayerSwitcher (div: "+divId+")");
            this._drawOpenLayerSwitcher(divId);
        } else {
//            console.log("renderBaseMap - LAYERS - map IS undefined - setup callback (div: "+divId+")");
            // This object has a listener for 'mapcreated' events that are fired
            portal.events.AppEvents.addListener(me, {callback:this.renderBaseMap, divId:divId});
        }
    },
    
    listeners : {
        mapcreated : function (args) {
            // Expect - arg1: functionToCall, arg2: divId
            var theFunction = args.callback;
            var theId = args.divId;
            var me = this;
            
//           console.log("OpenLayersMap - listener - mapCreated - theId: " +theId+", function: ", theFunction);
           theFunction.apply(me, [theId]);
        }
    },
    
    _drawOpenLayerSwitcher : function(divId) {
        if (! this.layerSwitcher) {
            if (divId) {
                this.layerSwitcher = new OpenLayers.Control.LayerSwitcher({
                    'div': OpenLayers.Util.getElement(divId),
                    'ascending':false
                });
            } else {
                // No div so it will appear on the map if the portal code doesn't call this
                this.layerSwitcher = new OpenLayers.Control.LayerSwitcher({
                    'ascending':false
                });
            }
            
            this.map.addControl(this.layerSwitcher);
            this.layerSwitcher.maximizeControl();
        }
    },
    
    _getNewVectorLayer : function(){
        var vectorLayer = new OpenLayers.Layer.Vector("Vectors", {
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
            },
            displayInLayerSwitcher : false
        });
        this.map.addLayer(vectorLayer);
        return vectorLayer;
    },
    /**
     * Returns the currently visible map bounds as a portal.util.BBox object.
     *
     * function()
     */
    getVisibleMapBounds : function() {
        var bounds = this.map.getExtent().transform('EPSG:3857','EPSG:4326').toArray();
        
        if(bounds[2]>180){
            var exceedLong=bounds[2] - 180;
            bounds[2] = (180 - exceedLong)*-1;
        }

        // Nasty Maths - work out the precision....
        eastWestDelta = Math.abs(Math.max(bounds[0], bounds[2]) - Math.min(bounds[0], bounds[2]));
        northSouthDelta = Math.abs(Math.max(bounds[1], bounds[3]) - Math.min(bounds[1], bounds[3]));

        // y = np.floor(1/np.power(x,0.15))
        /*
         * 0.0001  :  4.0
         * 0.001  :  3.0
         * 0.01  :  2.0
         * 0.1  :  1.0
         * 1.0  :  1.0
         * 10.0  :  0.0
         * 100.0  :  0.0
         */


        calcEWprecis = Math.floor(1/Math.pow(eastWestDelta,0.16));
        calcNSprecis = Math.floor(1/Math.pow(northSouthDelta,0.16));

        if (calcEWprecis < 0) {
            calcEWprecis = 0;
        }
        if (calcNSprecis < 0) {
            calcNSprecis = 0;
        }

        north = bounds[3].toFixed(calcNSprecis);
        south = bounds[1].toFixed(calcNSprecis);
        east = bounds[2].toFixed(calcEWprecis);
        west = bounds[0].toFixed(calcEWprecis);

        return Ext.create('portal.util.BBox', {
            westBoundLongitude : west,
            southBoundLatitude : south,
            eastBoundLongitude : east,
            northBoundLatitude : north
        });
    },

    /**
     * Creates a new empty instance of the portal.map.PrimitiveManager class for use
     * with this map
     *
     * function()
     */
    makePrimitiveManager : function(noLazyGeneration) {
       
        var clickableLayers = this.vectorLayers
        var clickControl = new portal.map.openlayers.ClickControl(clickableLayers, {
            map : this.map,
            trigger : Ext.bind(this._onClick, this)
        });
                
        var controlList = this.map.getControlsByClass('portal.map.openlayers.ClickControl');
                
        //VT: update the map clickControl with the updated layer
        for(var i = 0; i < controlList.length; i++){
            this.map.removeControl(controlList[i]);                        
        }
        
        this.map.addControl(clickControl);
        clickControl.activate();
                                     
        return Ext.create('portal.map.openlayers.PrimitiveManager', {
            baseMap : this,
            vectorLayerGenerator: Ext.bind(function() {
                var newVectorLayer = this._getNewVectorLayer();
                this.vectorLayers.push(newVectorLayer);
                return newVectorLayer;
            }, this),
            noLazyGeneration: noLazyGeneration,
            listeners: {
                //See ANVGL-106 for why we need to forcibly reorder thse
                addprimitives : Ext.bind(function() {
                    //Move highlight layer to top
                    
                    var highlightLayer = this.highlightPrimitiveManager.vectorLayer;
                    if (highlightLayer) {
                        this.map.setLayerIndex(highlightLayer, this.map.layers.length);
                    }
                    //Move drawing layer to top
                    var ctrls = this.map.getControlsByClass('OpenLayers.Control.DrawFeature');
                    if (!Ext.isEmpty(ctrls)) {
                        this.map.setLayerIndex(ctrls[0].layer, this.map.layers.length);
                    }
                }, this)
            }
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
        
        //Workaround
        //ExtJS needs events to bubble up to the window for them to work (it's where the event handlers live)
        //Unfortunately OpenLayers is too aggressive in consuming events occuring in a popup, so the events never make it.
        //So - to workaround this we capture relevant events in our parent div (sitting before the open layer popup handlers) 
        //and manually redirect them to the ExtJS handlers
        var node = Ext.get(divId).dom;
        var handler = function(e) {
            Ext.event.publisher.Dom.instance.onDelegatedEvent(e); //this is a private ExtJS function - it's likely to break on upgrade
            if (e.type !== 'click') {
                Ext.event.publisher.Gesture.instance.onDelegatedEvent(e);
            }
            return false;
        };
        node.addEventListener('mousedown', handler);
        node.addEventListener('mouseup', handler);
        node.addEventListener('mousemove', handler);
        node.addEventListener('click', handler);             

        //End workaround
        
        
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
                layout: 'fit',
                renderTo : divId,
                border : false,
                items : content
            });
          //VT:Tracking
            portal.util.PiwikAnalytic.trackevent('Query','layer:'+layer.get('name'),'id:' + content[0].tabTitle);
            
        } else {
            var tabPanelItems = [];
            for (var i = 0; i < content.length; i++) {
                if (Ext.isString(content[i])) {
                    tabPanelItems.push({
                        title : '',
                        border : false,
                        layout: 'fit',
                        autoScroll : true,
                        html : content[i]
                    });
                    portal.util.PiwikAnalytic.trackevent('Query','layer:'+layer.get('name'),'id:Unknown');
                } else {
                    tabPanelItems.push({
                        title : content[i].tabTitle,
                        border : false,
                        layout: 'fit',
                        autoScroll : true,
                        items : [content[i]]
                    });
                    portal.util.PiwikAnalytic.trackevent('Query','layer:'+layer.get('name'),'id:' + content[i].tabTitle);
                }
            }

            Ext.create('Ext.tab.Panel', {
                width : paddedSize.w,
                height : paddedSize.h,
                renderTo : divId, 
                layout: 'fit',
                //plain : true,
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
        this.map.zoomToExtent(bounds,true);
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
     * Add KML from String to the map layer.
     *
     * @param KMLString KML String
     */
    addKMLFromString : function(id,title, KMLString){
        var feature = this.getFeaturesFromKMLString(KMLString) 
        var vectorLayer = new OpenLayers.Layer.Vector(title,{
            projection: "EPSG:4326"
        });                               
        vectorLayer.addFeatures(feature);
        this.map.addLayer(vectorLayer);  
        this.map.zoomToExtent(vectorLayer.getDataExtent());
        return vectorLayer;

    },
    
    /**
     * Remove KML layer from the map
     *
     * @param the id of the KML layer to remove
     */
    removeKMLLayer : function(vectorlayer){           
        this.map.removeLayer(vectorlayer);
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
   
    _getDomain : function(data) {
        return portal.util.URL.extractHostNSubDir(data,1);
    },

    _activateClickControl : function() {
        var controlList = this.map.getControlsByClass('portal.map.openlayers.ClickControl');
        for(var i = 0; i < controlList.length; i++){
            controlList[i].activate();
        }
    },

    _deactivateClickControl : function() {
        var controlList = this.map.getControlsByClass('portal.map.openlayers.ClickControl');
        for(var i = 0; i < controlList.length; i++){
            controlList[i].deactivate();
        }
    }
});

