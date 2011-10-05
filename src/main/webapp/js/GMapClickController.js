
//Returns true if the click has originated from a generic parser layer
var genericParserClickHandler = function (map, overlay, latlng, parentOnlineResource) {
    if (overlay === null || !overlay.description) {
        return false;
    }

    if (!parentOnlineResource) {
        return false;
    }

    //The generic parser stamps the description with a specific string followed by the gml:id of the node
    var genericParserString = 'GENERIC_PARSER:';

    if (overlay.description.indexOf(genericParserString) === 0) {

        //Lets extract the ID and then lookup the parent record
        //Assumption - We are only interested in the first WFS record
        var gmlID = overlay.description.substring(genericParserString.length);
        var wfsUrl = parentOnlineResource.url;
        var wfsTypeName = parentOnlineResource.name;

        //Open an info window with a simple HTML div. This will be the 'frame' that the window contents
        //will be rendered to (after it loads)
        var maxWidth = 600;
        var maxHeight = 300;
        var divId = 'generic-parser-popup';
        var html = '<html><body><div id="' + divId + '" style="width: ' + maxWidth + 'px; height: ' + maxHeight  +'px;"></div></body></html>';
        var loc = null;
        if (overlay instanceof GMarker) {
            loc = overlay;
        } else {
            loc = overlay.getBounds().getCenter();
        }
        var params = {
            divId : divId,
            maxWidth : maxWidth,
            maxHeight : maxHeight,
            wfsUrl : wfsUrl,
            wfsTypeName : wfsTypeName,
            gmlID : gmlID
        };
        var infoWindowParams = undefined;

        //When the window opens, render a generic parser Ext JS panel to the div
        mapInfoWindowManager.openInfoWindow(loc, html,infoWindowParams, function(map, loc, params) {

            //Also ensure that the user sees a loading icon instead of a blank popup
            var loadMask = new Ext.LoadMask(params.divId, {
                removeMask : true
            });
            loadMask.show();

            //Before we can render our component we need to have the WFS response
            var wfsParser = new GenericParser.WFSParser({
                wfsUrl : params.wfsUrl,
                typeName : params.wfsTypeName,
                featureId : params.gmlID,
                rootCfg : {
                    renderTo : params.divId, //This is crucial, it ensures we bind our parsed component to our empty div
                    width : params.maxWidth,
                    height : params.maxHeight,
                    autoScroll : true
                }
            });

            //Make the request and on response hide our loading / show any errors
            wfsParser.makeWFSRequest(function(wfsParser, rootCmp) {
                loadMask.hide();
                if (!rootCmp) {
                    Ext.MessageBox.show({
                        buttons : Ext.MessageBox.OK,
                        icon : Ext.MessageBox.WARNING,
                        msg : 'There was an error requesting information about this feature. Please try again later.',
                        title : 'Warning'
                    });
                }
            });
        }, params);

        return true;
    }

    return false;
};

/**
 * When someone clicks on the google maps we show popups specific to each
 * feature type/marker that is clicked on
 *
 * This event is fired when the user clicks on the map with the mouse. A
 * click event passes different arguments based on the context of the
 * click, and whether or not the click occured on a clickable overlay. If
 * the click does not occur on a clickable overlay, the overlay argument
 * is null and the latlng argument contains the geographical coordinates
 * of the point that was clicked. If the user clicks on an overlay that
 * is clickable (such as a GMarker, GPolygon, GPolyline, or GInfoWindow),
 * the overlay argument contains the overlay object, while the
 *
 * overlay. In addition, a click event is then also fired on the overlay
 * itself.
 *
 * @param {GMap2}
 * @param {GOverlay} overlay object (such as a GMarker, GPolygon, GPolyline, or GInfoWindow)
 * @param {GLatLng}  geographical coordinates
 * @param {Ext.data.Store}
 *
 * @version $Id$
 */
var gMapClickController = function(map, overlay, latlng, overlayLatlng, activeLayersStore) {

    //If the reports popup menu is showing, destroy it.
    var reportsMenu = Ext.getCmp('reportsMenu');
    if(reportsMenu) {
        reportsMenu.destroy();
    }

    //An instance of ActiveLayersRecord
    var parentActiveLayerRecord = null;
    if (overlay && overlay.activeLayerRecord) {
        parentActiveLayerRecord = new ActiveLayersRecord(overlay.activeLayerRecord);
    }

    var parentKnownLayer = null;
    if (parentActiveLayerRecord) {
        parentKnownLayer = parentActiveLayerRecord.getParentKnownLayer();
    }

    //an instance of CSWRecord
    var parentCSWRecord = null;
    if (overlay && overlay.cswRecord) {
        parentCSWRecord = new CSWRecord(overlay.cswRecord);
    }

    //an object as returned from CSWRecord.getOnlineResources()
    var parentOnlineResource = null;
    if (overlay) {
        parentOnlineResource = overlay.onlineResource;
    }

    //Try to handle a generic parser layer click
    if (genericParserClickHandler(map,overlay,latlng,parentOnlineResource)) {
        return;
    }

    //If a polygon or marker has been clicked, we will have a direct link to the parentActiveLayerRecord
    if (parentActiveLayerRecord) {
        //Handle for WFS services (if any)
        if (parentKnownLayer && parentKnownLayer.getType() === 'KnownLayerWFS') {
            var wfsTypeName = parentOnlineResource.name;
            var wfsUrl = parentOnlineResource.url;


            if (overlay instanceof GMarker) {
                //This is a really bad hack to split NVCL and PressureDB
                if (wfsTypeName === "gsml:Borehole" && wfsUrl.indexOf("pressuredb") >= 0) {
                    var infoWindow = new PressureDbInfoWindow(map,overlay, wfsUrl);
                    infoWindow.show();
                } else if (wfsTypeName === "gsml:Borehole") {
                    var infoWindow = new NvclInfoWindow(map,overlay, wfsUrl);
                    infoWindow.show();
                }
                else if (wfsTypeName == "ngcp:GnssStation") {
                    var marker = new GeodesyMarker(wfsUrl, "geodesy:station_observations", overlay.title, overlay, overlay.description);
                    var clickFn = marker.getMarkerClickedFn();
                    clickFn();
                }
                else if (wfsTypeName == "gsml:GeologicUnit"){
                    var featureId;
                    var geochemParserString = 'featureId:';
                    if (overlay.description.indexOf(geochemParserString) === 0){
                        var indexOfSpace=overlay.description.indexOf('<');
                        featureId = overlay.description.substring(geochemParserString.length,indexOfSpace);
                    }
                    var infoWindow = new YilgarnGeoInfoWindow(map,overlay,wfsUrl,featureId,wfsTypeName);
                    infoWindow.show();

                }
                else if (overlay.description !== null) {
                    overlay.openInfoWindowHtml(overlay.description, {maxWidth:800, maxHeight:600, autoScroll:true});
                }
            //Otherwise it could be a WFS polygon
            } else if (overlay.description !== null) {
                map.openInfoWindowHtml(overlay.getVertex(0),overlay.description);
            }
            return;
        }

        //otherwise Handle for WCS services (if any)
        cswRecords = parentActiveLayerRecord.getCSWRecordsWithType('WCS');
        if (cswRecords.length !== 0) {

            var infoWindow = new GenericWCSInfoWindow(map, overlay, parentOnlineResource.url, parentOnlineResource.name, parentCSWRecord);
            infoWindow.showInfoWindow();
            return;
        }

        //otherwise we treat the selection as a 'Report'
        //If a polygon is selected, let's iterate all nearby polygons and maybe we'll find some that
        //overlap in the clicked area (in which case we provide the user with a choice of what polygon they mean)
        if (overlay instanceof GPolygon) {

            //an array of objects containing the fields [text, cswRecord, overlay]
            var intersectingOverlays = [];

            //Iterate our active layers looking for polygons in the clicked area
            for (var i = 0; i < activeLayersStore.getCount(); i++) {
                var alr = activeLayersStore.getActiveLayerAt(i);
                var overlayManager = alr.getOverlayManager();

                for(var j = 0; j < overlayManager.overlayList.length; j++) {

                    var overlayToTest = overlayManager.overlayList[j];

                    //if this reportOverlay contains the clicked point, add it to the list
                    if(overlayToTest instanceof GPolygon && overlayToTest.Contains(overlayLatlng)) {

                        var cswRecord = new CSWRecord(overlayToTest.cswRecord);
                        var shortTitle = cswRecord.getServiceName();
                        if(shortTitle.length > 60) {
                            shortTitle = shortTitle.substr(0, 60) + "...";
                        }

                        intersectingOverlays.push({
                            text: shortTitle,
                            cswRecord : cswRecord,
                            overlay : overlayToTest
                        });
                    }
                }
            }

            //if a single overlay has been selected show the info window otherwise give user the choice
            if(intersectingOverlays.length == 1) {
                var repWin = new ReportsInfoWindow(map, intersectingOverlays[0].overlay, intersectingOverlays[0].cswRecord);
                repWin.show();
            } else if(intersectingOverlays.length > 1) { //otherwise show the reports context menu

                //TODO: Shouldn't need this here but for some unknown reason we do...
                //If the reports popup menu is showing, destroy it.
                var reportsMenu = Ext.getCmp('reportsMenu');
                if(reportsMenu) {
                    reportsMenu.destroy();
                }

                var menuItems = [];
                for (var i = 0; i < intersectingOverlays.length; i++) {
                    menuItems.push({
                        text : intersectingOverlays[i].text,
                        scope : intersectingOverlays[i],
                        handler : function() {
                            var repWin = new ReportsInfoWindow(map, this.overlay, this.cswRecord);
                            repWin.show();
                        }
                    })
                }

                reportsMenu = new Ext.menu.Menu({
                    id: 'reportsMenu',
                    showSeparator: false,
                    boxMaxHeight: 200,
                    autoScroll: true,
                    enableScrolling: true,
                    items: menuItems
                });

                var pixel = map.fromLatLngToContainerPixel(overlayLatlng);
                var container = Ext.getCmp('center_region');
                reportsMenu.showAt([container.x + pixel.x, container.y + pixel.y]);
                reportsMenu.syncSize();
            }

        } else if (overlay instanceof GMarker) {
            var infoWindow = new ReportsInfoWindow(map, overlay, parentCSWRecord);
            infoWindow.show();
        }

    //Otherwise we test each of our WMS layers to see if a click will affect them
    }else {
        //If the user clicks on an info window, we will still get click events, lets ignore these
        if (latlng === null || latlng === undefined) {
            return;
        }

        //We will need to iterate over every WMS to see if they indicate a click event
        for (var i = 0; i < activeLayersStore.getCount(); i++) {
            var alr = new ActiveLayersRecord(activeLayersPanel.getStore().getAt(i));

            if (!alr.getLayerVisible()) {
                continue;
            }

            var wcsCSWRecords = alr.getCSWRecordsWithType('WCS');
            if(wcsCSWRecords.length !== 0){
                continue;
            }

            //each linked WMS record must be tested
            var wmsCSWRecords = alr.getCSWRecordsWithType('WMS');
            for (var j = 0; j < wmsCSWRecords.length; j++) {
                var wmsOnlineResources = wmsCSWRecords[j].getFilteredOnlineResources('WMS');

                for (var k = 0; k < wmsOnlineResources.length; k++) {
                    map.getDragObject().setDraggableCursor("pointer");

                    var TileUtl = new Tile(map,latlng);

                    var wmsOnlineResource = wmsOnlineResources[k];
                    var typeName = wmsOnlineResource.name;
                    var serviceUrl = wmsOnlineResources[k].url;

                    var url = "wmsMarkerPopup.do";
                    url += "?WMS_URL=" + serviceUrl;
                    if( serviceUrl.substr(-1) !== "&" ) {
                        url += '&';
                    }
                    url += "lat=" + latlng.lat();
                    url += "&lng=" + latlng.lng();
                    url += "&QUERY_LAYERS=" + typeName;
                    url += "&x=" + TileUtl.getTilePoint().x;
                    url += "&y=" + TileUtl.getTilePoint().y;
                    url += '&BBOX=' + TileUtl.getTileCoordinates();
                    url += '&WIDTH=' + TileUtl.getTileWidth();
                    url += '&HEIGHT=' + TileUtl.getTileHeight();

                    if(typeName.substring(0, typeName.indexOf(":")) == "gt") {
                        handleGeotransectWmsRecord(url, wmsCSWRecords[j], wmsOnlineResource, map, latlng);
                    } else {
                        handleGenericWmsRecord(url, typeName, map, latlng);
                    }
                }
            }
        }
    }
};

/**
 * Request json data from url, process response and open a GeotransectsInfoWindow
 * to present the data.
 *
 * @param url
 * @param activeLayersStore
 * @param map
 * @param latlng
 */
function handleGeotransectWmsRecord(url, cswRecord, wmsOnlineResource, map, latlng) {

    Ext.Ajax.request({
        url: url+"&INFO_FORMAT=application/vnd.ogc.gml",
        timeout		: 180000,
        wmsOnlineResource : wmsOnlineResource,
        cswRecord : cswRecord,
        success: function(response, options) {
            if (isGmlDataThere(response.responseText)) {

                //Parse the response
                var XmlDoc = GXml.parse(response.responseText);
                if (g_IsIE) {
                  XmlDoc.setProperty("SelectionLanguage", "XPath");
                }
                var rootNode = XmlDoc.documentElement;
                if (!rootNode) {
                  return;
                }

                var schemaLoc = rootNode.getAttribute("xsi:schemaLocation");

                var reqTypeName = schemaLoc.substring(schemaLoc.indexOf("typeName")+9,
                        schemaLoc.indexOf(' ', schemaLoc.indexOf("typeName")+9));
                //Browser may have replaced certain characters
                reqTypeName = reqTypeName.replace("%3A", ":");

                //Extract the line Id from the XML
                var line = rootNode.getElementsByTagName("gt:LINE");
                if(line === null || line.length <= 0) {
                    //Chrome, Opera may not want the namespace prefix
                    line = rootNode.getElementsByTagName("LINE");
                }

                // Change to enable the SURV_LINE which is the key in the shapefile
                if(line === null || line.length <= 0) {
                    line = rootNode.getElementsByTagName("gt:SURV_LINE");
                    if(line == null || line.length <= 0) {
                        line = rootNode.getElementsByTagName("SURV_LINE");
                    }
                }

                //Get the line
                var lineId = "";
                if(line !== null && line.length > 0) {
                    if(document.all) { //IE
                        lineId = line[0].text;
                    } else {
                        lineId = line[0].textContent;
                    }

                    //Remove the prefixes - we dont store them in the DB
                    if(lineId.indexOf("cdp") === 0) {
                        lineId = lineId.substring(3, lineId.length);
                    }

                    var infoWindow = new GeotransectsInfoWindow(latlng, map, lineId, options.cswRecord, options.wmsOnlineResource, url);
                    infoWindow.show();
                } else {
                    alert("Remote server returned an unsupported response.");
                }

            }
        },
        failure: function(response, options) {
            Ext.Msg.alert('Error requesting data', 'Error (' +
                    response.status + '): ' + response.statusText);
        }
    });

}
/**
 * Request html data from the url and open an info window to present the data.
 *
 * @param url
 * @param map
 * @param latlng
 */
function handleGenericWmsRecord(url, i, map, latlng) {

     url += "&INFO_FORMAT=text/html";
     Ext.Ajax.request({
         url: url,
         timeout		: 180000,
        success: function(response, options) {
            if (isHtmlDataThere(response.responseText)) {
                if (isHtmlPage(response.responseText)) {
                    var openWindow = window.open('','new'+i+'window');
                    if (openWindow) {
                        openWindow.document.write(response.responseText);
                        openWindow.document.close();
                    } else {
                        alert("Couldn't open popup window containing WMS information. Please disable any popup blockers and try again");
                    }
                } else {
                    map.openInfoWindowHtml(latlng, response.responseText, {autoScroll:true});
                }
            }
        },
        failure: function(response, options) {
            Ext.Msg.alert('Error requesting data', 'Error (' +
                    response.status + '): ' + response.statusText);
        }
    });
}

/**
 * Returns true if the WMS GetFeatureInfo query returns valid gml data
 * describing a feature. Verifies this by ensuring the gml contains at least
 * 1 featureMember.
 *
 * @param iStr GML string content to be verified
 * @return true if the WMS GetFeatureInfo query returns valid gml data.
 */
function isGmlDataThere(iStr) {
    var lowerCase = iStr.toLowerCase();
    return lowerCase.indexOf('<gml:featuremember>') > 0;
}

/**
 * Returns true if WMS GetFeatureInfo query returns data.
 *
 * We need to hack a bit here as there is not much that we can check for.
 * For example the data does not have to come in tabular format.
 * In addition html does not have to be well formed.
 * In addition an "empty" click can still send style information
 *
 * So ... we will assume that minimum html must be longer then 30 chars
 * eg. data string: <table border="1"></table>
 *
 * For a bit of safety lets only count the bytes in the body tag
 *
 * @param {iStr} HTML string content to be verified
 * @return {Boolean} Status of the
 */
function isHtmlDataThere(iStr) {
    //This isn't perfect and can technically fail
    //but it is "good enough" unless you want to start going mental with the checking
    var lowerCase = iStr.toLowerCase();

    //If we have something resembling well formed HTML,
    //We can test for the amount of data between the body tags
    var startIndex = lowerCase.indexOf('<body>');
    var endIndex = lowerCase.indexOf('</body>');
    if (startIndex >= 0 || endIndex >= 0) {
        return ((endIndex - startIndex) > 32);
    }

    //otherwise it's likely we've just been sent the contents of the body
    return lowerCase.length > 32;
}

/**
 * Returns true if WMS GetFeatureInfo query returns content
 * within html page markup.
 *
 * @param {iStr} HTML string content to be verified
 * @return {Boolean}
 */
function isHtmlPage(iStr) {
    return (iStr.toLowerCase().indexOf('<body') !=-1);
}