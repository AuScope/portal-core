/**
 * Builds a form panel for NVCL filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */

NvclFilterForm = function(id) {
    NvclFilterForm.superclass.constructor.call(this, id);

     // create the combo instance
    var serviceCombo = new Ext.form.ComboBox({
        anchor     : '95%',
        itemId     : 'serviceFilter-field',
        fieldLabel : 'Services',
        name       : 'serviceFilter',
        typeAhead: true,
        triggerAction: 'all',
        lazyRender:true,
        mode: 'remote',
        store: new Ext.data.Store({
            //use the title as shown on the GUI or in the context file
            baseParams: {title: 'National Virtual Core Library'},
            proxy    : new Ext.data.HttpProxy({
                url: 'getBoreholeServices.do',
                method: 'POST'
            }),
            reader : new Ext.data.JsonReader({
                root            : 'data',
                id              : 'url',
                successProperty : 'success',
                messageProperty : 'msg',
                fields          : [
                    'url',
                ]
            })

        }),
        valueField: 'url',
        displayField: 'url'
    });

    var fieldSet = this.getComponent('borehole-fieldset');
    fieldSet.setTitle('NVCL Filter Properties');
    fieldSet.add(serviceCombo);
    this.add({
        itemId     : 'hylogger-field',
        xtype      : 'hidden',
        name       : 'onlyHylogger',
        value      : true
    });
};

Ext.extend(NvclFilterForm, BoreholeFilterForm, {

});
