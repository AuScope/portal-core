/**
 * CSWRecord is a simplified representation of a metadata record
 * from a catalogue service for the web (CSW)
 */
Ext.require(['portal.csw.OnlineResourceType',
             'portal.util.BBoxType']);
Ext.define('portal.csw.CSWRecord', {
    extend: 'Ext.data.Model',
    requires: ['portal.csw.OnlineResourceType',
               'portal.util.BBoxType'],
    fields: [
        { name: 'id', type: 'string' }, //Based on CSWRecord's file identifier
        { name: 'name', type: 'string' }, //Human readable name/title of this record
        { name: 'description', type: 'string' }, //Human readable description of this record (based on abstract)
        { name: 'adminArea', type: 'string' }, //The adminstrative area this record identifies itself as being a part of (organisation name that owns this record)
        { name: 'contactOrg', type: 'string' }, //Who is providing this resource (organisation name)
        { name: 'descriptiveKeywords', type: 'auto' }, //an array of strings representing descriptive keywords for this record
        { name: 'geographicElements', convert: portal.util.BBoxType.convert}, //an array of portal.util.BBox objects representing the total spatial bounds of this record
        { name: 'onlineResources', convert: portal.csw.OnlineResourceType.convert}, //A set of portal.csw.OnlineResource objects
        { name: 'resourceProvider', type: 'string'}, //A set of portal.csw.OnlineResource objects
        { name: 'recordInfoUrl' , type:'string'},
        { name: 'constraints' , type:'auto'} //An array of strings representing access constraints that will be shown to a user before this layer is used
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
    }

});