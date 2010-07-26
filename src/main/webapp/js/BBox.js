
/**
 * Represents a simple Bounding Box 
 * @param northBoundLatitude Northern most latitude
 * @param southBoundLatitude Southern most latitude
 * @param eastBoundLongitude Eastern most longitude (in the range [-180, 180) )
 * @param westBoundLongitude Western most longitude (in the range [-180, 180) ) 
 */
BBox = function(northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude) {
	this.eastBoundLongitude = eastBoundLongitude;
	this.westBoundLongitude = westBoundLongitude;
	this.southBoundLatitude = southBoundLatitude;
	this.northBoundLatitude = northBoundLatitude;
};

BBox.prototype.eastBoundLongitude = 0;
BBox.prototype.westBoundLongitude = 0;
BBox.prototype.southBoundLatitude = 0;
BBox.prototype.northBoundLatitude = 0;
BBox.prototype.crs = 'EPSG:4326';

/**
 * Returns true if the bounding box spans the entire planet
 */
BBox.prototype.isGlobal = function() {
	return this.eastBoundLongitude == 180 && this.northBoundLatitude == 90 &&
	this.southBoundLatitude == -90 && this.westBoundLongitude == -180;
};

/**
 * Returns a clone of this bounding box
 */
BBox.prototype.clone = function() {
	return new BBox(this.northBoundLatitude, this.southBoundLatitude, this.eastBoundLongitude, this.westBoundLongitude, this.crs);
};

/**
 * Returns a list of BBox objects representing the bbox being split into 2 at the
 * specified latitude and longitudes (Will return 1,2 or 4 bbox objects)
 * @param longitude [Optional] The longitude to split at in the range [-180, 180)
 * @param latitude [Optional] The latitude to split at
 * @return
 */
BBox.prototype.splitAt = function(longitude, latitude) {
	
	var splitter = function (left, right, value, splits) {
		var newSplits = [];
		
		for (var i = 0; i < splits.length; i++) {
			var bbox = splits[i];
			var leftSplit = bbox.clone();
			var rightSplit = bbox.clone();
			
			//If we split across a range that sees a sign flip
			//ensure the sign across each split rectangle is equal
			var leftSplitValue = value;
			while (leftSplitValue < 0 && leftSplit[left] > 0)
				leftSplitValue += 360;
			while (leftSplitValue > 0 && leftSplit[left] < 0)
				leftSplitValue -= 360;
			var rightSplitValue = value;
			while (rightSplitValue < 0 && rightSplit[right] > 0)
				rightSplitValue += 360;
			while (rightSplitValue > 0 && rightSplit[right] < 0)
				rightSplitValue -= 360;
			
			leftSplit[left] = bbox[left];
			leftSplit[right] = leftSplitValue;
			rightSplit[left] = rightSplitValue;
			rightSplit[right] = bbox[right];
				
			newSplits.push(leftSplit);
			newSplits.push(rightSplit);
		}
		
		return newSplits;
	};
	
	
	var splits = [this];
	
	if (longitude !== undefined) {
		splits = splitter('westBoundLongitude', 'eastBoundLongitude', longitude, splits);
	}
	
	if (latitude !== undefined) {
		splits = splitter('northBoundLatitude', 'southBoundLatitude', latitude, splits);
	}
	
	return splits;
};
