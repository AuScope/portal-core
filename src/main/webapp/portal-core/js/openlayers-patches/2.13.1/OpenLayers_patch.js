/**
 * This issue came up while working on GPT-162--zombie layers 
 * 
 * The issue is a compatibility problem between OL2.13.1 and Google Maps API v3. 
 * If you set Google Street or Hybrid as base layer it is not rendered on the map 
 * on page load. Since Google Satellite is our default, this is mostly a problem
 * when reloading saved state, as the application should persist and reload the baselayer
 * as part of the state.
 * 
 * The patch was found in https://github.com/openlayers/ol2/issues/1450#issuecomment-146207698 and tested with a 
 * fully patched version of the OpenLayers.debug.js and OpenLayers.js files. It looked good. 
 * 
 * This file applies the patch by overriding the containing function in the OpenLayers object.
 */
if (window.OpenLayers) {
    /**
     * Method: setGMapVisibility
     * Display the GMap container and associated elements.
     * 
     * Parameters:
     * visible - {Boolean} Display the GMap elements.
     */
    OpenLayers.Layer.Google.v3.setGMapVisibility = function(visible) {
        var cache = OpenLayers.Layer.Google.cache[this.map.id];
        var map = this.map;
        if (cache) {
            var type = this.type;
            var layers = map.layers;
            var layer;
            for (var i=layers.length-1; i>=0; --i) {
                layer = layers[i];
                if (layer instanceof OpenLayers.Layer.Google &&
                            layer.visibility === true && layer.inRange === true) {
                    type = layer.type;
                    visible = true;
                    break;
                }
            }
            var container = this.mapObject.getDiv();
            if (visible === true) {
                if (container.parentNode !== map.div) {
                    if (!cache.rendered) {
                        var me = this;
                        google.maps.event.addListenerOnce(this.mapObject, 'tilesloaded', function() {
                            cache.rendered = true;
                            me.setGMapVisibility(me.getVisibility());
                            me.moveTo(me.map.getCenter());
                            cache.googleControl.appendChild(map.viewPortDiv);
                        });
                    } else {
                        cache.googleControl.appendChild(map.viewPortDiv);
                    }
                    map.div.appendChild(container);
                    google.maps.event.trigger(this.mapObject, 'resize');
                }
                this.mapObject.setMapTypeId(type);                
            } else if (cache.googleControl.hasChildNodes()) {
                map.div.appendChild(map.viewPortDiv);
                map.div.removeChild(container);
            }
        }
    };
}