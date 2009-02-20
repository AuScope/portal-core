//this runs on DOM load - you can access all the good stuff now.
Ext.onReady(function() {
    var map;

    var tree = new Ext.tree.TreePanel({
        title : 'Data Sources',
        region: 'west',
        split: true,
        collapsible: true,
        margins:'100 0 0 0',
        cmargins:'100 0 0 0',
        width: 200,
        useArrows:true,
        autoScroll:true,
        animate:true,
        containerScroll: true,
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
                //var tileLayer = new GWMSTileLayer(map, new GCopyrightCollection(""), 1, 17);
                var tileLayer =  new GTileLayer(null, null, null, {
                    tileUrlTemplate: node.attributes.wmsUrl+'layers='+node.id+'&zoom={Z}&x={X}&y={Y}',
                    isPng:true,
                    opacity:1.0 }
                );
                //tileLayer.baseURL = node.attributes.wmsUrl;
                //tileLayer.layers = node.id;
                node.attributes.tileOverlay = new GTileLayerOverlay(tileLayer);
                map.addOverlay(node.attributes.tileOverlay);
                //node.attributes.tileOverlay = new OpenLayers.Layer.WMS( "Some WMS", node.attributes.wmsUrl, {layers: node.id, format: "image/png", transparent: "true", projection: "EPSG:900913"});
                //map.addLayer(layer);
            }
            else if(node.attributes.layerType == 'wfs') {
                //we are assuming a KML response from the WFS requests
                node.attributes.tileOverlay = new GGeoXml(node.attributes.wfsUrl);
                map.addOverlay(node.attributes.tileOverlay);
            }
        }
        //the check was checked off so remove the overlay
        else {
            map.removeOverlay(node.attributes.tileOverlay);
            node.attributes.tileOverlay = null;
        }
    });

    var westPanel = {
        region:'west',
        id:'west-div',
        title:'Data Sources',
        split:true,
        //width: 200,
        //minSize: 175,
        maxSize: 400,
        collapsible: true,
        margins:'100 0 0 0',
        cmargins:'100 0 0 0'
    };

    var centerPanel = new Ext.Panel({region:"center", margins:'100 0 0 0', cmargins:'100 0 0 0'});
    var rightPanel = new Ext.Panel({region:"east", margins:'100 0 0 0', cmargins:'100 0 0 0', title: "More Options", split:true, size: 400, collapsible: true});

    var viewport = new Ext.Viewport({
        layout:'border',
        items:[tree, centerPanel, rightPanel]
    });

    //<![CDATA[

    //function load() {
    //alert(window.location.host);
    //alert(${2+2});

    // Is user's browser suppported by Google Maps?
    if (GBrowserIsCompatible()) {
        map = new GMap2(centerPanel.body.dom);
        var mgrOptions = { borderPadding: 50, maxZoom: 15, trackMarkers: true };
        var mgr = new MarkerManager(map, mgrOptions);

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
    }

    /*var options = {
     projection: new OpenLayers.Projection("EPSG:900913"),
     displayProjection: new OpenLayers.Projection("EPSG:4326"),
     units: "m",
     numZoomLevels: 18,
     maxResolution: 156543.0339,
     maxExtent: new OpenLayers.Bounds(-20037508, -20037508,
     20037508, 20037508.34),
     controls: [ new OpenLayers.Control.PanZoom(), new OpenLayers.Control.Permalink(), new OpenLayers.Control.MouseDefaults() ]
     };
     //map = new OpenLayers.Map(centerPanel.body.dom, options);

     map = new OpenLayers.Map(centerPanel.body.dom, {controls: [ new OpenLayers.Control.PanZoom(), new OpenLayers.Control.Permalink(), new OpenLayers.Control.MouseDefaults() ]});
     var google = new OpenLayers.Layer.WMS( "OpenLayers WMS", "http://labs.metacarta.com/wms/vmap0", {layers: 'basic'} );
     //var google = new OpenLayers.Layer.Google( "Google" , {type: G_SATELLITE_MAP, 'sphericalMercator': true, numZoomLevels: 22});

     map.addLayer(google);

     var center = new OpenLayers.LonLat(138.493652, -18.604601);
     var proj = new OpenLayers.Projection("EPSG:4326");
     center.transform(proj, map.getProjectionObject());
     map.setCenter(center, 5);*/

    //map.setCenter(new OpenLayers.LonLat(138.493652, -18.604601), 5);
    //map.addControl( new OpenLayers.Control.LayerSwitcher() );

    //getCapabilities();
});