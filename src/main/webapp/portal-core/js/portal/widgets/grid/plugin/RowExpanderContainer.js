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
        grid.on('cellclick', this._onContextMenuCell, this);
    },

    /**
     * Close any open containers
     */
    closeAllContainers : function() {
        for (idx in this.rowsExpanded) {
            if (this.rowsExpanded[idx] === true) {
                this.hideContainer(parseInt(idx));
            }
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
        this.rowsExpanded[rowIdx] = false;
        this.recordComponents[rowIdx].destroy();
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
        this.rowsExpanded[rowIdx] = true;
        view.refreshSize();
        view.fireEvent('contextshow', rowNode, record, nextBd.dom);

        this.recordComponents[rowIdx] = this.generateContainer(record, this.recordComponentIds[rowIdx]);
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
            componentId = this.recordComponentIds[idx];

        if (!componentId) {
            this.recordComponentIds[idx] = componentId = Ext.id();
        }

        var rowBody = this.rowBodyTpl.applyTemplate({'component-id' : componentId});

        o.rowBody = rowBody;
        o.rowCls = this.rowsExpanded[idx] ? '' : this.rowCollapsedCls;
        o.rowBodyCls = this.rowsExpanded[idx] ? '' : this.rowBodyHiddenCls;
        o[id + '-tdAttr'] = ' valign="top" rowspan="2" ';
        if (orig[id+'-tdAttr']) {
            o[id+'-tdAttr'] += orig[id+'-tdAttr'];
        }
        return o;
    },

    _onContextMenuCell : function(view, td, cellIndex, record, tr, index, e, eOpts) {
        e.stopEvent();

        this.toggleContainer(index);
    }
});