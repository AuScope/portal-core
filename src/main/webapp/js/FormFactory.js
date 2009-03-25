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
            title: 'Mine Filters',
            autoHeight:true,
            //defaults: {width: '100%'},
            //defaultType: 'textfield',
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
        },{
            xtype:'fieldset',
            title: 'Mining Activity Filters',
            //collapsible: true,
            autoHeight:true,
            //defaults: {width: '100%'},
            defaultType: 'datefield',

            items :[{
                fieldLabel: 'Start Date',
                name: 'startDate',
                format: "d-m-Y",
                value: ''
            }, {
                fieldLabel: 'End Date',
                name: 'endDate',
                format: "d-m-Y",
                value: ''
            }

            ]
        },{
            xtype:'fieldset',
            title: 'Commodity Filters',
            autoHeight:true,
            defaultType: 'textfield',
            items :[{
                fieldLabel: 'Ore Processed',
                name: 'oreProcessed'
            },{
                fieldLabel: 'Produced Material',
                name: 'producedMaterial'
            },{
                fieldLabel: 'Cut Off Grade',
                name: 'cutOffGrade'
            },{
                fieldLabel: 'Production',
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
                    success: successFunction
                });
            }
        }]
    });
        return thePanel;
    };

//}