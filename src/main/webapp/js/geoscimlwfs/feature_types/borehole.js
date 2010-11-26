/** 
* @fileoverview This file declares the Class Borehole.
* An object of Borehole will be maintained in NVCLMarker object. 
*/

/**
* @class
* This class defines information about updateCSWRecords gsml:borehole.
*
* @constructor
* @param {DomXmlNode} pBorehole The XML node for the borehole station.
* @return A new {@link Borehole}
*/
function Borehole(pBorehole) {
  this.parseXmlElement(pBorehole);
  return this;
}

/**
* The value of the attribute <b>gml:id</b> of the <b>gsml:Borehole</b> node.
* @type String
*/
Borehole.prototype.msId = null;

/**
* The value of the <b>gml:name</b> node.
* @type String
*/
Borehole.prototype.msName = null;

/**
* The value of the attribute <b>xlink:href</b> of the <b>gml:metaDataProperty</b> node.
* @type String
*/
Borehole.prototype.msProject = null;

/**
* The value of the <b>gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling</b> node.
* @type String
*/
Borehole.prototype.msDateOfDrilling = null;

/**
* To be implemented in the nvcl xml node.
* @type String
*/
Borehole.prototype.msDriller = null;

/**
* The value of the <b>gsml:indexData/gsml:BoreholeDetails/gsml:drillingMethod</b> node.
* @type String
*/
Borehole.prototype.msDrillingMethod = null;

/**
* The value of the <b>gsml:indexData/gsml:BoreholeDetails/gsml:inclinationType</b> node.
* @type String
*/
Borehole.prototype.msInclinationType = null;

/**
* To be implemented in the nvcl xml node.
* @type String
*/
Borehole.prototype.msNominalDiameter = null;

/**
* The value of the <b>gsml:indexData/gsml:BoreholeDetails/gsml:operator</b> node.
* @type String
*/
Borehole.prototype.msOperator = null;

/**
* The value of the <b>gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian</b> node.
* @type String
*/
Borehole.prototype.msCoreCustodian = null;

/**
* The value of the <b>gsml:indexData/gsml:BoreholeDetails/gsml:startPoint</b> node.
* @type String
*/
Borehole.prototype.msStartPoint = null;

/**
* To be implemented in the nvcl xml node.
* @type String
*/
Borehole.prototype.msCoredInterval = null;

/**
* To be implemented in the nvcl xml node.
* @type samplingCurve
*/
Borehole.prototype.moSamplingCurve = null;

/**
* An object of Location type
* created from <b>gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point</b> node.
* @type Location
*/
Borehole.prototype.moLocation = null;

/**
* The assignment of function implementations for Borehole
*/
Borehole.prototype.parseXmlElement = Borehole_parseXmlElement;

/**
* This function extracts information from updateCSWRecords gsml:borehole node
* and stores it in the members of the class.
* @param {DomXmlNode} pBoreholeNode The XML node for the borehole station.
*/
function Borehole_parseXmlElement(pBoreholeNode) {
  var nodeBorehole = pBoreholeNode;
  
  if (g_IsIE) {
    nodeBorehole.setProperty("SelectionLanguage", "XPath");
  }

  /**
  * Sample XML fragment for updateCSWRecords gsml:borehole node -
  *
  * <gsml:Borehole gml:id="nvcl_core.4">
  *  <gml:metaDataProperty xlink:href="http://nvcl.csiro.au/projects.aspx%3Fprojectid=Ernest%20Henry" xlink:role="Project" xlink:title="Ernest Henry"/>
  *  <gml:name>EH432_DB</gml:name>
  *  <sa:sampledFeature xlink:href="urn:ogc:def:nil:OGC:unknown"/>
  *  <sa:shape/>
  *  <gsml:collarLocation>
  *   <gsml:BoreholeCollar>
  *    <gsml:location>
  *     <gml:Point srsName="EPSG:4326">
  *      <gml:pos>140.717 -20.4528</gml:pos>
  *     </gml:Point>
  *    </gsml:location>
  *   </gsml:BoreholeCollar>
  *  </gsml:collarLocation>
  *  <gsml:indexData>
  *   <gsml:BoreholeDetails>
  *    <gsml:operator xlink:href="urn:cgi:party:CSIRO:Melissa%20Quigley"/>
  *    <gsml:dateOfDrilling>2007-03-30+08:00</gsml:dateOfDrilling>
  *    <gsml:drillingMethod>diamond core</gsml:drillingMethod>
  *    <gsml:startPoint>natural ground surface</gsml:startPoint>
  *    <gsml:inclinationType>vertical</gsml:inclinationType>
  *    <gsml:coredInterval/>
  *    <gsml:coreCustodian xlink:href="urn:cgi:party:CSIRO:Xstrata"/>
  *   </gsml:BoreholeDetails>
  *  </gsml:indexData>
  * </gsml:Borehole>
  */
  
  var sId = null;
  var sName = null;
  var metaDataNode, sProject = null;
  var coreCustodianNode, sCoreCustodian, aCoreCustodian = null;
  var sDateOfDrilling = null;
  var sDriller = null;
  var sDrillingMethod = null;
  var sInclinationType = null;
  var sNominalDiameter = null;
  var operatorNode, sOperator, aOperator  = null;
  var sStartPoint = null;
  var sCoredInterval = null;
  var oLocation = null;
  var oSamplingCurve = null;
	   
  sId = nodeBorehole.getAttribute("gml:id");
  sName = GXml.value(nodeBorehole.selectSingleNode("*[local-name() = 'name']"));

  // Extract all the data about the borehole from the XML node
  metaDataNode = nodeBorehole.selectSingleNode("*[local-name() = 'metaDataProperty']");
  if (metaDataNode) {
    sProject = metaDataNode.getAttribute("xlink:title");
  }
    
  coreCustodianNode = nodeBorehole.selectSingleNode(".//*[local-name() = 'coreCustodian']");
  if (coreCustodianNode) {
	sCoreCustodian = coreCustodianNode.getAttribute("xlink:href");
	aCoreCustodian = sCoreCustodian.split(":");
	sCoreCustodian = aCoreCustodian[aCoreCustodian.length-1];
  }		
  
  sDateOfDrilling = GXml.value(nodeBorehole.selectSingleNode(".//*[local-name() = 'dateOfDrilling']"));
  sDrillingMethod = GXml.value(nodeBorehole.selectSingleNode(".//*[local-name() = 'drillingMethod']"));
  sInclinationType = GXml.value(nodeBorehole.selectSingleNode(".//*[local-name() = 'inclinationType']"));
  
  operatorNode = nodeBorehole.selectSingleNode(".//*[local-name() = 'operator']");
  if (operatorNode) {
	sOperator = operatorNode.getAttribute("xlink:href");
	aOperator = sOperator.split(":");
	sOperator = unescape(aOperator[aOperator.length-1]);
  }
  
  sStartPoint = GXml.value(nodeBorehole.selectSingleNode(".//*[local-name() = 'startPoint']"));
   
  // gsml:borehole contains updateCSWRecords gsml:location node
  oLocation = new Location(nodeBorehole.selectSingleNode(".//*[local-name() = 'Point']"));

  // Populate the arrays for the object.
  this.msId = sId;
  this.msName = sName;
  this.msProject = sProject;
  this.msDateOfDrilling = sDateOfDrilling;
  this.msDriller = sDriller;
  this.msDrillingMethod = sDrillingMethod;
  this.msInclinationType = sInclinationType;
  this.msNominalDiameter = sNominalDiameter;
  this.msOperator = sOperator;
  this.msCoreCustodian = sCoreCustodian;
  this.msStartPoint = sStartPoint;
  this.msCoredInterval = sCoredInterval;
  this.moLocation = oLocation;
  this.moSamplingCurve = oSamplingCurve; 
}

