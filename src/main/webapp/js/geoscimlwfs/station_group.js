
function StationGroup(pIcon, pTitle, pDescription, pWfsUrl, pWfsFeatureType) {
  // Initialize the members of the class
  var reg = /\s+/g;
  this.msId = pTitle.replace(reg, '_');
  this.msTitle = pTitle;
  this.msDescription = pDescription;
  this.msIcon = pIcon;
  this.mbIsGroupOn = false;
  this.msFeatureType = pWfsFeatureType;
  this.maMarkers = [];
  
  this.msWfsUrl = pWfsUrl;
  this.msLayerName = pWfsFeatureType;
  
  this.getMarkers();
 
  // Create Html objects for the station
  // Create updateCSWRecords new station control element
  this.moHtmlBundle = document.createElement("div");
  this.moHtmlBundle.id = this.msId + "_div";
  this.moHtmlBundle.style.background = "#F7EFC1";
  this.moHtmlBundle.style.top = "5 px";
    
  // Create the img element for each group
  this.moHtmlImg = document.createElement("img");
  this.moHtmlImg.id = this.msId;
  this.moHtmlImg.src = this.msIcon;
  this.moHtmlImg.onmouseover = this.getShowToolTipFn();
  this.moHtmlImg.onmouseout = this.getHideToolTipFn();
     
  // Create the checkbox element for each image
  this.moHtmlChk = document.createElement("input");
  this.moHtmlChk.type = "checkbox";
  this.moHtmlChk.id =  this.msId + "_chk";
  this.moHtmlChk.checked = 0;
  this.moHtmlChk.onclick = this.getClickFn();
    
  // Create the lbl element for each image    
  this.moHtmlLbl = document.createElement("label");
  //this.moHtmlLbl.for = this.msId;
  this.moHtmlLbl.id = this.msId + "_lbl";
  this.moHtmlLbl.className = "textoff";
  this.moHtmlLbl.align = "left";
  this.moHtmlLbl.innerHTML = "<br/>" + this.msTitle + "<br/><br/>";
    
  // Add to the station control frame
  this.moHtmlBundle.appendChild(this.moHtmlImg);
  this.moHtmlBundle.appendChild(this.moHtmlChk);
  this.moHtmlBundle.appendChild(this.moHtmlLbl);	
}

/**
* Group id.
* This is the title of the layer from the capabilities document.
* @type String
*/
StationGroup.prototype.msId = null;

/**
* Group description.
* This description of the layer from the capabilities document.
* @type String
*/
StationGroup.prototype.msTitle = null;

/**
* Group tooltip.
* This tooltip to be displayed on the group.
* It is derived from the description in the capabilities document.
* @type String
*/
StationGroup.prototype.msDescription = null;

/**
* Relative path of the group icon for the group.
* Path is relative to the application root directory.
* @type String
*/
StationGroup.prototype.msIcon = null;

/**
* Flag indicating whether the stations belonging to the group
* are displayed on the map or not.
* @type Boolean
*/
StationGroup.prototype.mbIsGroupOn = 0;

/**
* The URL to query the GetFeatures request from the server.
* @type String
*/
StationGroup.prototype.msWfsUrl = null;

/**
* The layerName or typeName to be passed as updateCSWRecords parameter
* when querying the GetFeatures request.
* @type String
*/
StationGroup.prototype.msLayerName = null;

/**
* The featureType implemented by the server.
* Only feature types belonging to the array gaFeatureTypes
* can be recognized by the application
* @type String
*/
StationGroup.prototype.msFeatureType = null;

/**
* Array of the markers to be displayed when the icon for the group
* is clicked.
* @type Array
*/
StationGroup.prototype.maMarkers = null;

/**
* Html object of div type, to show the image, checkbox and label for the group.
* @type Object
*/
StationGroup.prototype.moHtmlBundle = null;

/**
* Html object of img type, to show the image for the group.
* @type Object
*/
StationGroup.prototype.moHtmlImg = null;

/**
* Html object of type input-checkbox, to show the checkbox for the group.
* @type Object
*/
StationGroup.prototype.moHtmlChk = null;

/**
* Html object of type label, to show the label for the group.
* @type Object
*/
StationGroup.prototype.moHtmlLbl = null;

/**
* The assignment of function implementations for StationGroup
*/
StationGroup.prototype.getMarkers = StationGroup_getMarkers;

StationGroup.prototype.parseXmlForMarkers = StationGroup_parseXmlForMarkers;

StationGroup.prototype.onClick = StationGroup_onClick;

StationGroup.prototype.getClickFn = StationGroup_getClickFn;

StationGroup.prototype.getShowToolTipFn = StationGroup_getShowToolTipFn;

StationGroup.prototype.getHideToolTipFn = StationGroup_getHideToolTipFn;

StationGroup.prototype.showMarkers = showMarkers;

StationGroup.prototype.hideMarkers = hideMarkers;

/**
* Function to download the WFS XML response for the layer. 
* It updates the array maMarkers depending on the featureType 
*/
function StationGroup_getMarkers() {
  
  var group = this;
  var wfsUrl = this.msWfsUrl + "request=GetFeature" + "&typeName=" + encodeURI(this.msLayerName);
  // AJAX call to get the unique station Ids first
  GDownloadUrl(wfsUrl, function(pData, pResponseCode) {    
      
    if(pResponseCode == 200) {
      // Call the parse function to read the XML data from the file.
      var xmlDoc = GXml.parse(pData);
      var rootNode = xmlDoc.documentElement;
      if (rootNode) {
        group.parseXmlForMarkers(rootNode);
      }
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
}

/**
* This function parses the WFS XML.
* It extracts all stations from the XML and creates updateCSWRecords marker for each of them.
* These markers are stored in the member array maMarkers, of the object
* The type of the marker depends on the featureType of the group
* @param {DomXmlNode} pRootNode The root node of the XML to be parsed 
*/
function StationGroup_parseXmlForMarkers(pRootNode) {

  var rootNode = pRootNode;
  
  if (g_IsIE) {
    rootNode.setProperty("SelectionLanguage", "XPath");
  }
  
  // Parse the XML for stations
  // Beware of using namespaces when using getElementsByTagName - it does not include namespaces in parsing.
  var aStations = rootNode.getElementsByTagName(this.msFeatureType);
  if (!aStations.length) {
    // Try to find elements without specifying their namespaces.
    aStations = rootNode.getElementsByTagName(this.msFeatureType.replace(/^[^:]+:/, ""));
  }

  if (!aStations.length) {
    return;
  }
  
  // Depending on the featureType, create markers for the stations.
  switch(this.msFeatureType) {
    case "gsml:Borehole" :
      for (var i = 0; i < aStations.length; i++) {
        var boreholeMarker = new NVCLMarker(aStations[i], this.msIcon);
        if (boreholeMarker) {
          this.maMarkers[i] = boreholeMarker;
        }
      }
      break;
    case "geodesy:stations" :
      for (var i = 0; i < aStations.length; i++) {
        var geodesyMarker = new GeodesyMarker(aStations[i], this.msIcon, this.msWfsUrl, "geodesy:station_observations");
        if (geodesyMarker) {
          this.maMarkers[i] = geodesyMarker;
        }
      }
      break;    
    case "sa:SamplingPoint" :
      for (var i = 0; i < aStations.length; i++) {
        var gnssMarker = new GNSSMarker(aStations[i], this.msIcon);
        if (gnssMarker) {
          this.maMarkers[i] = gnssMarker;
        }
      }
      break; 
    case "Avhrr48to72Hours-1404" :
      for (var i = 0; i < aStations.length; i++) {
        var gaSentinelMarker = new GaSentinelMarker(aStations[i], this.msIcon);
        if (gaSentinelMarker) {
          this.maMarkers[i] = gaSentinelMarker;
        }
      }
      break; 
    case "Modis48to72Hours-1604" :
      for (var i = 0; i < aStations.length; i++) {
        var gaSentinelMarker = new GaSentinelMarker(aStations[i], this.msIcon);
        if (gaSentinelMarker) {
          this.maMarkers[i] = gaSentinelMarker;
        }
      }
      break;       
  }
}

/**
* The function called when updateCSWRecords group icon is clicked.
* This is registered with the onclick event of the 
* image and checkbox associated with the group.
* Depending on the current state of the group,
* It displays or hides the markers, belonging to the group, on the map. 
*/
function StationGroup_onClick () {

  // Toggle station marker display depending on the previous state
  if (this.msIsGroupOn) {
    // Markers were previously displayed
    // Unhighlight the group icon
    this.msIsGroupOn = 0;
    this.moHtmlImg.src = this.msIcon;
    this.moHtmlBundle.style.background = "#F7EFC1";
    this.moHtmlChk.checked = 0;
    this.moHtmlLbl.className = "textoff";
    
    // Remove markers for the stations belonging to this group
    for(var i=0; i < this.maMarkers.length; i++) {
      goMap.moMap.removeOverlay(this.maMarkers[i].moMarker);
    }
    
  } else {
    // Markers were previously not displayed
    // Highlight the group icon    
    this.msIsGroupOn = 1;
    this.moHtmlImg.src = this.msIcon;
    this.moHtmlBundle.style.background = "#F3E3A5";
    this.moHtmlChk.checked = 1;
    this.moHtmlLbl.className = "texton";
    
    // Add markers for the stations belonging to this group
    // Remove markers for the stations belonging to this group
    for(var i=0; i < this.maMarkers.length; i++) {
      goMap.moMap.addOverlay(this.maMarkers[i].moMarker);
    }
        
  }
}

function hideMarkers(map) {
    this.msIsGroupOn = 0;
    this.moHtmlImg.src = this.msIcon;
    this.moHtmlBundle.style.background = "#F7EFC1";
    this.moHtmlChk.checked = 0;
    this.moHtmlLbl.className = "textoff";
    
    // Remove markers for the stations belonging to this group
    for(var i=0; i < this.maMarkers.length; i++) {
      map.removeOverlay(this.maMarkers[i].moMarker);
    }
}

function showMarkers(map) {
    this.msIsGroupOn = 1;
    this.moHtmlImg.src = this.msIcon;
    this.moHtmlBundle.style.background = "#F3E3A5";
    this.moHtmlChk.checked = 1;
    this.moHtmlLbl.className = "texton";

    // Add markers for the stations belonging to this group
    // Remove markers for the stations belonging to this group
    for(var i=0; i < this.maMarkers.length; i++) {
      map.addOverlay(this.maMarkers[i].moMarker);
    }
}

/**
* Get the function for the onclick event of the group
* image and checkbox.
*/
function StationGroup_getClickFn() {
  var group = this;
  return function() {
    group.onClick();
  };
}

/**
* Get the function to display tool tip for the group.
*/
function StationGroup_getShowToolTipFn() {
  var group = this;
  return function() {
    Tip(group.msDescription);
  };
}

/**
* Get the function to hide the tooltip for the group.
*/
function StationGroup_getHideToolTipFn() {
  return function() {
    UnTip();
  };
}