var g_IsIE = !!(window.attachEvent && !window.opera);

// check for XPath implementation 
// Implement selectNodes and selectSingleNode for firefox
if (!g_IsIE && !Element.prototype.selectSingleNode)
{
	Element.prototype.selectSingleNode = function(sXPath) {
		var oEvaluator = new XPathEvaluator();
		var oResult = oEvaluator.evaluate(sXPath, this, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
		return oResult == null ? null : oResult.singleNodeValue;
	}
}

// check for XPath implementation 
if( document.implementation.hasFeature("XPath", "3.0") ) { 
  // prototying the XMLDocument 
  XMLDocument.prototype.selectNodes = function(cXPathString, xNode) { 
    if( !xNode ) { xNode = this; }
    var oNSResolver = this.createNSResolver(this.documentElement) 
    var aItems = this.evaluate(cXPathString, xNode, oNSResolver,
    XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null) 
    var aResult = [];
    for( var i = 0; i < aItems.snapshotLength; i++) { 
      aResult[i] = aItems.snapshotItem(i);
    } 
    return aResult;
  } 

  // prototying the Element 
  Element.prototype.selectNodes = function(cXPathString) { 
    if(this.ownerDocument.selectNodes) { 
      return this.ownerDocument.selectNodes(cXPathString, this);
    } else {
      throw "For XML Elements Only";
    } 
  } 
} 

//window.onresize = resizeMapDiv;

/**
* This function resizes the "map" div,
* containing the google map,
* according to the client's window size 
*/
function resizeMapDiv() {

  // Get client's window size
  var windowSize = getWindowSize();
  if(windowSize && windowSize.height && windowSize.width) {
    var mapDiv = document.getElementById("map");
    
    // Set the map size accordingly.
    // Currently this percentage works well with all the portal headers etc.
    var width = windowSize.width * .75 ;
    var height = windowSize.height * .63;
    
    // We don't want the height or width to be lower than certain pixels
    // This is to accomodate for the right controls
    // and bottom controls on the portlet
    if (height < 410) {
      height = 410;
    }
    if (width < 805) {
      width = 805;
    }
    
    mapDiv.style.width =  width + 'px';
    mapDiv.style.height = height + 'px';
  }
}

/**
* This function gets the cleint's window size 
*/
function getWindowSize(){
  var e = new Object();
  // Handle cases for different browsers
  if(window.self && self.innerWidth){
    e.width = self.innerWidth;
    e.height = self.innerHeight;
  } else if(document.documentElement && document.documentElement.clientHeight) {
    e.width = document.documentElement.clientWidth;
    e.height = document.documentElement.clientHeight;
  } else {
    e.width = document.body.clientWidth;
    e.height = document.body.clientHeight;
  }
  return e
}

/**
* This function initializes all the global objects for the application
* and calls the getCapabilities function to parse capabilities documents
*/
function initialize(pMapContainer) {

  /*// Set the initial size of the "map" div
  // according to the client's window
  resizeMapDiv();

  // Google Map initialized
  goMap = new Map(pMapContainer);
  
  // Set the map type of the map to Satellite
  goMap.moMap.setMapType(G_SATELLITE_MAP);

  // Parse capabilities documents for different services.
  getCapabilities();
  
  // addLayers();*/
}

/**
* This is the main function which is called from the initialize function.
* It parses the capabilities document to get the GetFeature request for different services
*
* NOTE: This function is not used ...?
*/
function getCapabilities() {

    // NVCL Get Capabilities url
  var url = "" //"http://auscope-portal.arrc.csiro.au/nvcl/wfs?request=GetCapabilities&version=1.0.0";

  GDownloadUrl(ProxyURL+url, function(pData, pResponseCode) {
    if (pResponseCode == 200) {
      var xmlDoc = GXml.parse(pData);
      parseCapabilitiesDocument(xmlDoc);
    }else if(responseCode == -1) {
        alert("Data request timed out. Please try later.");
    } else if ((responseCode >= 400) & (responseCode < 500)){
        alert('Request not found, bad request or similar problem. Error code is: ' + responseCode);
    } else if ((responseCode >= 500) & (responseCode <= 506)){
        alert('Requested service not available, not implemented or internal service error. Error code is: ' + responseCode);
    }else {
        alert('Remote server returned error code: ' + responseCode);
    }
  });

  // Geodesy Get Capabilities url
  var url = "" //"http://auscope-portal.arrc.csiro.au/geodesy/wfs?request=GetCapabilities";
  GDownloadUrl(ProxyURL+url, function(pData, pResponseCode) {
    if (pResponseCode == 200) {
      var xmlDoc = GXml.parse(pData);
      parseCapabilitiesDocument(xmlDoc);
    }else if(responseCode == -1) {
        alert("Data request timed out. Please try later.");
    } else if ((responseCode >= 400) & (responseCode < 500)){
        alert('Request not found, bad request or similar problem. Error code is: ' + responseCode);
    } else if ((responseCode >= 500) & (responseCode <= 506)){
        alert('Requested service not available, not implemented or internal service error. Error code is: ' + responseCode);
    }else {
        alert('Remote server returned error code: ' + responseCode);
    }
  });

  /*
  // GNSS Get Capabilities url
  var url = top.location.protocol + "//" + top.location.host + "/geodesyworkflow/gnss/proxy?request=GetCapabilities&version=1.0.0";
  GDownloadUrl(url, function(pData, pResponseCode) {
    if (pResponseCode == 200) {
      var xmlDoc = GXml.parse(pData);
      parseCapabilitiesDocument(xmlDoc);
    }
  });

  // GNSS Get Capabilities url
  var url = top.location.protocol + "//" + top.location.host + "/geodesyworkflow/ga/sentinel/proxy?request=GetCapabilities&version=1.0.0";
  GDownloadUrl(url, function(pData, pResponseCode) {
    if (pResponseCode == 200) {
      var xmlDoc = GXml.parse(pData);
      parseCapabilitiesDocument(xmlDoc);
    }
  });    */
}

/**
* The application support capabilities documents version 1.0.0 and 1.1.0
*/
function parseCapabilitiesDocument(xmlDoc) {
  if (g_IsIE)
    xmlDoc.setProperty("SelectionLanguage", "XPath");
  
  // Parse the XML for "stations" or "geodesy:stations"
  var rootNode = xmlDoc.documentElement;

  if (!rootNode) 
    return;
  
  // Get wfs version implemented by this service
  var sWfsVersion = rootNode.getAttribute("version");
  if (sWfsVersion == "1.0.0") {
    parseCapabilitiesDocument1_0_0(rootNode);
  } else if (sWfsVersion == "1.1.0") {
    parseCapabilitiesDocument1_1_0(rootNode);
  }
}

/**
* Function to parse updateCSWRecords capabilities which implements wfs version 1.0.0
*/
function parseCapabilitiesDocument1_1_0(rootNode) {
  if (g_IsIE)
    rootNode.setProperty("SelectionLanguage", "XPath");
    
  var oServiceIdNode = rootNode.selectSingleNode("*[local-name() = 'ServiceIdentification']");
  if (!oServiceIdNode)
    return;
    
  var sTitle = GXml.value(oServiceIdNode.selectSingleNode(".//*[local-name() = 'Title']"));
  var sDescription = GXml.value(oServiceIdNode.selectSingleNode(".//*[local-name() = 'Abstract']"));
  
  // GetFeature node
  var oGetFeatureNode = rootNode.selectSingleNode(".//*[local-name() = 'Operation' and @name = 'GetFeature']");
  var sWfsNode = GXml.value(oGetFeatureNode.selectSingleNode(".//*[local-name() = 'Post']"));
  
  // FeatureType node 
  var aFeatureType = rootNode.selectNodes(".//*[local-name() = 'FeatureType']");
  var numFeatureTypes = aFeatureType.length;
  for (var i=0; i<numFeatureTypes; i++) {
    var oFeatureType = aFeatureType[i];
    var sFeatureType = GXml.value(oFeatureType.selectSingleNode(".//*[local-name() = 'Name']"));
    
    if (!isRecognizedFeature(sFeatureType)) {
      continue;
    }
        
    // Icons for the feature type
    // This should somehow come from the WFS - DescribeFeatureType maybe
    var sIcon = gaFeatureTypeIconOn[sFeatureType];    
 
    // TODO - Somehow we need to get the proxy for this service
    var sWfs = gaFeatureTypeProxy[sFeatureType];
    
    var group = new StationGroup(sIcon, sTitle, sDescription, sWfs, sFeatureType);
    gaGroups[sFeatureType] = group;
    
    var groupDiv = document.getElementById(gaFeatureTypeGroupSpace[sFeatureType]);
    groupDiv.appendChild(group.moHtmlBundle);
  }
}

/**
* Function to parse updateCSWRecords capabilities which implements wfs version 1.1.0
*/
function parseCapabilitiesDocument1_0_0(rootNode) {
  if (g_IsIE)
    rootNode.setProperty("SelectionLanguage", "XPath");
    
  var oServiceIdNode = rootNode.selectSingleNode("*[local-name() = 'Service']");
  if (!oServiceIdNode)
    return;
    
  var sTitle = GXml.value(oServiceIdNode.selectSingleNode(".//*[local-name() = 'Title']"));
  var sDescription = GXml.value(oServiceIdNode.selectSingleNode(".//*[local-name() = 'Abstract']"));
  
  // GetFeature node
  var oCapabilityNode = rootNode.selectSingleNode(".//*[local-name() = 'Capability']");
  var oGetFeatureNode = oCapabilityNode.selectSingleNode(".//*[local-name() = 'GetFeature']");
  var sWfsNode = GXml.value(oGetFeatureNode.selectSingleNode(".//*[local-name() = 'Post']"));

  // FeatureType node 
  var aFeatureType = rootNode.selectNodes(".//*[local-name() = 'FeatureType']");
  var numFeatureTypes = aFeatureType.length;
  for (var i=0; i<numFeatureTypes; i++) {
    var oFeatureType = aFeatureType[i];
    var sFeatureType = GXml.value(oFeatureType.selectSingleNode(".//*[local-name() = 'Name']"));
    
    if (!isRecognizedFeature(sFeatureType)) {
      continue;
    }

    // Icons for the feature type
    // This should somehow come from the WFS - DescribeFeatureType maybe
    var sIcon = gaFeatureTypeIconOn[sFeatureType];   
 
    // TODO - Somehow we need to get the proxy for this service
    var sWfs = gaFeatureTypeProxy[sFeatureType];

    var group = new StationGroup(sIcon, sTitle, sDescription, sWfs, sFeatureType);
    gaGroups[sFeatureType] = group;
        
    //var groupDiv = document.getElementById(gaFeatureTypeGroupSpace[sFeatureType]);
    //groupDiv.appendChild(group.moHtmlBundle);
  }
}

/*
* This function gets all the data urls checked across all categories, groups and stations.
* It creates an xml document of the format
* <dst:data>
*   <dst:url_date>
*     <dst:date>
*       2008-01-31
*     <dst:date>
*     <dst:url>
*        http://srb.ivec.org/gpsdata/08061/alic0610.08o.Z
*     <dst:url>
*   </dst:url_date>
* </dst:data>  
*/
function getXmlTextForAllCheckedDataUrls() {

  // Variables to create the xml text that would be converted to an XML doc
  // to be passed onto the DST portlet
  var xmlText = "";
  var xmlNamespace = "dst";
  var xmlRoot = xmlNamespace + ":data";

  
  var group;
  var num_stations;
  var station;
  var year;
  var month;
  var num_urls;
  var selected_urls = "";
  var date_str = "";
  xmlText += "<" + xmlRoot + ">";
  
  // Loop over all groups in geodesy
  var group = gaGroups["geodesy:stations"];
  if (group) {
    num_stations = group.maMarkers.length;
    // Loop over all the stations in the group
    for (var station_index=0; station_index<num_stations; station_index++) {
      station = group.maMarkers[station_index];
      // Loop over all years for the station
      for (var year_index=0; year_index<gaYears.length; year_index++) {
        year = gaYears[year_index];
        if (station.maStationDataForDate[year] != undefined) {
          // Loop over all months in the year
          for (var month_index=1; month_index<=12; month_index++) {
            month = gaMonths[month_index];
            if (station.maStationDataForDate[year][month] 
                && station.maStationDataForDate[year][month].length!=0) {
              // Loop over all dates for the month
              for (var date=1; date<=31; date++) {
                if (station.maStationDataForDate[year][month][date] 
                    && station.maStationDataForDate[year][month][date].length!=0) {
                  // If data is avaialable for this date,
                  // look for the renix urls that had been "checked" by the user
                  // using the station popup windows.
                  var num_urls = station.maStationDataForDate[year][month][date].length;
                  for (var url_index=0; url_index<num_urls; url_index++) {
                    if (station.maDataCheckedStateForDate[year][month][date][url_index]) {
                      // create the xml fragment for each node.
                      xmlDate = year + "-" + month + "-" + date;
                      xmlUrl = station.maStationDataForDate[year][month][date][url_index];
                      xmlText += createXmlNodeForDateUrl(xmlDate, xmlUrl);
  					}
                  }
                }
              } 
            }
          }
        }
      }
    }
  }
  xmlText += "</" + xmlRoot + ">";
  return xmlText;
}

// This function creates the xml node for updateCSWRecords date-url pair
// See the xml schema in the comment for function getXmlTextForAllCheckedDataUrls
// Please be sure that the schema of the xml document
// conforms to the one used in the function createXmlNodeForDateUrl
// in the DataServiceToolPortlet - WEB-INF/data_service_tool/dataservicetool.jsp file
function createXmlNodeForDateUrl (pDate, pUrl) {
	var xmlNode = "";
	var xmlNamespace = "dst";
	var xmlPairNode = xmlNamespace + ":url_date";
  	var xmlUrlNode = xmlNamespace + ":url";
  	var xmlDateNode = xmlNamespace + ":date";

	xmlNode += "<" + xmlPairNode + ">";
  	xmlNode += "<" + xmlDateNode + ">";
	xmlNode += pDate;
	xmlNode += "</" + xmlDateNode + ">";
	xmlNode += "<" + xmlUrlNode + ">";
	xmlNode += pUrl;
	xmlNode += "</" + xmlUrlNode + ">";
	xmlNode += "</" + xmlPairNode + ">";
	
	return xmlNode;
}

var MAGIC_NUMBER=6356752.3142;
var WGS84_SEMI_MAJOR_AXIS = 6378137.0;
var WGS84_ECCENTRICITY = 0.0818191913108718138;

var DEG2RAD=0.0174532922519943;
var PI=3.14159267;

//Default image format, used if none is specified
var FORMAT_DEFAULT="image/png";

//Google Maps Zoom level at which we switch from Mercator to Lat/Long.
var MERC_ZOOM_DEFAULT = 15;

function addLayers() {
  var tileCanada= new GTileLayer(new GCopyrightCollection(""),1,17);
  tileCanada.myLayers='AR-BALD EAGLE(Haliaeetus leucocephalus),AZ-BALD EAGLE(Haliaeetus leucocephalus),CA-BALD EAGLE(Haliaeetus leucocephalus),CO-BALD EAGLE(Haliaeetus leucocephalus),FL-BALD EAGLE(Haliaeetus leucocephalus),GA-BALD EAGLE(Haliaeetus leucocephalus),IA-BALD EAGLE(Haliaeetus leucocephalus),ID-BALD EAGLE(Haliaeetus leucocephalus),KS-BALD EAGLE(Haliaeetus leucocephalus),KY-BALD EAGLE(Haliaeetus leucocephalus),LA-BALD EAGLE(Haliaeetus leucocephalus),ME-BALD EAGLE(Haliaeetus leucocephalus),MI-BALD EAGLE(Haliaeetus leucocephalus),MO-BALD EAGLE(Haliaeetus leucocephalus),MS-BALD EAGLE(Haliaeetus leucocephalus),MT-BALD EAGLE(Haliaeetus leucocephalus),ND-BALD EAGLE(Haliaeetus leucocephalus),NM-BALD EAGLE(Haliaeetus leucocephalus),NV-BALD EAGLE(Haliaeetus leucocephalus),NY-BALD EAGLE(Haliaeetus leucocephalus),OK-BALD EAGLE(Haliaeetus leucocephalus),OR-BALD EAGLE(Haliaeetus leucocephalus),PA-BALD EAGLE(Haliaeetus leucocephalus),SC-BALD EAGLE(Haliaeetus leucocephalus),SD-BALD EAGLE(Haliaeetus leucocephalus),TN-BALD EAGLE(Haliaeetus leucocephalus),TX-BALD EAGLE(Haliaeetus leucocephalus),UT-BALD EAGLE(Haliaeetus leucocephalus),VA-BALD EAGLE(Haliaeetus leucocephalus),WA-BALD EAGLE(Haliaeetus leucocephalus),WV-BALD EAGLE(Haliaeetus leucocephalus),WY-BALD EAGLE(Haliaeetus leucocephalus)';
  tileCanada.myFormat='image/png';
  tileCanada.myBaseURL='http://gapwms.nbii.gov/wmsconnector/com.esri.wms.Esrimap?servicename=accipitridae';
  tileCanada.getTileUrl=CustomGetTileUrl;
  
  var tileSwiss= new GTileLayer(new GCopyrightCollection(""),1,17);
  tileSwiss.myLayers='CHE_SGS_500k_GeologicalUnits,CHE_SGS_500k_TectonicUnits';
  tileSwiss.myFormat='image/png';
  tileSwiss.myBaseURL='http://prod.swisstopogeodata.ch/SGS_Geol_Tecto/wms?';
  tileSwiss.getTileUrl=CustomGetTileUrl;
  
  var tileUK= new GTileLayer(new GCopyrightCollection(""),1,17);
  tileUK.myLayers='GBR_BGS_625k_BLT,GBR_BGS_625k_BLS';
  tileUK.myFormat='image/png';
  tileUK.myBaseURL='http://ogc.bgs.ac.uk/cgi-bin/BGS_Bedrock_and_Superficial_Geology/wms?';
  tileUK.getTileUrl=CustomGetTileUrl;
  
  var layers=[G_SATELLITE_MAP.getTileLayers()[0], G_HYBRID_MAP.getTileLayers()[1], tileCanada, tileSwiss, tileUK];
  var custommap1 = new GMapType(layers, G_SATELLITE_MAP.getProjection(), "OneGeology", G_SATELLITE_MAP);
  
  goMap.moMap.addMapType(custommap1);
}
function dd2MercMetersLng(p_lng) {
 return WGS84_SEMI_MAJOR_AXIS * (p_lng*DEG2RAD);
}

function dd2MercMetersLat(p_lat) {
 var lat_rad = p_lat * DEG2RAD;
 return WGS84_SEMI_MAJOR_AXIS * Math.log(Math.tan((lat_rad + PI / 2) / 2) * Math.pow( ((1 - WGS84_ECCENTRICITY * Math.sin(lat_rad)) / (1 + WGS84_ECCENTRICITY * Math.sin(lat_rad))), (WGS84_ECCENTRICITY/2)));
}

function CustomGetTileUrl(a,b,c) {
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