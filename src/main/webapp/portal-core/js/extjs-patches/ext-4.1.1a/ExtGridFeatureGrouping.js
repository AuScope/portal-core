/**
 * http://www.sencha.com/forum/showthread.php?238728
 * This is a known issue which has not been fixed yet
 */
Ext.define('Auscope.grid.feature.Grouping', {
	override : 'Ext.grid.feature.Grouping',
	
	collectData: function(records, preppedRecords, startIndex, fullWidth, o) {
        var me    = this,
            store = me.view.store,
            collapsedState = me.collapsedState,
            collapseGroups,
            g,
            groups, gLen, group;

        if (me.startCollapsed) {
            
            //VT:If the store is empty means it is not loaded
            //VT:therefore, do not invert the flag yet.
            if(store.getGroups().length >0){
                me.startCollapsed = false;
            }
            collapseGroups = true;
        }

        if (!me.disabled && store.isGrouped()) {
            o.rows = groups = store.getGroups();
            gLen   = groups.length;

            for (g = 0; g < gLen; g++) {
                group = groups[g];
                
                if (collapseGroups) {
                    collapsedState[group.name] = true;
                }

                me.getGroupRows(group, records, preppedRecords, fullWidth);
            }
        }
        return o;
    }
	
});