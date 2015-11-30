/**
 * Builds a form representing an empty filter panel
 */
Ext.define('portal.layer.filterer.forms.EmptyFilterForm', {
    extend: 'portal.layer.filterer.BaseFilterForm',

    constructor : function(config) {
        Ext.apply(config, {
            html: '<p class="centeredlabel"> Filter options will be shown here for special services.</p>'
        });

        this.callParent(arguments);
    }
});

