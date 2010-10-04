/**
 * An extension of a normal JSON store that makes it specialize into storing and retrieving CSWRecord's
 */
KnownLayerStore = function(url) {
	var conn = new Ext.data.Connection({
		url: url, 
		timeout:180000
	});
	
	KnownLayerStore.superclass.constructor.call(this, {
		proxy			: new Ext.data.HttpProxy(conn),
		storeId			: 'knownLayerRecordStore',
		root			: 'records',
		id				: 'featureTypeName',
		successProperty	: 'success',
		messageProperty : 'msg',
		fields			: [
		    'featureTypeName',
		    'type',
		    'layerName',
		    'title',
		    'descriptiveKeyword',
		    'styleName',
		    'description',
		    'id',
		    'proxyUrl',
		    'iconUrl',
		    'iconAnchor',
		    'infoWindowAnchor',
		    'iconSize',
		    'disableBboxFiltering'
		]
	});
};


Ext.extend(KnownLayerStore, Ext.data.JsonStore, {
	/**
	 * Gets a KnownLayerRecord object representation of the record at the specified location
	 */
    getKnownLayerAt		: function(index) {
		var dataRecord = KnownLayerStore.superclass.getAt.call(this,index);
		if (!dataRecord) {
			return null;
		}
		
		return new KnownLayerRecord(dataRecord);
	},

	/**
	 * Gets a KnownLayerRecord object representation of the record with the specified id
	 */
	getKnownLayerById	: function(id) {
		var dataRecord = KnownLayerStore.superclass.getById.call(this, id);
		if (!dataRecord) {
			return null;
		}
		
		return new KnownLayerRecord(dataRecord);
	}
});