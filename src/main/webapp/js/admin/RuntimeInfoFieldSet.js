/**
 * Displays information about the runtime system.
 */
Ext.define('admin.RuntimeInfoFieldSet', {
    extend : 'Ext.form.FieldSet',
    alias : 'widget.runtimeinfofieldset',

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
                labelStyle : 'font-weight:bold;'
            },
            items : [{
                xtype : 'displayfield',
                fieldLabel : 'Server Name',
                value : this._manifest.serverName
            },{
                xtype : 'displayfield',
                fieldLabel : 'Server Info',
                value : this._manifest.serverInfo
            },{
                xtype : 'displayfield',
                fieldLabel : 'Server Java Version',
                value : this._manifest.serverJavaVersion
            },{
                xtype : 'displayfield',
                fieldLabel : 'Server Java Vendor',
                value : this._manifest.serverJavaVendor
            },{
                xtype : 'displayfield',
                fieldLabel : 'Java Home',
                value : this._manifest.javaHome
            },{
                xtype : 'displayfield',
                fieldLabel : 'Server OS Architecture',
                value : this._manifest.serverOsArch
            },{
                xtype : 'displayfield',
                fieldLabel : 'Server OS Name',
                value : this._manifest.serverOsName
            },{
                xtype : 'displayfield',
                fieldLabel : 'Server OS Version',
                value : this._manifest.serverOsVersion
            }]
        });

        this.callParent(arguments);
    }
});