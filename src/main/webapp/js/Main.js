//this runs on DOM load - you can access all the good stuff now.
var theglobalexml;

Ext.onReady(function() {
    var map;
    var mgr;



    //this tree holds all of the data sources
    var tree = new Ext.tree.TreePanel({
        title : 'Data Sources',
        region: 'center',
        split: true,
        collapsible: true,
        width: 300,
        useArrows:true,
        //autoScroll:true,
        //animate:true,
        //containerScroll: true,
        rootVisible: false,
        dataUrl: 'dataSources.json',
        root: {
            nodeType: 'async',
            text: 'Ext JS',
            draggable:false,
            id:'root'
        }
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
                //alert(node.attributes.wfsUrl);
                //we are assuming a KML response from the WFS requests
                GDownloadUrl(node.attributes.kmlUrl, function(pData, pResponseCode) {
                    if (pResponseCode == 200) {
                        var exml;
                        var icon = new GIcon(G_DEFAULT_ICON, node.attributes.icon);
                        icon.iconSize = new GSize(32, 32);
                        exml = new GeoXml("theglobalexml", map, null, {baseicon:icon, markeroptions:{markerHandler:function(marker) {
                            marker.featureType = node.attributes.featureType;
                            marker.wfsUrl = node.attributes.kmlUrl;
                        }}});
                        exml.parseString(pData);

                        node.attributes.tileOverlay = exml;

                        //add a download button
                        node.attributes.downloadButton = makeButtonAndAdd(node.attributes.wfsUrl, node.text);
                    }
                });
            }
        }

        //the check was checked off so remove the overlay
        else {
            //remove the download button
            buttonPanel.remove(node.attributes.downloadButton);
            buttonPanel.doLayout();
            
            node.attributes.tileOverlay.clear();
            node.attributes.tileOverlay = null;
        }
    });

    //this center panel will hold the google maps
    var centerPanel = new Ext.Panel({region:"center", margins:'100 0 0 0', cmargins:'100 0 0 0'});

    //this panel will be used for extra options
    //var rightPanel = new Ext.Panel({region:"east", margins:'100 0 0 0', cmargins:'100 0 0 0', title: "More Options", split:true, size: 0, collapsible: true});


    // The action
    var action = new Ext.Action({
        text: 'New Button',
        width: "100%",
        handler: function(){
           buttonPanel.add(new Ext.Button( new Ext.Action({
                text: 'New Button2',
                width: "100%",
                handler: function(){

                }
            })));
            buttonPanel.doLayout();
        }
    });

    /*var buttonPanel = new Ext.Panel({
        width: '100%',
        items: [new Ext.Button(action)]
    });*/

    var buttonPanel = new Ext.FormPanel({
        title: 'Options',
        bodyStyle:'padding:5px 5px 0',

        region:"south",
        items: [new Ext.Container()],
        split:true,
        width: 300,
        height: 200,
        collapsible: true
    });

    function makeButtonAndAdd(wfsUrl, name) {
        var button = new Ext.Button( new Ext.Action({
            text: 'Get ' + name,
            width: "100%",
            handler: function(){
                window.open(wfsUrl+"%26BBOX="+getBoundingBox(), name);
            }
        }));

        buttonPanel.add(button);
        buttonPanel.doLayout();

        return button;
    }

    function getBoundingBox() {
        var bounds = map.getBounds();
        var southWest = bounds.getSouthWest();
        var northEast = bounds.getNorthEast();

        alert(southWest.lng() + ' - ' + northEast.lng());

        var cords =  southWest.lng() + "," +
                southWest.lat() + "," +
    	        (southWest.lng() > northEast.lng() ? northEast.lng() + 360 : northEast.lng()) + "," +
    	        northEast.lat();

        alert(cords);

        return cords;
    }

    //used to show extra details
    //var detailsPanel = new Ext.Panel({region:"south", title: "Stuff", split:true, width: 300, height: 200, collapsible: true, items:[buttonPanel]});

    //used as a placeholder for the tree and details panel on the left of screen
    var westPanel = new Ext.Panel({
        region:"west",
        margins:'100 0 0 0',
        cmargins:'100 0 0 0',
        title: "",
        split:true,
        width: 300,
        collapsible: true,
        layout:'border',
        items:[tree, buttonPanel]
    });

    //add all the panels to the viewport
    var viewport = new Ext.Viewport({
        layout:'border',
        items:[westPanel, centerPanel]
    });

    // Is user's browser suppported by Google Maps?
    if (GBrowserIsCompatible()) {
        map = new GMap2(centerPanel.body.dom);

        // Large pan and zoom control
        map.addControl(new GLargeMapControl());

        // Toggle between Map, Satellite, and Hybrid types
        map.addControl(new GMapTypeControl());

        var startZoom = 4;
        map.setCenter(new google.maps.LatLng(-26, 133.3), 4);
        map.setMapType(G_SATELLITE_MAP);

        //Thumbnail map
        var Tsize = new GSize(150, 150);
        map.addControl(new GOverviewMapControl(Tsize));

        var mgrOptions = { borderPadding: 50, maxZoom: 15, trackMarkers: true };
        mgr = new MarkerManager(map, mgrOptions);
    }

    theglobalexml = new GeoXml("theglobalexml", map, null, null);

    //when a person clicks on a marker then do something
    GEvent.addListener(map, "click", function(overlay, latlng) {
        if (overlay instanceof GMarker) {
            if (overlay.featureType == "gsml:Borehole")
                new NVCLMarker(overlay.getTitle(), overlay).getMarkerClickedFn()();
            else if (overlay.featureType == "geodesy:stations") {
                alert('yes geod');
                new GeodesyMarker(overlay.wfsUrl, "geodesy:station_observations", overlay.getTitle(), overlay).getMarkerClickedFn()();
            }
        }
    });
});





