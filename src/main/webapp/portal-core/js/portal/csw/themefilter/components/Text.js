Ext.namespace("CSWThemeFilter");

/**
 * An extension of CSWThemeFilter.BaseComponent to allow the filtering of 'AnyText'
 */
CSWThemeFilter.Text = Ext.extend(CSWThemeFilter.BaseComponent, {

    constructor : function(cfg) {

        //Build our configuration
        Ext.apply(cfg, {
            title : 'Text',
            collapsible : true,
            border : false,
            items : [{
                xtype : 'textfield',
                fieldLabel : '<span qtip="The query accepts the special characters: \'*\' match zero or more, \'#\' match just one character, \'!\' escape character">Match any text</span>',
                anchor : '100%'
            }]
        });

        //Construct our instance
        CSWThemeFilter.Text.superclass.constructor.call(this, cfg);
    },

    /**
     * Returns the selected query text
     */
    getFilterValues : function() {
        var anyText = this.items.itemAt(0).getValue();
        return {
            anyText : anyText
        };
    },

    /**
     * The Text component supports all URN's
     */
    supportsTheme : function(urn) {
        return true;
    }
});
