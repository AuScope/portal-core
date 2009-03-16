//this runs on DOM load - you can access all the good stuff now.
var theglobalexml;

Ext.onReady(function() {
    var map;
    //var mgr;
    var downloadUrls = new Hashtable();

    //this tree holds all of the data sources
    var tree = new Ext.tree.TreePanel({
        title : 'Data Sources',
        //region: 'center',
        //split: true,
        //collapsible: true,
        border: false,
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
                statusBar.setStatus({
                    text: 'Finished loading',
                    iconCls: 'ok-icon',
                    clear: true
                });
                statusBar.setVisible(true);
                viewport.doLayout();
                statusBar.showBusy();
                node.disable();

                if(node.attributes.featureType == "gsml:GeologicUnit") {
                    var ggeoxml = new GGeoXml(node.attributes.kmlUrl);
                    node.attributes.tileOverlay = ggeoxml;
                    map.addOverlay(ggeoxml);

                    node.enable();
                    statusBar.setVisible(false);
                    viewport.doLayout();
                    statusBar.clearStatus();
                }
                else {
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

        //the check was checked off so remove the overlay
        else {
            //remove the download button
            //buttonPanel.remove(node.attributes.downloadButton);
            //buttonPanel.doLayout();

            if(node.attributes.layerType == 'wfs') {
                downloadUrls.remove(node.attributes.wfsUrl);
            }
            
            if (node.attributes.tileOverlay instanceof GeoXml)
                node.attributes.tileOverlay.clear();
            else
                map.removeOverlay(node.attributes.tileOverlay);

            node.attributes.tileOverlay = null;

        }
    });

    //this center panel will hold the google maps
    var centerPanel = new Ext.Panel({region:"center", margins:'100 0 0 0', cmargins:'100 0 0 0'});

    //this panel will be used for extra options
    //var rightPanel = new Ext.Panel({region:"east", margins:'100 0 0 0', cmargins:'100 0 0 0', title: "More Options", split:true, size: 0, collapsible: true});

    var buttonPanel = new Ext.FormPanel({
        //title: 'Options',
        bodyStyle:'padding:5px 5px 0',
        region:"south",
        items: [new Ext.Container()],
        buttons: [{text: "Download"}],
        split:true,
        width: 300,
        height: 200,
        collapsible: true
    });

    function makeButtonAndAdd(wfsUrl, name) {
        var button = new Ext.Button(new Ext.Action({
            text: 'Get ' + name,
            width: "100%",
            handler: function() {
                window.open(wfsUrl, name);
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

    //used to show extra details
    //var detailsPanel = new Ext.Panel({region:"south", title: "Stuff", split:true, width: 300, height: 200, collapsible: true, items:[buttonPanel]});

    //used as a placeholder for the tree and details panel on the left of screen
    var westPanel = new Ext.FormPanel({
        region:"west",
        margins:'100 0 0 0',
        cmargins:'100 0 0 0',
        title: "",
        split:true,
        autoScroll:true,
        containerScroll: true,
        width: 300,
        collapsible: true,
        items:[tree],
        buttons: [{text: "Download Datasets", handler: function() {
            var url = "";

                var theUrls = downloadUrls.values();

                if(theUrls.length >= 1) {
                    for(i=0; i<theUrls.length; i++)
                        url += "urls=" + theUrls[i] + "%26";

                    //alert("downloadProxy?" + url);
                    window.open("downloadProxy?" + url, name);
                }
            }}]
    });

    var queryFilterPanel = new Ext.FormPanel({
        //margins:'100 0 0 0',
        //cmargins:'100 0 0 0',
        title: "",
        //split:true,
        autoScroll:true,
        containerScroll: true,
        width: 300,
        //collapsible: true,
        items: [{
            xtype:'fieldset',
            checkboxToggle:true,
            title: 'User Information',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            collapsed: true,
            items :[{
                    fieldLabel: 'First Name',
                    name: 'first',
                    allowBlank:false
                },{
                    fieldLabel: 'Last Name',
                    name: 'last'
                },{
                    fieldLabel: 'Company',
                    name: 'company'
                }, {
                    fieldLabel: 'Email',
                    name: 'email',
                    vtype:'email'
                }
            ]
        },{
            xtype:'fieldset',
            title: 'Phone Number',
            collapsible: true,
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            items :[{
                    fieldLabel: 'Home',
                    name: 'home',
                    value: '(888) 555-1212'
                },{
                    fieldLabel: 'Business',
                    name: 'business'
                },{
                    fieldLabel: 'Mobile',
                    name: 'mobile'
                },{
                    fieldLabel: 'Fax',
                    name: 'fax'
                }
            ]
        }],
        buttons: [{
            text: 'Filter'
        }]
    });

    /*
    function StatusBarManager(extjsStatusBar) {
        this.statusBar = extjsStatusBar;
        this.hashTable = new Hashtable();

        this.showStatus = function(message) {

            this.statusBar.setStatus({
                text: 'Finished',
                iconCls: 'ok-icon',
                clear: true
            });



            Math.random()
        }

        this.clearStatus = function() {

        }
    }        */

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
        map.setMapType(G_SATELLITE_MAP);

        //Thumbnail map
        var Tsize = new GSize(150, 150);
        map.addControl(new GOverviewMapControl(Tsize));

        map.addControl(new DragZoomControl(), new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(280, 5)));

        //var mgrOptions = { borderPadding: 50, maxZoom: 15, trackMarkers: true };
        //mgr = new MarkerManager(map, mgrOptions);
    }

    theglobalexml = new GeoXml("theglobalexml", map, null, null);

    //when a person clicks on a marker then do something
    GEvent.addListener(map, "click", function(overlay, latlng) {
        statusBar.showBusy();
        statusBar.setVisible(true);
        viewport.doLayout();

        if (overlay instanceof GMarker) {
            if (overlay.featureType == "gsml:Borehole") {
            	// FIXME overlay.getTitle() always returns the same value: "nvcl_core.1206"
                new NVCLMarker(overlay.getTitle(), overlay, overlay.description).getMarkerClickedFn()();
            }
            else if (overlay.featureType == "geodesy:stations") {
                new GeodesyMarker(overlay.wfsUrl, "geodesy:station_observations", overlay.getTitle(), overlay, overlay.description).getMarkerClickedFn()();
            }
            else if (overlay.description != null){
                overlay.openInfoWindowHtml(overlay.description);
            }

        }

        statusBar.clearStatus();
        statusBar.setVisible(false);
        viewport.doLayout();

    });
    
});





