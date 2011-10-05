/**
 * Class for making and then parsing a WFS request/response using a GenericParser.Parser class
 */
Ext.ns('GenericParser');
GenericParser.WFSParser = Ext.extend(Ext.util.Observable, {

    parser : null,
    wfsUrl : null,
    typeName : null,
    featureId : null,
    rootCfg : null,

    /**
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *  rootCfg : [Optional] Object - Passed (with modifications) to the constructor of the root
     *            GenericParser.BaseComponent object that gets generated
     *  wfsUrl : String - The WFS URL to query
     *  typeName : String - The WFS type to query
     *  featureId : String - WFS ID of the type to query
     * }
     */
    constructor : function(cfg) {
        GenericParser.WFSParser.superclass.constructor.call(this, cfg);

        this.parser = new GenericParser.Parser();
        this.wfsUrl = cfg.wfsUrl;
        this.typeName = cfg.typeName;
        this.featureId = cfg.featureId;
        this.rootCfg = cfg.rootCfg;
    },

    /**
     * Makes a WFS request, waits for the response and then parses it passing the results to callback
     * @param callback function(WFSParser this, GenericParser.BaseComponent rootComponent) -
     *                 Called whenever the internal WFS request is returned and parsed. rootComponent will be null if there has been a problem (connection problems, etc)
     */
    makeWFSRequest : function(callback) {

        if (!callback) {
            callback = Ext.emptyFn;
        }

        var wfsParser = this;
        Ext.Ajax.request( {
            url : 'requestFeature.do',
            params : {
                serviceUrl : this.wfsUrl,
                typeName : this.typeName,
                featureId : this.featureId
            },
            callback : function(options, success, response) {
                if (!success) {
                    callback(wfsParser, null);
                    return;
                }

                var jsonResponse = Ext.util.JSON.decode(response.responseText);
                if (!jsonResponse.success) {
                    callback(wfsParser, null);
                    return;
                }

                // Load our xml string into DOM
                var xmlString = jsonResponse.data.gml;
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
                    alert('Your web browser doesn\'t seem to support any form of XML to DOM parsing. Functionality will be affected');
                    callback(wfsParser, null);
                    return;
                }

                //Skip the opening containing elements (as they are constant for WFS)
                var wfsResponseRoot = xmlDocument.documentElement.childNodes[0].childNodes[0];

                //Parse our response, pass it along to the callback
                var rootComponent = wfsParser.parser.parseNode(wfsResponseRoot, wfsParser.wfsUrl, wfsParser.rootCfg);
                callback(wfsParser, rootComponent);
            }
        });
    }
});