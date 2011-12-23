/**
 * Displays information about the build system.
 */
Ext.ns('Admin');
Admin.BuildInfoFieldSet = Ext.extend(Ext.form.FieldSet, {

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
                labelStyle : 'font-weight:bold;',
            },
            items : [{
                xtype : 'label',
                fieldLabel : 'Version',
                text : this._manifest.implementationVersion
            },{
                xtype : 'label',
                fieldLabel : 'Revision',
                text : this._manifest.implementationBuild
            },{
                xtype : 'label',
                fieldLabel : 'Build Date',
                text : this._manifest.buildDate
            },{
                xtype : 'label',
                fieldLabel : 'Build JDK',
                text : this._manifest.buildJdk
            },{
                xtype : 'label',
                fieldLabel : 'Java Vendor',
                text : this._manifest.javaVendor
            },{
                xtype : 'label',
                fieldLabel : 'Built by',
                text : this._manifest.builtBy
            },{
                xtype : 'label',
                fieldLabel : 'OS Name',
                text : this._manifest.osName
            },{
                xtype : 'label',
                fieldLabel : 'OS Version',
                text : this._manifest.osVersion
            }]
        });

        Admin.BuildInfoFieldSet.superclass.constructor.call(this, cfg);
    }
});

Ext.reg('buildinfofieldset', Admin.BuildInfoFieldSet);