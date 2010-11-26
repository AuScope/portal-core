
function isRecognizedFeature(sFeature) {
  for (var i=0; i<gaFeatureTypes.length; i++) {
    if (gaFeatureTypes[i] == sFeature) {
      return true;
    }
  }
  return false;
}

/*
 * This function returns an array of years: 
 * [current_year, ..., yearFrom] 
 * eg. if the year now is 2009 the call to getYearsArrayFrom(2007)
 * will return [2009,2008,2007] 
 */
function getYearsArrayFrom(yearFrom) {
	var years = [];
	var currentYear = new Date().getFullYear();
	
	for (x=0;x<= currentYear - yearFrom;x++ ) {
		years[x] = currentYear - x;
	}
	return years;		
}