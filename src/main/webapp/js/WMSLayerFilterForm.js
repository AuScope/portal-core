/**
 * Builds a form panel for WMS Layers (Containing WMS specific options such as transparency).
 *
 */

WMSLayerFilterForm = function(activeLayerRecord, map) {

    var sliderHandler = function(caller, newValue) {
    	var overlayManager = activeLayerRecord.getOverlayManager();
    	var newOpacity = (newValue / 100);
    	
    	activeLayerRecord.setOpacity(newOpacity);
    	overlayManager.updateOpacity(newOpacity);
    };
    
    this.isFormLoaded = true; //We aren't reliant on any remote downloads

    //-----------Panel
    WMSLayerFilterForm.superclass.constructor.call(this, {
        id          : String.format('{0}',activeLayerRecord.getId()),
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
                    value       : (activeLayerRecord.getOpacity() * 100),
                    listeners   : {changecomplete: sliderHandler}
            }]
        }]

    });
};

Ext.extend(WMSLayerFilterForm, Ext.FormPanel, {
    
});