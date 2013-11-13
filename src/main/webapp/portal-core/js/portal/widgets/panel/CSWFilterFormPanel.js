/**
 * This is the panel for the form that pops up when you click on customize for the personal panel
 *
 */
Ext.define('portal.widgets.panel.CSWFilterFormPanel', {
    extend : 'portal.widgets.template.BaseTemplate',
    alias: 'widget.cswfilterformpanel',

    constructor : function(config){
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


        this._getTemplatedScriptGui(this.handleResponse, 'getFilteredCSWRecords.do', {
            xtype : 'form',
            width : 500,
            height : 520,
            items : [{
                     xtype:'tabpanel',
                     layout: 'fit',
                     items : [
                              generalTab,
                              spatialTab
                         ]
            }]
        });



    },


    handleResponse : function(status,store){
        var cswSelectionWindow = new CSWSelectionWindow({
            title : 'CSW Record Selection',
            store : store
        });
        cswSelectionWindow.show();

    }



});