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

    listenersHere : {
            removelayer : function(layerArray){
                this._removeLayer(layerArray);
            }
    },
    constructor : function(cfg) {
        var me = this;

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})',
            startCollapsed : true
        });
       
        me.listeners = Object.extend(me.listenersHere, cfg.listeners);
        
        var menuItems = [me._getVisibleBoundFilterAction(),me._getActivelayerFilterAction(),
                         me._getDataLayerFilterAction(),me._getImageLayerFilterAction()];

        Ext.apply(cfg, {
            cls : 'auscope-dark-grid',
            hideHeaders : true,
            features : [groupingFeature],
            viewConfig : {
                emptyText : '<p class="centeredlabel">No records match the current filter.</p>',
                preserveScrollOnRefresh: true    
            },          
            dockedItems : [{
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
            }],
            columns : [{
                //Loading icon column
                xtype : 'clickcolumn',
                dataIndex : 'active',
                renderer : me._deleteRenderer,
                hasTip : true,
                tipRenderer : function(value, layer, column, tip) {
                    if(layer.get('active')){
                        return 'Click to remove layer from map';
                    }else{
                        return 'Click to anywhere on this row to select drop down menu';
                    }
                },
                width: 32,
                listeners : {
                    columnclick : Ext.bind(me._deleteClickHandler, me)
                }
            },{
                //Loading icon column
                xtype : 'clickcolumn',
                dataIndex : 'loading',
                renderer : me._loadingRenderer,
                hasTip : true,
                tipRenderer : Ext.bind(me._loadingTipRenderer, me),
                width: 32,
                listeners : {
                    columnclick : Ext.bind(me._loadingClickHandler, me)
                }
            },{
                //Title column
                text : 'Title',
                dataIndex : 'name',
                flex: 1,
                renderer : me._titleRenderer
            },{
                //Service information column
                xtype : 'clickcolumn',
                dataIndex : 'serviceInformation',
                width: 32,
                renderer : me._serviceInformationRenderer,
                hasTip : true,
                tipRenderer : function(value, layer, column, tip) {
                    return 'Click for detailed information about the web services this layer utilises.';
                },
                listeners : {
                    columnclick : Ext.bind(me._serviceInformationClickHandler, me)
                }
            },{
                //Spatial bounds column
                xtype : 'clickcolumn',
                dataIndex : 'spatialBoundsRenderer',
                width: 32,
                renderer : me._spatialBoundsRenderer,
                hasTip : true,
                tipRenderer : function(value, layer, column, tip) {
                    return 'Click to see the bounds of this layer, double click to pan the map to those bounds.';
                },
                listeners : {
                    columnclick : Ext.bind(me._spatialBoundsClickHandler, me),
                    columndblclick : Ext.bind(me._spatialBoundsDoubleClickHandler, me)
                }
            }],
          plugins:[{                
              ptype : 'rowexpandercontainer',
              pluginId : 'maingrid_rowexpandercontainer',
              toggleColIndexes: [0, 2],
              generateContainer : function(record, parentElId, grid) {                  
                  //VT:if this is deserialized, we don't need to regenerate the layer
                  if(record.get('layer')) {                        
                      newLayer =  record.get('layer');                                    
                  }else if(record instanceof portal.csw.CSWRecord){                        
                      newLayer = cfg.layerFactory.generateLayerFromCSWRecord(record);                                                     
                  }else{
                      newLayer = cfg.layerFactory.generateLayerFromKnownLayer(record);                      
                  }           
                  record.set('layer',newLayer);
                  var filterForm = cfg.layerFactory.formFactory.getFilterForm(newLayer).form; //ALWAYS recreate filter form - see https://jira.csiro.au/browse/AUS-2588
                  filterForm.setLayer(newLayer);
                  var filterPanel = me._getInlineLayerPanel(filterForm, parentElId, this);
                  
                  //Update the layer panel to use
                  if (filterForm) {
                      var filterer = newLayer.get('filterer');
                      if (filterer) {
                          var existingParams = filterer.getParameters();
                          filterForm.getForm().setValues(existingParams);
                      }
                  }
                  grid.updateLayout({
                      defer:false,
                      isRoot:false
                  });                    
                  return filterPanel;
             }
         },{
          ptype: 'celltips'
         }]
                  
        });

        me.callParent(arguments);
        AppEvents.addListener(me);
    },
    
    _getInlineLayerPanel : function(filterForm, parentElId){                             
        var me = this;   
        var panel = Ext.create('portal.widgets.panel.FilterPanel', {    
            menuFactory : this.menuFactory,
            filterForm  : filterForm, 
            detachOnRemove : false,
            map         : this.map,
            renderTo    : parentElId,
            listeners : {
                addlayer : function(layer){
                    me.activelayerstore.suspendEvents(true);
                    me.activelayerstore.insert(0,layer); //this adds the layer to our store
                    me.activelayerstore.resumeEvents();
                    console.log("Added layer: ", layer);
                },
                removelayer : function(layer){
                    me.activelayerstore.remove(layer);
                }
            }
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
                    for(var i=0; i < csws.length; i++){
                        csws[i].set('customlayer',true);
                        customPanel.getStore().insert(0,csws[i]);
                    }
                    
                }
            }
        });
        cswSelectionWindow.show();
    },

    _deleteRenderer : function(value, metaData, record, row, col, store, gridView) {
        if (value) {
            return Ext.DomHelper.markup({
                tag : 'img',
                width : 16,
                height : 16,
                src: 'portal-core/img/trash.png'
            });
        } else {
            return Ext.DomHelper.markup({
                tag : 'img',
                width : 16,
                height : 16,
                src: 'portal-core/img/play_blue.png'
            });
        }
    },
    _deleteClickHandler :  function(value, record, rowIdx, tip) {
        var layer = record.get('layer');
        if(layer && record.get('active')){            
        	AppEvents.broadcast('removelayer', {layer:layer, rowIdx:rowIdx});
        } 
    },    
    _removeLayer : function(layerArray) {
        var layer = layerArray.layer;
        var rowIdx = layerArray.rowIdx;
        if (this.activelayerstore.find('id', layer.id) >= 0) {
            layer.removeDataFromMap();
            this.activelayerstore.remove(layer);          
            this.fireEvent('cellclick',this,undefined,undefined,layer,undefined,rowIdx);
            this.menuFactory.layerRemoveHandler(layer);
        } else {
            console.log('_removeLayer : no activeLayer with id:',layer.id," in this.activelayerstore: ", this.activelayerstore.getData());
        }
    },
    
    /**
     * Renderer for the loading column
     */
    _loadingRenderer : function(value, metaData, record, row, col, store, gridView) {
        if (value) {
            return Ext.DomHelper.markup({
                tag : 'img',
                width : 16,
                height : 16,
                src: 'portal-core/img/loading.gif'
            });
        } else {
            
            if(record.get('active')){
            
                var renderStatus = record.get('layer').get('renderer').renderStatus;
                var listOfStatus=renderStatus.getParameters();                
                var errorCount = this._statusListErrorCount(listOfStatus);
                var sizeOfList = Ext.Object.getSize(listOfStatus);
                if(errorCount > 0 && errorCount == sizeOfList){
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 16,
                        height : 16,
                        src: 'portal-core/img/exclamation.png'
                    });
                }else if(errorCount > 0 && errorCount < sizeOfList){
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 16,
                        height : 16,
                        src: 'portal-core/img/warning.png'
                    });
                }else{
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 16,
                        height : 16,
                        src: 'portal-core/img/tick.png'
                    });
                }
                
            }else{
                return Ext.DomHelper.markup({
                    tag : 'img',
                    width : 16,
                    height : 16,
                    src: 'portal-core/img/notloading.gif'
                });
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
    _loadingTipRenderer : function(value, record, column, tip) {
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
    
    _loadingClickHandler : function(value, record, rowIdx, tip) {
        
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
            items: {  // Let's put an empty grid in just to illustrate fit layout
                xtype: 'panel',
                autoScroll : true,                
                html : html
            }
        });
        
        win.show();
    }
});
