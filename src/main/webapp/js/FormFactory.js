//function FormFactory() {

/*Ext.override(Ext.form.Field, {
    hideItem :function(){
        this.formItem.addClass('x-hide-' + this.hideMode);
    },

    showItem: function(){
        this.formItem.removeClass('x-hide-' + this.hideMode);
    },
    setFieldLabel: function(text) {
    var ct = this.el.findParent('div.x-form-item', 3, true);
    var label = ct.first('label.x-form-item-label');
    label.update(text);
  }
});*/



var getFilterForm = function(record) {
    switch (record.get('typeName')) {
        case 'er:Mine': return buildMineFilterForm(record.get('id'), record.get('serviceURLs')[0]); break;
        case 'er:MiningActivity': return buildMiningActivityFilterForm(record.get('id'), record.get('serviceURLs')[0]); break;
        case 'er:MineralOccurrence': return buildMineralOccurrenceFilterForm(record.get('id')); break;
        default: return null; break;
    }
};

/**
 * Builds a form panel for Mine filters
 * @param id to specify the id of this formpanel instance
 * @param serviceUrl the service url for submit
 */
var buildMineFilterForm = function(id, serviceUrl) {
    var mineNamesStore = new Ext.data.Store({
        baseParams: {serviceUrl: serviceUrl},
        proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: '/getMineNames.do', timeout:180000})),
        reader: new Ext.data.JsonReader({
            root:'data'
        }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
    });

    var thePanel = new Ext.FormPanel({
        //region: "center",
        //collapsible: true,
        //title: "Filter Properties",
        //url: loadUrl,
        id: id,
        border: false,
        autoScroll:true,
        hideMode:'offsets',
        width: '100%',
        buttonAlign: 'right',
        labelAlign: 'right',
        labelWidth: 60,

        //labelWidth: 140,
        timeout: 180, //should not time out before the server does

        items: [{
            xtype:'fieldset',
            title: 'Mine Filter Properties',

            autoHeight:true,
            anchor: '100%',
            
            items :[
            {
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Mine Name',
                name: 'mineName'
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
    return thePanel;
};

/**
 * Builds a form panel for Mining Activity filters
 * @param id to specify the id of this formpanel instance
 * @param serviceUrl the service url for submit
 */
var buildMiningActivityFilterForm = function(id, serviceUrl) {
    var mineNamesStore = new Ext.data.Store({
        baseParams: {serviceUrl: serviceUrl},
        proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: '/getMineNames.do', timeout:180000})),
        reader: new Ext.data.JsonReader({
            root:'data'
        }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
    });

    var thePanel = new Ext.FormPanel({
        //region: "center",
        //collapsible: true,
        //title: "Filter Properties",
        //url: loadUrl,
        id: id,
        border: false,
        autoScroll:true,
        hideMode:'offsets',
        width: '100%',
        buttonAlign: 'right',
        labelAlign: 'right',
        labelWidth: 140,
        timeout: 180, //should not timeout before the server does

        items: [{
            xtype:'fieldset',
            title: 'Mining Activity Filter Properties',

            autoHeight:true,
            anchor: '100%',

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
    return thePanel;
}
;

/**
 * Builds a form panel for Mineral Occurrence filters
 * @param id to specify the id of this formpanel instance
 * @param serviceUrl the service url for submit
 */
var buildMineralOccurrenceFilterForm = function(id) {
    unitsOfMeasure = [
            ['CRT', 'urn:ogc:def:uom:UCUM::%5Bcar_m%5D'],
            ['CUB M/HA', 'urn:ogc:def:uom:UCUM::m3.har-1'],
            ['CUB M', 'urn:ogc:def:uom:UCUM::m3'],
            ['TONNE', 'urn:ogc:def:uom:UCUM::t'],
            ['TONNE/M', 'urn:ogc:def:uom:UCUM::t.m-1'],
            ['TONNE/100M', 'urn:ogc:def:uom:UCUM::t.hm-1'],
            ['GM/TONNE', 'urn:ogc:def:uom:UCUM::g.t-1'],
            ['KG/TONNE', 'urn:ogc:def:uom:UCUM::kg.t-1'],
            ['MILL TONNE', 'urn:ogc:def:uom:UCUM::Mt'],
            ['GM', 'urn:ogc:def:uom:UCUM::g'],
            ['KG', 'urn:ogc:def:uom:UCUM::kg'],
            ['M', 'urn:ogc:def:uom:UCUM::m'],
            ['%', 'urn:ogc:def:uom:UCUM::%25'],
            ['UKN', 'urn:ogc:def:nil:OGC::missing'],
            ['SQ M', 'urn:ogc:def:uom:UCUM::m2'],
            ['MA', 'urn:ogc:def:uom:UCUM::Ma'],
            ['NOUNIT', 'urn:ogc:def:nil:OGC::inapplicable'],
            ['PPM', 'urn:ogc:def:uom:UCUM::%5Bppm%5D'],
            ['PPB', 'urn:ogc:def:uom:UCUM::%5Bppb%5D'],
            ['MM', 'urn:ogc:def:uom:UCUM::mm'],
            ['UM', 'urn:ogc:def:uom:UCUM::um'],
            ['GCOUNT', 'urn:ogc:def:uom:UCUM::%7BGCOUNT%7D'],
            ['HA', 'urn:ogc:def:uom:UCUM::har'],
            ['MESH', 'urn:ogc:def:uom:UCUM::%5Bmesh_i%5D'],
            ['SI', 'urn:ogc:def:uom:UCUM::%7BSI%7D'],
            ['GM/CC', 'urn:ogc:def:uom:UCUM::g.cm-3']
        ];

    var unitOfMeasureStore = new Ext.data.SimpleStore({
        fields: ['unitLabel', 'urn'],
        data: unitsOfMeasure
    });

    measureTypes =  [
            ['Any'],
            ['Resource'],
            ['Reserve']
        ];

    var measureTypeStore = new Ext.data.SimpleStore({
        fields: ['type'],
        data: measureTypes
    });

    var measureTypeCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{type}" class="x-combo-list-item">{type}</div></tpl>',
        anchor: '100%',
        name: 'measureType',
        fieldLabel: 'Measure Type',
        //emptyText:'Select a Measure Type...',
        forceSelection: true,
        mode: 'local',
        //selectOnFocus: true,
        store: measureTypeStore,
        triggerAction: 'all',
        typeAhead: true,
        displayField:'type',
        valueField:'type'
    });

    measureTypeCombo.setValue('Any');

    var thePanel = new Ext.FormPanel({
        //region: "center",
        //collapsible: true,
        //title: "Filter Properties",
        //url: loadUrl,
        id: id,
        border: false,
        autoScroll:true,
        hideMode:'offsets',
        width: '100%',
        buttonAlign: 'right',
        labelAlign: 'right',
        labelWidth: 140,
        timeout: 180, //should not time out before the server does

        items: [{
            xtype:'fieldset',
            title: 'Mineral Occurrence Filter Properties',
            autoHeight:true,
            anchor: '100%',


            //defaultType: 'textfield',

            items :[{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Commodity Name',
                name: 'commodityName'
            },{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Commodity Group',
                name: 'commodityGroup'
            },
                measureTypeCombo
            ,{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Min. Ore Amount',
                name: 'minOreAmount'
            },  new Ext.form.ComboBox({
                tpl: '<tpl for="."><div ext:qtip="{unitLabel}" class="x-combo-list-item">{unitLabel}</div></tpl>',
                anchor: '100%',
                name: 'minOreAmountUOMName',
                hiddenName: 'minOreAmountUOM',
                fieldLabel: 'Min. Ore Amount Unit',
                emptyText:'Select a Unit Of Measure...',
                forceSelection: true,
                mode: 'local',
                //selectOnFocus: true,
                store: unitOfMeasureStore,
                triggerAction: 'all',
                typeAhead: true,
                displayField:'unitLabel',
                valueField:'urn'
            }),{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Min. Commodity Amount',
                name: 'minCommodityAmount'
            },  new Ext.form.ComboBox({
                tpl: '<tpl for="."><div ext:qtip="{unitLabel}" class="x-combo-list-item">{unitLabel}</div></tpl>',
                anchor: '100%',
                name: 'minCommodityAmountUOMName',
                hiddenName: 'minCommodityAmountUOM',
                fieldLabel: 'Min. Commodity Amount Unit',
                emptyText:'Select a Unit Of Measure...',
                forceSelection: true,
                mode: 'local',
                //selectOnFocus: true,
                store: unitOfMeasureStore,
                triggerAction: 'all',
                typeAhead: true,
                displayField:'unitLabel',
                valueField:'urn'
            }),{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Cut Off Grade',
                name: 'cutOffGrade',
                hidden: true,
                hideLabel: true
            },  new Ext.form.ComboBox({
                tpl: '<tpl for="."><div ext:qtip="{unitLabel}" class="x-combo-list-item">{unitLabel}</div></tpl>',
                anchor: '100%',
                name: 'cutOffGradeUOMName',
                hiddenName: 'cutOffGradeUOM',
                fieldLabel: 'Cut Off Grade Unit',
                emptyText:'Select a Unit Of Measure...',
                forceSelection: true,
                mode: 'local',
                //selectOnFocus: true,
                store: unitOfMeasureStore,
                triggerAction: 'all',
                typeAhead: true,
                displayField:'unitLabel',
                valueField:'urn',
                hidden: true,
                hideLabel: true
            })]
        }]
        /*,buttons: [{
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
    return thePanel;
};




//}