/**
 * Builds a form panel for Yilgarn Geochemistry filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */

YilgarnGeochemistryFilterForm = function(id) {
	Ext.FormPanel.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    :'offsets',
        width       :'100%',
        buttonAlign :'right',
        labelAlign  :'right',
        labelWidth  : 130,
        timeout     : 180, //should not time out before the server does
        bodyStyle   :'padding:5px',
        autoHeight: true,
        items       : [{
            xtype      :'fieldset',
            title      : 'YilgarnGeochemistry Filter Properties',
            autoHeight : true,
            items      : [
            {
            	anchor     : '100%',
                xtype      : 'textfield',
                fieldLabel : 'Geologic Unit Name',
                name       : 'geologicName'
            }
            ]
        }]
	});
    //return thePanel;
};

YilgarnGeochemistryFilterForm.prototype = new Ext.FormPanel();

