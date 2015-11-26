/**
 * Class for making and then parsing a WMS request/response
 */
Ext.define('portal.layer.querier.wms.WMSQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Creates a BaseComponent rendered with an error message
     *
     * @param message The message string to put in the body of the generated component
     * @param tabTitle The title of the tab (defaults to 'Error')
     */
    generateErrorComponent : function(message, tabTitle) {
        return Ext.create('portal.layer.querier.BaseComponent', {
            html: Ext.util.Format.format('<p class="centeredlabel">{0}</p>', message),
            tabTitle : tabTitle ? tabTitle : 'Error'
        });
    },

    /**
     * Returns true if WMS GetFeatureInfo query returns data.
     *
     * We need to hack a bit here as there is not much that we can check for.
     * For example the data does not have to come in tabular format.
     * In addition html does not have to be well formed.
     * In addition an "empty" click can still send style information
     *
     * So ... we will assume that minimum html must be longer then 30 chars
     * eg. data string: <table border="1"></table>
     *
     * For a bit of safety lets only count the bytes in the body tag
     *
     * @param {iStr} HTML string content to be verified
     * @return {Boolean} Status of the
     */
    isHtmlDataThere : function(iStr) {
        //This isn't perfect and can technically fail
        //but it is "good enough" unless you want to start going mental with the checking
        var lowerCase = iStr.toLowerCase();

        //If we have something resembling well formed HTML,
        //We can test for the amount of data between the body tags
        var startIndex = lowerCase.indexOf('<body>');
        var endIndex = lowerCase.indexOf('</body>');
        if (startIndex >= 0 || endIndex >= 0) {
            return ((endIndex - startIndex) > 32);
        }

        //otherwise it's likely we've just been sent the contents of the body
        return lowerCase.length > 32;
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
    },

    _parseStringXMLtoTreePanel : function(xmlString){
        var domNode = portal.util.xml.SimpleDOM.parseStringToDOM(xmlString);
        var rootNode = this._createTreeNode(domNode.documentElement);
        this._parseXmlTree(domNode.documentElement, rootNode);
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
            height: 300,
            items : [{
                xtype : 'treepanel',
                autoScroll : true,
                rootVisible : true,
                root : rootNode
            }]
        };

        return panelConfig;
    },

    /**
     * See parent class for definition
     *
     * Makes a WMS request, waits for the response and then parses it passing the results to callback
     */
    query : function(queryTarget, callback) {
        var proxyUrl = this.generateWmsProxyQuery(queryTarget, 'text/html');
        Ext.Ajax.request({
            url: proxyUrl,
            timeout : 180000,
            scope : this,
            callback : function(options, success, response) {
                var cmp = null;

                if (!success) {
                    cmp = this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.');
                } else if (this.isHtmlDataThere(response.responseText)) {
                    cmp = Ext.create('portal.layer.querier.BaseComponent', {
                        autoScroll : true,
                        html: response.responseText
                    });
                }

                if (cmp !== null) {
                    callback(this, [cmp], queryTarget);
                } else {
                    callback(this, [], queryTarget);
                }
            }
        });
    }
});