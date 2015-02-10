/**
 * A plugin for an Ext.grid.Panel class that a container will show
 * whenever a row is selected. The container will render horizontally
 * below the selected row, seemingly "inline"
 *
 * To use this plugin, assign the following field to the plugin constructor
 * {
 *  generateContainer : function(record, parentElId) - returns Ext.container.Container,
 *  allowMultipleOpen : Boolean - whether multiple containers can be open simultaneously.
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
 *                  generateContainer : function(record, parentElId) {
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
    rowBodyTpl: '<div id="rowexpandercontainer-{id}"></div>', //overrides parent
    storedHtml: null,   
    recordStatus: null,  

    
    constructor: function(config) {
        this.callParent(arguments);
        
        this.allowMultipleOpen = config.allowMultipleOpen ? true : false;
        this.storedHtml = {};
        this.recordStatus = {};
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
        
        view.on('beforerefresh', this.onBeforeRefresh, this);
        view.on('refresh', this.onRefresh, this);
        
        view.on('itemupdate', this.restoreRowHtml, this);
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
        if (!(record.id in this.recordStatus)) {
            return false;
        } else if (!this.recordStatus[record.id].expanded) {
            return false;
        } 
        
        //Is the record visible?
        var el = this.view.getRow(record);
        if (!el) {
            return false;
        }
        
        var body = Ext.DomQuery.selectNode('#rowexpandercontainer-' + record.id, el.parentNode);
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
    
    onExpandBody: function(rowNode, record, expandRow) {
        var id = "rowexpandercontainer-" + record.id;
        if (!(record.id in this.recordStatus) || this.recordStatus[record.id].rendered === false) {
            this.generateContainer(record, id);
            this.saveRowHtml(record);
        }
        
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
        
        var status = {
            rendered: true,
            expanded: true
        };
        this.recordStatus[record.id] = status;
    },
    
    onCollapseBody: function(rowNode, record, collapseRow) {
        var status = {
            rendered: true,
            expanded: false
        };
        this.recordStatus[record.id] = status;
    },
    
    onCellClick: function(view, td, cellIndex, record, tr, rowIndex) {
        this.toggleRow(rowIndex, record);
    },
    
    onGroupExpand : function(view, node, group, eOpts) {
        var recs = this.grid.getStore().getRange();
        var me = this;
        Ext.each(recs, function(record) {
            if (me.restorationRequired(record)) {
                me.restoreRowHtml(record);
            }
        });
    },
    
    onBeforeRefresh: function(view) {
        var store = this.grid.getStore(),
            n, record;
        this.storedHtml = {};
        for (n = 0; n < store.data.items.length; n++) {
            record = store.getAt(n);
            this.saveRowHtml(view, n, record);
        }
    },
    
    saveRowHtml: function(record) {
        if (this.getStoreRecord(record.id)) {
            var row = this.view.getRow(record);
            var body = Ext.DomQuery.selectNode('#rowexpandercontainer-' + record.id, row.parentNode);
            this.storedHtml[record.id] = body.innerHTML;
        }
    }, 
    
    onRefresh: function(view) {
        var store = this.grid.getStore(),
            n, row, record;
        for (n = 0; n < store.data.items.length; n++) {
            row = view.getRow(n);
            if (row) {
                record = store.getAt(n);
                this.restoreRowHtml(record);
            }
        }
    },
    
    restoreRowHtml: function(record) {
        //When restoring the Row Html for a Record, the restore has to happen at the current position of the Record after 
        //the refresh action happened. 
        var storedBody = this.storedHtml[record.id];
        if (!Ext.isEmpty(storedBody)) {
            var row = this.view.getRow(record);
            var body = Ext.DomQuery.selectNode('#rowexpandercontainer-' + record.id, row.parentNode);
            while (body.hasChildNodes()) {
                body.removeChild(body.lastChild);
            }
            body.innerHTML = storedBody;
        }
    }
    
});