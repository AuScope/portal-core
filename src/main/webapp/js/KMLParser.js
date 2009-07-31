function KMLParser(kml) {

    var rootNode = GXml.parse(kml).documentElement;

    this.makeMarkers = function(icon) {
        //an array to hold the markers
        var markers = [];

        var placemarks = rootNode.selectNodes(".//*[local-name() = 'Placemark']");

        //alert(placemarks[0].selectSingleNode(".//*[local-name() = 'name']").text);
        try {
            for(i = 0; i < placemarks.length; i++) {
                var name = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'name']"));
                var description = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'description']"));
                var coordinates = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'coordinates']")).split(',');
                //iconlast = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'Icon']/*[local-name() = 'href']")).split(',');
                var lon = coordinates[0];
                var lat = coordinates[1];
                var z = coordinates[2];


                var point = new GLatLng(parseFloat(lat), parseFloat(lon));

                var marker = new GMarker(point, {icon: icon});
                marker.description = description;

                markers.push(marker);
            }
        } catch(e) {alert(e);}

        return markers;
    };

};


