/**
 * A simple extension of the BaseFactory class.
 * It is capable of representing a domNode as a basic tree-like structure.
 */
Ext.define('portal.layer.querier.wfs.factories.SimpleFactory', {
    extend : 'portal.layer.querier.wfs.factories.BaseFactory',

    /**
     * Accepts all portal.layer.querier.wfs.factories.BaseFactory configuration options
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    /**
     * The simple node supports EVERY type of node as it only displays simplistic information
     * and is intended to be the 'catch all' factory for parsing nodes that have no specific
     * factory written
     */
    supportsNode : function(domNode) {
        return true;
    },


    /**
     * Generates a simple tree panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl) {
        // Turn our DOM Node in an ExtJS Tree
        var rootNode = this._createTreeNode(domNode);
        var gmlId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, '@gml:id');
        var sf = this;
        this._parseXmlTree(domNode, rootNode);
        rootNode.expanded = true;

        // Continuously expand child nodes until we hit a node with
        // something "interesting" defined as a node with more than 1 child
        if (rootNode.children.length == 1) {
            var childNode = rootNode.children[0];
            while (childNode) {
                childNode.expanded = true;

                if (childNode.children.length > 1) {
                    break;
                } else {
                    childNode = childNode.children[0];
                }
            }
        }

        var panelConfig = {
            layout : 'fit',
            tabTitle : gmlId,
            height: 300,
            items : [{
                xtype : 'treepanel',
                autoScroll : true,
                rootVisible : true,
                root : rootNode
            }],
            buttons : [{
                xtype : 'button',
                text : 'Download Feature',
                iconCls : 'download',
                handler : function() {
                    var getXmlUrl = sf._makeFeatureRequestUrl(wfsUrl, domNode.nodeName, gmlId);
                    portal.util.FileDownloader.downloadFile('downloadGMLAsZip.do',{
                        serviceUrls : getXmlUrl
                    });
                }
            }]
        };

        return Ext.create('portal.layer.querier.BaseComponent', panelConfig);
    },

    /**
     * This is for creating a Node Objects from a DOM Node in the form
     * {
     *  text : String
     *  leaf : Boolean
     * }
     */
    _createTreeNode : function(documentNode) {
        var treeNode = null;

        // We have a leaf
        if (portal.util.xml.SimpleDOM.isLeafNode(documentNode)) {
            var textContent = portal.util.xml.SimpleDOM.getNodeTextContent(documentNode);

            treeNode = {
                text : documentNode.tagName + " = " + textContent,
                children : [],
                leaf: true
            };
        } else { // we have a parent node
            var parentName = documentNode.tagName;
            if (documentNode.attributes.length > 0) {
                parentName += '(';
                for ( var i = 0; i < documentNode.attributes.length; i++) {
                    parentName += ' ' + documentNode.attributes[i].nodeName +
                                  '=' + documentNode.attributes[i].value;
                }
                parentName += ')';
            }
            treeNode = {
                text : parentName,
                children : [],
                leaf: true
            };
        }

        return treeNode;
    },

    /**
     * Given a DOM tree starting at xmlDocNode, this function returns the
     * equivelant tree in ExtJs Tree Nodes
     */
    _parseXmlTree : function(xmlDocNode, treeNode) {
        var nodes = [];
        Ext.each(xmlDocNode.childNodes, function(docNodeChild) {
            if (docNodeChild.nodeType == portal.util.xml.SimpleDOM.XML_NODE_ELEMENT) {
                var treeChildNode = this._createTreeNode(docNodeChild);
                treeNode.leaf = false;
                treeNode.children.push(treeChildNode);
                nodes.push(treeNode);
                this._parseXmlTree(docNodeChild, treeChildNode);
            }
        }, this);

        return nodes;
    }
});