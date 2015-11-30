/**
 * Represents a simple Polyline (series of straight line segments)
 */
Ext.define('portal.map.primitives.Polyline', {

    extend : 'portal.map.primitives.BasePrimitive',

    config : {
        /**
         * portal.map.Point[] - the bounds of this polyline
         */
        points : null,
        /**
         * String - HTML Style color string '#RRGGBB' for the vertices of the polygon
         */
        strokeColor : '#00FF00',
        /**
         * Number - Width of the stroke in pixels
         */
        strokeWeight : 1,
        /**
         * Number - the opacity of the vertices in the range [0, 1]
         */
        strokeOpacity : 0.7
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
        this.setPoints(cfg.points);
        this.setStrokeColor(cfg.strokeColor);
        this.setStrokeWeight(cfg.strokeWeight);
        this.setStrokeOpacity(cfg.strokeOpacity);
    }
});