/**
 * A specialised Ext.grid.Panel instance for
 * displaying a store of portal.layer.Layer objects.
 * VT:Mark for deletion
 */
Ext.define('portal.widgets.panel.LayerPanel', {
    extend : 'Ext.grid.Panel',
    alias: 'widget.layerpanel',

    map : null, //instance of portal.util.gmap.GMapWrapper
    allowDebugWindow : false, //whether this panel will show the debug window if clicked by the user

    constructor : function(cfg) {
        var me = this;

        this.map = cfg.map;
        this.allowDebugWindow = cfg.allowDebugWindow ? true : false;
      

        this.removeAction = new Ext.Action({
            text : 'Remove Layer',
            iconCls : 'remove',
            handler : Ext.bind(function(cmp) {
                var sm = this.getSelectionModel();
                var selectedRecords = sm.getSelection();
                if (selectedRecords && selectedRecords.length > 0) {
                    var store = this.getStore();
                    store.remove(selectedRecords);
                    this.fireEvent('removelayerrequest', this, selectedRecords[0]);//only support single selection
                }
            }, this)
        });
        
        
        this.downloadLayerAction = new Ext.Action({
            text : 'Download Layer',
            iconCls : 'download',
            handler : Ext.bind(this._downloadClickHandler,this)
        });
        

        Ext.apply(cfg, {
            columns : [{
                //legend column
                xtype : 'clickcolumn',
                dataIndex : 'renderer',
                renderer : this._legendIconRenderer,
                width : 32,
                listeners : {
                    columnclick : Ext.bind(this._legendClickHandler, this)
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
                    columnclick : Ext.bind(this._serviceInformationClickHandler, this)
                }
            },{
                //Layer name column
                xtype : 'clickcolumn',
                text : 'Layer Name',
                dataIndex : 'name',
                flex : 1,
                listeners : {
                    columnclick : Ext.bind(this._nameClickHandler, this)
                }
            },{
                //Visibility column
                xtype : 'renderablecheckcolumn',
                text : 'Visible',
                dataIndex : 'renderer',
                getCustomValueBool : function(header, renderer, layer) {
                    return renderer.getVisible();
                },
                setCustomValueBool : function(header, renderer, checked, layer) {
                    //update our bbox silently before updating visibility
                    if (checked) {
                        var filterer = layer.get('filterer');
                        filterer.setSpatialParam(me.map.getVisibleMapBounds(), true);
                    }

                    return renderer.setVisible(checked);
                },
                width : 40
            },{
                //Download column
                xtype : 'clickcolumn',
                dataIndex : 'renderer',
                width : 32,
                renderer : this._downloadRenderer,
                listeners : {                                                            
                    columnclick : function( column, record, recordIndex, cellIndex, e){
                        var menu = Ext.create('Ext.menu.Menu', {
                            items: [
                                    me.removeAction,
                                    me.downloadLayerAction
                                    ]                
                        });
                        menu.showAt(e.getXY());
                    }
                }
            }],
            plugins: [{
                ptype: 'rowexpander',
                rowBodyTpl : [
                    '<p>{description}</p><br>'
                ]
            },{
                ptype: 'celltips'
            },{
                ptype : 'rowcontextmenu',
                contextMenu : Ext.create('Ext.menu.Menu', {
                    items: [
                            this.removeAction,
                            this.downloadLayerAction
                            ]                
                })
            }],

            viewConfig:{
                plugins:[{
                    ptype: 'gridviewdragdrop',
                    dragText: 'Drag and drop to re-order'
                }],
                listeners: {
                    beforedrop: function(node, data, overModel, dropPosition,  dropFunction,  eOpts ){
                        if(data.records[0].data.renderer instanceof portal.layer.renderer.wms.LayerRenderer){
                            return true;
                        }else{
                            alert('Only wms layers can be reordered');
                            return false;
                        }
                    }
                }
            }
           
        });

        this.callParent(arguments);
    },

    /**
     * Renderer for the legend icon column
     */
    _legendIconRenderer : function(value, metaData, record, row, col, store, gridView) {
        if (!value) {
            return '';
        }

        var legend = value.getLegend();
        if (!legend) {
            return '';
        }

        return legend.getLegendIconHtml(record.getAllOnlineResources(), record.data.filterer);
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
            return Ext.DomHelper.markup({
                tag : 'img',
                width : 16,
                height : 16,
                src: 'portal-core/img/notloading.gif'
            });
        }
    },

    /**
     * Renderer for download column
     */
    _downloadRenderer : function(value, metaData, record, row, col, store, gridView) {
      
            return Ext.DomHelper.markup({
                tag : 'a',
                href : 'javascript: void(0)',
                children : [{
                    tag : 'img',
                    width : 16,
                    height : 16,
                    src: 'portal-core/img/setting.png'
                }]
            });
      
    },

    /**
     * A renderer for generating the contents of the tooltip that shows when the
     * layer is loading
     */
    _loadingTipRenderer : function(value, layer, column, tip) {
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

    /**
     * Raised whenever the name column is clicked
     */
    _nameClickHandler : function(column, layer, rowIndex, colIndex) {
        if (!this.allowDebugWindow) {
            return;
        }

        //Show off the rendering debug information
        var debugData = layer.get('renderer').getRendererDebuggerData();
        if (!debugData) {
            return;
        }

        //Raised whenever the underlying debug information changes
        //designed to update an already showing window
        var onChange = function(debugData, keys, eOpts) {
            eOpts.win.body.update(debugData.renderHtml());
            eOpts.win.doLayout();
        };

        //if the window gets closed - stop listening for change events
        var onClose = function(debugWin, eOpts) {
            debugData.un('change', onChange, this);
        };

        //Simplistic window
        var debugWin = Ext.create('Ext.window.Window', {
            title : 'Renderer Debug Information',
            autoScroll : true,
            width : 500,
            height : 300,
            modal : true,
            html : debugData.renderHtml()
        });

        debugData.on('change', onChange, this, {win : debugWin});
        debugWin.on('beforeclose', onClose, this);

        debugWin.show();
    },

    /**
     * Raised whenever the download column is clicked
     */
    _downloadClickHandler : function() {
        var layer = this.getSelectionModel().getSelection()[0];
        var downloader = layer.get('downloader');
        var renderer = layer.get('renderer');
        if (downloader && renderer.getHasData()) {
            //We need a copy of the current filter object (in case the user
            //has filled out filter options but NOT hit apply filter) and
            //the original filter objects
            var renderedFilterer = layer.get('filterer').clone();
            var currentFilterer = Ext.create('portal.layer.filterer.Filterer', {});
            var currentFilterForm = layer.get('filterForm');

            currentFilterer.setSpatialParam(this.map.getVisibleMapBounds(), true);
            currentFilterForm.writeToFilterer(currentFilterer);

            //Finally pass off the download handling to the appropriate downloader (if it exists)
            var onlineResources = layer.getAllOnlineResources();
            downloader.downloadData(layer, onlineResources, renderedFilterer, currentFilterer);

        }
    },

    /**
     * Show a popup containing info about the services that 'power' this layer
     */
    _serviceInformationClickHandler : function(column, record, rowIndex, colIndex) {
        var cswRecords = record.get('cswRecords');
        if (!cswRecords || cswRecords.length === 0) {
            return;
        }
        var popup = Ext.create('portal.widgets.window.CSWRecordDescriptionWindow', {
            cswRecords : cswRecords,
            parentRecord : record
        });
        popup.show();
    },

    /**
     * Raised whenever the legend column is clicked
     */
    _legendClickHandler : function(column, layer, rowIndex, colIndex) {
        //The callback takes our generated component and displays it in a popup window
        // this will be resized dynamically as legend content is added
        var legendCallback = function(legend, resources, filterer, success, form, layer){
            if (success && form) {
                var win = Ext.create('Ext.window.Window', {
                    title       : 'Legend: '+ layer.get('name'),
                    layout      : 'fit',
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

        Ext.Ajax.request({
            url: styleUrl,
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
});
