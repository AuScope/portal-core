/**
 * CSWRecord is a simplified representation of a metadata record
 * from a catalogue service for the web (CSW)
 */
Ext.require(['portal.csw.OnlineResourceType',
             'portal.util.BBoxType',
             'portal.csw.CSWRecordType']);
Ext.define('portal.csw.CSWRecord', {
    extend: 'Ext.data.Model',
    requires: ['portal.csw.OnlineResourceType',
               'portal.util.BBoxType',
               'portal.csw.CSWRecordType'],
    fields: [
        { name: 'id', type: 'string' }, //Based on CSWRecord's file identifier
        { name: 'name', type: 'string' }, //Human readable name/title of this record
        { name: 'description', type: 'string' }, //Human readable description of this record (based on abstract)
        { name: 'adminArea', type: 'string' }, //The adminstrative area this record identifies itself as being a part of (organisation name that owns this record)
        { name: 'contactOrg', type: 'string' }, //Who is providing this resource (organisation name)
        { name: 'descriptiveKeywords', type: 'auto' }, //an array of strings representing descriptive keywords for this record
        { name: 'dataSetURIs', type: 'auto' }, //an array of strings representing URIs where file downloads may be obtained
        { name: 'geographicElements', convert: portal.util.BBoxType.convert}, //an array of portal.util.BBox objects representing the total spatial bounds of this record
        { name: 'onlineResources', convert: portal.csw.OnlineResourceType.convert}, //A set of portal.csw.OnlineResource objects
        { name: 'childRecords', convert: portal.csw.CSWRecordType.convert}, //an array of child portal.csw.CSWRecord objects
        { name: 'resourceProvider', type: 'string'}, //A set of portal.csw.OnlineResource objects
        { name: 'recordInfoUrl' , type:'string'},        
        { name: 'noCache' , type:'boolean'},
        { name: 'extensions', type:'auto'}, //A normally undefined object. CSWRecord can be extended by filling in this field.
        { name: 'constraints' , type:'auto'}, //An array of strings representing access constraints that will be shown to a user before this layer is used
        { name: 'date' , type:'date', convert: function(dateString) {
            if(dateString){
                return new Date(Date.parse(dateString.replace(' UTC', '')));
            }else{
                return dateString;
            }
        }},//The date of this CSWRecord
        { name: 'loading', type: 'boolean', defaultValue: false },//Whether this layer is currently loading data or not
        { name: 'layer', type: 'auto'}, // store the layer after it has been converted.        
        { name: 'active', type: 'boolean', defaultValue: false },//Whether this layer is current active on the map.
        { name: 'customlayer', type: 'boolean', defaultValue: false }, //If true, this layer is added from browse catalogue
        { name: 'service', type: 'boolean', defaultValue: false } //If true, this layer is a service layer that may contain layers in the getCapabilities
    ],

    /**
     * Returns a boolean indicating whether or not this record has any access constraints associated with it.
     *
     * Empty access constraints will not be counted
     */
    hasConstraints : function() {
        var constraints = this.get('constraints');
        for (var i = 0; i < constraints.length; i++) {
            var constraint = constraints[i].replace(/^\s\s*/, '').replace(/\s\s*$/, ''); //trim the string
            if (constraint.length > 0) {
                return true;
            }
        }

        return false;
    },

    /**
     * Function to return true if the keywords matches any of the filter parameter
     *
     * return boolean
     * str - str(String) can either be an array of string or a single string value filter parameter
     */
    containsKeywords : function(str) {

        var keywords = str;
        if(!Ext.isArray(str)) {
           keywords = [str];
        }

        for (var j=0;j<keywords.length; j++) {
            var descriptiveKeywords = this.get('descriptiveKeywords');
            for (var i=0; i<descriptiveKeywords.length; i++) {
                if(descriptiveKeywords[i] == keywords[j]) {
                    return true;
                }
            }

        }
        return false;
    },

    /**
     * Function for checking whether this record contains the specified portal.csw.OnlineResource
     *
     * Comparisons are made on a field/field basis
     *
     * @param onlineResource A portal.csw.OnlineResource
     *
     * returns boolean indicating
     */
    containsOnlineResource : function(onlineResource) {
        var comparator = function(or1, or2) {
            return ((or1.get('url') === or2.get('url')) &&
                   (or1.get('type') === or2.get('type')) &&
                   (or1.get('name') === or2.get('name')) &&
                   (or1.get('description') === or2.get('description')));
        };

        var resourcesToMatch = this.get('onlineResources');
        for (var i = 0; i < resourcesToMatch.length; i++) {
            if (comparator(resourcesToMatch[i], onlineResource)) {
                return true;
            }
        }

        return false;
    },
    
    containsOnlineResourceUrl : function(url) {
        
        var resourcesToMatch = this.getAllChildOnlineResources();
        for (var i = 0; i < resourcesToMatch.length; i++) {
            if (resourcesToMatch[i].get('url').toLowerCase()===url.toLowerCase()) {
                return true;
            }
        }

        return false;
    },

    /**
     * Iterates this CSWRecord and all child CSWRecords. Each record will have it's OnlineResource
     * array concatenated and the sum total of all concatenations will be returned.
     */
    getAllChildOnlineResources : function() {
        var onlineResources = this.get('onlineResources');
        var childRecs = this.get('childRecords');

        if (!onlineResources) {
            onlineResources = [];
        }

        if(childRecs){
            for (var i = 0; i < childRecs.length; i++) {
                onlineResources = onlineResources.concat(childRecs[i].getAllChildOnlineResources());
            }
        }

        return onlineResources;
    },

    /**
     * Iterates this CSWRecord and optionally all child CSWRecords. Every CSWRecord will be searched
     * for an online resource with the specified orId. Returns a portal.csw.OnlineResource object
     * with the specified ID or null
     *
     * @param orId The ID to search for
     * @param searchChildren If true, any child records will be searched for a matching OnlineResource
     */
    getOnlineResourceById : function(orId, searchChildren) {
        var onlineResources = this.get('onlineResources');
        var childRecs = this.get('childRecords');

        if(onlineResources) {
            for (var i = 0; i < onlineResources.length; i++) {
                if (onlineResources[i].get('id') === orId) {
                    return onlineResources[i];
                }
            }
        }

        if(searchChildren && childRecs) {
            for (var i = 0; i < childRecs.length; i++) {
                var matchingOr = childRecs[i].getOnlineResourceById(orId, searchChildren);
                if (matchingOr) {
                    return matchingOr;
                }
            }
        }

        return null;
    }

});