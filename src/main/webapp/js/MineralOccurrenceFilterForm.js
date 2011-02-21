/**
 * Builds a form panel for Mineral Occurrence filters
 *
 * @param {id} to specify the id of this formpanel instance
 * @param {serviceUrl} the service url for submit
 */
MineralOccurrenceFilterForm = function(id) {
    var unitsOfMeasure = [
            ['CRT', 'urn:ogc:def:uom:UCUM::%5Bcar_m%5D'],
            ['CUB M', 'urn:ogc:def:uom:UCUM::m3'],
            ['TONNE', 'urn:ogc:def:uom:UCUM::t'],
            ['GM', 'urn:ogc:def:uom:UCUM::g'],
            ['KG', 'urn:ogc:def:uom:UCUM::kg'],
            ['UKN', 'urn:ogc:def:nil:OGC::missing']
        ];

    var unitOfMeasureStore = new Ext.data.SimpleStore({
        fields : ['unitLabel', 'urn'],
        data   : unitsOfMeasure
    });

    //-----------Measure type

    var measureTypes =  [
            ['None'],
            ['Any'],
            ['Endowment'],
            ['Reserve'],
            ['Resource']
        ];

    var measureTypeStore = new Ext.data.SimpleStore({
        fields : ['type'],
        data   : measureTypes
    });

    var measureTypeCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{type}" class="x-combo-list-item">{type}</div></tpl>',
        anchor         : '100%',
        name           : 'measureTypeDisplayed',
        hiddenName     : 'measureType',
        fieldLabel     : 'Measure Type',
        editable       : false,
        emptyText      : 'None',
        forceSelection : true,
        mode           : 'local',
        store          : measureTypeStore,
        triggerAction  : 'all',
        typeAhead      : true,
        displayField   : 'type',
        valueField     : 'type',
        //value          : 'Any'
        listeners      : {
            select: {
                fn:function(combo, record, index) {
                    if (combo.getValue() === 'None') {
                        Ext.getCmp('minOreAmountTxtFld').disable();
                        Ext.getCmp('minCommodityAmountTxtFld').disable();
                        Ext.getCmp('minOreAmountUOMCmbBx').disable();
                        Ext.getCmp('minCommodityAmountUOMCmbBx').disable();
                    }
                    else {
                        Ext.getCmp('minOreAmountTxtFld').enable();
                        Ext.getCmp('minCommodityAmountTxtFld').enable();
                        Ext.getCmp('minOreAmountUOMCmbBx').enable();
                        Ext.getCmp('minCommodityAmountUOMCmbBx').enable();
                    }
                }
            }
        }
    });

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

    var callingInstance = this;
    commodityStore.load( {
        callback : function() {
            callingInstance.isFormLoaded = true;
            callingInstance.fireEvent('formloaded');
        }
    });

    var commodityNameCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{label}" class="x-combo-list-item">{label}</div></tpl>',
        anchor         : '100%',
        name           : 'commodityNameDisplayed', /* this just returns the values from displayField! */
        hiddenName     : 'commodityName',    /* this returns the values from valueField! */
        fieldLabel     : '<span qtip="Please select a commodity from the Commodity Vocabulary. Powered by SISSVoc">' + 'Commodity' + '</span>',
        labelAlign     : 'right',
        forceSelection : true,
        mode           : 'local',
        /*selectOnFocus: true,*/
        store          : commodityStore,
        triggerAction  : 'all',
        typeAhead      : true,
        typeAheadDelay : 500,
        displayField   :'label',   /* change tpl field to this value as well! */
        valueField     :'urn'
    });

    var minOreAmountUOMCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{unitLabel}" class="x-combo-list-item">{unitLabel}</div></tpl>',
        anchor         : '100%',
        id             : 'minOreAmountUOMCmbBx',
        name           : 'minOreAmountUOMDisplayed',
        hiddenName     : 'minOreAmountUOM',
        fieldLabel     : 'Unit of Measure',
        labelAlign     : 'right',
        emptyText      : 'Any',
        forceSelection : true,
        mode           : 'local',
        selectOnFocus  : true,
        store          : unitOfMeasureStore,
        triggerAction  : 'all',
        typeAhead      : true,
        displayField   : 'unitLabel',
        valueField     : 'urn'
        //value          : 'Any'
    });

    var minCommodityAmountUOMCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{unitLabel}" class="x-combo-list-item">{unitLabel}</div></tpl>',
        anchor         : '100%',
        id             : 'minCommodityAmountUOMCmbBx',
        name           : 'minCommodityAmountUOMDisplayed',
        hiddenName     : 'minCommodityAmountUOM',
        fieldLabel     : 'Unit of Measure',
        emptyText      : 'Any',
        forceSelection : true,
        mode           : 'local',
        selectOnFocus  : true,
        store          : unitOfMeasureStore,
        triggerAction  : 'all',
        typeAhead      : true,
        displayField   : 'unitLabel',
        valueField     : 'urn'
        //value          : 'Any'
    });

    var cutOffGradeUOMCombo = new Ext.form.ComboBox({
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
    });


    //-----------Panel
    MineralOccurrenceFilterForm.superclass.constructor.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        //width       : '100%',
        labelAlign  : 'right',
        //labelWidth  : 100,
        timeout     : 180, /*should not time out before the server does*/
        bodyStyle   : 'padding:5px',
        autoHeight:    true,
        layout: 'anchor',
        delayFormPopulation : true,
        items: [{
            xtype      : 'fieldset',
            title      : '<span qtip="Please enter the filter constraints then hit \'Apply Filter\'">' +
                         'Mineral Occurrence Filter Properties' +
                         '</span>',
            autoHeight : true,
            labelAlign : 'right',
            bodyStyle   : 'padding:0px',
            items :[
                commodityNameCombo//,
//Uncomment this block to enable filtering by Amount                
//                {
//                    xtype      : 'fieldset',
//                    title      : 'Amount',
//                    autoHeight: true,
//                    items:[
//                        measureTypeCombo,
//                        {
//                            xtype      : 'fieldset',
//                            title      : 'Ore Amount',
//                            autoHeight: true,
//                            items:[
//                                {
//                                    id         : 'minOreAmountTxtFld',
//                                    anchor     : '100%',
//                                    xtype      : 'textfield',
//                                    fieldLabel : 'Min. Amount',
//                                    name       : 'minOreAmount',
//                                    disabled   : true
//                                },
//                                minOreAmountUOMCombo
//                            ]
//                        },{
//                            xtype       : 'fieldset',
//                            title       : 'Commodity Amount',
//                            autoHeight: true,
//                            items:[
//                                {
//                                    id         : 'minCommodityAmountTxtFld',
//                                    anchor     : '100%',
//                                    xtype      : 'textfield',
//                                    fieldLabel : 'Min. Amount',
//                                    name       : 'minCommodityAmount',
//                                    disabled   : true
//                                },
//                                minCommodityAmountUOMCombo
//                            ]
//                        }
//                    ]
//                }
            ]
        }]
    });
};

Ext.extend(MineralOccurrenceFilterForm, BaseFilterForm, {

});