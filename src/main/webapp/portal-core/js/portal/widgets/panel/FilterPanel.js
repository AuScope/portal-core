/**
 * A panel for displaying filter forms for a given portal.layer.Layer
 *
 * A filter panel is coupled tightly with a portal.widgets.panel.LayerPanel
 * as it is in charge of displayed appropriate filter forms matching the current
 * selection
 *
 * VT: THIS CLASS IS TO BE DELETED WITH THE NEW INLINE UI
 */
Ext.define('portal.widgets.panel.FilterPanel', {
    extend: 'Ext.Panel',

   
    _addLayerButton : null,

    filterForm : null,
    
    /**
     * Accepts all parameters for a normal Ext.Panel instance with the following additions
     * {
     *  layerPanel : [Required] an instance of a portal.widgets.panel.LayerPanel - selection events will be listend for
     * }
     */
    constructor : function(config) {
 
        this._map = config.map;
        this.filterForm = config.filterForm;
        
        this._addLayerButton = Ext.create('Ext.button.Button', {
            xtype : 'button',
            text      : 'Add layer to Map',
            iconCls    :   'add',
            handler : Ext.bind(this._onAddLayer, this)
        });
         

        Ext.apply(config, { 
            items : [
                this.filterForm
            ],
            buttons : [
                this._addLayerButton,
            {
                xtype:'tbfill'
            },{
                xtype : 'button',
                text      : 'Access options',
                iconCls    :   'setting',
                arrowAlign: 'right',
                menu      : [
                    {text: 'Item 1'},
                    {text: 'Item 2'},
                    {text: 'Item 3'},
                    {text: 'Item 4'}
                ]               
            }]
        
        });

        this.callParent(arguments);

 


    },



    /**
     * Internal handler for when the user clicks 'Apply Filter'.
     *
     * Simply updates the appropriate layer filterer. It's the responsibility
     * of renderers/layers to listen for filterer updates.
     */
    _onAddLayer : function() {      
        var layer = this.filterForm.layer; 
        var filterer = layer.get('filterer');      

        this._showConstraintWindow(layer);

        //Before applying filter, update the spatial bounds (silently)
        filterer.setSpatialParam(this._map.getVisibleMapBounds(), true);

        this.filterForm.writeToFilterer(filterer);
    },
    
    _showConstraintWindow : function(layer){
        var cswRecords = layer.get('cswRecords');
        for (var i = 0; i < cswRecords.length; i++) {
            if (cswRecords[i].hasConstraints()) {
                var popup = Ext.create('portal.widgets.window.CSWRecordConstraintsWindow', {
                    width : 625,
                    cswRecords : cswRecords
                });

                popup.show();

                  //HTML images may take a moment to load which stuffs up our layout
                  //This is a horrible, horrible workaround.
                var task = new Ext.util.DelayedTask(function(){
                    popup.doLayout();
                });
                task.delay(1000);

                break;
            }
        }
    },

    /**
     * Internal handler for when the user clicks 'Reset Filter'.
     *
     * Using the reset method from Ext.form.Basic. All fields in
     * the form will be reset. However, any record bound by loadRecord
     * will be retained.
     */
    _onResetFilter : function() {
        var baseFilterForm = this.getLayout().getActiveItem();
        baseFilterForm.getForm().reset();
    },

   

    clearFilter : function(){
        var layout = this.getLayout();

        //Remove custom CSS styles for filter button
        //this._filterButton.getEl().removeCls("applyFilterCls");

        //Disable the filter and reset buttons (set to default values)
        //this._filterButton.setDisabled(true);
        //this._resetButton.setDisabled(true);

        //Close active item to prevent memory leak
        var actvItem = layout.getActiveItem();
        if (actvItem) {
            actvItem.close();
        }
        layout.setActiveItem(this._emptyCard);
    }
});