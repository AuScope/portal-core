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
    extend: 'Ext.panel.Panel',

   
    _addOrUpdateLayerButton : null,
    
    filterForm : null,
    
    constraintShown : false,
    nagiosAlertShown: false,
    
    optionsButtonIsHidden : false,
    
    layerStore: null,
    
    menuFactory: null,

    /**
     * Accepts all parameters for a normal Ext.Panel instance with the following additions
     * {
     *  layerPanel : [Required] an instance of a portal.widgets.panel.LayerPanel - selection events will be listend for
     *  wantAddLayerButton : boolean [Optional - defaults to True]
     *  wantOptionsButton : boolean [Optional - defaults to True]
     * } 
     */
    constructor : function(config) {
 
        this._map = config.map;     
        
        this.layerStore = config.layerStore;
        
        // setup the default options or use those passed in by the portal
        var menuItems = config.menuItems ? config.menuItems :
            [this._getResetFormAction(),this._getDeleteAction(),this._setVisibilityAction()];
        
        if(Ext.isIE){
            this.filterForm = config.filterForm.cloneConfig();        
            this.filterForm.getForm().setValues(config.filterForm.getForm().getValues());
            config.filterForm  = this.filterForm;                
            config.filterForm.layer.set('filterForm',this.filterForm);
        }else{
            this.filterForm = config.filterForm
        }
                    
        if (typeof config.wantAddLayerButton === 'undefined' || config.wantAddLayerButton ) {
            this._addOrUpdateLayerButton = Ext.create('Ext.button.Button', {
                xtype : 'button',
                text      : 'Add layer to Map',
                iconCls    :   'add',
                handler : Ext.bind(this._onAddLayer, this)
            });
        } else if (config.wantUpdateLayerButton ) {
            this._addOrUpdateLayerButton = Ext.create('Ext.button.Button', {
                xtype : 'button',
                text      : 'Update Layer on Map',
                iconCls    :   'edit',
                handler : Ext.bind(this._onAddLayer, this)
            });
        }        
        
        if (typeof config.wantOptionsButton === 'undefined' || config.wantOptionsButton ) {
            this.optionsButtonIsHidden = false;
        } else {
            this.optionsButtonIsHidden = true;
        }                            
        
        if(config.menuFactory){
            this.menuFactory = config.menuFactory;
            var mf= config.menuFactory;
            if (mf.addResetFormActionForWMS) {
                menuItems.push(this._getResetFormAction());
            }
            mf.appendAdditionalActions(menuItems,this.filterForm.layer,this.filterForm.layer.get('source').get('group'),this._map);            
        }
        
        //VT:All special menu item should be determined from the menu factory. This is the only exception as all layers 
        //VT:Should have a legend action except for Insar data.
        // but even then if the portal is specifiying the menu items then don't add a legend by default
        if (config.menuItems && this.filterForm.layer.get('renderer').getLegend()){            
            menuItems.push(this._getLegendAction(this.filterForm.layer));
        }   
        
        if(!config.menuFactory){
            //VT:Default behavior if there are no menuFactory defined.
            if(this.filterForm.layer.get('cswRecords').length > 0 &&
                    this.filterForm.layer.get('cswRecords')[0].get('noCache')==false){
                     menuItems.push(this._getDownloadAction());
            }
        }
        

        var optionsButton = Ext.create('Ext.button.Button', {
            xtype : 'button',
            text : 'Options',
            iconCls : 'setting',
            arrowAlign : 'right'
        });
        
        if (menuItems.length === 0) {
            this.optionsButtonIsHidden = true;
        } else if (menuItems.length === 1) {
            var optionsAction = menuItems[0];
            optionsButton = Ext.create('Ext.button.Button', optionsAction);
        } else {
            optionsButton.setMenu(menuItems)
        }

        optionsButton.setHidden(this.optionsButtonIsHidden)

        Ext.apply(config, { 
            items : [
                this.filterForm
            ],
            buttons : [
                this._addOrUpdateLayerButton,
            {
                xtype:'tbfill'
            },
            optionsButton
            ]
        
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
    
    _getLegendAction : function(layer){                       
        var legend = layer.get('renderer').getLegend();
        var text = 'Get Legend';
        var me = this;
       
        var getLegendAction = new Ext.Action({
            text : text,
            icon : legend.iconUrl,
            //icon : null,
            iconCls : 'portal-ux-menu-icon-size',
            itemId : 'LegendAction',
            
            handler : function(){
                var legendCallback = function(legend, resources, filterer, success, form, layer){
                    if (success && form) {
                        var winId = 'legend-' + layer.get('id');    
                        if (Ext.getCmp(winId)) {
                            Ext.getCmp(winId).close();
                        }
                        var win = Ext.create('Ext.window.Window', {
                            id          : winId,
                            title       : 'Legend: '+ layer.get('name'),
                            layout      : 'fit',
                            width       : 200,
                            height      : 300,
                            modal : true,
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
                //VT: if a layer has style, the style should take priority as the default GetLegend source else use default
                if(styleUrl && styleUrl.length > 0){
                    //LJ: AUS-2619 Additional params for legend.
                    var sldConfig = me.menuFactory.appendAdditionalLegendParams(layer, filterer, styleUrl);

                    if (sldConfig.isSld_body === false) {
                        legend.getLegendComponent(onlineResources, filterer, sldConfig.sldUrl, false, Ext.bind(legendCallback, this, [layer], true));  
                    } else {
	                    Ext.Ajax.request({
	                        url: sldConfig.sldUrl,
	                        timeout : 180000,
	                        scope : this,
	                        success:function(response,opts){
	                            legend.getLegendComponent(onlineResources, filterer,response.responseText, true, Ext.bind(legendCallback, this, [layer], true));
	                        },
	                        failure: function(response, opts) {
	                            legend.getLegendComponent(onlineResources, filterer,"", true, Ext.bind(legendCallback, this, [layer], true));
	                        }                        
	                    });
                    }
                
                }else{
                    legend.getLegendComponent(onlineResources, filterer,"", true, Ext.bind(legendCallback, this, [layer], true));
                }
                
            }
        });
        
        return getLegendAction;
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
                ActiveLayerManager.removeLayer(layer);
                me.menuFactory.layerRemoveHandler(layer);
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
        try {
            filterer.setSpatialParam(this._map.getVisibleMapBounds(), true);
            this.filterForm.writeToFilterer(filterer);
        } catch (e) {
            console.log(e);
        }
        
        ActiveLayerManager.addLayer(layer);
        
        this._showConstraintWindow(layer);
        this._showNagiosAlerts(layer);
        this.menuFactory.layerAddHandler(layer);

        //VT: Tracking
        
        portal.util.GoogleAnalytic.trackevent('Add:' + layer.get('sourceType'), 'Layer:' + layer.get('name'),'Filter:' + Ext.encode(filterer.getParameters())); 
        
    },   
    
    _showNagiosAlerts : function(layer){
        if(this.nagiosAlertShown){
            return;
        }
        
        if (layer.get('sourceType') === portal.layer.Layer.KNOWN_LAYER &&
            layer.get('source').containsNagiosFailures()) {
            var failingHosts = layer.get('source').get('nagiosFailingHosts');
            
            var message = 'Please be aware that some of the services underpinning this layer have recently been reported as being unstable. The unstable hosts will be not be queried. The hosts reported to be experiencing problems are:<br><ul>';
            Ext.each(failingHosts, function(host) {
                message += '<li><b>' + host + '</b></li>';
            });
            message += '</ul>';
            
            Ext.MessageBox.show({
                title: 'Service Problems',
                message: message,
                icon: Ext.Msg.WARNING
            });
        }
        
        this.nagiosAlertShown = true;
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
