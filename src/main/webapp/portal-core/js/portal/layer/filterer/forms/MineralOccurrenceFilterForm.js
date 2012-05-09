/**
 * Builds a form panel for Mineral Occurrence filters
 */
Ext.define('portal.layer.filterer.forms.MineralOccurrenceFilterForm', {
    extend: 'portal.layer.filterer.BaseFilterForm',

    /**
     * Accepts a config for portal.layer.filterer.BaseFilterForm
     */
    constructor : function(config) {
        var commodityStore = Ext.create('Ext.data.Store', {
            fields : ['urn', 'label'],
            proxy : {
                type : 'ajax',
                url : 'getAllCommodities.do',
                reader : {
                    type : 'array',
                    root : 'data'
                }
            },
            autoLoad : true
        });

        Ext.apply(config, {
            delayedFormLoading: true, //we won't be ready until our commodity store is loaded - therefore we have to fire formloaded when ready
            border: false,
            autoScroll: true,
            hideMode: 'offsets',
            labelAlign: 'right',
            bodyStyle: 'padding:5px',
            autoHeight: true,
            layout: 'anchor',
            items: [{
                xtype: 'fieldset',
                title: '<span qtip="Please enter the filter constraints then hit \'Apply Filter\'">' +
                          'Mineral Occurrence Filter Properties' +
                       '</span>',
                autoHeight: true,
                labelAlign: 'right',
                bodyStyle: 'padding:0px',
                items :[{
                    xtype : 'combo',
                    anchor: '100%',
                    name: 'commodityNameDisplayed', /* this just returns the values from displayField! */
                    hiddenName: 'commodityName',    /* this returns the values from valueField! */
                    fieldLabel: '<span qtip="Please select a commodity from the Commodity Vocabulary. Powered by SISSVoc">' + 'Commodity' + '</span>',
                    labelAlign: 'right',
                    forceSelection: true,
                    queryMode: 'local',
                    store: commodityStore,
                    triggerAction: 'all',
                    typeAhead: true,
                    typeAheadDelay: 500,
                    displayField:'label',   /* change tpl field to this value as well! */
                    valueField:'urn'
                }]
            }]
        });

        this.callParent(arguments);

        //load our commodity store
        var callingInstance = this;
        commodityStore.load( {
            callback : function() {
                //It's very important that once all of our stores are loaded we fire the formloaded event
                //because we are setting the delayedFormLoading parameter to true in our constructor
                callingInstance.setIsFormLoaded(true);
                callingInstance.fireEvent('formloaded');
            }
        });
    }
});