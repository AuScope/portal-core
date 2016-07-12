/**
 * An extension to the ExtJS accordian layout. The original layout
 * would always flick back to an above/below panel when the current
 * panel is collapsed. This extension allows a "default" panel to
 * be reselected instead
 */
Ext.define('portal.widgets.layout.AccordianDefault', {
    extend : 'Ext.layout.container.Accordion',
    alias : 'layout.accordiondefault',
    type : 'accordiondefault',

    defaultQuery : null,

    /**
     * Adds the following config:
     * 
     * defaultId : String - itemId of the panel to always open when the current panel is collapsed 
     */
    constructor : function(config) {
        this.defaultQuery = '#' + config.defaultId;
        this.callParent(arguments);
    },
    
    /**
     * Stops all future animations running until resumeAnimations is called
     * 
     * This function is not re-entrant
     */
    suspendAnimations: function() {
        this.animate = false;
        this.originalAnimatePolicy = this.animatePolicy;
        this.animatePolicy = null;
    },
    
    /**
     * 
     * Resumes all future animations. 
     * 
     * This function is not re-entrant
     */
    resumeAnimations: function() {
        if (Ext.isObject(this.originalAnimatePolicy)) {
            this.animate = true;
            this.animatePolicy = this.originalAnimatePolicy;
            this.originalAnimatePolicy = null;
        }
    },

    /**
     * Override the default implementation with one that chooses the next expand
     * target using our "defaultQuery"
     * 
     * This was overridden from the Ext 5.1.1 implementation. Future Ext versions
     * may require this to be reworked
     */
    onBeforeComponentCollapse : function(comp) {
        var me = this, owner = me.owner, toExpand, expanded, previousValue;

        if (me.owner.items.getCount() === 1) {
            // do not allow collapse if there is only one item
            return false;
        }

        if (!me.processing) {
            me.processing = true;
            previousValue = owner.deferLayouts;
            owner.deferLayouts = true;
            //portal-core change
            toExpand = comp.previousSibling(me.defaultQuery)
                    || comp.nextSibling(me.defaultQuery);
            //end portal-core change

            // If we are allowing multi, and the "toCollapse" component is NOT
            // the only expanded Component,
            // then ask the box layout to collapse it to its header.
            if (me.multi) {
                expanded = me.getExpanded();

                // If the collapsing Panel is the only expanded one, expand the
                // following Component.
                // All this is handling fill: true, so there must be at least
                // one expanded,
                if (expanded.length === 1) {
                    toExpand.expand();
                }

            } else if (toExpand) {
                toExpand.expand();
            }
            owner.deferLayouts = previousValue;
            me.processing = false;
        }
    }
});
