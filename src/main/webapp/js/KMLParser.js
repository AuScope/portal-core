
KMLParser = function(kml) {
    this.rootNode = GXml.parse(kml).documentElement;
    this.markers = [];
    this.overlays = [];
};

//Given a series of space seperated CSV tuples, return a list of GLatLng
KMLParser.prototype.generateCoordList = function(coordsAsString) {
	var coordinateList = coordsAsString.split(' ');
	var parsedCoordList = [];
    for (var i = 0; i < coordinateList.length; i++) {
    	if (coordinateList[i].length === 0) {
    		continue;
    	}

    	var coords = coordinateList[i].split(',');

    	if (coords.length === 0) {
    		continue;
    	}

    	parsedCoordList.push(new GLatLng(parseFloat(coords[1]), parseFloat(coords[0])));
    }

    return parsedCoordList;
};

KMLParser.prototype.parseLineString = function(name, description, lineStringNode) {

    var parsedCoordList = this.generateCoordList(GXml.value(lineStringNode.getElementsByTagName("coordinates")[0]));
    if (parsedCoordList.length === 0) {
    	return null;
    }

    var lineString = new GPolyline(parsedCoordList, '#FF0000',3, 1, undefined);

    lineString.description = description;
    lineString.title = name;

    return lineString;
};

//Given a root placemark node attempt to parse it as a single point and return it
//Returns a single GPolygon
KMLParser.prototype.parsePolygon = function(name, description, polygonNode) {
    var parsedCoordList = this.generateCoordList(GXml.value(polygonNode.getElementsByTagName("coordinates")[0]));
    if (parsedCoordList.length === 0) {
    	return null;
    }

    var polygon = new GPolygon(parsedCoordList,undefined, undefined, 0.7,undefined, 0.6);
    polygon.description = description;
    polygon.title = name;

    return polygon;
};

//Given a root placemark node attempt to parse it as a single point and return it
//Returns a single GMarker
KMLParser.prototype.parsePoint = function(name, description, icon, pointNode) {
    var coordinates = GXml.value(pointNode.getElementsByTagName("coordinates")[0]).split(',');

    // We do not want placemarks without coordinates
    if (coordinates === "") {
        return null;
    }

    //iconlast = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'Icon']/*[local-name() = 'href']")).split(',');
    var lon = coordinates[0];
    var lat = coordinates[1];
    var z = coordinates[2];

    var point = new GLatLng(parseFloat(lat), parseFloat(lon));

    var marker = new GMarker(point, {icon: icon, title: name});
    marker.description = description;
    marker.title = name;

    return marker;
};

KMLParser.prototype.makeMarkers = function(icon, markerHandler) {

    var markers = [];

    var placemarks = this.rootNode.getElementsByTagName("Placemark");

    try {
        for(i = 0; i < placemarks.length; i++) {
        	var placemarkNode = placemarks[i];
        	var mapItem = null;

            //Get the settings global to the placemark
            var name = GXml.value(placemarkNode.getElementsByTagName("name")[0]);
            var description = GXml.value(placemarkNode.getElementsByTagName("description")[0]);

            //Then extract the actual geometry for the placemark
            var polygonList = placemarkNode.getElementsByTagName("Polygon");
            var lineStringList = placemarkNode.getElementsByTagName("LineString");
            var pointList = placemarkNode.getElementsByTagName("Point");

        	//Now parse the geometry
            //Parse any polygons
            for (var j = 0; j < polygonList.length; j++) {
            	mapItem = this.parsePolygon(name, description, polygonList[j]);
                if (mapItem === null) {
                    return;
                }

                if(markerHandler) {
                    markerHandler(mapItem);
                }

                this.overlays.push(mapItem);
        	}

            //Parse any lineStrings
            for (var j = 0; j < lineStringList.length; j++) {
            	mapItem = this.parseLineString(name, description, lineStringList[j]);
                if (mapItem === null) {
                    return;
                }

                if(markerHandler) {
                    markerHandler(mapItem);
                }

                this.overlays.push(mapItem);
        	}

            //Parse any points
            for (var j = 0; j < pointList.length; j++) {
            	mapItem = this.parsePoint(name, description, icon, pointList[j]);
                if (mapItem === null) {
                    return;
                }

                if(markerHandler) {
                    markerHandler(mapItem);
                }

                this.markers.push(mapItem);
        	}

        }
    } catch(e) {alert(e);}

    return markers;
};


