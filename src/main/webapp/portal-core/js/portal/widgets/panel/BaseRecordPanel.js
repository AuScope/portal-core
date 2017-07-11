/**
 * An abstract base class to be extended.
 *
 * Represents a grid panel for containing layers
 * that haven't yet been added to the map. Each row
 * will be grouped under a heading, contain links
 * to underlying data sources and have a spatial location
 * that can be viewed by the end user.
 *
 * This class is expected to be extended for usage within
 * the 'Registered Layers', 'Known Layers' and 'Custom Layers'
 * panels in the portal. Support for KnownLayers/CSWRecords and
 * other row types will be injected by implementing the abstract
 * functions of this class
 *
 */
Ext.define('portal.widgets.panel.BaseRecordPanel', {
    extend : 'portal.widgets.panel.CommonBaseRecordPanel',
    alias: 'widget.baserecordpanel',

    constructor : function(cfg) {
        var me = this;
       
        me.listeners = Object.extend(me.listenersHere, cfg.listeners);
        
        var menuItems = [me._getVisibleBoundFilterAction(),me._getActivelayerFilterAction(),
                         me._getDataLayerFilterAction(),me._getImageLayerFilterAction()];

        var dockedItems = null;
        if (cfg.hideSearch !== true) {
            dockedItems = [{
                xtype : 'toolbar',
                dock : 'top',
                portalName : 'search-bar', //used for distinguishing this toolbar
                items : [{
                    xtype : 'label',
                    text : 'Search: '
                },{
                    xtype : 'clientsearchfield',
                    id : 'hh-searchfield-' + cfg.title.replace(' ',''),
                    width : 200,
                    fieldName: 'name',
                    store : cfg.store
                },{
                    xtype : 'button',
                    id : 'hh-filterDisplayedLayer-' + cfg.title.replace(' ',''),
                    text : 'View by',
                    iconCls : 'filter',
                    tooltip: 'Provide more options for filtering layer\'s view',
                    arrowAlign : 'right',
                    menu : menuItems
                   
                }]
            }];
        }
        
        Ext.applyIf(cfg, {
            cls : 'auscope-dark-grid',
            emptyText : '<p class="centeredlabel">No records match the current filter.</p>',
            dockedItems : dockedItems,
            titleField: 'name',
            titleIndex: 2,
            tools: [{
                field: 'active',
                clickHandler: Ext.bind(me._deleteClickHandler, me),
                stopEvent: false,
                tipRenderer: function(value, layer, tip) {
                    if(value) {
                        return 'Click to remove layer from map';
                    } else {
                        return 'Click to anywhere on this row to select drop down menu';
                    }
                },
                iconRenderer: me._deleteRenderer
            },{
                field: ['loading', 'active'],
                stopEvent: true,
                clickHandler: Ext.bind(me._loadingClickHandler, me),
                tipRenderer: Ext.bind(me._loadingTipRenderer, me),
                iconRenderer: Ext.bind(me._loadingRenderer, this)
            },{
                field: 'serviceInformation',
                stopEvent: true,
                clickHandler: Ext.bind(me._serviceInformationClickHandler, me),
                tipRenderer: function(value, record, tip) {
                    if ((record instanceof portal.knownlayer.KnownLayer) && record.containsNagiosFailures()) {
                        return 'One or more of the services used by this layer are reported to be experiencing issues at the moment. Some aspects of this layer may not load/work.';
                    }
                    return 'Click for detailed information about the web services this layer utilises.';
                },
                iconRenderer: Ext.bind(me._serviceInformationRenderer, me)
            },{
                field: 'spatialBoundsRenderer',
                stopEvent: true,
                clickHandler: Ext.bind(me._spatialBoundsClickHandler, me),
                doubleClickHandler: Ext.bind(me._spatialBoundsDoubleClickHandler, me),
                tipRenderer: function(layer, tip) {
                    return 'Click to see the bounds of this layer, double click to pan the map to those bounds';
                },
                iconRenderer: Ext.bind(me._spatialBoundsRenderer, me)
            }],
            lazyLoadChildPanel: true,
            childPanelGenerator: function(record) {                  
                //For every filter panel we generate, also generate a portal.layer.Layer and 
                //attach it to the CSWRecord/KnownLayer
                var newLayer = null;
                if (record instanceof portal.csw.CSWRecord) {                        
                    newLayer = cfg.layerFactory.generateLayerFromCSWRecord(record);                                                     
                } else {
                    newLayer = cfg.layerFactory.generateLayerFromKnownLayer(record);                      
                }
                record.set('layer', newLayer);
                return me._getInlineLayerPanel(newLayer.get('filterForm'));
           }
        });

        me.callParent(arguments);
    },
    
    onDestroy : function() {
        me.callParent();
    },

    _getInlineLayerPanel : function(filterForm){                             
        var me = this;   
        var panel = Ext.create('portal.widgets.panel.FilterPanel', {    
            menuFactory : this.menuFactory,
            filterForm  : filterForm, 
            detachOnRemove : false,
            map         : this.map,
            menuItems : []
        });   
        
        return panel
    },
    
    _getVisibleBoundFilterAction : function(){   
        
        var me = this;
        return new Ext.Action({
            text : 'Visible Bound',
            iconCls : 'visible_eye',
            tooltip: 'Filter the layers based on its bounding box and the map\'s visible bound',
            handler : Ext.bind(me._handleVisibleFilterClick, this)
        })
        
    },
    
    _getActivelayerFilterAction : function(){
        var me = this;
        return new Ext.Action({
            text : 'Active Layer',
            iconCls : 'tick',
            tooltip: 'Display only active layer',
            handler : function(){
//              // TODO: Do I need this but using the new id for rowexpandercontainer?
//              // var rowExpander = me.getPlugin('maingrid_rowexpandercontainer');
//              //                rowExpander.closeAllContainers();          
                
                //function to check if layer is active on map
                var filterFn = function(rec) {
                    return rec.get('active');
                };

                var searchField = this.findParentByType('toolbar').getComponent(1);
                searchField.clearCustomFilter();
                searchField.runCustomFilter('<active layers>', Ext.bind(filterFn, this));
            }
        })
    },
    
    _getDataLayerFilterAction : function(){
        var me = this;
        return new Ext.Action({
            text : 'Data Layer',
            iconCls : 'data',
            tooltip: 'Display layer with data service',
            handler : function(){
//              // TODO: Do I need this but using the new id for rowexpandercontainer?
//              // var rowExpander = me.getPlugin('maingrid_rowexpandercontainer');
//              //                rowExpander.closeAllContainers();          
                
                //function to if layer contains data service
                var filterFn = function(rec) {
                    var onlineResources = me.getOnlineResourcesForRecord(rec)
                    var serviceType = me._getServiceType(onlineResources); 
                    
                    //VT:This part of the code is to keep it inline with the code in _serviceInformationRenderer
                    //VT: for rendering the icon
                    if (serviceType.containsDataService) {
                        return true; //a single data service will label the entire layer as a data layer
                    }else{
                        return false;
                    } 

                };

                var searchField = this.findParentByType('toolbar').getComponent(1);
                searchField.clearCustomFilter();
                searchField.runCustomFilter('<Data Layers>', Ext.bind(filterFn, this));
            }
        })
        
    },
    
    _getImageLayerFilterAction : function(){
        var me = this;
        return new Ext.Action({
            text : 'Portrayal Layer',
            iconCls : 'portrayal',
            tooltip: 'Display layers with image service',
            handler : function(){
//              // TODO: Do I need this but using the new id for rowexpandercontainer?
//              // var rowExpander = me.getPlugin('maingrid_rowexpandercontainer');
//              //                rowExpander.closeAllContainers();          
                
                //function to if layer contains image service
                var filterFn = function(rec) {           
                    var onlineResources = me.getOnlineResourcesForRecord(rec);
                    var serviceType = me._getServiceType(onlineResources);                                                                                             
                    
                    //VT:This part of the code is to keep it inline with the code in _serviceInformationRenderer
                    //VT: for rendering the picture icon
                    if (serviceType.containsDataService) {
                        return false; //a single data service will label the entire layer as a data layer
                    } else if (serviceType.containsImageService) {
                        return true;
                    } else {
                        return false;
                    }
                          
                };

                var searchField = this.findParentByType('toolbar').getComponent(1);
                searchField.clearCustomFilter();
                searchField.runCustomFilter('<Portrayal layers>', Ext.bind(filterFn, this));
            }
        })
    },
    
    /**
     * When the visible fn is clicked, ensure only the visible records pass the filter
     */
    _handleVisibleFilterClick : function(button) {                           
        var currentBounds = this.map.getVisibleMapBounds();

        //Function for testing intersection of a records's spatial bounds
        //against the current visible bounds
        var filterFn = function(rec) {
            var spatialBounds;
            spatialBounds = this.getSpatialBoundsForRecord(rec);
            for (var i = 0; i < spatialBounds.length; i++) {
                if (spatialBounds[i].intersects(currentBounds)) {
                    return true;
                }
            }

            return false;
        };

        var searchField = button.findParentByType('toolbar').getComponent(1);
        searchField.clearCustomFilter();
        searchField.runCustomFilter('<visible layers>', Ext.bind(filterFn, this));      
    },       
 
    // Used in sub-classes
    handleFilterSelectComplete : function(filteredResultPanels){
        var me = this;
        var cswSelectionWindow = new CSWSelectionWindow({
            title : 'CSW Record Selection',
            resultpanels : filteredResultPanels,
            listeners : {
                selectioncomplete : function(csws){  
                    var tabpanel =  Ext.getCmp('auscope-tabs-panel');
                    var customPanel = me.ownerCt.getComponent('org-auscope-custom-record-panel');
                    tabpanel.setActiveTab(customPanel);
                    if(!(csws instanceof Array)){
                        csws = [csws];
                    }
                    for(var i=csws.length-1; i>=0; i--){
                        csws[i].set('customlayer',true);
                        customPanel.getStore().insert(0,csws[i]);
                        customPanel.ensureVisible(0);
                    }
                    
                }
            }
        });
        cswSelectionWindow.show();
    },

    /**
     * When called, will update the visibility of any search bars
     */
    _updateSearchBar : function(visible) {
        var dockedItems = this.getDockedItems();
        var searchBar = null;
        for (var i = 0; i < dockedItems.length; i++) {
            if (dockedItems[i].initialConfig.portalName === 'search-bar') {
                searchBar = dockedItems[i];
            }
        }
        if (!searchBar) {
            return;
        }

        if (visible) {
            searchBar.show();
        } else {
            searchBar.hide();
        }
    },

    _deleteRenderer : function(value, record) {
        if (value) {
            return 'portal-core/img/trash.png';
        } else {
            return 'portal-core/img/play_blue.png';
        }
    },
    
    _deleteClickHandler :  function(value, record) {
        var layer = record.get('layer');
        if(layer && record.get('active')){
            ActiveLayerManager.removeLayer(layer);
            this.menuFactory.layerRemoveHandler(layer);
            this.fireEvent('cellclick',this,undefined,undefined,layer,undefined,undefined);
        }
    },

    /**
     * Renderer for the loading column
     */
    _loadingRenderer : function(value, record) {
        if (value) {
            return 'portal-core/img/loading.gif';
        } else {
            
            if(record.get('active')){
            
                var renderStatus = record.get('layer').get('renderer').renderStatus;
                var listOfStatus=renderStatus.getParameters();                
                var errorCount = this._statusListErrorCount(listOfStatus);
                var sizeOfList = Ext.Object.getSize(listOfStatus);
                if(errorCount > 0 && errorCount == sizeOfList){
                    return 'portal-core/img/warning.png';
                }else if(errorCount > 0 && errorCount < sizeOfList){
                    return 'portal-core/img/exclamation.png';
                }else{
                    return 'portal-core/img/tick.png';
                }
                
            }else{
                return 'portal-core/img/notloading.gif';
            }
        }
    },
    
    _statusListErrorCount : function(listOfStatus){
        var match =["reached","error","did not complete","AJAX","Unable"];
        
        var erroCount = 0;  
        
        for(key in listOfStatus){
            for(var i=0; i< match.length; i++){
                if(listOfStatus[key].indexOf(match[i]) > -1){
                    erroCount++;
                    break;
                }
            }
        }
        return erroCount;
    },
    
  
    /**
     * A renderer for generating the contents of the tooltip that shows when the
     * layer is loading
     */
    _loadingTipRenderer : function(value, record, tip) {
        var layer = record.get('layer');
        if(!layer){//VT:The layer has yet to be created.
            return 'No status has been recorded';
        }
        var renderer = layer.get('renderer');
        var update = function(renderStatus, keys) {
            tip.update(renderStatus.renderHtml());
        };

        //Update our tooltip as the underlying status changes
        renderer.renderStatus.on('change', update, this);
        tip.on('hide', function() {
            renderer.renderStatus.un('change', update); //ensure we remove the handler when the tip closes
        });

        return renderer.renderStatus.renderHtml();
    },
    
    _loadingClickHandler : function(value, record) {
        
        var layer = record.get('layer');
        
        var html = '<p>No Service recorded, Click on Add layer to map</p>';    
        
        if(layer){
            var renderer = layer.get('renderer');
            html =  renderer.renderStatus.renderHtml();
        }        
        var win = Ext.create('Ext.window.Window', {
            title: 'Service Loading Status',
            height: 200,
            width: 500,
            layout: 'fit',
            modal : true,
            items: {  // Let's put an empty grid in just to illustrate fit layout
                xtype: 'panel',
                autoScroll : true,                
                html : html
            }
        });
        
        win.show();
    }
});
