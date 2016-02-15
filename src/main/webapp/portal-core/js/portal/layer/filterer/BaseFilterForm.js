/**
 * The abstract base filter form for all Portal filter forms to inherit from
 *
 * The 'formloaded' event must be raised and isFormLoaded set to true before the system will
 * start requesting information from the specified filterer. By default this occurs in the constructor
 * if delayedFormLoading is false otherwise it is the responsiblity of the child class to raise/set this
 *
 * This allows forms to make external requests for data when initialising without fear of being
 * required to generate a filter object too early
 *
 *
 */
Ext.define('portal.layer.filterer.BaseFilterForm', {
    extend: 'Ext.form.Panel',

    config : {
        isFormLoaded : false //has the 'formloaded' event fired yet?
    },

    map : null, //an instance of portal.util.gmap.GMapWrapper
    layer : null, //an instance of portal.layer.Layer
    delayedFormLoading : false, //Setting this will indicate that this form will not be immediately available for filtering after creation
                                //due to having to load something from an external source.

    /**
     * Accepts a Ext.form.Panel config as well as
     * {
     *      map : [Required] an instance of portal.util.gmap.GMapWrapper
     *      layer : [Required] an instance of portal.layer.Layer which you wish to filter
     *      delayedFormLoading : If set to false, the form will able to interact with its filterer immediately, if set
     *                           to true then the form will not be interacted with until formloaded is raised
     * }
     */
    constructor : function(config) {
        this.map = config.map;
        this.layer = config.layer;
        this.setIsFormLoaded(false);
        this.delayedFormLoading = config.delayedFormLoading;        
        
        Ext.apply(config, {
            cls : 'filter-panel-color'
        })

        this.callParent(arguments);        
        
        if (!this.delayedFormLoading) {
            this.setIsFormLoaded(true);
            this.fireEvent('formloaded', this);
        }
    },
    
    onDestroy : function() {
        this.callParent();
    },
    
    setLayer : function(layer){
        this.layer = layer;
    },

    /**
     * Write this FilterForm's contents to the specified portal.layer.filterer.Filterer object.
     *
     * The default functionality is to use the internal FormPanel Form values to write
     * to the filterer, child classes can override this method for their own custom functionality.
     *
     * filterer - an instance of portal.layer.filterer.Filterer which will be cleared and then updated with this form's contents
     */
    writeToFilterer : function(filterer) {
        var parameters = this.getForm().getValues();

        //Ensure we preserve the spatial filter (if any).
        var bbox = filterer.getSpatialParam();
        parameters[portal.layer.filterer.Filterer.BBOX_FIELD] = bbox;

        //All other filter params should be overwritten
        filterer.setParameters(parameters, true);
    },

    /**
     * Write portal.layer.filterer.Filterer object and use it to populate the internal form's values (which will
     * update the corresponding GUI elements).
     *
     * The default functionality is to set the internal FormPanel Form values, child classes can override
     * this method with their own custom functionality.
     *
     * filterer - an instance of portal.layer.filterer.Filterer which will be read
     */
    readFromFilterer : function(filterer) {
        var parameters = filterer.getParameters();
        this.getForm().setValues(parameters);
    }    

});