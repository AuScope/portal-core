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

        //this.store.filter(this.fieldName, v, true, false);
        //this.store.load({ params : {service_URL : v} });
        this.store.setBaseParam('service_URL', v);
        this.store.load();
        this.hasSearch = true;
        this.triggers[0].show();
    }
});