/**
 * Displays information about the runtime system.
 */
Ext.ns('Admin');
Admin.RuntimeInfoFieldSet = Ext.extend(Ext.form.FieldSet, {

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
            title : 'Runtime Information',
            defaults : {
                labelStyle : 'font-weight:bold;',
            },
            items : [{
                xtype : 'label',
                fieldLabel : 'Server Name',
                text : this._manifest.serverName
            },{
                xtype : 'label',
                fieldLabel : 'Server Info',
                text : this._manifest.serverInfo
            },{
                xtype : 'label',
                fieldLabel : 'Server Java Version',
                text : this._manifest.serverJavaVersion
            },{
                xtype : 'label',
                fieldLabel : 'Server Java Vendor',
                text : this._manifest.serverJavaVendor
            },{
                xtype : 'label',
                fieldLabel : 'Java Home',
                text : this._manifest.javaHome
            },{
                xtype : 'label',
                fieldLabel : 'Server OS Architecture',
                text : this._manifest.serverOsArch
            },{
                xtype : 'label',
                fieldLabel : 'Server OS Name',
                text : this._manifest.serverOsName
            },{
                xtype : 'label',
                fieldLabel : 'Server OS Version',
                text : this._manifest.serverOsVersion
            }]
        });

        Admin.RuntimeInfoFieldSet.superclass.constructor.call(this, cfg);
    }
});

Ext.reg('runtimeinfofieldset', Admin.RuntimeInfoFieldSet);