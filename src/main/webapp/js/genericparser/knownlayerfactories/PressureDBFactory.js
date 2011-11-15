/**
 * A factory for parsing WFS features from the Pressure DB known layer.
 */
Ext.ns('GenericParser.KnownLayerFactory');
GenericParser.KnownLayerFactory.PressureDBFactory = Ext.extend(GenericParser.KnownLayerFactory.BaseFactory, {

    constructor : function(cfg) {
        GenericParser.KnownLayerFactory.PressureDBFactory.superclass.constructor.call(this, cfg);
    },

    /**
     * Overrides abstract supportsKnownLayer. Supports only the Pressure DB known layer
     */
    supportsKnownLayer : function(knownLayer) {
        return knownLayer.getId() === 'pressuredb-borehole';
    },

    retrieveAvailableOM : function(form, boreholeId, serviceUrl) {
        Ext.Ajax.request({
            url : 'pressuredb-getAvailableOM.do',
            params : {
                wellID : boreholeId,
                serviceUrl : serviceUrl
            },
            scope : this,
            success : function(response) {
                var responseObj = Ext.util.JSON.decode(response.responseText);

                if (responseObj && responseObj.success) {
                    var availableOmResponse = responseObj.data[0];

                    //Update our form based on our response
                    var checkBoxGroups = form.findByType('checkboxgroup');
                    checkBoxGroups[0].items.items[0].setDisabled(!availableOmResponse.temperatureT);

                    checkBoxGroups[1].items.items[0].setDisabled(!availableOmResponse.salinityTds);
                    checkBoxGroups[1].items.items[1].setDisabled(!availableOmResponse.salinityNacl);
                    checkBoxGroups[1].items.items[2].setDisabled(!availableOmResponse.salinityCl);

                    checkBoxGroups[2].items.items[0].setDisabled(!availableOmResponse.pressureRft);
                    checkBoxGroups[2].items.items[1].setDisabled(!availableOmResponse.pressureDst);
                    checkBoxGroups[2].items.items[2].setDisabled(!availableOmResponse.pressureFitp);
                }

                form.loadMask.hide();
            }
        });
    },

    /**
     * Overrides abstract parseKnownLayerFeature
     */
    parseKnownLayerFeature : function(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource, rootCfg) {
        var me = this;
        var pressureDbUrl = this.getBaseUrl(parentOnlineResource.url) + '/pressuredb-dataservices'; //This is a hack - somehow this needs to make it to the registry
        var featureId = featureId.replace('gsml.borehole.', '');

        //Load the form in full - when it renders we'll actually check what is available.
        //The user won't be able to interact with the form prior to load due to the loading mask
        Ext.apply(rootCfg, {
            border : false,
            items : [{
                xtype : 'form',
                width : 300,
                autoHeight : true,
                listeners : {
                    afterrender : function(form) {
                        form.loadMask = new Ext.LoadMask(form.el, {});
                        form.loadMask.show();
                        me.retrieveAvailableOM(form, featureId, pressureDbUrl);
                    }
                },
                items : [{
                    xtype : 'checkboxgroup',
                    fieldLabel: '<b>Temperature</b>',
                    columns : 1,
                    items : [{
                        boxLabel : 'Temperature',
                        name : 't',
                        disabled : true
                    }]
                },{
                    xtype : 'checkboxgroup',
                    fieldLabel: '<b>Salinity</b>',
                    columns : 1,
                    items : [{
                        boxLabel : 'Total Dissolved Solids',
                        name : 'tds',
                        disabled : true
                    },{
                        boxLabel : 'NaCl concentration',
                        name : 'nacl',
                        disabled : true
                    },{
                        boxLabel : 'Cl concentration',
                        name : 'cl',
                        disabled : true
                    }]
                },{
                    xtype : 'checkboxgroup',
                    fieldLabel: '<b>Pressure</b>',
                    columns : 1,
                    items : [{
                        boxLabel : 'Rock Formation Test',
                        name : 'rft',
                        disabled : true
                    },{
                        boxLabel : 'Drill Stem Test',
                        name : 'dst',
                        disabled : true
                    },{
                        boxLabel : 'Formation Interval Test Pressure',
                        name : 'fitp',
                        disabled : true
                    }]
                }],
                buttons : [{
                    xtype : 'button',
                    text : 'Refresh observations',
                    handler : function() {
                        var form = this.findParentByType('form');
                        form.loadMask.show();
                        me.retrieveAvailableOM(form, featureId, pressureDbUrl);
                    }
                },{
                    xtype : 'button',
                    text : 'Download selected observations',
                    handler : function() {
                        //We need to generate our download URL
                        var url = Ext.urlAppend('pressuredb-download.do', Ext.urlEncode({wellID : featureId}));
                        url = Ext.urlAppend(url, Ext.urlEncode({serviceUrl : pressureDbUrl}));

                        //Find the parent form - iterate the selected values
                        var parentForm = this.findParentByType('form');
                        var featuresAdded = 0;
                        for (feature in parentForm.getForm().getValues()) {
                            url = Ext.urlAppend(url, Ext.urlEncode({feature : feature}));
                            featuresAdded++;
                        }

                        if (featuresAdded > 0) {
                            FileDownloader.downloadFile(url);
                        } else {
                            Ext.Msg.show({
                                title:'No selection',
                                msg: 'No observations have been selected! You must select at least one before proceeding with a download.',
                                icon: Ext.MessageBox.WARNING
                             });
                        }
                    }
                }]
            }]
        });

        return new GenericParser.BaseComponent(rootCfg);
    }
});