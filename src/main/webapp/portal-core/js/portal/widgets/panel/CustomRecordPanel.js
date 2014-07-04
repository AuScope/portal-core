/**
 * A specialisation of portal.widgets.panel.CSWRecordPanel for rendering
 * records that are loaded directly from an external WMS source
 */
Ext.define('portal.widgets.panel.CustomRecordPanel', {
    extend : 'portal.widgets.panel.CSWRecordPanel',

    constructor : function(cfg) {
        this.callParent(arguments);

        this.on('afterrender', this._loadQueryBar, this);
    },

    _loadQueryBar : function() {
        this._updateSearchBar(false);
        this.addDocked({
            xtype: 'toolbar',
            dock: 'top',
            items: [{
                xtype : 'label',
                text : 'Enter WMS Url:'
            },{
                xtype : 'searchfield',
                store : this.getStore(),
                width : 243,
                name : 'STTField',
                paramName: 'service_URL',
                emptyText : 'http://'
            }]
        });
    }

});