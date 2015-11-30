/**
 * Represents information about a point on a single Tile from a Map
 */
Ext.define('portal.map.TileInformation', {

    config : {
        width : 0,  //Number - width of the tile in pixels
        height : 0, //Number - height of the tile in pixels
        offset : {  //Object - The point location within the tile being queried
            x : 0,  //Number - offset in x direction
            y : 0   //Number - offset in y direction
        },
        tileBounds : null //portal.util.BBox - bounds of the tile at the time of query
    },

    constructor : function(cfg) {
        this.callParent(arguments);

        this.setWidth(cfg.width);
        this.setHeight(cfg.height);
        this.setOffset(cfg.offset);
        this.setTileBounds(cfg.tileBounds);
    }
});