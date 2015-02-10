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
    recordIdAtRow: null, 
    recordStatus: null,  

    
    constructor: function(config) {
        this.callParent(arguments);
        
        this.allowMultipleOpen = config.allowMultipleOpen ? true : false;
        this.storedHtml = {};
        this.recordIdAtRow = {};
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
        view.on('groupcollapse', this.onGroupCollapse, this);
        
        grid.on('viewready', this.markRows, this);
        grid.on('rowsinserted', this.markRows, this);
        view.on('beforerefresh', this.onBeforeRefresh, this);
        view.on('refresh', this.onRefresh, this);
        
        view.on('itemupdate', this.restoreRowHtml, this);
    },
    
    onExpandBody: function(rowNode, record, expandRow) {
        var id = "rowexpandercontainer-" + record.id;
        if (!(record.id in this.recordStatus) || this.recordStatus[record.id].rendered === false) {
            this.generateContainer(record, id);
            this.saveRowHtml(record);
        }
        
        if (!this.allowMultipleOpen) {
            for (rowIndex in this.recordIdAtRow) {
                var openId = this.recordIdAtRow[rowIndex];
                if (openId in this.recordStatus && this.recordStatus[openId].expanded) {
                    var idxNum = Number(rowIndex);
                    this.toggleRow(idxNum, this.grid.getStore().getAt(idxNum));
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
        console.log('Expanding:', node);
    },
    
    onGroupCollapse : function(view, node, group, eOpts) {
        console.log('Collapsing:', node);
    },
    
    /////////////////////////////////////////////
    
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
        var rowIndex = null,
            found = false; //Find the view's rowIndex for this record before the refresh action happened. 
                           //While saving the Row Html for a Record, we have to find the previous position of the record in the view before 
                           //the refresh action happened. 
        for (rowIndex in this.recordIdAtRow) {
            if (this.recordIdAtRow[rowIndex] == record.id) {
                found = true;
                break;
            }
        }
        if (found) {
            var row = this.view.getRow(Number(rowIndex));
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
                this.restoreRowHtml(view, n, record);
            }
        }
    },
    restoreRowHtml: function(record, index) {
        //When restoring the Row Html for a Record, the restore has to happen at the current position of the Record after 
        //the refresh action happened. 
        var storedBody = this.storedHtml[record.id];
        if (!Ext.isEmpty(storedBody)) {
            var row = this.view.getRow(index);
            var body = Ext.DomQuery.selectNode('#rowexpandercontainer-' + record.id, row.parentNode);
            while (body.hasChildNodes()) {
                body.removeChild(body.lastChild);
            }
            var status = this.recordStatus[record.id];
            if (!status) { //the row has not been touched. 
                body.innerHTML = this.tpl.html;
                this.collapseRow(row);
            } else {
                body.innerHTML = storedBody;
                /*if (!status.expanded) { 
                    this.toggleRow(index, record);
                }*/
            }
        }
        this.markRow(this.view, index, record);
    }, 
    
    markRow: function(view, index, record) {
        var row = view.getRow(index);
        if (row) {
            this.recordIdAtRow[index] = record.id;
        }
    },
    
    markRows: function() {
        var view = this.grid.getView(),
            store = this.grid.getStore(),
            record, n;
        this.recordIdAtRow = {};
        for (n = 0; n < store.data.items.length; n++) {
            record = store.getAt(n);
            this.markRow(view, n, record);
        }
    }
});