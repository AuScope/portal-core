/**
 * Builds a form panel for WMS Layers (Containing WMS specific options such as transparency).
 *
 */
Ext.define('portal.layer.filterer.forms.WMSLayerFilterForm', {
    extend: 'portal.layer.filterer.BaseFilterForm',

    /**
     * Accepts a config for portal.layer.filterer.BaseFilterForm
     */
    constructor : function(config) {

        var filterer=config.layer.get('filterer');

        var sliderHandler = function(caller, newValue) {
            var newOpacity = (newValue / 100);
            filterer.setParameter('opacity',newOpacity);
        };

        if(!filterer.getParameter('opacity')){
            filterer.setParameter('opacity',100);
        }

        Ext.apply(config, {

            border      : false,
            autoScroll  : true,
            hideMode    : 'offsets',
            width       : '100%',
            labelAlign  : 'right',
            bodyStyle   : 'padding:5px',
            autoHeight:    true,
            layout: 'anchor',
            items:[ {
                xtype      :'fieldset',
                title      : 'WMS Properties',
                layout     : 'fit',
                autoHeight : true,
                items      : [{
                        xtype       : 'slider',
                        fieldLabel  : 'Opacity',
                        name        : 'opacity',
                        minValue    : 0,
                        maxValue    : 100,
                        value       : (config.layer.get('filterer').getParameter('opacity') * 100),
                        listeners   : {changecomplete: sliderHandler}
                }]
            }]
        });

        this.callParent(arguments);
    }
});

