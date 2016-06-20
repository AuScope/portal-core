/**
 * Class for providing an download interface into an OPeNDAP service
 */
Ext.define('portal.layer.downloader.coverage.OPeNDAPDownloader', {
    extend : 'portal.layer.downloader.Downloader',

    map : null,

    /**
     * Utility function for extracting all selected parameters as a config object
     */
    _getOPeNDAPParameters : function (win) {

        //Generates parameters recursively
        //as an array of constraints
        var generateConstraints = function(component) {
            if (!component) {
                return null;
            }

            if (component.initialConfig.variableType === 'axis') {
                var fromField = component.getComponent(0);
                var toField = component.getComponent(1);

                var obj = {
                    type        : component.initialConfig.variableType,
                    name        : component.initialConfig.name
                };

                if (component.initialConfig.usingDimensionBounds) {
                    obj.dimensionBounds = {
                        from        : parseFloat(fromField.getValue()),
                        to          : parseFloat(toField.getValue())
                    };
                } else {
                    obj.valueBounds = {
                        from        : parseFloat(fromField.getValue()),
                        to          : parseFloat(toField.getValue())
                    };
                }

                return obj;
            } else if (component.initialConfig.variableType === 'grid') {
                var childAxes = [];
                for (var i = 0; i < frm.items.getCount(); i++) {
                    var child = generateConstraints(component.items.get(i));
                    if (child) {
                        childAxes.push(child);
                    }
                }
                return {
                    type        : component.initialConfig.variableType,
                    name        : component.initialConfig.name,
                    axes        : childAxes
                };
            }

            return null;
        };

        var frm = win.getComponent('bounding-form');
        var fldSet = frm.getComponent('bounding-fieldset');
        var params = {
            opendapUrl : fldSet.getComponent('url').getValue(),
            downloadFormat : fldSet.getComponent('format').getValue()
        };

        //Generate constraints component
        var variableConstraints = [];
        for (var i = 0; i < frm.items.getCount(); i++) {
            var component = frm.items.get(i);

            if(component && !component.disabled && (component.checkboxToggle !== true || component.collapsed === false)) {
                var constraint = generateConstraints(component);
                if (constraint) {
                    variableConstraints.push(constraint);
                }
            }
        }

        params.constraints = Ext.JSON.encode({
            constraints : variableConstraints
        });

        return params;
    },

    /**
     * Updates the loading status of window to the specified message. If the message is null,
     * the loading status will be hidden
     */
    _updateLoadingStatus : function(win, newMessage) {
        var loadingLabel = win.getComponent('bounding-form').getComponent('bounding-fieldset').getComponent('loading');

        if (newMessage) {
            loadingLabel.setText(newMessage);
            loadingLabel.show();
        } else {
            loadingLabel.hide();
            loadingLabel.setText('');
        }
    },

    /**
     * Recursively generates a field set for a given variable
     */
    _generateVariableFieldSet : function(variable) {
        if (variable.type === 'axis') {
            var bounds;
            var title;
            var usingDimensionBounds;

            if (variable.valueBounds) {
                bounds = variable.valueBounds;
                title = variable.name + '[' + bounds.from + ', ' + bounds.to + ']' + ' - ' + variable.units;
                if(variable.name==='lat'){
                    var currentVisibleBounds = this.map.getVisibleMapBounds();
                    bounds.fromValue = (currentVisibleBounds.southBoundLatitude > bounds.from)?currentVisibleBounds.southBoundLatitude:bounds.from ;
                    bounds.toValue = (currentVisibleBounds.northBoundLatitude < bounds.to)?currentVisibleBounds.northBoundLatitude:bounds.to;
                }else if(variable.name==='lon'){
                    var currentVisibleBounds = this.map.getVisibleMapBounds();
                    currentVisibleBounds.eastBoundLongitude = portal.util.BBox.datelineCorrection(currentVisibleBounds.eastBoundLongitude,"EPSG:4326")
                    bounds.fromValue = (currentVisibleBounds.westBoundLongitude > bounds.from)?currentVisibleBounds.westBoundLongitude:bounds.from ;
                    bounds.toValue = (currentVisibleBounds.eastBoundLongitude < bounds.to)?currentVisibleBounds.eastBoundLongitude:bounds.to;
                }
                usingDimensionBounds = false;
            } else {
                bounds = variable.dimensionBounds;
                title = variable.name + '[' + bounds.from + ', ' + bounds.to + ']';
                usingDimensionBounds = true;
            }

            return {
                xtype       : 'fieldset',
                name        : variable.name,
                variableType: variable.type,
                title       : title,
                usingDimensionBounds : usingDimensionBounds,
                items       : [{
                    xtype       : 'numberfield',
                    fieldLabel  : 'From',
                    allowBlank  : false,
                    value       : bounds.fromValue,
                    minValue    : bounds.from,
                    maxValue    : bounds.to,
                    allowDecimals : !usingDimensionBounds,
                    //.62049699996703
                    decimalPrecision : 15,
                    anchor      : '-50'
                }, {
                    xtype       : 'numberfield',
                    fieldLabel  : 'To',
                    allowBlank  : false,
                    value       : bounds.toValue,
                    minValue    : bounds.from,
                    maxValue    : bounds.to,
                    allowDecimals : !usingDimensionBounds,
                    decimalPrecision : 15,
                    anchor      : '-50'
                }]
            };
        } else if (variable.type === 'grid') {
            var items = [];
            for (var i = 0; i < variable.axes.length; i++) {
                items.push(this._generateVariableFieldSet(variable.axes[i]));
            }

            return {
                xtype           : 'fieldset',
                title           : variable.name + ' - ' + variable.units,
                name            : variable.name,
                variableType    : variable.type,
                checkboxToggle  : true,
                items           : items
                //Listeners that enabled/disabled fieldsets on expand/collapse have been
                //removed because this is not compatible with IE7. It causes the checkbox
                //itself to become disabled.
            };
        }

        throw ('Unable to parse type=' + variable.type);
    },

    /**
     * Given a list of variables, this function will add the representation of those variable constraints
     * to the specified window
     */
    _variableListToWindow : function (win, variables) {
        var frm = win.getComponent('bounding-form');

        for (var i = 0; i < variables.length; i++) {
            var variableFldSet = this._generateVariableFieldSet(variables[i]);
            frm.add(variableFldSet);
        }

        win.doLayout();
    },

    /**
     * Called when the window skeleton first opens and is rendered
     */
    _onWindowOpen : function(win, eOpts, opendapUrl, variableName) {
        //Download our variable list - this will be used to generate our form parameters
        portal.util.Ajax.request({
            url     : 'opendapGetVariables.do',
            scope : this,
            params  : {
                opendapUrl : opendapUrl,
                variableName : variableName
            },
            callback : function(success, data, message) {
                //Check for errors
                if (!success) {
                    this._updateLoadingStatus(win, 'Error: ' + message);
                    return;
                }

                //Remove loading
                this._updateLoadingStatus(win, null);

                //Update our form with the downloaded variables
                this._variableListToWindow(win, data);
            }
        });
    },

    /**
     * Overridden method, See parent class for details.
     */
    downloadData : function(layer, resources, renderedFilterer, currentFilterer) {
        this.map=layer.get('renderer').map;
        var opendapResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.OPeNDAP);
        if (opendapResources.length === 0) {
            return;
        }
        var opendapResource = opendapResources[0];  //we are only providing a download for 1 resource
        var opendapUrl = opendapResource.get('url');
        var variableName = opendapResource.get('name');

        // See whether or not there is an FTP resource:
        var ftpResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.FTP);
        var ftpURL = ftpResources.length > 0 ? ftpResources[0].get('url') : '';

        //Our objective is to build up a list of field sets
        var fieldSetsToDisplay = [];

        var formatsStore = Ext.create('Ext.data.Store', {
            fields   : ['format'],
            proxy : {
                type : 'ajax',
                url : 'opendapGetSupportedFormats.do',
                reader : {
                    type : 'array'
                }
            },
            autoLoad : true
        });

        //Create our popup with no variable fieldsets. These will be added later
        var win = Ext.create('Ext.window.Window', {
            layout : 'fit',
            modal : true,
            buttonAlign : 'right',
            title : 'OPeNDAP Download',
            height : 600,
            width : 500,
            items:[{
                // Bounding form
                xtype :'form',
                itemId : 'bounding-form',
                items : [{
                    xtype : 'fieldset',
                    itemId : 'bounding-fieldset',
                    title : 'Required Information',
                    items : [{
                        xtype : 'textfield',
                        itemId : 'url',
                        fieldLabel : 'URL',
                        value : opendapUrl,
                        name : 'opendapUrl',
                        readOnly : true,
                        anchor : '-50'
                    },{
                        xtype : 'combo',
                        itemId : 'format',
                        name : 'format',
                        fieldLabel : 'Format',
                        emptyText : '',
                        forceSelection : true,
                        allowBlank : false,
                        mode : 'local',
                        store : formatsStore,
                        typeAhead : true,
                        triggerAction : 'all',
                        displayField : 'format',
                        anchor : '-50',
                        valueField : 'format'
                    },{
                        xtype : 'label',
                        itemId : 'loading',
                        text : 'Loading...'
                    }]
                }]
            }],
            buttons:[{
                xtype : 'button',
                text : 'Download',
                iconCls : 'download',
                scope : this,
                handler: function(button) {
                    var win = button.ownerCt.ownerCt;
                    var frm = win.getComponent('bounding-form');
                    var loadingLabel = frm.getComponent('bounding-fieldset').getComponent('loading');

                    //Our form must be valid + finished loading
                    if (!frm.getForm().isValid() || !loadingLabel.isHidden()) {
                        Ext.Msg.alert('Invalid Fields','One or more fields are invalid');
                        return;
                    }

                    //POSTing JSON through the FileDownloader seems to cause some issues with FF
                    //This is our weird workaround
                    var params = this._getOPeNDAPParameters(win);

                    var url = 'opendapMakeRequest.do?constraints=' + escape(params.constraints) +
                        (ftpURL ? '&' + Ext.Object.toQueryString({ftpURL: ftpURL}) : '');

                    delete params.constraints;

                    portal.util.FileDownloader.downloadFile(url, params);
                }
            }],
            listeners : {
                afterrender : Ext.bind(this._onWindowOpen, this, [opendapUrl, variableName], true)
            }
        });

        win.show();
    }
});