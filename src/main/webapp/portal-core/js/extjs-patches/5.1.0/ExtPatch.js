//Ext.define('Auscope.container.Container', {
//    override : 'Ext.container.Container',
//    
//    onBeforeAdd: function(item) {
//        
//        var owner = item.ownerCt;
//        if (owner && owner !== this) {
//            owner.remove(item, false);
//        }
//    },
//    
//    remove: function(component, autoDestroy) {
//        console.log(component.ownerCt);
//        var me = this,
//            c = me.getComponent(component);
//        if (!arguments.length) {
//            Ext.log.warn("Ext.container.Container: remove takes an argument of the component to remove. cmp.remove() is incorrect usage.");
//        }
//        if (c && (!me.hasListeners.beforeremove || me.fireEvent('beforeremove', me, c) !== false)) {
//            me.doRemove(c, autoDestroy);
//            if (me.hasListeners.remove) {
//                me.fireEvent('remove', me, c);
//            }
//            if (!me.destroying && !c.floating) {
//                console.log(component.ownerCt);
//                me.updateLayout();
//            }
//        }
//        return c;
//    }
// 
//});
//
//Ext.define('Auscope.layout.component.Dock', {
//    override : 'Ext.layout.component.Dock',
//    
//    renderItems: function(items, target) {
//        var me = this,
//            dockedItemCount = items.length,
//            itemIndex = 0,
//            correctPosition = 0,
//            staticNodeCount = 0,
//            targetNodes = me.getRenderTarget().dom?me.getRenderTarget().dom.childNodes:[],
//            targetChildCount = targetNodes.length,
//            i, j, targetChildNode, item;
//        
//        for (i = 0 , j = 0; i < targetChildCount; i++) {
//            targetChildNode = targetNodes[i];
//            if (targetChildNode.nodeType === 1 && Ext.fly(targetChildNode).hasCls(Ext.baseCSSPrefix + 'resizable-handle')) {
//                break;
//            }
//            for (j = 0; j < dockedItemCount; j++) {
//                item = items[j];
//                if (item.rendered && item.el.dom === targetChildNode) {
//                    break;
//                }
//            }
//            
//            
//            if (j === dockedItemCount) {
//                staticNodeCount++;
//            }
//        }
//        
//        for (; itemIndex < dockedItemCount; itemIndex++ , correctPosition++) {
//            item = items[itemIndex];
//            
//            if (itemIndex === correctPosition && (item.dock === 'right' || item.dock === 'bottom')) {
//                correctPosition += staticNodeCount;
//            }
//            
//            if (item && !item.rendered) {
//                me.renderItem(item, target, correctPosition);
//            } else if (!me.isValidParent(item, target, correctPosition)) {
//                me.moveItem(item, target, correctPosition);
//            }
//        }
//    }
// 
//});
//
//Ext.define('Auscope.layout.Layout', {
//    override : 'Ext.layout.Layout',
//    
//    isValidParent: function(item, target, position) {
//        var targetDom = (target && target.dom) || target,
//            itemDom = this.getItemLayoutEl(item);
//        
//        if (itemDom && targetDom) {
//            if (typeof position == 'number') {
//                position = this.getPositionOffset(position);
//                return itemDom === targetDom.childNodes[position];
//            }
//            return itemDom.parentNode === targetDom;
//        }
//        return false;
//    },
//    
//    getItemLayoutEl: function(item) {
//        var dom = item.el ? item.el.dom : Ext.getDom(item);
//        
//        var parentNode = dom?dom.parentNode:null; 
//        var className;
//        if (parentNode) {
//            className = parentNode.className;
//            if (className && className.indexOf(Ext.baseCSSPrefix + 'resizable-wrap') !== -1) {
//                dom = dom.parentNode;
//            }
//        }
//        return dom;
//    }
// 
//});
