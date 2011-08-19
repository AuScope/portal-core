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

        //Build our configuration object
        Ext.apply(cfg, {
            layout : 'fit',
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
            height : 450,
            title : this.cswRecord.getServiceName()
        });

        //Call parent constructor
        CSWRecordMetadataWindow.superclass.constructor.call(this, cfg);
    }
});