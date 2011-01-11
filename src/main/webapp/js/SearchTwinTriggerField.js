/*!
 * This class was borrowed from EXT JS library (Ext.ux.form.SearchField) 
 * and slightly customised for portal purposes. 
 * 
 * Ext JS Library 3.1.0
 * Copyright(c) 2006-2009 Ext JS, LLC
 * licensing@extjs.com
 * http://www.extjs.com/license
 * 
 * @version $Id$
 */
Ext.ns('Ext.ux.form');

Ext.ux.form.SearchTwinTriggerField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent : function(){
        Ext.ux.form.SearchTwinTriggerField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.onTrigger2Click();
            }
        }, this);
    },

    validationEvent:false,
    validateOnBlur:false,
    trigger1Class:'x-form-clear-trigger',
    trigger2Class:'x-form-search-trigger',
    hideTrigger1:true,
    width:180,
    hasSearch : false,
    paramName : 'query',
    fieldName : '',

    onTrigger1Click : function(){    	
        if(this.hasSearch){
        	this.reset();
            this.triggers[0].hide();
            this.hasSearch = false;
            this.store.removeAll();    
        }
    },

    onTrigger2Click : function(){
        var v = this.getRawValue();
        if(v.length < 1){
            this.onTrigger1Click();
            return;
        }
        
        pattern = /http(s)?:\/\/([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/;
        if(!(pattern.test(v))){
        	Ext.Msg.show({
    			title:'Error!',
    			msg: 'Url Is Not Valid',
    			buttons: Ext.Msg.OK
    		});
        	return;
        }
        
        if((v.match('request'))!==null || (v.match('version'))!==null){
        	Ext.Msg.show({
    			title:'Error!',
    			msg: 'The WMS URL specified cannot include a "version" or "request" parameter.<br/> You will have to remove these parameters before continuing',
    			buttons: Ext.Msg.OK
    		});
        	return;
        }
        
        //this.store.filter(this.fieldName, v, true, false);
        //this.store.load({ params : {service_URL : v} });
        this.store.setBaseParam('service_URL', v);
        this.store.load({
        	callback : function(records, options, success) {
        		if(success){
        			var invalidLayerCount = this.reader.jsonData.invalidLayerCount;
        			if (records.length !== 0){
        				if(invalidLayerCount > 0){
        					Ext.Msg.show({
                    			title:'Non EPSG:4326 WMS Layers!',
                    			msg: 'There are ' + invalidLayerCount + ' WMS layers that do not support EPSG:4326.<br/> These layers will not be available for display.',
                    			buttons: Ext.Msg.OK
                    		});
        				}
        				
        			}        			
        			else{
        				if(invalidLayerCount === 0){
	        				Ext.Msg.show({
	                			title:'No WMS Layers!',
	                			msg: 'There are no WMS Layers in the given URL',
	                			buttons: Ext.Msg.OK
	                		});
        				}else{
        					Ext.Msg.show({
                    			title:'Non EPSG:4326 WMS Layers!',
                    			msg: 'There are ' + invalidLayerCount + ' WMS layers that do not support EPSG:4326.<br/> These layers will not be available for display.',
                    			buttons: Ext.Msg.OK
                    		});
        				}
        			}        			
        		}
        		else{
        			Ext.Msg.show({
            			title:'Error!',
            			msg: 'Either the URL is not valid or it does not conform to EPSG:4326 WMS layers standard!',
            			buttons: Ext.Msg.OK
            		});
        		}
          }
        });
        this.hasSearch = true;
        this.triggers[0].show();
    }
});