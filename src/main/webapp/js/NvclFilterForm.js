/**
 * Builds a form panel for NVCL filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */

NvclFilterForm = function(id,activeLayersRecord) {
    NvclFilterForm.superclass.constructor.call(this, id, activeLayersRecord);

    var fieldSet = this.getComponent('borehole-fieldset');
    fieldSet.setTitle('NVCL Filter Properties');
    this.add({
        itemId     : 'hylogger-field',
        xtype      : 'hidden',
        name       : 'onlyHylogger',
        value      : true
    });
};

Ext.extend(NvclFilterForm, BoreholeFilterForm, {

});
