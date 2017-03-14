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
    mouseDragFlag: false,
    downX: 0,
    downY: 0,
    handle : function(event) {
        //Remember our event (we'll inject it during our triggerCallback override)

        var handled = OpenLayers.Handler.Feature.prototype.handle.apply(this, arguments);

        //If we don't handle the click, return the event anyway
        //LJ AUS-2592.5 Separate the panning dragging event and click event 
        //   by calculating the distance between mousedown and mouseup position.
        if (!handled) {
            var type = event.type;
            if (type === 'click' && !this.mouseDragFlag) {
                handled = true;                
                this.callback('click', [null, event]);                
            } else if (type === 'mousedown') {
                this.downX = event.xy.x;
                this.downY = event.xy.y;     
                this.mouseDragFlag = true;                
            } else if (type === 'mouseup') {    
            	if (this.lastHandledEvent != null && this.lastHandledEvent.type == 'mousedown') {
                    var xx = Math.abs(event.xy.x - this.downX);
                    var yy = Math.abs(event.xy.y - this.downY);
                    if ( xx < 5 && yy < 5)
                        this.mouseDragFlag = false;
            	}                    
            }            
        }
        this.lastHandledEvent = event;
        return handled;
    },

    triggerCallback : function(type, mode, args) {
        args.push(this.lastHandledEvent);

        return OpenLayers.Handler.Feature.prototype.triggerCallback.apply(this, arguments);
    }
});