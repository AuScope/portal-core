/**
 * Class for parsing a set of portal.csw.CSWRecord objects request/response
 * using the Querier interface
 */
Ext.define('portal.layer.querier.csw.CSWQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        this.callParent(arguments);
    },

    /**
     * See parent class for definition
     */
    query : function(queryTarget, callback) {
        var cswRecord = queryTarget.get('cswRecord');
        if (!cswRecord) {
            callback(this, [], queryTarget);
            return;
        }

        var panel = Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            autoScroll : true,
            items : [{
                xtype : 'cswmetadatapanel',
                border : false,
                cswRecord : cswRecord
            }]
        });

        callback(this, [panel], queryTarget);
    }
});