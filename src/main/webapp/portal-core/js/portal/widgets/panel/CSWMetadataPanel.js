/**
 * A Ext.Panel specialisation for allowing the user to browse
 * through the metadata within a single CSWRecord
 */
Ext.define('portal.widgets.panel.CSWMetadataPanel', {
    extend : 'Ext.form.Panel',
    alias : 'widget.cswmetadatapanel',

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
        var keywords = this.cswRecord.get('descriptiveKeywords');
        for (var i = 0; i < keywords.length; i++) {
            keywordsString += keywords[i];
            if (i < (keywords.length - 1)) {
                keywordsString += ', ';
            }
        }

        //Build our configuration object
        Ext.apply(cfg, {
            layout : 'fit',
            items : [{
                xtype : 'fieldset',
                items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'Source',
                    value : Ext.util.Format.format('<a target="_blank" href="{0}">Link back to registry</a>', this.cswRecord.get('recordInfoUrl'))
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Title',
                    anchor : '100%',
                    value : this.cswRecord.get('name')
                }, {
                    xtype : 'textarea',
                    fieldLabel : 'Abstract',
                    anchor : '100%',
                    value : this.cswRecord.get('description'),
                    readOnly : true
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Keywords',
                    anchor : '100%',
                    value : keywordsString
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Contact Org',
                    anchor : '100%',
                    value : this.cswRecord.get('contactOrg')
                },{
                    fieldLabel : 'Resources',
                    xtype : 'onlineresourcepanel',
                    cswRecords : this.cswRecord
                }]
            }]
        });

        this.callParent(arguments);
    }
});