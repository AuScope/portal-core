/**
 * A basic window to show a permanent link to the current map state.
 *
 * It consists of a simple warning and the link itself
 *
 */
Ext.define('portal.widgets.window.PermanentLinkWindow', {
    extend : 'Ext.window.Window',

    /**
     * Extends Ext.window.Window and adds {
     *  state : String - state string to be encoded into the permalink
     * }
     */
    constructor : function(cfg) {
        var mapStateSerializer = cfg.mapStateSerializer;

        //Rewrite our current URL with the new state info (leave the other URL params intact)
        var urlParams = Ext.Object.fromQueryString(location.search.substring(1));
        urlParams.state = cfg.state;
        var linkedUrl = location.href.split('?')[0];

        linkedUrl = Ext.urlAppend(linkedUrl, Ext.Object.toQueryString(urlParams));

        var htmlDescription = '<p><b>Warning:</b></p>' +
                              '<p>This link will only save your selected layers and queries. The actual data received and displayed may be subject to change</p></br>';

        //If the URL gets too long it may not work with some common browsers or web servers
        if (linkedUrl.length > 8192) {
            htmlDescription += '<p><b>Note: </b>This permanent link is very long and will be unuseable with the Internet Explorer web browser. It may also cause problems for various web servers so it is recommended you test your permanent link before saving/sharing it.</p>';
        } else if (linkedUrl.length > 2047) {
            htmlDescription += '<p><b>Note: </b>This permanent link is rather long and will be unuseable with the Internet Explorer web browser.</p>';
        }

        Ext.apply(cfg, {
            title: 'Permanent Link',
            autoDestroy : true,
            modal : true,
            width : 500,
            autoHeight : true,
            items : [{
                xtype : 'panel',
                style : {
                    font : '12px tahoma,arial,helvetica,sans-serif'
                },
                html : htmlDescription
            }, {
                xtype : 'form',
                layout : 'form',
                autoHeight : true,
                items : [{
                    xtype : 'textfield',
                    anchor : '100%',
                    fieldLabel : 'Paste this link',
                    labelStyle: 'font-weight:bold;',
                    value : linkedUrl,
                    readOnly : true,
                    listeners : {
                        afterrender : function(textField) {
                            textField.focus(true, 100);
                        }
                    }
                }]
            }]

        });

        this.callParent(arguments);
    }
});