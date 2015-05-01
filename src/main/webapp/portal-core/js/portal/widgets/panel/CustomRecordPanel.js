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
            var menuItems = [this._getRegistryAction(),this._getHandleKMLAction()];
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
    
    addKMLtoPanel : function(name,file){
        var csw = Ext.create('portal.csw.CSWRecord',{
            id : Ext.id(),
            name : name,
            resourceProvider : 'kml',
            geographicElements : [Ext.create('portal.util.BBox',{
                eastBoundLongitude : 180,
                westBoundLongitude : -180,
                northBoundLatitude : 90,
                southBoundLatitude : -90
            })],
            constraints : [],
            extensions : file,
            recordInfoUrl : "",
            noCache : true
        })
        csw.set('customlayer',true);
        this.getStore().insert(0,csw);
        return csw;
    },
    
   
    /**
     * This returns a file window download box to allow users to add or point to a kml file. 
     */
    _getKMLFileDialog : function(){
        var me = this;
        var panel1 = Ext.create('Ext.form.Panel', {  
            title : 'KML File',
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

                               var tabpanel =  Ext.getCmp('auscope-tabs-panel');
                               var customPanel = me.ownerCt.getComponent('org-auscope-custom-record-panel');
                               tabpanel.setActiveTab(customPanel);
                               
                               customPanel.addKMLtoPanel(o.result.name,o.result.file);
                               
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
        
        var panel2 = Ext.create('Ext.form.Panel', {          
            bodyPadding: 15,
            title : 'KML URL',
            frame: true,            
            items: [{
                xtype: 'textfield',
                value : 'https://capdf-dev.csiro.au/gs-hydrogeochem/public/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=public:hydrogeochem&maxFeatures=50&outputFormat=application/vnd.google-earth.kml+xml',
                name: 'file',
                fieldLabel: 'URL',
                labelWidth: 50,
                msgTarget: 'side',
                allowBlank: false,
                anchor: '100%'
            }],

            buttons: [{
                text: 'Add KML',
                handler: function() {
                    var form = this.up('form').getForm();
                    if(form.isValid()){
                        form.submit({
                            url: 'addKMLUrl.do',
                            params:{
                              url : form.getFieldValues().file  
                            },
                            waitMsg: 'Adding KML Layer...',
                            success: function(fp, o) {                              

                               var tabpanel =  Ext.getCmp('auscope-tabs-panel');
                               var customPanel = me.ownerCt.getComponent('org-auscope-custom-record-panel');
                               tabpanel.setActiveTab(customPanel);
                               
                               customPanel.addKMLtoPanel(o.result.data.name,o.result.data.file);
                                                              
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
        
        
        var tabpanel = Ext.create('Ext.tab.Panel', {            
            items: [panel1,panel2]
        }); 
        
        return Ext.create('Ext.window.Window', {
            title: 'KML Input',
            height: 190,
            width: 400,
            frameHeader : false,
            layout: 'fit',
            items: tabpanel
        })
    }
    
    

});