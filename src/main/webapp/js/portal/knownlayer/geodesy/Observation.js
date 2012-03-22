/**
 * Geodesy observations represent the station observations from a geodesy WFS
 */
Ext.define('portal.knownlayer.geodesy.Observation', {
    extend: 'Ext.data.Model',

    fields : [
        {name : 'stationId', type: 'string'},
        {name : 'date', type: 'string'},
        {name : 'url', type: 'string'}
    ]
});