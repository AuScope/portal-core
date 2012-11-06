/**
 * Class for making and then parsing a WFS request/response using a GenericParser.Parser class
 */
Ext.define('portal.layer.querier.wfs.WFSWithMapQuerier', {
    extend: 'portal.layer.querier.Querier',

    featureSource : null,

    /**
     * {
     *  featureSource : [Optional] An portal.layer.querier.wfs.FeatureSource implementation. If omitted WFSFeatureSource will be used
     *  parser : portal.layer.querier.wfs.Parser instances to parse features in GUI elements
     *  knownLayerParser : portal.layer.querier.wfs.KnownLayerParser to parse features in GUI elements
     * }
     */
    constructor: function(config){

        if (config.featureSource) {
            this.featureSource = config.featureSource;
        } else {
            this.featureSource = Ext.create('portal.layer.querier.wfs.featuresources.WFSFeatureSource', {});
        }

        this.parser = config.parser ? config.parser : Ext.create('portal.layer.querier.wfs.Parser', {});
        this.knownLayerParser = config.knownLayerParser ? config.knownLayerParser : Ext.create('portal.layer.querier.wfs.KnownLayerParser', {});

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },


    _generateErrorComponent : function(message) {
        return Ext.create('portal.layer.querier.BaseComponent', {
            html: Ext.util.Format.format('<p class="centeredlabel">{0}</p>', message)
        });
    },


    /**
     * See parent class for definition
     *
     * Makes a WFS request, waits for the response and then parses it passing the results to callback
     */
    query : function(queryTarget, callback) {

        var onlineResource = queryTarget.get('onlineResource');

        if(onlineResource.get('type')==='WMS'){
             this._handleWMSQuery(queryTarget,callback,this);
        }else if(onlineResource.get('type')=='WFS'){
            this._handleWFSQuery(queryTarget, callback);
        }

    },



    _handleWMSQuery : function(queryTarget,callback,scope){
        //VT:app-schema wms requires the gml version to be declared in the info_format
        var proxyUrl = this.generateWmsProxyQuery(queryTarget, 'application/vnd.ogc.gml/3.1.1');

        //Start off by making a request for the GML at the specified location
        //We need to extract the survey line ID of the place we clicked
        Ext.Ajax.request({
            url : proxyUrl,
            timeout : 180000,
            scope : this,
            callback : function(options, success, response) {
                if (!success) {
                    callback(this, [this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.')], queryTarget);
                    return;
                }

                //TODO: There is a convergence here between this and the WFSQuerier (parsing a wfs:FeatureCollection)
                var domDoc = portal.util.xml.SimpleDOM.parseStringToDOM(response.responseText);
                var featureMemberNodes = portal.util.xml.SimpleDOM.getMatchingChildNodes(domDoc.documentElement, 'http://www.opengis.net/gml', 'featureMember');
                if (featureMemberNodes.length === 0) {
                    featureMemberNodes = portal.util.xml.SimpleDOM.getMatchingChildNodes(domDoc.documentElement, 'http://www.opengis.net/gml', 'featureMembers');
                }
                if (featureMemberNodes.length === 0 || featureMemberNodes[0].childNodes.length === 0) {
                    //we got an empty response - likely because the feature ID DNE.
                    callback(this, [], queryTarget);
                    return;
                }

                var featureTypeRoot = featureMemberNodes[0].childNodes[0];

                //Extract the line ID of what we clicked
                var id = portal.util.xml.SimpleXPath.evaluateXPathString(featureTypeRoot, '@gml:id');


                //VT:handle the wfs download

                var layer = queryTarget.get('layer');
                //var id = queryTarget.get('id');
                var layer = queryTarget.get('layer');
                var onlineResource = queryTarget.get('onlineResource');
                var typeName = onlineResource.get('name');
                var wfsUrl = onlineResource.get('url');


                //we need to get a reference to the parent known layer (if it is a known layer)
                var knownLayer = null;
                if (layer.get('sourceType') === portal.layer.Layer.KNOWN_LAYER) {
                    knownLayer = layer.get('source');
                }

                //Download the DOM of the feature we are interested in
                var me = this;
                this.featureSource.getFeature(id, typeName, wfsUrl, function(wfsResponseRoot, id, typeName, wfsUrl) {
                    if (!wfsResponseRoot) {
                        callback(me, [me._generateErrorComponent(Ext.util.Format.format('There was a problem when looking up the feature with id \"{0}\"', id))], queryTarget);
                        return;
                    }

                    //Parse our response into a number of GUI components, pass those along to the callback
                    var allComponents = [];
                    allComponents.push(me.parser.parseNode(wfsResponseRoot, onlineResource.get('url')));
                    if (knownLayer && me.knownLayerParser.canParseKnownLayerFeature(queryTarget.get('id'), knownLayer, onlineResource, layer)) {
                        allComponents.push(me.knownLayerParser.parseKnownLayerFeature(queryTarget.get('id'), knownLayer, onlineResource, layer));
                    }

                    callback(me, allComponents, queryTarget);
                });

            }
        });
    },

    _handleWFSQuery : function(queryTarget, callback){

        //This class can only query for specific WFS feature's
        var id = queryTarget.get('id');
        var onlineResource = queryTarget.get('onlineResource');
        var layer = queryTarget.get('layer');
        var typeName = onlineResource.get('name');
        var wfsUrl = onlineResource.get('url');

        //we need to get a reference to the parent known layer (if it is a known layer)
        var knownLayer = null;
        if (layer.get('sourceType') === portal.layer.Layer.KNOWN_LAYER) {
            knownLayer = layer.get('source');
        }

        //Download the DOM of the feature we are interested in
        var me = this;
        this.featureSource.getFeature(id, typeName, wfsUrl, function(wfsResponseRoot, id, typeName, wfsUrl) {
            if (!wfsResponseRoot) {
                callback(me, [me._generateErrorComponent(Ext.util.Format.format('There was a problem when looking up the feature with id \"{0}\"', id))], queryTarget);
                return;
            }

            //Parse our response into a number of GUI components, pass those along to the callback
            var allComponents = [];
            allComponents.push(me.parser.parseNode(wfsResponseRoot, onlineResource.get('url')));
            if (knownLayer && me.knownLayerParser.canParseKnownLayerFeature(queryTarget.get('id'), knownLayer, onlineResource, layer)) {
                allComponents.push(me.knownLayerParser.parseKnownLayerFeature(queryTarget.get('id'), knownLayer, onlineResource, layer));
            }

            callback(me, allComponents, queryTarget);
        });
    }

});