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

        var keywordMatchTypeStore = Ext.create('Ext.data.Store', {
            fields: ['display', 'value'],
            data : [
                {"display":"Any", "value":"Any"},
                {"display":"All", "value":"All"}
            ]
        });

        var keywordStore = new Ext.data.Store({
            autoload: true,
            fields: ['keyword', 'count'],
            proxy : {
                type : 'ajax',
                url : 'getCSWKeywords.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            }

        });



        var generalTab = {
                title : 'General filter',
                layout : 'anchor',
                items : [{
                    xtype:'fieldset',
                    title : 'Match Type',
                    items:[{
                        xtype : 'combobox',
                        name : 'keywordMatchType',
                        queryMode:'local',
                        valueField:'value',
                        displayField:'display',
                        fieldLabel : 'Match Type',
                        store: keywordMatchTypeStore
                    },{
                        xtype : 'fieldset',
                        layout : 'column',
                        anchor : '100%',
                        border : false,
                        style : 'padding:0px 0px 0px 0px',
                        items : [{
                            xtype : 'combobox',
                            width : 380,
                            name : 'keywords',
                            queryMode : 'remote',
                            typeAhead: true,
                            typeAheadDelay : 500,
                            forceSelection : true,
                            triggerAction : 'all',
                            valueField:'keyword',

                            fieldLabel : 'Keywords',
                            store :    keywordStore,
                            tpl: Ext.create('Ext.XTemplate',
                                    '<tpl for=".">',
                                        '<div class="x-boundlist-item">{keyword} - <b>({count})</b></div>',
                                    '</tpl>'
                                ),
                                // template for the content inside text field
                            displayTpl: Ext.create('Ext.XTemplate',
                                    '<tpl for=".">',
                                        '{keyword}',
                                    '</tpl>'
                                )

                        },{
                            xtype : 'button',
                            iconCls : 'add',
                            keypos : 0,
                            width : 21

                        },{
                            xtype : 'button',
                            iconCls : 'remove',
                            keypos : 0,
                            hidden : true,
                            width : 21,
                        }]
                    }]
                },{
                    xtype:'fieldset',
                    title : 'Match Text',
                    items:[{
                        xtype : 'textfield',
                        name : 'anyText',
                        fieldLabel : 'Match Any Text'
                    }]
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
            id : 'personalpanelcswfilterform',
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