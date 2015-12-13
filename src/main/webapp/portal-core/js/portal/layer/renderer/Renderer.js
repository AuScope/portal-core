/**
 * Renderer is an abstract class representing the process of
 * requesting and displaying data from a data source.
 *
 *  events:
 *      renderstarted(portal.layer.renderer.Renderer this, portal.csw.OnlineResource[] resources, portal.layer.filterer.Filterer filterer)
 *          Fired whenever the renderer begins the process of rendering a new layer
 *      renderfinished(portal.layer.renderer.Renderer this)
 *          Fired whenever the renderer finishes the rendering of new data
 *      visibilitychanged(portal.layer.renderer.Renderer this, bool newVisibility)
 *          Fired whenever the layer's visibility changes
 */
Ext.define('portal.layer.renderer.Renderer', {
    extend: 'Ext.util.Observable',

    map : null, //portal.map.BaseMap
    visible : false, //whether the render is currently 'visible' or not,
    hasData : false, //whether the renderer has rendered any data or not,
    proxyUrl : '',  //a url to proxy data requests through (implementation specific)
    proxyCountUrl : '', //a url to proxy data count requests through (implementation specific)
    parentLayer : null, // a reference to the portal.layer.Layer that owns this renderer
    renderDebuggerData : null,
    renderStatus : null,
    primitiveManager : null,

    /**
     * Expects a Ext.util.Observable config with the following additions
     * {
     *  map : An instance of a google map GMap2 object
     * }
     */
    constructor: function(config) {

      

        //Setup class variables
        this.listeners = config.listeners;
        this.map = config.map;
        this.parentLayer = config.parentLayer;
        this.primitiveManager = this.map.makePrimitiveManager();
        this.renderStatus = Ext.create('portal.layer.renderer.RenderStatus', {}); //for maintaining the status of rendering,
        this.renderDebuggerData = Ext.create('portal.layer.renderer.RenderDebuggerData', {}); //for maintaining debug info about underlying requests

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * An abstract function for displaying data from a variety of data sources. This function will
     * raise the renderstarted and renderfinished events as appropriate. The effect of multiple calls
     * to this function (ie calling displayData again before renderfinished is raised) is undefined.
     *
     * This function will re-render itself entirely and thus may call removeData() during the normal
     * operation of this function
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer,
     *          function(portal.layer.renderer.Renderer this, portal.csw.OnlineResource[] resources, portal.layer.filterer.Filterer filterer, bool success) callback
     *
     * returns - void
     *
     * resources - an array of data sources which should be used to render data
     * filterer - A custom filter that can be applied to the specified data sources
     * callback - Will be called when the rendering process is completed and passed an instance of this renderer and the parameters used to call this function
     */
    displayData : portal.util.UnimplementedFunction,

    /**
     * An abstract function for aborting the display process. If this function is called any
     * in process rendering should attempt to be halted (if possible). If no rendering is underway
     * then this function should have no effect
     *
     * This function will typically be called immediately prior to a remove with the expectation
     * that any existing data be removed from the map AND no more data be added to the map.
     *
     * function()
     *
     * returns - void
     */
    abortDisplay : portal.util.UnimplementedFunction,

    /**
     * An abstract function for creating a legend that can describe the displayed data. If no
     * such thing exists for this renderer then null should be returned.
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer)
     *
     * returns - portal.layer.legend.Legend or null
     *
     * resources - (same as displayData) an array of data sources which should be used to render data
     * filterer - (same as displayData) A custom filter that can be applied to the specified data sources
     */
    getLegend : portal.util.UnimplementedFunction,

    /**
     * An abstract function that is called when this layer needs to be permanently removed from the map.
     * In response to this function all rendered information should be removed
     *
     * function()
     *
     * returns - void
     */
    removeData : portal.util.UnimplementedFunction,

    ////////////////// Getter/Setters

    /**
     * A function for setting this layer's visibility.
     *  
     * visible - a bool
     * VT: not in use: Mark for deletion
     */
    getVisible : function() {
        return this.visible;
    },

    /**
     * A function for setting this layer's visibility.
     * 
     * visible - a bool
     * 
     * VT: not in use: Mark for deletion
     */
    setVisible : function(visible) {
        this.visible = visible;
        this.fireEvent('visibilitychanged', this, visible);
    },

    /**
     * Gets whether this renderer 'has data'. i.e. whether
     * this renderer has successfully been able to render information on the map
     */
    getHasData : function() {
        return this.hasData;
    },

    /**
     * Sets whether this renderer 'has data'. i.e. whether
     * this renderer has successfully been able to render information on the map
     */
    setHasData : function(hasData) {
        this.hasData = hasData;
    },

    /**
     * Gets the url to proxy data requests through (implementation specific)
     */
    getProxyUrl : function() {
        return this.proxyUrl;
    },

    /**
     * Gets the url to proxy data count requests through (implementation specific)
     */
    getProxyCountUrl : function() {
        return this.proxyCountUrl;
    },

    /**
     * Gets the portal.layer.renderer.RendererDebuggerData used by this renderer
     */
    getRendererDebuggerData : function() {
        return this.renderDebuggerData;
    },
    
    setVisibility : function(visible) {
        this.primitiveManager.setVisibility(visible);
    }
});