/**
 * A Ext.form.FormPanel specialisation for allowing the user to generate
 * a filter query for an underlying CSW based on a number of preconfigured themes.
 *
 * The filter is generated dynamically from a series of plugin components
 */
CSWThemeFilterForm = Ext.extend(Ext.form.FormPanel, {

    availableComponents : [],
    themeStore : null,
    cswServiceItemStore : null,
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

        //Load our list of CSW Items (when the form is rendered)
        this.cswServiceItemStore = new Ext.data.Store({
            proxy    : new Ext.data.HttpProxy({url: 'getCSWServices.do'}),
            reader : new Ext.data.JsonReader({
                root            : 'data',
                id              : 'id',
                successProperty : 'success',
                messageProperty : 'msg',
                fields          : [
                    'id',
                    'title',
                    'url'
                ]
            })
        });

        //Load all components
        this.availableComponents.push(CSWThemeFilter.Text);
        this.availableComponents.push(CSWThemeFilter.Keywords);
        this.availableComponents.push(CSWThemeFilter.Spatial);

        //Build our configuration
        Ext.apply(cfg, {
            hideBorders : true,
            items : [{
                xtype : 'fieldset',
                hideBorders : true,
                items : [{
                    xtype : 'combo',
                    hideBorders : true,
                    fieldLabel : 'Theme',
                    anchor : '100%',
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
                }],
                listeners : {
                    afterrender : function() {
                        cswThemeFilterForm.cswServiceItemStore.load({
                            callback : cswThemeFilterForm._updateCSWList.createDelegate(cswThemeFilterForm)
                        });
                    }
                }
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
     * Updates the list of CSW services available checkbox group (if this form is yet to be rendered this function has no effect)
     */
    _updateCSWList : function() {
        if (!this.rendered) {
            return;
        }

        //delete our checkbox group (if it exists)
        var parentFieldSet = this.findByType('fieldset')[0];
        var checkBoxItems = parentFieldSet.findByType('checkboxgroup');
        for (var i = 0; i < checkBoxItems.length; i++) {
            var cmp = checkBoxItems[i];
            var parent = cmp.ownerCt;
            parent.remove(cmp);
        }

        //Create a new checkbox group based upon the items in the cswServiceItemStore
        var checkBoxItems = [];
        for (var i = 0; i < this.cswServiceItemStore.getCount(); i++) {
            var cswServiceItemRec = this.cswServiceItemStore.getAt(i);
            checkBoxItems.push({
                boxLabel : cswServiceItemRec.get('title'),
                name : cswServiceItemRec.get('id'),
                checked : true
            });
        }
        parentFieldSet.insert(1, {
            xtype : 'checkboxgroup',
            fieldLabel : 'Registries',
            columns : 1,
            items : checkBoxItems
        });

        this.doLayout();
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
    },

    /**
     * Returns an array of objects representing the list of CSW services that the user has chosen to query
     *[{
     *  id : String - Unique ID for the service
     *  url : String - URL of the CSW
     *  title : String - descriptive title of the service
     * }]
     */
    getSelectedCSWServices : function() {
        var parentFieldSet = this.findByType('fieldset')[0];
        var checkBoxGroup = parentFieldSet.findByType('checkboxgroup')[0];

        var items = checkBoxGroup.getValue();
        var result = [];
        for (var i = 0; i < items.length; i++) {
            var id = items[i].getName();
            var rec = this.cswServiceItemStore.getById(id);
            result.push({
                id : rec.get('id'),
                title : rec.get('title'),
                url : rec.get('url')
            });
        }
        return result;
    }
});