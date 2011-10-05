/**
 * Abstract base class for all Generic Parser factories to inherit from.
 */
Ext.ns('GenericParser.Factory');
GenericParser.Factory.SimpleFactory = Ext.extend(GenericParser.Factory.BaseFactory, {

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        GenericParser.Factory.SimpleFactory.superclass.constructor.call(this, cfg);
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
    parseNode : function(domNode, wfsUrl, rootCfg) {
        // Turn our DOM Node in an ExtJS Tree
        var rootNode = this._createTreeNode(domNode);
        var gmlId = this._evaluateXPathString(domNode, '@gml:id');
        this._parseXmlTree(domNode, rootNode);
        rootNode.expanded = true;

        // Continuously expand child nodes until we hit a node with
        // something "interesting"
        if (rootNode.childNodes.length == 1) {
            var childNode = rootNode.childNodes[0];
            while (childNode) {
                childNode.expanded = true;

                if (childNode.childNodes.length > 1) {
                    break;
                } else {
                    childNode = childNode.childNodes[0];
                }
            }
        }

        Ext.apply(rootCfg, {
            layout : 'fit',
            height : 300,
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
                    var getXmlUrl = window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/" + "requestFeature.do" + "?" +
                        "serviceUrl=" + wfsUrl + "&typeName=" + domNode.nodeName +
                        "&featureId=" + gmlId;

                    var url = 'downloadGMLAsZip.do?serviceUrls=' + escape(getXmlUrl);
                    FileDownloader.downloadFile(url);
                }
            }]
         });

        return new GenericParser.BaseComponent(rootCfg);
    },

    /**
     * This is for creating a Ext.tree.TreeNode from a DOM Node
     */
    _createTreeNode : function(documentNode) {
        var treeNode = null;

        // We have a leaf
        if (this._isLeafNode(documentNode)) {
            var textContent = this._getNodeTextContent(documentNode);

            treeNode = new Ext.tree.TreeNode( {
                text : documentNode.tagName + " = " + textContent
            });
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
            treeNode = new Ext.tree.TreeNode( {
                text : parentName
            });
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
            if (docNodeChild.nodeType == this.XML_NODE_ELEMENT) {
                var treeChildNode = this._createTreeNode(docNodeChild);
                treeNode.appendChild(treeChildNode);
                nodes.push(treeNode);
                this._parseXmlTree(docNodeChild, treeChildNode);
            }
        }, this);

        return nodes;
    }
});