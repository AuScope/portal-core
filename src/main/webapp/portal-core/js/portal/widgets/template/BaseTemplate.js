/**
 * The base template that all other ScriptBuilder templates should inherit from.
 *
 * A script builder template utilitiy for setting variables. These variables
 * will be coupled with a script template in order to generate an actual text file.
 */
Ext.define('portal.widgets.template.BaseTemplate', {
    extend : 'portal.util.ObservableMap',

    statics : {
        /**
         * The templating operation succeeded
         */
        TEMPLATE_RESULT_SUCCESS : 0,
        /**
         * The templating operation was cancelled by the user
         */
        TEMPLATE_RESULT_CANCELLED : 1,
        /**
         * The templating operation failed and the user should be warned
         */
        TEMPLATE_RESULT_ERROR : 2
    },



    constructor : function(config) {
        this.callParent(arguments);
    },

    /**
     * Utility for getting the templated script AFTER showing a popup GUI where the user can
     * enter in additional parameters.
     *
     * callback(Number status, String script) - called by the template when a script snippet has finished templating.
     * templateName - the name of the template to use
     * formPanel - and Ext.form.Panel or equivalent constructor object that will be shown in the popup window
     * includeEmptyText - Whether the form field's empty text will be included in the template (defaults to false)
     */
    _getTemplatedScriptGui : function(callback, panelStore, controllerUrl, formPanel, includeEmptyText) {
        var popup = Ext.create('Ext.window.Window', {
            title : 'Enter Parameters',
            layout : 'fit',
            modal : true,
            width : (formPanel.width ? formPanel.width : 500),
            height : (formPanel.height ? formPanel.height : 400),
            items : [formPanel],
            buttons:[{
                xtype: 'button',
                text: 'Search',
                scope : this,
                iconCls : 'add',
                handler: function(button) {
                    var parent = button.findParentByType('window');
                    var panel = parent.getComponent(0);

                    if (panel.getForm().isValid()) {
                        var additionalParams = panel.getForm().getValues(false, false, includeEmptyText, false);

                        //We need to close our window when finished so we wrap callback
                        //with a function that ensures closing BEFORE the callback is executed
                        this._getTemplatedScript(function(status,panelStore, script) {
                            parent.ignoreCloseEvent = true;
                            parent.close();
                            callback(status,panelStore, script);
                        }, panelStore,controllerUrl, additionalParams);
                    }
                }
            }],
            listeners : {
                close : function(popup) {
                    if (!popup.ignoreCloseEvent) {
                        callback(portal.widgets.template.BaseTemplate.TEMPLATE_RESULT_CANCELLED,panelStore, null);
                    }
                }
            }
        });

        popup.show();
    },

    /**
     * Utility for calling a template function getTemplatedScript.do
     *
     * The keys from getValues and baseTemplateVariables will be used to populate
     * the keys/values list for the template
     *
     * callback(Number status, String script) - called by the template when a script snippet has finished templating.
     * additionalParams - a regular object containing key/value pairs to inject into the specified template
     * templateName - the name of the template to use
     */
    _getTemplatedScript : function(callback,panelStore, controllerUrl, additionalParams) {
        //Convert our keys/values into a form the controller can read
        var keys = [];
        var values = [];
        //Utility function
        var denormaliseKvp = function(keyList, valueList, kvpObj) {
            if (kvpObj) {
                for (key in kvpObj) {
                    if(kvpObj[key] && kvpObj[key].length>0){
                        keyList.push(key);
                        valueList.push(kvpObj[key]);
                    }
                }
            }
        };

        denormaliseKvp(keys, values, this.getParameters());
        denormaliseKvp(keys, values, additionalParams);

      //Create our CSWRecord store (holds all CSWRecords not mapped by known layers)
        var filterCSWStore = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            pageSize: 15,
            autoLoad: false,
            proxy : {
                type : 'ajax',
                url : controllerUrl,
                reader : {
                    type : 'json',
                    root : 'data',
                    successProperty: 'success',
                    totalProperty: 'totalResults'
                },
                extraParams: {
                    key : keys,
                    value : values
                }

            }

        });


        callback(portal.widgets.template.BaseTemplate.TEMPLATE_RESULT_SUCCESS,panelStore, filterCSWStore);

    },

    /**
     * Function for generating a script snippet (string) representing this components values
     *
     * function(Function callback) - returns void
     *
     * callback(Number status, String script) - called by the template when a script snippet has finished templating.
     */
    requestScript : portal.util.UnimplementedFunction
});

