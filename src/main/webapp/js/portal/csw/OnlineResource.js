/**
 * an OnlineResource is a 'fundamental' unit of all CSWRecords, it represents
 * a resource available somewhere in the web. An OnlineResource is basically
 * a URL coupled with identifying information to better understand what that URL
 * represents.
 *
 * The name field will typically represent a descriptive name but as a rule of
 * thumb will represent something more rigorous for some values of type:
 *      WFS - name will represent the typeName
 *      WMS - name will represent the layerName
 *      WCS - name will represent the coverageName
 *      OPeNDAP - name will represent the variable name
 */
Ext.define('portal.csw.OnlineResource', {
    extend: 'Ext.data.Model',

    //For generating unique ID's for each resource for easier referencing
    requires: ['Ext.data.SequentialIdGenerator'],
    idgen: 'sequential',

    //Static value representations of the 'type' field
    statics : {
        WMS : 'WMS', //represents a Web Map Service
        WFS : 'WFS', //represents a Web Feature Service
        WCS : 'WCS', //represents a Web Coverage Service
        WWW : 'WWW', //represents a regular HTTP web link
        OPeNDAP : 'OPeNDAP', //represents an OPeNDAP service

        /**
         * Static utility function for extracting a subset of OnlineResources from an array
         * according to a variety of filter options
         *
         * name - [Set to undefined to not filter] The name to filter by
         * description - [Set to undefined to not filter] The description to filter by
         * url - [Set to undefined to not filter] The url to filter by
         * strict - if set to true will filter the full url else only filter the host. Defaults to true
         * array - An array of portal.csw.OnlineResource objects
         */
        getFilteredFromArray : function(array, type, name, description, url, strict) {
            var filtered = [];
            for (var i = 0; i < array.length; i++) {
                var cmp = array[i];

                if (type !== undefined && cmp.get('type') !== type) {
                    continue;
                }

                if (name !== undefined && cmp.get('name') !== name) {
                    continue;
                }

                if (description !== undefined && cmp.get('description') !== description) {
                    continue;
                }

                if (url !== undefined && (strict === true || strict === undefined) && cmp.get('url') !== url) {
                    continue;
                }

                if (url !== undefined && strict === false && this.getHostname(cmp.url) !== this.getHostname(url)) {
                    var re = new RegExp('^(?:f|ht)tp(?:s)?\://([^/]+)', 'im');
                    var hostName = cmp.get('url').match(re)[1].toString();

                    if (url !== hostName) {
                        continue;
                    }
                }

                filtered.push(cmp);
            }
            return filtered;
        }
    },

    fields: [
        {name: 'id', type: 'string'}, //A unique auto generated ID for this OnlineResource
        {name: 'url', type: 'string'}, //A URL representing the location of the remote resource
        {name: 'name', type: 'string'}, //A name for this resource - it's use will vary depending on type (see comments at top of page)
        {name: 'description', type: 'string'}, //A human readable description of this resource
        {name: 'type', type: 'string'} //An enumerated type
    ]
});

