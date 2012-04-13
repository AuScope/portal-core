/**
 * Represents a simple point based marker as implemented by the OpenLayers API
 */
Ext.define('portal.map.openlayers.primitives.Marker', {

    extend : 'portal.map.primitives.Marker',

    config : {
        /**
         * Instance of OpenLayers.Marker
         */
        marker : null
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

        var olPoint = new OpenLayers.LonLat(point.getLongitude(), point.getLatitude());
        var olIconSize = undefined;
        var olIconOffset = undefined;
        if (Ext.isNumber(icon.getWidth()) && Ext.isNumber(icon.getHeight())) {
            olIconSize = {
                w : icon.getWidth(),
                h : icon.getHeight()
            };
        }
        if (Ext.isNumber(icon.getAnchorOffsetX()) && Ext.isNumber(icon.getAnchorOffsetY())) {
            olIconOffset = {
                x : (0 - icon.getAnchorOffsetX()),
                y : (0 - icon.getAnchorOffsetY())
            };
        }
        var olIcon = new OpenLayers.Icon(icon.getUrl(), olIconSize, olIconOffset);

        var marker = new OpenLayers.Marker(olPoint, olIcon);
        marker._portalBasePrimitive = this;

        this.setMarker(marker);
    }
});