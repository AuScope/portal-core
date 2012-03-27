/**
 * A Panel specialisation for allowing the user to browse
 * the online resource contents of a set of portal.csw.OnlineResource
 * objects.
 */
Ext.define('portal.widgets.panel.OnlineResourcePanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.onlineresourcepanel',

    cswRecords : null, //Array of portal.csw.CSWRecord objects

    /**
     * Accepts all Ext.grid.Panel options as well as
     * {
     *  cswRecords : single instance of array of portal.csw.CSWRecord objects
     * }
     */
    constructor : function(cfg) {
        if (Ext.isArray(cfg.cswRecords)) {
            this.cswRecords = cfg.cswRecords;
        } else {
            this.cswRecords = [cfg.cswRecords];
        }

        //Generate our flattened 'data items' list for rendering to the grid
        var dataItems = [];
        for (var i = 0; i < this.cswRecords.length; i++) {
            var onlineResources = this.cswRecords[i].get('onlineResources');
            for (var j = 0; j < onlineResources.length; j++) {
                var group = '';

                //ensure we have a type we want to describe
                switch (onlineResources[j].get('type')) {
                case portal.csw.OnlineResource.WWW:
                    group = 'Web Link';
                    break;
                case portal.csw.OnlineResource.WFS:
                    group = 'OGC Web Feature Service 1.1.0';
                    break;
                case portal.csw.OnlineResource.WMS:
                    group = 'OGC Web Map Service 1.1.1';
                    break;
                case portal.csw.OnlineResource.WCS:
                    group = 'OGC Web Coverage Service 1.0.0';
                    break;
                case portal.csw.OnlineResource.OPeNDAP:
                    group = 'OPeNDAP Service';
                    break;
                default:
                    continue;//don't include anything else
                }

                dataItems.push({
                    group : group,
                    onlineResource : onlineResources[j],
                    cswRecord : this.cswRecords[i]
                });
            }
        }

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
        });

        //Build our configuration object
        Ext.apply(cfg, {
            features : [groupingFeature],
            store : Ext.create('Ext.data.Store', {
                groupField : 'group',
                fields : [
                    {name : 'group', type: 'string'},
                    {name : 'onlineResource', type: 'auto'},
                    {name : 'cswRecord', type: 'auto'}
                ],
                data : dataItems
            }),
            hideHeaders : true,
            viewConfig : {
                tpl: new Ext.Template(
                        '<td class="x-grid-cell x-grid-cell-gridcolumn-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
                        '<div class="x-grid-cell-inner x-selectable" {attr}>{value}</div>',
                        '</td>')
            },
            columns: [{
                //Title column
                dataIndex: 'onlineResource',
                menuDisabled: true,
                sortable: true,
                flex: 1,
                renderer: Ext.bind(this._titleRenderer, this)
            },{
                dataIndex: 'onlineResource',
                width: 140,
                renderer: Ext.bind(this._previewRenderer, this)
            }]
        });

        this.callParent(arguments);
    },

    _titleRenderer : function(value, metaData, record, row, col, store, gridView) {
        var onlineResource = record.get('onlineResource');
        var cswRecord = record.get('cswRecord');
        var name = onlineResource.get('name');
        var url = onlineResource.get('url');
        var description = onlineResource.get('description');

        //Ensure there is a title (even it is just '<Untitled>'
        if (!name || name.length === 0) {
            name = '&gt;Untitled&lt;';
        }

        //Truncate description
        var maxLength = 190;
        if (description.length > maxLength) {
            description = description.substring(0, maxLength) + '...';
        }

        //Render our HTML
        switch(onlineResource.get('type')) {
        case portal.csw.OnlineResource.WWW:
            return Ext.util.Format.format('<div class="x-selectable"><a target="_blank" href="{0}"><b>{1}</b></a><br/><span style="color:#555;">{2}</span></div>', url, name, description);
        default:
            return Ext.util.Format.format('<div class="x-selectable"><b>{0}</b><br/><span style="color:#555;">{1}<br/>{2}</span></div>', name, url, description);
        }
    },

    _previewRenderer : function(value, metaData, record, row, col, store, gridView) {
        var onlineRes = record.get('onlineResource');
        var cswRecord = record.get('cswRecord');
        var url = onlineRes.get('url');
        var name = onlineRes.get('name');

        //We preview types differently
        switch(onlineRes.get('type')) {
        case portal.csw.OnlineResource.WFS:
            var getFeatureUrl = url + this.internalURLSeperator(url) + 'SERVICE=WFS&REQUEST=GetFeature&VERSION=1.1.0&maxFeatures=5&typeName=' + name;
            return Ext.DomHelper.markup({
                tag : 'a',
                target : '_blank',
                href : getFeatureUrl,
                html : 'First 5 features'
            });
        case portal.csw.OnlineResource.WCS:
            var describeCoverageUrl = url + this.internalURLSeperator(url) + 'SERVICE=WCS&REQUEST=DescribeCoverage&VERSION=1.0.0&coverage=' + name;
            return Ext.DomHelper.markup({
                tag : 'a',
                target : '_blank',
                href : describeCoverageUrl,
                html : 'DescribeCoverage response'
            });
        case portal.csw.OnlineResource.OPeNDAP:
            return Ext.DomHelper.markup({
                tag : 'a',
                target : '_blank',
                href : url + '.html',
                html : 'OPeNDAP Data access form'
            });
        case portal.csw.OnlineResource.WMS:
            //Form the WMS url
            var getMapUrl = url + this.internalURLSeperator(url) + 'SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.1&LAYERS=' + name;
            getMapUrl += '&SRS=EPSG:4326&FORMAT=image/png&STYLES=';

            //To generate the url we will need to use the bounding box to make the request
            //To avoid distortion, we also scale the width height independently
            var geoEls = cswRecord.get('geographicElements');
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

                return Ext.DomHelper.markup({
                    tag : 'a',
                    target : '_blank',
                    href : getMapUrl,
                    children : [{
                        tag : 'img',
                        width : thumbWidth,
                        height : thumbHeight,
                        alt : 'Loading preview...',
                        src : getMapUrl
                    }]
                });
            }
            return 'Unable to preview WMS';
        default :
            return '';
        }
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
        } else if (lastChar === '&') {
            return '';
        } else if (url.indexOf('?') >= 0) {
            return '&';
        } else {
            return '?';
        }
    }
});
