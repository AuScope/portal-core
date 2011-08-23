/**
 * A Ext.grid.GridPanel specialisation for allowing the user to browse
 * the online resource contents of a set of CSWRecords
 */
CSWResourcesGrid = Ext.extend(Ext.grid.GridPanel, {

    cswRecords : null,

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.grid.GridPanel as well as the following values
     * {
     *  cswRecords : Array of CSWRecord objects or a single CSWRecord object
     * }
     */
    constructor : function(cfg) {

        if (Ext.isArray(cfg.cswRecords)) {
            cfg.cswRecords = cfg.cswRecords;
        } else {
            cfg.cswRecords = [cfg.cswRecords];
        }
        this.cswRecords = cfg.cswRecords;

        //Generate our flattened 'data items' list for rendering to the grid
        var dataItems = [];
        for (var i = 0; i < this.cswRecords.length; i++) {
            var onlineResources = this.cswRecords[i].getOnlineResources();
            for (var j = 0; j < onlineResources.length; j++) {

                //ensure we have a type we want to describe
                switch (onlineResources[j].onlineResourceType) {
                case 'WWW':
                    break;
                case 'WFS':
                    break;
                case 'WMS':
                    break;
                case 'WCS':
                    break;
                default:
                    continue;//don't include anything else
                }


                dataItems.push([
                    onlineResources[j].name,
                    onlineResources[j].description,
                    onlineResources[j].url,
                    onlineResources[j],
                    onlineResources[j].onlineResourceType,
                    i
                ]);
            }
        }

        //Build our configuration object
        Ext.apply(cfg, {
            store : new Ext.data.GroupingStore({
                autoDestroy     : true,
                groupField      : 'type',
                sortInfo        : {
                    field           : 'name',
                    direction       : 'ASC'
                },
                reader : new Ext.data.ArrayReader({
                    fields : [
                        'name',
                        'description',
                        'url',
                        'preview',
                        'type',
                        'cswRecordIndex'
                    ]
                }),
                data : dataItems
            }),
            hideHeaders : true,
            autoHeight: true,
            autoExpandColumn: 'text',
            viewConfig : {
                templates: {
                    cell: new Ext.Template(
                        '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>',
                        '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>',
                        '</td>')
                }
            },
            columns: [{
                id : 'text',
                header : 'Text',
                dataIndex: 'name',
                menuDisabled: true,
                scope : this,
                sortable: true,
                renderer: function(value, metadata, record) {
                    var name = record.get('name');
                    var description = record.get('description');
                    var cswRecord = this.cswRecords[record.get('cswRecordIndex')];
                    var onlineRes = record.get('preview');

                    //Ensure there is a title (even it is just '<Untitled>'
                    if (!name || name.length === 0) {
                        name = '&gt;Untitled&lt;';
                    }

                    //Adjust our name with our service type (if appopriate)
                    switch(record.get('type')) {
                    case 'WFS':
                        name += ' [Web Feature Service]';
                        break;
                    case 'WMS':
                        name += ' [Web Map Service]';
                        break;
                    case 'WCS':
                        name += ' [Web Coverage Service]';
                        break;
                    }

                    //Truncate description
                    var maxLength = 190;
                    if (description.length > maxLength) {
                        description = description.substring(0, maxLength) + '...';
                    }

                    switch(record.get('type')) {
                    case 'WWW':
                        return '<a target="_blank" href="' + onlineRes.url + '"><b>' + name + '</b></a><br/><span style="color:#555;">' + description + '</span>';
                    default:
                        return '<b>' + name + '</b><br/><span style="color:#555;">' + description + '</span>';
                    }
                }
            },{
                id : 'preview',
                header : 'Preview',
                dataIndex: 'preview',
                scope: this,
                width: 140,
                sortable: false,
                menuDisabled: true,
                renderer: function(value, metadata, record) {
                    var onlineRes = value;
                    var cswRecord = this.cswRecords[record.get('cswRecordIndex')];

                    //We preview types differently
                    switch(record.get('type')) {
                    case 'WFS':
                        var getFeatureUrl = onlineRes.url + this.internalURLSeperator(onlineRes.url) + 'SERVICE=WFS&REQUEST=GetFeature&VERSION=1.1.0&maxFeatures=5&typeName=' + onlineRes.name;
                        return '<a target="_blank" href="' + getFeatureUrl + '"><p>First 5 features</p></a>';
                    case 'WCS':
                        var describeCoverageUrl = onlineRes.url + this.internalURLSeperator(onlineRes.url) + 'SERVICE=WCS&REQUEST=DescribeCoverage&VERSION=1.0.0&coverage=' + onlineRes.name;
                        return '<a target="_blank" href="' + describeCoverageUrl + '"><p>DescribeCoverage response</p></a>';
                    case 'WMS':
                        //Form the WMS url
                        var getMapUrl = onlineRes.url + this.internalURLSeperator(onlineRes.url) + 'SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.1&LAYERS=' + onlineRes.name;
                        getMapUrl += '&SRS=EPSG:4326&FORMAT=image/png&STYLES=';

                        //To generate the url we will need to use the bounding box to make the request
                        //To avoid distortion, we also scale the width height independently
                        var geoEls = cswRecord.getGeographicElements();
                        if (geoEls && geoEls.length > 0) {
                            var superBbox = geoEls[0];
                            for (var i = 1; i < geoEls.length; i++) {
                                superBbox = superBbox.combine(geoEls[i]);
                            }

                            var superBboxStr = superBbox.westBoundLongitude + "," +
                                                superBbox.southBoundLatitude + "," +
                                                superBbox.eastBoundLongitude + "," +
                                                superBbox.northBoundLatitude;

                            //Set our width to a constant and scale the height appropriately
                            var heightRatio = (superBbox.northBoundLatitude - superBbox.southBoundLatitude) /
                                              (superBbox.eastBoundLongitude - superBbox.westBoundLongitude);
                            var width = 512;
                            var height = Math.floor(width * heightRatio);

                            getMapUrl += '&WIDTH=' + width;
                            getMapUrl += '&HEIGHT=' + height;
                            getMapUrl += '&BBOX=' + superBboxStr;

                            var thumbWidth = width;
                            var thumbHeight = height;

                            //Scale our thumbnail appropriately
                            if (thumbWidth > 128) {
                                thumbWidth = 128;
                                thumbHeight = thumbWidth * heightRatio;
                            }

                            return '<a target="_blank" href="' + getMapUrl + '"><img width="' + thumbWidth + '" height="' + thumbHeight + '" alt="Loading preview..." src="' + getMapUrl + '"/></a>';
                        }
                        return 'Unable to preview WMS';
                    default :
                        return '';
                    }
                }
            }]
        });

        //Call parent constructor
        CSWResourcesGrid.superclass.constructor.call(this, cfg);
    },

    /**
     * Given a URL this will determine the correct character that can be appended
     * so that a number of URL parameters can also be appended
     *
     * See AUS-1931 for why this function should NOT exist
     */
    internalURLSeperator : function(url) {
        var lastChar = url[url.length - 1];
        if (lastChar == '?') {
            return '';
        } else if (lastChar == '&') {
            return '';
        } else if (url.indexOf('?') >= 0) {
            return '&';
        } else {
            return '?';
        }
    }
});

Ext.reg('cswresourcesgrid', CSWResourcesGrid);