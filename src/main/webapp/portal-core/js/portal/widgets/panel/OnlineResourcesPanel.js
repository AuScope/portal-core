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
     *  parentRecord: The KnownLayer/KML/CSWRecord that owns these records
     *  nagiosErrorIcon: Error icon to show when rendering resources from a host that nagios has declared as "down"
     *  allow
     * }
     */
    constructor : function(cfg) {
        // Ensures this.cswRecords is an array:
        this.cswRecords = [].concat(cfg.cswRecords);
        
        this.nagiosErrorIcon = Ext.isEmpty(cfg.nagiosErrorIcon) ? 'portal-core/img/warning.png' : cfg.nagiosErrorIcon;

        //Generate our flattened 'data items' list for rendering to the grid
        var dataItems = portal.widgets.panel.OnlineResourcePanelRow.parseCswRecords(this.cswRecords);

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
        });

        //The following two Configs variables can be set by the owner
        var sortable = true;
        var hideHeaders = true;
        if (typeof(cfg.hideHeaders) !== 'undefined' && cfg.hideHeaders != null) {
            hideHeaders = cfg.hideHeaders;
        }
        if (typeof(cfg.sortable) !== 'undefined' && cfg.sortable != null) {
            sortable = cfg.sortable;
        }
        
        //Build up our set of "problem" hosts for marking with a special icon
        this.problemHosts = [];
        if (cfg.parentRecord && 
            cfg.parentRecord instanceof portal.knownlayer.KnownLayer && 
            cfg.parentRecord.containsNagiosFailures()) {
            this.problemHosts = cfg.parentRecord.get('nagiosFailingHosts');
        }

        //We allow the owner to specify additional columns
        var columns = [{
            //Title column
            dataIndex: 'onlineResource',
            menuDisabled: true,
            sortable: sortable,
            flex: 1,
            renderer: Ext.bind(this._titleRenderer, this)
        },{
            width: 55,
            dataIndex: 'onlineResource',
            menuDisabled: true,
            renderer: Ext.bind(this._errorRenderer, this)
        },{
            dataIndex: 'onlineResource',
            width: 140,
            renderer: Ext.bind(this._previewRenderer, this)
        }];
        if (cfg.columns) {
            columns = columns.concat(cfg.columns);
        }

        //Build our configuration object
        Ext.apply(cfg, {
            selModel: cfg.selModel,
            features : [groupingFeature],
            store : Ext.create('Ext.data.Store', {
                groupField : 'group',
                model : 'portal.widgets.panel.OnlineResourcePanelRow',
                data : dataItems
            }),
            plugins : [{
                ptype : 'selectablegrid'
            }],
            hideHeaders : hideHeaders,
            columns: columns,
            viewConfig: {
                enableTextSelection: true
              }
        });

        this.callParent(arguments);
    },
    
    _errorRenderer : function(value, metaData, record, row, col, store, gridView) {
        var onlineResource = record.get('onlineResource');
        var url = onlineResource.get('url');
        var matchesError = false;
        Ext.each(this.problemHosts, function(host) {
            if (url.indexOf(host) >= 0) {
                matchesError = true;
                return false;
            }
        })
        
        if (matchesError) {
            return Ext.DomHelper.markup({
                tag : 'img',
                src: this.nagiosErrorIcon,
                width: 32,
                height: 32,
                style: 'margin-top:6px;',
                title: 'This service is reported to be experiencing issues at the moment. Some aspects of this layer may not load/work.'
            });
        }
        
        return '';
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
        case portal.csw.OnlineResource.FTP:
        case portal.csw.OnlineResource.IRIS:
        case portal.csw.OnlineResource.UNSUPPORTED:
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{
                    tag : 'a',
                    target : '_blank',
                    href : url,
                    children : [{
                        tag : 'b',
                        html : name
                    }]
                },{
                    tag : 'br'
                },{
                    tag : 'span',
                    style : {
                        color : '#555'
                    },
                    html : description
                }]
            });
        default:
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{
                    tag : 'b',
                    html : name
                },{
                    tag : 'br'
                },{
                    tag : 'span',
                    style : {
                        color : '#555'
                    },
                    children : [{
                        html : url
                    },{
                        html : description
                    }]
                }]
            });
        }
    },

    _previewRenderer : function(value, metaData, record, row, col, store, gridView) {
        var onlineRes = record.get('onlineResource');
        var cswRecord = record.get('cswRecord');
        var url = onlineRes.get('url');
        var name = onlineRes.get('name');
        var description = onlineRes.get('description');

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
        case portal.csw.OnlineResource.SOS:
            var getObservations = url + this.internalURLSeperator(url) + 'SERVICE=SOS&REQUEST=GetObservation&VERSION=2.0.0&OFFERING=' + escape(name) + '&OBSERVEDPROPERTY=' + escape(description) + '&RESPONSEFORMAT=' + escape('http://www.opengis.net/om/2.0');
            return Ext.DomHelper.markup({
                tag : 'a',
                target : '_blank',
                href : getObservations,
                html : 'Observations for ' + description
            });
        case portal.csw.OnlineResource.WMS:

            //To generate the url we will need to use the bounding box to make the request
            //To avoid distortion, we also scale the width height independently
            var geoEls = cswRecord.get('geographicElements');
            if (geoEls && geoEls.length > 0) {
                var superBbox = geoEls[0];
                for (var i = 1; i < geoEls.length; i++) {
                    superBbox = superBbox.combine(geoEls[i]);
                }

                //Set our width to a constant and scale the height appropriately
                var heightRatio = (superBbox.northBoundLatitude - superBbox.southBoundLatitude) /
                                  (superBbox.eastBoundLongitude - superBbox.westBoundLongitude);
                var width = 512;
                var height = Math.floor(width * heightRatio);

                var thumbWidth = width;
                var thumbHeight = height;

                //Scale our thumbnail appropriately
                if (thumbWidth > 128) {
                    thumbWidth = 128;
                    thumbHeight = thumbWidth * heightRatio;
                }

                var getMapUrl = '';
                if(cswRecord.get('version')=='1.3.0'){
                    getMapUrl = portal.map.primitives.BaseWMSPrimitive.getWms_130_Url(url, name, superBbox, width, height);
                    console.log("1.1.1:" + portal.map.primitives.BaseWMSPrimitive.getWmsUrl(url, name, superBbox, width, height));
                    console.log("1.3.0:"+portal.map.primitives.BaseWMSPrimitive.getWms_130_Url(url, name, superBbox, width, height));
                }else{
                    getMapUrl = portal.map.primitives.BaseWMSPrimitive.getWmsUrl(url, name, superBbox, width, height);
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
/**
 * Convenience class for representing the rows in the OnlineResourcesPanel
 */
Ext.define('portal.widgets.panel.OnlineResourcePanelRow', {
    extend : 'Ext.data.Model',

    statics : {
        /**
         * Turns an array of portal.csw.CSWRecord objects into an equivalent array of
         * portal.widgets.panel.OnlineResourcePanelRow objects
         */
        parseCswRecords : function(cswRecords) {
            var dataItems = [];
            for (var i = 0; i < cswRecords.length; i++) {
                var onlineResources = cswRecords[i].getAllChildOnlineResources();
                for (var j = 0; j < onlineResources.length; j++) {

                    //ensure we have a type we want to describe
                    var group = portal.csw.OnlineResource.typeToString(onlineResources[j].get('type'),onlineResources[j].get('version'));
                    if (!group) {
                        continue; //don't include anything else
                    }

                    dataItems.push(Ext.create('portal.widgets.panel.OnlineResourcePanelRow',{
                        group : group,
                        onlineResource : onlineResources[j],
                        cswRecord : cswRecords[i]
                    }));
                }
            }

            return dataItems;
        }
    },

    fields: [
             {name : 'group', type: 'string'},
             {name : 'onlineResource', type: 'auto'},
             {name : 'cswRecord', type: 'auto'}
    ]
});