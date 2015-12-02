/**
 * Base primitive that all map primitives should extend
 */
Ext.define('portal.map.primitives.BasePrimitive', {

    config : {
        /**
         * an identification string that has no uniqueness requirement. Eg - used for identifying WFS feature gml:id's
         */
        id : '',
        /**
         * portal.layer.Layer - the layer that spawned this primitive. Can be null or empty
         */
        layer : null,
        /**
         * portal.csw.OnlineResource - the OnlineResource that spawned this primitive. Can be null or empty
         */
        onlineResource : null,
        /**
         * portal.csw.CSWRecord - the CSWRecord that spawned this primitive. Can be null or empty.
         */
        cswRecord : null
    },

    /**
     * Accepts the following
     *
     * id : String - an identification string that has no uniqueness requirement. Eg - used for identifying WFS feature gml:id's
     * layer : portal.layer.Layer - the layer that spawned this primitive. Can be null or empty.
     * onlineResource : portal.csw.OnlineResource - the OnlineResource that spawned this primitive. Can be null or empty.
     * cswRecord : portal.csw.CSWRecord - the CSWRecord that spawned this primitive. Can be null or empty.
     */
    constructor : function(cfg) {
        this.callParent(arguments);

        this.setId(cfg.id);
        this.setLayer(cfg.layer);
        this.setOnlineResource(cfg.onlineResource);
        this.setCswRecord(cfg.cswRecord);
    }
});