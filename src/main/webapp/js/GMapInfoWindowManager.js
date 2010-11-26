function GMapInfoWindowManager (map) {
    this.map = map;
}

//Instance variables
GMapInfoWindowManager.prototype.map = null;


//Methods

/**
 * Opens an info window at a location with the specified content. When the window loads initFunction will be called
 * 
 * windowLocation - either a GMarker or a GLatLng which is where the window will be opened from
 * content - A HTML string representing the content of the window OR a array of GInfoWindowTab
 * infoWindowOpts - [Optional] an instance of GInfoWindowOptions that will be passed to the new window
 * initFunction - [Optional] function(map, location, initFunctionParam) a function that will be called with initFunctionParam when the window opens
 * initFunctionParam - [Optional] will be passed as a parameter to initFunction
 * 
 */
GMapInfoWindowManager.prototype.openInfoWindow = function(windowLocation, content,infoWindowOpts , initFunction, initFunctionParam) {
	
	//We listen for the open event once
	var scope = this;
	var listenerHandler = null;
	var listenerFunction = function() {
		GEvent.removeListener(listenerHandler);
		
		if (initFunction) {
			initFunction(scope.map, windowLocation, initFunctionParam);
		}
	};

	
	//Figure out which function to call based upon our parameters
	if (windowLocation instanceof GLatLng) {
		listenerHandler = GEvent.addListener(this.map, "infowindowopen", listenerFunction);
		
		if (content instanceof Array) {
			this.map.openInfoWindowTabs(windowLocation, content, infoWindowOpts);
		} else if (typeof(content) === "string") {
			this.map.openInfoWindowHtml(windowLocation, content, infoWindowOpts);
		}
	} else if (windowLocation instanceof GMarker) {
		listenerHandler = GEvent.addListener(windowLocation, "infowindowopen", listenerFunction);
		
		if (content instanceof Array) {
			windowLocation.openInfoWindowTabs(content, infoWindowOpts);
		} else if (typeof(content) === "string") {
			windowLocation.openInfoWindowHtml(content, infoWindowOpts);
		}
	}
};