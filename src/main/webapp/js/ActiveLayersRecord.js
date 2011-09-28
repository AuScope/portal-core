/**
 * A simple wrapper for records in ActiveLayersStore that provides a number of useful helper methods
 */
ActiveLayersRecord = Ext.extend(AbstractRecordWrapper, {

    constructor : function(internalRecord) {
        ActiveLayersRecord.superclass.constructor.call(this, {
            internalRecord : internalRecord
        });
    },

    /**
     * Gets the id of this active layer as a String
     */
    getId : function() {
        return this.getStringField('id');
    },

    /**
     * Gets the title of this active layer as a String
     */
    getTitle : function() {
        return this.getStringField('title');
    },

    /**
     * Gets the description of this active layer as a String
     */
    getDescription : function() {
        return this.getStringField('description');
    },

    /**
     * Gets the Proxy URL of this active layer as a String (or null)
     *
     * The Proxy URL is the url that should be queried instead of the onlineResource URL.
     */
    getProxyUrl : function() {
        return this.internalRecord.get('proxyUrl');
    },

    /**
     * Gets an array of CSWRecord objects that represent this layer
     */
    getCSWRecords : function() {
        return this.getArrayField('cswRecords');
    },

    /**
     * Gets an array of CSWRecord objects which have at least one online resource with the specified type
     *
     * type: One of ['WFS', 'WMS', 'WCS', 'OPeNDAP']
     */
    getCSWRecordsWithType : function(type) {
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
    },

    /**
     * Gets the URL of this active layer's icon as a String (Can be null/empty)
     */
    getIconUrl : function() {
        return this.internalRecord.get('iconUrl');
    },

    /**
     * Gets the list of service endpoints applicable to the layer
     */
    getServiceEndpoints : function() {
        return this.internalRecord.get('serviceEndpoints');
    },

    /**
     * Gets the flag indicating whether the listed service endpoints should be
     * included or excluded from the layer.
     */
    includeEndpoints : function() {
        return this.internalRecord.get('includeEndpoints');
    },

    /**
     * Gets the html representation of the key column as a String
     */
    getKeyIconHtml : function() {
        return this.getStringField('keyIconHtml');
    },

    /**
     * Gets whether this record is still loading or not as a boolean
     */
    getIsLoading : function() {
        return this.getBooleanField('isLoading');
    },

    /**
     * Sets whether this record is still loading or not as a boolean
     */
    setIsLoading : function(isLoading) {
        this.internalRecord.set('isLoading', isLoading);
    },

    /**
     * Gets whether this record currently has data available for download or not.
     */
    hasData : function() {
        return this.getBooleanField('hasData');
    },

    /**
     * Sets whether this record currently has data available for download or not.
     */
    setHasData : function(hasData) {
        return this.internalRecord.set('hasData', hasData);
    },

    /**
     * Gets whether this record is visible or not as a boolean
     */
    getLayerVisible : function() {
        return this.getBooleanField('layerVisible');
    },

    /**
     * Sets whether this record is visible or not as a boolean
     */
    setLayerVisible : function(layerVisible) {
        this.internalRecord.set('layerVisible', layerVisible);
    },

    /**
     * Gets the html representation of the download column as a String
     */
    getDownloadIconHtml : function() {
        return this.getStringField('downloadIconHtml');
    },

    /**
     * Gets the numerical representation of the Opacity as a Number in the range [0,1]
     */
    getOpacity : function() {
        return this.getNumberField('opacity', 1);
    },

    /**
     * Gets the numerical representation of the Opacity as a Number in the range [0,1]
     */
    setOpacity : function(opacity) {
        if (opacity < 0) {
            opacity = 0;
        } else if (opacity > 1) {
            opacity = 1;
        }

        this.internalRecord.set('opacity', opacity);
    },

    /**
     * Gets the source record type that was used to make this ActiveLayer
     *
     * Returns one of the following ['KnownLayer', 'CSWRecord']
     */
    getSource : function() {
        return this.getStringField('source');
    },

    /**
     * Gets an instance of OverlayManager or null
     */
    getOverlayManager : function() {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        return this.internalRecord.overlayManager;
    },

    /**
     * Sets an instance of OverlayManager or null
     */
    setOverlayManager : function(overlayManager) {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        this.internalRecord.overlayManager = overlayManager;
    },

    /**
     * Gets an instance of a FormFactory.getFilterForm response or null
     */
    getFilterPanel : function() {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        return this.internalRecord.filterPanel;
    },

    /**
     * Sets an instance of FormFactory.getFilterForm response or null
     */
    setFilterPanel : function(filterPanel) {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        this.internalRecord.filterPanel = filterPanel;
    },

    /**
     * Gets an instance of ResponseToolTip or null
     */
    getResponseToolTip : function() {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        return this.internalRecord.responseToolTip;
    },

    /**
     * Sets an instance of ResponseToolTip or null
     */
    setResponseToolTip : function(responseToolTip) {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        this.internalRecord.responseToolTip = responseToolTip;
    },

    /**
     * Gets an instance of Ext.Window or null
     *
     * It represents the popup window with legend information for this active layer
     */
    getLegendWindow : function() {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        return this.internalRecord.legendWindow;
    },

    /**
     * Sets an instance of Ext.Window or null
     *
     * It represents the popup window with legend information for this active layer
     */
    setLegendWindow : function(legendWindow) {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        this.internalRecord.legendWindow = legendWindow;
    },

    /**
     * Gets an instance of DebuggerData or null
     *
     * It represents some debug information associated with this layer
     */
    getDebuggerData : function() {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        return this.internalRecord.debuggerData;
    },

    /**
     * Sets an instance of DebuggerData or null
     *
     * It represents some debug information associated with this layer
     */
    setDebuggerData : function(debuggerData) {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        this.internalRecord.debuggerData = debuggerData;
    },

    /**
     * Gets an instance of KnownLayerRecord or null
     *
     * It represents the KnownLayer 'owns' the CSWRecords of this active layer
     * (not all layers will be created from an active layer)
     */
    getParentKnownLayer : function() {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        var rec = this.internalRecord.parentKnownLayer;
        if (rec) {
            return new KnownLayerRecord(rec);
        }

        return null;
    },

    /**
     * Sets an instance of KnownLayerRecord or null
     *
     * It represents the KnownLayer that 'owns' the CSWRecords of this active layer
     * (not all layers will be created from an active layer)
     */
    setParentKnownLayer : function(knownLayerRecord) {
        //We are forced to read/write directly to the record because this
        //field is too complex to be serialized using JSON
        if (knownLayerRecord) {
            this.internalRecord.parentKnownLayer = knownLayerRecord.internalRecord;
        } else {
            this.internalRecord.parentKnownLayer = null;
        }
    },

    /**
     * Gets the last set of filter params (as a basic object) that were used to query
     * this layer. Can be null/undefined
     */
    getLastFilterParameters : function() {
        return this.internalRecord.lastFilterParams;
    },

    /**
     * Sets the last set of filter params (as a basic object) that were used to query
     * this layer. Can be null/undefined
     */
    setLastFilterParameters : function(filterParams) {
        this.internalRecord.lastFilterParams = filterParams;
    },

    /**
     * Gets the array of unique ids of the services request of a layer.
     * Can be null/undefined
     */
    getWFSRequestTransId : function() {
        return this.internalRecord.transId;
    },

    /**
     * Sets the array of unique ids of the services request of a layer.
     * Can be null/undefined
     */
    setWFSRequestTransId : function(transId) {
        this.internalRecord.transId = transId;
    },

    /**
     * Gets the service Url for which the unique ids are set.
     * Can be null/undefined
     */
    getWFSRequestTransIdUrl : function() {
        return this.internalRecord.transIdUrl;
    },

    /**
     * Sets the service Url for which the unique ids are set.
     * Can be null/undefined
     */
    setWFSRequestTransIdUrl : function(transIdUrl) {
        this.internalRecord.transIdUrl = transIdUrl;
    }
});



