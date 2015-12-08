/**
 * Represents information about an Icon that can be reused by
 * some aspects of the map
 */
Ext.define('portal.map.Icon', {

    statics : {
        markerOrder : 0,
        genericMarkers : [
            'http://maps.google.com/mapfiles/ms/micons/ylw-pushpin.png',
            'http://maps.google.com/mapfiles/ms/micons/blue-pushpin.png',
            'http://maps.google.com/mapfiles/ms/micons/grn-pushpin.png',
            'http://maps.google.com/mapfiles/ms/micons/ltblu-pushpin.png',
            'http://maps.google.com/mapfiles/ms/micons/pink-pushpin.png',
            'http://maps.google.com/mapfiles/ms/micons/purple-pushpin.png',
            'http://maps.google.com/mapfiles/ms/micons/red-pushpin.png'
        ],
        colorMap : {
            purple : '#9C16F0',
            orange : '#F59B0A',
            wht : '#E8DECF',
            red : '#CF082D',
            pink : '#F249A9',
            grn : '#65ED4A',
            blu : '#442BE3',
            ltblu : '#66EDD4',
            ylw : '#E8E527',
            defaultColor : '#78686C'
        },//VT: This is to generate the right wms color to match the marker icon color.
        mapIconColor : function(url){         
            for(var key in portal.map.Icon.colorMap){
                if(url.indexOf(key)!==-1){
                    return portal.map.Icon.colorMap[key];
                }
            }
        }
    },



    config : {
        /**
         * String - URL of the icon image
         */
        url : '',

        /**
         * Mark this icon has a randomly allocated icon
         */
        isDefault : false,
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
        if(cfg.url && cfg.url.length > 0){
            this.setUrl(cfg.url);
        }else{
            //VT:give this a default marker to use.
            var order=this.self.markerOrder;
            this.setUrl(this.self.genericMarkers[order]);
            if(order == 6){
                this.self.markerOrder = 0;
            }else{
                this.self.markerOrder ++;
            }
            this.setIsDefault(true);
        }
        this.setWidth(cfg.width);
        this.setHeight(cfg.height);
        this.setAnchorOffsetX(cfg.anchorOffsetX);
        this.setAnchorOffsetY(cfg.anchorOffsetY);

    }
});