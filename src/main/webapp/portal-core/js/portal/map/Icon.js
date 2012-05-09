/**
 * Represents information about an Icon that can be reused by
 * some aspects of the map
 */
Ext.define('portal.map.Icon', {

    config : {
        /**
         * String - URL of the icon image
         */
        url : '',
        /**
         * Number - the width of the icon in pixels
         */
        width : 0,
        /**
         * Number - the height of the icon in pixels
         */
        height : 0,
        /**
         * Number - the offset in pixels (x direction) of the point in the icon which will be anchored (touching) the map.
         */
        anchorOffsetX : 0,
        /**
         * Number - the offset in pixels (y direction) of the point in the icon which will be anchored (touching) the map.
         */
        anchorOffsetY : 0
    },

    /**
     * Accepts the following
     *
     * url : String - URL of the icon image
     * width : Number - the width of the icon in pixels
     * height : Number - the height of the icon in pixels
     * anchorOffsetX : Number - the offset in pixels (x direction) of the point in the icon which will be anchored (touching) the map.
     * anchorOffsetY : Number - the offset in pixels (y direction) of the point in the icon which will be anchored (touching) the map.
     */
    constructor : function(cfg) {
        this.callParent(arguments);
        this.setUrl(cfg.url);
        this.setWidth(cfg.width);
        this.setHeight(cfg.height);
        this.setAnchorOffsetX(cfg.anchorOffsetX);
        this.setAnchorOffsetY(cfg.anchorOffsetY);
    }
});