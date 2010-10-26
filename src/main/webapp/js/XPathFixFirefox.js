var g_IsIE = !!(window.attachEvent && !window.opera);

// check for XPath implementation
// Implement selectNodes and selectSingleNode for firefox
if (!g_IsIE && !Element.prototype.selectSingleNode)
{
	Element.prototype.selectSingleNode = function(sXPath) {
		var oEvaluator = new XPathEvaluator();
		var oResult = oEvaluator.evaluate(sXPath, this, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
		return oResult === null ? null : oResult.singleNodeValue;
	};
}

// check for XPath implementation
if( document.implementation.hasFeature("XPath", "3.0") ) {
  // prototying the XMLDocument
  XMLDocument.prototype.selectNodes = function(cXPathString, xNode) {
    if( !xNode ) { xNode = this; }
    var oNSResolver = this.createNSResolver(this.documentElement);
    var aItems = this.evaluate(cXPathString, xNode, oNSResolver, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
    var aResult = [];
    for( var i = 0; i < aItems.snapshotLength; i++) {
      aResult[i] = aItems.snapshotItem(i);
    }
    return aResult;
  };

  // prototying the Element
  Element.prototype.selectNodes = function(cXPathString) {
    if(this.ownerDocument.selectNodes) {
      return this.ownerDocument.selectNodes(cXPathString, this);
    } else {
      throw "For XML Elements Only";
    }
  };
}