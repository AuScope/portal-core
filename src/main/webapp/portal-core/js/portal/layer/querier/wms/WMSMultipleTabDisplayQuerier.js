/**
 * Class for making and then parsing a WMS request/response for WMSGetFeatureRequests that may return more than on Feature Field and render each field
 * in a tab in the panel / window.
 * Example of this code use, https://github.com/GeoscienceAustralia/geoscience-portal/pull/80/files
 * This should be subclassed with implementations of a number of unimplemented methods defined here. 
 */
Ext.define('records', {
    extend : 'Ext.data.Model', 
    fields: [
             {name: 'field', type: 'string'},
             {name: 'value', type: 'string'}
    ]
});

Ext.define('portal.layer.querier.wms.WMSMultipleTabDisplayQuerier', {
    extend: 'portal.layer.querier.wms.WMSQuerier',

    constructor: function(config){
        this.callParent(arguments);
    },

    /**
     * @Override
     * See parent class for definition
     *
     * Makes a WMS request, waits for the response and then parses it passing the results to callback
     */
    query : function(queryTarget, callback) {
        var me = this;
        var proxyUrl = this.generateWmsProxyQuery(queryTarget, 'text/xml');
        var layerName = queryTarget.get('layer').get('name');
        
        // We need to prepare the data first with an array of maps.  The array is the fields (to display as a tab per field) and the maps is the data
        // in each tab.  We display it later in a Store passed to a grid.Panel
        var fieldsArray = [];
        
        Ext.Ajax.request({
            url : proxyUrl,
            timeout : 180000,
            scope : this,
            callback : function(options, success, response) {
                if (success) {
                    var xmlResponse = response.responseText;
                    var domDoc = portal.util.xml.SimpleDOM.parseStringToDOM(xmlResponse);
                    var wmsGetFeatureInfo = new OpenLayers.Format.WMSGetFeatureInfo();
                    var fields = wmsGetFeatureInfo.read_FeatureInfoResponse(domDoc);
                    //    console.log("Fields: ", fields);
                    me.populateFeatureFieldsDisplayArray(fields, fieldsArray);
                    me._drawFeatureFieldsTabs(layerName, fieldsArray);
                    callback(me, [], queryTarget);
                }else{
                    callback(this, [this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.')], queryTarget);
                }
            }
        });
    },

    _getPopulatedStore : function(fields) {
        // Stores are best way to display data in the Extjs UI
        var store = Ext.create('Ext.data.Store', {
            model : 'records'
        });
        
        var order = fields.order;
        for (var j = 0; j < order.length; j++) {
            var key = order[j];
            store.add([{field:key, value:fields[key]}]);
        }
        return store;
    }, 
    
    _fieldNameMapping : function (dataKey) {
        var fieldNameMappingMap = this.getFieldNameMappingMap();
        if (fieldNameMappingMap.hasOwnProperty(dataKey)) {
            return fieldNameMappingMap[dataKey];
        }
        return false;
    },
    
    _drawFeatureFieldsTabs : function(name, fieldsArray) {
        var win = Ext.create('Ext.Window', {
            border      : true,
            layout      : 'fit',
            resizable   : false,
            modal       : true,
            plain       : false,
            title       : name,
            constrain   : true,
            items:[{
                xtype           : 'tabpanel',
                activeItem      : 0,
                enableTabScroll : true,
                buttonAlign     : 'center',
                items           : []
            }]
        });
        var tabPanel = win.items.getAt(0);

        for (var i = 0; i < fieldsArray.length; i++) {
            var store = this._getPopulatedStore(fieldsArray[i]);
            var tabTitle = fieldsArray[i][this.getTabTitleMappedName()];


            var gridPanel = Ext.create('Ext.grid.Panel', {
                store : store,
                width : 860,
                hideHeaders : true,
                columns : [
                   {
                       text : "Feature",
                       dataIndex:"field",
                       width : 250,
                       align : "right",
                       renderer: function(value) {
                           return '<span style="font-size : 1.2 em; font-weight : bolder">'+value+'</span>';
                       }
                   },
                   {
                       text : "Value",
                       dataIndex: "value",
                       flex : true,
                       renderer: function(value) {
                           if (value.indexOf("http") == 0) {
                               return '<a href="' + value + '" target="_blank">' + value + '</a>';
                           } else {
                               return value;
                           }
                       },
                       listeners : {
                           delegate: 'div a',
                           click : function(name, title, cell, element) {
                               var link = element.innerText.trim();
                               if (link.indexOf("http") === 0 ){
                                   portal.util.GoogleAnalytic.trackevent("QueryPanelLinkClick", name, title, link);
                               }
                           },
                           args: [name, tabTitle]
                       }

                   }
               ]
            });

            tabPanel.add({
                title : tabTitle,
                items : [gridPanel]
            });
        }
        win.show();
    },

    /**
     * Define the mapping from WFS GetFeatureInfo Key to what should be displayed for it.
     * eg. 
     * var fieldNameMappingMap = {
     *   EDITION: "Edition",
     *   PUBYEAR: "Publication Year",
     *   LABEL: "Map Title"
     * };
     * return fieldNameMappingMap;
     */
    getFieldNameMappingMap : portal.util.UnimplementedFunction,
    
    /**
     * Return the mapped field name as defined in getFieldNameMappingMap() to use to title the tab
     * eg.
     * return "Map Title";
     */
    getTabTitleMappedName : portal.util.UnimplementedFunction,
    
    /**
     * Populate the passed in array featureFieldsDisplayArray with display data from featureFieldsArray which contains all 
     * the returned fields for the features that for example, were clicked upon.
     *
     * Arguments:
     *  featureFieldsArray - array of features where each feature is an object Map of the fields from WFS GetFeatureInfo
     *  featureFieldsDisplayArray - passed in empty array to populate -
     *      Array of Object Map of fields to be displayed.  Included in that map is 'order' array of the fields keys
     *          [feature1={'order':[field1, field2, ..], 'field1':scalar string, 'field2':scalar string}, feature2={'order' ...}];
     *      where each fieldX is the display name as returned in the value part of map by getFieldNameMappingMap()
     */
    populateFeatureFieldsDisplayArray : portal.util.UnimplementedFunction
});