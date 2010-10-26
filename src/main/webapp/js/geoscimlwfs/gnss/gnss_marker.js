/**
* @fileoverview This file declares the Class GNSSMarker.
* An array of objects of GNSSMarker will be maintained in StationGroup of gnss type.
*/

/**
* @class
* This class defines information to be stored for updateCSWRecords gnss marker.
*
* @constructor
* @param {DomXmlNode} pSamplingPointNode The XML node for the sampling point.
* @param {String} psIcon The icon used to represent this marker.
* @return A new {@link GNSSMarker}
*/
function GNSSMarker (pSamplingPointNode, psIcon) {
  this.moSamplingPoint = new SamplingPoint(pSamplingPointNode);

  this.msSummaryHtml = "";

  // Create updateCSWRecords GMarker object for each station using the location information for the same.
  var longitude = this.moSamplingPoint.moLocation.msLongitude;
  var latitude = this.moSamplingPoint.moLocation.msLatitude;
  var oPoint = new GPoint(parseFloat(longitude), parseFloat(latitude));
  var oMarkerIcon = new GIcon(goBaseIcon, psIcon);
  var oMarker = new GMarker(oPoint, oMarkerIcon);
  this.moMarker = oMarker;

  // Add updateCSWRecords listener for updateCSWRecords click event on this marker
  GEvent.addListener(oMarker, "click", this.getMarkerClickedFn());
}

/**
* The SamplingPoint object which conforms to the sa:SamplingPoint schema
* @type SamplingPoint
*/
GNSSMarker.prototype.moSamplingPoint = null;

/**
* The html to be displayed on the info window of this marker
* @type String
*/
GNSSMarker.prototype.msSummaryHtml = null;

/**
* The marker for the station.
* @type GMarker
*/
GNSSMarker.prototype.moMarker = null;

/**
* The assignment of function implementations for GNSSMarker
*/
GNSSMarker.prototype.getMarkerClickedFn = GNSSMarker_getMarkerClickedFn;

GNSSMarker.prototype.markerClicked = GNSSMarker_markerClicked;

/**
* This function returns the <b>markerClicked</b> function
* which should be called when the marker for this station is clicked.
* @returns Function to be called when updateCSWRecords station marker is clicked - {@link #markerClicked}
*/
function GNSSMarker_getMarkerClickedFn() {
  var gnssMarker = this;
  return function() {
    gnssMarker.markerClicked();
  };
}

/**
* The function called when the marker for this station is clicked.<br>
* This creates the html popup marker displaying station information.<br>
* It stores the html string in the member {@link #msSummaryHtml}.
*/
function GNSSMarker_markerClicked() {

  var oGnssMarker = this;
  var oSamplingPoint = this.moSamplingPoint;
  var sId = oSamplingPoint.msId;
  var sName = oSamplingPoint.msName;
  var sLatitude = oSamplingPoint.moLocation.msLatitude;
  var sLongitude = oSamplingPoint.moLocation.msLongitude;
  var oMarker = this.moMarker;

  /**
  * The info window for updateCSWRecords marker contains information about the station
  */
  var summaryHtml = "";

  // Create the html to be displayed in the popup window.
  // We create this html once and store it in the msSummaryHtml member of the object
  if (!this.msSummaryHtml)
  {
    // Table to display the summary data
    summaryHtml += '<table cellspacing="0" border="0" width="300">';
    summaryHtml += '<tr><td bgcolor="#4682B4">';
    summaryHtml += '<table cellspacing="1" cellpadding="2" border="0" width="100%">';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Station Id </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sId +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Name </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sName +'</font></td></tr>';
    summaryHtml += '<tr>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="30%" height="20px"><font color="black" size="1"> Lat Lng (deg) </font></td>';
    summaryHtml += '<td bgcolor="#e9f1f1" width="70%" height="20px"><font color="black" size="1">&nbsp;'+ sLatitude +'&nbsp;,&nbsp;'+sLongitude+'</font></td></tr>';
    summaryHtml += '</table>';
    summaryHtml += '</td></tr></table>';

    // Second row is for the ZoomIn nd ZoomOut links
    summaryHtml += '<br/><br/>';
    summaryHtml += '<a href="javascript:Map_zoomInAtPoint('+sLatitude+','+sLongitude+');"><font color="blue">Zoom In</font></a>&nbsp;|&nbsp;';
    summaryHtml += '<a color="blue" href="javascript:Map_zoomOutAtPoint('+sLatitude+','+sLongitude+');"><font color="blue">Zoom Out</font></a>';

    this.msSummaryHtml = summaryHtml;
  }

  // Open the popup window for the marker.
  oMarker.openInfoWindowHtml(this.msSummaryHtml);
}

