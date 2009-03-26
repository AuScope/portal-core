//function FormFactory() {

    /**
     * Builds a form panel for Mine filters
     * @param id to specify the id of this formpanel instance
     * @param serviceUrl the service url for submit
     */
    function buildMineFilterForm(id, loadUrl, submitUrl, serviceUrl, successFunction, preSubmitFunction) {
        var mineNamesStore = new Ext.data.Store({
              baseParams: {serviceUrl: serviceUrl},
              proxy: new Ext.data.HttpProxy({url: '/getMineNames.do'}),
              reader: new Ext.data.JsonReader({
                  root:'data'
              }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
          });

        var thePanel = new Ext.FormPanel({
        //region: "center",
        //collapsible: true,
        //title: "Filter Properties",
        url: loadUrl,
        id: id,
        border: false,
        autoScroll:true,
        hideMode:'offsets',
        width: '100%',
        buttonAlign: 'right',
        items: [{
            xtype:'fieldset',
            title: 'Mine Filter Properties',

            autoHeight:true,
            items :[new Ext.form.ComboBox({
                fieldLabel: 'Mine Name',
                name: 'mineName',
                typeAhead: true,
                forceSelection: true,
                mode: 'remote',
                triggerAction: 'all',
                selectOnFocus: true,
                editable: true,
                xtype: 'combo',
                store: mineNamesStore,
                displayField:'mineDisplayName',
                valueField:'mineDisplayName'
            })
            ]
        }],
        buttons: [{
            text: 'Show Me >>',
            handler: function() {
                preSubmitFunction();
                thePanel.getForm().submit({
                    url:submitUrl,
                    waitMsg:'Running query...',
                    params: {serviceUrl: serviceUrl},
                    success: successFunction,
                    failure: function(form,action) {Ext.MessageBox.show({
                               title: 'Filter Failed',
                               msg: action.result.msg,
                               buttons: Ext.MessageBox.OK,
                               animEl: 'mb9',
                               icon: Ext.MessageBox.ERROR
                           });
                    }
                });
            }
        }]
    });
        return thePanel;
    };

      /**
     * Builds a form panel for Mining Activity filters
     * @param id to specify the id of this formpanel instance
     * @param serviceUrl the service url for submit
     */
    function buildMiningActivityFilterForm(id, loadUrl, submitUrl, serviceUrl, successFunction, preSubmitFunction) {
        var mineNamesStore = new Ext.data.Store({
              baseParams: {serviceUrl: serviceUrl},
              proxy: new Ext.data.HttpProxy({url: '/getMineNames.do'}),
              reader: new Ext.data.JsonReader({
                  root:'data'
              }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
          });

        var thePanel = new Ext.FormPanel({
        //region: "center",
        //collapsible: true,
        //title: "Filter Properties",
        url: loadUrl,
        id: id,
        border: false,
        autoScroll:true,
        hideMode:'offsets',
        width: '100%',
        buttonAlign: 'right',
        items: [{
            xtype:'fieldset',
            title: 'Mining Activity Filter Properties',

            autoHeight:true,

            defaultType: 'datefield',

            items :[new Ext.form.ComboBox({
                fieldLabel: 'Mine Name',
                name: 'mineName',
                typeAhead: true,
                forceSelection: true,
                mode: 'remote',
                triggerAction: 'all',
                selectOnFocus: true,
                editable: true,
                xtype: 'combo',
                store: mineNamesStore,
                displayField:'mineDisplayName',
                valueField:'mineDisplayName'
            }),{
                fieldLabel: 'Start Date',
                name: 'startDate',
                format: "d/M/Y",
                value: ''
            }, {
                fieldLabel: 'End Date',
                name: 'endDate',
                format: "d/M/Y",
                value: ''
            },{
                xtype: 'textfield',
                fieldLabel: 'Min. Ore Processed',
                name: 'oreProcessed'
            },{
                xtype: 'textfield',
                fieldLabel: 'Produced Material Name',
                name: 'producedMaterial'
            },{
                xtype: 'textfield',
                fieldLabel: 'Grade',
                name: 'cutOffGrade'
            },{
                xtype: 'textfield',
                fieldLabel: 'Min. Production Amount',
                name: 'production'
            }]
        }],
        buttons: [{
            text: 'Show Me >>',
            handler: function() {
                preSubmitFunction();
                thePanel.getForm().submit({
                    url:submitUrl,
                    waitMsg:'Running query...',
                    params: {serviceUrl: serviceUrl},
                    success: successFunction,
                    failure: function(form,action) {Ext.MessageBox.show({
                               title: 'Filter Failed',
                               msg: action.result.msg,
                               buttons: Ext.MessageBox.OK,
                               animEl: 'mb9',
                               icon: Ext.MessageBox.ERROR
                           });}
                });
            }
        }]
    });
        return thePanel;
    };

    /**
     * Builds a form panel for Mineral Occurrence filters
     * @param id to specify the id of this formpanel instance
     * @param serviceUrl the service url for submit
     */
    function buildMineralOccurrenceFilterForm(id, loadUrl, submitUrl, serviceUrl, successFunction, preSubmitFunction) {

        var thePanel = new Ext.FormPanel({
        //region: "center",
        //collapsible: true,
        //title: "Filter Properties",
        url: loadUrl,
        id: id,
        border: false,
        autoScroll:true,
        hideMode:'offsets',
        width: '100%',
        buttonAlign: 'right',
        items: [{
            xtype:'fieldset',
            title: 'Mineral Occurrence Filter Properties',

            autoHeight:true,
            defaultType: 'textfield',

            items :[{
                xtype: 'textfield',
                fieldLabel: 'Min. Ore Amount',
                name: 'minOreAmount'
            },{
                xtype: 'textfield',
                fieldLabel: 'Min. Commodity Amount',
                name: 'minCommodityAmount'
            },{
                xtype: 'textfield',
                fieldLabel: 'Cut Off Grade',
                name: 'minCutOffGrade'
            },{
                xtype: 'textfield',
                fieldLabel: 'Min. Production Amount',
                name: 'production'
            }]
        }],
        buttons: [{
            text: 'Show Me >>',
            handler: function() {
                preSubmitFunction();
                thePanel.getForm().submit({
                    url:submitUrl,
                    waitMsg:'Running query...',
                    params: {serviceUrl: serviceUrl},
                    success: successFunction,
                    failure: function(form,action) {Ext.MessageBox.show({
                               title: 'Filter Failed',
                               msg: action.result.msg,
                               buttons: Ext.MessageBox.OK,
                               animEl: 'mb9',
                               icon: Ext.MessageBox.ERROR
                           });}
                });
            }
        }]
    });
        return thePanel;
    };

//}