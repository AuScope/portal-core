/**
 * Utility functions for providing W3C DOM functionality with a single API for most browsers.
 *
 * Currently supported Browsers
 * IE 6-9
 * Mozilla Firefox 3+
 * Google Chrome
 */
Ext.ns('portal.util.xml.SimpleDOM');

//Constants
portal.util.xml.SimpleDOM.XML_NODE_ELEMENT = 1;
portal.util.xml.SimpleDOM.XML_NODE_ATTRIBUTE = 2;
portal.util.xml.SimpleDOM.XML_NODE_TEXT = 3;

/**
 * Utility for retrieving a W3C DOM Node 'localName' attribute across browsers.
 *
 * The localName is the node name without any namespace prefixes
 */
portal.util.xml.SimpleDOM.getNodeLocalName = function(domNode) {
    return domNode.localName ? domNode.localName : domNode.baseName;
};

/**
 * Returns the set of classes this node belongs to as an array of strings
 */
portal.util.xml.SimpleDOM.getClassList = function(domNode) {
    if (domNode.classList) {
        return domNode.classList;
    } else if (domNode['class']) {
        return domNode['class'].split(' ');
    } else if (domNode.className) {
        return domNode.className.split(' ');
    }
    return [];
};

/**
 * Figure out if domNode is a leaf or not
 * (Leaves have no nodes from XML_NODE_ELEMENT)
 */
portal.util.xml.SimpleDOM.isLeafNode = function(domNode) {
    var isLeaf = true;
    for ( var i = 0; i < domNode.childNodes.length && isLeaf; i++) {
        isLeaf = domNode.childNodes[i].nodeType !== portal.util.xml.SimpleDOM.XML_NODE_ELEMENT;
    }

    return isLeaf;
};

/**
 * Filters an array of DOM Nodes according to the specified parameters
 * @param nodeArray An Array of DOM Nodes
 * @param nodeType [Optional] An integer node type
 * @param namespaceUri [Optional] String to compare against node namespaceURI
 * @param nodeName [Optional] String to compare against the node localName
 */
portal.util.xml.SimpleDOM.filterNodeArray = function(nodeArray, nodeType, namespaceUri, nodeName) {
    var matchingNodes = [];
    for (var i = 0; i < nodeArray.length; i++) {
        var node = nodeArray[i];

        if (nodeType && node.nodeType !== nodeType) {
            continue;
        }

        if (namespaceUri && namespaceUri !== node.namespaceURI) {
            continue;
        }

        if (nodeName && nodeName !== portal.util.xml.SimpleDOM.getNodeLocalName(node)) {
            continue;
        }

        matchingNodes.push(node);
    }

    return matchingNodes;
};

/**
 * Gets all children of domNode as an Array that match the specified filter parameters
 * @param childNamespaceURI [Optional] The URI to lookup as a String
 * @param childNodeName [Optional] The node name to lookup as a String
 */
portal.util.xml.SimpleDOM.getMatchingChildNodes = function(domNode, childNamespaceURI, childNodeName) {
    return portal.util.xml.SimpleDOM.filterNodeArray(domNode.childNodes, portal.util.xml.SimpleDOM.XML_NODE_ELEMENT, childNamespaceURI, childNodeName);
};

/**
 * Gets all Attributes of domNode as an Array that match the specified filter parameters
 * @param childNamespaceURI [Optional] The URI to lookup as a String
 * @param childNodeName [Optional] The node name to lookup as a String
 */
portal.util.xml.SimpleDOM.getMatchingAttributes = function(domNode, attributeNamespaceURI, attributeName) {
    return portal.util.xml.SimpleDOM._filterNodeArray(domNode.attributes, portal.util.xml.SimpleDOM.XML_NODE_ATTRIBUTE, attributeNamespaceURI, attributeName);
};

/**
 * Given a DOM node, return its text content (however the browser defines it)
 */
portal.util.xml.SimpleDOM.getNodeTextContent = function(domNode) {
    return domNode.textContent ? domNode.textContent : domNode.text;
};

/**
 * Parse string to DOM
 */
portal.util.xml.SimpleDOM.parseStringToDOM = function(xmlString){
    var isIE11 = !!navigator.userAgent.match(/Trident.*rv[ :]*11\./)
    // Load our xml string into DOM
    var xmlDocument = null;
    if(window.DOMParser) {
        //browser supports DOMParser
        var parser = new DOMParser();
        xmlDocument = parser.parseFromString(xmlString, "text/xml");
    } else if(window.ActiveXObject) {
        //IE
        xmlDocument = new ActiveXObject("Microsoft.XMLDOM");
        xmlDocument.async="false";
        xmlDocument.loadXML(xmlString);
    } else {
        return null;
    }
    return xmlDocument;
};
