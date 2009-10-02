
KMLParser = function(kml) {
    this.rootNode = GXml.parse(kml).documentElement;
};

KMLParser.prototype.makeMarkers = function(icon, markerHandler) {
    
    var markers = [];
    // var placemarks = this.rootNode.selectNodes(".//*[local-name() = 'Placemark']");
    // alert(placemarks[0].selectSingleNode(".//*[local-name() = 'Placemark']").text);
    
    var placemarks = this.rootNode.getElementsByTagName("Placemark");
    
    try {
        for(i = 0; i < placemarks.length; i++) {
            // var name = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'name']"));
            var name = GXml.value(placemarks[i].getElementsByTagName("name")[0]);

            // var description = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'description']"));
            var description = GXml.value(placemarks[i].getElementsByTagName("description")[0]);

            // var coordinates = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'coordinates']")).split(',');
            var coordinates = GXml.value(placemarks[i].getElementsByTagName("coordinates")[0]).split(',');

            // We do not want placemarks without coordinates
            if (coordinates == "")
                continue;
            
            //iconlast = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'Icon']/*[local-name() = 'href']")).split(',');
            var lon = coordinates[0];
            var lat = coordinates[1];
            var z = coordinates[2];

            var point = new GLatLng(parseFloat(lat), parseFloat(lon));

            var marker = new GMarker(point, {icon: icon});
            marker.description = description;
            marker.title = name;

            //if there are some custom properties that need to be set
            if(markerHandler)
                markerHandler(marker);

            markers.push(marker);
        }
    } catch(e) {alert(e);}

    return markers;
};


