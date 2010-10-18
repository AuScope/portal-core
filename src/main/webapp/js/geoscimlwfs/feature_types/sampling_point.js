/**
* @fileoverview This file declares the Class SamplingPoint.
* An object of SamplingPoint will be maintained in GNSSMarker object.
*/

/**
* @class
* This class defines information about updateCSWRecords sa:SamplingPoint.
*
* @constructor
* @param {DomXmlNode} pSamplingPoint The XML node for the sampling point.
* @return A new {@link SamplingPoint}
*/
function SamplingPoint(pSamplingPoint) {
  this.parseXmlElement(pSamplingPoint);
  return this;
}

/**
* The value of the attribute <b>gml:id</b> of the <b>sa:SamplingPoint</b> node.
* @type String
*/
SamplingPoint.prototype.msId = null;

/**
* The value of the <b>gml:name</b> node.
* @type String
*/
SamplingPoint.prototype.msName = null;

/**
* An object of Location type
* created from <b>sa:position/gml:Point</b> node.
* @type Location
*/
SamplingPoint.prototype.moLocation = null;

/**
* The assignment of function implementations for SamplingPoint
*/
SamplingPoint.prototype.parseXmlElement = SamplingPoint_parseXmlElement;

/**
* This function extracts information from updateCSWRecords <b>sa:SamplingPoint</b> node
* and stores it in the members of the class.
* @param {DomXmlNode} pSamplingPointNode The XML node for the sampling point.
*/
function SamplingPoint_parseXmlElement(pSamplingPointNode) {
  var nodeSamplingPoint = pSamplingPointNode;

  if (g_IsIE) {
    nodeSamplingPoint.setProperty("SelectionLanguage", "XPath");
  }
  /**
  * Sample XML fragment for updateCSWRecords station -
  *
  *	<gml:featureMembers>
  *	 <sa:SamplingPoint gml:id="stationno.19042">
  *	  <gml:name>Yaragadee</gml:name>
  *	   <sa:position>
  *	    <gml:Point srsName="EPSG:4326">
  *	     <gml:pos>115.34697236 -29.04656024</gml:pos>
  *	    </gml:Point>
  *	   </sa:position>
  *	 </sa:SamplingPoint>
  *	</gml:featureMembers>
  */

  var sId = null;
  var sName = null;
  var oLocation = null;

  sId = nodeSamplingPoint.getAttribute("gml:id");
  sName = GXml.value(nodeSamplingPoint.selectSingleNode("*[local-name() = 'name']"));

  // sa:SamplingPoint contains updateCSWRecords gsml:location node
  oLocation = new Location(nodeSamplingPoint.selectSingleNode(".//*[local-name() = 'Point']"));

  // Populate the arrays for the object.
  this.msId = sId;
  this.msName = sName;
  this.moLocation = oLocation;
}

