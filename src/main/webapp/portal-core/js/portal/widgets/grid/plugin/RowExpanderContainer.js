/**
 * A plugin for an Ext.grid.Panel class that a container will show
 * whenever a row is selected. The container will render horizontally
 * below the selected row, seemingly "inline"
 *
 * To use this plugin, assign the following field to the plugin constructor
 * {
 *  generateContainer : function(record, parentElId, grid) - returns Ext.container.Container,
 *  allowMultipleOpen : Boolean - whether multiple containers can be open simultaneously.
 *  toggleColIndexes : int[] - Optional - Which column indexes can toggle open/close on single click - Defaults to every column
 *  recordIdProperty: Optional String - The property that should be used for record ID value (defaults to 'id'). If the 
 *                                      record idProperty has been defined you will to specify that value here. 
 *  baseId : String - Optional (default='rowexpandercontainer') - To be used as the base in the containing element Id so can 
 *      reuse this control in multiple locations (all baseIds must be unique) 
 * }
 *
 * Contains two events:
 *  containerhide, containershow
 *
 * Example usage:
 *  var panel = Ext.create('Ext.grid.Panel', {
 *                title : 'Grid Panel Test',
 *                store : store,
 *                split: true,
 *                renderTo: 'foo',
 *                plugins : [{
 *                  ptype : 'rowexpandercontainer',
 *                  generateContainer : function(record, parentElId, grid) {
 *                     return Ext.create('Ext.panel.Panel', {});
 *                  }
 *                }]
 *  });
 *
 * Work has been adapted from: http://www.rahulsingla.com/blog/2010/04/extjs-preserving-rowexpander-markup-across-view-refreshes#comment-86
 */
Ext.define('portal.widgets.grid.plugin.RowExpanderContainer', {
    extend: 'Ext.grid.plugin.RowExpander',
    
    alias: 'plugin.rowexpandercontainer',
    generateContainer : portal.util.UnimplementedFunction,
    allowMultipleOpen : false,
    recordIdProperty: null, //can be null
    rowBodyTpl: null, 
    storedHtml: null,   
    recordStatus: null,  
    generationRunning: false,
    toggleColIndexes: null,
    baseId: "RowExpanderContainer",
    expandOnEnter : false,

    
    constructor: function(config) {
        this.callParent(arguments);
        
        this.toggleColIndexes = Ext.isArray(config.toggleColIndexes) ? config.toggleColIndexes : null;
        this.allowMultipleOpen = config.allowMultipleOpen ? true : false;
        this.storedHtml = {};
        this.recordStatus = {};
        
        if (config.recordIdProperty) {
            this.recordIdProperty = config.recordIdProperty;
        }
        
        if (config.baseId) {
            this.baseId = config.baseId;
        }
        this.rowBodyTpl = '<div id="'+this.baseId+'-{[values.' + (this.recordIdProperty ? this.recordIdProperty : 'id') + '.toString().replace(/[^0-9A-Za-z\\-]/g,\'-\')]}"></div>'
    },
    
    //override to do nothing. We don't want an expander column
    addExpander: function(expanderGrid) {
        
    },
    
    init : function(grid) {
        this.callParent(arguments);
        var view = grid.getView();
        
        this.view = view;
        this.grid = grid;
        
        view.on('expandbody', this.onExpandBody, this);
        view.on('collapsebody', this.onCollapseBody, this);
        view.on('cellclick', this.onCellClick, this);
        view.on('groupexpand', this.onGroupExpand, this);
        view.on('groupcollapse', this.onGroupExpand, this);
        
        view.on('refresh', this.onRefresh, this);
        
        view.on('itemupdate', this.restoreRowContainer, this);
        
        view.on('resize', this.onResize, this);
        
        grid.getStore().on('clear', function(store) {
            this._hardReset();
        }, this);
    },
    
    /**
     * Resets the row expander state to that of a fresh instance. Cleans up any existing containers
     */
    _hardReset: function() {
        for (id in this.recordStatus) {
            var status = this.recordStatus[id];
            if (status && status.container) {
                status.container.destroy();
            }
        }
        
        this.storedHtml = {};
        this.recordStatus = {};
    },
    
    /**
     * Utility for accessing the defined "ID" property of a record
     */
    _getId: function(record, sanitise) {
        var id;
        if (this.recordIdProperty) {
            id = record.get(this.recordIdProperty).toString();
        } else {
            id = record.id.toString();
        }
        
        if (sanitise) {
            id = id.replace(/[^0-9A-Za-z\\-]/g,'-');
        }
        
        return id;
    },
    
    /**
     * Returns record if it exists or null.
     * 
     * @param recordId String
     */
    getStoreRecord: function(recordId) {
        return this.grid.getStore().getById(recordId);
    },
    
    /**
     * Returns true if the given record is rendered, expanded AND 
     * the internal rowbody is empty
     */
    restorationRequired: function(record) {
        //Is the record expanded?
        var id = this._getId(record);
        if (!(id in this.recordStatus)) {
            return false;
        } else if (!this.recordStatus[id].expanded) {
            return false;
        } 
        
        //Is the record visible?
        var el = this.view.getRow(record);
        if (!el) {
            return false;
        }
        
        var body = Ext.DomQuery.selectNode('#'+this.baseId + '-' + this._getId(record, true), el.parentNode); // rowexpandercontainer-'
        if (body.hasChildNodes()) {
            return false;
        }
        
        return true;
    },
    
    getRecordsForGroup: function(group) {
        var ds = this.grid.getStore();
        
        var groupInfo = ds.getGroups()[group];
        if (!groupInfo) {
            return [];
        }
        
        return groupInfo.children;
    },



    onExpandBody : function(rowNode, record, expandRow) {
        if (!this.allowMultipleOpen) {
            for (openId in this.recordStatus) {
                if (this.recordStatus[openId].expanded) {
                    var openRec = this.getStoreRecord(openId);
                    var openEl = this.view.getRow(openRec);
                    if (openEl !== null) {
                        this.toggleRow(openEl, openRec);
                    }
                }
            }
        }
        
        this.recordStatus[this._getId(record)] = {
            expanded : true,
            container : null 
        };
        
        this.restoreRowContainer(record);
    },
    
    onCollapseBody: function(rowNode, record, collapseRow) {
        this.recordStatus[this._getId(record)].expanded = false;
    },
    
    onCellClick: function(view, td, cellIndex, record, tr, rowIndex) {
        if (!Ext.isArray(this.toggleColIndexes) || Ext.Array.contains(this.toggleColIndexes, cellIndex)) {
            this.toggleRow(rowIndex, record);
        }
    },
    
    onResize: function() {
        for (openId in this.recordStatus) {
            if (this.recordStatus[openId].expanded) {
                if (this.recordStatus[openId].container) {
                    this.recordStatus[openId].container.updateLayout({
                        defer:false,
                        isRoot:false
                    });
                }
            }
        }
    },
    
    onGroupExpand : function(view, node, group, eOpts) {
        var recs = this.grid.getStore().getRange();
        var me = this;
        Ext.each(recs, function(record) {
            me.restoreRowContainer(record);
        });
    },
    
    onRefresh: function(view) {
        var store = this.grid.getStore(),
            n, row, record;
        for (n = 0; n < store.data.items.length; n++) {
            row = view.getRow(n);
            if (row) {
                record = store.getAt(n);
                this.restoreRowContainer(record);
            }
        }
    },
    
    restoreRowContainer: function(record) {        
        var me = this;
        
        //We don't want this function to be re-entrant
        //Which can occur if the generateContainer callback
        //makes any updates to record
        if (me.generationRunning === true) {
            return;
        }
        
        me.generationRunning = true;
        if (me.restorationRequired(record)) {
            var id = me.baseId + '-' + me._getId(record, true);   // "rowexpandercontainer-"
            var container = me.generateContainer(record, id, me.grid);
            
            // If a container already existed then destroy it first.
            if (me.recordStatus[me._getId(record)].container) {
                //AUS-2608 - Wrapped this in a try-catch due to some tooltips getting orphaned and throwing a JS error when
                //           trying to reference their dead parent.                
                try {
                    me.recordStatus[me._getId(record)].container.destroy();
                } catch(err) {
                    console.log("Error destroying parent container:", err);
                }
            }
            me.recordStatus[me._getId(record)].container = container;
            me.recordStatus[me._getId(record)].container.updateLayout({
                defer:false,
                isRoot:false
            });
        }

        this.generationRunning = false;
    }
    
});
