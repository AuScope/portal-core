/**
 * Abstract child class that children of a RecordPanel should extend.
 * 
 * Provides basic functionality for expanding/collapsing this and any potential
 * child classes of this class.
 */
Ext.define('portal.widgets.panel.recordpanel.AbstractChild', {
    extend: 'Ext.panel.Panel',

    config: {
        /**
         * This will need to be set consistently across all RecordPanelAbstractChild
         * for a given "generation"
         * 
         * If true - this abstract child will act as a top level group which means:
         *              Any sibling expanding/collapsing will not affect this panel's expansion
         *              Any child expanding in a sibling will collapse any children of this child
         *              
         * If false - this abstract child will not affect it's siblings expand/collapse state if its child expands
         *              
         */
        groupMode: false
    },
    
    onChildExpand : function(thisPanel, rowPanel) {
        if (thisPanel.getGroupMode()) {
            thisPanel.ownerCt.suspendLayouts();
            thisPanel.ownerCt.items.each(function(sibling) {
                if (sibling instanceof portal.widgets.panel.recordpanel.AbstractChild && sibling.getId() !== thisPanel.getId()) {
                    sibling.collapseChildren();
                }
            });
            thisPanel.ownerCt.resumeLayouts();
        }
    },

    /**
     * Collapses all children panels of this group
     */
    collapseChildren : function() {
        this.items.each(function(item) {
            if (item instanceof portal.widgets.panel.recordpanel.AbstractChild) {
                if (item.getCollapsed() === false) {
                    item.collapse();
                }
            }
        }, this);
    },

    initComponent : function() {
        this.callParent(arguments);

        this.items.each(function(item) {
            if (item instanceof portal.widgets.panel.recordpanel.AbstractChild) {
                item.on('beforeexpand', function(item) {
                    this.fireEvent('childexpand', this, item);
                }, this);
            }
        }, this);

        this.on({
            childexpand : this.onChildExpand,
            scope : this
        });
    }
});
