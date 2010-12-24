/**
 * The base filter form for all Portal filter forms to inherit from
 * 
 * All children of this class must raise the 'formloaded' event when the form is ready to be populated with values
 * (Alternatively set delayFormPopulation to false)
 */
BaseFilterForm = function(config) {
    BaseFilterForm.superclass.constructor.call(this, config);
};


Ext.extend(BaseFilterForm, Ext.FormPanel, {
    
    //Child instances must set this variable whenever the form is loaded
    isFormLoaded : false,
    
    //Private function
    initComponent : function(){
        BaseFilterForm.superclass.initComponent.call(this);
        this.addEvents('formloaded');
    }
});