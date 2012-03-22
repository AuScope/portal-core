/**
 * A factory for parsing WFS features from the Geodesy known layer.
 */
Ext.define('portal.layer.querier.wfs.knownlayerfactories.GeodesyFactory', {
    extend : 'portal.layer.querier.wfs.knownlayerfactories.BaseFactory',

    constructor : function(cfg) {
        this.callParent(arguments);
    },

    /**
     * Overrides abstract supportsKnownLayer. Supports only the Geodesy known layer
     */
    supportsKnownLayer : function(knownLayer) {
        return knownLayer.getId() === 'geodesy:gnssstation';
    },

    _handleDateChange : function(field, newValue, oldValue, eOpts, store, wfsUrl, featureId) {
        var fieldSet = field.ownerCt;
        var to = fieldSet.getComponent('to');
        var from = fieldSet.getComponent('from');

        var formatDate = function(date) {
            return Ext.util.Format.format('{0}-{1}-{2}Z', date.getFullYear(), date.getMonth(), date.getDate());
        };

        store.setProxy({
            type : 'ajax',
            url : 'getGeodesyObservations.do',
            extraParams : {
                serviceUrl : wfsUrl,
                stationId : featureId,
                startDate : formatDate(from.getValue()),
                endDate : formatDate(to.getValue())
            },
            reader : {
                type : 'json',
                root : 'data'
            }
        });

        store.load();
    },


    /**
     * Overrides abstract parseKnownLayerFeature
     */
    parseKnownLayerFeature : function(featureId, parentKnownLayer, parentOnlineResource) {
        var me = this;

        var store = Ext.create('Ext.data.Store', {
            model : 'portal.knownlayer.geodesy.Observation',
            autoLoad : false,
            proxy : {
                type : 'ajax',
                url : 'getGeodesyObservations.do',
                extraParams : {
                    serviceUrl : parentOnlineResource.get('url'),
                    stationId : featureId
                },
                reader : {
                    type : 'json',
                    root : 'data'
                }
            }
        });

        //Load the form in full - when it renders we'll actually check what is available.
        //The user won't be able to interact with the form prior to load due to the loading mask
        return Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            tabTitle : 'Rinex Files',
            items : [{
                xtype : 'fieldset',
                title : 'Rinex files for date range',
                items : [{
                    xtype : 'datefield',
                    itemId : 'from',
                    fieldLabel: 'From',
                    format: 'Y-m-d',
                    name : 'startDate',
                    value: new Date(),  // defaults to today
                    listeners : {
                        change : Ext.bind(this._handleDateChange, this, [store, parentOnlineResource.get('url'), featureId], true)
                    }
                },{
                    xtype: 'datefield',
                    itemId : 'to',
                    format: 'Y-m-d',
                    fieldLabel: 'To',
                    name: 'endDate',
                    value: new Date(),  // defaults to today
                    listeners : {
                        change : Ext.bind(this._handleDateChange, this, [store, parentOnlineResource.get('url'), featureId], true)
                    }
                }]
            },{
                xtype : 'grid',
                store : store,
                height : 185,
                columns : [{
                    //Title column
                    text : 'Date',
                    dataIndex : 'date',
                    width : 100,
                },{
                    //Service information column
                    text : 'URL',
                    dataIndex : 'url',
                    flex: 1,
                    renderer : function(value) {
                        return Ext.DomHelper.markup({
                            tag : 'a',
                            target : '_blank',
                            href : value,
                            html : value
                        });
                    }
                }]
            }]
        });
    }
});