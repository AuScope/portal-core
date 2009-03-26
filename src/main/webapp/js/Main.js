//this runs on DOM load - you can access all the good stuff now.
var theglobalexml;

Ext.onReady(function() {
    var map;
    var downloadUrls = new Hashtable();

    //this tree holds all of the data sources
    var tree = new Ext.tree.TreePanel({
        title: 'Themes',
        region:'north',
        split: true,
        height: 300,
        autoScroll: true,

        rootVisible: false,
        dataUrl: 'dataSources.json',
        root: {
            nodeType: 'async',
            text: 'Ext JS',
            draggable:false,
            id:'root'
        }
    });

    //used to show extra details for querying services
    var filterPanel = new Ext.Panel({
        title: "Filter Properties",
        region: 'center',
        width: '100%',
        layout: 'card',
        activeItem: 0,
        items: [{html: '<p style="margin:15px;padding:15px;border:1px dotted #999;color:#555;background: #f9f9f9;"> Filter options will be shown here for special services.</p>'}]
    });


    var buttonsPanel = new Ext.FormPanel({
        region: 'south',
        autoScroll:true,
        width: '100%',
        items: [{border: false}],
        buttons: [{text: "Download Datasets", handler: function() {handleDownload();} }]
    });

    //used as a placeholder for the tree and details panel on the left of screen
    var westPanel = {
        layout: 'border',
        region:'west',
        border: false,
        split:true,
        margins: '100 0 0 0',
        width: 350,

        items:[tree, filterPanel, buttonsPanel]
    }

    //this center panel will hold the google maps
    var centerPanel = new Ext.Panel({region:"center", margins:'100 0 0 0', cmargins:'100 0 0 0'});

    var statusBar = new Ext.StatusBar({
        region: "south",
        id: 'my-status',
        hidden: true,

        // defaults to use when the status is cleared:
        defaultText: 'Default status text',
        defaultIconCls: 'default-icon',

        // values to set initially:
        text: 'Ready',
        iconCls: 'ready-icon'
    });

    //add all the panels to the viewport
    var viewport = new Ext.Viewport({
        layout:'border',
        items:[westPanel, centerPanel, statusBar]
    });

    // Is user's browser suppported by Google Maps?
    if (GBrowserIsCompatible()) {
        map = new GMap2(centerPanel.body.dom);
        map.setUIToDefault();

        // Large pan and zoom control
        //map.addControl(new GLargeMapControl(),  new GControlPosition(G_ANCHOR_TOP_LEFT));

        // Toggle between Map, Satellite, and Hybrid types
        map.addControl(new GMapTypeControl());

        var startZoom = 4;
        map.setCenter(new google.maps.LatLng(-26, 133.3), 4);
        map.setMapType(G_HYBRID_MAP);

        //Thumbnail map
        var Tsize = new GSize(150, 150);
        map.addControl(new GOverviewMapControl(Tsize));

        map.addControl(new DragZoomControl(), new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(280, 7)));

        //var mgrOptions = { borderPadding: 50, maxZoom: 15, trackMarkers: true };
        //mgr = new MarkerManager(map, mgrOptions);
    }

    //a dud gloabal for geoxml class
    theglobalexml = new GeoXml("theglobalexml", map, null, null);

    //event handlers and listeners
    tree.on('click', function(node, event) {
        isFilterPanelNeeded(node);
    });
    
    tree.on('checkchange', function(node, isChecked) {
        //the check was checked on
        if (isChecked) {
            if (node.attributes.layerType == 'wms' && (node.attributes.tileOverlay == null || node.attributes.tileOverlay == '')) {
                var tileLayer = new GTileLayer(null, null, null, {
                    tileUrlTemplate: node.attributes.wmsUrl + 'layers=' + node.id + '&zoom={Z}&x={X}&y={Y}',
                    isPng:true,
                    opacity:1.0 }
                        );
                node.attributes.tileOverlay = new GTileLayerOverlay(tileLayer);
                map.addOverlay(node.attributes.tileOverlay);
            }
            else if (node.attributes.layerType == 'wfs') {
                if(!isFilterPanelNeeded(node)) {
                    //we are assuming a KML response from the WFS requests
                    statusBar.setStatus({
                        text: 'Finished loading',
                        iconCls: 'ok-icon',
                        clear: true
                    });
                    statusBar.setVisible(true);
                    viewport.doLayout();
                    statusBar.showBusy();
                    node.disable();

                    if (node.attributes.featureType == "gsml:GeologicUnit") {
                        var ggeoxml = new GGeoXml(node.attributes.kmlUrl);
                        node.attributes.tileOverlay = ggeoxml;
                        map.addOverlay(ggeoxml);

                        node.enable();
                        statusBar.setVisible(false);
                        viewport.doLayout();
                        statusBar.clearStatus();
                    }
                    else {
                        GDownloadUrl(kmlProxyUrl+node.attributes.kmlUrl, function(pData, pResponseCode) {
                            if (pResponseCode == 200) {
                                var exml;
                                var icon = new GIcon(G_DEFAULT_ICON, node.attributes.icon);
                                icon.iconSize = new GSize(32, 32);
                                exml = new GeoXml("theglobalexml", map, null, {baseicon:icon, markeroptions:{markerHandler:function(marker) {
                                    marker.featureType = node.attributes.featureType;
                                    marker.wfsUrl = node.attributes.kmlUrl;
                                }}});
                                exml.parseString(pData);
                                node.enable();
                                statusBar.setVisible(false);
                                viewport.doLayout();
                                statusBar.clearStatus();

                                node.attributes.tileOverlay = exml;

                                downloadUrls.put(node.attributes.wfsUrl, node.attributes.wfsUrl);
                                //add a download button
                                // node.attributes.downloadButton = makeButtonAndAdd(node.attributes.wfsUrl, node.text);
                            }
                        });
                    }
                }
            }
        }
        //the check was checked off so remove the overlay
        else {
            if (node.attributes.layerType == 'wfs') {
                downloadUrls.remove(node.attributes.wfsUrl);
            }

            if (node.attributes.tileOverlay instanceof GeoXml)
                node.attributes.tileOverlay.clear();
            else if(node.attributes.tileOverlay != null)
                map.removeOverlay(node.attributes.tileOverlay);

            node.attributes.tileOverlay = null;

            isFilterPanelNeeded(node);
        }
    });

    var handleDownload = function() {
        var url = "";
        var theUrls = downloadUrls.values();

        if (theUrls.length >= 1) {
            for (i = 0; i < theUrls.length; i++)
                url += "urls=" + theUrls[i] + "%26";

            //alert("downloadProxy?" + url);
            window.open("downloadProxy?" + url, name);
        }
    };

    var serviceUrl;

    //when a person clicks on a marker then do something
    GEvent.addListener(map, "click", function(overlay, latlng) {
        statusBar.showBusy();
        statusBar.setVisible(true);
        viewport.doLayout();

        if (overlay instanceof GMarker) {

            if (overlay.featureType == "gsml:Borehole") {
                new NVCLMarker(overlay.title, overlay, overlay.description).getMarkerClickedFn()();
            }
            else if (overlay.featureType == "geodesy:stations") {
                new GeodesyMarker(overlay.wfsUrl, "geodesy:station_observations", overlay.getTitle(), overlay, overlay.description).getMarkerClickedFn()();
            }
            else if (overlay.description != null) {
                    overlay.openInfoWindowHtml(overlay.description);
                }

        }

        statusBar.clearStatus();
        statusBar.setVisible(false);
        viewport.doLayout();

    });

    //if this feature type needs to be filtered or not
    var isFilterPanelNeeded = function(node) {
        if(node.attributes.featureType == "mo:MiningActivity" && node.getUI().isChecked()) {

            if(node.attributes.filterPanel == null || node.attributes.filterPanel == "") {
                node.attributes.filterPanel = new buildMiningActivityFilterForm(node.id, "/getMineNames.do", "/doMineralOccurrenceFilter.do", node.attributes.wfsUrl, function(form, action){
                    addQueryLayer(node, action.result.data.kml);
                }, function() {

                    if (node.attributes.tileOverlay instanceof GeoXml) {
                        node.attributes.tileOverlay.clear();
                        node.attributes.tileOverlay = null;
                    }
                });
            }

            filterPanel.add(node.attributes.filterPanel);
            filterPanel.doLayout();
            filterPanel.getLayout().setActiveItem(node.id);

            //node.attributes.filterPanel.load();

            return true;
        } else if(node.attributes.featureType == "mo:Mine" && node.getUI().isChecked()) {

            if(node.attributes.filterPanel == null || node.attributes.filterPanel == "") {
                node.attributes.filterPanel = new buildMineFilterForm(node.id, "/getMineNames.do", "/doMineralOccurrenceFilter.do", node.attributes.wfsUrl, function(form, action){
                    addQueryLayer(node, action.result.data.kml);
                }, function() {

                    if (node.attributes.tileOverlay instanceof GeoXml) {
                        node.attributes.tileOverlay.clear();
                        node.attributes.tileOverlay = null;
                    }
                });
            }

            filterPanel.add(node.attributes.filterPanel);
            filterPanel.doLayout();
            filterPanel.getLayout().setActiveItem(node.id);

            //node.attributes.filterPanel.load();

            return true;
        } else if(node.attributes.featureType == "mo:MineralOccurrence" && node.getUI().isChecked()) {

            if(node.attributes.filterPanel == null || node.attributes.filterPanel == "") {
                node.attributes.filterPanel = new buildMineralOccurrenceFilterForm(node.id, "/getMineNames.do", "/doMineralOccurrenceFilter.do", node.attributes.wfsUrl, function(form, action){
                    addQueryLayer(node, action.result.data.kml);
                }, function() {

                    if (node.attributes.tileOverlay instanceof GeoXml) {
                        node.attributes.tileOverlay.clear();
                        node.attributes.tileOverlay = null;
                    }
                });
            }

            filterPanel.add(node.attributes.filterPanel);
            filterPanel.doLayout();
            filterPanel.getLayout().setActiveItem(node.id);

            //node.attributes.filterPanel.load();

            return true;
        }

        filterPanel.getLayout().setActiveItem(0);
        return false;
    };

    var addQueryLayer = function(node, kml) {
        var exml;
        var icon = new GIcon(G_DEFAULT_ICON, node.attributes.icon);
        icon.iconSize = new GSize(32, 32);
        exml = new GeoXml("theglobalexml", map, null, {baseicon:icon, markeroptions:{markerHandler:function(marker) {
            marker.featureType = node.attributes.featureType;
            marker.wfsUrl = node.attributes.kmlUrl;
        }}});
        exml.parseString(kml);
        node.enable();
        statusBar.setVisible(false);
        viewport.doLayout();
        statusBar.clearStatus();

        node.attributes.tileOverlay = exml;
    }


    //utility functions
    function getBoundingBox() {
        var bounds = map.getBounds();
        var southWest = bounds.getSouthWest();
        var northEast = bounds.getNorthEast();

        //alert(southWest.lng() + ' - ' + northEast.lng());

        var cords = southWest.lng() + "," +
                    southWest.lat() + "," +
                    (southWest.lng() > northEast.lng() ? northEast.lng() + 360 : northEast.lng()) + "," +
                    northEast.lat();

        //alert(cords);

        return "%26BBOX=" + cords;
    }

    function getFilter() {
        var bounds = map.getBounds();
        var southWest = bounds.getSouthWest();
        var northEast = bounds.getNorthEast();

        var lowerCorner = southWest.lng() + " " +
                          southWest.lat();

        var upperCorner = (southWest.lng() > northEast.lng() ? northEast.lng() + 360 : northEast.lng()) + " " +
                          northEast.lat();


        return '%26FILTER=<ogc:Filter xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0">' +
               '<ogc:BBOX><ogc:PropertyName>gsml:shape</ogc:PropertyName>' +
               '<gml:Envelope srsName="EPSG:4326">' +
               '<gml:lowerCorner>' + lowerCorner + '</gml:lowerCorner>' +
               '<gml:upperCorner>' + upperCorner + '</gml:upperCorner>' +
               '</gml:Envelope>' +
               '</ogc:BBOX>' +
               '</ogc:Filter>';
    }

});





