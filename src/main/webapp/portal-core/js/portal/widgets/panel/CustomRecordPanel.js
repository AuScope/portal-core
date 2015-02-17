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
            this.addDocked({
                xtype: 'toolbar',
                dock: 'top',
                items: [{
                    xtype : 'label',
                    text : 'WMS Url:'
                },{
                    xtype : 'searchfield',
                    store : this.getStore(),
                    width : 190,
                    name : 'STTField',
                    paramName: 'service_URL',
                    emptyText : 'http://'
                },{
                    xtype:'tbseparator'
                },{
                    xtype : 'button',
                    text:'Registry',
                    iconCls : 'magglass',
                    itemId: 'browseCatalogue',
                    tooltip:'Browse and filter through the available catalogue',                                      
                    scope:this,
                    handler: function(btn) {
                        //VT: TODO use BrowserWindowWithWarning.js
                        if(me.browseCatalogueDNSMessage==true){
                            var cswFilterWindow = new portal.widgets.window.CSWFilterWindow({
                                name : 'CSW Filter',
                                listeners : {
                                    filterselectcomplete : Ext.bind(this.handleFilterSelectComplete, this)
                                }
                            });
                            cswFilterWindow.show();
                        }else{
                            Ext.MessageBox.show({
                                title:    'Browse Catalogue',
                                msg:      'Select the filters across the tabs and once you are happy with the result, click on OK to apply all the filters<br><br><input type="checkbox" id="do_not_show_again" value="true" checked/>Do not show this message again',
                                buttons:  Ext.MessageBox.OK,
                                scope : this,
                                fn: function(btn) {
                                    if( btn == 'ok') {
                                        if (Ext.get('do_not_show_again').dom.checked == true){
                                            me.browseCatalogueDNSMessage=true;
                                        }
                                        var cswFilterWindow = new portal.widgets.window.CSWFilterWindow({
                                            name : 'CSW Filter',
                                            listeners : {
                                                filterselectcomplete : Ext.bind(this.handleFilterSelectComplete, this)
                                            }
                                        });
                                        cswFilterWindow.show();
                                    }
                                }
                            });
                        }

                    }//VT:End handler

                   
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
       
    }

});