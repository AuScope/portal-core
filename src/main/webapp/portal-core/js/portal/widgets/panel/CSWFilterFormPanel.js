/**
 * This is the panel for the form that pops up when you click on customize for the personal panel
 *
 */
Ext.define('portal.widgets.panel.CSWFilterFormPanel', {
    extend : 'Ext.tab.Panel',
    alias: 'widget.cswfilterformpanel',

    constructor : function(cfg){

        var generalTab = {
                title : 'General filter',
                items : [{
                    xtype : 'textfield',
                    fieldLabel : 'Match Any Text'
                }]
        };

        var spatialTab ={
                title : 'Spatial filter',
                items : [{
                    xtype : 'textfield',
                    fieldLabel : 'North'
                },{
                    xtype : 'textfield',
                    fieldLabel : 'South'
                },{
                    xtype : 'textfield',
                    fieldLabel : 'East'
                },{
                    xtype : 'textfield',
                    fieldLabel : 'West'
                }]
        };



        Ext.apply(cfg,{
            items : [
                 generalTab,
                 spatialTab
            ]

        });


        this.callParent(arguments);
    }



});