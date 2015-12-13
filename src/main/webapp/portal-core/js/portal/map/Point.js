/**
 * Represents a single point in WGS:84 space
 */
Ext.define('portal.map.Point', {

    config : {
        /**
         * String - URL of the icon image
         */
        srs : 'WGS:84',
        /**
         * Number - the latitude of this point (WGS:84)
         */
        latitude : 0,
        /**
         * Number - the longitude of this point (WGS:84)
         */
        longitude : 0
    },

    /**
     * Accepts the following
     *
     * latitude : Number - the latitude of this point (WGS:84)
     * longitude : Number - the longitude of this point (WGS:84)
     */
    constructor : function(cfg) {
        this.callParent(arguments);
        this.setSrs(cfg.srs);
        this.setLatitude(cfg.latitude);
        this.setLongitude(cfg.longitude);
    }
});