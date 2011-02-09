/**
 * Builds a form panel for Mine filters
 * @param {number} id of this formpanel instance
 * @param {string} the service url for submit
 */
MineFilterForm = function(id) {
    /*
    var mineNamesStore = new Ext.data.Store({
        baseParams: {serviceUrl: serviceUrl},
        proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: 'getMineNames.do', timeout:180000})),
        reader: new Ext.data.JsonReader({
            root:'data'
        }, [{name:'mineDisplayName', mapping:'mineDisplayName'}])
    });*/

    this.isFormLoaded = true; //We aren't reliant on any remote downloads

    MineFilterForm.superclass.constructor.call(this, {
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
            title      : '<span qtip="Please enter the filter constraints then hit \'Apply Filter\'">' +
        				 'Mine Filter Properties' +
        				 '</span>',
            autoHeight : true,
            items      : [
            {
                anchor     : '100%',
                xtype      : 'textfield',
                fieldLabel : '<span qtip="Wildcards: \'!\' escape character; \'*\' zero or more, \'#\' just one character.">' +
                			 'Mine Name' +
                			 '</span>',
                name       : 'mineName'
            }]
        }]
    });
    //return thePanel;
};

Ext.extend(MineFilterForm, BaseFilterForm, {

});

