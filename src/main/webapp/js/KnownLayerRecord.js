/**
 * A representation of a KnownLayerRecord in the user interface (as returned by the getKnownFeatures.do handler).
 */
KnownLayerRecord = Ext.extend(AbstractRecordWrapper, {

    constructor : function(internalRecord) {
        KnownLayerRecord.superclass.constructor.call(this, {
            internalRecord : internalRecord
        });
    },

    /**
     * Gets the type of this known layer.
     *
     * Can be ['KnownLayerWFS', 'KnownLayerWMS', 'KnownLayerKeywords']
     */
    getType : function() {
        return this.getStringField('type');
    },

    /**
     * Gets an array of String feature type names or wms layer names
     * Gets every feature type/layer name that is related to this feature type/layer. Only valid if type=='KnownLayerWFS' or type=='KnownLayerWMS'
     */
    getRelatedNames : function() {
        var recs = this.internalRecord.get('relatedNames');
        if (!recs) {
            return [];
        }

        return recs;
    },

    /**
     * Gets whether this known layer should be hidden from the user as a boolean
     */
    getHidden : function() {
        return this.getStringField('hidden');
    },

    /**
     * Gets the id of this known layer as a String.
     */
    getId : function() {
        return this.getStringField('id');
    },

    /**
     * Gets the feature type as a String. Only valid if type=='KnownLayerWFS'
     */
    getFeatureTypeName : function() {
        return this.getStringField('featureTypeName');
    },

    /**
     * Gets whether bounding box filtering should be disabled for this known layer.
     * Only valid if type=='KnownLayerWFS'
     */
    getDisableBboxFiltering : function() {
        return this.getStringField('disableBboxFiltering');
    },

    /**
     * Gets the display name as a String
     */
    getTitle : function() {
        return this.getStringField('title');
    },

    /**
     * Gets the description as a String
     */
    getDescription : function() {
        return this.getStringField('description');
    },

    /**
     * Gets the proxy URL as a String. Only valid if type=='KnownLayerWFS'
     */
    getProxyUrl : function() {
        return this.getStringField('proxyUrl');
    },

    /**
     * Gets the icon URL as a String. Only valid if type=='KnownLayerWFS'
     */
    getIconUrl : function() {
        return this.getStringField('iconUrl');
    },

    /**
     * Gets the list of service endpoints to be included/excluded from the layer
     */
    getServiceEndpoints : function() {
        return this.internalRecord.get('serviceEndpoints');
    },

    /**
     * Gets the flag indicating whether the listed endpoints should be included or excluded
     */
    includeEndpoints : function() {
        return this.internalRecord.get('includeEndpoints');
    },

    /**
     * Gets the descriptive keyword as a String. Only valid if type=='KnownLayerKeywords'
     */
    getDescriptiveKeyword : function() {
        return this.getStringField('descriptiveKeyword');
    },

    /**
     * Gets the layer name as a String. Only valid if type=='KnownLayerWMS'
     */
    getLayerName : function() {
        return this.getStringField('layerName');
    },

    /**
     * Gets the style name as a String. Only valid if type=='KnownLayerWMS'
     */
    getStyleName : function() {
        return this.getStringField('styleName');
    },

    /**
     * Gets the icon anchor location as an Object. Only valid if type=='KnownLayerWFS'
     * {
     *  x : Number
     *  y : Number
     * }
     */
    getIconAnchor : function() {
        return this.getStringField('iconAnchor');
    },

    /**
     * Gets the info window anchor location as an Object. Only valid if type=='KnownLayerWFS'
     * {
     *  x : Number
     *  y : Number
     * }
     */
    getInfoWindowAnchor : function() {
        return this.getStringField('infoWindowAnchor');
    },

    /**
     * Gets the icon size as an Object. Only valid if type=='KnownLayerWFS'
     * {
     *  width  : Number
     *  height : Number
     * }
     */
    getIconSize : function() {
        return this.getStringField('iconSize');
    },

    /**
    * Gets an OverlayManager that holds the list of bounding boxes for this layer (or null/undefined)
    */
    getBboxOverlayManager : function() {
        return this.internalRecord.bboxOverlayManager;
    },

    /**
    * Sets an OverlayManager that holds the list of bounding boxes for this layer (or null/undefined)
    */
    setBboxOverlayManager : function(bboxOverlayManager) {
        this.internalRecord.bboxOverlayManager = bboxOverlayManager;
    },

    /**
     * Given a CSWRecordStore this function will return an array of CSWRecords that
     * this KnownLayerRecord is representing
     */
    getLinkedCSWRecords : function(cswRecordStore) {
        var type = this.getType();

        //Internal cache added for AUS-1968
        if (this.internalRecord.cachedLinkedRecords) {
            return this.internalRecord.cachedLinkedRecords;
        }

        switch (this.getType()) {
        case 'KnownLayerWFS':
            var featureTypeName = this.getFeatureTypeName();
            this.internalRecord.cachedLinkedRecords = cswRecordStore.getCSWRecordsByOnlineResource(featureTypeName, null);
            return this.internalRecord.cachedLinkedRecords;
        case 'KnownLayerWMS':
            var layerName = this.getLayerName();
            this.internalRecord.cachedLinkedRecords = cswRecordStore.getCSWRecordsByOnlineResource(layerName, null);
            return this.internalRecord.cachedLinkedRecords;
        case 'KnownLayerKeywords':
            var keyword = this.getDescriptiveKeyword();
            this.internalRecord.cachedLinkedRecords = cswRecordStore.getCSWRecordsByKeywords(keyword);
            return this.internalRecord.cachedLinkedRecords;
        }

        return [];
    },

    /**
     * Given a CSWRecordStore this function will return an array of CSWRecords that
     * this KnownLayerRecord is 'related' to. How that relation is defined depends on the
     * KnownLayer type.
     */
    getRelatedCSWRecords : function(cswRecordStore) {
        var type = this.getType();

        //Internal cache added for AUS-1968
        if (this.internalRecord.cachedRelatedRecords) {
            return this.internalRecord.cachedRelatedRecords;
        }

        switch (type) {
        case 'KnownLayerWMS':
        case 'KnownLayerWFS':
            var names = this.getRelatedNames();
            var relatedRecs = [];

            for (var i = 0; i < names.length; i++) {
                relatedRecs = relatedRecs.concat(cswRecordStore.getCSWRecordsByOnlineResource(names[i], null));
            }

            this.internalRecord.cachedRelatedRecords = relatedRecs;
            return this.internalRecord.cachedRelatedRecords;
        }

        return [];
    },

    /**
     * Given a CSWRecordStore this function will return an array of CSWRecords that
     * this KnownLayerRecord is representing, containing the specified keyword.
     *
     * Only valid if type=='KnownLayerKeywords'
     */
    getLinkedCSWRecordsByKeywords : function(cswRecordStore, keywords) {
        var type = this.getType();

        switch (this.getType()) {
        case 'KnownLayerKeywords':
            var keyword = this.getDescriptiveKeyword();
            keywords.add(keyword);
            return cswRecordStore.getCSWRecordsByKeywords(keywords);
        }

        return [];
    },

    /**
     * Given a CSWRecordStore this function will return an array of arrays
     * containing unique keywords and the number of records containing each keyword.
     *
     * If includeOwnKeyword is set to true the known layer's own keyword will be
     * included in the list returned, otherwise it will not be included in the list.
     *
     * Only valid if type=='KnownLayerKeywords'
     *
     * @returns {Array}
     */
    getLinkedCSWRecordsKeywordCount : function(cswRecordStore, includeOwnKeyword) {
        var keywords = [];
        var temp = [];
        var recs = cswRecordStore.getCSWRecordsByKeywords([this.getDescriptiveKeyword()]);
        var ownKeyword = this.getDescriptiveKeyword();
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
    },

    /**
     * Given a CSWRecordStore this function will return an array of arrays
     * containing unique resource providers and the number of records belong to
     * each resource provider.
     *
     * Only valid if type=='KnownLayerKeywords'
     *
     * @returns {Array}
     */
    getLinkedCSWRecordResourceProvidersCount : function(cswRecordStore) {
        var resourceProviders = [];
        var temp = [];
        var recs = cswRecordStore.getCSWRecordsByKeywords([this.getDescriptiveKeyword()]);
        var k = 0;

        for(var i=0; i<recs.length; i++) {
            var resourceProvider = recs[i].getResourceProvider();
            var matchIndex = temp.indexOf(resourceProvider);

            if(matchIndex < 0) {
                resourceProviders[k] = [resourceProvider, 1];
                temp[k] = resourceProvider;
                k++;
            } else {
                resourceProviders[matchIndex][1]++;
            }
        }

        return resourceProviders;
    },

    /**
     * Gets the organisational group of this KnownLayer as a string (can be null/emopty)
     */
    getGroup : function() {
        return this.getStringField('group');
    }
});

