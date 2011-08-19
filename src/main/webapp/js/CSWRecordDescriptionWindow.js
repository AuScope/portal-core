
/**
 * The CSWRecordDescriptionWindow is a class that specialises Ext.Window into displaying
 * detailed information about a list of CSWRecords
 *
 *  cswRecords - a CSWRecord or Array of CSWRecords
 *  knownLayerRecord
 */
CSWRecordDescriptionWindow = function(cswRecords, knownLayerRecord) {
    CSWRecordDescriptionWindow.superclass.constructor.call(this, {
        title: 'Service Information',
        autoDestroy : true,
        width : 800,
        items : [{
            xtype : 'cswresourcesgrid',
            cswRecords : cswRecords
        }]
    });
};

/**
 * determines whether or not a particular endpoint should be included when loading
 * a layer
 */
var includeEndpoint = function(endpoints, endpoint, includeEndpoints) {
    for(var i = 0; i < endpoints.length; i++) {
        if(endpoints[i].indexOf(endpoint) >= 0) {
            return includeEndpoints;
        }
    }
    return !includeEndpoints;
};
;

Ext.extend(CSWRecordDescriptionWindow, Ext.Window, {

});