/**
 * A Layer is what a portal.csw.CSWRecord or portal.knownlayer.KnownLayer becomes
 * when the user wishes to add it to the map.
 *
 * i.e. What a collection of service URL's becomes so that the GUI can render and
 * make the resulting data interactive
 */
Ext.define('portal.layer.Layer', {
    extend: 'Ext.data.Model',

    statics : {
        KNOWN_LAYER : 'KnownLayer', //A value for 'sourceType'
        CSW_RECORD : 'CSWRecord' //A value for 'sourceType'
    },

    fields: [
        { name: 'id', type: 'string' }, //A unique ID of this layer - sourced from the original KnownLayer/CSWRecord
        { name: 'sourceType', type: 'string' }, //an 'enum' representing whether this Layer was constructed from a KnownLayer or CSWRecord
        { name: 'source', type: 'auto' }, //a reference to an instance of portal.knownlayer.KnownLayer or portal.csw.CSWRecord that was used to create this layer
        { name: 'name', type: 'string' }, //A human readable name/title of this layer
        { name: 'description', type: 'string' }, //A human readable description/abstract of this layer
        { name: 'renderer', type: 'auto' }, //A concrete implementation of a portal.layer.renderer.Renderer
        { name: 'filterer', type: 'auto' }, //A concrete implementation of a portal.layer.filterer.Filterer
        { name: 'downloader', type: 'auto' }, //A concrete implementation of a portal.layer.downloader.Downloader
        { name: 'querier', type: 'auto' }, //A concrete implementation of a portal.layer.querier.Querier
        { name: 'cswRecords', type: 'auto'}, //The source of all underlying data is an array of portal.csw.CSWRecord objects
        { name: 'loading', type: 'boolean', defaultValue: false }, //Whether this layer is currently loading data or not
        { name: 'filterForm', type: 'auto'}, //The portal.layer.filterer.BaseFilterForm that houses the GUI for editing this layer's filterer
        { name: 'renderOnAdd', type: 'boolean', defaultValue: false }, //If true then this layer should be rendered the moment it is added to the map
        { name: 'deserialized', type: 'boolean', defaultValue: false } //If true then this layer has been deserialized from a permanent link
    ],

    /**
     * Utility function for concatenating all online resources stored in all
     * CSWRecords and returning the result as an Array.
     *
     * returns an Array of portal.csw.OnlineResource objects
     */
    getAllOnlineResources : function() {
        var resources = [];
        var cswRecords = this.get('cswRecords');
        for (var i = 0; i < cswRecords.length; i++) {
            resources = resources.concat(cswRecords[i].get('onlineResources'));
        }
        return resources;
    },

    onRenderStarted : function(renderer, onlineResources, filterer) {
        this.set('loading', true);
    },

    onRenderFinished : function(renderer) {
        this.set('loading', false);
    },

    /**
     * Whenever our layer is told to update visibility - let's take the brute force approach of deleting/re-adding the layer
     */
    onVisibilityChanged : function(renderer, newVisibility) {
        if (newVisibility) {
            renderer.displayData(this.getAllOnlineResources(), this.get('filterer'), Ext.emptyFn);
        } else {
            renderer.abortDisplay();
            renderer.removeData();
        }
    },

    /**
     * Whenever our filter changes, update the rendered page
     */
    onFilterChanged : function(filterer, keys) {
        var renderer = this.get('renderer');
        if (renderer.getVisible()) {
            renderer.displayData(this.getAllOnlineResources(), this.get('filterer'), Ext.emptyFn);
        }
    },

    getCSWRecordsByKeywords : function(keyword){
        //Filter our results
        var results = [];
        var cswRecords=this.get('cswRecords');
        for(var i=0; i < cswRecords.length;i++){
            if(cswRecords[i].containsKeywords(keyword)){
                results.push(cswRecords[i]);
            }
        }
        return results;
    }
});