/**
 * KnownLayers are portal-defined grouping of CSWRecords. The records are grouped
 * 'manually' at the portal due to limitations with the way services can identify themselves.
 *
 * For example, there may be many WFSs with gsml:Borehole features but there is no way to
 * automatically identify them as 'Pressure DB' or 'National Virtual Core Library' feature
 * services. Instead we manually perform the grouping on the portal backend
 */
Ext.require('portal.csw.CSWRecordType');
Ext.define('portal.knownlayer.KnownLayer', {
    extend: 'Ext.data.Model',

    requires: ['portal.csw.CSWRecordType'],

    fields: [
        { name: 'id', type: 'string' }, //a unique ID of the known layer grouping
        { name: 'name', type: 'string'}, //A human readable name/title for this grouping
        { name: 'description', type: 'string' }, //A human readable description of this KnownLayer
        { name: 'group', type: 'string' }, //A term in which like KnownLayers can be grouped under
        { name: 'proxyUrl', type: 'string' }, //A URL of a backend controller method for fetching available data with a filter specific for this KnonwLayer
        { name: 'proxyGetFeatureInfoUrl', type: 'string' }, // A URL of a backend controller method for processing the WMS Get Feature Info response
        { name: 'proxyCountUrl', type: 'string' }, //A URL of a backend controller method for fetching the count of data available (eg for WFS a URL that will set featureType=hits)
        { name: 'proxyStyleUrl', type: 'string' }, // A URL of a backend controller method for fetching style
        { name: 'proxyDownloadUrl', type: 'string' }, // A URL of a backend controller method for download request
        { name: 'iconUrl', type: 'string' }, //A URL of an icon that will be used for rendering GMarkers associated with this layer
        { name: 'polygonColor', type: 'string' }, //Color of the polygon for csw rendering
        { name: 'iconAnchor', type: 'auto' }, //An object containing x,y for the pixel location of where the icon gets anchored to the map
        { name: 'iconSize', type: 'auto' }, //An object containing width,height for the pixel size of the icon
        { name: 'cswRecords', convert: portal.csw.CSWRecordType.convert}, //a set of portal.csw.CSWRecord objects that belong to this KnownLayer grouping
        { name: 'relatedRecords', convert: portal.csw.CSWRecordType.convert},// a set of portal.csw.CSWRecord objects that relate to this knownlayer
        { name: 'loading', type: 'boolean', defaultValue: false },//Whether this layer is currently loading data or not
        { name: 'layer', type: 'auto'}, // store the layer after it has been converted.        
        { name: 'active', type: 'boolean', defaultValue: false },//Whether this layer is current active on the map.
        { name: 'feature_count', type: 'string'}, //GetFeatureInfo feature_count attribute, 0 would be to default to whatever is set on the server.
        { name: 'order', type: 'string'},	// Order of the layers within a group
        { name: 'singleTile', type: 'boolean'},    // Whether the layer should be requested as a single image (ie not tiled)
        { name: 'nagiosFailingHosts', type: 'auto'},    // An array of host names that are failing according to a remote Nagios instance.
        { name: 'staticLegendUrl', type: 'string'}    // A URL to use to grab a canned legend graphic for the layer, optional.
    ],

    /**
     * Collates all portal.csw.OnlineResource objects owned by every CSWRecord instance in this known layer
     * and returns the set as an Array.
     */
    getAllOnlineResources : function() {
        var ors = [];
        for (var i = 0; i < this.data.cswRecords.length; i++) {
            ors = ors.concat(this.data.cswRecords[i].get('onlineResources'));
        }
        return ors;
    },

    /**
     * Given a keyword, search through every portal.csw.CSWRecord contained by this KnownLayer and return
     * an Array of portal.csw.CSWRecord objects that have the specified keyword
     * @param keyword String keyword or an Array of strings
     */
    getCSWRecordsByKeywords : function(keyword){
        //Filter our results
        var results = [];
        var cswRecords = this.get('cswRecords');
        for (var i = 0; i < cswRecords.length; i++){
            if (cswRecords[i].containsKeywords(keyword)) {
                results.push(cswRecords[i]);
            }
        }
        return results;
    },

    /**
     * Similar to getCSWRecordsByKeywords but instead sources portal.csw.CSWRecord objects
     * from the 'relatedRecords' property of this known layer
     * @param keyword String keyword or an Array of strings
     */
    getRelatedCSWRecordsByKeywords : function(keyword){
        //Filter our results
        var results = [];
        var cswRecords = this.get('relatedRecords');
        for (var i = 0; i < cswRecords.length; i++){
            if (cswRecords[i].containsKeywords(keyword)) {
                results.push(cswRecords[i]);
            }
        }
        return results;
    },

    containsCSWService : function() {
        var cswRecords = this.get('cswRecords');
        if (cswRecords.length == 1) {
            var onlineResources = cswRecords[0].get('onlineResources');
            if (onlineResources.length == 1) {
                return onlineResources[0].get('type') == portal.csw.OnlineResource.CSWService;
            }
        }

        return false;
    },
    
    /**
     * Returns true if this knownlayer has one or more hosts failing according to nagios
     */
    containsNagiosFailures : function() {
        return !Ext.isEmpty(this.get('nagiosFailingHosts'));
    }
});