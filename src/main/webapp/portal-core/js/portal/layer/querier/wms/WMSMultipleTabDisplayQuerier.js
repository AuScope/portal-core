/**
 * Class for making and then parsing a WMS request/response for WMSGetFeatureRequests that may return more than on Feature Field and render each field
 * in a tab in the panel / window.
 */
Ext.define('records', {
    extend : 'Ext.data.Model', 
    fields: [
             {name: 'field', type: 'string'},
             {name: 'value', type: 'string'},
     ]
});

// Include the mapping from the data key to the field name to display.
// For this to be generic I'd move this to a sub-class.  
// Currently specific to GA GPT-88 - Geological Maps / Scanned 250K Geological Map Index  
var fieldNameMappingMap = {
        QMAPID: "1:250K Map ID",
        QMAPNAME: "1:250K Tile Name",
        EDITION: "Edition",
        PUBYEAR: "Publication Year",
        LOCN125: "View / Download Map image 125dpi",
        LOCN250: "View / Download Map image 250dpi",
        LABEL: "Map Title"
};

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
                    console.log("Fields: ", fields);
                    me._populateFieldsArray(fieldsArray, fields);
                    me._drawFieldsTabs(layerName, fieldsArray);
                }else{
                    callback(this, [this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.')], queryTarget);
                }
            }
        });
    },

    _populateFieldsArray : function(fieldsArray, fields) {
        for (var i = 0; i < fields.length; i++) {
            var record = {};
            var order = []; // Order want to retrieve field from record map
            record['order'] = order;
            fieldsArray.push(record);

            var tile = fields[i];
            
            var mapTitle = this._lookupTileData(tile, "LABEL");
            record[mapTitle[0]] = mapTitle[1];
            order.push(mapTitle[0]);

            var mapId = this._lookupTileData(tile, "QMAPID");
            record[mapId[0]] = mapId[1];
            order.push(mapId[0]);
            
            var tileName = this._lookupTileData(tile, "QMAPNAME");
            record[tileName[0]] = tileName[1];
            order.push(tileName[0]);
            
            var edition = this._lookupTileData(tile, "EDITION");
            record[edition[0]] = edition[1];
            order.push(edition[0]);
            
            var pubYear = this._lookupTileData(tile, "PUBYEAR");
            record[pubYear[0]] = pubYear[1];
            order.push(pubYear[0]);
            
            var pubAgency = "";
            if (tile.data['AGENCY1']) {
                pubAgency = tile.data['AGENCY1'];
            }
            if (tile.data['AGENCY2']) {
                if (pubAgency.size > 0) {
                    pubAgency += ", ";
                } 
                pubAgency += tile.data['AGENCY2'];
            }
            record["Publishing Agency"] = pubAgency;
            order.push("Publishing Agency");
            
            var location125 = this._lookupTileData(tile, "LOCN125");
            record[location125[0]] = location125[1];
            order.push(location125[0]);
            
            var location250 = this._lookupTileData(tile, "LOCN250");
            record[location250[0]] = location250[1];
            order.push(location250[0]);
        }
    },
    
    _lookupTileData : function(tile, fieldName) {
        return [this._fieldNameMapping(fieldName), tile.data[fieldName]];
    },
    
    _getPopulatedStore : function(fields) {
        // Stores are best way to display data in the Extjs UI
        var store = Ext.create('Ext.data.Store', {
            model : 'records',
        });
        
        var order = fields.order;
        for (var j = 0; j < order.length; j++) {
            var key = order[j];
            store.add([{field:key, value:fields[key]}]);
        }
        return store;
    }, 
    
    _fieldNameMapping : function (dataKey) {
        if (fieldNameMappingMap.hasOwnProperty(dataKey)) {
            return fieldNameMappingMap[dataKey];
        }
        return false;
    },
    
    _drawFieldsTabs : function(name, fieldsArray) {

        var me = this;
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
            
            var gridPanel = Ext.create('Ext.grid.Panel', {
                store : store,
                width : 860,
                hideHeaders : true,
                columns : [
                   {text : "Feature", dataIndex:"field", width : 250, align : "right", 
                       renderer: function(value) {
                           return '<span style="font-size : 1.2 em; font-weight : bolder">'+value+'</span>';
                       }
                   },
                   {text : "Value", dataIndex:"value", flex : true, 
                       renderer: function(value){
                           if (value.startsWith("http")) {
                               return '<a href="'+value+'" target="_blank">'+value+'</a>';
                           } else {
                               return value;
                           }
                       }
                   }
               ]
            });
     
            tabPanel.add({
                title : fieldsArray[i]['Map Title'],
                items : [gridPanel]
            });
        }
        win.show();
    }
});