/**
 * Builds a form panel for Mining Activity filters
 *
 * @param {number} the id of this formpanel instance
 * @param {string} the service url for submit
 */
MiningActivityFilterForm = function(id) {
    /*var mineNamesStore = new Ext.data.Store({
        baseParams: {serviceUrl: serviceUrl},
        proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: 'getMineNames.do', timeout:180000})),
        reader: new Ext.data.JsonReader({
            root:'data'
        }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
    });*/
	
    //-----------Commodities

    var commodityStore = new Ext.data.SimpleStore({
        fields   : ['urn', 'label'],
        proxy    : new Ext.data.HttpProxy({url: 'getAllCommodities.do'}),
        sortInfo : {field:'label',order:'ASC'},
        reader : new Ext.data.ArrayReader({}, [
            { name:'urn'   },
            { name:'label' }
        ])
    });
    
    commodityStore.reload();
    
    var producedMaterialCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{label}" class="x-combo-list-item">{label}</div></tpl>',
        anchor         : '100%',
        name           : 'producedMaterialDisplayed', /* this just returns the values from displayField! */
        hiddenName     : 'producedMaterial',         /* this returns the values from valueField! */
        fieldLabel     : 'Produced Material',
        labelAlign     : 'right',
        forceSelection : true,
        mode           : 'local',
        /*selectOnFocus: true,*/
        store          : commodityStore,
        triggerAction  : 'all',
        typeAhead      : true,
        displayField   :'label',   /* change tpl field to this value as well! */
        valueField     :'label'
    });	
	
	
    Ext.FormPanel.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        width       : '100%',
        labelAlign  : 'right',
        labelWidth  : 130,
        timeout     : 180, //should not timeout before the server does
        //height: 300,
        //autoHeight: true,
        bodyStyle   : 'padding:5px',
        items: [{
            xtype:'fieldset',
            title: 'Mining Activity Filter Properties',
            //autoHeight:true,
            defaultType: 'textfield',
            defaults: {anchor: '100%'},
            items :[
            {
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
            })*/
            ,producedMaterialCombo
            ,{
                xtype: 'datefield',
                fieldLabel: 'Activity Start Date',
                name: 'startDate',
                format: "Y-m-d",
                value: ''
            },{
                xtype: 'datefield',
                fieldLabel: 'Activity End Date',
                name: 'endDate',
                format: "Y-m-d",
                value: ''
            },{
                fieldLabel: 'Min. Ore Processed',
                name: 'oreProcessed'
            },{
                fieldLabel: 'Min. Prod. Amount',
                name: 'production'
            },{                
                fieldLabel: 'Grade',
                name: 'cutOffGrade',
                hidden: true,
                hideLabel: true
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