/**
 * The CSWRecordDescriptionWindow is a class that specialises Ext.Window into displaying
 * detailed information about a list of CSWRecords
 */
CSWRecordDescriptionWindow = Ext.extend(Ext.Window, {
    /**
     *  cfg can contain all the elements for Ext.Window along with the following additions
     *
     *  cswRecords - a CSWRecord or Array of CSWRecords - these will generate the contents of this window.
     *  knownLayer - [Optionally] A KnownLayerRecord responsible for CSWRecords
     */
    constructor : function(cfg) {
        var cswRecords = cfg.cswRecords;

        var onlineResourcesCount = 0;
        if (Ext.isArray(cswRecords)) {
            for (var i = 0; i < cswRecords.length; i++) {
                onlineResourcesCount += this._getValidResources(cswRecords[i]).length;
            }
        } else {
            onlineResourcesCount += this._getValidResources(cswRecords).length;
        }

        //Ext JS 3 doesn't allow us to limit autoHeight panels
        //I believe there is a 'max height' element added in Ext JS 4
        var height = undefined;
        var autoHeight = true;
        if (onlineResourcesCount > 4) {
            height = 400;
            autoHeight = false;
        }

        //Set our default values (if they haven't been set)
        Ext.applyIf(cfg, {
            title: 'Service Information',
            autoDestroy : true,
            autoHeight : autoHeight,
            autoScroll : true,
            height : height,
            width : 800,
        })

        //Set our 'always override' values
        Ext.apply(cfg, {
            items : [{
                xtype : 'cswresourcesgrid',
                cswRecords : cswRecords
            }]
        });

        CSWRecordDescriptionWindow.superclass.constructor.call(this, cfg);
    },

    /**
     * Returns an Array of CSWOnlineResource objects in cswRecord that will be visualised in the popup
     */
    _getValidResources : function(cswRecord) {
        var valid = [];
        var resources = cswRecord.getOnlineResources();


        for (var i = 0; i < resources.length; i++) {
            if (resources[i].onlineResourceType !== 'Unsupported') {
                valid.push(resources[i]);
            }
        }

        return valid;
    }
});