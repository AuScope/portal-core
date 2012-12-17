/**
 * An implementation of a portal.layer.Renderer for rendering generic Layers
 * that belong to a set of portal.csw.CSWRecord objects.
 */
Ext.define('portal.layer.renderer.csw.CSWRenderer', {
    extend: 'portal.layer.renderer.Renderer',
    config : {
        icon : null
    },
    constructor: function(config) {
        this.legend = Ext.create('portal.layer.legend.wfs.WFSLegend', {
            iconUrl : config.icon ? config.icon.getUrl() : ''
        });
        this.callParent(arguments);
    },

    /**
     * A function for displaying generic data from a variety of data sources. This function will
     * raise the renderstarted and renderfinished events as appropriate. The effect of multiple calls
     * to this function (ie calling displayData again before renderfinished is raised) is undefined.
     *
     * This function will re-render itself entirely and thus may call removeData() during the normal
     * operation of this function
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer,
     *          function(portal.layer.renderer.Renderer this, portal.csw.OnlineResource[] resources, portal.layer.filterer.Filterer filterer, bool success) callback
     *
     * returns - void
     *
     * resources - an array of data sources which should be used to render data
     * filterer - A custom filter that can be applied to the specified data sources
     * callback - Will be called when the rendering process is completed and passed an instance of this renderer and the parameters used to call this function
     */
    displayData : function(resources, filterer, callback) {
        this.removeData();
        var titleFilter = '';
        var keywordFilter = '';
        var resourceProviderFilter = '';
        var filterObj=null;
        if(filterer.getParameters()){
            filterObj=filterer.getParameters();
        }

        var regexp = /\*/;
        if(filterObj){
            titleFilter = filterObj.title;
            if(titleFilter !== '' && /^\w+/.test(titleFilter)) {
                regexp = new RegExp(titleFilter, "i");
            }
        }

        if(filterObj && filterObj.keyword) {
            keywordFilter = filterObj.keyword;
        }

        if(filterObj && filterObj.resourceProvider) {
            resourceProviderFilter = filterObj.resourceProvider;
        }

        this.fireEvent('renderstarted', this, resources, filterer);


        var cswRecords = this.parentLayer.get('cswRecords');
        var numRecords = 0;
        var primitives = [];
        for (var i = 0; i < cswRecords.length; i++) {
            if ((titleFilter === '' || regexp.test(cswRecords[i].get('name'))) &&
                    (keywordFilter === '' || cswRecords[i].containsKeywords(keywordFilter)) &&
                    (resourceProviderFilter === '' || cswRecords[i].get('resourceProvider') === resourceProviderFilter)) {
                numRecords++;
                var geoEls = cswRecords[i].get('geographicElements');
                for (var j = 0; j < geoEls.length; j++) {
                    var geoEl = geoEls[j];
                    if (geoEl instanceof portal.util.BBox) {
                        if(geoEl.eastBoundLongitude === geoEl.westBoundLongitude &&
                            geoEl.southBoundLatitude === geoEl.northBoundLatitude) {
                            //We only have a point
                            var point = Ext.create('portal.map.Point', {
                                latitude : geoEl.southBoundLatitude,
                                longitude : geoEl.eastBoundLongitude
                            });

                            primitives.push(this.map.makeMarker(cswRecords[i].get('id'), cswRecords[i].get('name'), cswRecords[i], undefined, this.parentLayer, point, this.getIcon()));
                        } else { //polygon
                            var polygonList = geoEl.toPolygon(this.map, '#0003F9', 4, 0.75,'#0055FE', 0.4, undefined,
                                    cswRecords[i].get('id'), cswRecords[i], undefined, this.parentLayer);

                            for (var k = 0; k < polygonList.length; k++) {
                                primitives.push(polygonList[k]);
                            }
                        }
                    }
                }
            }
        }

        this.primitiveManager.addPrimitives(primitives);
        this.fireEvent('renderfinished', this);
    },

    /**
     * An abstract function for creating a legend that can describe the displayed data. If no
     * such thing exists for this renderer then null should be returned.
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer)
     *
     * returns - portal.layer.legend.Legend or null
     *
     * resources - (same as displayData) an array of data sources which should be used to render data
     * filterer - (same as displayData) A custom filter that can be applied to the specified data sources
     */
    getLegend : function(resources, filterer) {
        return this.legend;
    },

    /**
     * An abstract function that is called when this layer needs to be permanently removed from the map.
     * In response to this function all rendered information should be removed
     *
     * function()
     *
     * returns - void
     */
    removeData : function() {
        this.primitiveManager.clearPrimitives();
    },

    /**
     * No point aborting a bbox rendering
     */
    abortDisplay : Ext.emptyFn
});