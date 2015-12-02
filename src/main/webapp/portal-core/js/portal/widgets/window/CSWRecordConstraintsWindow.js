/**
 * A window specialisation that is designed for showing copyright information
 * for one or more CSWRecords
 */
Ext.define('portal.widgets.window.CSWRecordConstraintsWindow', {
    extend : 'Ext.window.Window',

    /**
     *  cfg can contain all the elements for Ext.Window along with the following additions
     *
     *  cswRecords - a CSWRecord or Array of CSWRecords - these will generate the contents of this window.
     */
    constructor : function(cfg) {
        var cswRecords = cfg.cswRecords;

        //Set our default values (if they haven't been set)
        Ext.applyIf(cfg, {
            title: 'Copyright Information',
            autoDestroy : true,
            width       : 500,
            autoHeight : true,
            modal : true
        });

        //Set our 'always override' values
        Ext.apply(cfg, {
            autoScroll : true,
            layout : 'fit',
            items : [{
                xtype : 'cswconstraintspanel',
                autoScroll  : true,
                cswRecords : cswRecords
            }]
        });

        this.callParent(arguments);
    }
});