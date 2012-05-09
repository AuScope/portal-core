/**
 * NVCL Logs are a representation of a log response from the NVCL data service
 */
Ext.define('portal.knownlayer.nvcl.Dataset', {
    extend: 'Ext.data.Model',

    fields : [
        {name : 'datasetId', type: 'string'},
        {name : 'datasetName', type: 'string'},
        {name : 'omUrl', type: 'string'}
    ]
});