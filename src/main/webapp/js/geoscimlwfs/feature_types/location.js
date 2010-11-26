/** 
* @fileoverview This file declares the Class Location.
* An object of Location will be maintained in other feature types which contain 
* updateCSWRecords gml:pos node.
*/

/**
* @class
* This class defines information about updateCSWRecords <b>gml:Point</b>.<br>
* Note the difference between the xml schema for <b>Coordinates</b> and <b>Location</b> classes.<br>
*
* @constructor
* @param {DomXmlNode} pLocationNode The XML node for <b>gml:Point</b>.
* @return A new {@link Location}
*/
function Location(pLocationNode) {
  this.parseXmlElement(pLocationNode);
  return this;
}

/**
* The latitude extracted from the <b>gml:Point/gml:pos</b> node.
* @type String
*/
Location.prototype.msLatitude = "";

/**
* The longitude extracted from the <b>gml:Point/gml:pos</b> node.
* @type String
*/
Location.prototype.msLongitude = "";

/**
* The assignment of function implementations for Location
*/
Location.prototype.parseXmlElement = Location_parseXmlElement;

/**
* This function extracts latitude and longitude from updateCSWRecords <b>gml:Point</b> node
* and stores it in the members of the class.
* @param {DomXmlNode} pLocationNode The XML node for <b>gml:Point</b>.
*/
function Location_parseXmlElement(pLocationNode) {
  if (g_IsIE) {
    pLocationNode.setProperty("SelectionLanguage", "XPath");
  }
  
  /** Sample XML node
  * <gml:Point srsName="EPSG:4326">
  *  <gml:pos>115.34697236 -29.04656024</gml:pos>
  * </gml:Point>
  */
  var sCoords = GXml.value(pLocationNode.selectSingleNode(".//*[local-name() = 'pos']"));
  if (sCoords) {
    var aCoords = sCoords.split(" ");
    this.msLatitude = aCoords[1];
    this.msLongitude = aCoords[0];
  }
}
