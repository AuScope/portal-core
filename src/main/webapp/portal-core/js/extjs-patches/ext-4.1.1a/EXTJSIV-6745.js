/**
 * Underlying cause - http://www.sencha.com/forum/showthread.php?228970
 * First Discovered as part of http://jira.csiro.au/browse/AUS-2185
 *
 * Fixes Form Panels not removing their loadmask when the underlying BasicForm
 * finishes loading/submitting.
 */
Ext.define('Ext.form.SubmitFix', {
    override: 'Ext.ZIndexManager',


    register : function(comp) {
        var me = this,
            compAfterHide = comp.afterHide;

        if (comp.zIndexManager) {
            comp.zIndexManager.unregister(comp);
        }
        comp.zIndexManager = me;

        me.list[comp.id] = comp;
        me.zIndexStack.push(comp);

        // Hook into Component's afterHide processing
        comp.afterHide = function() {
            compAfterHide.apply(comp, arguments);
            me.onComponentHide(comp);
        };
    },

    /**
     * Unregisters a {@link Ext.Component} from this ZIndexManager. This should not
     * need to be called. Components are automatically unregistered upon destruction.
     * See {@link #register}.
     * @param {Ext.Component} comp The Component to unregister.
     */
    unregister : function(comp) {
        var me = this,
            list = me.list;

        delete comp.zIndexManager;
        if (list && list[comp.id]) {
            delete list[comp.id];

            // Relinquish control of Component's afterHide processing
            delete comp.afterHide;
            Ext.Array.remove(me.zIndexStack, comp);

            // Destruction requires that the topmost visible floater be activated. Same as hiding.
            me._activateLast();
        }
    }
});