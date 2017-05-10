/**
 * An implementation of portal.layer.legend.Legend for providing
 * simple GUI details on a CSW boxes and features added to the map
 */
Ext.define('portal.layer.legend.csw.CSWLegend', {
    extend: 'portal.layer.legend.Legend',

    iconUrl : '',
    
    polygonColorArr : [],

    /**
     * @param cfg an object in the form {
     *  iconUrl : String The URL of the marker icon for this layer
     *  polygonColor : String of the polygon colours '<fill colour>,<border colour>'
     * }
     */
    constructor : function(cfg) {
        this.iconUrl = cfg.iconUrl;
        if (cfg.polygonColor) this.polygonColorArr = cfg.polygonColor.split(',');
        this.callParent(arguments);
    },

    /**
     * Implemented function, see parent class
     */
    getLegendComponent : function(resources, filterer,response, isSld_body, callback) {
        var table = '<table><tr><td><img height="25" width="25" src="' + this.iconUrl +'"></td><td> Point</td></tr>';
        if (this.polygonColorArr.length>1) {
            table += '<tr><td><svg width="20" height="20"><rect width="20" height="20" style="fill:'+this.polygonColorArr[1]+';stroke-width:8;stroke:'+this.polygonColorArr[0]+';opacity:0.8;"/></svg></td><td> Geographical Bounding Box</td></tr>';
        }
        table += '</table>';
        
        var form = Ext.create('Ext.form.Panel',{
            title : 'CSW Resources',
            layout: 'fit',
            width: 250,
            html :  table
            });
        
        callback(this, resources, filterer, true, form); //this layer cannot generate a GUI popup
    },

    /**
     * Implemented function, see parent class
     */
    getLegendIconHtml : function(resources, filterer) {
        if (this.iconUrl && this.iconUrl.length > 0) {
            return Ext.DomHelper.markup({
                tag : 'div',
                style : 'text-align:center;',
                children : [{
                    tag : 'img',
                    width : 16,
                    height : 16,
                    align: 'CENTER',
                    src: this.iconUrl
                }]
            });
        } else {
            return '';
        }
    }
});