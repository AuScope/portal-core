Ext.define('portal.widgets.model.CustomRegistryModel', {
     extend: 'Ext.data.Model',

     fields: [
         {name: 'id', type: 'string'},
         {name: 'title',  type: 'string'},
         {name: 'serviceUrl',       type: 'string'},
         {name: 'recordInformationUrl',  type: 'string'},
         {name: 'active',  type: 'boolean'}
     ]

 });