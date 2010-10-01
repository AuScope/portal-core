/**
 * Builds a form panel for Mine filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */
MineFilterForm = function(id) {
    /*
    var mineNamesStore = new Ext.data.Store({
        baseParams: {serviceUrl: serviceUrl},
        proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: 'getMineNames.do', timeout:180000})),
        reader: new Ext.data.JsonReader({
            root:'data'
        }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
    });*/
    
    Ext.FormPanel.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    :'offsets',
        width       :'100%',
        buttonAlign :'right',
        labelAlign  :'right',
        labelWidth  : 70,
        timeout     : 180, //should not time out before the server does
        bodyStyle   :'padding:5px',
        autoHeight: true,
        items       : [{
            xtype      :'fieldset',
            title      : 'Mine Filter Properties',
            autoHeight : true,
            items      : [
            {
                anchor     : '100%',
                xtype      : 'textfield',
                fieldLabel : 'Mine Name',
                name       : 'mineName'
            }
            /*new Ext.form.ComboBox({
                anchor: '100%',
                autoWidth: true,
                name: 'mineName',
                displayField:'mineDisplayName',
                editable: true,
                fieldLabel: 'Mine Name',
                forceSelection: true,
                listWidth: 300,            // 'auto' does not work in IE6
                mode: 'remote',
                selectOnFocus: true,
                store: mineNamesStore,
                triggerAction: 'all',
                typeAhead: true,
                valueField:'mineDisplayName',
                xtype: 'combo'
            })*/
            ]
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
    //return thePanel;
};

MineFilterForm.prototype = new Ext.FormPanel();
