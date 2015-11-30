/**
 * Represents a simple point based marker
 */
Ext.define('portal.map.primitives.Marker', {

    extend : 'portal.map.primitives.BasePrimitive',

    config : {
        /**
         * String - Tooltip to show on mouseover of this marker (if supported).
         */
        tooltip : '',
        /**
         * portal.map.Point - Location of this marker
         */
        point : null,
        /**
         * portal.map.Icon - Information about the icon used to render this marker.
         */
        icon : null
    },

    /**
     * Accepts the following in addition to portal.map.primitives.BasePrimitive's constructor options
     *
     * tooltip : String - Tooltip to show on mouseover of this marker (if supported).
     * point : portal.map.Point - location of this marker
     * icon : portal.map.Icon - Information about the icon used to render this marker.
     */
    constructor : function(cfg) {
        this.callParent(arguments);
        this.setTooltip(cfg.tooltip);
        this.setPoint(cfg.point);
        this.setIcon(cfg.icon);
    }
});