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

    /**
     * We set a very high z-index on our featured layers to make sure that they are rendered in the correct order.
     * This also now means that we need to override the popup and control defaults to make sure they have a higher z-index.
     */
    OpenLayers.Map.Z_INDEX_BASE = {
        BaseLayer: 100,
        Overlay: 1000,
        Feature: 10000,
        Popup: 10000000,
        Control: 20000000
    };

    OpenLayers.Map.prototype.addPopup = function(popup, exclusive) {
        
        if (exclusive) {
            //remove all other popups from screen
            for (var i = this.popups.length - 1; i >= 0; --i) {
                this.removePopup(this.popups[i]);
            }
        }

        popup.map = this;
        this.popups.push(popup);
        var popupDiv = popup.draw();
        if (popupDiv) {
            popupDiv.style.zIndex = OpenLayers.Map.Z_INDEX_BASE['Popup'] + this.popups.length;
            this.layerContainerDiv.appendChild(popupDiv);
        }
    };
};

