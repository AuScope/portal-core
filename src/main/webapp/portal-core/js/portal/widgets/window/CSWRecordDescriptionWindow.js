/**
 * The CSWRecordDescriptionWindow is a class that specialises Ext.Window into displaying
 * detailed information about a list of CSWRecords
 */
Ext.define('portal.widgets.window.CSWRecordDescriptionWindow', {
    extend : 'Ext.window.Window',

    /**
     *  cfg can contain all the elements for Ext.Window along with the following additions
     *
     *  cswRecords - a CSWRecord or Array of CSWRecords - these will generate the contents of this window.
     */
    constructor : function(cfg) {
        var cswRecords = cfg.cswRecords;
        var maxHeight = 400;

        //Set our default values (if they haven't been set)
        Ext.applyIf(cfg, {
            title: 'Service Information',
            autoDestroy : true,
            width : 800,
            maxHeight : 400,
            minHeight : 100
        });

        //Set our 'always override' values
        Ext.apply(cfg, {
            autoScroll : true,
            items : [{
                xtype : 'onlineresourcepanel',
                cswRecords : cswRecords
            }],
            listeners : {
                resize : function(win, width, height) {
                    if (win.getHeight() > win.maxHeight) {
                        win.setSize(width, win.maxHeight);
                    }
                }
            }
        });

        this.callParent(arguments);
    }
});