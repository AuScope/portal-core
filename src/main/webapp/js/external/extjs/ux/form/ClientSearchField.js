/*!
 * Ext JS Library 3.1.0
 * Copyright(c) 2006-2009 Ext JS, LLC
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.ns('Ext.ux.form');

Ext.ux.form.ClientSearchField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent : function(){
        Ext.ux.form.ClientSearchField.superclass.initComponent.call(this);
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
        	this.store.clearFilter(false);
            this.triggers[0].hide();
			this.triggers[1].show();
            this.hasSearch = false;
			this.setDisabled(false);
            this.setValue('');
            
        }
    },

    onTrigger2Click : function(){
        var v = this.getRawValue();
        if(v.length < 1){
            this.onTrigger1Click();
            return;
        }

        this.store.filter(this.fieldName, v, true, false);
        this.hasSearch = true;
        this.triggers[0].show();
    },
    
    /**
     * text : The text to include in the box (to indicate that a custom filter has been run)
     * func : function(record, id) that should return true/false for each record it receives
     */
    runCustomFilter : function(text, func) {
    	//Clear any existing filter
    	this.onTrigger1Click();
    	
    	this.hasSearch = true;
    	this.setValue(text);
    	
    	this.store.filterBy(func);
    	this.triggers[0].show();
    	this.triggers[1].hide();
    	this.setDisabled(true);
    }
});