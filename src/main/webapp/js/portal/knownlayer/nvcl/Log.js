/**
 * NVCL Logs are a representation of a log response from the NVCL data service
 */
Ext.define('portal.knownlayer.nvcl.Log', {
    extend: 'Ext.data.Model',

    fields : [
        {name : 'logId', type: 'string'},
        {name : 'logName', type: 'string'},
        {name : 'sampleCount', type: 'int'}
    ]
});