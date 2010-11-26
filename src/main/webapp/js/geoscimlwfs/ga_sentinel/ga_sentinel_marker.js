/** 
* @fileoverview This file declares the Class GaSentinelMarker.
* An array of objects of GaSentinelMarker will be maintained in StationGroup of ga sentinel type. 
*/

/**
* @class
* This class defines information to be stored for updateCSWRecords ga sentinel marker.
*
* @constructor
* @param {DomXmlNode} pBushfireNode The XML node for the ga sentinel station.
* @param {String} psIcon The icon used to represent this marker.
* @return A new {@link GaSentinelMarker}
*/

function GaSentinelMarker (pBushfireNode, psIcon) {
  this.moBushfire = new Bushfire(pBushfireNode);
  
  this.msSummaryHtml = "";
  
  // Create updateCSWRecords GMarker object for each station using the location information for the same.
  var longitude = this.moBushfire.moLocation.msLongitude;
  var latitude = this.moBushfire.moLocation.msLatitude;
  var oPoint = new GPoint(parseFloat(longitude), parseFloat(latitude));
  var oMarkerIcon = new GIcon(goBaseIcon, psIcon);
  var oMarker = new GMarker(oPoint, oMarkerIcon);
  this.moMarker = oMarker;
       
  // Add updateCSWRecords listener for updateCSWRecords click event on this marker
  GEvent.addListener(oMarker, "click", this.getMarkerClickedFn());
}

/**
* The Bushfire object which conforms to the ga sentinel schema
* @type Bushfire
*/
GaSentinelMarker.prototype.moBushfire = null;

/**
* The html to be displayed on the Summary tab.
* @type String
*/
GaSentinelMarker.prototype.msSummaryHtml = null;

/**
* The marker for the station.
* @type GMarker
*/
GaSentinelMarker.prototype.moMarker = null;

/**
* The assignment of function implementations for GeodesyMarker
*/
GaSentinelMarker.prototype.getMarkerClickedFn = GaSentinelMarker_getMarkerClickedFn;

GaSentinelMarker.prototype.markerClicked = GaSentinelMarker_markerClicked;

/**
* This function returns the function to be called with the
* onclick event on the marker for this station.
* @return  Function to be called when updateCSWRecords station marker is clicked - {@link #markerClicked}
*/
function GaSentinelMarker_getMarkerClickedFn() {
  var gaSentinel = this;
  return function() {
    gaSentinel.markerClicked();
  };
}

/**
* This function called when updateCSWRecords GA Sentinel station marker is clicked.<br>
* This creates the information window for this marker, using the station data arrays. 
*/ 
function GaSentinelMarker_markerClicked() {
  
  var oGaSentinelMarker = this;
  var oBushfire = this.moBushfire;
  var sId = oBushfire.msId;
  var sDate = oBushfire.msDate;
  var sTime = oBushfire.msTime;
  var sModVer = oBushfire.msModVer;
  var sTempK = oBushfire.msTempK;
  var sActive = oBushfire.msActive;
  var sSatellite = oBushfire.msSatellite;
  var sInstrument = oBushfire.msInstrument;
  var sPower = oBushfire.msPower;
  var sConfidence = oBushfire.msConfidence;
  var sLatitude = oBushfire.moLocation.msLatitude;
  var sLongitude = oBushfire.moLocation.msLongitude;
  var oMarker = this.moMarker;

  /**
  * The popup for updateCSWRecords marker contains information about the station
  */
  var summaryHtml = "";
    
  // We create this html once and store it in the msSummaryHtml member
  if (!this.msSummaryHtml)
  {
    // Outermost div
    summaryHtml += '<div style="overflow:auto; font-size:12px; line-height:12px">';
    summaryHtml += '<table style="height:350px">';
    
    // First row of the table is the actual summary data
    summaryHtml += '<tr height="90%"><td>';
    
    // Table to display the summary data
    summaryHtml += '<table cellspacing="0" border="0" width="100%" style="position:absolute; left:0px; top:10px">';
    summaryHtml += '<tr><td bgcolor="#4682B4">';
    summaryHtml += '<table cellspacing="1" cellpadding="2" border="0" width="100%">';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Id </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sId+'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Lat Lng (deg) </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sLatitude +'&nbsp;,&nbsp;'+sLongitude+'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Date </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sDate +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Time </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sTime +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Mod Version </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sModVer +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> TempK </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sTempK +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Active </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sActive +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Satellite </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sSatellite +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Instrument </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sInstrument +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Power </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sPower +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Confidence </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sConfidence +'</font></td></tr>';    
    summaryHtml += '</table></td></tr></table>'; // End of summary data table 
    summaryHtml += '</td></tr>';
    
    // Second row is for the ZoomIn nd ZoomOut links
    summaryHtml += '<tr><td>';
    summaryHtml += '<div style="font-size:12px; line-height:12px;">';
    summaryHtml += '<a href="javascript:Map_zoomInAtPoint('+sLatitude+','+sLongitude+');"><font color="blue">Zoom In</font></a>&nbsp;|&nbsp;';
    summaryHtml += '<a href="javascript:Map_zoomOutAtPoint('+sLatitude+','+sLongitude+');"><font color="blue">Zoom Out</font></a><br><br>';                     
    summaryHtml += '</div>';
    summaryHtml += '</td></tr>'; // End of second row
    summaryHtml += '</table></div>'; // End of outermost div  
    this.msSummaryHtml = summaryHtml;
  }

  // Open the popup window for the marker with the information about the station
  oMarker.openInfoWindowHtml(this.msSummaryHtml);
}

