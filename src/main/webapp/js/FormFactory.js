//function FormFactory() {

/*Ext.override(Ext.form.Field, {
    hideItem :function(){
        this.formItem.addClass('x-hide-' + this.hideMode);
    },

    showItem: function(){
        this.formItem.removeClass('x-hide-' + this.hideMode);
    },
    setFieldLabel: function(text) {
    var ct = this.el.findParent('div.x-form-item', 3, true);
    var label = ct.first('label.x-form-item-label');
    label.update(text);
  }
});*/

FormFactory = function() {};

FormFactory.prototype.internalGenerateResult = function(form, supportsFiltering) {
	return {
		form				: form,
		supportsFiltering	: supportsFiltering
	};
};

/**
 * Given an activeLayersRecord, work out whether there is an appropriate filter form
 *
 * Returns a response in the form
 * {
 *    form : Ext.FormPanel - can be null - the formpanel to be displayed when this layer is selected
 *    supportsFiltering : boolean - whether this formpanel supports the usage of the filter button
 * }
 *
 */
FormFactory.prototype.getFilterForm = function(activeLayersRecord, map, cswRecordStore) {

	var cswRecords = activeLayersRecord.getCSWRecords();


    //We simplify things by treating the record list as a single type of WFS, WCS or WMS
    //So lets find the first record with a type we can choose (Prioritise WFS -> WCS -> WMS)
    if (cswRecords.length > 0) {
    	var cswRecord = cswRecords[0];
    	var id = activeLayersRecord.getId();
    	var parentKnownLayer = activeLayersRecord.getParentKnownLayer();

    	//a Known WFS may have a custom filter form
    	if (parentKnownLayer && parentKnownLayer.getType() === 'KnownLayerWFS') {
    	    //Pick out any known layers specifically by their ID
    	    switch (parentKnownLayer.getId()) {
    	        case 'pressuredb-borehole': return this.internalGenerateResult(new PressureDBFilterForm(id), true);
    	        case 'nvcl-borehole': return this.internalGenerateResult(new NvclFilterForm(id), true);
    	    }
    	    
    	    //Otherwise assign a filter form based on the feature type
            switch (parentKnownLayer.getFeatureTypeName()) {
                case 'er:Mine': return this.internalGenerateResult(new MineFilterForm(id), true); 
                case 'er:MiningActivity': return this.internalGenerateResult(new MiningActivityFilterForm(id), true); 
                case 'er:MineralOccurrence': return this.internalGenerateResult(new MineralOccurrenceFilterForm(id), true); 
                case 'gsml:GeologicUnit': return this.internalGenerateResult(new YilgarnGeochemistryFilterForm(id), true);
                case 'gsml:Borehole': return this.internalGenerateResult(new BoreholeFilterForm(id), true);
                default: return this.internalGenerateResult(null, false); 
            }
    	}

    	//Otherwise we may show opacity if there is an associated WMS (whether it is known or not)
    	var onlineResources = cswRecord.getFilteredOnlineResources('WMS');
    	if (onlineResources.length !== 0) {
    		return this.internalGenerateResult(new WMSLayerFilterForm(activeLayersRecord, map), false);
    	}

    	//WCS Records have no filter applied
    	onlineResources = cswRecord.getFilteredOnlineResources('WCS');
    	if (onlineResources.length !== 0) {
    		return this.internalGenerateResult(null, false);
    	}

    	//Otherwise we may just have a boring old metadata record
    	if (parentKnownLayer && parentKnownLayer.getType() === 'KnownLayerKeywords') {
	    	switch (parentKnownLayer.getDescriptiveKeyword()) {
	    		case 'Report':return this.internalGenerateResult(new ReportFilterForm(id, parentKnownLayer, cswRecordStore), true); 
	    		default: return this.internalGenerateResult(null, false); 
	    	}
    	}
    }

    return this.internalGenerateResult(null, false);
};