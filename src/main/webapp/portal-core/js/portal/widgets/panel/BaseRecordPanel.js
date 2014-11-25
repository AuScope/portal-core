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
    extend : 'Ext.grid.Panel',
    alias: 'widget.baserecordpanel',
    browseCatalogueDNSMessage : false, //VT: Flags the do not show message when browse catalogue is clicked.
    map : null,
    activelayerstore : null,

    constructor : function(cfg) {
        var me = this;
        this.map = cfg.map;
        this.activelayerstore = cfg.activelayerstore;
        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})',
            startCollapsed : true
        });
       
        this.listeners = cfg.listeners;

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
                    id : 'hh-searchfield-' + cfg.title,
                    width : 200,
                    fieldName: 'name',
                    store : cfg.store
                },{
                    xtype : 'button',
                    id : 'hh-filterVisible-' + cfg.title,
                    text : 'Filter Visible',
                    iconCls : 'visible_eye',
                    tooltip: 'Filter the layers based on its bounding box and the map\'s visible bound',
                    handler : Ext.bind(this._handleVisibleFilterClick, this)
                }]
            }],
            columns : [{
                //Loading icon column
                xtype : 'clickcolumn',
                dataIndex : 'active',
                renderer : this._deleteRenderer,
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
                    columnclick : Ext.bind(this._deleteClickHandler, this)
                }
            },{
                //Loading icon column
                xtype : 'clickcolumn',
                dataIndex : 'loading',
                renderer : this._loadingRenderer,
                hasTip : true,
                tipRenderer : Ext.bind(this._loadingTipRenderer, this),
                width: 32,
                listeners : {
                    columnclick : Ext.bind(this._loadingClickHandler, this)
                }
            },{
                //Title column
                text : 'Title',
                dataIndex : 'name',
                flex: 1,
                renderer : this._titleRenderer
            },{
                //Service information column
                xtype : 'clickcolumn',
                dataIndex : 'serviceInformation',
                width: 32,
                renderer : this._serviceInformationRenderer,
                hasTip : true,
                tipRenderer : function(value, layer, column, tip) {
                    return 'Click for detailed information about the web services this layer utilises.';
                },
                listeners : {
                    columnclick : Ext.bind(this._serviceInformationClickHandler, this)
                }
            },{
                //Spatial bounds column
                xtype : 'clickcolumn',
                dataIndex : 'spatialBoundsRenderer',
                width: 32,
                renderer : this._spatialBoundsRenderer,
                hasTip : true,
                tipRenderer : function(value, layer, column, tip) {
                    return 'Click to see the bounds of this layer, double click to pan the map to those bounds.';
                },
                listeners : {
                    columnclick : Ext.bind(this._spatialBoundsClickHandler, this),
                    columndblclick : Ext.bind(this._spatialBoundsDoubleClickHandler, this)
                }
            }],
          plugins:[{                
              ptype : 'rowexpandercontainer',
              pluginId : 'maingrid_rowexpandercontainer',
              generateContainer : function(record, parentElId) {
                  var newLayer=null;
                  //VT:if this is deserialized, we don't need to regenerate the layer
                  if(record.get('layer') && record.get('layer').get('deserialized')){                        
                      newLayer =  record.get('layer');                                           
                  }else if(record instanceof portal.csw.CSWRecord){                        
                      newLayer = cfg.layerFactory.generateLayerFromCSWRecord(record);                                                     
                  }else{
                      newLayer = cfg.layerFactory.generateLayerFromKnownLayer(record);                      
                  }           
                  record.set('layer',newLayer);            
                  var filterForm = newLayer ? newLayer.get('filterForm') : null;                                    
                  var filterPanel = me._getInlineLayerPanel(filterForm, parentElId, this);                     
                  return filterPanel;
             }
         },{
          ptype: 'celltips'
         }]
                  
        });

        this.callParent(arguments);
    },
    
    
    _getInlineLayerPanel : function(filterForm, parentElId){                             
        var me = this;   
        var panel =Ext.create('portal.widgets.panel.FilterPanel', {            
            filterForm  : filterForm,                       
            map         : this.map,
            renderTo    : parentElId,
            listeners : {
                addlayer : function(layer){
                    me.activelayerstore.insert(0,layer); //this adds the layer to our store       
                },
                removelayer : function(layer){
                    me.activelayerstore.remove(layer);
                }
            }
        });   
        
        return panel
    },

 

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


        //
    },

    //-------- Abstract methods requiring implementation ---------

    /**
     * Abstract function - Should return a string based title
     * for a given record
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose title should be extracted
     */
    getTitleForRecord : portal.util.UnimplementedFunction,

    /**
     * Abstract function - Should return an Array of portal.csw.OnlineResource
     * objects that make up the specified record. If no online resources exist
     * then an empty array can be returned
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose underlying online resources should be extracted.
     */
    getOnlineResourcesForRecord : portal.util.UnimplementedFunction,

    /**
     * Abstract function - Should return an Array of portal.util.BBox
     * objects that represent the total spatial bounds of the record. If no
     * bounds exist then an empty array can be returned
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose spatial bounds should be extracted.
     */
    getSpatialBoundsForRecord : portal.util.UnimplementedFunction,

    /**
     * Abstract function - Should return an Array of portal.csw.CSWRecord
     * objects that make up the specified record.
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose underlying CSWRecords should be extracted.
     */
    getCSWRecordsForRecord : portal.util.UnimplementedFunction,

    //--------- Class Methods ---------

    /**
     * Generates an Ext.DomHelper.markup for the specified imageUrl
     * for usage as an image icon within this grid.
     */
    _generateHTMLIconMarkup : function(imageUrl) {
        return Ext.DomHelper.markup({
            tag : 'div',
            style : 'text-align:center;',
            children : [{
                tag : 'img',
                width : 16,
                height : 16,
                align: 'CENTER',
                src: imageUrl
            }]
        });
    },

    /**
     * Internal method, acts as an ExtJS 4 column renderer function for rendering
     * the title of the record.
     *
     * http://docs.sencha.com/ext-js/4-0/#!/api/Ext.grid.column.Column-cfg-renderer
     */
    _titleRenderer : function(value, metaData, record, row, col, store, gridView) {
        return this.getTitleForRecord(record);
    },

    /**
     * Internal method, acts as an ExtJS 4 column renderer function for rendering
     * the service information of the record.
     *
     * http://docs.sencha.com/ext-js/4-0/#!/api/Ext.grid.column.Column-cfg-renderer
     */
    _serviceInformationRenderer : function(value, metaData, record, row, col, store, gridView) {
        var onlineResources = this.getOnlineResourcesForRecord(record);

        var containsDataService = false;
        var containsImageService = false;

        //We classify resources as being data or image sources.
        for (var i = 0; i < onlineResources.length; i++) {
            switch(onlineResources[i].get('type')) {
            case portal.csw.OnlineResource.WFS:
            case portal.csw.OnlineResource.WCS:
            case portal.csw.OnlineResource.SOS:
            case portal.csw.OnlineResource.OPeNDAP:
            case portal.csw.OnlineResource.CSWService:
            case portal.csw.OnlineResource.IRIS:
                containsDataService = true;
                break;
            case portal.csw.OnlineResource.WMS:
            case portal.csw.OnlineResource.WWW:
            case portal.csw.OnlineResource.FTP:
            case portal.csw.OnlineResource.CSW:
            case portal.csw.OnlineResource.UNSUPPORTED:
                containsImageService = true;
                break;
            }
        }

        var iconPath = null;
        if (containsDataService) {
            iconPath = 'img/binary.png'; //a single data service will label the entire layer as a data layer
        } else if (containsImageService) {
            iconPath = 'img/picture.png';
        } else {
            iconPath = 'img/cross.png';
        }

        return this._generateHTMLIconMarkup(iconPath);
    },

    /**
     * Internal method, acts as an ExtJS 4 column renderer function for rendering
     * the spatial bounds column of the record.
     *
     * http://docs.sencha.com/ext-js/4-0/#!/api/Ext.grid.column.Column-cfg-renderer
     */
    _spatialBoundsRenderer : function(value, metaData, record, row, col, store, gridView) {
        var spatialBounds = this.getSpatialBoundsForRecord(record);
        if (spatialBounds.length > 0 || record.internalId == 'portal-InSar-reports') {
            // create one for insar
            return this._generateHTMLIconMarkup('img/magglass.gif');
        }

        return '';
    },

    /**
     * Show a popup containing info about the services that 'power' this layer
     */
    _serviceInformationClickHandler : function(column, record, rowIndex, colIndex) {
        var cswRecords = this.getCSWRecordsForRecord(record);
        if (!cswRecords || cswRecords.length === 0) {
            return;
        }

        var popup = Ext.create('portal.widgets.window.CSWRecordDescriptionWindow', {
            cswRecords : cswRecords
        });

        popup.show();
    },


    /**
     * On single click, show a highlight of all BBoxes
     */
    _spatialBoundsClickHandler : function(column, record, rowIndex, colIndex) {
        var spatialBoundsArray;
        if (record.internalId == 'portal-InSar-reports') {
            spatialBoundsArray = this.getWholeGlobeBounds();
        } else {
            spatialBoundsArray = this.getSpatialBoundsForRecord(record);
        }
        var nonPointBounds = [];

        //No point showing a highlight for bboxes that are points
        for (var i = 0; i < spatialBoundsArray.length; i++) {
            var bbox = spatialBoundsArray[i];
            if (bbox.southBoundLatitude !== bbox.northBoundLatitude ||
                bbox.eastBoundLongitude !== bbox.westBoundLongitude) {

                //VT: Google map uses EPSG:3857 and its maximum latitude is only 85 degrees
                // anything more will stretch the transformation
                if(bbox.northBoundLatitude>85){
                    bbox.northBoundLatitude=85;
                }
                if(bbox.southBoundLatitude<-85){
                    bbox.southBoundLatitude=-85;
                }
                nonPointBounds.push(bbox);
            }
        }

        this.map.highlightBounds(nonPointBounds);
    },

    /**
     * Return the max bbox for insar layer as it is a dummy CSW.
     */
    getWholeGlobeBounds : function() {
        var bbox = new Array();
        bbox[0] = Ext.create('portal.util.BBox', {
            northBoundLatitude : 85,
            southBoundLatitude : -85,
            eastBoundLongitude : 180,
            westBoundLongitude : -180
        });
        return bbox;
    },

    /**
     * On double click, move the map so that specified bounds are visible
     */
    _spatialBoundsDoubleClickHandler : function(column, record, rowIndex, colIndex) {
        var spatialBoundsArray;
        if (record.internalId == 'portal-InSar-reports') {
            spatialBoundsArray = this.getWholeGlobeBounds();
        } else {
            spatialBoundsArray = this.getSpatialBoundsForRecord(record);
        }

        if (spatialBoundsArray.length > 0) {
            var superBBox = spatialBoundsArray[0];

            for (var i = 1; i < spatialBoundsArray.length; i++) {
                superBBox = superBBox.combine(spatialBoundsArray[i]);
            }

            this.map.scrollToBounds(superBBox);
        }
    },

    /**
     * When the visible fn is clicked, ensure only the visible records pass the filter
     */
    _handleVisibleFilterClick : function(button) {                
       if(button.getText()=='Filter Visible'){
           var rowExpander = this.getPlugin('maingrid_rowexpandercontainer');
           rowExpander.closeAllContainers();
           button.setText('Clear Filter');
           this._visibleFilterClick(button);
       }else{
           button.setText('Filter Visible');
           this._clearVisibleFilterClick(button);
       }
    },
    
    _visibleFilterClick : function(button) {
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

        var searchField = button.ownerCt.items.getAt(1);
        searchField.runCustomFilter('<visible layers>', Ext.bind(filterFn, this));
    },
    
    _clearVisibleFilterClick : function(button) {
      
        var searchField = button.ownerCt.items.getAt(1);
        searchField.clearCustomFilter();
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
        
    _deleteRenderer : function(value, metaData, record, row, col, store, gridView) {
        if (value) {
            return Ext.DomHelper.markup({
                tag : 'img',
                width : 16,
                height : 16,
                src: 'img/trash.png'
            });
        } else {
            return Ext.DomHelper.markup({
                tag : 'img',
                width : 16,
                height : 16,
                src: 'img/play_blue.png'
            });
        }
    },
    
    _deleteClickHandler :  function(value, record, rowIdx, tip) {
        var layer = record.get('layer');
        if(layer && record.get('active')){            
            layer.removeDataFromMap();
            this.activelayerstore.remove(layer);
            layer.data.filterForm.ownerCt.updateButton(false);
            this.fireEvent('cellclick',this,undefined,undefined,record,undefined,rowIdx);
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
                src: 'img/loading.gif'
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
                        src: 'img/exclamation.png'
                    });
                }else if(errorCount > 0 && errorCount < sizeOfList){
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 16,
                        height : 16,
                        src: 'img/warning.png'
                    });
                }else{
                    return Ext.DomHelper.markup({
                        tag : 'img',
                        width : 16,
                        height : 16,
                        src: 'img/tick.png'
                    });
                }
                
            }else{
                return Ext.DomHelper.markup({
                    tag : 'img',
                    width : 16,
                    height : 16,
                    src: 'img/notloading.gif'
                });
            }
            
            
        }
    },
    
    _statusListErrorCount : function(listOfStatus){
        var match =["reached","error","did not complete"];
        
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