/**
 * A simple wrapper for records in ActiveLayersStore that provides a number of useful helper methods
 */
ActiveLayersRecord = function(dataStoreRecord) {
	this.internalRecord = dataStoreRecord;
};


ActiveLayersRecord.prototype.internalRecord = null;
ActiveLayersRecord.prototype.internalGetStringField = function(fieldName) {
	var str = this.internalRecord.get(fieldName);
	if (!str) {
		return '';
	}
	
	return str;
};
ActiveLayersRecord.prototype.internalGetArrayField = function(fieldName) {
	var arr = this.internalRecord.get(fieldName);
	if (!arr) {
		return [];
	}
	
	return arr;
};
ActiveLayersRecord.prototype.internalGetBooleanField = function(fieldName) {
	var b = this.internalRecord.get(fieldName);
	if (b == null || b == undefined) {
		return false;
	}
	
	return b;
};
ActiveLayersRecord.prototype.internalGetNumberField = function(fieldName, defaultValue) {
	var num = this.internalRecord.get(fieldName);
	if (num == null || num == undefined) {
		return defaultValue;
	}
	
	return num;
};


/**
 * Gets the id of this active layer as a String
 */
ActiveLayersRecord.prototype.getId = function() {
	return this.internalGetStringField('id');
};

/**
 * Gets the title of this active layer as a String
 */
ActiveLayersRecord.prototype.getTitle = function() {
	return this.internalGetStringField('title');
};

/**
 * Gets the description of this active layer as a String
 */
ActiveLayersRecord.prototype.getDescription = function() {
	return this.internalGetStringField('description');
};

/**
 * Gets the Proxy URL of this active layer as a String (or null)
 * 
 * The Proxy URL is the url that should be queried instead of the onlineResource URL.
 */
ActiveLayersRecord.prototype.getProxyUrl = function() {
	return this.internalRecord.get('proxyUrl');
};

/**
 * Gets an array of CSWRecord objects that represent this layer
 */
ActiveLayersRecord.prototype.getCSWRecords = function() {
	return this.internalGetArrayField('cswRecords');
};

/**
 * Gets an array of CSWRecord objects which have at least one online resource with the specified type
 * 
 * type: One of ['WFS', 'WMS', 'WCS', 'OPeNDAP']
 */
ActiveLayersRecord.prototype.getCSWRecordsWithType = function(type) {
	var unfilteredRecords = this.getCSWRecords();
	var filteredRecords = [];
	
	for (var i = 0; i < unfilteredRecords.length; i++) {
		var onlineRess = unfilteredRecords[i].getOnlineResources();

		//Search for a matching type
		for (var j = 0; j < onlineRess.length; j++) {
			if (onlineRess[j].onlineResourceType === type) {
				filteredRecords.push(unfilteredRecords[i]);
				break;
			}
		}
	}
	
	return filteredRecords;
};

/**
 * Gets the URL of this active layer's icon as a String (Can be null/empty)
 */
ActiveLayersRecord.prototype.getIconUrl = function() {
	return this.internalRecord.get('iconUrl');
};

/**
 * Gets the html representation of the key column as a String
 */
ActiveLayersRecord.prototype.getKeyIconHtml = function() {
	return this.internalGetStringField('keyIconHtml');
};

/**
 * Gets whether this record is still loading or not as a boolean
 */
ActiveLayersRecord.prototype.getIsLoading = function() {
	return this.internalGetBooleanField('isLoading');
};

/**
 * Sets whether this record is still loading or not as a boolean
 */
ActiveLayersRecord.prototype.setIsLoading = function(isLoading) {
	this.internalRecord.set('isLoading', isLoading);
};

/**
 * Gets whether this record is visible or not as a boolean
 */
ActiveLayersRecord.prototype.getLayerVisible = function() {
	return this.internalGetBooleanField('layerVisible');
};

/**
 * Sets whether this record is visible or not as a boolean
 */
ActiveLayersRecord.prototype.setLayerVisible = function(layerVisible) {
	this.internalRecord.set('layerVisible', layerVisible);
};

/**
 * Gets the html representation of the download column as a String
 */
ActiveLayersRecord.prototype.getDownloadIconHtml = function() {
	return this.internalGetStringField('downloadIconHtml');
};

/**
 * Gets the numerical representation of the Opacity as a Number in the range [0,1]
 */
ActiveLayersRecord.prototype.getOpacity = function() {
	return this.internalGetNumberField('opacity', 1);
};

/**
 * Gets the numerical representation of the Opacity as a Number in the range [0,1]
 */
ActiveLayersRecord.prototype.setOpacity = function(opacity) {
	if (opacity < 0) {
		opacity = 0;
	} else if (opacity > 1) {
		opacity = 1;
	}
		
	this.internalRecord.set('opacity', opacity);
};

/**
 * Gets an instance of OverlayManager or null
 */
ActiveLayersRecord.prototype.getOverlayManager = function() {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.overlayManager;
};

/**
 * Sets an instance of OverlayManager or null
 */
ActiveLayersRecord.prototype.setOverlayManager = function(overlayManager) {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.overlayManager = overlayManager;
};

/**
 * Gets an instance of a FormFactory.getFilterForm response or null
 */
ActiveLayersRecord.prototype.getFilterPanel = function() {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.filterPanel;
};

/**
 * Sets an instance of FormFactory.getFilterForm response or null
 */
ActiveLayersRecord.prototype.setFilterPanel = function(filterPanel) {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.filterPanel = filterPanel;
};

/**
 * Gets an instance of ResponseToolTip or null
 */
ActiveLayersRecord.prototype.getResponseToolTip = function() {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.responseToolTip;
};

/**
 * Sets an instance of ResponseToolTip or null
 */
ActiveLayersRecord.prototype.setResponseToolTip = function(responseToolTip) {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.responseToolTip = responseToolTip;
};

/**
 * Gets an instance of Ext.Window or null
 * 
 * It represents the popup window with legend information for this active layer
 */
ActiveLayersRecord.prototype.getLegendWindow = function() {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.legendWindow;
};

/**
 * Sets an instance of Ext.Window or null
 * 
 * It represents the popup window with legend information for this active layer
 */
ActiveLayersRecord.prototype.setLegendWindow = function(legendWindow) {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.legendWindow = legendWindow;
};

/**
 * Gets an instance of DebuggerData or null
 * 
 * It represents some debug information associated with this layer
 */
ActiveLayersRecord.prototype.getDebuggerData = function() {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.debuggerData;
};

/**
 * Sets an instance of DebuggerData or null
 * 
 * It represents some debug information associated with this layer
 */
ActiveLayersRecord.prototype.setDebuggerData = function(debuggerData) {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	return this.internalRecord.debuggerData = debuggerData;
};

/**
 * Gets an instance of KnownLayerRecord or null
 * 
 * It represents the KnownLayer 'owns' the CSWRecords of this active layer 
 * (not all layers will be created from an active layer) 
 */
ActiveLayersRecord.prototype.getParentKnownLayer = function() {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	var rec = this.internalRecord.parentKnownLayer;
	if (rec) {
		return new KnownLayerRecord(rec);
	}
	
	return null;
};

/**
 * Sets an instance of KnownLayerRecord or null
 * 
 * It represents the KnownLayer that 'owns' the CSWRecords of this active layer 
 * (not all layers will be created from an active layer) 
 */
ActiveLayersRecord.prototype.setParentKnownLayer = function(knownLayerRecord) {
	//We are forced to read/write directly to the record because this 
	//field is too complex to be serialized using JSON
	if (knownLayerRecord) {
		this.internalRecord.parentKnownLayer = knownLayerRecord.internalRecord;
	} else {
		this.internalRecord.parentKnownLayer = null;
	}
};
