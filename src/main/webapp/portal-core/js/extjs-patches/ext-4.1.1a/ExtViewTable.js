/**
 * ExtViewTable.js
 * Jira Issue: AUS-2548:Streaming the portal UI
 * Pull request: https://github.com/AuScope/AuScope-Portal/pull/7
 * Reason: Ext.view.Table did not probably copy over the cls when worked in conjunction with grid and columns.
 */
Ext.define('Ext.view.AuScopeTable', {
	override : 'Ext.view.Table',
    
    onUpdate : function(store, record, operation, changedFieldNames) {
        var me = this,
            index,
            newRow, newAttrs, attLen, i, attName, oldRow, oldRowDom,
            oldCells, newCells, len, i,
            columns, overItemCls,
            isHovered, row,
            
            isEditing = me.editingPlugin && me.editingPlugin.editing;

        if (me.viewReady) {

            index = me.store.indexOf(record);
            columns = me.headerCt.getGridColumns();
            overItemCls = me.overItemCls;
            focusedItemCls = me.focusedItemCls;
            beforeFocusedItemCls = me.beforeFocusedItemCls;
            selectedItemCls = me.selectedItemCls;
            beforeSelectedItemCls = me.beforeSelectedItemCls;
            
            
            
            
            
            if (columns.length && index > -1) {
                newRow = me.bufferRender([record], index)[0];
                oldRow = me.all.item(index);
                if (oldRow) {
                    oldRowDom = oldRow.dom;
                    isHovered = oldRow.hasCls(overItemCls);
                    
                    if (oldRow.hasCls(overItemCls)) {
                        Ext.fly(newRow).addCls(overItemCls);
                    }
                    if (oldRow.hasCls(focusedItemCls)) {
                        Ext.fly(newRow).addCls(focusedItemCls);
                    }
                    if (oldRow.hasCls(beforeFocusedItemCls)) {
                        Ext.fly(newRow).addCls(beforeFocusedItemCls);
                    }
                    if (oldRow.hasCls(selectedItemCls)) {
                        Ext.fly(newRow).addCls(selectedItemCls);
                    }
                    if (oldRow.hasCls(beforeSelectedItemCls)) {
                        Ext.fly(newRow).addCls(beforeSelectedItemCls);
                    }

                    
                    if (oldRowDom.mergeAttributes) {
                        oldRowDom.mergeAttributes(newRow, true);
                    } else {
                        newAttrs = newRow.attributes;
                        attLen = newAttrs.length;
                        for (i = 0; i < attLen; i++) {
                            attName = newAttrs[i].name;
                            if (attName !== 'id') {
                                oldRowDom.setAttribute(attName, newAttrs[i].value);
                            }
                        }
                    }

                    if (isHovered) {
                        oldRow.addCls(overItemCls);
                    }

                    
                    oldCells = oldRow.query(me.cellSelector);
                    newCells = Ext.fly(newRow).query(me.cellSelector);
                    len = newCells.length;
                    
                    row = oldCells[0].parentNode;
                    for (i = 0; i < len; i++) {
                        
                        
                        if (me.shouldUpdateCell(columns[i], changedFieldNames)) {
                            
                            if (isEditing) {
                                Ext.fly(oldCells[i]).syncContent(newCells[i]);
                            }
                            
                            else {
                                row.insertBefore(newCells[i], oldCells[i]);
                                row.removeChild(oldCells[i]);
                            }
                        }
                    }
                }
                me.fireEvent('itemupdate', record, index, newRow);
            }
        }
    }
 
});