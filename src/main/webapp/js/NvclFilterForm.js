/**
 * Builds a form panel for NVCL filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */

NvclFilterForm = function(id) {
	
	this.isFormLoaded = true; //We aren't reliant on any remote downloads
	
	var showQtip = null;
	if (id.indexOf("Pressure_DB") !== -1){
		showQtip = '<span qtip="This filter is not available in the current dataset">' + 'Custodian' + '</span>';
	}else 
		showQtip = 'Custodian';
	
	NvclFilterForm.superclass.constructor.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    :'offsets',
        width       :'100%',
        buttonAlign :'right',
        labelAlign  :'right',
        labelWidth  : 70,
        timeout     : 180, //should not time out before the server does
        bodyStyle   :'padding:5px',
        autoHeight: true,
        items       : [{
            xtype      :'fieldset',
            title      : 'Borehole Filter Properties',
            autoHeight : true,
            items      : [
            {
            	anchor     : '95%',
                xtype      : 'textfield',
                fieldLabel : 'Name',
                name       : 'boreholeName'
            },
            {
            	anchor     : '95%',
                xtype      : 'textfield',
                fieldLabel :  showQtip,
                name       : 'custodian'
            },
            {
            	anchor     : '95%',
                xtype      : 'textfield',
                fieldLabel : 'Date',
                name       : 'dateOfDrilling'
            }
            ]
        }]
	});
    //return thePanel;
};

Ext.extend(NvclFilterForm, BaseFilterForm, {

});
