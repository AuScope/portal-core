/**
 * Represents a simple point based marker as implemented by the Gmap API
 */
Ext.define('portal.map.gmap.primitives.Marker', {

    extend : 'portal.map.primitives.Marker',

    config : {
        /**
         * Instance of GMap2 Marker
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

        var latLng = new GLatLng(point.getLatitude(), point.getLongitude());
        var gIcon = new GIcon(G_DEFAULT_ICON, icon.getUrl());
        gIcon.shadow = null;
        if (Ext.isNumber(icon.getWidth()) && Ext.isNumber(icon.getHeight())) {
            gIcon.iconSize = new GSize(icon.getWidth(), icon.getHeight());
        }
        if (Ext.isNumber(icon.getAnchorOffsetX()) && Ext.isNumber(icon.getAnchorOffsetY())) {
            gIcon.iconAnchor = new GPoint(icon.getAnchorOffsetX(), icon.getAnchorOffsetY());
            gIcon.infoWindowAnchor = new GPoint(icon.getAnchorOffsetX(), icon.getAnchorOffsetY());
        }

        var marker = new GMarker(latLng, {icon: gIcon, title: cfg.tooltip});

        //Overload marker with useful info
        marker._portalBasePrimitive = this;

        this.setMarker(marker);
    }
});