/**
 * Static methods for converting clicks on a map into portal.layer.querier.QueryTarget
 * objects that can be tested against various layer's querier instances for more information
 */
Ext.define('portal.map.gmap.ClickController', {

    statics : {
        /**
         * Utility for turning a click on a feature into a single QueryTarget
         */
        _marker : function(marker, overlayLatLng) {

            var basePrim = marker._portalBasePrimitive;
            if (!basePrim) {
                return [];
            }

            var id = basePrim.getId();
            var onlineResource = basePrim.getOnlineResource();
            var layer = basePrim.getLayer();
            var cswRecord = basePrim.getCswRecord();

            return [Ext.create('portal.layer.querier.QueryTarget', {
                id : id,
                lat : overlayLatLng.lat(),
                lng : overlayLatLng.lng(),
                onlineResource : onlineResource,
                layer : layer,
                cswRecord : cswRecord,
                explicit : true
            })];
        },

        /**
         * Utility for turning a click onto a polygon into a set of overlapping QueryTargets
         */
        _polygon : function(polygon, latLng, overlayLatLng, layerStore) {
            var queryTargets = [];
            var point = overlayLatLng ? overlayLatLng : latLng;

            //Look for all polygons intesecting the clicked point
            for (var i = 0; i < layerStore.getCount(); i++) {
                var layer = layerStore.getAt(i);
                var renderer = layer.data.renderer;
                var primitiveManager = renderer.primitiveManager;

                //Do this by diving straight into every renderer's list of polygons
                for (var j = 0; j < primitiveManager.primitiveList.length; j++) {
                    var overlayToTest =  primitiveManager.primitiveList[j];
                    if (overlayToTest instanceof GPolygon &&
                        overlayToTest.Contains(point)) {

                        var basePrim = overlayToTest._portalBasePrimitive;

                        var id = basePrim.getId();
                        var onlineResource = basePrim.getOnlineResource();
                        var layer = basePrim.getLayer();
                        var cswRecord = basePrim.getCswRecord();

                        queryTargets.push(Ext.create('portal.layer.querier.QueryTarget', {
                            id : id,
                            lat : point.lat(),
                            lng : point.lng(),
                            onlineResource : onlineResource,
                            layer : layer,
                            cswRecord : cswRecord,
                            explicit : true
                        }));
                    }
                }
            }

            return queryTargets;
        },

        /**
         * Utility for turning a raw point on the map into a series of QueryTargets
         * for all WMS/WCS layers.
         *
         * They are not explicit clicks because they occur based on the bounding box of the
         * WMS
         */
        _nonExplicit : function(latlng, layerStore) {
            var queryTargets = [];
            //Iterate everything with WMS/WCS - no way around this :(
            for (var i = 0; i < layerStore.getCount(); i++) {
                var layer = layerStore.getAt(i);

                var cswRecords = layer.get('cswRecords');
                for(var j = 0; j < cswRecords.length; j++){
                    var cswRecord = cswRecords[j];

                    //ensure this click lies within this CSW record
                    var containsPoint = false;
                    var geoEls = cswRecord.get('geographicElements');
                    for (var k = 0; k < geoEls.length; k++) {
                        if (geoEls[k] instanceof portal.util.BBox &&
                            geoEls[k].contains(latlng.lat(), latlng.lng())) {
                            containsPoint = true;
                            break;
                        }
                    }

                    //If it doesn't, don't consider this point for examination
                    if (!containsPoint) {
                        continue;
                    }

                    //Finally we don't include WMS query targets if we
                    //have WCS queries for the same record
                    var allResources = cswRecord.get('onlineResources');
                    var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(allResources, portal.csw.OnlineResource.WMS);
                    var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(allResources, portal.csw.OnlineResource.WCS);
                    var resourcesToIterate = [];
                    if (wcsResources.length > 0) {
                        resourcesToIterate = wcsResources;
                    } else {
                        resourcesToIterate = wmsResources;
                    }

                    //Generate our query targets for WMS/WCS layers
                    for (var k = 0; k < resourcesToIterate.length; k++) {
                        var type = resourcesToIterate[k].get('type');
                        if (type === portal.csw.OnlineResource.WMS ||
                            type === portal.csw.OnlineResource.WCS) {
                            queryTargets.push(Ext.create('portal.layer.querier.QueryTarget', {
                                id : '',
                                lat : latlng.lat(),
                                lng : latlng.lng(),
                                cswRecord   : cswRecord,
                                onlineResource : resourcesToIterate[k],
                                layer : layer,
                                explicit : true
                            }));
                        }
                    }
                }
            }

            return queryTargets;
        },

        /**
         * Given a raw click on the map - workout exactly what layer / feature has been interacted
         * with. Return the results as an Array portal.layer.querier.QueryTarget objects
         *
         * This function will normally return a singleton QueryTarget HOWEVER certain circumstances
         * might dictate that multiple items are going to be queried (such as in the case of overlapping polygons).
         *
         * @param overlay An instance of GOverlay (gmap api) - can be null
         * @param latlng An instance of GLatLng (gmap api) - can be null - the actual location clicked
         * @param overlayLatLng An instance of GLatLng (gmap api) - can be null - the actual location of the overlay clicked
         * @param layerStore An instance of portal.layer.LayerStore containing layers to be examined.
         */
        generateQueryTargets : function(overlay, latlng, overlayLatlng, layerStore) {
            if (!overlay) {
                return portal.map.gmap.ClickController._nonExplicit(latlng, layerStore);
            } else if (overlay instanceof GMarker) {
                return portal.map.gmap.ClickController._marker(overlay, overlayLatlng);
            } else if (overlay instanceof GPolygon) {
                return portal.map.gmap.ClickController._polygon(overlay, latlng, overlayLatlng, layerStore);
            } else {
                return []; //unable to handle clicks on other geometry types
            }
        }
    }
});