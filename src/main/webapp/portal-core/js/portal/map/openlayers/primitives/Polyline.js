/**
 * Represents a simple Polyline (series of straight line segments) as implemented by the OpenLayers API
 */
Ext.define('portal.map.openlayers.primitives.Polyline', {

    extend : 'portal.map.primitives.Polyline',

    config : {
        /**
         * Instance of a OpenLayers.Feature.Vector
         */
        vector : null
    },

    /**
     * Accepts the following in addition to portal.map.primitives.BasePrimitive's constructor options
     *
     * points : portal.map.Point[] - the bounds of this polygon
     * strokeColor : String - HTML Style color string '#RRGGBB' for the vertices of the polygon
     * strokeWeight : Number - Width of the stroke in pixels
     * strokeOpacity : Number - the opacity of the vertices in the range [0, 1]
     */
    constructor : function(cfg) {

        this.callParent(arguments);

        var olPoints = [];
        for (var i = 0; i < cfg.points.length; i++) {
            olPoints.push(new OpenLayers.Geometry.Point(cfg.points[i].getLongitude(), cfg.points[i].getLatitude()));
        }

        var olLineString = new OpenLayers.Geometry.LineString(olPoints);

        //Construct our feature
        var vector = new OpenLayers.Feature.Vector(olLineString, undefined, {
            stroke : true,
            strokeColor : this.getStrokeColor(),
            strokeOpacity : this.getStrokeOpacity(),
            strokeWidth : this.getStrokeWeight()
        });

        this.setVector(vector);
    }
});