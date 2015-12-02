/**
 * Represents a simple closed polygon as implemented by the Gmap API
 */
Ext.define('portal.map.openlayers.primitives.Polygon', {

    extend : 'portal.map.primitives.Polygon',

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
     * fillColor : String - HTML Style color string '#RRGGBB' for the fill of the polygon
     * fillOpacity : Number - the opacity of the fill in the range [0, 1]
     */
    constructor : function(cfg) {
        this.callParent(arguments);


        //Construct our geometry
        var olPoints = [];
        for (var i = 0; i < cfg.points.length; i++) {
            olPoints.push(new OpenLayers.Geometry.Point(cfg.points[i].getLongitude(), cfg.points[i].getLatitude()));
        }
        var olRing = new OpenLayers.Geometry.LinearRing(olPoints);
        var olPolygon = new OpenLayers.Geometry.Polygon(olRing);

        //Construct our feature
        var vector = new OpenLayers.Feature.Vector(olPolygon, {
            portalBasePrimitive : this
        }, {
            fill : true,
            fillColor : this.getFillColor(),
            fillOpacity : this.getFillOpacity(),
            stroke : true,
            strokeColor : this.getStrokeColor(),
            strokeOpacity : this.getStrokeOpacity(),
            strokeWidth : this.getStrokeWeight()
        });


        this.setVector(vector);
    }
});