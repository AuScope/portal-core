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
     * Makes a URL that when queried will cause the backend to proxy a WFS request to wfsUrl for a type with a specific ID.
     * The resulting XML will be returned.
     *
     * @param wfsUrl String - WFS url to query
     * @param typeName String - WFS type to query
     * @param featureTypeId String - the ID of the type to query
     */
    _makeFeatureRequestUrl : function(wfsUrl, typeName, featureTypeId) {
        return window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/" + "requestFeature.do" + "?" +
            "serviceUrl=" + wfsUrl + "&typeName=" + typeName +
            "&featureId=" + featureTypeId;
    }
});