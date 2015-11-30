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

    //Static value representations of the 'type' field
    statics : {
        WMS : 'WMS', //represents a Web Map Service
        WFS : 'WFS', //represents a Web Feature Service
        WCS : 'WCS', //represents a Web Coverage Service
        WWW : 'WWW', //represents a regular HTTP web link
        OPeNDAP : 'OPeNDAP', //represents an OPeNDAP service
        FTP : 'FTP', //represents a File Transfer Protocol service
        SOS : 'SOS', //represents a SOS service
        UNSUPPORTED : 'Unsupported', //The backend doesn't recognise the type of service/protocol
        IRIS : 'IRIS', // IRIS web service
        CSWService : 'CSWService', // A CSW Service such as a GeoNetwork endpoint.
        NCSS : 'NCSS', // A NetCDF Subset Service.

        /**
         * Utility for turning the various portal.csw.OnlineResource.* enums into an English readable string.
         * Returns null if type isn't recognised
         */
        typeToString : function(type,version) {
            switch (type) {
            case portal.csw.OnlineResource.WWW:
            case portal.csw.OnlineResource.FTP:
                return 'Web Link';
            case portal.csw.OnlineResource.WFS:
                return 'OGC Web Feature Service 1.1.0';
            case portal.csw.OnlineResource.WMS:
                if(version=="1.3.0"){
                    return 'OGC Web Map Service 1.3.0';
                }else{
                    return 'OGC Web Map Service 1.1.1';
                }
            case portal.csw.OnlineResource.WCS:
                return 'OGC Web Coverage Service 1.0.0';
            case portal.csw.OnlineResource.OPeNDAP:
                return 'OPeNDAP Service';
            case portal.csw.OnlineResource.SOS:
                return 'Sensor Observation Service 2.0.0';
            case portal.csw.OnlineResource.IRIS:
                return 'IRIS Web Service';
            case portal.csw.OnlineResource.CSWService:
                return 'CSW Service';
            case portal.csw.OnlineResource.NCSS:
                return 'NetCDF Subset Service';
            // don't display a group for "Unsupported" resources, even though there might be information in there        
            case portal.csw.OnlineResource.UNSUPPORTED:
            default:
                return null;
            }
        },

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
            if(!array){
                return filtered;
            }
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
        {name: 'url', type: 'string'}, //A URL representing the location of the remote resource
        {name: 'name', type: 'string'}, //A name for this resource - it's use will vary depending on type (see comments at top of page)
        {name: 'description', type: 'string'}, //A human readable description of this resource
        {name: 'type', type: 'string'}, //An enumerated type
        {name: 'version' , type:'string'}
    ]
});

