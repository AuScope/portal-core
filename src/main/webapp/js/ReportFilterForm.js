/**
 * Builds a form panel for Report filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */
ReportFilterForm = function(id, knownLayerRecord, cswRecordStore) {
    
	var keywords = knownLayerRecord.getLinkedCSWRecordsKeywordCount(cswRecordStore, false);
		
    var keywordStore = new Ext.data.SimpleStore({
        fields   : ['keyword', 'count'],
        sortInfo : {field:'keyword',order:'ASC'},
        data: keywords,
        reader : new Ext.data.ArrayReader({}, [
            { name:'keyword' },
            { name:'count' }
        ])
    });
    
    Ext.QuickTips.init();
    
    var keywordCombo = new Ext.form.ComboBox({
        tpl: '<tpl for="."><div style="word-wrap" ext:qtip="{keyword} - {count} record(s)" class="x-combo-list-item">{keyword}</div></tpl>',
        anchor         : '100%',
        name           : 'keyword',
        hiddenName     : 'keyword',  
        fieldLabel     : 'Keyword',
        labelAlign     : 'right',
        forceSelection : true,
        mode           : 'local',
        store          : keywordStore,
        triggerAction  : 'all',
        typeAhead      : true,
        displayField   :'keyword',
        valueField     :'keyword',
        autoScroll	   : true
    });
	
    Ext.FormPanel.call(this, {
        id          : String.format('{0}',id),
        border      : false,
        autoScroll  : true,
        hideMode    :'offsets',
        width       :'100%',
        buttonAlign :'right',
        labelAlign  :'right',
        labelWidth  : 70,
        timeout     : 180, //should not time out before the server does
        bodyStyle   :'padding:5px',
        autoHeight: true,
        items       : [{
            xtype      :'fieldset',
            title      : 'Report Filter Properties',
            autoHeight : true,
            items      : [
            {
                anchor     : '100%',
                xtype      : 'textfield',
                fieldLabel : 'Title',
                name       : 'title'
            },
            keywordCombo
            ]
        }]
    });

};

ReportFilterForm.prototype = new Ext.FormPanel();
