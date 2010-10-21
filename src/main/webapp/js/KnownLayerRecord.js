/**
 * A representation of a KnownLayerRecord in the user interface (as returned by the getKnownFeatures.do handler).
 */
KnownLayerRecord = function(dataStoreRecord) {
	this.internalRecord = dataStoreRecord;
};


KnownLayerRecord.prototype.internalRecord = null;
KnownLayerRecord.prototype.internalGetStringField = function(fieldName) {
	var str = this.internalRecord.get(fieldName);
	if (!str) {
		return '';
	}
	
	return str;
};
KnownLayerRecord.prototype.internalGetBooleanField = function(fieldName) {
	var b = this.internalRecord.get(fieldName);
	if (b == null || b == undefined) {
		return false;
	}
	
	return b;
};

/**
 * Gets the type of this known layer.
 * 
 * Can be ['KnownLayerWFS', 'KnownLayerWMS', 'KnownLayerKeywords']
 */
KnownLayerRecord.prototype.getType = function() {
	return this.internalGetStringField('type');
};

/**
 * Gets the id of this known layer as a String.
 */
KnownLayerRecord.prototype.getId = function() {
	return this.internalGetStringField('id');
};

/**
 * Gets the feature type as a String. Only valid if type=='KnownLayerWFS'
 */
KnownLayerRecord.prototype.getFeatureTypeName = function() {
	return this.internalGetStringField('featureTypeName');
};

/**
 * Gets whether bounding box filtering should be disabled for this known layer. 
 * Only valid if type=='KnownLayerWFS'
 */
KnownLayerRecord.prototype.getDisableBboxFiltering = function() {
	return this.internalGetStringField('disableBboxFiltering');
};

/**
 * Gets the display name as a String
 */
KnownLayerRecord.prototype.getTitle = function() {
	return this.internalGetStringField('title');
};

/**
 * Gets the description as a String
 */
KnownLayerRecord.prototype.getDescription = function() {
	return this.internalGetStringField('description');
};

/**
 * Gets the proxy URL as a String. Only valid if type=='KnownLayerWFS'
 */
KnownLayerRecord.prototype.getProxyUrl = function() {
	return this.internalGetStringField('proxyUrl');
};

/**
 * Gets the icon URL as a String. Only valid if type=='KnownLayerWFS'
 */
KnownLayerRecord.prototype.getIconUrl = function() {
	return this.internalGetStringField('iconUrl');
};

/**
 * Gets the descriptive keyword as a String. Only valid if type=='KnownLayerKeywords'
 */
KnownLayerRecord.prototype.getDescriptiveKeyword = function() {
	return this.internalGetStringField('descriptiveKeyword');
};

/**
 * Gets the layer name as a String. Only valid if type=='KnownLayerWMS'
 */
KnownLayerRecord.prototype.getLayerName = function() {
	return this.internalGetStringField('layerName');
};

/**
 * Gets the style name as a String. Only valid if type=='KnownLayerWMS'
 */
KnownLayerRecord.prototype.getStyleName = function() {
	return this.internalGetStringField('styleName');
};

/**
 * Gets the icon anchor location as an Object. Only valid if type=='KnownLayerWFS'
 * {
 * 	x : Number
 * 	y : Number
 * }
 */
KnownLayerRecord.prototype.getIconAnchor = function() {
	return this.internalGetStringField('iconAnchor');
};

/**
 * Gets the info window anchor location as an Object. Only valid if type=='KnownLayerWFS'
 * {
 * 	x : Number
 * 	y : Number
 * }
 */
KnownLayerRecord.prototype.getInfoWindowAnchor = function() {
	return this.internalGetStringField('infoWindowAnchor');
};

/**
 * Gets the icon size as an Object. Only valid if type=='KnownLayerWFS'
 * {
 * 	width  : Number
 * 	height : Number
 * }
 */
KnownLayerRecord.prototype.getIconSize = function() {
	return this.internalGetStringField('iconSize');
};

/**
* Gets an OverlayManager that holds the list of bounding boxes for this layer (or null/undefined)
*/
KnownLayerRecord.prototype.getBboxOverlayManager = function() {
	return this.internalRecord.bboxOverlayManager;
};

/**
* Sets an OverlayManager that holds the list of bounding boxes for this layer (or null/undefined)
*/
KnownLayerRecord.prototype.setBboxOverlayManager = function(bboxOverlayManager) {
	this.internalRecord.bboxOverlayManager = bboxOverlayManager;
};

/**
 * Given a CSWRecordStore this function will return an array of CSWRecords that 
 * this KnownLayerRecord is representing
 */
KnownLayerRecord.prototype.getLinkedCSWRecords = function(cswRecordStore) {
	var type = this.getType();
	
	switch (this.getType()) {
	case 'KnownLayerWFS':
		var featureTypeName = this.getFeatureTypeName();
		return cswRecordStore.getCSWRecordsByOnlineResource(featureTypeName, null);
	case 'KnownLayerWMS':
		var layerName = this.getLayerName();
		return cswRecordStore.getCSWRecordsByOnlineResource(layerName, null);
	case 'KnownLayerKeywords':
		var keyword = this.getDescriptiveKeyword();
		return cswRecordStore.getCSWRecordsByKeywords([keyword]);
	}
	
	return [];
};

/**
 * Given a CSWRecordStore this function will return an array of CSWRecords that 
 * this KnownLayerRecord is representing, containing the specified keyword.
 * 
 * TODO: This is currently only implemented for KnownLayerKeywords
 */
KnownLayerRecord.prototype.getLinkedCSWRecordsByKeywords = function(cswRecordStore, keywords) {
	var type = this.getType();
	
	switch (this.getType()) {
	case 'KnownLayerKeywords':
		var keyword = this.getDescriptiveKeyword();
		keywords.add(keyword);
		return cswRecordStore.getCSWRecordsByKeywords(keywords);
	}
	
	return [];
};

/**
 * Given a CSWRecordStore this function will return an array of arrays
 * containing unique keywords and the number of records containing each keyword.
 * 
 * If includeOwnKeyword is set to true the known layer's own keyword will be 
 * included in the list returned, otherwise it will not be included in the list.
 * 
 * @returns {Array}
 */
KnownLayerRecord.prototype.getLinkedCSWRecordsKeywordCount = function(cswRecordStore, includeOwnKeyword) {
	var keywords = new Array();
	var temp = new Array();
	var recs = cswRecordStore.getCSWRecordsByKeywords([this.getDescriptiveKeyword()]);
	var ownKeyword = this.getDescriptiveKeyword();;
	var k = 0;

	for(var i=0; i< recs.length; i++) {
		var descriptiveKeywords = recs[i].getDescriptiveKeywords();
		
		for(var j=0; j<descriptiveKeywords.length; j++) {
			var matchIndex = temp.indexOf(descriptiveKeywords[j]);
			
			if(descriptiveKeywords[j] != ownKeyword || includeOwnKeyword) {
				if(matchIndex < 0) {
					keywords[k] = [descriptiveKeywords[j], 1];
					temp[k] = descriptiveKeywords[j];
					k++;
				} else {
					keywords[matchIndex][1]++;
				}
			}
		}
	}
	
	return keywords;
};