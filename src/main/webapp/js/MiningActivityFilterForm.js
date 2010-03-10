/**
 * Builds a form panel for Mining Activity filters
 *
 * @param {number} the id of this formpanel instance
 * @param {string} the service url for submit
 */
MiningActivityFilterForm = function(id, serviceUrl) {
    /*var mineNamesStore = new Ext.data.Store({
        baseParams: {serviceUrl: serviceUrl},
        proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: '/getMineNames.do', timeout:180000})),
        reader: new Ext.data.JsonReader({
            root:'data'
        }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
    });*/

    Ext.FormPanel.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        width       : '100%',
        labelAlign  : 'right',
        labelWidth  : 150,
        timeout     : 180, //should not timeout before the server does
        //height: 300,
        //autoHeight: true,
        bodyStyle   : 'padding:5px',
        items: [{
            xtype:'fieldset',
            title: 'Mining Activity Filter Properties',
            autoHeight:true,
            defaultType: 'datefield',
            items :[
            {
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Associated Mine',
                name: 'mineName'
            }
            /*new Ext.form.ComboBox({
                anchor: '100%',
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
            })*/,{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Produced Material Name',
                name: 'producedMaterial'
            },{
                anchor: '100%',
                fieldLabel: 'Mining Activity Start Date',
                name: 'startDate',
                format: "d/M/Y",
                value: ''
            }, {
                anchor: '100%',
                fieldLabel: 'Mining Activity End Date',
                name: 'endDate',
                format: "d/M/Y",
                value: ''
            },{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Min. Ore Processed',
                name: 'oreProcessed'
            },{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Grade',
                name: 'cutOffGrade',
                hidden: true,
                hideLabel: true
            },{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Min. Production Amount',
                name: 'production'
            }]
        }]
        /*buttons: [{
            text: 'Show Me >>',
            handler: function() {
                preSubmitFunction();
                thePanel.getForm().submit({
                    url:submitUrl,
                    waitMsg:'Running query...',
                    params: {serviceUrl: serviceUrl},
                    success: successFunction,
                    failure: function(form, action) {
                        Ext.MessageBox.show({
                            title: 'Filter Failed',
                            msg: action.result.msg,
                            buttons: Ext.MessageBox.OK,
                            animEl: 'mb9',
                            icon: Ext.MessageBox.ERROR
                        });
                    }
                });
            }
        }]*/
    });
};

MiningActivityFilterForm.prototype = new Ext.FormPanel();