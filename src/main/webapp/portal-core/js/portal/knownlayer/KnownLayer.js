/**
 * KnownLayer's are a portal defined grouping of CSWRecords. The records are grouped
 * 'manually' at the portal due to limitations with the way services can identify themselves.
 *
 * For example, there may be many WFS's with gsml:Borehole features but there is no way to
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
        { name: 'proxyCountUrl', type: 'string' }, //A URL of a backend controller method for fetching the count of data available (eg for WFS a URL that will set featureType=hits)
        { name: 'iconUrl', type: 'string' }, //A URL of an icon that will be used for rendering GMarkers associated with this layer
        { name: 'iconAnchor', type: 'auto' }, //An object containing x,y for the pixel location of where the icon get's anchored to the map
        { name: 'iconSize', type: 'auto' }, //An object containing width,height for the pixel size of the icon
        { name: 'cswRecords', convert: portal.csw.CSWRecordType.convert}, //a set of portal.csw.CSWRecord objects that belong to this KnownLayer grouping
        { name: 'relatedRecords', convert: portal.csw.CSWRecordType.convert}// a set of portal.csw.CSWRecord objects that relates to this knownlayer
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
    }
});