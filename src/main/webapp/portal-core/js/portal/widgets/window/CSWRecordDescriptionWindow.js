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
     *  parentRecord - the parent layer
     *  onlineResourcePanelType A specific subclass of online resource panel to use
     */
    constructor : function(cfg) {
        var cswRecords = cfg.cswRecords;
        var parentRecord = cfg.parentRecord;
        var onlineResourcePanelType = cfg.onlineResourcePanelType || 'onlineresourcepanel';        

        //Set our default values (if they haven't been set)
        Ext.applyIf(cfg, {
            title: 'Service Information',
            autoDestroy : true,
            width : 830,
            maxHeight : 400,
            minHeight : 100,
            modal : true
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
                    xtype : onlineResourcePanelType,
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
