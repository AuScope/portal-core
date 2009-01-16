/**
* @fileoverview This file declares the Class WebMapService.
* An object of this class will be created for each WMS to be displayed on the map.
*/

/**
* @class
* This class defines information to be stored for a WMS layer.
*
* @constructor
* @param {DomXmlNode} rootNode The XML root node for the document.
* @return A new {@link WebMapService}
*/
function WebMapService(rootNode, map) {
  if (!rootNode)
    return null;

  // Get the WMS Title and Abstract
  var sTitle = GXml.value(rootNode.selectSingleNode(".//*[local-name() = 'Title']"));
  var sDescription = GXml.value(rootNode.selectSingleNode(".//*[local-name() = 'Abstract']"));

  //var sTitle = GXml.value(SelectSingleNode(rootNode, ".//*[local-name() = 'Title']"));
  //var sDescription = GXml.value(SelectSingleNode(rootNode, ".//*[local-name() = 'Abstract']"));

  if (sTitle=="" || sDescription=="") {
    return null;
  }
  // Get the WMS URL
  // This will be the same as URL for getting the capabilities document,
  // but we should follow the right process and extract it from the capabilities document.
  var wmsNode = rootNode.selectSingleNode(".//*[local-name() = 'GetMap']//*[local-name() = 'OnlineResource']/@*[local-name() = 'href']");
  var wmsUrl = GXml.value(rootNode.selectSingleNode(".//*[local-name() = 'GetMap']//*[local-name() = 'OnlineResource']/@*[local-name() = 'href']"));

  //var wmsNode = SelectSingleNode(rootNode, ".//*[local-name() = 'GetMap']//*[local-name() = 'OnlineResource']/@*[local-name() = 'href']");
  //var wmsUrl = GXml.value(SelectSingleNode(rootNode, ".//*[local-name() = 'GetMap']//*[local-name() = 'OnlineResource']/@*[local-name() = 'href']"));


  //var wmsProxyUrl = getProxyMappingForServerUrl(wmsUrl);

  this.msTitle = sTitle;
  this.msDescription = sDescription;

  this.maLayerNames = new Array();
  this.maLayers = new Array();
  this.maTileLayers = new Array();
  this.lats = new Array();
  this.longs = new Array();

  // Extract all the layers belonging to this WMS
  var aLayers = rootNode.selectNodes(".//*[local-name() = 'Layer' and @queryable = '1']");
  var sLayers = "";

  for (var i=0; i<aLayers.length; i++) {

    // Get the XML node for each layer
    var layerNode = aLayers[i];

    // Extract name, title and abstract from the XML node.
    var layerName = GXml.value(layerNode.selectSingleNode(".//*[local-name() = 'Name']"));
    //var layerName = GXml.value(SelectSingleNode(layerNode, ".//*[local-name() = 'Name']"));

    // Sometimes layers are duplicated in the capabilities document
    // Avoid creating duplicate layer members.
    if (this.maLayerNames.indexOf(layerName) != -1) {
      continue;
    }
    var layerTitle = GXml.value(layerNode.selectSingleNode(".//*[local-name() = 'Title']"));
    var layerAbstract = GXml.value(layerNode.selectSingleNode(".//*[local-name() = 'Abstract']"));

    //var layerTitle = GXml.value(SelectSingleNode(layerNode, ".//*[local-name() = 'Title']"));
    //var layerAbstract = GXml.value(SelectSingleNode(layerNode, ".//*[local-name() = 'Abstract']"));

    // Create a WmsLayer object for each queryable layer belonging to this WMS.
    var layer = new WmsLayer(layerName, layerTitle, layerAbstract);

    // Create GTileLayer object for each layer belonging to this WMS
    // (refer to GMAP API reference)
    // This is the layer that is overlayed on the map.
    /*var tileLayer= new GTileLayer(new GCopyrightCollection(""),1,17);
    tileLayer.myLayers = layerName;
    tileLayer.myFormat = 'image/png';
    tileLayer.myBaseURL = wmsUrl;
    tileLayer.getTileUrl = getTileUrl;*/

    var tileLayer= new GWMSTileLayer(map, new GCopyrightCollection(""),1,17);
    tileLayer.baseURL = wmsUrl;
    tileLayer.layers = layerName;

    var tileLayerOverlay = new GTileLayerOverlay(tileLayer);

    // Save the tile overlay in the member array
    // The layer name is used as an index for this array.
    this.maLayerNames[this.maLayerNames.length] = layerName;
    this.maLayers[layerName] = layer;
    this.maTileLayers[layerName] = tileLayerOverlay;

    // The initial state for all layers is 0 (invisible)
    //this.listLayerVisible[layerName] = 0;

     var boundingBoxNode = layerNode.selectSingleNode(".//*[local-name() = 'LatLonBoundingBox']");
     var minx = parseFloat(boundingBoxNode.getAttribute("minx"));
     var maxx = parseFloat(boundingBoxNode.getAttribute("maxx"));
     var miny = parseFloat(boundingBoxNode.getAttribute("miny"));
     var maxy = parseFloat(boundingBoxNode.getAttribute("maxy"));

      var long = (minx + (maxx - minx) / 2.0);
      var lat = (miny + (maxy - miny) / 2.0);

      this.lats[layerName] = long;
    this.longs[layerName] = lat;


  }
  this.msWmsUrl = wmsUrl;
}


// Member Variables
/**
* WMS title.
* The title of the layer from the capabilities document.
* @type String
*/
WebMapService.prototype.msTitle = null;

/**
* WMS tooltip.
* The tooltip to be displayed on the WMS.
* It is derived from the description in the capabilities document.
* @type String
*/
WebMapService.prototype.msDescription = null;

/**
* The WMS URL for this service
* @type String
*/
WebMapService.prototype.msWmsUrl = null;

/**
* Array of the layer names belonging to this WMS
* @type Array
*/
WebMapService.prototype.maLayerNames = null;

/**
* Associative array of the WmsLayer object for
* each layer belonging to this WMS
* @type Array
*/
WebMapService.prototype.maLayers = null;

/**
* Associative array of the GTileLayerOverlay object for
* each layer belonging to this WMS
* @type Array
*/
WebMapService.prototype.maTileLayers = null;

// Member Functions
/**
* The assignment of function implementations for WebMapService
*/
WebMapService.prototype.getClickFn = WebMapService_getClickFn;
WebMapService.prototype.onClick = WebMapService_onClick;
WebMapService.prototype.addLayer = WebMapService_addLayer;
WebMapService.prototype.removeLayer = WebMapService_removeLayer;

/**
* Get the function for the onclick event of the WMS
* image and checkbox.
*/
function WebMapService_getClickFn(pLayerName) {
  var wms = this;
  var layerName = pLayerName;
  return function() {
    wms.onClick(layerName);
  }
}

/**
* The fucnction called with the checking and
* unchecking of the layers checkbox.
*/
function WebMapService_onClick(layerName) {
  var layer = this.maLayers[layerName];
  // Depending on the current visibility of the layer,
  // toggle the state.
  if (layer) {
    if (layer.mbIsVisible) {
      // The member variable msIsVisible is toggled in
      // the removeLayer or addLayer functions.
      this.removeLayer(layerName);
    } else {
      this.addLayer(layerName);
    }
  }
}
/**
* This function adds a layer to the current view
* @param {String} layerName The index name of the layer in the array
*/
function WebMapService_addLayer(layerName) {
  // Get the GTileLayerOverlay and WmsLayer objects for
  // the layer with the given name.
  var currentLayerOverlay = this.maTileLayers[layerName];
  var currentLayer = this.maLayers[layerName];

  if (currentLayer!=undefined && currentLayerOverlay!=undefined) {
    // Find the visibility status of the layer
    var isLayerVisible = currentLayer.mbIsVisible;

    // Add the layer to the map,
    // only if it is not already visible.
    if (isLayerVisible!=undefined && !isLayerVisible) {
  	  goMap.moMap.addOverlay(currentLayerOverlay);
  	  currentLayer.mbIsVisible = 1;
  	}
  }
}
/**
* This function removes a layer from the current view
* @param {String} layerName The index name of the layer in the array
*/
function WebMapService_removeLayer(layerName) {
  // Get the GTileLayerOverlay and WmsLayer objects for
  // the layer with the given name.
  var currentLayerOverlay = this.maTileLayers[layerName];
  var currentLayer = this.maLayers[layerName];

  if (currentLayer!=undefined && currentLayerOverlay!=undefined) {
    // Find the visibility status of the layer
    var isLayerVisible = currentLayer.mbIsVisible;

    // Remove the layer from the map,
    // only if it is visible.
    if (isLayerVisible!=undefined && isLayerVisible) {
  	  goMap.moMap.removeOverlay(currentLayerOverlay);
  	  currentLayer.mbIsVisible = 0;
  	}
  }
}


var WGS84_SEMI_MAJOR_AXIS = 6378137.0;
var WGS84_ECCENTRICITY = 0.0818191913108718138;

var DEG2RAD=0.0174532922519943;
var PI=3.14159267;

//Default image format, used if none is specified
var FORMAT_DEFAULT="image/png";

//Google Maps Zoom level at which we switch from Mercator to Lat/Long.
var MERC_ZOOM_DEFAULT = 15;


function dd2MercMetersLng(p_lng) {
 return WGS84_SEMI_MAJOR_AXIS * (p_lng*DEG2RAD);
}

function dd2MercMetersLat(p_lat) {
 var lat_rad = p_lat * DEG2RAD;
 return WGS84_SEMI_MAJOR_AXIS * Math.log(Math.tan((lat_rad + PI / 2) / 2) * Math.pow( ((1 - WGS84_ECCENTRICITY * Math.sin(lat_rad)) / (1 + WGS84_ECCENTRICITY * Math.sin(lat_rad))), (WGS84_ECCENTRICITY/2)));
}

function getTileUrl(a,b,c) {
  if (this.myMercZoomLevel == undefined) {
    this.myMercZoomLevel = MERC_ZOOM_DEFAULT;
  }

  if (this.myFormat == undefined) {
    this.myFormat = FORMAT_DEFAULT;
  }

  if (typeof(window['this.myStyles'])=="undefined") this.myStyles="";
  var lULP = new GPoint(a.x*256,(a.y+1)*256);
  var lLRP = new GPoint((a.x+1)*256,a.y*256);
  var lUL = G_NORMAL_MAP.getProjection().fromPixelToLatLng(lULP,b,c);
  var lLR = G_NORMAL_MAP.getProjection().fromPixelToLatLng(lLRP,b,c);

 // switch between Mercator and DD if merczoomlevel is set
 // NOTE -it is now safe to use Mercator exclusively for all zoom levels (if your WMS supports it)
 // so you can just use the two lines of code below the IF (& delete the ELSE)
 if (this.myMercZoomLevel!=0 && goMap.moMap.getZoom() < this.myMercZoomLevel) {
 var lBbox=dd2MercMetersLng(lUL.x)+","+dd2MercMetersLat(lUL.y)+","+dd2MercMetersLng(lLR.x)+","+dd2MercMetersLat(lLR.y);
 //Change for GeoServer - 41001 is mercator and installed by default.
 var lSRS="EPSG:54004";
 } else {
 var lBbox=lUL.x+","+lUL.y+","+lLR.x+","+lLR.y;
 var lSRS="EPSG:4326";
 }

 var lBbox=lUL.x+","+lUL.y+","+lLR.x+","+lLR.y;
 var lSRS="EPSG:4326";

 // dmr notes
 // change this to ensure some flexibility in version based on request from capabilities request
 //
 var lURL=this.myBaseURL;
 lURL+="&REQUEST=GetMap";
 lURL+="&SERVICE=WMS";
 lURL+="&VERSION=1.1.1";
 lURL+="&LAYERS="+this.myLayers;
 lURL+="&STYLES="+this.myStyles;
 lURL+="&FORMAT="+this.myFormat;
 lURL+="&BGCOLOR=0xFFFFFF";
 lURL+="&TRANSPARENT=TRUE";
 lURL+="&SRS="+lSRS;
 lURL+="&BBOX="+lBbox;
 lURL+="&WIDTH=256";
 lURL+="&HEIGHT=256";
 lURL+="&reaspect=false";
 //document.write(lURL + "<br/>")
 //alert(" url is " + lURL);
 return lURL;
}