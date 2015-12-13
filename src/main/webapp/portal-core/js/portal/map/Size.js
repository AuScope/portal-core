/**
 * Represents a size of some mapping component
 */
Ext.define('portal.map.Size', {

    config : {
        /**
         * Number - the width in pixels
         */
        width : 0,
        /**
         * Number - the height in pixels
         */
        height : 0
    },

    /**
     * Accepts the following
     *
     * width : Number - the width in pixels
     * height : Number - the height in pixels
     */
    constructor : function(cfg) {
        this.callParent(arguments);
        this.setWidth(cfg.width);
        this.setHeight(cfg.height);
    }
});