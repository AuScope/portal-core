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
        	if (this.layer.get('source').get('active')) {
        		filterer.setParameter('opacity',newValue);
        	}
        };

        if(!filterer.getParameter('opacity')){
            filterer.setParameter('opacity',1,true);
        }

        Ext.apply(config, {

            border      : false,
            autoScroll  : false,
            hideMode    : 'offsets',
            width       : '100%',
            labelAlign  : 'right',
            bodyStyle   : 'padding:5px',
            height      :    65,
            layout: 'anchor',
            items:[ {
                xtype      :'fieldset',
                title      : 'WMS Properties',
                layout     : 'fit',
                height : '100%',
                items      : [{
                        xtype       : 'slider',
                        fieldLabel  : 'Opacity',
                        name        : 'opacity',
                        minValue    : 0,
                        increment   : 0.01,
                        decimalPrecision : false,
                        maxValue    : 1,
                        value       : config.layer.get('filterer').getParameter('opacity'),
                        listeners   : {
                        	changecomplete: sliderHandler,
                        	scope: this
                        }
                }]
            }]
        });

        this.callParent(arguments);
    }
});

