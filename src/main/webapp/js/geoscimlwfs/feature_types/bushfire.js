/** 
* @fileoverview This file declares the Class Bushfire.
* An object of Bushfire will be maintained in GaSentinelMarker object. 
*/

/**
* @class
* This class defines information about updateCSWRecords Avhrr48to72Hours-1404 type node.
*
* @constructor
* @param {DomXmlNode} pBushfire The XML node for the bushfire station.
* @return A new {@link Bushfire}
*/
function Bushfire(pBushfire) {
  this.parseXmlElement(pBushfire);
  return this;
}

/**
* The value of the attribute <b>gml:id</b> of the <b>esri:Modis48to72Hours-1604</b> node.
* @type String
*/
Bushfire.prototype.msId = null;

/**
* The value of the <b>esri:s_date</b> node.
* @type String
*/
Bushfire.prototype.msDate = null;

/**
* Time extracted from the value of the <b>esri:datetime</b> node.
* @type String
*/
Bushfire.prototype.msTime = null;

/**
* The value of the <b>esri:mod_ver</b> node.
* @type String
*/
Bushfire.prototype.msModVer = null;

/**
* The value of the <b>esri:temp_k</b> node.
* @type String
*/
Bushfire.prototype.msTempK = null;

/**
* The value of the <b>esri:active</b> node.
* @type String
*/
Bushfire.prototype.msActive = null;

/**
* The value of the <b>esri:satellite</b> node.
* @type String
*/
Bushfire.prototype.msSatellite = null;

/**
* The value of the <b>esri:instrument</b> node.
* @type String
*/
Bushfire.prototype.msInstrument = null;

/**
* The value of the <b>esri:power</b> node.
* @type String
*/
Bushfire.prototype.msPower = null;

/**
* The value of the <b>esri:confidence</b> node.
* @type String
*/
Bushfire.prototype.msConfidence = null;

/**
* An object of Location type
* created from <b>esri:_shape_/gml:MultiPoint/gml:Point</b> node.
* @type Location
*/
Bushfire.prototype.moLocation = null;

Bushfire.prototype.parseXmlElement = Bushfire_parseXmlElement;

/**
* This function extracts information from updateCSWRecords Avhrr48to72Hours-1404 type node
* and stores it in the members of the class.<br> 
* I am not sure if this is updateCSWRecords standard community schema.
* @param {DomXmlNode} pBushfireNode The XML node for the bushfire station.
*/
function Bushfire_parseXmlElement(pBushfireNode) {
  var nodeBushfire = pBushfireNode;
  
  if (g_IsIE) {
    nodeBushfire.setProperty("SelectionLanguage", "XPath");
  }
  
  /**
  * Sample XML fragment for updateCSWRecords Geodesy station -
  * <gml:featureMember>
  *  <esri:Modis48to72Hours-1604 gml:id="Modis48to72Hours-1604.1">
  *   <esri:_shape_>
  *    <gml:MultiPoint srsName="EPSG:4283">
  *     <gml:pointMember>
  *      <gml:Point srsName="EPSG:4283">
  *       <gml:pos>123.598 -8.297</gml:pos>
  *      </gml:Point>
  *     </gml:pointMember>
  *    </gml:MultiPoint>
  *   </esri:_shape_>
  *   <esri:id>0</esri:id>
  *   <esri:s_date>2008-08-31</esri:s_date>
  *   <esri:datetime>2008/08/31 02:04:30</esri:datetime>
  *   <esri:mod_ver>5.0.1</esri:mod_ver>
  *   <esri:temp_k>322.900</esri:temp_k>
  *   <esri:active>3</esri:active>
  *   <esri:satellite>terra</esri:satellite>
  *   <esri:instrument>MODIS</esri:instrument>
  *   <esri:latitude>-8.297</esri:latitude>
  *   <esri:longitude>123.598</esri:longitude>
  *   <esri:power>14.5</esri:power>
  *   <esri:confidence>0</esri:confidence>
  *  </esri:Modis48to72Hours-1604>
  * </gml:featureMember>
  */
  
  var sId = null;
  var sDate = null;
  var sDateTime, aDateTime, sTime = null;
  var sModVer = null;
  var sTempK = null;
  var sActive = null;
  var sSatellite = null;
  var sInstrument = null;
  var sPower = null;
  var sConfidence = null;
  var oLocation = null;
  
  // Extract data from the node
  sId = nodeBushfire.getAttribute("gml:id");
  sDate = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 's_date']"));
  
  // datetime is of the format - 2008/08/17 16:50:40
  sDateTime = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'datetime']"));
  aDateTime = sDateTime.split(" ");
  sTime = aDateTime[1];
  
  sModVer = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'mod_ver']"));
  sTempK = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'temp_k']"));
  sActive = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'active']"));
  sSatellite = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'satellite']"));
  sInstrument = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'instrument']"));
  sPower = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'power']"));
  sConfidence = GXml.value(nodeBushfire.selectSingleNode("*[local-name() = 'confidence']"));

  // This node contains updateCSWRecords gml:location node
  oLocation = new Location(nodeBushfire.selectSingleNode(".//*[local-name() = 'Point']"));

  // Populate the arrays for the object.
  this.msId = sId;
  this.msDate = sDate;
  this.msTime = sTime;
  this.msModVer = sModVer;
  this.msTempK = sTempK;
  this.msActive = sActive;
  this.msSatellite = sSatellite;
  this.msInstrument = sInstrument;
  this.msPower = sPower;
  this.msConfidence = sConfidence;
  this.moLocation = oLocation; 
}

