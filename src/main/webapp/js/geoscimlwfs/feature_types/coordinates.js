/** 
* @fileoverview This file declares the Class Coordinates.
* An object of Coordinates will be maintained in other feature types which contain 
* updateCSWRecords <b>gml:Point</b> node.
*/

/**
* @class
* This class defines information about updateCSWRecords <b>gml:Point</b>.<br>
* Note the difference between the xml schema for <b>Coordinates</b> and <b>Location</b> classes.<br>
*
* @constructor
* @param {DomXmlNode} pCoordinatesNode The XML node for <b>gml:Point</b>.
* @return A new {@link Coordinates}
*/
function Coordinates(pCoordinatesNode) {
  this.parseXmlElement(pCoordinatesNode);
  return this;
}

/**
* The latitude extracted from the <b>gml:Point/gml:coordinates</b> node.
* @type String
*/
Coordinates.prototype.msLatitude = "";

/**
* The longitude extracted from the <b>gml:Point/gml:coordinates</b> node.
* @type String
*/
Coordinates.prototype.msLongitude = "";

/**
* The assignment of function implementations for Coordinates
*/
Coordinates.prototype.parseXmlElement = Coordinates_parseXmlElement;

/**
* This function extracts latitude and longitude from updateCSWRecords <b>gml:Point</b> node
* and stores it in the members of the class.
* @param {DomXmlNode} pCoordinatesNode The XML node for <b>gml:Point</b>.
*/
function Coordinates_parseXmlElement(pCoordinatesNode) {
  if (g_IsIE) {
    pCoordinatesNode.setProperty("SelectionLanguage", "XPath");
  }
  
  /** Sample XML node
  * <gml:Point srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
  *  <gml:coordinates decimal="." cs="," ts=" ">133.8855,-23.6701</gml:coordinates>
  * </gml:Point>
  */
  var sCoords = GXml.value(pCoordinatesNode.selectSingleNode(".//*[local-name() = 'coordinates']"));
  if (sCoords) {
    var aCoords = sCoords.split(",");
    this.msLatitude = aCoords[1];
    this.msLongitude = aCoords[0];
  }
}
