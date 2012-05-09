/**
 * An extension to OpenLayers.Handler.Feature which ensures the raw click event
 * is also passed up to any event handlers.
 *
 * It also ensures that click events that do NOT hit a feature still return a click event (sans feature)
 */
Ext.ns('portal.map.openlayers');
portal.map.openlayers.FeatureWithLocationHandler = OpenLayers.Class(OpenLayers.Handler.Feature, {

    /**
     * Hackish approach to remembering the last handled event so we can append it to the argument list
     * of the event handler
     */
    lastHandledEvent : null,

    handle : function(event) {
        //Remember our event (we'll inject it during our triggerCallback override)
        this.lastHandledEvent = event;
        var handled = OpenLayers.Handler.Feature.prototype.handle.apply(this, arguments);

        //If we don't handle the click, return the event anyway
        if (!handled) {
            var type = event.type;
            var click = (type === 'click');
            if (click) {
                handled = true;
                this.callback('click', [null, event]);
            }
        }

        return handled;
    },

    triggerCallback : function(type, mode, args) {
        args.push(this.lastHandledEvent);

        return OpenLayers.Handler.Feature.prototype.triggerCallback.apply(this, arguments);
    }
});