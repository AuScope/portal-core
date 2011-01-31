
/**
 * The CSWRecordDescriptionWindow is a class that specialises Ext.Window into displaying
 * detailed information about a list of CSWRecords
 *
 *  cswRecords - a CSWRecord or Array of CSWRecords
 *  knownLayerRecord
 */
CSWRecordDescriptionWindow = function(cswRecords, knownLayerRecord) {
	if (cswRecords) {
		if (cswRecords instanceof CSWRecord) {
			this.cswRecords = [cswRecords];
		} else {
			this.cswRecords = cswRecords;
		}
	}

	//Generate our flattened 'data items' list for rendering to the grid
	var dataItems = [];
	for (var i = 0; i < this.cswRecords.length; i++) {
		var onlineResources = this.cswRecords[i].getOnlineResources();
		for (var j = 0; j < onlineResources.length; j++) {

			//ensure we have a type we want to describe
			switch (onlineResources[j].onlineResourceType) {
			case 'WWW':
				break;
			case 'WFS':
				break;
			case 'WMS':
				break;
			case 'WCS':
				break;
			default:
				continue;//don't include anything else
			}

			if(knownLayerRecord == null || knownLayerRecord.getServiceEndpoints() == null || 
					includeEndpoint(knownLayerRecord.getServiceEndpoints(), onlineResources[j].url, 
							knownLayerRecord.includeEndpoints())) {
				dataItems.push([
				    onlineResources[j].name,
				    onlineResources[j].description,
				    onlineResources[j].url,
				    onlineResources[j],
				    onlineResources[j].onlineResourceType,
				    i
				]);
			}
		}
	}

	//Create the internal store that this window will use for its grids
	this.store = new Ext.data.GroupingStore({
		autoDestroy		: true,
		groupField		: 'type',
		sortInfo		: {
			field			: 'name',
			direction		: 'ASC'
		},
		reader : new Ext.data.ArrayReader({
			fields : [
			    'name',
			    'description',
			    'url',
			    'preview',
			    'type',
			    'cswRecordIndex'
			]
		}),
		data : dataItems
	});

	CSWRecordDescriptionWindow.superclass.constructor.call(this, {
        title: 'Service Information',
        autoDestroy : true,
        width : 800,
        items : [{
        	xtype : 'grid',
        	store : this.store,
        	autoHeight: true,
        	view : new Ext.grid.GroupingView({
                groupTextTpl: '{text}',
                showGroupName: false,
                templates: {
        			cell: new Ext.Template(
        				'<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>',
        				'<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>',
        				'</td>')
        		}
            }),
            autoExpandColumn: 'description',
            columns: [{
            	id : 'name',
            	header : 'Name',
            	dataIndex: 'name',
            	menuDisabled: true,
            	sortable: true,
            	renderer: function(value, metadata, record) {
            		return '<b>' + value + '</b>';
            	}
            },{
            	id : 'description',
            	header : 'Description',
            	dataIndex: 'description',
            	sortable: false,
            	menuDisabled: true,
            	width: 290,
            	renderer: function(value, metadata, record) {
            	return '<p><i>' + value + '</i></p>';
            	}
            }, {
            	id : 'url',
            	header : 'URL',
            	dataIndex: 'url',
            	sortable: false,
            	menuDisabled: true,
            	width: 256,
            	renderer: function(value, metadata, record) {
            		switch(record.get('type')) {
            		case 'WWW':
            			return '<a target="_blank" href="' + value + '"><p>' + value + '</p></a>';
            		default:
            			return value;
            		}
            	}
            },{
            	id : 'preview',
            	header : 'Preview',
            	dataIndex: 'preview',
            	scope: this,
            	width: 140,
            	sortable: false,
            	menuDisabled: true,
            	renderer: function(value, metadata, record) {
            		var onlineRes = value;
            		var cswRecord = this.cswRecords[record.get('cswRecordIndex')];

            		//We preview types differently
            		switch(record.get('type')) {
            		case 'WFS':
            			var getFeatureUrl = onlineRes.url + this.internalURLSeperator(onlineRes.url) + 'SERVICE=WFS&REQUEST=GetFeature&VERSION=1.1.0&maxFeatures=5&typeName=' + onlineRes.name;
            			return '<a target="_blank" href="' + getFeatureUrl + '"><p>First 5 features</p></a>';
            		case 'WCS':
            			var describeCoverageUrl = onlineRes.url + this.internalURLSeperator(onlineRes.url) + 'SERVICE=WCS&REQUEST=DescribeCoverage&VERSION=1.0.0&coverage=' + onlineRes.name;
            			return '<a target="_blank" href="' + describeCoverageUrl + '"><p>DescribeCoverage response</p></a>';
            		case 'WMS':
            			//Form the WMS url
            			var getMapUrl = onlineRes.url + this.internalURLSeperator(onlineRes.url) + 'SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.1&LAYERS=' + onlineRes.name;
            			getMapUrl += '&SRS=EPSG:4326&FORMAT=image/png&STYLES=';

            			//To generate the url we will need to use the bounding box to make the request
            			//To avoid distortion, we also scale the width height independently
            			var geoEls = cswRecord.getGeographicElements();
            			if (geoEls.length > 0) {
            				var superBbox = geoEls[0];
            				for (var i = 1; i < geoEls.length; i++) {
            					superBbox = superBbox.combine(geoEls[i]);
            				}

            				var superBboxStr = superBbox.westBoundLongitude + "," +
            									superBbox.southBoundLatitude + "," +
            									superBbox.eastBoundLongitude + "," +
            									superBbox.northBoundLatitude;

            				//Set our width to a constant and scale the height appropriately
            				var heightRatio = (superBbox.northBoundLatitude - superBbox.southBoundLatitude) /
            							 	  (superBbox.eastBoundLongitude - superBbox.westBoundLongitude);
            				var width = 512;
            				var height = Math.floor(width * heightRatio);

            				getMapUrl += '&WIDTH=' + width;
            				getMapUrl += '&HEIGHT=' + height;
            				getMapUrl += '&BBOX=' + superBboxStr;

            				var thumbWidth = width;
            				var thumbHeight = height;

            				//Scale our thumbnail appropriately
            				if (thumbWidth > 128) {
            					thumbWidth = 128;
            					thumbHeight = thumbWidth * heightRatio;
            				}

            				return '<a target="_blank" href="' + getMapUrl + '"><img width="' + thumbWidth + '" height="' + thumbHeight + '" alt="Loading preview..." src="' + getMapUrl + '"/></a>';
            			}
            			return 'N/A';
            		default :
            			return 'N/A';
            		}
            	}
            }, {
            	id : 'type',
            	header : 'Service Type',
            	dataIndex: 'type',
            	hidden: true,
            	renderer : function(value) {
	    			switch (value) {
	    			case 'WWW':
	    				return 'Web Link';
	    			case 'WFS':
	    				return 'OGC Web Feature Service 1.1.0';
	    			case 'WMS':
	    				return 'OGC Web Map Service 1.1.1';
	    			case 'WCS':
	    				return 'OGC Web Coverage Service 1.0.0';
	    			}

	    			return '';
            	}
            }]
        }]
    });
};

CSWRecordDescriptionWindow.prototype.cswRecords = [];
CSWRecordDescriptionWindow.prototype.store = null;


/**
 * determines whether or not a particular endpoint should be included when loading
 * a layer
 */
var includeEndpoint = function(endpoints, endpoint, includeEndpoints) { 	
	for(var i = 0; i < endpoints.length; i++) {
		if(endpoints[i].indexOf(endpoint) >= 0) {
			return includeEndpoints;
		}
	}
	return !includeEndpoints;
};


Ext.extend(CSWRecordDescriptionWindow, Ext.Window, {
	/**
	 * Given a URL this will determine the correct character that can be appended
	 * so that a number of URL parameters can also be appended
	 *
	 * See AUS-1931 for why this function should NOT exist
	 */
	'internalURLSeperator' : function(url) {
		var lastChar = url[url.length - 1];
		if (lastChar == '?') {
			return '';
		} else if (lastChar == '&') {
			return '';
		} else if (url.indexOf('?') >= 0) {
			return '&';
		} else {
			return '?';
		}
	}
});