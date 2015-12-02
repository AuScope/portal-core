/**
 * Represents a simple point based marker as implemented by the OpenLayers API
 */
Ext.define('portal.map.openlayers.primitives.Marker', {

    extend : 'portal.map.primitives.Marker',

    config : {
        /**
         * Instance of OpenLayers.Feature.Vector
         */
        vector : null
    },

    /**
     * Accepts the following in addition to portal.map.primitives.Marker's constructor options
     *
     * tooltip : String - Tooltip to show on mouseover of this marker (if supported).
     * point : portal.map.Point - location of this marker
     * icon : portal.map.Icon - Information about the icon used to render this marker.
     */
    constructor : function(cfg) {
        this.callParent(arguments);

        var point = this.getPoint();
        var icon = this.getIcon();

        //Style info about the icon
        var iconWidth = undefined;
        var iconHeight = undefined;
        var iconOffsetX = undefined;
        var iconOffsetY = undefined;
        if (icon && Ext.isNumber(icon.getWidth()) && Ext.isNumber(icon.getHeight())) {
            iconWidth = icon.getWidth();
            iconHeight = icon.getHeight();
        }
        if (icon && Ext.isNumber(icon.getAnchorOffsetX()) && Ext.isNumber(icon.getAnchorOffsetY())) {
            iconOffsetX = (0 - icon.getAnchorOffsetX());
            iconOffsetY = (0 - icon.getAnchorOffsetY());
        }

        //Construct our feature
        var olPoint = new OpenLayers.Geometry.Point(point.getLongitude(), point.getLatitude());
        var vector = new OpenLayers.Feature.Vector(olPoint, {
            portalBasePrimitive : this
        }, {
            externalGraphic : icon.getUrl(),
            graphicWidth : iconWidth,
            graphicHeight : iconHeight,
            graphicXOffset : iconOffsetX,
            graphicYOffset : iconOffsetY,
            cursor : 'pointer',
            graphicTitle : cfg.tooltip

        });

        this.setVector(vector);


    }
});