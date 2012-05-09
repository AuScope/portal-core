/**
 * A class for parsing a KML document into a list of GMarker and GOverlay primitives.
 */
Ext.define('portal.layer.renderer.wfs.KMLParser', {
    /**
     * portal.map.BaseMap - The map to generate primitives for
     */
    map : null,


    /**
     * Must the following config
     * {
     *  kml - string - a String of KML that will be parsed
     *  map - portal.map.BaseMap - The map to generate primitives for
     * }
     */
    constructor : function(config) {
        this.rootNode = portal.util.xml.SimpleDOM.parseStringToDOM(config.kml);
        this.map = config.map;
        this.callParent(arguments);
    },

    /**
     * Given features run through the KML parser, we can extract our gml ID from anything
     * running through the GENERIC PARSER workflow. Everything else gets lost
     */
    descriptionToGmlId : function(description) {
        var idPrefix = 'GENERIC_PARSER:';
        if (description.indexOf(idPrefix) === 0) {
            return description.substring(idPrefix.length);
        }

        return '';
    },

    //Given a series of space seperated CSV tuples, return a list of portal.map.Point
    generateCoordList : function(coordsAsString) {
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

            parsedCoordList.push(Ext.create('portal.map.Point', {
                latitude : parseFloat(coords[1]),
                longitude : parseFloat(coords[0])
            }));
        }

        return parsedCoordList;
    },

    parseLineString : function(onlineResource, layer, name, description, lineStringNode) {
        var parsedCoordList = this.generateCoordList(portal.util.xml.SimpleDOM.getNodeTextContent(lineStringNode.getElementsByTagName("coordinates")[0]));
        if (parsedCoordList.length === 0) {
            return null;
        }

        var gmlId = this.descriptionToGmlId(description);

        return this.map.makePolyline(gmlId, undefined, onlineResource, layer, parsedCoordList, '#FF0000', 3, 1);
    },

    //Given a root placemark node attempt to parse it as a single point and return it
    //Returns a single portal.map.primitives.Polygon
    parsePolygon : function(onlineResource, layer, name, description, polygonNode) {
        var parsedCoordList = this.generateCoordList(portal.util.xml.SimpleDOM.getNodeTextContent(polygonNode.getElementsByTagName("coordinates")[0]));
        if (parsedCoordList.length === 0) {
            return null;
        }

        var gmlId = this.descriptionToGmlId(description);
        return this.map.makePolygon(gmlId, undefined, onlineResource, layer, parsedCoordList, undefined, undefined,0.7,undefined,0.6);
    },

    //Given a root placemark node attempt to parse it as a single point and return it
    //Returns a single portal.map.primitives.Marker
    parsePoint : function(onlineResource, layer, name, description, icon, pointNode) {
        var textCoordinates = portal.util.xml.SimpleDOM.getNodeTextContent(pointNode.getElementsByTagName("coordinates")[0]);
        var coordinates = textCoordinates.split(',');

        // We do not want placemarks without coordinates
        if (!coordinates || coordinates.length < 2) {
            return null;
        }

        var lon = coordinates[0];
        var lat = coordinates[1];
        var point = Ext.create('portal.map.Point', {latitude : parseFloat(lat), longitude : parseFloat(lon)});
        var gmlId = this.descriptionToGmlId(description);
        return this.map.makeMarker(gmlId, name,undefined, onlineResource, layer, point, icon);
    },

    makePrimitives : function(icon, onlineResource, layer) {
        var primitives = [];
        var placemarks = this.rootNode.getElementsByTagName("Placemark");

        for(i = 0; i < placemarks.length; i++) {
            var placemarkNode = placemarks[i];
            var mapItem = null;

            //Get the settings global to the placemark

            var name = portal.util.xml.SimpleDOM.getNodeTextContent(placemarkNode.getElementsByTagName("name")[0]);
            var description = portal.util.xml.SimpleDOM.getNodeTextContent(placemarkNode.getElementsByTagName("description")[0]);

            //Then extract the actual geometry for the placemark
            var polygonList = placemarkNode.getElementsByTagName("Polygon");
            var lineStringList = placemarkNode.getElementsByTagName("LineString");
            var pointList = placemarkNode.getElementsByTagName("Point");

            //Now parse the geometry
            //Parse any polygons
            for (var j = 0; j < polygonList.length; j++) {
                mapItem = this.parsePolygon(onlineResource, layer, name, description, polygonList[j]);
                if (mapItem === null) {
                    return;
                }

                primitives.push(mapItem);
            }

            //Parse any lineStrings
            for (var j = 0; j < lineStringList.length; j++) {
                mapItem = this.parseLineString(onlineResource, layer, name, description, lineStringList[j]);
                if (mapItem === null) {
                    return;
                }

                primitives.push(mapItem);
            }

            //Parse any points
            for (var j = 0; j < pointList.length; j++) {
                mapItem = this.parsePoint(onlineResource, layer, name, description, icon, pointList[j]);
                if (mapItem === null) {
                    return;
                }

                primitives.push(mapItem);
            }

        }


        return primitives;
    }
});


