
/**
 * The CSWRecordDescriptionWindow is a class that specialises Ext.Window into displaying
 * detailed information about a list of CSWRecords
 *
 *  cswRecords - a CSWRecord or Array of CSWRecords
 *  knownLayerRecord
 */
CSWRecordDescriptionWindow = function(cswRecords, knownLayerRecord) {

    var onlineResourcesCount = 0;
    if (Ext.isArray(cswRecords)) {
        for (var i = 0; i < cswRecords.length; i++) {
            onlineResourcesCount += cswRecords[i].getOnlineResources().length;
        }
    } else {
        onlineResourcesCount += cswRecords.getOnlineResources().length;
    }

    //Ext JS 3 doesn't allow us to limit autoHeight panels
    //I believe there is a 'max height' element added in Ext JS 4
    var height = undefined;
    var autoHeight = true;
    if (onlineResourcesCount > 4) {
        height = 400;
        autoHeight = false;
    }


    CSWRecordDescriptionWindow.superclass.constructor.call(this, {
        title: 'Service Information',
        autoDestroy : true,
        autoHeight : autoHeight,
        autoScroll : true,
        height : height,
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