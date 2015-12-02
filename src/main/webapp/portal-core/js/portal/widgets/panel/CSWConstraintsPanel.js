/**
 * A Ext.Panel specialisation for allowing the user to browse
 * through the constraints of a set of CSWRecords
 */
Ext.define('portal.widgets.panel.CSWConstraintsPanel', {
    extend : 'Ext.tab.Panel',
    alias : 'widget.cswconstraintspanel',

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.Panel as well as the following values
     * {
     *  cswRecords : A single CSWRecord object or an array of CSWRecord objects.
     * }
     */
    constructor : function(cfg) {

        var cswRecords = cfg.cswRecords;
        if (!Ext.isArray(cswRecords)) {
            cswRecords = [cswRecords];
        }

        var tabConfigs = [];
        for (var i = 0; i < cswRecords.length; i++) {
            if (!cswRecords[i].hasConstraints()) {
                continue;
            }

            var constraints = cswRecords[i].get('constraints');

            //Each tab lays out the constraints one by one
            var constraintsAdded = 0;
            var html = '<table style="border:0px;">';
            for (var j = 0; j < constraints.length; j++) {
                var constraint = constraints[j];
                constraint = constraint.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); //trim the string
                if (constraint.length === 0) {
                    continue;
                }

                constraintsAdded++;
                if (/^http:\/\//.test(constraint)) {
                    html += '<tr><td style="padding:5px;font-size:11px;"><a href="' + constraint + '" target="_blank">' + constraint + '</a></td></tr>';
                } else {
                    html += '<tr><td style="padding:5px;font-size:11px;">' + constraint + '</td></tr>';
                }
            }
            html += "</table>";

            if (constraintsAdded > 0) {
                tabConfigs.push({
                    title : cswRecords[i].get('name'),
                    items : [{
                        xtype : 'panel',
                        html : html
                    }]
                });
            }
        }


        if (tabConfigs.length === 0) {
            tabConfigs.push({
                title : 'No constraints',
                items : [{
                    xtype : 'label',
                    text : 'There have been no access constraints specified at the registry where this layer was sourced from. However, there may still be access constraints that this portal isn\'t aware of.'
                }]
            });
        }

        //Build our configuration object
        Ext.apply(cfg, {
            items : tabConfigs
        });

        this.callParent(arguments);

        //Hide the tab bar if we only have 1 tab
        this.on('afterrender', function(me) {
            if (tabConfigs.length <= 1) {
                me.getTabBar().hide();
            }
        });
    }
});