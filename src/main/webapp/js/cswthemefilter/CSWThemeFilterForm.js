/**
 * A Ext.form.FormPanel specialisation for allowing the user to generate
 * a filter query for an underlying CSW based on a number of preconfigured themes.
 *
 * The filter is generated dynamically from a series of plugin components
 */
CSWThemeFilterForm = Ext.extend(Ext.form.FormPanel, {

    availableComponents : [],
    themeStore : null,
    getMapFn : null,

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.form.FormPanel as well as the following extensions
     * {
     *  getMapFn : [Required] A function which returns an instance of GMap2 that may be utilised by child components
     * }
     */
    constructor : function(cfg) {
        var cswThemeFilterForm = this;  //To maintain our scope in callbacks

        this.getMapFn = cfg.getMapFn;

        //Load our list of themes
        this.themeStore = new Ext.data.Store({
            proxy    : new Ext.data.HttpProxy({url: 'getAllCSWThemes.do'}),
            reader : new Ext.data.JsonReader({
                root            : 'data',
                id              : 'urn',
                successProperty : 'success',
                messageProperty : 'msg',
                fields          : [
                    'urn',
                    'label',
                    'indent'
                ]
            })
        });
        this.themeStore.load();

        //Load all components
        this.availableComponents.push(CSWThemeFilter.Keywords);
        this.availableComponents.push(CSWThemeFilter.Spatial);

        //Build our configuration
        Ext.apply(cfg, {
            items : [{
                xtype : 'fieldset',
                title : 'CSW Theme Filter',
                hideBorders : true,
                items : [{
                    xtype : 'combo',
                    fieldLabel : 'Theme',
                    name : 'theme',
                    store : this.themeStore,
                    forceSelection : true,
                    triggerAction : 'all',
                    typeAhead : true,
                    typeAheadDelay : 500,
                    displayField : 'label',
                    valueField : 'urn',
                    mode : 'local',
                    //This template allows us to treat 'indent' levels differently
                    tpl :  new Ext.XTemplate(
                            '<tpl for=".">',
                                '<div class="x-combo-list-item">',
                                    '<tpl if="indent==0"><b>{label}</b></tpl>',
                                    '<tpl if="indent==1">&bull; {label}</tpl>',
                                    '<tpl if="indent==2">&raquo; {label}</tpl>',
                                '</div>',
                            '</tpl>'),
                    listeners : {
                        // On selection update our list of active base components
                        select : function(combo, record, index) {
                            cswThemeFilterForm._clearBaseComponents();
                            if (record) {
                                var urn = record.get('urn');
                                for (var i = 0; i < cswThemeFilterForm.availableComponents.length; i++) {
                                    //Only add components that support the newly selected theme
                                    var cmp = new cswThemeFilterForm.availableComponents[i]({
                                        map : cswThemeFilterForm.getMapFn()
                                    });
                                    if (cmp.supportsTheme(urn)) {
                                        this.ownerCt.add(cmp);
                                    } else {
                                        cmp.destroy();
                                    }
                                }
                            }
                            cswThemeFilterForm.doLayout();
                        }
                    }
                }]
            }]
        });

        //construct our instance
        CSWThemeFilter.BaseComponent.superclass.constructor.call(this, cfg);
    },

    /**
     * Gets every BaseComponent instance that is added to this form's fieldset
     *
     * Returns an array of BaseComponent objects.
     */
    _getBaseComponents : function() {
        var parentFieldSet = this.findByType('fieldset')[0];
        return parentFieldSet.items.filterBy(function(item) {
            return item.isBaseComponent;
        });
    },

    /**
     * Deletes all BaseComponents (excluding the Theme Combo) from a
     * CSWThemeFilterForm
     */
    _clearBaseComponents : function() {
        var components = this._getBaseComponents();

        for (var i = 0; i < components.getCount(); i++) {
            var cmp = components.get(i);
            var parent = cmp.ownerCt;
            var obj = parent.remove(cmp);
        }
    },

    /**
     * Iterates through all components in this filter form and merges their
     * filter attributes into a single object which is returned
     *
     * Returns a javascript object
     */
    generateCSWFilterParameters : function() {
        var components = this._getBaseComponents();
        var filterParams = {};
        for (var i = 0; i < components.getCount(); i++) {
            var cmpFilterValues = components.get(i).getFilterValues();
            Ext.apply(filterParams, cmpFilterValues);
        }

        return filterParams;
    }
});