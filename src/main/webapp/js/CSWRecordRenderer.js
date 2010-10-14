
/**
 * The CSWRecordRenderer is a class that when given a CSWRecord (or list of CSWRecords)
 * will 'render' HTML snippets about various aspects of the records
 * 
 *  cswRecords - a CSWRecord or Array of CSWRecords
 */
CSWRecordRenderer = function(cswRecords) {
	if (cswRecords) {
		if (cswRecords instanceof CSWRecord) {
			this.cswRecords = [cswRecords];
		} else {
			this.cswRecords = cswRecords;
		}
	}
};


CSWRecordRenderer.prototype.cswRecords = [];

/*
 * Given a URL, will determine the correct character that can be appended
 * so that a number of URL parameters can also be appended
 * 
 * See AUS-1931 for why this function should not exist
 */
CSWRecordRenderer.prototype.internalURLSeperator = function(url) {
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
};

CSWRecordRenderer.prototype.internalRenderWMS = function(wms, parentCSWRecord) {
	var html = '<tr>';
	
	var getCapUrl = wms.url + this.internalURLSeperator(wms.url) + 'SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1';
	
	
	html += '<td>';
	html += '<a target="_blank" href="' + getCapUrl + '">' + wms.name + '</a>';
	html += '</td>';
	
	html += '<td>';
	html += '<i>' + wms.description + '</i>';
	html += '</td>';
	
	html += '<td>';
	html += '<p>' + wms.url + '</p>';
	html += '</td>';
	
	
	//Form the WMS url
	var getMapUrl = wms.url + this.internalURLSeperator(wms.url) + 'SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.1&LAYERS=' + wms.name;
	getMapUrl += '&SRS=EPSG:4326&FORMAT=image/png&STYLES=';
	
	//To generate the url we will need to use the bounding box to make the request
	//To avoid distortion, we also scale the width height independently
	html += '<td>';
	var geoEls = parentCSWRecord.getGeographicElements();
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
		var width = 256;
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
		
		html += '<a target="_blank" href="' + getMapUrl + '"><img width="' + thumbWidth + '" height="' + thumbHeight + '" alt="Loading preview..." src="' + getMapUrl + '"/></a>';
	}
	html += '</td>';
	
	html += '</tr>';
	return html;
};

CSWRecordRenderer.prototype.internalRenderWFS = function(wfs) {
	var html = '<tr>';
	
	var getFeatureUrl = wfs.url + this.internalURLSeperator(wfs.url) + 'SERVICE=WFS&REQUEST=GetFeature&VERSION=1.1.0&maxFeatures=5&typeName=' + wfs.name;
	var getCapUrl = wfs.url + this.internalURLSeperator(wfs.url) + 'SERVICE=WFS&REQUEST=GetCapabilities&VERSION=1.1.0';
	
	html += '<td>';
	html += '<a target="_blank" href="' + getCapUrl + '">' + wfs.name + '</a>';
	html += '</td>';
	
	html += '<td>';
	html += '<i>' + wfs.description + '</i>';
	html += '</td>';
	
	html += '<td>';
	html += '<p>' + wfs.url + '</p>';
	html += '</td>';
	
	html += '<td>';
	html += '<a target="_blank" href="' + getFeatureUrl + '"><p>First 5 features</p></a>';
	html += '</td>';
	
	html += '</tr>';
	return html;
};

CSWRecordRenderer.prototype.internalRenderWCS = function(wcs) {
	var html = '<tr>';
	
	var describeCoverageUrl = wcs.url + this.internalURLSeperator(wcs.url) + 'SERVICE=WCS&REQUEST=DescribeCoverage&VERSION=1.0.0&coverage=' + wcs.name;
	var getCapUrl = wcs.url + this.internalURLSeperator(wcs.url) + 'SERVICE=WCS&REQUEST=GetCapabilities&VERSION=1.0.0';
	
	html += '<td>';
	html += '<a target="_blank" href="' + getCapUrl + '">' + wcs.name + '</a>';
	html += '</td>';
	
	html += '<td>';
	html += '<i>' + wcs.description + '</i>';
	html += '</td>';
	
	html += '<td>';
	html += '<p>' + wcs.url + '</p>';
	html += '</td>';
	
	html += '<td>';
	html += '<a target="_blank" href="' + describeCoverageUrl + '"><p>DescribeCoverage response</p></a>';
	html += '</td>';
	
	html += '</tr>';
	return html;
};

CSWRecordRenderer.prototype.internalRenderWWW = function(www) {
	var html = '<tr>';
	
	html += '<td>';
	html += '<p>' + www.name + '</p>';
	html += '</td>';
	
	html += '<td>';
	html += '<i>' + www.description + '</i>';
	html += '</td>';
	
	html += '<td>';
	html += '<a target="_blank" href="' + www.url + '">' + www.url + '</a>';
	html += '</td>';
	
	html += '</tr>';
	return html;
};

/**
 * Returns an array of objects in the form
 * {
 * 	onlineResource : Object //the online resource object
 * 	cswRecord : CSWRecord //the parent CSWRecord
 * }
 */
CSWRecordRenderer.prototype.collateOnlineResourcesByType = function(type) { 
	var results = [];
	
	for (var i = 0; i < this.cswRecords.length; i++) {
		var filtered = this.cswRecords[i].getFilteredOnlineResources(type);
		
		for (var j = 0; j < filtered.length; j++) {
			results.push({
				onlineResource : filtered[j],
				cswRecord : this.cswRecords[i]
			});
		}
	}
	
	return results;
};

/**
 * Generates a HTML describing all of the online resources
 * @return
 */
CSWRecordRenderer.prototype.renderOnlineResources = function() {
	var html = '';
	
	//Firstly collate our online resources into their specified types
	var wms = this.collateOnlineResourcesByType('WMS');
	var wfs = this.collateOnlineResourcesByType('WFS');
	var wcs = this.collateOnlineResourcesByType('WCS');
	var www = this.collateOnlineResourcesByType('WWW');
	
	//Now generate our HTML
	html += '<div id="csw-record-renderer-content">';
	html += '<br/>';
	
	if (wms.length > 0) {
		html += '<a target="_blank" href="http://www.opengeospatial.org/standards/wms"><h1>OGC Web Map Services 1.1.1</h1></a>';
		html += '<table border="1" cellspacing="1" cellpadding="2" width="100%">';
		html += '<tr><td><b>Layer</b></td><td><b>Description</b></td><td><b>WMS URL</b></td><td><b>Preview</b></td></tr>';
		
		for (var i = 0; i < wms.length; i++) {
			html += this.internalRenderWMS(wms[i].onlineResource, wms[i].cswRecord);
		}
		
		html += '</table>';
		html += '<br/>';
	}
	
	if (wfs.length > 0) {
		html += '<a target="_blank" href="http://www.opengeospatial.org/standards/wfs"><h1>OGC Web Feature Services 1.1.0</h1></a>';
		html += '<table border="1" cellspacing="1" cellpadding="2" width="100%">';
		html += '<tr><td><b>Feature</b></td><td><b>Description</b></td><td><b>WFS URL</b></td><td><b>Preview</b></td></tr>';
		
		for (var i = 0; i < wfs.length; i++) {
			html += this.internalRenderWFS(wfs[i].onlineResource);
		}
		
		html += '</table>';
		html += '<br/>';
	}
	
	if (wcs.length > 0) {
		html += '<a target="_blank" href="http://www.opengeospatial.org/standards/wcs"><h1>OGC Web Coverage Services 1.0.0</h1></a>';
		html += '<table border="1" cellspacing="1" cellpadding="2" width="100%">';
		html += '<tr><td><b>Coverage</b></td><td><b>Description</b></td><td><b>WCS URL</b></td><td><b>Describe Coverage</b></td></tr>';
		
		for (var i = 0; i < wcs.length; i++) {
			html += this.internalRenderWCS(wcs[i].onlineResource);
		}
		
		html += '</table>';
		html += '<br/>';
	}
	
	if (www.length > 0) {
		html += '<h1>Related Links</h1>';
		html += '<table border="1" cellspacing="1" cellpadding="2" width="100%">';
		html += '<tr><td><b>Name</b></td><td><b>Description</b></td><td><b>URL</b></td></tr>';
		
		for (var i = 0; i < www.length; i++) {
			html += this.internalRenderWWW(www[i].onlineResource);
		}
		
		html += '</table>';
		html += '<br/>';
	}
	
	
	
	html += '</div>';
	
	return html;
};