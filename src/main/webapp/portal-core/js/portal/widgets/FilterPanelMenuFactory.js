/**
 * A FormFactory is a Factory class for generating instances of portal.layer.filterer.forms.BaseFilterForm
 * that are appropriate for a given portal.layer.Layer
 */
Ext.define('portal.widgets.FilterPanelMenuFactory', {
    extend : 'Ext.util.Observable',
       
    constructor : function(config) {        
        this.callParent(arguments);
    },
   
    /**
     * Given an portal.layer.Layer, work out whether there is an appropriate portal.layer.filterer.BaseFilterForm to show
     *
     * function(layer)
     *
     * layer - a portal.layer.Layer
     *
     * Returns a response in the form
     * {
     *    form : Ext.FormPanel - the formpanel to be displayed when this layer is selected (can be EmptyFilterForm)
     *    supportsFiltering : boolean - whether this formpanel supports the usage of the filter button
     *    layer : portal.layer.Layer that was used to generate this object
     * }
     *
     */
    appendAdditionalActions : portal.util.UnimplementedFunction
    
});