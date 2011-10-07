/**
 * Abstract base class for all Generic Parser factories to inherit from.
 */
Ext.ns('GenericParser.Factory');

GenericParser.Factory.BaseFactory = Ext.extend(Ext.util.Observable, {

    //Namespace Constants
    XMLNS_GSML_2 : 'urn:cgi:xmlns:CGI:GeoSciML:2.0',
    XMLNS_GML : 'http://www.opengis.net/gml',
    XMLNS_SA : 'http://www.opengis.net/sampling/1.0',
    XMLNS_OM : 'http://www.opengis.net/om/1.0',
    XMLNS_SWE : 'http://www.opengis.net/swe/1.0.1',

    //DOM constants
    XML_NODE_ELEMENT : 1,
    XML_NODE_ATTRIBUTE : 2,
    XML_NODE_TEXT : 3,

    //XPath Constants (have to be copied due to IE)
    XPATH_STRING_TYPE : undefined,
    XPATH_UNORDERED_NODE_ITERATOR_TYPE : undefined,


    //Reference back to genericParser that spawned this factory. Use
    //this reference to parse nodes that your factory cannot handle.
    genericParser : null,

    /**
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *  genericParser : GenericParser - the generic parser that owns this factory
     * }
     */
    constructor : function(cfg) {
        this.genericParser = cfg.genericParser;

        //IE and Safari dont have xPath so we need to work around that
        if (Ext.isIE || Ext.isSafari) {
            this.XPATH_STRING_TYPE = 0;
            this.XPATH_UNORDERED_NODE_ITERATOR_TYPE = 1;
        } else {
            this.XPATH_STRING_TYPE = XPathResult.STRING_TYPE;
            this.XPATH_UNORDERED_NODE_ITERATOR_TYPE = XPathResult.UNORDERED_NODE_ITERATOR_TYPE;
        }

        GenericParser.Factory.BaseFactory.superclass.constructor.call(this, cfg);
    },

    /**
     * abstract - Must be overridden by extending classes
     * This function will return true if this factory is capable of generating a
     * GenericParserComponent for the specified DOM node.
     *
     * Otherwise false must be returned
     *
     * @param domNode A W3C DOM Node object
     */
    supportsNode : function(domNode) {
        return false;
    },

    /**
     * abstract - Must be overridden by extending classes
     * This function must return a GenericParserComponent that represents
     * domNode.
     *
     * @param domNode A W3C DOM Node object
     * @param wfsUrl The URL of the WFS where domNode was sourced from
     * @param rootCfg a configuration object to be applied to the root GenericParser.BaseComponent
     */
    parseNode : function(domNode, wfsUrl, rootCfg) {
        return new GenericParser.BaseComponent(rootCfg);
    },

    /**
     * Utility for retrieving a W3C DOM Node 'localName' attribute across browsers.
     *
     * The localName is the node name without any namespace prefixes
     */
    _getNodeLocalName : function(domNode) {
        return domNode.localName ? domNode.localName : domNode.baseName;
    },

    /**
     * Figure out if domNode is a leaf or not
     * (Leaves have no nodes from XML_NODE_ELEMENT)
     */
    _isLeafNode : function(domNode) {
        var isLeaf = true;
        for ( var i = 0; i < domNode.childNodes.length && isLeaf; i++) {
            isLeaf = domNode.childNodes[i].nodeType != this.XML_NODE_ELEMENT;
        }

        return isLeaf;
    },

    /**
     * Filters an array of DOM Nodes according to the specified parameters
     * @param nodeArray An Array of DOM Nodes
     * @param nodeType [Optional] An integer node type
     * @param namespaceUri [Optional] String to compare against node namespaceURI
     * @param nodeName [Optional] String to compare against the node localName
     */
    _filterNodeArray : function(nodeArray, nodeType, namespaceUri, nodeName) {
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
    },

    /**
     * Gets all children of domNode as an Array that match the specified filter parameters
     * @param childNamespaceURI [Optional] The URI to lookup as a String
     * @param childNodeName [Optional] The node name to lookup as a String
     */
    _getMatchingChildNodes : function(domNode, childNamespaceURI, childNodeName) {
        return this._filterNodeArray(domNode.childNodes, this.XML_NODE_ELEMENT, childNamespaceURI, childNodeName);
    },

    /**
     * Gets all Attributes of domNode as an Array that match the specified filter parameters
     * @param childNamespaceURI [Optional] The URI to lookup as a String
     * @param childNodeName [Optional] The node name to lookup as a String
     */
    _getMatchingAttributes : function(domNode, attributeNamespaceURI, attributeName) {
        return this._filterNodeArray(domNode.attributes, this.XML_NODE_ATTRIBUTE, attributeNamespaceURI, attributeName);
    },

    /**
     * Given a DOM node, return its text content (however the browser defines it)
     */
    _getNodeTextContent : function(domNode) {
        return domNode.textContent ? domNode.textContent : domNode.text;
    },

    /**
     * Makes a HTML string containing an Anchor element with the specified content.
     * The anchor element will be configured to open a WFS Popup window on click that gets
     * data from the specified URL
     */
    _makeWFSPopupHtml : function(wfsUrl, typeName, featureId, content, qtip) {
        return String.format('<a href="#" qtip="{4}" onclick="var w=window.open(\'wfsFeaturePopup.do?url={0}&typeName={1}&featureId={2}\',\'AboutWin\',\'toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820\');w.focus();return false;">{3}</a>',escape(wfsUrl), escape(typeName), escape(featureId), content, qtip ? qtip : '');
    },

    /**
     * Makes a HTML string containing an anchor with the specified content.
     * The anchor element will be configured to open another window on click that gets
     * data from the specified URL
     */
    _makeGeneralPopupHtml : function(url, content, qtip) {
        return String.format('<a href="#" qtip="{2}" onclick="var w=window.open(\'{0}\',\'AboutWin\',\'toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820\');w.focus();return false;">{1}</a>',url, content, qtip ? qtip : '');
    },

    /**
     * Makes a HTML string containing an Anchor element with the specified content.
     * The anchor element will be configured to open a RDF Popup window on click that gets
     * data from the specified URI
     */
    _makeVocabPopupHtml : function(conceptUri, content, qtip) {
        var vocabUrl = VOCAB_SERVICE_URL;
        if (vocabUrl[vocabUrl.length - 1] !== '/') {
            vocabUrl += '/';
        }
        vocabUrl += 'getConceptByURI?';

        return String.format('<a href="#" qtip="{3}" onclick="var w=window.open(\'{0}{1}\',\'AboutWin\',\'toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820\');w.focus();return false;">{2}</a>', vocabUrl, conceptUri, content, qtip ? qtip : '');
    },

    /**
     * Because not every browser supports document.evaluate we need to have a pure javascript
     * backup in place
     */
    _evaluateXPath : function(document, domNode, xPath, resultType) {
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
            case this.XPATH_STRING_TYPE:
                var stringValue = null;
                if (matchingNodeArray.length > 0) {
                    stringValue = this._getNodeTextContent(matchingNodeArray[0]);
                }

                return {
                    stringValue : stringValue
                };
            case this.XPATH_UNORDERED_NODE_ITERATOR_TYPE:
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
    },

    /**
     * Evaluates an Xpath for returning a node array
     */
    _evaluateXPathNodeArray : function(domNode, xPath) {
        var document = domNode.ownerDocument;
        var xpathResult = null;
        try {
            xpathResult = this._evaluateXPath(document, domNode, xPath, this.XPATH_UNORDERED_NODE_ITERATOR_TYPE);
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
    },

    /**
     * Evaluates an Xpath for returning a string
     */
    _evaluateXPathString : function(domNode, xPath) {
        var document = domNode.ownerDocument;
        var xpathResult = this._evaluateXPath(document, domNode, xPath, this.XPATH_STRING_TYPE);
        return xpathResult.stringValue;
    }
});