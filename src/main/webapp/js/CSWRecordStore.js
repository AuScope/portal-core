/**
 * An extension of a normal JSON store that makes it specialize into storing and retrieving CSWRecord's
 */
CSWRecordStore = function(url) {
	var conn = new Ext.data.Connection({
		url: url, 
		timeout:180000
	});
	
	//Converts an array of BBox records into an actual BBox object array
    var convertGeographicEls = function(v, record) {
        for (var i = 0; i < v.length; i++) {
        	if (v[i].type === 'bbox') {
	            v[i] = new BBox(v[i].northBoundLatitude,
	            		v[i].southBoundLatitude,
	            		v[i].eastBoundLongitude,
	            		v[i].westBoundLongitude);
        	}
        }
            
        return v;
    };
	
	CSWRecordStore.superclass.constructor.call(this, {
		proxy			: new Ext.data.HttpProxy(conn),
		groupField		: 'contactOrganisation',
		sortInfo		: {
			field			: 'serviceName',
			direction		: 'ASC'
		},
		reader			: new Ext.data.JsonReader({
			root			: 'records',
			id				: 'fileIdentifier',
			successProperty	: 'success',
			messageProperty : 'msg',
			fields			: [
			    'serviceName',
			    'contactOrganisation',
			    'fileIdentifier',
			    'recordInfoUrl',
			    'dataIdentificationAbstract',
			    'onlineResources',
			    'descriptiveKeywords',
			    {name : 'geographicElements', convert : convertGeographicEls}
			]
		})
	});
};


Ext.extend(CSWRecordStore, Ext.data.GroupingStore, {
	
	/**
	 * Clears this store and then copies all records from sourceCSWRecordStore into this store.
	 * 
	 * sourceCSWRecordStore : The source CSWRecordStore
	 * filterFunc : [Optional] A function(CSWRecord) that will be called on each record copied, if the 
	 *              the function returns true, the record will be copied
	 */
	copyFrom	: function(sourceCSWRecordStore, filterFunc) {
		this.removeAll();
		
		var recordsToCopy = sourceCSWRecordStore.getRange();		
		
		if (filterFunc) {
			var recordsToAdd = [];
			
			for (var i = 0; i < recordsToCopy.length; i++) {
				var cswRecord = new CSWRecord(recordsToCopy[i]);
					
				if (!filterFunc(cswRecord)) {
					continue;
				}
					
				recordsToAdd.push(recordsToCopy[i])
			}
			
			this.add(recordsToAdd);
		} else {
			this.add(recordsToCopy);
		}
	},
	
	/**
	 * Gets a CSWRecord object representation of the record at the specified location
	 */
    getCSWRecordAt		: function(index) {
		var dataRecord = CSWRecordStore.superclass.getAt.call(this,index);
		if (!dataRecord) {
			return null;
		}
		
		return new CSWRecord(dataRecord);
	},

	/**
	 * Gets a CSWRecord object representation of the record with the specified id
	 */
	getCSWRecordById	: function(id) {
		var dataRecord = CSWRecordStore.superclass.getById.call(this, id);
		if (!dataRecord) {
			return null;
		}
		
		return new CSWRecord(dataRecord);
	},
	
	/**
	 * Gets all records that have a descriptive keyword that matches the specified
	 * keyword(s).
	 * 
	 * Returns an array of CSWRecord objects
	 */
	getCSWRecordsByKeywords : function(keywords) {
		
		//Filter our results
		var results = [];
		CSWRecordStore.superclass.each.call(this, function(rec) {
			var descriptiveKeywords = rec.get('descriptiveKeywords');
			
			var containsKeywords = true;
			for(var i=0; i<keywords.length; i++) {				 
				if (descriptiveKeywords.indexOf(keywords[i]) < 0) {
					containsKeywords = false;
					break;
				}
			}
			
			if (containsKeywords) {
				results.push(new CSWRecord(rec));
			}
			
			return true;
		});	
		
		return results;
	},
	
	/**
	 * Gets all records that have a specified online resource(s) that match
	 * one of the specified filter parameters
	 * type : [Optional] one of ['WCS', 'WFS', 'WMS', 'OPeNDAP']
	 * name : [Optional] the online resource name to match
	 * 
	 * Returns an array of CSWRecord objects
	 */
	getCSWRecordsByOnlineResource : function(name, type) {
		
		//Filter our results
		var results = [];
		CSWRecordStore.superclass.each.call(this, function(rec) {
			var onlineResources = rec.get('onlineResources');
			if (!onlineResources) {
				return true;
			}
			
			//search for any instances of the specified name/type
			//and then turn them into a CSWRecord
			for (var i = 0; i < onlineResources.length; i++) {
				if (type && (onlineResources[i].onlineResourceType === type)) {
					results.push(new CSWRecord(rec));
					return true;
				}
				
				if (name && (onlineResources[i].name === name)) {
					results.push(new CSWRecord(rec));
					return true;
				}
			}
			
			return true;
		});	
		
		return results;
	}	
	
});