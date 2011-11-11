/**
 * Utility functions for providing W3C DOM functionality with a single API for most browsers.
 *
 * Currently supported Browsers
 * IE 6-9
 * Mozilla Firefox 3+
 * Google Chrome
 */
Ext.ns('SimpleDOM');

//Constants
SimpleDOM.XML_NODE_ELEMENT = 1;
SimpleDOM.XML_NODE_ATTRIBUTE = 2;
SimpleDOM.XML_NODE_TEXT = 3;

/**
 * Utility for retrieving a W3C DOM Node 'localName' attribute across browsers.
 *
 * The localName is the node name without any namespace prefixes
 */
SimpleDOM.getNodeLocalName = function(domNode) {
    return domNode.localName ? domNode.localName : domNode.baseName;
};

/**
 * Figure out if domNode is a leaf or not
 * (Leaves have no nodes from XML_NODE_ELEMENT)
 */
SimpleDOM.isLeafNode = function(domNode) {
    var isLeaf = true;
    for ( var i = 0; i < domNode.childNodes.length && isLeaf; i++) {
        isLeaf = domNode.childNodes[i].nodeType != SimpleDOM.XML_NODE_ELEMENT;
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
SimpleDOM.filterNodeArray = function(nodeArray, nodeType, namespaceUri, nodeName) {
    var matchingNodes = [];
    for (var i = 0; i < nodeArray.length; i++) {
        var node = nodeArray[i];

        if (nodeType && node.nodeType !== nodeType) {
            continue;
        }

        if (namespaceUri && namespaceUri !== node.namespaceURI) {
            continue;
        }

        if (nodeName && nodeName !== node.localName) {
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
SimpleDOM.getMatchingChildNodes = function(domNode, childNamespaceURI, childNodeName) {
    return SimpleDOM.filterNodeArray(domNode.childNodes, SimpleDOM.XML_NODE_ELEMENT, childNamespaceURI, childNodeName);
},

/**
 * Gets all Attributes of domNode as an Array that match the specified filter parameters
 * @param childNamespaceURI [Optional] The URI to lookup as a String
 * @param childNodeName [Optional] The node name to lookup as a String
 */
SimpleDOM.getMatchingAttributes = function(domNode, attributeNamespaceURI, attributeName) {
    return SimpleDOM._filterNodeArray(domNode.attributes, SimpleDOM.XML_NODE_ATTRIBUTE, attributeNamespaceURI, attributeName);
};

/**
 * Given a DOM node, return its text content (however the browser defines it)
 */
SimpleDOM.getNodeTextContent = function(domNode) {
    return domNode.textContent ? domNode.textContent : domNode.text;
};
