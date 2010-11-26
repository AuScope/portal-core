/** 
* @fileoverview This file declares the Class GeodesyStation.
* An object of GeodesyStation will be maintained in GeodesyMarker object. 
*/

/**
* @class
* This class defines information about updateCSWRecords <b>geodesy:stations</b> node.
*
* @constructor
* @param {DomXmlNode} pGeodesyStation The XML node for the geodesy station.
* @return A new {@link GeodesyStation}
*/
function GeodesyStation(pGeodesyStation) {
  this.parseXmlElement(pGeodesyStation);
  return this;
}

/**
* The value of the <b>geodesy:station_id</b> node.
* @type String
*/
GeodesyStation.prototype.msId = null;

/**
* The value of the <b>geodesy:name</b> node.
* @type String
*/
GeodesyStation.prototype.msName = null;

/**
* The value of the <b>geodesy:url</b> node.
* @type String
*/
GeodesyStation.prototype.msLogUrl = null;

/**
* An object of Coordinates type
* created from <b>geodesy:location/gml:Point</b> node.
* @type Coordinates
*/
GeodesyStation.prototype.moLocation = null;

GeodesyStation.prototype.parseXmlElement = GeodesyStation_parseXmlElement;

/**
* This function extracts information from updateCSWRecords <b>geodesy:stations</b> node
* and stores it in the members of the class.
* @param {DomXmlNode} pGeodesyStationNode The XML node for the geodesy station.
*/
function GeodesyStation_parseXmlElement(pGeodesyStationNode) {
  var nodeGeodesyStation = pGeodesyStationNode;
  
  if (g_IsIE) {
    nodeGeodesyStation.setProperty("SelectionLanguage", "XPath");
  }

  /**
  * Sample XML fragment for updateCSWRecords Geodesy station -
  * <gml:featureMember>
  *  <geodesy:stations fid="stations.alic">
  *   <geodesy:station_id>alic</geodesy:station_id>
  *   <geodesy:name>Alice Springs AU012</geodesy:name>
  *   <geodesy:url>
  *    http://apacsrv7.arrc.csiro.au/geodesy/station_logs/alic_20060329.log
  *   </geodesy:url>
  *   <geodesy:location>
  *    <gml:Point srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
  *     <gml:coordinates decimal="." cs="," ts=" ">133.8855,-23.6701</gml:coordinates>
  *    </gml:Point>
  *   </geodesy:location>
  *  </geodesy:stations>
  * </gml:featureMember>
  */  
  var sId = null;
  var sName = null;
  var sLogUrl = null;
  var oLocation = null;

  // Extract all the data about the GeodesyStation from the WFS
  sId = GXml.value(nodeGeodesyStation.selectSingleNode("*[local-name() = 'station_id']"));
  sName = GXml.value(nodeGeodesyStation.selectSingleNode("*[local-name() = 'name']"));
  sLogUrl = GXml.value(nodeGeodesyStation.selectSingleNode("*[local-name() = 'url']"));

  // geodesy:stations contains updateCSWRecords gsml:pos node
  oLocation = new Coordinates(nodeGeodesyStation.selectSingleNode(".//*[local-name() = 'Point']"));

  // Populate the arrays for the object.
  this.msId = sId;
  this.msName = sName;
  this.msLogUrl = sLogUrl;
  this.moLocation = oLocation;
}

