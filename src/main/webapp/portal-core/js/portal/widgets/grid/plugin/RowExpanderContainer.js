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
                  title : 'Grid Panel Test',
                  store : store,
                 split: true,
                  renderTo: 'foo',
                  plugins : [{
                      ptype : 'rowexpandercontainer',
                      generateContainer : function(record, parentElId) {
                           return Ext.create('Ext.panel.Panel', {});
                     }
                  }]
    });

 */
Ext.define('portal.widgets.grid.plugin.RowExpanderContainer', {
    extend: 'Ext.AbstractPlugin',

    alias: 'plugin.rowexpandercontainer',

    generateContainer : portal.util.UnimplementedFunction,
    allowMultipleOpen : false,

    recordComponentIds : null,
    recordComponents : null,

    rowBodyTrSelector: '.x-grid-rowbody-tr',
    rowBodyHiddenCls: 'x-grid-row-body-hidden',
    rowCollapsedCls: 'x-grid-row-collapsed',

    /**
     * Supported config options
     * {
     *  generateContainer : function(record, parentElId) - returns Ext.container.Container
     * }
     */
    constructor : function(cfg) {
        this.callParent(arguments);

        if (cfg.generateContainer) {
            this.generateContainer= cfg.generateContainer;
        }
        if (cfg.allowMultipleOpen) {
            this.allowMultipleOpen = cfg.allowMultipleOpen;
        }
        this.recordComponentIds = {};
        this.recordComponents = {};
        this.rowsExpanded = {};

        // We enable the toolbar by bootstrapping it into the Ext.grid.Panel
        // rowbody feature which allows us to inject arbitrary HTML. We use this
        // to generate a DIV which we render our toolbar into.
        var features = [{
            ftype: 'rowbody',
            recordComponentIds: this.recordComponentIds,
            recordComponents: this.recordComponents,
            rowsExpanded : this.rowsExpanded,
            rowBodyHiddenCls: this.rowBodyHiddenCls,
            rowCollapsedCls: this.rowCollapsedCls,
            getAdditionalData: this._getRowBodyFeatureData,
            rowBodyTpl: Ext.create('Ext.XTemplate', '<div id="{component-id}"></div>')
        },{
            ftype: 'rowwrap'
        }];

        var grid = this.getCmp();
        if (grid.features) {
            grid.features = features.concat(grid.features);
        } else {
            grid.features = features;
        }               

    },

    init: function(grid) {
        grid.on('cellclick', this._onContextMenuItemClick, this);      
      
    },

    /**
     * Close any open containers
     */
    closeAllContainers : function() {
        for (idx in this.rowsExpanded) {
            var i = parseInt(this.rowsExpanded[idx]);            
            this.hideContainer(i);            
        }                      
    },      

    /**
     * Hide a specifc container (by row index)
     */
    hideContainer : function(rowIdx) {
        var grid = this.getCmp(),
            view = grid.getView(),
            rowNode = view.getNode(rowIdx),
            row = Ext.get(rowNode),
            nextBd = Ext.get(row).down(this.rowBodyTrSelector),
            record = view.getRecord(rowNode);

        row.addCls(this.rowCollapsedCls);
        nextBd.addCls(this.rowBodyHiddenCls);
        delete this.rowsExpanded[record.internalId];
        //this.recordComponents[rowIdx].destroy();
        view.refreshSize();
        view.fireEvent('contexthide', rowNode, record, nextBd.dom);
    },

    /**
     * Show a specifc container (by row index)
     */
    showContainer : function(rowIdx) {
        var grid = this.getCmp(),
            view = grid.getView(),
            rowNode = view.getNode(rowIdx),
            row = Ext.get(rowNode),
            nextBd = Ext.get(row).down(this.rowBodyTrSelector),
            record = view.getRecord(rowNode);

        //Close any open context menus
        if (!this.allowMultipleOpen) {
            this.closeAllContainers();
        }

        row.removeCls(this.rowCollapsedCls);
        nextBd.removeCls(this.rowBodyHiddenCls);
        this.rowsExpanded[record.internalId] = rowIdx;
        view.refreshSize();
        view.fireEvent('contextshow', rowNode, record, nextBd.dom);
        
        if(!this.recordComponents[record.internalId]){
            this.recordComponents[record.internalId] = this.generateContainer(record, this.recordComponentIds[record.internalId]);
        }else{
            //VT:Check that the dom still exist as grid:refresh can wipe the panel clean.
            var parentEl = Ext.get(this.recordComponentIds[record.internalId]);
            if(parentEl.dom.firstChild == null && parentEl){             
                this.recordComponents[record.internalId].render(parentEl.dom);               
            }
        }
    },

    /**
     * Toggle Hide/Show for a specifc row index
     */
    toggleContainer : function(rowIdx) {
        var rowNode = this.getCmp().getView().getNode(rowIdx),
            row = Ext.get(rowNode),
            nextBd = Ext.get(row).down(this.rowBodyTrSelector);

        if (row.hasCls(this.rowCollapsedCls)) {
            this.showContainer(rowIdx);
        } else {
            this.hideContainer(rowIdx);
        }
    },

    _getRowBodyFeatureData: function(data, idx, record, orig) {
        var o = Ext.grid.feature.RowBody.prototype.getAdditionalData.apply(this, arguments),
            id = this.columnId,
            componentId = this.recordComponentIds[record.internalId];

        if (!componentId) {
            this.recordComponentIds[record.internalId] = componentId = Ext.id();
        }

        var rowBody = this.rowBodyTpl.applyTemplate({'component-id' : componentId});

        o.rowBody = rowBody;
        if(parseInt(this.rowsExpanded[record.internalId])>=0){
            //VT: if the number of layers and position have changed, update the rowsExpanded list
            if(parseInt(this.rowsExpanded[record.internalId]) != idx ){
                this.rowsExpanded[record.internalId] = idx;
            }
            o.rowCls =  '' ;
            o.rowBodyCls ='';
        }else{
            o.rowCls = this.rowCollapsedCls;
            o.rowBodyCls = this.rowBodyHiddenCls
        }
               
        o[id + '-tdAttr'] = ' valign="top" rowspan="2" ';
        if (orig[id+'-tdAttr']) {
            o[id+'-tdAttr'] += orig[id+'-tdAttr'];
        }
        return o;
    },

    _onContextMenuItemClick : function( view, td, cellIndex, record, tr, index, e, eOpts ) {
        this.toggleContainer(index);        
    }

  
});