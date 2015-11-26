/**
 * Represents a simple Polyline (series of straight line segments) as implemented by the Gmap API
 */
Ext.define('portal.map.gmap.primitives.Polyline', {

    extend : 'portal.map.primitives.Polyline',

    config : {
        /**
         * GPolyline instance
         */
        polyline : null
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

        var latLngs = [];
        for (var i = 0; i < cfg.points.length; i++) {
            latLngs.push(new GLatLng(cfg.points[i].getLatitude(), cfg.points[i].getLongitude()));
        }

        var line = new GPolyline(latLngs, this.getStrokeColor(), this.getStrokeWeight(), this.getStrokeOpacity());
        line._portalBasePrimitive = this;

        this.setPolyline(line);
    }
});