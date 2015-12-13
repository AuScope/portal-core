/**
 * Represents a simple closed polygon as implemented by the Gmap API
 */
Ext.define('portal.map.gmap.primitives.Polygon', {

    extend : 'portal.map.primitives.Polygon',

    config : {
        /**
         * Instance of a GPolygon
         */
        polygon : null
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

        var latLngs = [];
        for (var i = 0; i < cfg.points.length; i++) {
            latLngs.push(new GLatLng(cfg.points[i].getLatitude(), cfg.points[i].getLongitude()));
        }

        var polygon = new GPolygon(latLngs, this.getStrokeColor(), this.getStrokeWeight(), this.getStrokeOpacity(), this.getFillColor(), this.getFillOpacity());
        polygon._portalBasePrimitive = this;

        this.setPolygon(polygon);
    }
});