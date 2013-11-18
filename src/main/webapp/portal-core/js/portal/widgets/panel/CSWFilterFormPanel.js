/**
 * This is the panel for the form that pops up when you click on customize for the personal panel
 *
 */
Ext.define('portal.widgets.panel.CSWFilterFormPanel', {
    extend : 'portal.widgets.template.BaseCSWFilterForm',
    alias: 'widget.cswfilterformpanel',

    panelStore : null,

    constructor : function(config){
        this.panelStore=config.panelStore
        this.callParent(arguments);
    },


    requestScript : function() {


        var generalTab = {
                title : 'General filter',
                items : [{
                    xtype : 'textfield',
                    name : 'keywords',
                    fieldLabel : 'Keywords'
                },{
                    xtype : 'textfield',
                    name : 'any',
                    fieldLabel : 'Match Any Text'
                }]
        };


        var registriesTab = {
                title : 'Registries Filter',
                xtype : 'panel',
                items:[{
                    xtype: 'checkboxgroup',
                    name : 'cswServiceId',
                    id : 'registryTabCheckboxGroup',
                    fieldLabel: 'Registries',
                    // Arrange checkboxes into two columns, distributed vertically
                    columns: 1,
                    vertical: true

                }]
        };

        var checkBoxItems = [];

        var cswServiceItemStore = new Ext.data.Store({
            model   : 'portal.widgets.model.CSWServices',
            proxy : {
                type : 'ajax',
                url : 'getCSWServices.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            listeners : {
                load  :  function(store, records, successful, eopts){
                    for (var i = 0; i < records.length; i++) {
                        var cswServiceItemRec = records[i];
                        checkBoxItems.push({
                            boxLabel : cswServiceItemRec.get('title'),
                            name : 'cswServiceId',
                            inputValue: cswServiceItemRec.get('id'),
                            checked : cswServiceItemRec.get('selectedByDefault')
                        });
                    }

                    Ext.getCmp('registryTabCheckboxGroup').add(checkBoxItems);
                }
            }

        });
        cswServiceItemStore.load();





        var spatialTab ={
                title : 'Spatial filter',
                items : [{
                    xtype : 'textfield',
                    name : 'north',
                    fieldLabel : 'North'
                },{
                    xtype : 'textfield',
                    name : 'south',
                    fieldLabel : 'South'
                },{
                    xtype : 'textfield',
                    name : 'east',
                    fieldLabel : 'East'
                },{
                    xtype : 'textfield',
                    name : 'west',
                    fieldLabel : 'West'
                }]
        };


        this._getFilteredResult(this.panelStore, 'getFilteredCSWRecords.do', {
            xtype : 'form',
            width : 500,
            height : 520,
            items : [{
                     xtype:'tabpanel',
                     layout: 'fit',
                     items : [
                              generalTab,
                              registriesTab,
                              spatialTab
                         ]
            }]
        });



    }




});