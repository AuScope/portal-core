/**
 * A plugin for an Ext.grid.Panel class that adds dynamic
 * tooltips to individual cells.
 *
 * To use this plugin, assign the following fields to each of the grid's columns
 * {
 *  hasTip : Boolean - whether this column has a tip associated with it
 *  tipRenderer : function(Object value, Ext.data.Model record, Ext.grid.Column column, Ext.tip.ToolTip tip) - should return a value to be rendered into a tool tip
 * }
 *
 * Original idea adapted from http://stackoverflow.com/questions/7539006/extjs4-set-tooltip-on-each-column-hover-in-gridpanel
 */
Ext.define('portal.widgets.grid.plugin.CellTips', {
    alias: 'plugin.celltips',

    _grid : null,

    constructor : function(cfg) {
        this.callParent(arguments);
    },

    init: function(grid) {
        this._grid = grid;
        grid.getView().on('render', this._registerTips, this);

    },

    _registerTips : function(view) {
        view.tip = Ext.create('Ext.tip.ToolTip', {
            // The overall target element.
            target: view.el,
            // Each grid row causes its own seperate show and hide.
            delegate: view.cellSelector,
            // Moving within the row should not hide the tip.
            trackMouse: true,
            // Render immediately so that tip.body can be referenced prior to the first show.
            renderTo: Ext.getBody(),
            // Allow tooltips to grow to their maximum
            maxWidth: 500,
            listeners: {
                // Change content dynamically depending on which element triggered the show.
                beforeshow: Ext.bind(this._tipRenderer, this, [view], true)
            }
        });
    },

    /**
     * Recurses through el's parents until a parent is reached that matches
     * parentElType AND has a class parentElClass
     *
     * @param el A DOM node
     * @param parentElType The string based local name of the node you are looking up
     * @param parentElClass The string class name parent must have
     */
    _findParentElWithClass : function(el, parentElType, parentElClass) {
        var parent = el.parentNode;

        while(parent) {
            if (portal.util.xml.SimpleDOM.getNodeLocalName(parent) === 'tr') {
                var classList = portal.util.xml.SimpleDOM.getClassList(parent);
                for (var i = 0; i < classList.length; i++) {
                    if (classList[i] === parentElClass) {
                        return parent;
                    }
                }
            }

            parent = parent.parentNode;
        }

        return null;
    },

    _tipRenderer : function(tip, opt, view) {
        //Firstly we lookup the parent column
        var gridColums = view.getGridColumns();
        var colIndex = tip.triggerElement.cellIndex;
        var column = gridColums[tip.triggerElement.cellIndex];

        if (!column || !column.hasTip) {
            return false;
        }

        //Next we iterate through our parent nodes until we hit the containing tr
        var parent = this._findParentElWithClass(tip.triggerElement, 'tr', Ext.baseCSSPrefix + 'grid-row');
        if (!parent) {
            return false;
        }

        //Finally we pass along the 'useful' information to the tipRenderer
        var record = view.getRecord(parent);
        var value = record.get(column.dataIndex);
        if (column.tipRenderer) {
            tip.update(column.tipRenderer(value, record, column, tip));
        } else {
            tip.update(value);
        }

        return true;
     }

});