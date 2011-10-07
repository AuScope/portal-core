/**
 * A Ext.grid.GridPanel specialisation for allowing the user to browse
 * a CSW service with a configured filter.
 *
 * The filter is designed to be generated from a CSWThemeFilterForm
 */
CSWFilterResultsPanel = Ext.extend(Ext.grid.GridPanel, {


    cswRecordStore : null,
    filterParams : null,
    map : null,

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.grid.GridPanel as well as the following values
     * {
     *  filterParams : An object containing filter parameters (generate this from a CSWThemeFilterForm)
     *  map : An instance of a Map object
     * }
     */
    constructor : function(cfg) {
        var cswFilterResultsPanel = this;

        this.map = cfg.map;
        this.filterParams = cfg.filterParams;
        this.cswRecordStore = new CSWRecordStore('getFilteredCSWRecords.do', cfg.filterParams);

        var initialLoadParams = Ext.apply(this.filterParams, {
            limit : 10,
            start : 0
        })
        this.cswRecordStore.load(initialLoadParams);

        //Build our configuration object
        Ext.apply(cfg, {
            hideHeaders : true,
            cm : new Ext.grid.ColumnModel([{
                id : 'title',
                dataIndex : 'serviceName',
                renderer : function(value, p, record) {
                    return String.format('<div><b>{0}</b></div>', value);
                }
            },{
                id : 'recordType',
                header : '',
                width: 18,
                dataIndex: 'onlineResources',
                renderer: function(value, metadata, record) {
                    for (var i = 0; i < value.length; i++) {
                        if (value[i].onlineResourceType == 'WCS' ||
                            value[i].onlineResourceType == 'WFS') {
                            return '<div style="text-align:center"><img src="img/binary.png" width="16" height="16" align="CENTER"/></div>';
                        }
                    }

                    return '<div style="text-align:center"><img src="img/picture.png" width="16" height="16" align="CENTER"/></div>';
                }
            }, {
                id:'search',
                header: '',
                width: 45,
                dataIndex: 'geographicElements',
                resizable: false,
                menuDisabled: true,
                sortable: false,
                fixed: true,
                renderer: function (value) {
                    if (value.length > 0) {
                        return '<img src="img/magglass.gif"/>';
                    } else {
                        return '';
                    }
                }
            }]),
            store : this.cswRecordStore,
            sm : new Ext.grid.RowSelectionModel({
                singleSelect : false
            }),
            loadMask : {msg : 'Performing CSW Filter...'},
            viewConfig : {
                forceFit : true,
                enableRowBody:true,
                showPreview:true,
                getRowClass : function(record, rowIndex, p, ds){
                    if(this.showPreview){
                        var description = record.data.dataIdentificationAbstract;
                        var maxLength = 190;
                        if (description.length > maxLength) {
                            description = description.substring(0, maxLength) + '...';
                        }
                        p.body = '<p style="margin:5px 10px 10px 25px;color:#555;">'+description+'</p>';
                        return 'x-grid3-row-expanded';
                    }
                    return 'x-grid3-row-collapsed';
                }
            },
            bbar : new Ext.PagingToolbar({
                pageSize: 10,
                store: this.cswRecordStore,
                displayInfo : true,
                displayMsg : 'Displaying records {0} - {1} of {2}',
                emptyMsg: 'No records pass the specified filter(s)'
            }),
            listeners : {
                celldblclick : function (grid, rowIndex, colIndex, e) {
                    var record = grid.getStore().getAt(rowIndex);
                    var fieldName = grid.getColumnModel().getDataIndex(colIndex);
                    if (fieldName === 'geographicElements') {
                        e.stopEvent();

                        cswFilterResultsPanel._moveToBoundsHandler(grid.getStore().getCSWRecordAt(rowIndex));
                    }
                },

                //On single click show information based on column
                cellclick : function (grid, rowIndex, colIndex, e) {
                    var fieldName = grid.getColumnModel().getDataIndex(colIndex);
                    var cswRecord = grid.getStore().getCSWRecordAt(rowIndex);
                    if (fieldName === 'geographicElements') {
                        e.stopEvent();

                        cswFilterResultsPanel._showBoundsCSWRecord(cswRecord);
                    } else if (fieldName === 'onlineResources') {
                        e.stopEvent();

                        var selectedRecord = grid.cswRecordStore.getCSWRecordAt(rowIndex);
                        var popup = new CSWRecordMetadataWindow({
                            cswRecord : selectedRecord
                        });

                        //var popup = new CSWRecordDescriptionWindow(selectedRecord);
                        popup.show(this);
                    }
                },

                //Handles tooltip generation
                mouseover : function(e, t) {
                    e.stopEvent();

                    var row = e.getTarget('.x-grid3-row');
                    var col = e.getTarget('.x-grid3-col');


                    //if there is no visible tooltip then create one, if on is visible already we dont want to layer another one on top
                    if (col !== null && (!this.currentToolTip || !this.currentToolTip.isVisible())) {

                        //get the actual data record
                        var theRow = this.getView().findRow(row);
                        var cswRecord = new CSWRecord(this.getStore().getAt(theRow.rowIndex));

                        var autoWidth = !Ext.isIE6 && !Ext.isIE7;

                        //This is for the 'record type' column
                        if (col.cellIndex == '1') {

                            this.currentToolTip = new Ext.ToolTip({
                                target: e.target ,
                                title: 'Service Information for ' + cswRecord.getServiceName(),
                                autoHide : true,
                                html: 'Click for detailed information about the web services this record utilises',
                                anchor: 'bottom',
                                trackMouse: true,
                                showDelay:60,
                                autoHeight:true,
                                autoWidth: autoWidth,
                                listeners : {
                                    hide : function(component) {
                                        component.destroy();
                                    }
                                }
                            });
                        }
                        //this is the status icon column
                        else if (col.cellIndex == '2') {
                            this.currentToolTip = new Ext.ToolTip({
                                target: e.target ,
                                title: 'Bounds Information',
                                autoHide : true,
                                html: 'Click to see the bounds of this record',
                                anchor: 'bottom',
                                trackMouse: true,
                                showDelay:60,
                                autoHeight:true,
                                autoWidth: autoWidth,
                                listeners : {
                                    hide : function(component) {
                                        component.destroy();
                                    }
                                }
                            });
                        }
                    }
                }
            }

        });

        //Call parent constructor
        CSWFilterResultsPanel.superclass.constructor.call(this, cfg);
    },

    /**
     * A helper function for moving the internal map so that cswRecord's bounds are visible
     */
    _moveToBoundsHandler : function(cswRecord) {
        var bbox = cswRecord.generateGeographicExtent();
        if (!bbox) {
            return;
        }

        var sw = new GLatLng(bbox.southBoundLatitude, bbox.westBoundLongitude);
        var ne = new GLatLng(bbox.northBoundLatitude, bbox.eastBoundLongitude);
        var layerBounds = new GLatLngBounds(sw,ne);

        //Adjust zoom if required
        var visibleBounds = map.getBounds();
        this.map.setZoom(this.map.getBoundsZoomLevel(layerBounds));

        //Pan to position
        var layerCenter = layerBounds.getCenter();
        this.map.panTo(layerCenter);
    },

    /**
     * A helper function for visualising (on the internal map) a cswRecord's bounds
     */
    _showBoundsCSWRecord : function(cswRecord) {
        var geoEls = cswRecord.getGeographicElements();
        var bboxOverlayManager = cswRecord.getBboxOverlayManager();
        if (bboxOverlayManager) {
            bboxOverlayManager.clearOverlays();
        } else {
            bboxOverlayManager = new OverlayManager(this.map);
            cswRecord.setBboxOverlayManager(bboxOverlayManager);
        }

        //Iterate our geographic els to get our list of bboxes
        for (var i = 0; i < geoEls.length; i++) {
            var geoEl = geoEls[i];
            if (geoEl instanceof BBox) {
                var polygonList = geoEl.toGMapPolygon('00FF00', 0, 0.7,'#00FF00', 0.6);

                for (var j = 0; j < polygonList.length; j++) {
                    polygonList[j].title = 'bbox';
                    bboxOverlayManager.addOverlay(polygonList[j]);
                }
            }
        }

        //Make the bbox disappear after a short while
        var clearTask = new Ext.util.DelayedTask(function(){
            bboxOverlayManager.clearOverlays();
        });

        clearTask.delay(2000);
    },

    /**
     * Returns a (possibly empty) Array of CSWRecord objects representing the
     * selected records
     */
    getSelectedCSWRecords : function() {
        var sm = this.getSelectionModel();
        var selectedRecords = sm.getSelections();

        //Transform our selected records into proper CSWRecord objects
        for (var i = 0; i < selectedRecords.length; i++) {
            selectedRecords[i] = new CSWRecord(selectedRecords[i]);
        }

        return selectedRecords;
    }
});

Ext.reg('cswresultspanel', CSWFilterResultsPanel);