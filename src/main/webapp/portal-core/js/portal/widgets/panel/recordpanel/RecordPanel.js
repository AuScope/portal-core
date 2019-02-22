/**
 * Ext.panel.Panel extension to roughly reproduce an Ext.grid.Panel for displaying
 * grouped layer records with custom panels on each layer's expander 
 * 
 * The old grid panel was deprecated as part of AUS-2685
 */
Ext.define('portal.widgets.panel.recordpanel.RecordPanel', {
    extend : 'Ext.panel.Panel',
    xtype : 'recordpanel',

    statics: {
        LAZY_LOAD_ID: 'lazy-load'
    },
    
    config: {
        allowReordering: false,
        store: null,
        titleField: 'name',
        titleIndex: 0,
        tools: null,
        childPanelGenerator: Ext.emptyFn,
        lazyLoadChildPanel: false
    },


    toolFieldMap: null, //A map of tool config objects keyed by field name
    recordRowMap: null, //A map of RecordRowPanel itemId's keyed by their recordId
    recordGroupMap: null, //A map of RecordGroupPanel itemId's keyed by their group key (only valid when store is in group mode)

    /**
     * Extends Ext.panel.Panel and adds the following:
     * {
     *  allowReordering: Boolean - If true, the records will be able to be reordered by dragging and dropping. Currently only supported with non grouped stores.
     *  emptyText: String - HTML string that will be shown when the contents of this panel are empty.
     *  store: Ext.data.Store - Contains the layer elements
     *  titleField: String - The field in store's underlying data model that will populate the title of each record
     *  titleIndex: Number - The 0 based index of where the title field will fit in amongst tools (default - 0)
     *  tools: Object[] - The additional tool columns, each bound to fields in the underlying data model
     *             field - Array/String - The field name in the model to bind this tool icon to. 
     *                                    Can be an array of field names in which case changes to any field in this array will trigger an update of the renderer/tip
     *                                    All callback function values will be the field value for the first element in the array (the primary field)
     *             stopEvent: Boolean - If true, click events will not propogate upwards from this tool.
     *             clickHandler - function(value, record) - Called whenever this tool is clicked. No return value.
     *             doubleClickHandler - function(value, record) - Called whenever this tool is clicked. No return value.
     *             tipRenderer - function(value, record, tip) - Called whenever a tooltip is generated. Return HTML content to display in tip
     *             iconRenderer - function(value, record) - Called whenever the underlying field updates. Return String URL to icon that will be displayed in tip.
     *  childPanelGenerator: function(record) - Called when records are added to the store. Return a generated Ext.Container for display in the specified row's expander
     *  lazyLoadChildPanel: Boolean - If true. the childPanelGenerator won't be fired until the row is expanded (default - false)
     * }
     * 
     * And the following events:
     * reorder(recordPanel, record, newIndex, oldIndex) - Fired when the store is reordered (by user interaction)
     */
    constructor : function(config) {
        var grouped = config.store.isGrouped();

        this.recordRowMap = {};
        this.recordGroupMap = {};

        //Ensure we setup the correct layout
        Ext.apply(config, {
            layout: {
                type: 'accordion',
                hideCollapseTool: !grouped,
                collapseFirst: true,
                fill: false,
                multi: grouped
            },
            items: [{
                xtype: 'panel',
                itemId: 'emptytext',
                collapsed: false,
                header: {
                    hidden: true
                },
                html: config.emptyText
            }],
            autoScroll: true,
            plugins: ['collapsedaccordian']
        });

        this.callParent(arguments);

        this._generateToolFieldMap();

        this.store.on({
            update: this.onStoreUpdate,
            load: this.onStoreLoad,
            clear: this.onStoreLoad, //The load handler will perform a full clear for us
            beforeload: this.onStoreBeforeLoad,
            filterchange: this.onStoreFilterChange,
            add: this.onStoreAdd,
            remove: this.onStoreRemove,
            scope: this
        });

        //Setup our Drag Drop zones when the component is rendered.
        if (this.rendered) {
            this._initDDZones();
        } else {
            this.on({
                afterrender: {
                    fn: this._initDDZones,
                    scope: this,
                    options: {
                        single: true
                    }
                }
            });
        }
        
        this.on({
            show: this.onComponentShow,
            scope: this
        });

        //If our store is already loaded - fill panel with existing contents
        if (this.store.getCount()) {
            var existingRecords = null;
            if (this.store.isFiltered()) {
                existingRecords = this.store.getData().getSource().getRange();
            } else {
                existingRecords = this.store.getData().getRange();
            }
            this.onStoreLoad(this.store, existingRecords, true);
        }
    },

    _getPrimaryField: function(toolCfg) {
        if (Ext.isArray(toolCfg.field)) {
            return toolCfg.field[0];
        } else {
            return toolCfg.field;
        }
    },
    
    /**
     * Register drag/drop zones (if applicable)
     */
    _initDDZones: function() {
        var me = this;
        if (me.allowReordering && !me.store.isGrouped()) {
            var dragEl = this.getEl();
            me.ddGroup = Ext.id(undefined, 'recordpanel-dd-');
            me.dragZone = new Ext.dd.DragZone(dragEl, {
                ddGroup: this.ddGroup,
                getDragData: function(e) {
                    var el = Ext.fly(e.target);
                    if (!el.hasCls('recordrowpanel')) {
                        el = el.up('.recordrowpanel');
                    }

                    if (el) {
                        var xy = el.getXY();

                        //Back reference our component from the DOM and then use that to lookup our record
                        var rowPanel = Ext.getCmp(el.dom.id);
                        var record = me.store.getById(rowPanel.recordId);

                        //Only show the header in the drag
                        el = el.down('.x-panel-header');
                        var sourceEl = el.dom;
                        var d = sourceEl.cloneNode(true);
                        d.id = Ext.id();

                        return {
                            ddel: d,
                            sourceEl: sourceEl,
                            repairXY: xy,
                            source: me,
                            draggedRecord: record
                        }
                    }
                },

                // Provide coordinates for the proxy to slide back to on failed drag.
                // This is the original XY coordinates of the draggable element captured
                // in the getDragData method.
                getRepairXY: function() {
                    return this.dragData.repairXY;
                }
            });


            this.dropTarget = new Ext.dd.DropTarget(dragEl, {
                ddGroup : me.ddGroup,
                notifyDrop: function(ddSource, e, data) {
                    var dropSuccess = false;
                    if (Ext.isNumber(me.lastDDInsertionIdx)) {
                        var rec = data.draggedRecord;
                        var oldIdx = me.store.indexOf(rec);
                        var newIdx = me.lastDDInsertionIdx;
                        
                        if (oldIdx < newIdx) {
                            newIdx--;
                        }
                        
                        if (oldIdx !== newIdx) {
                            me.store.remove(rec, true);
                            me.store.insert(newIdx, rec);
                            me.fireEvent('reorder', me, rec, newIdx, oldIdx);
                        }
                        dropSuccess = true;
                    }
                    
                    me._clearDropTarget();
                    return dropSuccess;
                },
                notifyOver: function(ddSource, e, data) {
                    //To generate our highlight we need to iterate all row panels and look
                    //for the row under our mouse (not ideal - but should be quick)
                    var pageX = e.getX();
                    var pageY = e.getY();
                    var recordPanelEls = data.source.getEl().query('.recordrowpanel', false);
                    // Returns 0 if no intercept. Returns -1 if in top half of box, Returns 1 if in bottom half
                    var boxIntercept = function(x, y, box) {
                        if (x < box.x || x >= (box.x + box.width) ||
                            y < box.y || y >= (box.y + box.height)) {
                            return 0;
                        } else if (y < (box.y + box.height / 2)) {
                            return -1;
                        } else {
                            return 1;
                        }
                    };
                    
                    var isHighlightMade = false;
                    me._clearDropTarget();
                    Ext.each(recordPanelEls, function(el, idx) {
                        var box = el.getBox();
                        var intercept = 0;
                        if (intercept = boxIntercept(pageX, pageY, box)) {
                            if (intercept < 0) {
                                el.addCls('recordpanel-insertabove');
                            } else {
                                el.addCls('recordpanel-insertbelow');
                            }
                        
                            isHighlightMade = true;
                            me.lastDDHighlight = el;
                            me.lastDDInsertionIdx = intercept < 0 ? idx : (idx + 1);
                            return false;
                        }
                    });
                    
                    return isHighlightMade ? Ext.dd.DropZone.prototype.dropAllowed : false;
                },
                notifyOut: function(ddSource, e, data) {
                    me._clearDropTarget();
                }
            });
        }
    },
    
    /**
     * If there is a drag drop highlight set, this function will clear the
     * visual highlight effect and associated element references.
     */
    _clearDropTarget: function() {
        if (this.lastDDHighlight) {
            this.lastDDHighlight.removeCls('recordpanel-insertabove');
            this.lastDDHighlight.removeCls('recordpanel-insertbelow');
            this.lastDDHighlight = null;
            this.lastDDInsertionIdx = null;
        }
    },

    /**
     * Populates toolFieldMap with the contents of the current tool config
     */
    _generateToolFieldMap: function() {
        this.toolFieldMap = {};

        Ext.each(this.tools, function(toolCfg) {
            toolCfg.toolId = Ext.id(null, 'recordpanel-tool-'); //assign each tool a unique ID

            var fields = toolCfg.field;
            if (!Ext.isArray(fields)) {
                fields = [fields];
            }

            Ext.each(fields, function(field) {
                if (Ext.isEmpty(this.toolFieldMap[field])) {
                    this.toolFieldMap[field] = [toolCfg];
                } else {
                    this.toolFieldMap[field].push(toolCfg);
                }
            }, this);
        }, this);
    },

    /**
     * Enumerates each RecordGroupPanel and passes them one by one to callback
     */
    _eachGroup: function(callback, scope) {
        this.items.each(function(recordGroupPanel) {
            if (recordGroupPanel instanceof portal.widgets.panel.recordpanel.GroupPanel) { 
                callback.call(scope, recordGroupPanel);
            }
        });
    },

    /**
     * Enumerates each RecordRowPanel and passes them one by one to callback
     */
    _eachRow: function(callback, scope) {
        //If we are grouped, our rows are children of groups. Otherwise they can be found at the top level
        if (this.store.isGrouped()) {
            this._eachGroup(function(recordGroupPanel) {
                recordGroupPanel.items.each(function(recordRowPanel) {
                    if (recordRowPanel instanceof portal.widgets.panel.recordpanel.RowPanel) {
                        callback.call(scope, recordRowPanel);
                    }
                });
            });
        } else {
            this.items.each(function(recordRowPanel) {
                if (recordRowPanel instanceof portal.widgets.panel.recordpanel.RowPanel) { 
                    callback.call(scope, recordRowPanel);
                }
            });
        }
    },

    /**
     * Simple wrapper around a tool click event that extracts the current
     * record field value and passes it to the delegate
     */
    _clickMarshaller: function(record, fieldName, handler) {
        var value = record.get(fieldName);
        handler.call(this, value, record);
    },

    /**
     * Installs all tooltips for the specified recordRowPanel. Ensure this is only
     * called once per recordRowPanel or tooltips will leak.
     */
    _installToolTips: function(recordRowPanel) {
        var record = this.store.getById(recordRowPanel.recordId);
        recordRowPanel.tipMap = {};

        //Install a unique tooltip for each tool
        Ext.each(this.tools, function(tool) {
            var primaryField = this._getPrimaryField(tool);

            recordRowPanel.tipMap[tool.toolId] = Ext.create('Ext.tip.ToolTip', {
                target: recordRowPanel.getHeader().down('#' + tool.toolId).getEl(),
                trackMouse: true,
                listeners: {
                    beforeshow: function(tip) {
                        var content = tool.tipRenderer(record.get(primaryField), record, tip);
                        tip.update(content);
                    }
                }
            });
        }, this);

        //Ensure we destroy the tips if we remove this panel
        recordRowPanel.on('destroy', function(recordRowPanel) {
            for (var toolId in recordRowPanel.tipMap) {
                recordRowPanel.tipMap[toolId].destroy();
            }
            recordRowPanel.tipMap = {};
        });
    },

    /**
     * Generates a RecordRowPanel config object for a given record (also registers it internally so ensure this config gets added to the widget)
     */
    _generateRecordRowConfig: function(record, groupMode) {
        var tools = [];
        Ext.each(this.tools, function(tool) {
            var field = this._getPrimaryField(tool);
            var fieldValue = record.get(field);
            var clickBind = Ext.isEmpty(tool.clickHandler) ? null : Ext.bind(this._clickMarshaller, this, [record, field, tool.clickHandler], false);
            var doubleClickBind = Ext.isEmpty(tool.doubleClickHandler) ? null : Ext.bind(this._clickMarshaller, this, [record, field, tool.doubleClickHandler], false);
            tools.push({
                itemId: tool.toolId,
                stopEvent: tool.stopEvent,
                clickHandler: clickBind,
                doubleClickHandler: doubleClickBind,
                icon: tool.iconRenderer(fieldValue, record)
            });
        }, this);

        var recordId = record.getId();
        var newItemId = Ext.id(null, 'record-row-');
        this.recordRowMap[recordId] = newItemId; 
        
        
        var childPanel = null;
        if (this.lazyLoadChildPanel) {
            childPanel = {xtype: 'container', itemId: portal.widgets.panel.recordpanel.RecordPanel.LAZY_LOAD_ID};
        } else {
            childPanel = this.childPanelGenerator ? this.childPanelGenerator(record) : null;
        }
        
        return {
            xtype: 'recordrowpanel',
            recordId: recordId,
            itemId: newItemId,
            title: record.get(this.titleField), 
            titleIndex: this.titleIndex,
            groupMode: groupMode,
            tools: tools,
            record: record,
            items: childPanel ? [childPanel] : null,
            listeners: {
                scope: this,
                afterrender: this._installToolTips,
                beforeexpand: function(rp) {
                    if (!this.lazyLoadChildPanel) {
                        return;
                    }
                    
                    var placeHolder = rp.down('#' + portal.widgets.panel.recordpanel.RecordPanel.LAZY_LOAD_ID);
                    if (!placeHolder) {
                        return;
                    }
                    
                    rp.remove(placeHolder);
                    if (this.childPanelGenerator) {
                        rp.add(this.childPanelGenerator(rp.record));
                    }
                }
            }
        };
    },

    /**
     * Generates all widgets for a grouped data store
     * 
     * @param recordSelection Record[] ONLY records in this list will be considered for widget/group generation
     */
    _generateGrouped: function(recordSelection) {
        //Run through our groups of records, creating new 
        //items as we go 
        var newItems = [];
        var groups = this.store.getGroups();
        groups.each(function(groupObj) {
            var rows = [];

            //Create a RecordRowPanel for each row we receive
            Ext.each(groupObj.items, function(record) {
                var add = recordSelection === null || recordSelection === undefined;
                if (!add) {
                    Ext.each(recordSelection, function(r) {
                        if (r.getId() === record.getId()) {
                            add = true;
                            return false;
                        }
                    });
                }
                
                if (add) {
                    rows.push(this._generateRecordRowConfig(record, false));
                }
            }, this);

            //Shortcut out if we've filtered this grouping down to nothing
            if (Ext.isEmpty(rows)) {
                return;
            }
            
            //Next step is to figure out if our group already exists or whether we need
            //to create a new group
            var groupKey = groupObj.getConfig().groupKey;
            if (Ext.isEmpty(this.recordGroupMap[groupKey])) {
                //Create group
                var newGroupId = Ext.id(null, 'record-group-');
                this.recordGroupMap[groupKey] = newGroupId; 
                var newGroup = {
                    xtype: 'recordgrouppanel',
                    title: groupKey,
                    itemId: newGroupId,
                    items: rows,
                    groupKey: groupKey
                };
                newItems.push(newGroup);
            } else {
                //Add in situ
                var groupId = this.recordGroupMap[groupKey];
                var groupCmp = this.queryById(groupId);
                groupCmp.add(rows);
                groupCmp.refreshTitleCount();
            }
        }, this);

        this.add(newItems);
    },

    /**
     * Generates all widgets for an un-grouped data store
     * 
     * @param records Record[] if set, only these records will be used to generate widgets. 
     *                         Otherwise the entire store is used
     * @param insertionIndex Where the records should be inserted (if undefined, just append)           
     */
    _generateUnGrouped: function(records, insertionIndex) {
        var rows = [];
        var iterFn = function(record) {
            rows.push(this._generateRecordRowConfig(record, true));
        };
        
        if (records === null || records === undefined) {
            this.store.each(iterFn, this);
        } else {
            Ext.each(records, iterFn, this);
        }
        
        if (Ext.isNumber(insertionIndex)) {
            this.insert(insertionIndex, rows);
        } else {
            this.add(rows);
        }
    },
    
    /**
     * Updates the empty text hidden/visible status depending on whether any records are showing. Will
     * ALWAYS show empty text if this component is hidden.
     */
    _updateEmptyText: function() {
        var visibleItems = 0;
        
        if (this.store.isGrouped()) {
            this._eachGroup(function(recordGroupPanel) {
                if (recordGroupPanel.isVisible()) {
                    visibleItems++;
                }
            });
        } else {
            this._eachRow(function(recordRowPanel) {
                if (recordRowPanel.isVisible()) {
                    visibleItems++;
                }
            });
        }
        
        this.down('#emptytext').setHidden(visibleItems !== 0);
    },

    onComponentShow: function(recordPanel) {
        //Update our empty text visibility if the component is shown. Empty text visibility
        //does NOT properly work when the component is hidden hence we are forced to check this
        //every time the component is returned from hiding - See AUS-2811
        this._updateEmptyText();
    },
    
    /**
     * Handle updating renderers/tips for the modified fields
     */
    onStoreUpdate: function(store, record, operation, modifiedFieldNames, details) {
        if (!this.items.getCount()) {
            return;
        }

        //Figure out what fields we actually need to update
        var toolsToUpdate = {}; //Tools that require updates keyed by toolId 
        var anyToolsToUpdate = false;
        Ext.each(modifiedFieldNames, function(modifiedField) {
            if (!Ext.isEmpty(this.toolFieldMap[modifiedField])) {
                Ext.each(this.toolFieldMap[modifiedField], function(toolCfg) {
                    toolsToUpdate[toolCfg.toolId] = toolCfg;
                    anyToolsToUpdate = true;
                }, this);
            }
        }, this);
        if (!anyToolsToUpdate) {
            return;
        }

        //Update the modifiedFields
        var itemId = this.recordRowMap[record.getId()];
        var recordRowPanel = this.down('#' + itemId);
        if (!recordRowPanel) {
            return;
        }

        for (var toolId in toolsToUpdate) {
            var tool = toolsToUpdate[toolId];
            var primaryField = this._getPrimaryField(tool);
            var img = recordRowPanel.down('#' + toolId);
            var newSrc = tool.iconRenderer(record.get(primaryField), record);
            img.setSrc(newSrc);
        }
    },

    /**
     * When the store starts loading, begin prepping the panel for displaying
     * records
     */
    onStoreBeforeLoad: function(store, operation) {
        if (!this.rendered) {
            return;
        }

        if (!this.loadMask) {
            this.loadMask = new Ext.LoadMask({
                msg: 'Loading...',
                target: this
            });
        } 

        this.loadMask.show();
    },

    /**
     * When a filter changes, we need to enumerate each record row to see if it's currently in the filtered store (or not)
     * and shift its visibility accordingly
     */
    onStoreFilterChange: function(store, filters) {
        this.getLayout().suspendAnimations();
        Ext.suspendLayouts();

        this._eachRow(function(recordRowPanel) {
            var filtered = store.find('id', recordRowPanel.recordId) < 0; //we cant use store.getById as that bypasses any filters
            recordRowPanel.setHidden(filtered);
        }, this);

        this._eachGroup(function(recordGroupPanel) {
            recordGroupPanel.refreshTitleCount();
            if (recordGroupPanel.visibleItemCount) {
                recordGroupPanel.setHidden(false);
            } else {
                recordGroupPanel.setHidden(true);
            }
        });

        Ext.resumeLayouts();
        this.getLayout().resumeAnimations();
        
        this._updateEmptyText();
    },

    /**
     * When we receive a new set of records, update all items in the display from the
     * specified store. Any existing displaying records will be cleared first.
     */
    onStoreLoad: function(store) {
        if (this.loadMask) {
            this.loadMask.hide();
        }

        //Clear out the panel first (dont use removeAll otherwise we'll remove
        //our #collapsedtarget hidden items from CollapsedAccordianLayout
        for (var i = this.items.getCount() - 1; i >= 0; i--) {
            var item = this.items.getAt(i);
            if (item instanceof portal.widgets.panel.recordpanel.AbstractChild) {
                this.remove(item);
            }
        }
        this.recordRowMap = {};
        this.recordGroupMap = {};

        if (store.isGrouped()) {
            this._generateGrouped();
        } else {
            this._generateUnGrouped();
        }
        
        this._updateEmptyText();
    },

    /**
     * Fired when we get records to add that don't
     * require the entire store to be reloaded. In this case,
     * create groups (if required) and generate items.
     */
    onStoreAdd: function(store, records, index) {
        this.getLayout().suspendAnimations();
        Ext.suspendLayouts();
        
        if (store.isGrouped()) {
            this._generateGrouped(records);
        } else {
            //We insert at row + 1 to account for collapsed accordian item at position 0
            this._generateUnGrouped(records, index + 1);
        }
        
        Ext.resumeLayouts();
        this.getLayout().resumeAnimations();
        this.doLayout();
        
        this._updateEmptyText();
    },

    /**
     * Fired when we get small subsets of records to remove. In this case
     * we just delete the individual widgets (clearing up groups as required)
     */
    onStoreRemove: function(store, records, index, isMove) {
        this.getLayout().suspendAnimations();
        Ext.suspendLayouts();
        
        Ext.each(records, function(record) {
            var recordId = record.getId();
            var rowId = this.recordRowMap[recordId];
            var rowCmp = this.queryById(rowId);
            
            if (store.isGrouped()) {
                var groupCmp = rowCmp.ownerCt;
                var groupId = groupCmp.getItemId();
                
                delete this.recordRowMap[recordId];
                groupCmp.remove(rowCmp);
                if (groupCmp.items.getCount() <= 1) {
                    delete this.recordGroupMap[groupCmp.groupKey];
                    this.remove(groupCmp);
                } else {
                    groupCmp.refreshTitleCount();
                }
            } else {
                delete this.recordRowMap[recordId];
                this.remove(rowCmp);
            }
        }, this);
        
        Ext.resumeLayouts();
        this.getLayout().resumeAnimations();
        this.doLayout();
        
        this._updateEmptyText();
    },

    /**
     * Expands the row with the specified recordId. If that ID DNE, this has no effect.
     * 
     * The group containing the row will also be expanded.
     */
    expandRecordById: function(recordId) {
        this._eachRow(function(row) {
            if (row.recordId === recordId) {
                if (this.store.isGrouped()) {
                    row.ownerCt.expand();
                }
                row.expand();
            }
        });
    }
});
