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

    visible : true,

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
        //{ name: 'loading', type: 'boolean', defaultValue: false }, //Whether this layer is currently loading data or not
        { name: 'filterForm', type: 'auto'}, //The portal.layer.filterer.BaseFilterForm that houses the GUI for editing this layer's filterer
        { name: 'renderOnAdd', type: 'boolean', defaultValue: false }, //If true then this layer should be rendered the moment it is added to the map:VT: Mark for deletion
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
    
    setLayerVisibility : function(visibility){
        this.get('renderer').primitiveManager.setVisibility(visibility);
        this.visible = visibility;
    },                

    onRenderStarted : function(renderer, onlineResources, filterer) {
        //this.set('loading', true);
        this.get('source').set('loading', true);
        this.get('source').set('active', true);
    },

    onRenderFinished : function(renderer) {
        //this.set('loading', false);
        this.get('source').set('loading', false);
    },


    /**
     * Whenever our filter changes, update the rendered page
     */
    onFilterChanged : function(filterer, keys) {
        var renderer = this.get('renderer');      
        this.removeDataFromMap();                  
        renderer.displayData(this.getAllOnlineResources(), this.get('filterer'), Ext.emptyFn);
        
        var group = 'group';
        if(this.get('sourceType')=='CSWRecord'){
            group='contactOrg';
        }
        
        this._expandGridGroup(this.get('source').get(group));
                       
    },
    
    _expandGridGroup : function(groupname){
        var activeTab = Ext.getCmp('auscope-tabs-panel').activeTab;
        var feature = activeTab.features[2]
        feature.expand(groupname,true);
        
    },
    
   removeDataFromMap:function(){
       var renderer = this.get('renderer');       
       renderer.removeData();
       renderer.map.closeInfoWindow(this.get('id')); 
       this.get('source').set('active', false);
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
    },
    
    getCSWRecordsByResourceURL : function(resourceURL){
        //Filter our results
        var results = [];
        var cswRecords=this.get('cswRecords');
        for(var i=0; i < cswRecords.length;i++){
            if(cswRecords[i].containsOnlineResourceUrl(resourceURL)){
                results.push(cswRecords[i]);
            }            
        }
        return results;
    },

    containsCSWService : function() {
        // If the layer is a known layer, then ask the KnownLayer
        // object if it contains a CSW service endpoint:
        if (this.get('sourceType') == portal.layer.Layer.KNOWN_LAYER) {
            return this.get('source').containsCSWService();
        }

        return false;
    }
});



