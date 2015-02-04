/**
 * A panel for displaying filter forms for a given portal.layer.Layer
 *
 * A filter panel is coupled tightly with a portal.widgets.panel.LayerPanel
 * as it is in charge of displayed appropriate filter forms matching the current
 * selection
 *
 * VT: THIS CLASS IS TO BE DELETED WITH THE NEW INLINE UI
 */
Ext.define('portal.widgets.panel.FilterPanel', {
    extend: 'Ext.Panel',

   
    _addLayerButton : null,

    filterForm : null,
    
    constraintShown : false,
    
    /**
     * Accepts all parameters for a normal Ext.Panel instance with the following additions
     * {
     *  layerPanel : [Required] an instance of a portal.widgets.panel.LayerPanel - selection events will be listend for
     * }
     * 
     * Adds the following event:
     * addlayer - fire when we request to add a layer
     * removelayer - fire when a request to remove layer is made
     */
    constructor : function(config) {
 
        this._map = config.map;
        this.filterForm = config.filterForm;             
        
        this._addLayerButton = Ext.create('Ext.button.Button', {
            xtype : 'button',
            text      : 'Add layer to Map',
            iconCls    :   'add',
            handler : Ext.bind(this._onAddLayer, this)
        });
         
        
        var menuItems = [this._getResetFormAction(),this._getDeleteAction(),this._setVisibilityAction()];
        var legendAction=this._getLegendAction();
        if(legendAction){
            menuItems.push(legendAction);
        }
        
        if(this.filterForm.layer.get('cswRecords').length > 0 &&
           this.filterForm.layer.get('cswRecords')[0].get('noCache')==false){
            menuItems.push(this._getDownloadAction());
        }
        
        var group = this.filterForm.layer.get('source').get('group');
        if(group && group.indexOf('Analytic') >= 0){
            menuItems.push(this._getAnalyticLink());
        }
            

        Ext.apply(config, { 
            items : [
                this.filterForm
            ],
            buttons : [
                this._addLayerButton,
            {
                xtype:'tbfill'
            },{
                xtype : 'button',
                text      : 'Options',
                iconCls    :   'setting',
                arrowAlign: 'right',
                menu      : menuItems           
            }]
        
        });

        this.callParent(arguments);

 


    },
    
    _getResetFormAction : function(){
        var baseform = this.filterForm;
        
        return new Ext.Action({
            text : 'Reset Form',
            iconCls : 'refresh',
            handler : function(){
                baseform.getForm().reset();
            }
        })
        
    },
    
    _getLegendAction : function(){                 
        var me = this;
        var layer = me.filterForm.layer;
        var legend = layer.get('renderer').getLegend();
        var text = 'Get Legend';
        if(!legend){
           return null;
        }
        
        var getLegendAction = new Ext.Action({
            text : text,
            icon : legend.iconUrl,
            handler : function(){
                var legendCallback = function(legend, resources, filterer, success, form, layer){
                    if (success && form) {
                        var win = Ext.create('Ext.window.Window', {
                            title       : 'Legend: '+ layer.get('name'),
                            layout      : 'fit',
                            width       : 200,
                            height      : 300,
                            items: form
                        });
                        return win.show();
                    }
                };

                var onlineResources = layer.getAllOnlineResources();
                var filterer = layer.get('filterer');
                var renderer = layer.get('renderer');
                var legend = renderer.getLegend(onlineResources, filterer);

                //VT: this style is just for the legend therefore no filter is required.
                var styleUrl = layer.get('renderer').parentLayer.get('source').get('proxyStyleUrl');

                Ext.Ajax.request({
                    url: styleUrl,
                    timeout : 180000,
                    scope : this,
                    success:function(response,opts){
                        legend.getLegendComponent(onlineResources, filterer,response.responseText, Ext.bind(legendCallback, this, [layer], true));
                    },
                    failure: function(response, opts) {
                        legend.getLegendComponent(onlineResources, filterer,"", Ext.bind(legendCallback, this, [layer], true));
                    }
                });
            }
        });
        
        return getLegendAction;
    },
    
    _getAnalyticLink : function(){
        var me=this;
        var layer = this.filterForm.layer; 
        
        return new Ext.Action({
            text : 'Vgl Analytics',
            iconCls : 'link',
            handler : function(){                
                
                var mss = Ext.create('portal.util.permalink.MapStateSerializer');
                var layerStore = Ext.create('portal.layer.LayerStore', {});
                layerStore.insert(0,layer);

                mss.addMapState(me._map);
                mss.addLayers(layerStore);
                mss.serialize(function(state, version) {
                    var urlParams = Ext.Object.fromQueryString(location.search.substring(1));
                    urlParams.s = state;
                    if (version) {
                        urlParams.v = version;
                    }
                    //VT: Hardcoding this for now, don't foresee any changes anytime soon.
                    var linkedUrl = "http://vgl.auscope.org/VGL-Portal/gmap.html";

                    var params = Ext.Object.toQueryString(urlParams);

                    //*HACK:* sssssshhhh dont tell anyone we don't care about escaping....
                    linkedUrl = Ext.urlAppend(linkedUrl, decodeURIComponent(params));
                    window.open(linkedUrl);
                });
                
            }
        });
        
    },

    
    _getDownloadAction : function(){
        var me = this;
        var downloadLayerAction = new Ext.Action({
            text : 'Download Layer',
            iconCls : 'download',
            handler : function(){
                var layer = me.filterForm.layer; 
                var downloader = layer.get('downloader');
                var renderer = layer.get('renderer');
                if (downloader) {// && renderer.getHasData() -> VT: It is too confusing when the download will be active. We will treat it as always active to 
                                 // make it easier for the user.
                    //We need a copy of the current filter object (in case the user
                    //has filled out filter options but NOT hit apply filter) and
                    //the original filter objects
                    var renderedFilterer = layer.get('filterer').clone();
                    var currentFilterer = Ext.create('portal.layer.filterer.Filterer', {});
                    var currentFilterForm = layer.get('filterForm');

                    currentFilterer.setSpatialParam(me._map.getVisibleMapBounds(), true);
                    currentFilterForm.writeToFilterer(currentFilterer);

                    //Finally pass off the download handling to the appropriate downloader (if it exists)
                    var onlineResources = layer.getAllOnlineResources();
                    downloader.downloadData(layer, onlineResources, renderedFilterer, currentFilterer);

                }
            }
        });
        
        return downloadLayerAction
    },
    
    
    _getDeleteAction : function(){
        var me = this;
        var downloadLayerAction = new Ext.Action({
            text : 'Remove Layer',
            iconCls : 'trash',
            handler : function(){
                var layer = me.filterForm.layer; 
                layer.removeDataFromMap();
                me.updateButton(false);
                me.fireEvent('removelayer', layer);
            }
        });
        
        return downloadLayerAction;
    },
    
    _setVisibilityAction : function(){
        var me = this;
        var visibleLayerAction = new Ext.Action({
            text : 'Toggle Layer Visibility OFF',
            iconCls : 'visible_eye',
            handler : function(){
                var layer = me.filterForm.layer;                 
                layer.setLayerVisibility(!layer.visible);
                if(layer.visible){
                    this.setText('Toggle Layer Visibility OFF');
                }else{
                    this.setText('Toggle Layer Visibility ON');
                }
                
            }
        });
        
        return visibleLayerAction;
    },


    /**
     * Internal handler for when the user clicks 'Apply Filter'.
     *
     * Simply updates the appropriate layer filterer. It's the responsibility
     * of renderers/layers to listen for filterer updates.
     */
    _onAddLayer : function() {      
        var layer = this.filterForm.layer; 
        var filterer = layer.get('filterer');      
        
        //Before applying filter, update the spatial bounds (silently)
        filterer.setSpatialParam(this._map.getVisibleMapBounds(), true);

        this.filterForm.writeToFilterer(filterer);
        this.fireEvent('addlayer', layer);
        this.updateButton(true);
        this._showConstraintWindow(layer);
    },
    
    /**
     * Change the state of the button
     * State 1: original text "Add layer to Map"
     * State 2: after adding layer "Update layer on Map")
     */
    updateButton : function(islayerOnMap){
        if(islayerOnMap){
            this._addLayerButton.setText('Update layer on Map');
        }else{
            this._addLayerButton.setText('Add layer to Map');
        }
    },
    
    _showConstraintWindow : function(layer){
        if(this.constraintShown){
            return;
        }
        var cswRecords = layer.get('cswRecords');
        for (var i = 0; i < cswRecords.length; i++) {
            if (cswRecords[i].hasConstraints()) {
                var popup = Ext.create('portal.widgets.window.CSWRecordConstraintsWindow', {
                    width : 625,
                    cswRecords : cswRecords
                });

                popup.show();

                  //HTML images may take a moment to load which stuffs up our layout
                  //This is a horrible, horrible workaround.
                var task = new Ext.util.DelayedTask(function(){
                    popup.doLayout();
                });
                task.delay(1000);

                break;
            }
        }
        this.constraintShown = true;
    },

    /**
     * Internal handler for when the user clicks 'Reset Filter'.
     *
     * Using the reset method from Ext.form.Basic. All fields in
     * the form will be reset. However, any record bound by loadRecord
     * will be retained.
     */
    _onResetFilter : function() {
        var baseFilterForm = this.getLayout().getActiveItem();
        baseFilterForm.getForm().reset();
    },

   

    clearFilter : function(){
        var layout = this.getLayout();

        //Remove custom CSS styles for filter button
        //this._filterButton.getEl().removeCls("applyFilterCls");

        //Disable the filter and reset buttons (set to default values)
        //this._filterButton.setDisabled(true);
        //this._resetButton.setDisabled(true);

        //Close active item to prevent memory leak
        var actvItem = layout.getActiveItem();
        if (actvItem) {
            actvItem.close();
        }
        layout.setActiveItem(this._emptyCard);
    }
});