/**
 * A Ext.Window specialisation for allowing the user to browse
 * through the metadata within a single CSWRecord in a self contained window
 */
CSWRecordMetadataWindow = Ext.extend(Ext.Window, {

    cswRecord : null,

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.Window as well as the following values
     * {
     *  cswRecord : A single CSWRecord object
     * }
     */
    constructor : function(cfg) {

        this.cswRecord = cfg.cswRecord;

        //Ext JS 3 doesn't allow us to limit autoHeight panels
        //I believe there is a 'max height' element added in Ext JS 4
        var height = undefined;
        var autoHeight = true;
        if (this.cswRecord.getOnlineResources().length > 4) {
            height = 400;
            autoHeight = false;
        }

        //Build our configuration object
        Ext.apply(cfg, {
            layout : 'auto',
            height : height,
            autoScroll : true,
            autoHeight : autoHeight,
            items : [{
                xtype : 'cswmetadatapanel',
                cswRecord : this.cswRecord,
                bodyStyle : {
                    'background-color' : '#ffffff'
                },
                hideBorders : true
            }]
        }, {
            width : 800,
            //height : 450,
            title : this.cswRecord.getServiceName()
        });

        //Call parent constructor
        CSWRecordMetadataWindow.superclass.constructor.call(this, cfg);
    }
});