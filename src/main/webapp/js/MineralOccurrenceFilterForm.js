/**
 * Builds a form panel for Mineral Occurrence filters
 *
 * @param {id} to specify the id of this formpanel instance
 * @param {serviceUrl} the service url for submit
 */
MineralOccurrenceFilterForm = function(id) {
    var unitsOfMeasure = [
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

    //-----------Measure type

    var measureTypes =  [
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
        editable: false,
        forceSelection: true,
        mode: 'local',
        store: measureTypeStore,
        triggerAction: 'all',
        typeAhead: true,
        displayField: 'type',
        valueField: 'type',
        value: 'Any'
    });

    //-----------Commodities

    var commodityStore = new Ext.data.SimpleStore({
        fields: ['urn', 'label'],
        proxy: new Ext.data.HttpProxy({url: '/getCommodities.do'}),
        reader: new Ext.data.ArrayReader({}, [
            { name:'urn'   },
            { name:'label' }
        ])
    });
    
    commodityStore.reload();
    
    var commodityNameCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{label}" class="x-combo-list-item">{label}</div></tpl>',
        anchor: '100%',
        name: 'commodityNameDisplayed', /* this just returns the values from displayField! */
        hiddenName: 'commodityName',    /* this returns the values from valueField! */
        fieldLabel: 'Commodity Name',
        forceSelection: true,
        mode: 'local',
        /*selectOnFocus: true,*/
        store: commodityStore,
        triggerAction: 'all',
        typeAhead: true,
        displayField:'label',           /* change tpl field to this value as well! */
        valueField:'urn'
    });
    
    //-----------Panel
    
    Ext.FormPanel.call(this,{
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        width       : '100%',
        buttonAlign : 'right',
        labelAlign  : 'right',
        labelWidth  : 140,
        timeout     : 180, /*should not time out before the server does*/

        items: [{
            xtype:'fieldset',
            title: 'Mineral Occurrence Filter Properties',
            autoHeight:true,
            anchor: '100%',

            /*defaultType: 'textfield',*/

            items :[
                commodityNameCombo
            ,{
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
                name: 'minOreAmountUOMDisplayed',
                hiddenName: 'minOreAmountUOM',
                fieldLabel: 'Min. Ore Amount Unit',
                emptyText:'Select a Unit Of Measure...',
                forceSelection: true,
                mode: 'local',
                selectOnFocus: true,
                store: unitOfMeasureStore,
                triggerAction: 'all',
                typeAhead: true,
                displayField: 'unitLabel',
                valueField: 'urn',
                value: 'TONNE'
            }),{
                anchor: '100%',
                xtype: 'textfield',
                fieldLabel: 'Min. Commodity Amount',
                name: 'minCommodityAmount'
            },  new Ext.form.ComboBox({
                tpl: '<tpl for="."><div ext:qtip="{unitLabel}" class="x-combo-list-item">{unitLabel}</div></tpl>',
                anchor: '100%',
                name: 'minCommodityAmountUOMDisplayed',
                hiddenName: 'minCommodityAmountUOM',
                fieldLabel: 'Min. Commodity Amount Unit',
                emptyText:'Select a Unit Of Measure...',
                forceSelection: true,
                mode: 'local',
                selectOnFocus: true,
                store: unitOfMeasureStore,
                triggerAction: 'all',
                typeAhead: true,
                displayField: 'unitLabel',
                valueField: 'urn',
                value: 'TONNE'
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
                name: 'cutOffGradeUOMDisplayed',
                hiddenName: 'cutOffGradeUOM',
                fieldLabel: 'Cut Off Grade Unit',
                emptyText:'Select a Unit Of Measure...',
                forceSelection: true,
                mode: 'local',
                selectOnFocus: true,
                store: unitOfMeasureStore,
                triggerAction: 'all',
                typeAhead: true,
                displayField: 'unitLabel',
                valueField: 'urn',
                value: 'TONNE',
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
};

MineralOccurrenceFilterForm.prototype = new Ext.FormPanel();