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
        var parentRecord = cfg.parentRecord;

        //Set our default values (if they haven't been set)
        Ext.applyIf(cfg, {
            title: 'Service Information',
            autoDestroy : true,
            width : 830,
            height : 400,
            minHeight : 100
        });
        
        if(cswRecords[0].get('resourceProvider')=='kml'){
            
            Ext.apply(cfg, {
                autoScroll : true,
                html : '<p>This layer have been generated from a custom KML file</p>',
                listeners : {
                    resize : function(win, width, height) {
                        win.setSize(width, height);
                    }
                }
            });
            
        }else{
          //Set our 'always override' values
            Ext.apply(cfg, {
                autoScroll : true,
                items : [{
                    xtype : 'onlineresourcepanel',
                    cswRecords : cswRecords,
                    parentRecord : parentRecord
                }],
                listeners : {
                    resize : function(win, width, height) {
                        win.setSize(width, height);
                    }
                }
            });
        }

        

        this.callParent(arguments);
    }
});