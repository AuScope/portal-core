/**
 * This is the panel for the form that pops up when you click on customize for the personal panel
 *
 */
Ext.define('portal.widgets.panel.CSWFilterFormPanel', {
    extend : 'portal.widgets.template.BaseCSWFilterForm',
    alias: 'widget.cswfilterformpanel',

    panelStore : null,
    keywordStore : null,
    keywordIDCounter : 0,
    spacerHeight : 22,
    keywordMatchTypeStore : null,

    constructor : function(config){
        this.panelStore=config.panelStore;

        this.keywordStore = new Ext.data.Store({
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


        this.keywordMatchTypeStore = Ext.create('Ext.data.Store', {
            fields: ['display', 'value'],
            data : [
                {"display":"Any", "value":"Any"},
                {"display":"All", "value":"All"}
            ]
        });



        this.callParent(arguments);
    },


    requestScript : function() {


        var keywordsComponent = this;
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
                        store: this.keywordMatchTypeStore
                    },{
                        xtype : 'fieldset',
                        itemId : 'cswfilterkeywordfieldsetitemid',
                        layout : 'column',
                        anchor : '100%',
                        border : false,
                        style : 'padding:5px 10px 0px 10px',
                        items : [{
                            columnWidth : 1,
                            border : false,
                            layout : 'anchor',
                            bodyStyle:'padding:0px 0 0px 2px',
                            items : []
                        },{
                            width : 25,
                            border : false,
                            bodyStyle:'padding:0px 0 0px 0px',
                            items : []
                        }, {
                            width : 25,
                            border : false,
                            bodyStyle:'padding:0px 0 0px 0px',
                            items : []
                        }]
                    }],
                    listeners : {
                        afterrender : function() {
                            keywordsComponent.handlerNewKeywordField();
                        }
                    }
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



    },

    /**
     * Updates the visibility on all add/remove buttons
     */
    updateAddRemoveButtons : function() {
        var keywordFieldSet = Ext.getCmp('personalpanelcswfilterform').query('fieldset[itemId=cswfilterkeywordfieldsetitemid]')[0]

        var comboKeywordColumn = keywordFieldSet.items.items[0];
        var buttonRemoveColumn = keywordFieldSet.items.items[1];
        var buttonAddColumn = keywordFieldSet.items.items[2];

        var existingKeywordFields = comboKeywordColumn.items.getCount();

        for (var i = 0; i < existingKeywordFields; i++) {
            var addButton = buttonAddColumn.items.items[i];
            var removeButton = buttonRemoveColumn.items.items[i];

            //We can always remove so long as we have at least 1 keyword
            if (existingKeywordFields <= 1) {
                removeButton.hide();
            } else {
                removeButton.show();
            }
        }
    },


    /**
     * This function removes the buttons and keywords associated with button
     */
    handlerRemoveKeywordField : function(button, e) {
        var keywordFieldSet = Ext.getCmp('personalpanelcswfilterform').query('fieldset[itemId=cswfilterkeywordfieldsetitemid]')[0]

        var comboKeywordColumn = keywordFieldSet.items.items[0];
        var buttonRemoveColumn = keywordFieldSet.items.items[1];
        var buttonAddColumn = keywordFieldSet.items.items[2];

        //Figure out what component index we are attempting to remove
        var id = button.initialConfig.keywordIDCounter;
        var index = buttonRemoveColumn.items.findIndexBy(function(cmp) {
            return cmp === button;
        });

        //Remove that index from each column
        comboKeywordColumn.remove(comboKeywordColumn.getComponent(index));
        buttonRemoveColumn.remove(buttonRemoveColumn.getComponent(index));
        buttonAddColumn.remove(buttonAddColumn.getComponent(0)); //always remove spacers

        //Update our add/remove buttons
        this.updateAddRemoveButtons();
        keywordFieldSet.doLayout();
    },



    handlerNewKeywordField : function(button, e) {
        var keywordFieldSet = Ext.getCmp('personalpanelcswfilterform').query('fieldset[itemId=cswfilterkeywordfieldsetitemid]')[0]

        var comboKeywordColumn = keywordFieldSet.items.items[0];
        var buttonRemoveColumn = keywordFieldSet.items.items[1];
        var buttonAddColumn = keywordFieldSet.items.items[2];


        //Add our combo for selecting keywords
        comboKeywordColumn.add({
            xtype : 'combobox',
            width : 380,
            name : 'keywords',
            queryMode : 'remote',
            typeAhead: true,
            style: {
                marginBottom: '0px'
            },
            typeAheadDelay : 500,
            forceSelection : true,
            triggerAction : 'all',
            valueField:'keyword',
            fieldLabel : 'keyword',
            store :    this.keywordStore,
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
        });

        //We also need a button to remove this keyword field
        buttonRemoveColumn.add({
            xtype : 'button',
            iconCls : 'remove',
            width : 22,
            height : this.spacerHeight,
            scope : this,
            keywordIDCounter : this.keywordIDCounter,
            handler : this.handlerRemoveKeywordField
        });

        //Because our add button will always be at the bottom of the list
        //we need to pad preceding elements with spacers
        if (buttonAddColumn.items.getCount()===0) {
            buttonAddColumn.add({
                xtype : 'button',
                iconCls : 'add',
                width : 22,
                height : this.spacerHeight,
                scope : this,
                handler : this.handlerNewKeywordField
            });
        } else {
            buttonAddColumn.insert(0, {
                xtype : 'box',
                width : 22,
                height : this.spacerHeight
            })
        }

        this.keywordIDCounter++;
        this.updateAddRemoveButtons();
        keywordFieldSet.doLayout();

    }




});