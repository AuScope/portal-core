Ext.define('portal.widgets.model.CSWServices', {
     extend: 'Ext.data.Model',
     fields: [
         {name: 'id', type: 'string'},
         {name: 'title',  type: 'string'},
         {name: 'url',       type: 'string'},
         {name: 'selectedByDefault',  type: 'string'}
     ]
 });