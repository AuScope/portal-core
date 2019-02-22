/**
 * Abstract base class for all Parser factories to inherit from.
 */
Ext.define('portal.layer.querier.wfs.factories.BaseFactory', {
    extend : 'Ext.util.Observable',

    //Namespace Constants
    XMLNS_ER : 'urn:cgi:xmlns:GGIC:EarthResource:1.1',
    XMLNS_ER_2 : 'http://xmlns.earthresourceml.org/EarthResource/2.0',
    XMLNS_ERL : 'http://xmlns.earthresourceml.org/earthresourceml-lite/1.0',
    XMLNS_GSML_2 : 'urn:cgi:xmlns:CGI:GeoSciML:2.0',
    XMLNS_GSML_32 : 'http://xmlns.geosciml.org/GeoSciML-Core/3.2',
    XMLNS_GML : 'http://www.opengis.net/gml',
    XMLNS_GML_32 : 'http://www.opengis.net/gml/3.2',
    XMLNS_SA : 'http://www.opengis.net/sampling/1.0',
    XMLNS_OM : 'http://www.opengis.net/om/1.0',
    XMLNS_SWE : 'http://www.opengis.net/swe/1.0.1',
    XMLNS_GSMLP : 'http://xmlns.geosciml.org/geosciml-portrayal/2.0',
    XMLNS_GSMLP_4 : 'http://xmlns.geosciml.org/geosciml-portrayal/4.0',
    XMLNS_MT : 'http://xmlns.geoscience.gov.au/mineraltenementml/1.0',
    XMLNS_MO : 'http://xmlns.geoscience.gov.au/minoccml/1.0',
    XMLNS_RA : 'http://remanentanomalies.csiro.au',
    XMLNS_CAPDF : 'http://capdf.csiro.au/',
    XMLNS_TIMA : 'https://ogc-jdlc.curtin.edu.au/ns/tima', 

    config : {
        //Reference back to portal.layer.querier.wfs.Parser that spawned this factory. Use
        //this reference to parse nodes that your factory cannot handle.
        parser : null
    },

    /**
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *  parser : portal.layer.querier.wfs.Parser - the parser that owns this factory
     * }
     */
    constructor : function(cfg) {
        this.listeners = cfg.listeners;
        this.callParent(arguments);
    },

    /**
     * abstract - Must be overridden by extending classes
     * This function will return true if this factory is capable of generating a
     * GenericParserComponent for the specified DOM node.
     *
     * Otherwise false must be returned
     *
     * function(domNode)
     *
     * domNode - A W3C DOM Node object
     */
    supportsNode : portal.util.UnimplementedFunction,

    /**
     * abstract - Must be overridden by extending classes
     * This function must return a GenericParserComponent that represents
     * domNode.
     *
     * function(domNode, wfsUrl, rootCfg)
     *
     * domNode - A W3C DOM Node object
     * wfsUrl - The URL of the WFS where domNode was sourced from
     * rootCfg - a configuration object to be applied to the root GenericParser.BaseComponent
     */
    parseNode : portal.util.UnimplementedFunction,


    /**
     * Filters an array of DOM nodes based on the value of an xPath for each node
     * @param nodeList an array of DOM nodes
     * @param xPath String based xPath expression
     * @param value String based comparison value
     */
    _filterNodesWithXPath : function(nodeList, xPath, value) {
        var filteredNodes = [];
        for (var i = 0; i < nodeList.length; i++) {
            if (portal.util.xml.SimpleXPath.evaluateXPathString(nodeList[i], xPath) === value) {
                filteredNodes.push(nodeList[i]);
            }
        }
        return filteredNodes;
    },

    /**
     * Makes a HTML string containing an Anchor element with the specified content.
     * The anchor element will be configured to open a WFS Popup window on click that gets
     * data from the specified URL
     */
    _makeWfsUriPopupHtml : function(uri, content, qtip) {
        return Ext.util.Format.format('<a href="#" qtip="{2}" onclick="var w=window.open(\'wfsFeaturePopup.do?url={0}\',\'AboutWin\',\'toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820\');w.focus();return false;">{1}</a>', uri, content, qtip ? qtip : '');
    },

    /**
     * Makes a HTML string containing an Anchor element with the specified content.
     * The anchor element will be configured to open a WFS Popup window on click that gets
     * data from the specified URL
     */
    _makeWFSPopupHtml : function(wfsUrl, typeName, featureId, content, qtip) {
        var url = Ext.util.Format.format('{0}&typeName={1}&featureId={2}', wfsUrl, typeName, featureId);
        return this._makeWfsUriPopupHtml(url, content, qtip);
    },

    /**
     * Makes a HTML string containing an anchor with the specified content.
     * The anchor element will be configured to open another window on click that gets
     * data from the specified URL
     */
    _makeGeneralPopupHtml : function(url, content, qtip) {
        return Ext.util.Format.format('<a href="#" qtip="{2}" onclick="var w=window.open(\'{0}\',\'AboutWin\',\'toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820\');w.focus();return false;">{1}</a>',url, content, qtip ? qtip : '');
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

        return Ext.util.Format.format('<a href="#" qtip="{3}" onclick="var w=window.open(\'{0}{1}\',\'AboutWin\',\'toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820\');w.focus();return false;">{2}</a>', vocabUrl, conceptUri, content, qtip ? qtip : '');
    },

    /**
     * Makes a URL that when queried will cause the backend to proxy a WFS request to wfsUrl for a type with a specific ID.
     * The resulting XML will be returned.
     *
     * @param wfsUrl String - WFS url to query
     * @param typeName String - WFS type to query
     * @param featureTypeId String - the ID of the type to query
     */
    _makeFeatureRequestUrl : function(wfsUrl, typeName, featureTypeId, optionalParams) {
        var result = portal.util.URL.base + "requestFeature.do" + "?" +
            "serviceUrl=" + wfsUrl + "&typeName=" + typeName +
            "&featureId=" + featureTypeId;
        if(optionalParams){
            for (var i=0; i < optionalParams.length; i++){
                result = result + "&" + optionalParams[i].key + "=" + optionalParams[i].value
            }
        }

        return result;
    },


    /**
     * Parse a wfs request to the spatial server
     * The resulting XML will be returned.
     *
     * @param wfsUrl String - WFS url to query
     * @param typeName String - WFS type to query
     * @param featureTypeId String - the ID of the type to query
     */
    _makeWFSFeatureRequestUrl : function(wfsUrl, typeName, featureTypeId, optionalParams) {

        // VT: ugly hack to convert a wms url to a wfs url. This is ugly because in Openlayer.map getQueryTarget,
        // we hav to iterate through a wms resource to make a GetFeatureInfo request. we did not design it to handle
        // wfs resources. this solution will have to suffice for now until we see the need to redesign the QueryTarget object.
        // I thought of adding wfsonlineresource but that would just add more confusion. so far we only require the wfs url. if the
        // need arise we can then refactor.
        wfsUrl=wfsUrl.substring(0,wfsUrl.indexOf("?"));
        if(wfsUrl.substring((wfsUrl.length -3),wfsUrl.length).toLowerCase() == "wms"){
            wfsUrl=wfsUrl.substring(0,(wfsUrl.length - 3));
        }
        wfsUrl = wfsUrl + "wfs";

        var result = wfsUrl + "?service=WFS&version=1.1.0&request=GetFeature&typeName=" + typeName +
            "&featureId=" + featureTypeId;
        if(optionalParams){
            for (var i=0; i < optionalParams.length; i++){
                result = result + "&" + optionalParams[i].key + "=" + optionalParams[i].value
            }
        }

        return result;
    },

    /**
     * Decomposes a 'normal' URL in the form http://url.com/long/path/name to just its prefix + hostname http://url.com
     * @param url The url to decompose
     */
    _getBaseUrl : function(url) {
        var splitUrl = url.split('://'); //this should split us into 2 parts
        return splitUrl[0] + '://' + splitUrl[1].slice(0, splitUrl[1].indexOf('/'));
    }
});