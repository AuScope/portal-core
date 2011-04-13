/**
 * Builds a form panel for Pressure DB filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */

PressureDBFilterForm = function(id) {
	PressureDBFilterForm.superclass.constructor.call(this, id);
	
	var fieldSet = this.getComponent('borehole-fieldset');
	fieldSet.setTitle('Pressure DB Filter Properties');
	fieldSet.remove(fieldSet.getComponent('custodian-field'));
	this.doLayout();
};

Ext.extend(PressureDBFilterForm, BoreholeFilterForm, {

});
