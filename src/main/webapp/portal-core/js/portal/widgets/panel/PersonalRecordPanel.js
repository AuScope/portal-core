/**
 * A specialisation of portal.widgets.panel.CSWRecordPanel for rendering
 * records that are loaded directly from an external WMS source
 */
Ext.define('portal.widgets.panel.PersonalRecordPanel', {
    extend : 'portal.widgets.panel.KnownLayerPanel',

    constructor : function(cfg) {
        this.callParent(arguments);

        this.on('activate', this._activateButtons, this);
        this.on('beforedeactivate', this._deactivateButtons, this);
    },

    _activateButtons : function() {
        this._personalTabActive(true);
    },

    _deactivateButtons : function() {
        this._personalTabActive(false);
    }

});