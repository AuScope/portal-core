/**
 * A class for parsing a GML document into a list of Marker and Overlay primitives.
 */
Ext.define('portal.layer.renderer.wfs.GMLParser', {
    /**
     * portal.map.BaseMap - The map to generate primitives for
     */
    map : null,


    /**
     * Must the following config
     * {
     *  gml - string - a String of KML that will be parsed
     *  map - portal.map.BaseMap - The map to generate primitives for
     * }
     */
    constructor : function(config) {
        this.rootNode = portal.util.xml.SimpleDOM.parseStringToDOM(config.gml);
        this.map = config.map;
        this.callParent(arguments);
    },

    //Given a series of space seperated tuples, return a list of portal.map.Point
    generateCoordList : function(coordsAsString, srsName) {
        var coordinateList = coordsAsString.split(' ');
        var parsedCoordList = [];
        
        for (var i = 0; i < coordinateList.length; i+=2) {
            this.forceLonLat(coordinateList, srsName, i);

            parsedCoordList.push(Ext.create('portal.map.Point', {
                latitude : parseFloat(coordinateList[i + 1]),
                longitude : parseFloat(coordinateList[i])
            }));
        }

        return parsedCoordList;
    },
    
    getSrsName : function(node) {
        var srsName = node.getAttribute("srsName");
        if (Ext.isEmpty(srsName)) {
            srsName = node.getAttribute("srs");
        }
        
        if (!srsName) {
            return '';
        }
        
        return srsName;
    },
    
    /**
     * Forces lon/lat coords into coords (an array). Swaps coords[offset] and coords[offset + 1] if srsName requires it
     */
    forceLonLat : function(coords, srsName, offset) {
        if (!offset) {
            offset = 0;
        }
        
        if (srsName.indexOf('http://www.opengis.net/gml/srs/epsg.xml#4283') == 0 || 
            srsName.indexOf('urn:x-ogc:def:crs:EPSG') == 0) {
            //lat/lon
            var tmp = coords[offset];
            coords[offset] = coords[offset + 1];
            coords[offset + 1] = tmp;
        } else if (srsName.indexOf('EPSG') == 0 ||
                   srsName.indexOf('http://www.opengis.net/gml/srs/epsg.xml') == 0) {
            //lon/lat (no action required)
        } else {
            //fallback to lon/lat
        }
    },

    parseLineString : function(onlineResource, layer, name, description, lineStringNode) {
        var srsName = this.getSrsName(lineStringNode);
        var parsedCoordList = this.generateCoordList(portal.util.xml.SimpleDOM.getNodeTextContent(lineStringNode.getElementsByTagNameNS("*", "posList")[0]), srsName);
        if (parsedCoordList.length === 0) {
            return null;
        }

        //I've seen a few lines come in with start/end points being EXACTLY the same with no other points. These can be ignored
        if (parsedCoordList.length === 2) {
            if (parsedCoordList[0].getLongitude() === parsedCoordList[1].getLongitude() &&
                parsedCoordList[0].getLatitude() === parsedCoordList[1].getLatitude()) {
                return null;
            }
        }
        
        return this.map.makePolyline(name, undefined, onlineResource, layer, parsedCoordList, '#FF0000', 3, 1);
    },

    //Given a root placemark node attempt to parse it as a single point and return it
    //Returns a single portal.map.primitives.Polygon
    parsePolygon : function(onlineResource, layer, name, description, polygonNode) {
        var srsName = this.getSrsName(polygonNode);
        var parsedCoordList = this.generateCoordList(portal.util.xml.SimpleDOM.getNodeTextContent(polygonNode.getElementsByTagNameNS("*", "posList")[0]), srsName);
        if (parsedCoordList.length === 0) {
            return null;
        }
        
        //I've seen a few lines come in with start/end points being EXACTLY the same with no other points. These can be ignored
        if (parsedCoordList.length === 2) {
            if (parsedCoordList[0].getLongitude() === parsedCoordList[1].getLongitude() &&
                parsedCoordList[0].getLatitude() === parsedCoordList[1].getLatitude()) {
                return null;
            }
        }

        return this.map.makePolygon(name, undefined, onlineResource, layer, parsedCoordList, undefined, undefined,0.7,undefined,0.6);
    },

    //Given a root placemark node attempt to parse it as a single point and return it
    //Returns a single portal.map.primitives.Marker
    parsePoint : function(onlineResource, layer, name, description, icon, pointNode) {
        var rawPoints = portal.util.xml.SimpleDOM.getNodeTextContent(pointNode.getElementsByTagNameNS("*", "pos")[0]);
        var coordinates = rawPoints.split(' ');
        if (!coordinates || coordinates.length < 2) {
            return null;
        }
        
        //Workout whether we are lat/lon or lon/lat
        var srsName = this.getSrsName(pointNode);
        this.forceLonLat(coordinates, srsName);

        var lon = coordinates[0];
        var lat = coordinates[1];
        var point = Ext.create('portal.map.Point', {latitude : parseFloat(lat), longitude : parseFloat(lon)});
        return this.map.makeMarker(name, description, undefined, onlineResource, layer, point, icon);
    },

    /**
     * Returns the feature count as reported by the WFS response. Returns null if the count cannot be parsed.
     */
    getFeatureCount : function() {
        var wfsFeatureCollection = portal.util.xml.SimpleDOM.getMatchingChildNodes(this.rootNode, null, "FeatureCollection");
        if (Ext.isEmpty(wfsFeatureCollection)) {
            return null;
        }
        
        var count = parseInt(wfsFeatureCollection[0].getAttribute('numberOfFeatures'));
        if (Ext.isNumber(count)) {
            return count;
        }
        
        return null;
    },
    
    makePrimitives : function(icon, onlineResource, layer) {
        var primitives = [];
        var wfsFeatureCollection = portal.util.xml.SimpleDOM.getMatchingChildNodes(this.rootNode, null, "FeatureCollection");
        
        //Read through our wfs:FeatureCollection and gml:featureMember(s) elements
        if (Ext.isEmpty(wfsFeatureCollection)) {
            return primitives;
        }
        var featureMembers = portal.util.xml.SimpleDOM.getMatchingChildNodes(wfsFeatureCollection[0], null, "featureMembers");
        var features = [];
        
        if (Ext.isEmpty(featureMembers)) {
            featureMembers = portal.util.xml.SimpleDOM.getMatchingChildNodes(wfsFeatureCollection[0], null, "featureMember");
            for (var i = 0; i < featureMembers.length; i++) {
                features.push(featureMembers[i].firstElementChild);
            }
        }else{
            features = featureMembers[0].childNodes;
        }
        
        for(var i = 0; i < features.length; i++) {
            //Pull out some general stuff that we expect all features to have
            var featureNode = features[i]; 
            var name = featureNode.getAttribute('gml:id');
            var description = portal.util.xml.SimpleXPath.evaluateXPath(this.rootNode, featureNode, "gml:description", portal.util.xml.SimpleXPath.XPATH_STRING_TYPE).stringValue;
            if (Ext.isEmpty(description)) {
                description = portal.util.xml.SimpleXPath.evaluateXPath(this.rootNode, featureNode, "gml:name", portal.util.xml.SimpleXPath.XPATH_STRING_TYPE).stringValue;
                if (Ext.isEmpty(description)) {
                    description = name; //resort to gml ID if we have to
                }
            }
            
            //Look for geometry under this feature
            var pointNodes = featureNode.getElementsByTagNameNS("*", "Point");
            var polygonNodes = featureNode.getElementsByTagNameNS("*", "Polygon");
            var lineStringNodes = featureNode.getElementsByTagNameNS("*", "LineString");
            
            //Parse the geometry we found into map primitives
            for (var geomIndex = 0; geomIndex < polygonNodes.length; geomIndex++) {
                mapItem = this.parsePolygon(onlineResource, layer, name, description, polygonNodes[geomIndex]);
                if (mapItem !== null) {
                    primitives.push(mapItem);
                }
            }
            
            for (var geomIndex = 0; geomIndex < pointNodes.length; geomIndex++) {
                mapItem = this.parsePoint(onlineResource, layer, name, description, icon, pointNodes[geomIndex]);
                if (mapItem !== null) {
                    primitives.push(mapItem);
                }
            }
            
            for (var geomIndex = 0; geomIndex < lineStringNodes.length; geomIndex++) {
                mapItem = this.parseLineString(onlineResource, layer, name, description, lineStringNodes[geomIndex]);
                if (mapItem !== null) {
                    primitives.push(mapItem);
                }
            }
        }

        return primitives;
    }
});


