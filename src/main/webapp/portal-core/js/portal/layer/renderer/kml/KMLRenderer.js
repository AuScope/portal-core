/**
 * An implementation of a portal.layer.Renderer for rendering generic Layers
 * that belong to a set of portal.csw.CSWRecord objects.
 */
Ext.define('portal.layer.renderer.csw.KMLRenderer', {
    extend: 'portal.layer.renderer.Renderer',

    vectorLayer : null,
    renderedId : null,

    constructor: function(config) {
       
        this.callParent(arguments);
    }, 

    /**   
     *
     * A function for rendering KML layers either from KML file input or URL.
     * 
     * returns - void
     *
     * resources - an array of data sources which should be used to render data
     * filterer - A custom filter that can be applied to the specified data sources
     * callback - Will be called when the rendering process is completed and passed an instance of this renderer and the parameters used to call this function
     */
    displayData : function(resources, filterer, callback) {
        
        //VT: I have taken a short path to render this layer on the map. 
        //If there are any actual demand to improve KML, 
        //create a KML Primitive and add makeKML in openlayersmap.js
        //this.PrimitiveManager.add(kml) and in primitiveManager handle if primitive type is kml.
        this.removeData();
        this.fireEvent('renderstarted', this, resources, filterer);
        this.renderStatus.initialiseResponses("KML Layer", 'Rendering...');
        this.renderedId=this.parentLayer.get('id');
        var me = this;
        var task = new Ext.util.DelayedTask(function() {
            me.vectorLayer = me.map.addKMLFromString(me.renderedId,me.parentLayer.get('name'), me.parentLayer.get('source').get('extensions'));    
            me.renderStatus.updateResponse("KML Layer", "Complete");   
            me.fireEvent('renderfinished', me);
        });

        task.delay(500);

    },


   

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
    getLegend : function(resources, filterer) {
        return null
    },

    /**
     * An abstract function that is called when this layer needs to be permanently removed from the map.
     * In response to this function all rendered information should be removed
     *
     * function()
     *
     * returns - void
     */
    removeData : function() {
        if(this.vectorLayer){
            this.map.removeKMLLayer(this.vectorLayer);
            this.vectorLayer = null;
        }
    },
    
    setVisibility : function(visible) {
        return this.vectorLayer.setVisibility(visible);
    },

    /**
     * No point aborting a bbox rendering
     */
    abortDisplay : Ext.emptyFn
});