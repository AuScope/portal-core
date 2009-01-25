
function isRecognizedFeature(sFeature) {
  for (var i=0; i<gaFeatureTypes.length; i++) {
    if (gaFeatureTypes[i] == sFeature) 
      return true;
  }
  return false;
}