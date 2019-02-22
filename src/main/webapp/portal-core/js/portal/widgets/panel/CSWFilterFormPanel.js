/**
 * This is the panel for the form that pops up when you click on customize for the personal panel
 *
 */
Ext.define('portal.widgets.panel.CSWFilterFormPanel', {
    extend : 'Ext.form.Panel',
    alias: 'widget.cswfilterformpanel',


    keywordStore : null,
    keywordIDCounter : 0,
    spacerHeight : 22,
    keywordMatchTypeStore : null,
    miniMap : null,
    boxLayer : null,

    constructor : function(cfg){



        this.keywordStore = new Ext.data.Store({
            autoload: true,
            fields: ['keyword', 'count'],
            proxy : {
                type : 'ajax',
                url : 'getFilteredCSWKeywords.do',
                extraParams : {
                    cswServiceIds : []
                 },
                reader : {
                    type : 'json',
                    rootProperty : 'data'
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





        Ext.apply(cfg, {
            xtype : 'form',
            id : 'personalpanelcswfilterform',
            width : 500,
            autoScroll :true,
            border : false,
            height : 520,
            items : [{
                     xtype:'tabpanel',
                     layout: 'fit',
                     items : [
                              this.getRegistryTab(),
                              this.getGeneralTab(),
                              this.getSpatialTab()
                         ]
            }]
        });


        this.callParent(arguments);


    },



    getGeneralTab : function() {
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
                        xtype : 'label',
                        html : '<font size="0.7" color="red">The keywords here are generated dynamically based on the NON CUSTOM registries that are ticked on the Registries Filter Tab</font>'

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
                },{
                    xtype:'fieldset',
                    title : 'Text Search',
                    items:[{
                        xtype : 'textfield',
                        name : 'title',
                        fieldLabel : 'Search Title'
                    },{
                        xtype : 'textfield',
                        name : 'abstract',
                        fieldLabel : 'Search abstract'
                    }]
                }]
        };

        return generalTab;
    },





    getSpatialTab : function(){
        var cswFilterFormPanelMe = this;

        var spatialTab ={
                title : 'Spatial filter',
                layout : 'fit',
                autoScroll :false,
                items : [{
                    xtype : 'fieldset',
                    itemId : 'cswspatialfiltercoordfieldset',
                    title : 'Coordinates',
                    items : [{
                        xtype : 'textfield',
                        name : 'north',
                        itemId : 'north',
                        fieldLabel : 'North'
                    },{
                        xtype : 'textfield',
                        name : 'south',
                        itemId : 'south',
                        fieldLabel : 'South'
                    },{
                        xtype : 'textfield',
                        name : 'east',
                        itemId : 'east',
                        fieldLabel : 'East'
                    },{
                        xtype : 'textfield',
                        name : 'west',
                        itemId : 'west',
                        fieldLabel : 'West'
                    },{
                        xtype : 'button',
                        toggle : true,
                        text: 'Draw Bounds',
                        handler: function() {
                            var myMap = cswFilterFormPanelMe.miniMap;
                            for(var i in myMap.controls){
                                if(myMap.controls[i] instanceof OpenLayers.Control.DrawFeature){
                                    //VT : get a hold of the DrawFeature and toggle it.
                                    if(this.toggle == true){
                                        this.toggle = false;
                                        this.setText('Clear bounds')
                                        myMap.controls[i].activate();
                                    }else{
                                        this.toggle = true;
                                        this.setText('Draw Bounds')
                                        cswFilterFormPanelMe.boxLayer.removeAllFeatures();
                                        this.ownerCt.getComponent('north').setValue('');
                                        this.ownerCt.getComponent('south').setValue('');
                                        this.ownerCt.getComponent('east').setValue('');
                                        this.ownerCt.getComponent('west').setValue('');
                                        myMap.controls[i].deactivate();
                                    }

                                }
                            }
                        }

                    }]
                },{
                    xtype:'panel',
                    id: 'cswminimapselection',
                    width : 500,
                    height : 800,
                    html : "<div style='width:100%; height:100%' id='cswselection-mini-map'></div>",
                    listeners : {
                        afterrender : function() {
                            cswFilterFormPanelMe._getMap(this,'cswselection-mini-map');
                        }
                    }
                }]
        };

        return spatialTab
    },



    getRegistryTab : function(){
        var me = this;
        var registriesTab = {
                title : 'Registries Filter',
                xtype : 'panel',
                type: 'vbox',
                items:[{
                    xtype:'fieldset',
                    title:'Default Registries',
                    flex : 1,
                    items:[{
                        xtype: 'checkboxgroup',
                        name : 'cswServiceId',
                        id : 'registryTabCheckboxGroup',
                        fieldLabel: 'Registries',
                        // Arrange checkboxes into two columns, distributed vertically
                        columns: 1,
                        vertical: true,
                        listeners : {
                            change : function(scope,newValue, oldValue, eOpts ){
                                me.keywordStore.getProxy().extraParams = {
                                    cswServiceIds : scope.getValue().cswServiceId
                                };
                            },        
                            //VT: Stop it from adding the same url twice.
                            beforeadd : function(scope, component, index, eOpts){      
                                var addItem = true
                                scope.items.each(function(item,index,len){
                                    if(this.inputValue.serviceUrl && (this.inputValue.serviceUrl===component.inputValue.serviceUrl)){
                                        Ext.MessageBox.alert('Status', 'You are attempting to add a duplicated service URL');
                                        addItem = false;
                                        return false;
                                    }
                                })
                                return addItem;
                            }
                        }

                    }]

                },{
                    xtype:'fieldset',
                    title:'Add custom registry',
                    collapsible : true,
                    items:[{
                        xtype: 'form',
                        itemId : 'customRegistryFormID',
                        border: false,
                        flex : 1,
                        buttonAlign : 'left',
                        buttons:[{
                            xtype : 'button',
                            text : 'Manage Saved Registries',
                            handler : function(){
                                var CustomRegistryTreeGridPanel = new portal.widgets.panel.CustomRegistryTreeGrid();
                                Ext.create('Ext.window.Window', {
                                    title: 'Manage Saved Registries',
                                    height: 200,
                                    width: 600,
                                    layout: 'fit',
                                    modal: true,
                                    items: [CustomRegistryTreeGridPanel]
                                }).show();
                            }
                        },{
                            xtype : 'button',
                            text : 'Save Registry',
                            disabled : true,
                            itemId : 'cswFilterFormSaveRegistryButton',
                            tooltip : 'Add custom registry and save it to the cookies for future use.',
                            scope : this,
                            handler : function(){                                
                                //VT:_addFormToRegistry(true,false) true to add to cookies, false to not add to checkgroup
                                this._addFormToRegistry(true,false);
                            }
                        },{
                            xtype: 'tbfill'
                        },{
                            xtype : 'button',
                            text : 'Add Registry',
                            tooltip : 'Add custom registry to the registry list',
                            scope : this,
                            handler : function(){
                                this._addFormToRegistry(false,true);                                                             
                            }
                        }],
                        items:[{
                            xtype : 'textfield',
                            anchor:'100%',
                            itemId : 'cswFilterFormServiceURLTextField',
                            name : 'DNA_serviceUrl',
                            allowBlank: true,
                            fieldLabel : 'Service Url',
                            emptyText: 'CSW url in the format: http://test/gn/srv/eng/csw',
                            triggers: {
                                foo: {
                                    cls: 'x-form-clear-trigger',
                                    handler: function() {
                                        this.setRawValue('');
                                        me._setAllowRegistryAdd();
                                    }
                                }                       
                            }
                        }]
                    }]
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
                    rootProperty : 'data'
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
                    var registry=Ext.getCmp('registryTabCheckboxGroup');
                    registry.add(checkBoxItems);

                    var cookieRegistries = me._getCustomRegistriesCookies();
                    me._addCookieToRegistry(cookieRegistries);

                    me.keywordStore.getProxy().extraParams = {
                        cswServiceIds : registry.getValue().cswServiceId
                    };
                }
            }

        });
        cswServiceItemStore.load();

        return registriesTab;
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
            xtype : 'combo',
            width : 380,
            name : 'keywords',
            queryMode : 'remote',
            typeAhead: true,
            style: {
                marginBottom: '0px'
            },
            typeAheadDelay : 500,
            forceSelection : false,
            //triggerAction : 'all',
            queryParam: 'keyword',
            valueField:'keyword',
            fieldLabel : 'keyword',
            store :    this.keywordStore,
            listeners: {
                beforequery: function(qe){
                    delete qe.combo.lastQuery;
                }
            },
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

    },



    _getMap : function(container,divId){
        var containerId = divId;
        var map = new OpenLayers.Map(containerId, {
            projection: 'EPSG:3857',
            controls : [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.PanZoomBar({zoomStopHeight:8})
            ],
            layers: [
                     new OpenLayers.Layer.Google(
                             "Google Hybrid",
                             {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
                         ),
                     new OpenLayers.Layer.Google(
                         "Google Physical",
                         {type: google.maps.MapTypeId.TERRAIN}
                     ),
                     new OpenLayers.Layer.Google(
                         "Google Streets", // the default
                         {numZoomLevels: 20}
                     ),
                     new OpenLayers.Layer.Google(
                         "Google Satellite",
                         {type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22}
                     )
                 ],
                 center: new OpenLayers.LonLat(133.3, -61)
                     // Google.v3 uses web mercator as projection, so we have to
                     // transform our coordinates
                     .transform('EPSG:4326', 'EPSG:3857'),
                 zoom: 3
        });



        this.boxLayer = new OpenLayers.Layer.Vector("Box layer");
        map.addLayer(this.boxLayer);


        var box= new OpenLayers.Control.DrawFeature(this.boxLayer,
                OpenLayers.Handler.RegularPolygon, {
                    handlerOptions: {
                        sides: 4,
                        irregular: true
                    }
                }
            )
        map.addControl(box);


      //We need to listen for when a feature is drawn and act accordingly
        box.events.register('featureadded', {}, Ext.bind(function(e,c){
            var ctrl = e.object;
            var feature = e.feature;

            //raise the data selection event
            var originalBounds = feature.geometry.getBounds();
            var bounds = originalBounds.transform('EPSG:3857','EPSG:4326').toArray();
            var spatialCoordFieldSet = c.ownerLayout.owner.getComponent('cswspatialfiltercoordfieldset');
            spatialCoordFieldSet.getComponent('north').setValue(bounds[3]);
            spatialCoordFieldSet.getComponent('south').setValue(bounds[1]);
            spatialCoordFieldSet.getComponent('east').setValue(bounds[2]);
            spatialCoordFieldSet.getComponent('west').setValue(bounds[0]);





            //Because click events are still 'caught' even if the click control is deactive, the click event
            //still gets fired. To work around this, add a tiny delay to when we reactivate click events
            var task = new Ext.util.DelayedTask(Ext.bind(function(ctrl){
                ctrl.deactivate();
            }, this, [ctrl]));
            task.delay(50);
        }, this,container,true));







        container.on('resize', function() {
            map.updateSize();
        }, this);

        this.miniMap = map;

    },

    /**
     * addToCookie if set to true will also add this registry to cookie
     */
    _addFormToRegistry : function(addToCookie, updateCheckGroup){

        var customRegistryForm = this.query('form[itemId=customRegistryFormID]')[0]

        portal.util.Ajax.request({
            url: 'getCSWGetCapabilities.do',
            scope : this,
            params: {
                cswServiceUrl: customRegistryForm.getValues().DNA_serviceUrl
            },
            callback : function(success, data) {
                //Check for errors
                //VT: title should be extracted from the response;
                if (success) {
                    //VT if title == null pop up box to ask user for title;
                    var title = data.title;


                    var registry=Ext.getCmp('registryTabCheckboxGroup');
                    var checkBoxItems = [];
                    var cswUrl = customRegistryForm.getValues().DNA_serviceUrl;

                    var registryEntity = {
                        id:'randomIdGen_' + Ext.id(),
                        title:title,
                        serviceUrl:cswUrl,
                        recordInformationUrl : this._covertCSWtoRecordInfoUrl(cswUrl)
                    }

                    checkBoxItems.push({
                        boxLabel : title,
                        name : 'cswServiceId',
                        inputValue: {
                            id: registryEntity.id,
                            title: registryEntity.title,
                            serviceUrl:registryEntity.serviceUrl,
                            recordInformationUrl : registryEntity.recordInformationUrl
                        },
                        checked : true
                    });

                    if(updateCheckGroup && updateCheckGroup){
                        registry.add(checkBoxItems);
                        this._setAllowRegistryAdd(true);
                    }

                    if(addToCookie && addToCookie==true){
                        if(!(this._saveToCookie(registryEntity))){
                            Ext.Msg.alert('WARNING', 'Only a maximum of 3 registry are allowed to be store locally due to space limitation. This record is not saved but will be added to the registries above. Click on "Manage Saved Registries" to delete');
                        }else{
                            Ext.Msg.alert('Status', 'Registry saved locally. Click on "Manage Saved Registries" to delete');
                        }
                    }

                }else{
                    Ext.Msg.alert('WARNING', 'Failure to connect to the registry. Check your URL and ensure it is in the right format e.g http://test/gn/srv/eng/csw');
                }

            }
        });
    },
    
    _setAllowRegistryAdd : function(allowAdd){
        var textfield = this.query('[itemId="cswFilterFormServiceURLTextField"]')[0]; 
        var button = this.query('[itemId="cswFilterFormSaveRegistryButton"]')[0] ;
        
        if(allowAdd){            
            button.enable();
            textfield.setEditable(false);
            //textfield.setFieldStyle('background-color: #E6E6E6; background-image: none; opacity: 0.8;');
            textfield.addCls('portal-ux-textfield-disabled');
        }else{
            button.disable();
            textfield.setEditable(true);
            textfield.removeCls('portal-ux-textfield-disabled');
        }
    },

    _covertCSWtoRecordInfoUrl:function(cswUrl){
        if(cswUrl.substring(cswUrl.length -3,cswUrl.length).toLowerCase()=='csw'){
            return cswUrl.slice(0,cswUrl.length - 3) + 'main.home';
        }
        return cswUrl;
    },

    _addCookieToRegistry : function(registry){

        if(!(registry instanceof Array)){
            registry=[registry]
        }
        var formRegistry=Ext.getCmp('registryTabCheckboxGroup');
        var checkBoxItems = [];
        for(var i=0;i<registry.length;i++){
            checkBoxItems.push({
                boxLabel : registry[i].title,
                name : 'cswServiceId',
                inputValue: {
                    id:registry[i].id,
                    title:registry[i].title,
                    serviceUrl:registry[i].serviceUrl,
                    recordInformationUrl : registry[i].recordInformationUrl
                },
                checked : false
            });
        }

        formRegistry.add(checkBoxItems);

    },

    _saveToCookie : function(registry){
        if(Ext.util.Cookies.get('Registries1')==null || Ext.util.Cookies.get('Registries1')=='null'){
            Ext.util.Cookies.set('Registries1',Ext.encode(registry))
            return true;
        }else if(Ext.util.Cookies.get('Registries2')==null || Ext.util.Cookies.get('Registries2')=='null'){
            Ext.util.Cookies.set('Registries2',Ext.encode(registry))
            return true;
        }else if(Ext.util.Cookies.get('Registries3')==null || Ext.util.Cookies.get('Registries3')=='null'){
            Ext.util.Cookies.set('Registries3',Ext.encode(registry))
            return true;
        };

        return false;

    },

    _getCustomRegistriesCookies : function(){
        var children=[];
        for(var i=1; i < 4 ; i++){
            var cookieRegistry = Ext.decode(Ext.util.Cookies.get('Registries' + i));
            if(cookieRegistry != null){
                var registry = {
                        id: cookieRegistry.id,
                        title: cookieRegistry.title,
                        serviceUrl:cookieRegistry.serviceUrl,
                        recordInformation:cookieRegistry.recordInformationUrl
                }
                children.push(registry);
            }
        }
        return children;

    }


});
