/**
 * Ext.panel.Panel extensions to emulate the display of a grid panel group for a CommonBaseRecordPanel widget.
 * 
 * It will be used to grouping instances of portal.widgets.panel.RecordRowPanel 
 * 
 * The grid panel was deprecated as part of AUS-2685
 */
Ext.define('portal.widgets.panel.recordpanel.GroupPanel', {
    extend : 'portal.widgets.panel.recordpanel.AbstractChild',
    xtype : 'recordgrouppanel',

    /**
     * 
     */
    constructor : function(config) {
        var plugins = Ext.isEmpty(config.plugins) ? [] : config.plugins;
        plugins.push('collapsedaccordian');
        
        Ext.apply(config, {
            collapsed: true,
            groupMode: true,
            bodyPadding: '0 0 0 0',
            header: {
                style: 'cursor: pointer;',
                padding: '8 4 8 4'
            },
            layout: {
                type: 'accordion',
                hideCollapseTool: true,
                fill: false
            },
            plugins: plugins
        });
        this.callParent(arguments);
    },
    
    /**
     * Recalculates the visible item count for this group
     */
    refreshTitleCount: function() {
        this.setTitle(this.rawTitle);
    },
    
    /**
     * Overrides the normal implementation by adding a visible item count 
     */
    setTitle: function(title) {
        var visibleItemCount = 0;
        
        //We have to adjust our item count algorithm based on whether
        //we are dealing with a constructed widget or a config object
        if (this.items instanceof Ext.util.AbstractMixedCollection) {
            this.items.each(function(cmp) {
                if (!cmp.isHidden()) {
                    visibleItemCount++;
                }
            });
        } else {
            Ext.each(this.items, function(cmp) {
                if (Ext.isBoolean(cmp.hidden)) {
                    if (cmp.hidden) {
                        return;
                    }
                } else if (Ext.isBoolean(cmp.visible)) {
                    if (!cmp.visible) {
                        return;
                    }
                }
                visibleItemCount++;
            });
        }
        
        this.rawTitle = title;
        this.visibleItemCount = visibleItemCount;
        title = Ext.util.Format.format('{0} ({1} item{2})', title, visibleItemCount, (visibleItemCount != 1 ? 's' : ''));
        return this.callParent([title]);
    }
});