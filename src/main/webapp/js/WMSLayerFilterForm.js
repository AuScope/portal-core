/**
 * Builds a form panel for WMS Layers (Containing WMS specific options such as transparency).
 *
 */

WMSLayerFilterForm = function(record, map) {

    var sliderHandler = function(caller, newValue) {
        record.set('opacity', (newValue / 100));
        
        if (record.tileOverlay instanceof OverlayManager) {
        	record.tileOverlay.updateOpacity(record.get('opacity'));
        } else {
	        record.tileOverlay.getTileLayer().opacity = record.get('opacity');
	
	        map.removeOverlay(record.tileOverlay);
	        map.addOverlay(record.tileOverlay);
        }
    };

    //-----------Panel
    WMSLayerFilterForm.superclass.constructor.call(this, {
        id          : String.format('{0}',record.get('id')),
        border      : false,
        autoScroll  : true,
        hideMode    : 'offsets',
        //width       : '100%',
        labelAlign  : 'right',
        bodyStyle   : 'padding:5px',
        autoHeight:    true,
        layout: 'anchor',
        items:[ {
            xtype      :'fieldset',
            title      : 'WMS Properties',
            autoHeight : true,
            items      : [{
                    xtype       : 'slider',
                    fieldLabel  : 'Opacity',
                    minValue    : 0,
                    maxValue    : 100,
                    value       : (record.get('opacity') * 100),
                    listeners   : {changecomplete: sliderHandler}
            }]
        }]

    });
};

Ext.extend(WMSLayerFilterForm, Ext.FormPanel, {
    
});