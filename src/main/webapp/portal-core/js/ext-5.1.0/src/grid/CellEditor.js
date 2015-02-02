/**
 * Internal utility class that provides default configuration for cell editing.
 * @private
 */
Ext.define('Ext.grid.CellEditor', {
    extend: 'Ext.Editor',

    alignment: 'l-l?',

    hideEl : false,

    cls: Ext.baseCSSPrefix + 'small-editor ' +
        Ext.baseCSSPrefix + 'grid-editor ' +
        Ext.baseCSSPrefix + 'grid-cell-editor',

    treeNodeSelector: '.' + Ext.baseCSSPrefix + 'tree-node-text',

    shim: false,

    shadow: false,

    constructor: function(config) {
        var field;

        // Editor must appear at the top so that it does not contribute to scrollbars.
        this.y = 0;

        config = Ext.apply({}, config);
        field = config.field;

        if (field) {
            field.monitorTab = false;
        }

        this.callParent([config]);
    },

    // Set the grid that owns this editor.
    // Usually this will only *change* once, and the renderTo will cause
    // rendering into the owning grid.
    // However in a Lockable assembly the editor has to swap sides if the column is moved across.
    // Called by CellEditing#getEditor
    setGrid: function(grid) {
        var me = this,
            oldGrid = me.grid,
            view,
            viewListeners;

        if (grid !== oldGrid) {
            viewListeners = {
                beforerefresh: me.beforeViewRefresh,
                refresh: me.onViewRefresh,
                scope: me
            };
            // Remove previous refresh listener
            if (oldGrid) {
                oldGrid.getView().un(viewListeners);
            }

            // Set the renderTo target to reflect new grid view ownership
            view = grid.getView();
            me.renderTo = view.getTargetEl().dom;
            me.grid = grid;

            // On view refresh, we need to copy our DOM into the detached body to prevent it from being garbage collected.
            view.on(viewListeners);
        }
    },

    afterFirstLayout: function(width, height) {
        // After we've been laid out, we can get rid of the y property, we don't want
        // to be positioned now
        delete this.y;
        this.callParent([width, height]);
    },

    beforeViewRefresh: function() {
        var me = this,
            dom = me.el && me.el.dom;

        if (dom) {
            if (me.editing && !(me.field.column && me.field.column.sorting)) {

                // Clear the Panel's cellFocused flag prior to removing it from the DOM
                // This will prevent the Panels onFocusLeave from processing the resulting blurring.
                me.grid.view.cellFocused = false;

                // Set the Editor.allowBlur setting so that it does not process the upcoming field blur event and terminate the edit
                me.wasAllowBlur = me.allowBlur;
                me.allowBlur = false;
            }

            // Remove the editor from the view to protect it from anihilation: https://sencha.jira.com/browse/EXTJSIV-11713
            if (dom.parentNode) {
                dom.parentNode.removeChild(dom);
            }
        }
    },

    onViewRefresh: function() {
        var me = this,
            dom = me.el && me.el.dom,
            sorting;

        if (dom) {
            sorting = me.field.column && me.field.column.sorting;

            // If the view was refreshed while we were editing, replace it.
            if (me.editing && !sorting) {
                me.allowBlur = me.wasAllowBlur;
                me.renderTo.appendChild(dom);
                
                // The removal will have blurred, so avoid the processing in onFocusEnter by restoring the previous
                // cellFocused setting
                me.grid.view.cellFocused = true;

                me.field.focus();
            } else if (!sorting) {
                Ext.getDetachedBody().dom.appendChild(dom);
            }

            // If the column was sorted while editing, we must detect that and complete the edit
            // because the view will be refreshed and the editor will be removed from the dom.
            if (me.editing && sorting) {
                me.completeEdit();
            }
        }
    },

    startEdit: function(record, columnHeader) {
        this.context = this.editingPlugin.context;
        this.callParent([record, columnHeader]);
    },

    /**
     * @private
     * Shows the editor, end ensures that it is rendered into the correct view
     * Hides the grid cell inner element when a cell editor is shown.
     */
    onShow: function() {
        var me = this,
            innerCell = me.boundEl.first();

        // If we have had our owning grid changed (by a column switching sides in a Lockable assembly)
        // or, if a view refresh has removed us from the DOM
        // append this component into its renderTo target.
        if (me.el.dom.parentNode !== me.renderTo) {
            me.renderTo.appendChild(me.el.dom);
        }

        if (innerCell) {
            if (me.isForTree) {
                innerCell = innerCell.child(me.treeNodeSelector);
            }
            innerCell.hide();
        }

        me.callParent(arguments);
    },

    onEditComplete: function(remainVisible) {
        // When being asked to process edit completion, if we are not hiding, restore the cell now
        if (remainVisible) {
            this.restoreCell();
        }
        this.callParent(arguments);
    },

    /**
     * @private
     * Shows the grid cell inner element when a cell editor is hidden
     */
    onHide: function() {
        var me = this,
            context = me.context,
            focusContext;

        me.restoreCell();

        if (context && me.el.contains(Ext.Element.getActiveElement())) {
            focusContext = context.clone();

            // If, after hiding, focus has not been programatically moved to another target
            // (eg, we are in a TAB event listener, and move to edit the next cell)
            // then revert focus back to the corresponding grid cell.
            Ext.on({
                idle: function() {
                    if (Ext.Element.getActiveElement() === document.body) {
                        context.view.getNavigationModel().setPosition(focusContext, null, null, null, true);
                    }
                },
                single: true
            });
            me.context = null;
        }
        me.callParent(arguments);
    },

    restoreCell: function() {
        var me = this,
            innerCell = me.boundEl.first();

        if (innerCell) {
            if (me.isForTree) {
                innerCell = innerCell.child(me.treeNodeSelector);
            }
            innerCell.show();
        }        
    },

    /**
     * @private
     * Fix checkbox blur when it is clicked.
     */
    afterRender: function() {
        var me = this,
            field = me.field;

        me.callParent(arguments);

        if (field.isCheckbox) {
            field.mon(field.inputEl, {
                mousedown: me.onCheckBoxMouseDown,
                click: me.onCheckBoxClick,
                scope: me
            });
        }
    },
    
    /**
     * @private
     * Because when checkbox is clicked it loses focus  completeEdit is bypassed.
     */
    onCheckBoxMouseDown: function() {
        this.completeEdit = Ext.emptyFn;
    },
     
    /**
     * @private
     * Restore checkbox focus and completeEdit method.
     */
    onCheckBoxClick: function() {
        delete this.completeEdit;
        this.field.focus(false, 10);
    },
    
    /**
     * @private
     * Realigns the Editor to the grid cell, or to the text node in the grid inner cell
     * if the inner cell contains multiple child nodes.
     */
    realign: function(autoSize) {
        var me = this,
            boundEl = me.boundEl,
            innerCell = boundEl.first(),
            innerCellTextNode = innerCell.dom.firstChild,
            width = boundEl.getWidth(),
            offsets = Ext.Array.clone(me.offsets),
            grid = me.grid,
            xOffset,
            v = '',

            // innerCell is empty if there are no children, or there is one text node, and it contains whitespace
            isEmpty = !innerCellTextNode || (innerCellTextNode.nodeType === 3 && !(Ext.String.trim(v = innerCellTextNode.data).length));

        if (me.isForTree) {
            // When editing a tree, adjust the width and offsets of the editor to line
            // up with the tree cell's text element
            xOffset = me.getTreeNodeOffset(innerCell);
            width -= Math.abs(xOffset);
            offsets[0] += xOffset;
        }

        if (grid.columnLines) {
            // Subtract the column border width so that the editor displays inside the
            // borders. The column border could be either on the left or the right depending
            // on whether the grid is RTL - using the sum of both borders works in both modes.
            width -= boundEl.getBorderWidth('rl');
        }

        if (autoSize === true) {
            me.field.setWidth(width);
        }

        // https://sencha.jira.com/browse/EXTJSIV-10871 Ensure the data bearing element has a height from text.
        if (isEmpty) {
            innerCell.dom.innerHTML = 'X';
        }

        me.alignTo(innerCell, me.alignment, offsets);

        if (isEmpty) {
            innerCell.dom.firstChild.data = v;
        }
    },

    // private
    getTreeNodeOffset: function(innerCell) {
        return innerCell.child(this.treeNodeSelector).getOffsetsTo(innerCell)[0];
    },

    onEditorTab: function(e){
        var field = this.field;
        if (field.onEditorTab) {
            field.onEditorTab(e);
        }
    },

    onFocusLeave : function(e) {
        // We are going to hide because of this focus exit.
        // Ensure that hide processing does not throw focus back to the previously focused element.
        this.previousFocus = null;

        this.callParent([e]);

        // Reset the flag that may have been set by CellEditing#startEdit to prevent
        // Ext.Editor#onFieldBlur from canceling editing.
        this.selectSameEditor = false;
    }
});
