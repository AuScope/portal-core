/**
 * Ext.panel.Panel extensions to emulate the display of a grid panel row for a CommonBaseRecordPanel widget.
 * 
 * The grid panel was deprecated as part of AUS-2685
 */
Ext.define('portal.widgets.panel.recordpanel.RowPanel', {
    extend : 'portal.widgets.panel.recordpanel.AbstractChild',
    xtype : 'recordrowpanel',
    
    config: {
        titleIndex: 0,
        tools: null
    },
    
    /**
     * {
     *  title: String - title for this row panel
     *  titleIndex: Number - 0 based position of the title amongst all tools. (default 0)
     * 
     *  tools: Object[] - The additional tool columns, each bound to fields in the underlying data model
     *             clickHandler - function() - Called whenever this tool is clicked. No return value.
     *             doubleClickHandler - function() - Called whenever this tool is double clicked. No return value.
     *             icon - String - the icon to be displayed
     *             stopEvent - Boolean - whether the click events should be stopped propogating upwards
     * }
     */
    constructor : function(config) {
        var headerItems = [];
        Ext.each(config.tools, function(tool, index) {
            var leftMargin = '5';
            var rightMargin = '5';
            if (index == 0) {
                leftMargin = '0';
            }
            
            if (index == (config.tools.length - 1)) {
                rightMargin = '0';
            }
            
            headerItems.push({
                xtype : 'image',
                itemId: tool.itemId,
                width : 16,
                height : 16,
                margin : Ext.util.Format.format('0 {0} 0 {1}', rightMargin, leftMargin),
                src : tool.icon,
                plugins : [{
                    ptype: 'clickableimage', 
                    stopEvent: !!tool.stopEvent
                }],
                listeners : {
                    click : Ext.isEmpty(tool.clickHandler) ? Ext.emptyFn : tool.clickHandler,
                    dblclick : Ext.isEmpty(tool.doubleClickHandler) ? Ext.emptyFn : tool.doubleClickHandler
                }
            });
        });
        
        var title = config.title;
        
        delete config.tools;
        delete config.title;
        
        Ext.apply(config, {
            layout : 'fit',
            border: true,
            cls: 'recordrowpanel',
            bodyStyle: {
                'border-color': '#ededed'
            },
            margin: '0 0 0 0',
            header : {
                titlePosition : Ext.isNumber(config.titleIndex) ? config.titleIndex : 0,
                border: false,
                cls: 'recordrowpanelheader',
                style : {
                    'background-color':'white',
                    'border-color': '#ededed'
                },
                items : headerItems,
                padding: '8 0 8 0',
                height: 30,
                title: {
                    text: title,
                    margin: '0 0 0 10',
                    style: {
                        'font-size': '13px',
                        'font-weight': 'normal',
                        'color': '#000000'
                    }
                }
            }
        });
        this.callParent(arguments);
    }
});