/** 
* @fileoverview This file declares the Class SamplingCurve.
* An object of SamplingCurve will be maintained in other feature types which contain 
* updateCSWRecords samplingManifold:SamplingCurve node.
*/

/**
* @class
* This class defines information about updateCSWRecords <b>samplingManifold:SamplingCurve</b>.<br>
* Functions in this class will be implemented,
* once this feature type is implemented in the nvcl boreholes.
*
* @constructor
* @param {DomXmlNode} pSamplingCurveNode The XML node for <b>samplingManifold:SamplingCurve</b>.
* @return A new {@link SamplingCurve}
*/
function SamplingCurve(pSamplingCurveNode) {
  this.parseXmlElement(pSamplingCurveNode);
  return this;
}

/**
* To be implmented
* @type String
*/
SamplingCurve.prototype.length = "";

/**
* To be implmented
* @type String
*/
SamplingCurve.prototype.shape = "";

/**
* To be implemented.
* @param {DomXmlNode} pSamplingCurveNode The XML node for <b>samplingManifold:SamplingCurve</b>.
*/
function Location_parseXmlElement(pSamplingCurveNode) {
  if (g_IsIE) {
    pSamplingCurveNode.setProperty("SelectionLanguage", "XPath");
  }
}