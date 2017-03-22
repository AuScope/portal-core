/**
 * Querier for WCS instances
 */
Ext.define('portal.layer.querier.coverage.WCSQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        this.callParent(arguments);
    },
    _generateErrorComponent : function(message) {
        return Ext.create('portal.layer.querier.BaseComponent', {
            html: Ext.util.Format.format('<p class="centeredlabel">{0}</p>', message)
        });
    },
    statics : {
        _parseArrayToString : function(arr, contentFunc){

            if (!arr || arr.length === 0) {
                return '';
            }

            if (!contentFunc) {
                contentFunc = function (item) {
                    return item;
                };
            }

            var description = '';
            for (var i = 0; i < arr.length; i++) {
                description += contentFunc(arr[i]);
                if (arr[i+1]) {
                    description += '<br/>';
                }
            }
            return description;
        }
    },



    query : function(queryTarget, callback) {
        var allOnlineResources = queryTarget.get('cswRecord').get('onlineResources');
        var opendapResources = portal.csw.OnlineResource.getFilteredFromArray(allOnlineResources, portal.csw.OnlineResource.OPeNDAP);

        portal.util.Ajax.request({
            url: 'describeCoverage.do',
            timeout : 180000,
            params      : {
                serviceUrl      : queryTarget.get('onlineResource').get('url'),
                layerName       : queryTarget.get('onlineResource').get('name')
                //cswRecord       : queryTarget.get('cswRecord')
            },
            scope : this,
            callback : function(success, data, message) {
                if (!success || !data) { //LJ: AUS-2598 ASTER mask hanging when server is down.
                    callback(this, [this._generateErrorComponent('There was a problem when looking up the coverage: ' + message)], queryTarget);
                    return;
                }
                
                if(success) {
                    var record = data[0];
                    var spatialFunc=function(item) {
                        var s = '';
                        if (item.type === 'Envelope' || item.type === 'EnvelopeWithTimePeriod') {
                            s += '[';
                            s += 'E' + item.eastBoundLongitude + ', ';
                            s += 'W' + item.westBoundLongitude + ', ';
                            s += 'N' + item.northBoundLatitude + ', ';
                            s += 'S' + item.southBoundLatitude;
                            s   += ']';
                        } else {
                            s += item.type;
                        }

                        return s;
                    };

                    var cmp= Ext.create('portal.layer.querier.BaseComponent',{  
                        overrideInfoWindowSize : {
                            width : 600,
                            height : 400
                        },
                        layout : 'fit',
                        items : [{
                            xtype : 'fieldset',
                            border : false,
                            autoScroll:true,
                            labelWidth: 75,
                            layout:'anchor',

                            items : [{
                                xtype : 'displayfield',
                                fieldLabel : 'name',
                                value : record.name
                            },{
                                xtype : 'displayfield',
                                fieldLabel : 'Description',
                                value : record.description
                            },{
                                xtype : 'displayfield',
                                fieldLabel : 'Label',
                                value : record.label
                            },{
                                xtype : 'displayfield',
                                fieldLabel : "Supported Request CRSs",
                                value : portal.layer.querier.coverage.WCSQuerier._parseArrayToString(record.supportedRequestCRSs)
                            },{
                                xtype : 'displayfield',
                                fieldLabel : "Supported Response CRSs",
                                value : portal.layer.querier.coverage.WCSQuerier._parseArrayToString(record.supportedResponseCRSs)
                            },{
                                xtype : 'displayfield',
                                fieldLabel : 'Supported Formats',
                                value : portal.layer.querier.coverage.WCSQuerier._parseArrayToString(record.supportedFormats)
                            },{
                                xtype : 'displayfield',
                                fieldLabel : 'Supported Interpolation',
                                value : portal.layer.querier.coverage.WCSQuerier._parseArrayToString(record.supportedInterpolations)
                            },{
                                xtype : 'displayfield',
                                fieldLabel : 'WMS URLs',
                                value : queryTarget.get('onlineResource').get('name') + '-' + queryTarget.get('onlineResource').get('url')
                            },{
                                xtype : 'displayfield',
                                fieldLabel : 'Spatial Domain',
                                value : portal.layer.querier.coverage.WCSQuerier._parseArrayToString(record.spatialDomain.envelopes,spatialFunc)
                            }]
                        }],
                        buttonAlign : 'right',
                        buttons : [{
                            text : 'Download WCS',
                            iconCls : 'download',
                            handler : function() {
                                var layer=queryTarget.get('layer');
                                var downloader=layer.get('downloader');
                                var renderedFilterer = layer.get('filterer').clone();
                                downloader.downloadData(layer, layer.getAllOnlineResources(), renderedFilterer, undefined);
                            }
                        },{
                            text : 'Download OPeNDAP',
                            iconCls : 'download',
                            hidden : opendapResources.length === 0,
                            handler : function() {
                                var opendapDownloader = Ext.create('portal.layer.downloader.coverage.OPeNDAPDownloader', {map : this.map});
                                opendapDownloader.downloadData(queryTarget.get('layer'), allOnlineResources, null, null);
                            }
                        }]
                    });
                    callback(this, [cmp], queryTarget);
                }
            }
        });


    }

});