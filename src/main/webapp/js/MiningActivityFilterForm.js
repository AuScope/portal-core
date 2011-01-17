/**
 * Builds a form panel for Mining Activity filters
 *
 * @param {number} the id of this formpanel instance
 * @param {string} the service url for submit
 */
MiningActivityFilterForm = function(id) {
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

    var producedMaterialCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div ext:qtip="{label}" class="x-combo-list-item">{label}</div></tpl>',
        anchor         : '100%',
        name           : 'producedMaterialDisplayed', /* this just returns the values from displayField! */
        hiddenName     : 'producedMaterial',         /* this returns the values from valueField! */
        fieldLabel     : 'Produced Material',
        labelAlign     : 'right',
        forceSelection : true,
        mode           : 'local',
        store          : commodityStore,
        triggerAction  : 'all',
        typeAhead      : true,
        displayField   :'label',   /* change tpl field to this value as well! */
        valueField     :'label'
    });


    MiningActivityFilterForm.superclass.constructor.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        width       : '100%',
        labelAlign  : 'right',
        labelWidth  : 130,
        timeout     : 180, //should not timeout before the server does
        bodyStyle   : 'padding:5px',
        items: [{
            xtype:'fieldset',
            title: '<span qtip="Please enter the filter constraints then hit \'Apply Filter\'">' +
            	   'Mining Activity Filter Properties' +
            	   '</span>',
            defaultType: 'textfield',
            defaults: {anchor: '100%'},
            items :[
            {
                fieldLabel: '<span qtip="Wildcards: \'!\' escape character; \'*\' zero or more, \'#\' just one character.">' +
            				'Associated Mine' +
            				'</span>',
                name: 'mineName'
            },
            producedMaterialCombo,
            {
                xtype: 'datefield',
                fieldLabel: '<span qtip="Activity which start AFTER this date">' +
                			'Activity Start Date' +
                			'</span>',
                name: 'startDate',
                format: "Y-m-d",
                value: ''
            },{
                xtype: 'datefield',
                fieldLabel: '<span qtip="Activity which end BEFORE this date">' +
                			'Activity End Date' +
                			'</span>',

                name: 'endDate',
                format: "Y-m-d",
                value: ''
            },{
                fieldLabel: '<span qtip="Minimum Amount of Ore Processed">' +
                			'Min. Ore Processed' +
                			'</span>',
                name: 'oreProcessed'
            },{
                fieldLabel: '<span qtip="Minimum Amount of Product Produced">' +
                			'Min. Prod. Amount' +
                			'</span>',
                name: 'production'
            },{
                fieldLabel: 'Grade',
                name: 'cutOffGrade',
                hidden: true,
                hideLabel: true
            }]
        }]
    });
};

Ext.extend(MiningActivityFilterForm, BaseFilterForm, {

});

