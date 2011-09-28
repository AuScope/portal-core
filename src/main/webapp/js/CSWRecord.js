/**
 * A representation of a CSWRecord in the user interface (as returned by the getCSWRecords.do handler).
 */
CSWRecord = Ext.extend(AbstractRecordWrapper, {
    /**
     * Accepts a Ext.util.Observable configuration object with the following extensions
     * {
     *  internalRecord : Ext.data.Record - The record to be wrapped (will be assigned to internalRecord)
     * }
     */
    constructor : function(internalRecord) {
        CSWRecord.superclass.constructor.call(this, {
            internalRecord : internalRecord
        });
    },

    /**
     * Gets the name of the service as a String
     */
    getServiceName : function() {
        return this.getStringField('serviceName');
    },

    /**
     * Gets the Contact organisation as a String
     */
    getContactOrganisation : function() {
        return this.getStringField('contactOrganisation');
    },

    /**
     * Gets the resource provider as a String
     */
    getResourceProvider : function() {
        return this.getStringField('resourceProvider');
    },

    /**
     * Gets the file identifier as a String
     * @return
     */
    getFileIdentifier : function() {
        return this.getStringField('fileIdentifier');
    },

    /**
     * Gets the record info URL as a String
     * @return
     */
    getRecordInfoUrl : function() {
        return this.getStringField('recordInfoUrl');
    },

    /**
     * Gets the abstract for this record as a String
     * @return
     */
    getDataIdentificationAbstract : function() {
        return this.getStringField('dataIdentificationAbstract');
    },

    /**
     * Gets all online resource representations associated with this record
     *
     * Returns an Array of Objects in the form
     * {
     *  url                 : String
     *  onlineResourceType  : String
     *  name                : String
     *  description         : String
     * }
     * @return
     */
    getOnlineResources : function() {
        return this.getArrayField('onlineResources');
    },

    /**
     * Gets all online resources associated with this record that pass the specified filters
     *
     *
     * onlineResourceType : [Set to undefined to not filter] must be from ['WMS', 'WCS', 'WFS', 'OPeNDAP']
     * name : [Set to undefined to not filter] The name to filter by
     * description : [Set to undefined to not filter] The description to filter by
     * url : [Set to undefined to not filter] The url to filter by
     *
     * Returns an Array of Objects in the following form that pass every specified filter
     * {
     *  url                 : String
     *  onlineResourceType  : String
     *  name                : String
     *  description         : String
     * }
     * @return
     */
    getFilteredOnlineResources : function(onlineResourceType, name, description, url) {
        var all = this.getOnlineResources();
        var filtered = [];

        for (var i = 0; i < all.length; i++) {
            var cmp = all[i];

            if (onlineResourceType !== undefined && cmp.onlineResourceType !== onlineResourceType) {
                continue;
            }

            if (name !== undefined && cmp.name !== name) {
                continue;
            }

            if (description !== undefined && cmp.description !== description) {
                continue;
            }

            if (url !== undefined && cmp.url !== url) {
                continue;
            }

            filtered.push(cmp);
        }

        return filtered;
    },

    /**
     * Gets all geographic element representations associated with this record
     *
     * Returns an Array of BBox Objects
     * @return
     */
    getGeographicElements : function() {
        return this.getArrayField('geographicElements');
    },

    /**
     * Gets all keywords associated with this record
     *
     * Returns an Array of String
     * @return
     */
    getDescriptiveKeywords : function() {
        return this.getArrayField('descriptiveKeywords');
    },

    getConstraints : function() {
        return this.getArrayField('constraints');
    },

    /**
     * Iterates through all geographic elements and returns a bounding box that contains them all.
     *
     * Returns a BBox object.
     *
     * If there are no geographic elements that can be parsed, null will be returned;
     * @return
     */
    generateGeographicExtent : function() {
        var geoEls = this.getGeographicElements();
        var extent = null;

        for (var i = 0; i < geoEls.length; i++) {
            if (geoEls[i] instanceof BBox) {

                if (extent === null) {
                    extent = geoEls[i];
                } else {
                    extent = extent.combine(geoEls[i]);
                }
            }
        }

        return extent;
    },

    /**
    * Gets an OverlayManager that holds the list of bounding boxes for this layer (or null/undefined)
    */
    getBboxOverlayManager : function() {
        return this.internalRecord.bboxOverlayManager;
    },

    /**
    * Sets an OverlayManager that holds the list of bounding boxes for this layer (or null/undefined)
    */
    setBboxOverlayManager : function(bboxOverlayManager) {
        this.internalRecord.bboxOverlayManager = bboxOverlayManager;
    },

    /**
     * Returns true if this record contains the given descriptive keyword, false otherwise.
     */
    containsKeyword : function(str) {
        var descriptiveKeywords = this.getArrayField('descriptiveKeywords');
        for(var i=0; i<descriptiveKeywords.length; i++) {
            if(descriptiveKeywords[i] == str) {
                return true;
            }
        }
        return false;
    },

    /**
     * Returns true if every onlineResource in the specified array can be found in this CSWRecord
     * param onlineResources - an array of objects that match the response from getOnlineResources.
     */
    matchesOnlineResources : function(onlineResources) {
        var comparator = function(or1, or2) {
            return (or1.url === or2.url) &&
                   (or1.onlineResourceType === or2.onlineResourceType) &&
                   (or1.name === or2.name) &&
                   (or1.description === or2.description);
        };

        var resourcesToMatch = this.getOnlineResources();

        //Iterate the specified list of online resources
        for (var i = 0; i < onlineResources.length; i++) {
            var matchFound = false;

            //Find an associated match in the online resources of this object
            for (var j = 0; j < resourcesToMatch.length && !matchFound; j++) {
                matchFound = comparator(onlineResources[i], resourcesToMatch[j]);
            }

            if (!matchFound) {
                return false;
            }
        }

        return true;
    }
});



