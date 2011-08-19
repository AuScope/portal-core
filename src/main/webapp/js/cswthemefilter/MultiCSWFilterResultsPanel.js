/**
 * A Ext.TabPanel specialisation for allowing the user to browse
 * a multiple CSWFilterResultsPanel tabs
 *
 * The filter is designed to be generated from a CSWThemeFilterForm
 */
MultiCSWFilterResultsPanel = Ext.extend(Ext.TabPanel, {

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.TabPanel as well as the following values
     * {
     *  filterParams : An object containing filter parameters (generate this from a CSWThemeFilterForm)
     *  cswServiceItems : An array of objects with 'title' and 'id' being set to the details of a CSW service
     *  map : An instance of a GMap object
     * }
     */
    constructor : function(cfg) {
        var multiCSWFilterResultsPanel = this;

        this.map = cfg.map;

        //Build a result browser for each CSW
        var resultBrowsers = [];
        for (var i = 0; i < cfg.cswServiceItems.length; i++) {
            var newParams = clone(cfg.filterParams);
            newParams.cswServiceId = cfg.cswServiceItems[i].id;

            resultBrowsers.push({
                xtype : 'cswresultspanel',
                title : cfg.cswServiceItems[i].title,
                layout : 'fit',
                map : this.map,
                filterParams : newParams
            });
        }

        //Build our configuration object
        Ext.apply(cfg, {
            autoScroll : true,
            activeTab : 0,
            items : resultBrowsers
        });

        //Call parent constructor
        MultiCSWFilterResultsPanel.superclass.constructor.call(this, cfg);
    },

    /**
     * Returns a (possibly empty) Array of CSWRecord objects representing the
     * selected records
     */
    getSelectedCSWRecords : function() {
        var activeTab = this.getActiveTab();
        if (activeTab == null) {
            return [];
        }

        return activeTab.getSelectedCSWRecords();
    }
});

Ext.reg('multicswresultspanel', MultiCSWFilterResultsPanel);