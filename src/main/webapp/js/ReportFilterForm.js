/**
 * Builds a form panel for Report filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */
ReportFilterForm = function(id, serviceUrl) {
    
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
            }
            ]
        }]
    });

};

ReportFilterForm.prototype = new Ext.FormPanel();
