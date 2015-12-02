/**
 * Utility functions for providing XPath functionality with a single API for most browsers.
 *
 * Supported functionality is only a subset of XPath (although you may get additional functionality
 * depending on browser).
 *      + Support for Axes is limited
 *      + Support for predicates is limited to only integer positions (no conditionals)
 *
 * Currently supported Browsers
 * IE 6-9
 * Mozilla Firefox 3+
 * Google Chrome
 */
Ext.ns('portal.util.xml.SimpleXPath');

//Constants
portal.util.xml.SimpleXPath.XPATH_STRING_TYPE = window.XPathResult ? XPathResult.STRING_TYPE : 0;
portal.util.xml.SimpleXPath.XPATH_UNORDERED_NODE_ITERATOR_TYPE = window.XPathResult ? XPathResult.UNORDERED_NODE_ITERATOR_TYPE : 1;

/**
 * A wrapper around the DOM defined Document.evaluate function
 *
 * Because not every browser supports document.evaluate we need to have a pure javascript
 * backup in place
 */
portal.util.xml.SimpleXPath.evaluateXPath = function(document, domNode, xPath, resultType) {
    if (document.evaluate) {
        return document.evaluate(xPath, domNode, document.createNSResolver(domNode), resultType, null);
    } else {
        //This gets us a list of dom nodes
        var matchingNodeArray = XPath.selectNodes(xPath, domNode);
        if (!matchingNodeArray) {
            matchingNodeArray = [];
        }

        //we need to turn that into an XPathResult object (or an emulation of one)
        switch(resultType) {
        case portal.util.xml.SimpleXPath.XPATH_STRING_TYPE:
            var stringValue = null;
            if (matchingNodeArray.length > 0) {
                stringValue = portal.util.xml.SimpleDOM.getNodeTextContent(matchingNodeArray[0]);
            }

            return {
                stringValue : stringValue
            };
        case portal.util.xml.SimpleXPath.XPATH_UNORDERED_NODE_ITERATOR_TYPE:
            return {
                _arr : matchingNodeArray,
                _i : 0,
                iterateNext : function() {
                    if (this._i >= this._arr.length) {
                        return null;
                    } else  {
                        return this._arr[this._i++];
                    }
                }
            };

        }

        throw 'Unrecognised resultType';
    }
};


/**
 * Evaluates an XPath which will return an array of W3C DOM nodes
 */
portal.util.xml.SimpleXPath.evaluateXPathNodeArray = function(domNode, xPath) {
    var document = domNode.ownerDocument;
    var xpathResult = null;
    try {
        xpathResult = portal.util.xml.SimpleXPath.evaluateXPath(document, domNode, xPath, portal.util.xml.SimpleXPath.XPATH_UNORDERED_NODE_ITERATOR_TYPE);
    } catch(err) {
        return [];
    }
    var matchingNodes = [];

    var matchingNode = xpathResult.iterateNext();
    while (matchingNode) {
        matchingNodes.push(matchingNode);
        matchingNode = xpathResult.iterateNext();
    }

    return matchingNodes;
};

/**
 * Evaluates an Xpath for returning a string
 */
portal.util.xml.SimpleXPath.evaluateXPathString = function(domNode, xPath) {
    var document = domNode.ownerDocument;
    var xpathResult = portal.util.xml.SimpleXPath.evaluateXPath(document, domNode, xPath, portal.util.xml.SimpleXPath.XPATH_STRING_TYPE);
    return xpathResult.stringValue;
};