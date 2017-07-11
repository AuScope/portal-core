
Ext.define('portal.widgets.window.CSWFilterWindow', {
    extend : 'Ext.window.Window',

    cswFilterFormPanel : null,   
    
    constructor : function(cfg) {      
        
    	var me = this;
    	
    	var controlButtons = [];
    	
        // use the search panel defined in the config if present, otherwise use the Auscope Core default
    	me.cswFilterFormPanel = cfg.cswFilterFormPanel || new portal.widgets.panel.CSWFilterFormPanel({
            name : 'Filter Form'
        });
        
        // Only add a reset button if the search panel implements resetForm()
        if (me.cswFilterFormPanel.resetForm) {
        	controlButtons.push({
                xtype: 'button',
                text:'Reset Form',
                handler:function(button){
                    me.cswFilterFormPanel.resetForm();                        
                }
        	});
        };
        	
        controlButtons.push({            
            xtype: 'button',
            text: 'Search',
            scope : me,
            iconCls : 'add',
            handler: function(button) {
                var parent = button.findParentByType('window');
                var panel = parent.getComponent(0);

                if (panel.getForm().isValid()) {                 
                    var additionalParams = panel.getForm().getValues(false, false, false, false);
                    var filteredResultPanels=[];
                    for(additionalParamKey in additionalParams){
                        if(additionalParamKey == 'cswServiceId'){
                            if(!(additionalParams[additionalParamKey] instanceof Array)){
                                additionalParams[additionalParamKey]=[additionalParams[additionalParamKey]]
                            }
                            for(var j=0; j < additionalParams[additionalParamKey].length;j++){
                                //VT:
                                filteredResultPanels.push(this._getTabPanels(additionalParams,additionalParams[additionalParamKey][j]));
                            }
                        }
                    }
                    parent.fireEvent('filterselectcomplete',filteredResultPanels);
                    parent.hide();  
                    
                } else {
                    Ext.Msg.alert('Invalid Data', 'Please correct form errors.')
                }
            }
        });	
        
        Ext.apply(cfg, {
            title : 'Enter Parameters',
            layout : 'fit',
            modal : true,
            width : 500,
            items : [me.cswFilterFormPanel],
            buttons: controlButtons            
        });


        this.callParent(arguments);
    },
    
    /**
     * Return configuration for the tabpanels
     *
     * params - the parameter used to filter results for each tab panel
     * cswServiceId - The id of the csw registry.
     */
    _getTabPanels : function(params,cswServiceId) {
        //Convert our keys/values into a form the controller can read
        var keys = [];
        var values = [];
        var customRegistries=[];

        var additionalParams = params;

        //Utility function
        var denormaliseKvp = function(keyList, valueList, kvpObj) {
            if (kvpObj) {
                for (key in kvpObj) {
                    if (kvpObj[key]) {
                        var value = kvpObj[key].toString();
                        if(value.length>0 && key != 'cswServiceId' && !(key.slice(0, 4) == 'DNA_')){
                            keyList.push(key);
                            valueList.push(value);
                        }
                    }
                }
            }
        };


        denormaliseKvp(keys, values, additionalParams);
        if(typeof cswServiceId.id == 'undefined'){
            keys.push('cswServiceId');
            values.push(cswServiceId);
        }

      //Create our CSWRecord store (holds all CSWRecords not mapped by known layers)
        var filterCSWStore = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            pageSize: 35,
            autoLoad: false,
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            proxy : {
                type : 'ajax',
                url : 'getFilteredCSWRecords.do',
                reader : {
                    type : 'json',
                    rootProperty : 'data',
                    successProperty: 'success',
                    totalProperty: 'totalResults'
                },
                extraParams: {
                    key : keys,
                    value : values,
                    customregistries : {
                        id: cswServiceId.id,
                        title: cswServiceId.title,
                        serviceUrl: cswServiceId.serviceUrl,
                        recordInformationUrl: cswServiceId.recordInformationUrl
                    }
                }

            }

        });

        var registriesArray = Ext.getCmp('registryTabCheckboxGroup').getChecked();
        var title = "Error retrieving title";
        for(var i = 0; i < registriesArray.length; i ++){
            if(registriesArray[i].inputValue === cswServiceId){
                title = registriesArray[i].boxLabel;
                break;
            }
        }


        var result={
                title : title,
                xtype: 'cswrecordpagingpanel',
                layout : 'fit',
                store : filterCSWStore
            };

        return result;

    },
    
    // overridden close method to obtain a reference to the search window and close it as well
    close: function() {      
        this.hide();
    }
});

