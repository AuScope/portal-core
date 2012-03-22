/**
 * A specialised Ext.grid.Panel instance for
 * displaying a store of portal.layer.Layer objects.
 *
 */
Ext.define('portal.widgets.panel.LayerPanel', {
    extend : 'Ext.grid.Panel',
    alias: 'widget.layerpanel',

    map : null, //instance of portal.util.gmap.GMapWrapper

    constructor : function(cfg) {
        var me = this;

        this.map = cfg.map;
        this.filterPanel = cfg.filterPanel;

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
                dataIndex : 'loading',
                renderer : this._loadingRenderer,
                hasTip : true,
                tipRenderer : Ext.bind(this._loadingTipRenderer, this),
                width: 32
            },{
                //Layer name column
                text : 'Layer Name',
                dataIndex : 'name',
                flex : 1
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
                    columnclick : Ext.bind(this._downloadClickHandler, this)
                }
            }],
            plugins: [{
                ptype: 'rowexpander',
                rowBodyTpl : [
                    '<p>{description}</p><br>'
                ]
            },{
                ptype: 'celltips'
            }],
            bbar: [{
                text : 'Remove Layer',
                iconCls : 'remove',
                handler : function(button) {
                    var grid = button.findParentByType('layerpanel');
                    var sm = grid.getSelectionModel();
                    var selectedRecords = sm.getSelection();
                    if (selectedRecords && selectedRecords.length > 0) {
                        var store = grid.getStore();
                        store.remove(selectedRecords);
                    }
                }
            }]
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
                src: 'img/loading.gif'
            });
        } else {
            return Ext.DomHelper.markup({
                tag : 'img',
                width : 16,
                height : 16,
                src: 'img/notloading.gif'
            });
        }
    },

    /**
     * Renderer for download column
     */
    _downloadRenderer : function(value, metaData, record, row, col, store, gridView) {
        var downloader = record.get('downloader');
        if (value.getHasData() && downloader) { //value is a portal.layer.renderer.Renderer
            return Ext.DomHelper.markup({
                tag : 'a',
                href : 'javascript: void(0)',
                children : [{
                    tag : 'img',
                    width : 16,
                    height : 16,
                    src: 'img/page_code.png'
                }]
            });
        } else {
            return Ext.DomHelper.markup({
                tag : 'a',
                href : 'javascript: void(0)',
                children : [{
                    tag : 'img',
                    width : 16,
                    height : 16,
                    src: 'img/page_code_disabled.png'
                }]
            });
        }
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
     * Raised whenever the download column is clicked
     */
    _downloadClickHandler : function(column, layer, rowIndex, colIndex) {
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
            var downloader = layer.get('downloader');
            if (downloader) {
                var onlineResources = layer.getAllOnlineResources();
                downloader.downloadData(layer, onlineResources, renderedFilterer, currentFilterer);
            }
        }
    },

    /**
     * Raised whenever the legend column is clicked
     */
    _legendClickHandler : function(column, layer, rowIndex, colIndex) {
        //The callback takes our generated component and displays it in a popup window
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

        legend.getLegendComponent(onlineResources, filterer, Ext.bind(legendCallback, this, [layer], true));
    }
});