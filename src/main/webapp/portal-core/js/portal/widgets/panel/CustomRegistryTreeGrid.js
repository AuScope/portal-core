Ext.define('portal.widgets.panel.CustomRegistryTreeGrid', {
    extend: 'Ext.tree.Panel',

    requires: [
        'Ext.data.*',
        'Ext.grid.*',
        'Ext.tree.*',
        'Ext.ux.CheckColumn',
        'portal.widgets.model.CustomRegistryModel'
    ],
    xtype: 'tree-grid',


    title: 'Saved Registries',
    layout : 'fit',
    useArrows: true,
    rootVisible: false,
    multiSelect: true,
    singleExpand: true,

    constructor: function(cfg) {     

        Ext.apply(this, {
            itemId : 'customRegistryNavTree',
            store: new Ext.data.TreeStore({
                model: portal.widgets.model.CustomRegistryModel,
                proxy: {
                    type: 'memory'                         
                }                      
            }),
            selModel : Ext.create('Ext.selection.CheckboxModel', {}),
            columns: [{
                xtype: 'treecolumn', //this is so we know which column will show the tree
                text: 'Title',
                flex: 1,
                sortable: true,
                dataIndex: 'title'
            },{
                text: 'CSW Endpoint',
                flex: 3,
                dataIndex: 'serviceUrl',
                sortable: true
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [{
                    xtype:'button',
                    align : 'right',
                    text : 'Delete Selected',
                    scope: this,
                    handler : function(button,event){
                        var record = this.getSelectionModel().getSelection();
                        var checkboxgrp = Ext.getCmp('registryTabCheckboxGroup')
                        for(var i=0;i<record.length;i++){
                            var id = record[i].get('id');
                            this._deleteFromCookie(id);
                            this.getStore().remove(record[i]);

                            var checkBoxItems=checkboxgrp.items.items;
                            for(var j=0;j<checkBoxItems.length;j++){
                                if(checkBoxItems[j].inputValue.id && checkBoxItems[j].inputValue.id===id){
                                    checkboxgrp.remove(checkBoxItems[j]);
                                    break;
                                }
                            }

                        }
                    }
                }]
            }]
        });

        this.setRootNode(this._getRegistryFromCookie());
        this.callParent();
    },

    _getRegistryFromCookie : function(){

        var children=[];
        for(var i=1; i < 4 ; i++){
            var cookieRegistry = Ext.decode(Ext.util.Cookies.get('Registries' + i));
            if(cookieRegistry != null){
                var registry = {
                        id: cookieRegistry.id,
                        title: cookieRegistry.title,
                        serviceUrl:cookieRegistry.serviceUrl,
                        recordInformation:cookieRegistry.recordInformation,
                        active:false,
                        leaf: true, // this is a branch
                        expanded: false
                }
                children.push(registry);
            }
        }

        var data = {
           success : true,
           children : [{
               id : 'localRegistry',
               title : 'Registries',
               serviceUrl : '',
               recordInformation : '',
               active : false,
               leaf : false, // this is a branch
               expanded : true,
               children : children
           }]
       };
              

       return data;

    },

    _deleteFromCookie : function(id){
        for(var i=1; i < 4 ; i++){
            var registry = 'Registries' + i;

            var cookieRegistry = Ext.decode(Ext.util.Cookies.get('Registries' + i));
            if(cookieRegistry != null){
                if(cookieRegistry.id==id){
                    Ext.util.Cookies.clear(registry);
                }
            }
        }
    }
});