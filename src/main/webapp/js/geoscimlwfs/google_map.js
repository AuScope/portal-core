/** 
* @fileoverview This file declares the Class Map.
* An object of this class will be created and referenced by the goMap variable.
*/

/**
* @class 
* This class defines information about the map client that is used for displaying 
* station markers and other WFS information.
* Only one object of this class should be instantiated in the application.
* 
* @constructor
* @param {string} pMapContainer document div id in which the map has to be laid.
*/

function Map(pMapContainer) {

  /**
  * Default properties of the map.
  * Note this member will not be updated during map movements.
  * I am breaking the rules of strict OOP here. 
  * These members could have been private,
  * but I would rather keep them public and avoid one extra 'get' function call.
  */
  this.mnZoomLevel = DEFAULT_MAP_ZOOM_LEVEL;
  this.mnMaxZoomLevel = MAX_ZOOM_LEVEL;
  										   
  this.moCenter = new GLatLng(DEFAULT_MAP_CENTRE["lat"], DEFAULT_MAP_CENTRE["lon"]);					 
  
  // Instance of Google Map.
  this.moMap = new GMap2(document.getElementById(pMapContainer));
  
  // Add an overview map control 
  this.moMap.addControl(this.moOverviewMapControl = new GOverviewMapControl());
  
  // Add the Bounding Box control to the map
  this.moMap.addControl(this.moDragZoomControl = new GZoomControl(
                              { nOpacity: .2
                              },
                              {
                                sButtonHTML: "Bounding Box",                           
                                oButtonStartingStyle:{width:'68px',height:'11px',border:'1px dotted #3366CC',padding:'0px 2px 3px 5px',color:'#3366CC',
                                sButtonZoomingHTML:"Drag Box"}
                              },
                              {
                                dragging: this.zoomboxDragging
			    		      }));
  this.moMap.addControl(new GSmallMapControl(), new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(25,40)));
  this.moMap.addControl(new GMapTypeControl(), new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(10,10)));

  // The map must be centered before setting the mapType   
  this.moMap.setCenter(this.moCenter, this.mnZoomLevel);
    
  // Update Latitude/Longitude with mouse move
  this.moMoveListener = GEvent.addListener(this.moMap, "mousemove", this.mouseMove);
  // Update the map type of the overlay map based on the main map
  this.moMapTypeChangedListener = GEvent.addListener(this.moMap, "maptypechanged", this.mapTypeChanged);
  
}

/**
* Initial zoom level.
* Set as DEFAULT_MAP_ZOOM_LEVEL
* @type Integer
*/
Map.prototype.mnZoomLevel = 0;

/**
* Maximum zoom level.
* Set as MAX_ZOOM_LEVEL
* @type Integer
*/
Map.prototype.mnMaxZoomLevel = 0;

/**
* Initial centre of the map.
* Set as DEFAULT_MAP_CENTRE
* @type GLatLng
*/
Map.prototype.moCenter = 0;

/**
* Google map object
* @type GMap2
*/
Map.prototype.moMap = 0;

/**
* Control for map overview.
* Displayed in the bottom right corner of the map.
* @type GOverviewMapControl
*/
Map.prototype.moOverviewMapControl = 0;

/**
* Control for switching between Bounding Box and Drag Box
* Displayed in the top left corner of the map
* @type GZoomControl
*/
Map.prototype.moDragZoomControl = 0;

/**
* Listener for the mouse move event on the map.
* The function registered with this listener will be used to update Lat/Lng information
* @type GEventListener
*/
Map.prototype.moMoveListener = 0;

/**
* Listener for the map type changed event on the map.
* The function registered with this listener will be used to 
* update the map type of the overview map
* This seems like updateCSWRecords bug in the Google code ->
* the map type of the overlay map is not initially set to the map type of the main map.
* @type GEventListener
*/
Map.prototype.moMapTypeChangedListener = 0;

/**
* The assignment of function implementations for Map
*/
Map.zoomIn = Map_zoomInAtPoint;

Map.zoomOut = Map_zoomOutAtPoint;

Map.prototype.mouseMove = Map_mouseMove;

Map.prototype.mapTypeChanged = Map_mapTypeChanged;

Map.prototype.zoomboxDragging = Map_zoomboxDragging;

/**
* Not to be used
*/
/**
Map.prototype.addWmsOverlay = Map_addWmsOverlay;

Map.prototype.getTileUrl = Map_getTileUrl;

Map.prototype.addWfsOverlay = Map_addWfsOverlay;

Map.prototype.click = Map_click;
*/

/**
* Zoom in on the map and set centre as the given point
* Registered with the "Zoom In" link on the marker popups.
* Ideally this should not be updateCSWRecords class function.
* But there are issues with getting the map object at the time of 
* invocation of this function.
* I could have made this an instance method and invoked it as goMap.zoomInAtPoint
* Its another ugly way of doing the same thing.
*/
function Map_zoomInAtPoint(pLng, pLat) {
  
  goMap.moMap.setZoom(goMap.moMap.getZoom()+1);
  //goMap.moMap.setCenter(new GLatLng(pLat, pLng), goMap.moMap.getZoom()+1);
}

/**
* Zoom out on the map and set centre as the given point.
* Registered with the "Zoom Out" link on the marker popups.
* TODO Make updateCSWRecords function for updating zoomLevel string
*/
function Map_zoomOutAtPoint(pLng, pLat) {
  goMap.moMap.setZoom(goMap.moMap.getZoom()-1);
  //goMap.moMap.setCenter(new GLatLng(pLat, pLng), goMap.moMap.getZoom()-1);
}

/**
* Update the Latitude Longitude values based on mouse move
* Registered with the moMoveListener (mouse move event)
* @param (GLatLng} pLatLng The new position of the mouse in Lat/Lng
*/
function Map_mouseMove (pLatLng) {
  
  if (pLatLng) {
    // Update the cursor coordinates.
    var  pMouseCursorLat = document.getElementById(HTML_INPUT_LAT);
    var  pMouseCursorLng = document.getElementById(HTML_INPUT_LNG);
  
    // Create updateCSWRecords string for the Lat/Lng
    var sLat = pLatLng.lat().toFixed(4);
    var sLng = pLatLng.lng().toFixed(4);
    pMouseCursorLat.value = sLat;
    pMouseCursorLng.value = sLng;
  }
}

/**
* Update the map type of the overlay map based on the map type of the main map
*/
function Map_mapTypeChanged () {
  goMap.moOverviewMapControl.setMapType(goMap.moMap.getCurrentMapType());
}
/**
* Update the Boundary box Lat/Lng while it is being dragged
* This is registered with the _dragging event of GZoomControl
*/
function Map_zoomboxDragging(ne, sw){
  // Get HtTML elements
  var SWLat = document.getElementById(HTML_INPUT_SW_LAT);
  var SWLng = document.getElementById(HTML_INPUT_SW_LNG);
  var NELat = document.getElementById(HTML_INPUT_NE_LAT);
  var NELng = document.getElementById(HTML_INPUT_NE_LNG);

  // Update values for the Box Coordinate text boxes.
  SWLat.value = sw.lat().toFixed(4);
  SWLng.value = sw.lng().toFixed(4);  		
  NELat.value = ne.lat().toFixed(4);
  NELng.value = ne.lng().toFixed(4); 
}

/**
* Crap code.
* Not to be used.
* Retaining for later reference
*/

/**
function Map_addWfsOverlay() {
  baseURL = "http://apacsrv7.arrc.csiro.au:80/geodesy/wfs?";
  typename = "geodesy:station_observations";
  buffer = 0;
  proxy = "/servlet/proxy";
	
  wfs = new GeodesyWFS(this.moMap, baseURL, typename, buffer, proxy, this.moBaseIcon);
  wfs.refreshInBoundsWithBuffer();
}

function Map_addWmsOverlay() {
  this.geodesyStationsTileLayer = new GTileLayer(new GCopyrightCollection(""),1, this.mnMaxZoomLevel-1);
  
  this.geodesyStationsTileLayer.baseUrl = "http://apacsrv7.arrc.csiro.au/geodesy/wms?";
  this.geodesyStationsTileLayer.layerName = "geodesytest:station_observations";
  
  this.geodesyStationsTileLayer.getTileUrl = Map_getTileUrl;
  this.geodesyStationsTileLayer.getOpacity = function() { return 0.7; }
  this.geodesyStationsTileLayer.isPng = function() {return true; } 
 
  this.geodesyStationsOverlay = new GTileLayerOverlay(this.geodesyStationsTileLayer); 
  
  //this.moMap.addOverlay(this.geodesyStationsOverlay);
}
  
function Map_getTileUrl(pTile, pZoom) {
  // max zoom plus 1
  var projection = new GMercatorProjection(18);
    
  var x = pTile.x;
  var y = pTile.y;
    
  var nTileSize = 256;

  // Four vertices in pixel in GPoint coordinates
  var p1 = new GPoint(x*nTileSize, y*nTileSize);
  var p2 = new GPoint(p1.x+nTileSize,p1.y+nTileSize);

  // latitude/longitude of four vertices in decimal degree
  var latlng1 = projection.fromPixelToLatLng(p1,pZoom);
  var latlng2 = projection.fromPixelToLatLng(p2,pZoom);

  var lat1 = latlng1.lat();
  var lon1 = latlng1.lng();
  var lat2 = latlng2.lat();
  var lon2 = latlng2.lng();

  var bbox = lon1 + "," + lat2 + "," + lon2 + "," + lat1;

  // Build updateCSWRecords request for WMS
  var url = this.baseUrl;
  url+="request=GetMap";
  url+="&layers=" + this.layerName;
  url+="&srs=EPSG:4326";
  url+="&format=image/png";
  url+="&BBOX=" + bbox;
  url+="&width=" + nTileSize;
  url+="&height=" + nTileSize;
  url+="&styles=";
  url+="&transparent=true"; 

  //var url = "http://apacsrv7.arrc.csiro.au:80/geodesy/wfs?request=GetFeature&typeName=geodesytest:station_observations&propertyName=STATE_NAME,PERSONS&BBOX=" + bbox;
  return url
}

 function Map_click(overlay, pLatLng){
   if(overlay) return;
		
  var oProjection = new GMercatorProjection(18);
  var oGPoint = oProjection.fromLatLngToPixel(pLatLng,  this.getZoom());
  var x = oGPoint.x;
  var y = oGPoint.y;
    
  var nMapSize = this.getSize();
  var oMapBounds = this.getBounds();
  var oSouthWest = oMapBounds.getSouthWest();
  var oNorthEast = oMapBounds.getNorthEast();
  var w = oSouthWest.lng();
  var s = oSouthWest.lat();
  var e = oNorthEast.lng();
  var n = oNorthEast.lat();

  var nWidth = nMapSize.width;
  var nHeight = nMapSize.height;
  
		var label1 = 'Main';
		var label2 = 'Details';
		var html1 = "";
		var html2 = "You clicked on <br>" + pLatLng;

		var URL = GetURL(x, y, w, s, e, n, nWidth, nHeight);

		html1 += '<updateCSWRecords href="javascript:zoomIN('+pLatLng+');">Zoom In</updateCSWRecords>&nbsp;|&nbsp;<updateCSWRecords href="javascript:zoomOUT('+pLatLng+');">Zoom Out</updateCSWRecords><br><br>';
		html1 += "<iframe style=\"width:275px;height:175px\" src=\"";
		html1 += URL;
		html1 += "\" ></iframe>";

//		map.openInfoWindowHtml(pt, htm);
		this.openInfoWindowTabsHtml(pLatLng, [new GInfoWindowTab(label1,html1), new GInfoWindowTab(label2,html2)])

	}


	function GetURL(x, y, w, s, e, n, width, height){
		query = "/servlet/proxy?";
		query+="&SERVICE=WMS";
		query+="&SRS=EPSG:4326";
		query+="&VERSION=1.1.1";
		query+="&REQUEST=GetFeatureInfo";
		query+="&X=" + parseInt(x);
		query+="&Y=" + parseInt(y);
		query+="&QUERY_LAYERS=geodesytest:station_observations";
		query+="&LAYERS=geodesytest:station_observations";
		query+="&FORMAT=text/html";
		query+="&BBOX="+w+","+s+","+e+","+n;
		query+="&WIDTH="+parseInt(width)+"&HEIGHT="+ parseInt(height);
		query+="&STYLES=";
		return query;
	}
*/