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

    /**
     * @cfg {Boolean} expandOnEnter
     * `true` to toggle selected row(s) between expanded/collapsed when the enter
     * key is pressed (defaults to `true`).
     */
    expandOnEnter: true,

    /**
     * @cfg {Boolean} selectRowOnExpand
     * `true` to select a row when clicking on the expander icon
     * (defaults to `false`).
     */
    selectRowOnExpand: false,
    
    /**
     * @cfg {Boolean} [bodyBefore=false]
     * Configure as `true` to put the row expander body *before* the data row.
     * 
     */
    bodyBefore: false,

    rowBodyTrSelector: '.' + Ext.baseCSSPrefix + 'grid-rowbody-tr',
    rowBodyHiddenCls: Ext.baseCSSPrefix + 'grid-row-body-hidden',
    rowCollapsedCls: Ext.baseCSSPrefix + 'grid-row-collapsed',

    addCollapsedCls: {
        fn: function(out, values, parent) {
            var me = this.rowExpander;
            if (values.record.internalId in me.recordsExpanded) {
                values.itemClasses.push(me.rowCollapsedCls);
            }
            this.nextTpl.applyOut(values, out, parent);
        },

        // We need a high priority to get in ahead of the outerRowTpl
        // so we can setup row data
        priority: 20000
    },

    /**
     * @event expandbody
     * **Fired through the grid's View**
     * @param {HTMLElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HTMLElement} expandRow The &lt;tr> element containing the expanded data.
     */
    /**
     * @event collapsebody
     * **Fired through the grid's View.**
     * @param {HTMLElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HTMLElement} expandRow The &lt;tr> element containing the expanded data.
     */

    setCmp: function(grid) {
        var me = this,
            features;

        me.callParent(arguments);

        me.recordsExpanded = {};
        me.recordComponentIds = {};
        me.recordComponents = {};
        me.rowBodyTpl = Ext.create('Ext.XTemplate', '<div id="{component-id}"></div>');
        features = me.getFeatureConfig(grid);

        if (grid.features) {
            grid.features = Ext.Array.push(features, grid.features);
        } else {
            grid.features = features;
        }
        // NOTE: features have to be added before init (before Table.initComponent)
    },

    /**
     * @protected
     * @return {Array} And array of Features or Feature config objects.
     * Returns the array of Feature configurations needed to make the RowExpander work.
     * May be overridden in a subclass to modify the returned array.
     */
    getFeatureConfig: function(grid) {
        var me = this,
            features = [],
            featuresCfg = {
                ftype: 'rowbody',
                rowExpander: me,
                bodyBefore: me.bodyBefore,
                recordsExpanded: me.recordsExpanded,
                rowBodyHiddenCls: me.rowBodyHiddenCls,
                rowCollapsedCls: me.rowCollapsedCls,
                setupRowData: me.getRowBodyFeatureData,
                setup: me.setup
            };

        features.push(Ext.apply({
            lockableScope: 'normal',
            getRowBodyContents: me.getRowBodyContentsFn(me.rowBodyTpl)
        }, featuresCfg));

        return features;
    },
    
    getRowBodyContentsFn: function(rowBodyTpl) {
        var me = this;
        return function (rowValues) {
            var componentId = me.recordComponentIds[rowValues.record.internalId];
            if (!componentId) {
                me.recordComponentIds[rowValues.record.internalId] = componentId = Ext.id();
            }
            
            rowBodyTpl.owner = me;
            var r = rowBodyTpl.applyTemplate({'component-id' : componentId});
            return r;
        };
    },

    init: function(grid) {
        
        var me = this,
            view;

        me.callParent(arguments);
        me.grid = grid;
        view = me.view = grid.getView();

        // Bind to view for key and mouse events
        // Add row processor which adds collapsed class
        view.addRowTpl(me.addCollapsedCls).rowExpander = me;
        
        grid.on('beforereconfigure', me.beforeReconfigure, me);
        grid.on('cellclick', this._onContextMenuItemClick, this);    
        grid.on('resize', this._handleResize, this);
    },

    beforeReconfigure: function(grid, store, columns, oldStore, oldColumns) {
        var me = this;

        if (me.viewListeners) {
            me.viewListeners.destroy();    
        }
    },

    getRowBodyFeatureData: function(record, idx, rowValues) {
        var me = this;
        me.self.prototype.setupRowData.apply(me, arguments);
        rowValues.rowBody = me.getRowBodyContents(rowValues);
        rowValues.rowBodyCls = (record.internalId in me.recordsExpanded) ? '' : me.rowBodyHiddenCls;
    },

    setup: function(rows, rowValues){
        var me = this;
        me.self.prototype.setup.apply(me, arguments);
    },

    toggleRow: function(rowIdx, record) {
        var me = this,
            view = me.view,
            bufferedRenderer = view.bufferedRenderer,
            scroller = view.getScrollable(),
            fireView = view,
            rowNode = view.getNode(rowIdx),
            normalRow = Ext.fly(rowNode),
            nextBd = normalRow.down(me.rowBodyTrSelector, true),
            wasCollapsed = normalRow.hasCls(me.rowCollapsedCls),
            addOrRemoveCls = wasCollapsed ? 'removeCls' : 'addCls',

            // The expander column should be rowSpan="2" only when the expander is expanded
            rowSpan = wasCollapsed ? 2 : 1,
            expanderCell;
        
        normalRow[addOrRemoveCls](me.rowCollapsedCls);
        Ext.fly(nextBd)[addOrRemoveCls](me.rowBodyHiddenCls);
        me.recordsExpanded[record.internalId] = rowIdx;

        fireView.fireEvent(wasCollapsed ? 'expandbody' : 'collapsebody', rowNode, record, nextBd);

        if (wasCollapsed) {
            this.showContainer(rowIdx);
        } else {
            this.hideContainer(rowIdx);
        }
        
        // Layout needed of we are shrinkwrapping height, or there are locked/unlocked sides to sync
        // Will sync the expander row heights between locked and normal sides
        if (view.getSizeModel().height.shrinkWrap) {
            view.refreshSize(true);
        }
        // If we are using the touch scroller, ensure that the scroller knows about
        // the correct scrollable range
        if (scroller) {
            if (bufferedRenderer) {
                bufferedRenderer.refreshSize();
            } else {
                scroller.refresh(true);
            }
        }    
    },
    
    _handleResize : function(){
        for (id in this.recordsExpanded) {
            this.recordComponents[id].doComponentLayout();
        }    
    },
    

    /**
     * Close any open containers
     */
    closeAllContainers : function() {
        for (id in this.recordsExpanded) {
            var i = parseInt(this.recordsExpanded[id]);            
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
        delete this.recordsExpanded[record.internalId];
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
        view.refreshSize();
        view.fireEvent('contextshow', rowNode, record, nextBd.dom);
        
        if(!this.recordComponents[record.internalId]) {
            this.recordComponents[record.internalId] = this.generateContainer(record, this.recordComponentIds[record.internalId]);
        } else {
            //VT:Check that the dom still exist as grid:refresh can wipe the panel clean.
            var parentEl = Ext.get(this.recordComponentIds[record.internalId]);
            if(parentEl.dom.firstChild == null && parentEl) {             
                this.recordComponents[record.internalId].render(parentEl.dom);               
            }
        }
        this._handleResize();
    },

    _onContextMenuItemClick : function( view, td, cellIndex, record, tr, index, e, eOpts ) {       
        this.toggleRow(index, record);    
    }

  
});