/**
 * A Panel specialisation for allowing the user to browse and execute
 * a set of DiagnosticFunction objects
 */
Ext.define('admin.DiagnosticFunctionsPanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.diagnosticfunctionpanel',

    diagnosticFunctions : null, //Array of admin.DiagnosticFunction objects

    /**
     * Accepts all Ext.grid.Panel options as well as
     * {
     *  diagnosticFunctions : single instance or an array of admin.DiagnosticFunction objects
     * }
     */
    constructor : function(cfg) {
        if (Ext.isArray(cfg.diagnosticFunctions)) {
            this.diagnosticFunctions = cfg.diagnosticFunctions;
        } else {
            this.diagnosticFunctions = [cfg.diagnosticFunctions];
        }

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
        });

        //Build our configuration object
        Ext.apply(cfg, {
            features : [groupingFeature],
            store : Ext.create('Ext.data.Store', {
                groupField : 'group',
                model : 'admin.DiagnosticFunction',
                data : this.diagnosticFunctions
            }),
            plugins : [{
                ptype : 'selectablegrid'
            }],
            hideHeaders : true,
            columns: [{
                //Title column
                dataIndex: 'name',
                menuDisabled: true,
                sortable: true,
                flex: 1,
                renderer: Ext.bind(this._titleRenderer, this)
            },{
                dataIndex: 'executeFn',
                width: 140,
                renderer : Ext.bind(this._executeRenderer, this)
            }]
        });

        this.callParent(arguments);
    },

    _titleRenderer : function(value, metaData, record, row, col, store, gridView) {
        var name = record.get('name');
        var description = record.get('description');

        //Render our HTML
        return Ext.DomHelper.markup({
            tag : 'div',
            children : [{
                tag : 'b',
                html : name
            },{
                tag : 'br'
            },{
                tag : 'span',
                style : {
                    color : '#555'
                },
                children : [{
                    html : description
                }]
            }]
        });
    },

    _executeRenderer : function(value, metaData, record, row, col, store, gridView) {
        var name = record.get('name');
        var executeFn = record.get('executeFn');

        var id = Ext.id();
        Ext.Function.defer(this._createGridButton, 1, this, [name, executeFn, id, record]);
        return Ext.DomHelper.markup({
            tag : 'div',
            id : id
        });
    },

    _createGridButton : function(text, executeFn, id, record) {
        new Ext.Button({
            text: text,
            scope : this,
            handler : function(btn, e) {
                var mask = new Ext.LoadMask({
                    msg : 'Performing function. Please be patient.',
                    target : this
                    });
                mask.show();
                executeFn(function(success, msg) {
                    mask.hide();
                    if (!msg || msg.length === 0) {
                        msg  = 'No message returned.';
                    }
                    Ext.MessageBox.alert(success ? 'Success!' : 'Failure',  'Message: ' + msg);
                });
            }
        }).render(document.body, id);
    }
});
