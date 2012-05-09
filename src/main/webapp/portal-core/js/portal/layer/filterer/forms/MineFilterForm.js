/**
 * Builds a form panel for Mine filters
 */
Ext.define('portal.layer.filterer.forms.MineFilterForm', {
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
            labelWidth: 70,
            timeout: 180, //should not time out before the server does
            bodyStyle:'padding:5px',
            autoHeight: true,
            items: [{
                xtype:'fieldset',
                title: '<span qtip="Please enter the filter constraints then hit \'Apply Filter\'">' +
                           'Mine Filter Properties' +
                       '</span>',
                autoHeight: true,
                items: [{
                    anchor: '100%',
                    xtype: 'textfield',
                    fieldLabel: '<span qtip="Wildcards: \'!\' escape character; \'*\' zero or more, \'#\' just one character.">' +
                                    'Mine Name' +
                                '</span>',
                    name: 'mineName'
                }]
            }]
        });

        this.callParent(arguments);
    }
});

