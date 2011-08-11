/**
 * A specialized store for holding "Active" layers that will come from individual CSWRecords or KnownFeatureTypes
 */
ActiveLayersStore = function() {
    ActiveLayersStore.superclass.constructor.call(this, {
        storeId     : 'active-layers-store',
        reader		: new Ext.data.JsonReader({
            idProperty		: 'id',
            root			: 'data',
            fields 			: [
                {   name	: 'id'              },	//String: The unique ID of this active layer.
                {   name	: 'title'           },	//String: Text appears under the title column
                {   name	: 'description'     },	//String: Text that appears when the row is 'expanded'
                {   name	: 'cswRecords'      },	//[CSWRecord]: Objects that represent the content of this layer
                {	name	: 'proxyUrl'		},	//String: The raw URL that references the location the active layer should query
                {   name	: 'iconUrl'         },	//String: The raw URL pointing to an appropriate image icon (used for WFS)
                {   name    : 'serviceEndpoints'},  //String[]: The list of service endpoints that will be included/excluded from the layer
                {   name    : 'includeEndpoints'},  //boolean: The flag indicating whether the listed endpoints will be included or excluded
                {   name	: 'keyIconHtml'     },	//String: HTML that will appear under the 'key' column
                {   name	: 'isLoading' 		},	//boolean: Whether this layer is currently 'loading'
                {   name    : 'hasData'         },  //boolean: Whether this layer currently has data avaiable for download
                {   name	: 'layerVisible'    },	//boolean: Whether the layer is currently visible
                {   name	: 'opacity' 		},	//number: The layers opacity (if applicable) from [0, 1],
                {   name    : 'source'          }   //String: Will be either ['KnownLayer','CSWRecord']
            ]
        })
    });
};


Ext.extend(ActiveLayersStore, Ext.data.Store, {
    /**
     * Not for usage outside this class
     */
    internalAddSingleRecord : function(recObj) {
        var data  = {
            success : true,
            data : [recObj]
        };

        ActiveLayersStore.superclass.loadData.call(this, data, true);
    },

    /**
     * Adds the specified CSWRecord object to this datastore (Duplicate ID's will override existing records)
     *
     * Returns the ActiveLayersRecord that was added
     */
    addCSWRecord		: function (cswRecord) {
        var keyIconHtml = null;

        //If we have WMS component(s) we include the 'key' icon
        if (cswRecord.getFilteredOnlineResources('WMS').length > 0) {
            keyIconHtml = '<img width="16" height="16" src="img/key.png">';
        }

        this.internalAddSingleRecord({
            id			: cswRecord.getFileIdentifier(),
            title		: cswRecord.getServiceName(),
            description	: cswRecord.getDataIdentificationAbstract(),
            proxyUrl	: null,
            cswRecords	: [cswRecord],
            iconUrl		: null,
            serviceEndpoints : null,
            includeEndpoints : false,
            keyIconHtml	: keyIconHtml,
            isLoading	: false,
            hasData     : false,
            layerVisible: true,
            opacity		: 1,
            source      : 'CSWRecord'
        });

        return this.getByCSWRecord(cswRecord);
    },

    /**
     * Adds the specified KnownLayerRecord object to this datastore (Duplicate ID's will override existing records)
     *
     * Returns the ActiveLayersRecord that was added
     */
    addKnownLayer	: function (knownLayerRecord, cswRecordStore) {
        var linkedCSWRecords = knownLayerRecord.getLinkedCSWRecords(cswRecordStore);

        var keyIcon = '<img width="16" height="16" src="' + knownLayerRecord.getIconUrl() + '">';
        if(knownLayerRecord.getType() === "KnownLayerWMS"){
            keyIcon = '<img width="16" height="16" src="img/key.png">';
        }

        this.internalAddSingleRecord({
            id			: knownLayerRecord.getId(),
            title		: knownLayerRecord.getTitle(),
            description	: knownLayerRecord.getDescription(),
            proxyUrl	: knownLayerRecord.getProxyUrl(),
            cswRecords	: linkedCSWRecords,
            iconUrl		: knownLayerRecord.getIconUrl(),
            serviceEndpoints: knownLayerRecord.getServiceEndpoints(),
            includeEndpoints: knownLayerRecord.includeEndpoints(),
            keyIconHtml	: keyIcon,
            isLoading	: false,
            hasData     : false,
            layerVisible: true,
            opacity		: 1,
            source      : 'KnownLayer'
        });


        var activeLayerRecord =  this.getByKnownLayerRecord(knownLayerRecord);
        activeLayerRecord.setParentKnownLayer(knownLayerRecord);

        return activeLayerRecord;
    },

    /**
     * Gets the record from this store that was generated from the specified KnownLayer with addKnownLayer
     *
     * Returns null if the record DNE otherwise the record will be returned wrapped in a ActiveLayersRecord object
     */
    getByKnownLayerRecord : function (knownLayer) {
        var rec = ActiveLayersStore.superclass.getById.call(this, knownLayer.getId());
        if (rec) {
            return new ActiveLayersRecord(rec);
        }
        return null;
    },

    /**
     * Gets the record from this store that was generated from the specified CSWRecord with addCSWRecord
     *
     * Returns null if the record DNE otherwise the record will be returned wrapped in a ActiveLayersRecord object
     */
    getByCSWRecord : function (cswRecord) {
        var rec = ActiveLayersStore.superclass.getById.call(this, cswRecord.getFileIdentifier());
        if (rec) {
            return new ActiveLayersRecord(rec);
        }
        return null;
    },

    /**
     * Removes the specified activeLayersRecord from this store
     */
    removeActiveLayersRecord : function(activeLayersRecord) {
        if (activeLayersRecord) {
            ActiveLayersStore.superclass.remove.call(this, activeLayersRecord.internalRecord);
        }
    },

    /**
     * Get the ActiveLayersRecord at the specified index (or null)
     */
    getActiveLayerAt : function(index) {
        var rec = ActiveLayersStore.superclass.getAt.call(this, index);
        if (rec) {
            return new ActiveLayersRecord(rec);
        }
        return null;
    }
});