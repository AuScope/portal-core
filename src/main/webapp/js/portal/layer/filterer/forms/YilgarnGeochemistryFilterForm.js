/**
 * Builds a form panel for Yilgarn Geochemistry filters
 */
Ext.define('portal.layer.filterer.forms.YilgarnGeochemistryFilterForm', {
    extend: 'portal.layer.filterer.BaseFilterForm',

    /**
     * Accepts a config for portal.layer.filterer.BaseFilterForm
     */
    constructor : function(config) {
        Ext.apply(config, {
            delayedFormLoading: false,
            border: false,
            autoScroll: true,
            hideMode:'offsets',
            width:'100%',
            buttonAlign:'right',
            labelAlign:'right',
            labelWidth: 130,
            bodyStyle:'padding:5px',
            autoHeight: true,
            items: [{
                xtype:'fieldset',
                title: 'YilgarnGeochemistry Filter Properties',
                autoHeight: true,
                items: [{
                    anchor: '100%',
                    xtype: 'textfield',
                    fieldLabel: 'Geologic Unit Name',
                    name: 'geologicName'
                }]
            }]
        });

        this.callParent(arguments);
    }
});
