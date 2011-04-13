/**
 * Builds a form panel for generic gsml:Borehole filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */

BoreholeFilterForm = function(id) {
	
	this.isFormLoaded = true; //We aren't reliant on any remote downloads
	
	BoreholeFilterForm.superclass.constructor.call(this, {
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
            itemId     : 'borehole-fieldset',
            title      : 'Borehole Filter Properties',
            autoHeight : true,
            items      : [
            {
            	anchor     : '95%',
            	itemId     : 'name-field',
                xtype      : 'textfield',
                fieldLabel : 'Name',
                name       : 'boreholeName'
            },
            {
            	anchor     : '95%',
            	itemId     : 'custodian-field',
                xtype      : 'textfield',
                fieldLabel : 'Custodian',
                name       : 'custodian'
            },
            {
            	anchor     : '95%',
            	itemId     : 'drillingdate-field',
                xtype      : 'textfield',
                fieldLabel : 'Date',
                name       : 'dateOfDrilling'
            }
            ]
        }]
	});
    //return thePanel;
};

Ext.extend(BoreholeFilterForm, BaseFilterForm, {

});
