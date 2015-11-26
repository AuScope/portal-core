/**
 * Displays information about the build system.
 */
Ext.define('admin.BuildInfoFieldSet', {
    extend : 'Ext.form.FieldSet',
    alias : 'widget.buildinfofieldset',

    _manifest : null,

    /**
     * Accepts all the configuration options of a Ext.form.FieldSet with the following additions
     * {
     * manifest : Object - dictionary of all values from MenuController representing the compiled manifest
     * }
     */
    constructor : function(cfg) {

        this._manifest = cfg.manifest;

        Ext.apply(cfg, {
            title : 'Build Information',
            defaults : {
                labelStyle : 'font-weight:bold;'
            },
            items : [{
                xtype : 'displayfield',
                fieldLabel : 'Version',
                value : this._manifest.implementationVersion
            },{
                xtype : 'displayfield',
                fieldLabel : 'Revision',
                value : this._manifest.implementationBuild
            },{
                xtype : 'displayfield',
                fieldLabel : 'Build Date',
                value : this._manifest.buildDate
            },{
                xtype : 'displayfield',
                fieldLabel : 'Build JDK',
                value : this._manifest.buildJdk
            },{
                xtype : 'displayfield',
                fieldLabel : 'Java Vendor',
                value : this._manifest.javaVendor
            },{
                xtype : 'displayfield',
                fieldLabel : 'Built by',
                value : this._manifest.builtBy
            },{
                xtype : 'displayfield',
                fieldLabel : 'OS Name',
                value : this._manifest.osName
            },{
                xtype : 'displayfield',
                fieldLabel : 'OS Version',
                value : this._manifest.osVersion
            }]
        });

        this.callParent(arguments);
    }
});