/**
 * LocatedSpecimens are a representation of observation and sampling data for the Yilgarn Laterite Geochemistry dataset
 */
Ext.define('portal.knownlayer.yilgarngeochem.LocatedSpecimen', {
    extend: 'Ext.data.Model',

    fields : [
        {name: 'analyteName', type: 'string'},
        {name: 'analyteValue', type: 'string'},
        {name: 'uom', type: 'string'},
        {name: 'analyticalMethod', type: 'string'},
        {name: 'labDetails', type: 'string'},
        {name: 'analysisDate', type: 'string'},
        {name: 'preparationDetails', type: 'string'},
        {name: 'recordIndex', type: 'int'}
    ]
});