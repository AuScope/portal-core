/**
 * Class for making and then parsing a WFS request/response using a GenericParser.Parser class
 */
Ext.define('portal.layer.querier.wfs.WFSQuerier', {
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
        //This class can only query for specific WFS feature's
        var id = queryTarget.get('id');
        var onlineResource = queryTarget.get('onlineResource');
        var layer = queryTarget.get('layer');
        var typeName = onlineResource.get('name');
        var wfsUrl = onlineResource.get('url');
        var applicationProfile = queryTarget.get('onlineResource').get('applicationProfile');

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
            allComponents.push(me.parser.parseNode(wfsResponseRoot, onlineResource.get('url'), applicationProfile));
            if (knownLayer && me.knownLayerParser.canParseKnownLayerFeature(queryTarget.get('id'), knownLayer, onlineResource, layer)) {
                var knownLayerFeature = me.knownLayerParser.parseKnownLayerFeature(queryTarget.get('id'), knownLayer, onlineResource, layer);
                if(knownLayerFeature){
                    allComponents.push(knownLayerFeature);
                }
            }

            callback(me, allComponents, queryTarget);
        });
    }
});