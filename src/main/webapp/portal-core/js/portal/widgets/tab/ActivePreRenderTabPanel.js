/**
 * An Ext.tab.Panel extension that can be manipulated BEFORE
 * the panel is actually rendered. It will behave (programmatically)
 * the same as a rendered tab panel.
 *
 * This means You should be able to add/remove tabs, set active tabs etc.
 * Attempting to do this on a regular tab panel BEFORE it has been
 * rendered will result in all sorts of undefined behaviour.
 */
Ext.define('portal.widgets.tab.ActivePreRenderTabPanel', {
    extend : 'Ext.tab.Panel',
    alias : 'widget.activeprerendertabpanel',

    //this exists to workaround issues with adding tabs to a non rendered tab panel
    //The strategy is that before the panel is rendered, all child tabs are added to this list instead
    //Upon the first render, this list will be used to populate the newly rendered tab panel.
    beforeRenderedTabs : [],

    //This is for setting the active tab AFTER rendering
    beforeRenderedActiveTab : '',

    constructor : function(config) {
        this.callParent(arguments);

        this.on('render', function(cmp) {
            cmp.loadBeforeRenderedTabs();
        });
    },

    /**
     * Our workaround is to record all tabs as they added/removed to this panel pre render. When
     * the render occurs, apply all the changes to the newly rendered panel.
     */
    loadBeforeRenderedTabs : function() {
        this.removeAll(true);
        this.add(this.beforeRenderedTabs);
        this.beforeRenderedTabs = [];
        if (this.beforeRenderedActiveTab) {
            this.setActiveTab(this.beforeRenderedActiveTab);
        }
    },

    /**
     * Wraps the normal add function, intercepts adds on non rendered panels
     */
    add : function(items) {
        if (this.rendered) {
            return this.callParent(arguments);
        }

        if (Ext.isArray(items)) {
            this.beforeRenderedTabs = this.beforeRenderedTabs.concat(items);
        } else {
            this.beforeRenderedTabs.push(items);
        }

        return items;
    },

    /**
     * Wraps the normal getComponent function, intercepts gets on non rendered panels.
     */
    getComponent : function(comp) {
        if (this.rendered) {
            return this.callParent(arguments);
        }

        var compId = null;
        if (Ext.isString(comp)) {
            compId = comp;
        } else if (Ext.isObject(comp) && comp.getItemId) {
            compId = comp.getItemId();
        } else {
            compId = comp.itemId;
        }

        for (var i = 0; i < this.beforeRenderedTabs.length; i++) {
            if (compId === this.beforeRenderedTabs[i].itemId) {
                return this.beforeRenderedTabs[i];
            }
        }

        return null;
    },

    /**
     * Wraps the normal remove function, intercepts remove before tab render
     */
    remove : function(comp, autoDestroy) {
        if (this.rendered) {
            return this.callParent(arguments);
        }

        var beforeRenderedCmp = this.getComponent(comp);
        if (beforeRenderedCmp) {
            var beforeLen = this.beforeRenderedTabs.length;
            this.beforeRenderedTabs = Ext.Array.remove(this.beforeRenderedTabs, comp);

            var destroy = autoDestroy || this.autoDestroy || beforeRenderedCmp.autoDestroy;
            if (destroy && beforeRenderedCmp.rendered) {
                beforeRenderedCmp.destroy();
            }
        }

        return beforeRenderedCmp;
    },

    /**
     * Wraps the removeAll function
     */
    removeAll : function(autoDestroy) {
        if (this.rendered) {
            return this.callParent(arguments);
        }

        for (var i = this.beforeRenderedTabs.length - 1; i >= 0; i--) {
            this.remove(this.beforeRenderedTabs[i], autoDestroy);
        }

        return [];
    },

    /**
     * Wraps the normal setActiveTab function.
     */
    setActiveTab : function(tabName) {
        if (this.rendered) {
            return this.callParent(arguments);
        }

        if (this.getComponent(tabName)) {
            this.beforeRenderedActiveTab = tabName;
            return true;
        }

        return false;
    }
});