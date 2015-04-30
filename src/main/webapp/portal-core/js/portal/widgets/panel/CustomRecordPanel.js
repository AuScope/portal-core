/**
 * A specialisation of portal.widgets.panel.CSWRecordPanel for rendering
 * records that are loaded directly from an external WMS source
 */
Ext.define('portal.widgets.panel.CustomRecordPanel', {
    extend : 'portal.widgets.panel.CSWRecordPanel',

    enableBrowse : false,
    
    
    constructor : function(cfg) {
        this.callParent(arguments);
        this.on('afterrender', this._loadQueryBar, this);        
        this.enableBrowse = cfg.enableBrowse;
           
        
    },
    

    _loadQueryBar : function() {        
        this._updateSearchBar(false);
        
        var me = this;
        if(this.enableBrowse){
            var menuItems = [this._getRegistryAction(),this._getHandleKMLAction(),this._getClearAllKMLAction()];
            this.addDocked({
                xtype: 'toolbar',
                dock: 'top',
                items: [{
                    xtype : 'label',
                    text : 'WMS Url:'
                },{
                    xtype : 'wmscustomsearchfield',
                    store : this.getStore(),
                    width : 190,
                    name : 'STTField',
                    paramName: 'service_URL',
                    emptyText : 'http://'
                },{
                    xtype:'tbseparator'
                },{
                    xtype : 'button',
                    text      : 'More',
                    iconCls    :   'setting fa-spin',
                    arrowAlign: 'right',
                    menu      : menuItems  
                }]
            });
        }else{
            this.addDocked({
                xtype: 'toolbar',
                dock: 'top',
                items: [{
                    xtype : 'label',
                    text : 'WMS Url:'
                },{
                    xtype : 'searchfield',
                    store : this.getStore(),
                    width : 243,
                    name : 'STTField',
                    paramName: 'service_URL',
                    emptyText : 'http://'
                }]
            });
        }
       
    },
    
    _getRegistryAction : function(){
        var baseform = this.filterForm;
        
        var me = this;
        
        return new Ext.Action({
            xtype : 'button',
            text:'Registry',
            iconCls : 'magglass',
            itemId: 'browseCatalogue',
            tooltip:'Browse and filter through the available catalogue',                                      
            scope:me,
            handler: function(btn) {
                //VT: TODO use BrowserWindowWithWarning.js
                if(me.browseCatalogueDNSMessage==true){
                    var cswFilterWindow = new portal.widgets.window.CSWFilterWindow({
                        name : 'CSW Filter',
                        listeners : {
                            filterselectcomplete : Ext.bind(me.handleFilterSelectComplete, me)
                        }
                    });
                    cswFilterWindow.show();
                }else{
                    Ext.MessageBox.show({
                        title:    'Browse Catalogue',
                        msg:      'Select the filters across the tabs and once you are happy with the result, click on OK to apply all the filters<br><br><input type="checkbox" id="do_not_show_again" value="true" checked/>Do not show this message again',
                        buttons:  Ext.MessageBox.OK,
                        scope : me,
                        fn: function(btn) {
                            if( btn == 'ok') {
                                if (Ext.get('do_not_show_again').dom.checked == true){
                                    me.browseCatalogueDNSMessage=true;
                                }
                                var cswFilterWindow = new portal.widgets.window.CSWFilterWindow({
                                    name : 'CSW Filter',
                                    listeners : {
                                        filterselectcomplete : Ext.bind(me.handleFilterSelectComplete, me)
                                    }
                                });
                                cswFilterWindow.show();
                            }
                        }
                    });
                }

            }//VT:End handler

        })
        
    },
    
    _getHandleKMLAction : function(){
        var baseform = this.filterForm;
        var me = this;
        return new Ext.Action({
            text : 'Add KML ',
            iconCls : 'fa fa-file-code-o fa-gray-icon',
            handler : function(){
                me._getKMLFileDialog().show();
            }
        })
        
    },
    
    _getClearAllKMLAction : function(){
       
        var me = this;
        return new Ext.Action({
            text : 'Remove All KML ',
            iconCls : 'fa fa-remove fa-red-icon',
            handler : function(){
              me.map.removeAllKMLLayer();
            }
        })
        
    },
    
    _getKMLFileDialog : function(){
        var me = this;
        var panel = Ext.create('Ext.form.Panel', {          
            bodyPadding: 15,
            frame: true,            
            items: [{
                xtype: 'filefield',
                name: 'file',
                fieldLabel: 'KML',
                labelWidth: 50,
                msgTarget: 'side',
                allowBlank: false,
                anchor: '100%',
                buttonText: 'Select KML...'
            }],

            buttons: [{
                text: 'Add KML',
                handler: function() {
                    var form = this.up('form').getForm();
                    if(form.isValid()){
                        form.submit({
                            url: 'addKMLLayer.do',
                            waitMsg: 'Adding KML Layer...',
                            success: function(fp, o) {                              
                               me.map.addKMLFromString(o.result.name, o.result.file);                               
                               this.form.owner.up('window').close();
                            },
                            failure : function(fp,action){
                                Ext.Msg.alert('Status', 'Unable to parse file. Make sure the file is a valid KML file.');
                            }
                        });
                    }
                }
            }]
        });
        
        return Ext.create('Ext.window.Window', {
            title: 'Select KML',
            height: 150,
            width: 400,
            layout: 'fit',
            items: panel
        })
    }
    
    

});