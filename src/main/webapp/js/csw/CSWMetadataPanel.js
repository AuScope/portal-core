/**
 * A Ext.Panel specialisation for allowing the user to browse
 * through the metadata within a single CSWRecord
 */
CSWMetadataPanel = Ext.extend(Ext.Panel, {

    cswRecord : null,

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.Panel as well as the following values
     * {
     *  cswRecord : A single CSWRecord object
     * }
     */
    constructor : function(cfg) {

        this.cswRecord = cfg.cswRecord;

        var keywordsString = "";
        var keywords = this.cswRecord.getDescriptiveKeywords();
        for (var i = 0; i < keywords.length; i++) {
            keywordsString += keywords[i];
            if (i < (keywords.length - 1)) {
                keywordsString += ', ';
            }
        }

        //Build our configuration object
        Ext.apply(cfg, {
            layout : 'auto',
            autoHeight : true,
            items : [{
                xtype : 'fieldset',
                hideBorders : true,
                autoHeight : true,
                region : 'north',
                items : [{
                    xtype : 'linklabel',
                    fieldLabel : 'Source',
                    qtip : 'This is a link back to the registry of origin for this record.',
                    href : this.cswRecord.getRecordInfoUrl(),
                    text : 'Link back to registry'
                },{
                    xtype : 'label',
                    fieldLabel : 'Title',
                    anchor : '100%',
                    text : this.cswRecord.getServiceName()
                }, {
                    xtype : 'textarea',
                    fieldLabel : 'Abstract',
                    anchor : '100%',
                    value : this.cswRecord.getDataIdentificationAbstract(),
                    readOnly : true
                },{
                    xtype : 'label',
                    fieldLabel : 'Keywords',
                    anchor : '100%',
                    text : keywordsString
                },{
                    xtype : 'label',
                    fieldLabel : 'Contact Org',
                    anchor : '100%',
                    text : this.cswRecord.getContactOrganisation()
                },{
                    fieldLabel : 'Resources',
                    region : 'center',
                    xtype : 'cswresourcesgrid',
                    cswRecords : this.cswRecord
                }]
            }]
        });

        //Call parent constructor
        CSWMetadataPanel.superclass.constructor.call(this, cfg);
    }
});

Ext.reg('cswmetadatapanel', CSWMetadataPanel);