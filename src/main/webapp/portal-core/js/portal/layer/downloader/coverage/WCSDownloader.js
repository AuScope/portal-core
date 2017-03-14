Ext.define('portal.layer.downloader.coverage.WCSDownloader', {
    extend: 'portal.layer.downloader.Downloader',

    constructor : function(cfg) {
        this.callParent(arguments);
    },

    downloadData : function(layer, resources, renderedFilterer, currentFilterer) {
        var wcsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WCS);
        var ftpResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.FTP);
        var ftpURL = ftpResources.length > 0 ? ftpResources[0].get('url') : '';
        this.showWCSDownload(wcsResources[0].get('url'), wcsResources[0].get('name'), layer.get('renderer').map, ftpURL);
    },// end downloadData

  //rec must be a record from the response from the describeCoverage.do handler
  //The north, south, east and west reference the EPSG:4326 latitudes/longitudes that represent the current visible section of the map
  //Alternatively pass a GLatLng bounds as the north parameter
    showWCSDownload : function (serviceUrl, layerName, map, ftpURL) {
        var currentVisibleBounds = map.getVisibleMapBounds();
        me = this;
        portal.util.Ajax.request({
            url         : 'describeCoverage.do',
            timeout     : 180000,
            params      : {
                serviceUrl      : serviceUrl,
                layerName       : layerName
            },
            //This gets called if the server returns an error
            failure     : function(message) {
                Ext.Msg.alert('Error Describing Coverage', 'ERROR: ' + message);
            },
            //This gets called if the server returned HTTP 200 (the actual response object could still be bad though)
            success : function(data, message) {
                if (data.length === 0) {
                    Ext.Msg.alert('Error Describing Coverage', 'The URL ' + serviceUrl + ' returned no parsable DescribeCoverage records');
                    return;
                }

                //We only parse the first record (as there should only be 1)
                var rec = data[0];
                var interpolationAllowed = rec.supportedInterpolations.length === 0 || rec.supportedInterpolations[0] !== 'none';

                if (!rec.temporalDomain) {
                    rec.temporalDomain = [];
                }
                if (!rec.spatialDomain) {
                    rec.spatialDomain = {envelopes : [], rectifiedGrid : null};
                }

                //Add a proper date time method to each temporal domain element
                for (var i = 0; i < rec.temporalDomain.length; i++) {
                    if (rec.temporalDomain[i].type === 'timePosition') {
                        if (rec.temporalDomain[i].timePosition.time) {
                            rec.temporalDomain[i].timePosition = new Date(rec.temporalDomain[i].timePosition);
                        }
                    } else if (rec.temporalDomain[i].type === 'timePeriod') {
                        if (rec.temporalDomain[i].beginPosition.time) {
                            rec.temporalDomain[i].beginPosition = new Date(rec.temporalDomain[i].beginPosition);
                        }
                        if (rec.temporalDomain[i].endPosition.time) {
                            rec.temporalDomain[i].endPosition = new Date(rec.temporalDomain[i].endPosition);
                        }
                    }
                }

                //Preprocess our list of strings into a list of lists
                for (var i = 0; i < rec.supportedRequestCRSs.length; i++)  {
                    rec.supportedRequestCRSs[i] = [rec.supportedRequestCRSs[i]];
                }
                for (var i = 0; i < rec.supportedResponseCRSs.length; i++) {
                    rec.supportedResponseCRSs[i] = [rec.supportedResponseCRSs[i]];
                }
                for (var i = 0; i < rec.supportedFormats.length; i++) {
                    rec.supportedFormats[i] = [rec.supportedFormats[i]];
                }

                //This list will be populate with each field set (in accordance to domains we have received)
                var fieldSetsToDisplay = [{
                    xtype   :'hidden',
                    name    :'layerName', //name of the field sent to the server
                    value   : layerName  //value of the field
                },{
                    xtype   :'hidden',
                    name    :'serviceUrl', //name of the field sent to the server
                    value   : serviceUrl  //value of the field
                }];

                //Completely disables a field set and stops its values from being selected by the "getValues" function
                //This function is recursive over fieldset objects
                var setFieldSetDisabled = function (fieldSet, disabled, depth) {
                    if (depth === undefined) {
                        depth = 0;
                    }

                    //IE workaround
                    //VT: I don't think we need this any. setting the disabling the fieldset from this level
                    // disable the checkbox associted with the fieldset as well. Can be seen from the ASTER WMS Download
                    //once bounding box is check, it cannot be unchecked anymore. disabling this works for all three browsers.
                    //if (!Ext.isIE || depth != 0) {
                    //    fieldSet.setDisabled(disabled);
                    //}

                    for (var i = 0; i < fieldSet.items.length; i++) {
                        var item = fieldSet.items.get(i);

                        if (item.getXType() == 'fieldset') {
                            setFieldSetDisabled(item, disabled, depth + 1);
                        } else {
                            item.setDisabled(disabled);
                        }
                    }
                };
                
               
                currentVisibleBounds.eastBoundLongitude = portal.util.BBox.datelineCorrection(currentVisibleBounds.eastBoundLongitude,"EPSG:4326")
                

                //Contains the fields for bbox selection
                if (rec.spatialDomain.envelopes.length > 0) {
                    fieldSetsToDisplay.push(new Ext.form.FieldSet({
                        id              : 'bboxFldSet',
                        title           : 'Bounding box constraint',
                        checkboxToggle  : true,
                        checkboxName    : 'usingBboxConstraint',
                        defaultType     : 'textfield',
                        bodyStyle       : 'padding: 0 0 0 50px',
                        listeners: {
                            expand : {
                                scope: this,
                                fn : function(panel, anim) {
                                    setFieldSetDisabled(panel, false);
                                }
                            },
                            collapse : {
                                scope: this,
                                fn : function(panel, anim) {
                                    setFieldSetDisabled(panel, true);
                                }
                            }
                        },
                        items:[{
                            id              : 'northBoundLatitude',
                            xtype           : 'numberfield',
                            fieldLabel      : 'Latitude (North)',
                            value           : currentVisibleBounds.northBoundLatitude,
                            name            : 'northBoundLatitude',
                            allowBlank      : false,
                            anchor          : '-50'
                        },{
                            id              : 'southBoundLatitude',
                            xtype           : 'numberfield',
                            fieldLabel      : 'Latitude (South)',
                            value           : currentVisibleBounds.southBoundLatitude,
                            name            : 'southBoundLatitude',
                            allowBlank      : false,
                            anchor          : '-50'
                        },{
                            id              : 'eastBoundLongitude',
                            xtype           : 'numberfield',
                            fieldLabel      : 'Longitude (East)',
                            value           : currentVisibleBounds.eastBoundLongitude,
                            name            : 'eastBoundLongitude',
                            allowBlank      : false,
                            anchor          : '-50'
                        },{
                            id              : 'westBoundLongitude',
                            xtype           : 'numberfield',
                            fieldLabel      : 'Longitude (West)',
                            value           : currentVisibleBounds.westBoundLongitude,
                            name            : 'westBoundLongitude',
                            allowBlank      : false,
                            anchor          : '-50'
                        }]
                    }));
                }

                //Contains the fields for temporal instance selection
                if (rec.temporalDomain.length > 0 && rec.temporalDomain[0].type === 'timePosition') {
                    var checkBoxList = [];

                    for (var i = 0; i < rec.temporalDomain.length; i++) {
                        checkBoxList.push({
                            boxLabel    : rec.temporalDomain[i].timePosition.format('Y-m-d H:i:s T'),
                            name        : 'timePosition',
                            inputValue  : rec.temporalDomain[i].timePosition.format('Y-m-d H:i:s T') + 'GMT'
                        });
                    }

                    fieldSetsToDisplay.push(new Ext.form.FieldSet({
                        id              : 'timePositionFldSet',
                        title           : 'Time Position Constraints',
                        checkboxToggle  : true,
                        checkboxName    : 'usingTimePositionConstraint',
                        defaultType     : 'textfield',
                        bodyStyle       : 'padding: 0 0 0 50px',
                        allowBlank      : false,
                        listeners: {
                            expand      : {
                                scope: this,
                                fn : function(panel, anim) {
                                    setFieldSetDisabled(panel, false);
                                }
                            },
                            collapse : {
                                scope: this,
                                fn : function(panel, anim) {
                                    setFieldSetDisabled(panel, true);
                                }
                            }
                        },
                        items:{
                            // Use the default, automatic layout to distribute the controls evenly
                            // across a single row
                            xtype: 'radiogroup',
                            fieldLabel: 'Time Positions',
                            columns: 1,
                            items: checkBoxList
                        }
                    }));
                }

                //Contains the fields for temporal range selection
                //This will be hidden if there is no temporalRange in the temporalDomain
                if (rec.temporalDomain.length > 0 && rec.temporalDomain[0].type === 'timePeriod') {
                    fieldSetsToDisplay.push(new Ext.form.FieldSet({
                        id              : 'timePeriodFldSet',
                        title           : 'Time Period Constraints',
                        checkboxToggle  : true,
                        checkboxName    : 'usingTimePeriodConstraint',
                        defaultType     : 'textfield',
                        bodyStyle       : 'padding: 0 0 0 50px',
                        listeners: {
                            expand      : {
                                scope: this,
                                fn : function(panel, anim) {
                                    setFieldSetDisabled(panel, false);
                                }
                            },
                            collapse : {
                                scope: this,
                                fn : function(panel, anim) {
                                    setFieldSetDisabled(panel, true);
                                }
                            }
                        },
                        items:[{
                            id              : 'dateFrom',
                            xtype           : 'datefield',
                            fieldLabel      : 'Date From',
                            name            : 'dateFrom',
                            format          : 'Y-m-d',
                            allowBlank      : false,
                            value           : rec.temporalDomain[0].beginPosition,
                            anchor          : '-50',
                            submitValue     : false         // We don't submit here as we perform some custom parsing for the query
                        },{
                            id              : 'timeFrom',
                            xtype           : 'timefield',
                            fieldLabel      : 'Time From',
                            name            : 'timeFrom',
                            format          : 'H:i:s',
                            allowBlank      : false,
                            value           : rec.temporalDomain[0].beginPosition.format('H:i:s'),
                            anchor          : '-50',
                            submitValue     : false         // We don't submit here as we perform some custom parsing for the query
                        },{
                            id              : 'dateTo',
                            xtype           : 'datefield',
                            fieldLabel      : 'Date To',
                            name            : 'dateTo',
                            format          : 'Y-m-d',
                            allowBlank      : false,
                            value           : rec.temporalDomain[0].endPosition,
                            anchor          : '-50',
                            submitValue     : false         // We don't submit here as we perform some custom parsing for the query
                        },{
                            id              : 'timeTo',
                            xtype           : 'timefield',
                            fieldLabel      : 'Time To',
                            name            : 'timeTo',
                            format          : 'H:i:s',
                            allowBlank      : false,
                            value           : rec.temporalDomain[0].endPosition.format('H:i:s'),
                            anchor          : '-50',
                            submitValue     : false         // We don't submit here as we perform some custom parsing for the query
                        }]
                    }));
                }

                //lets add our list (if any) of axis constraints that can be applied to the download
                var axisConstraints = [];
                if (rec.rangeSet.axisDescriptions && rec.rangeSet.axisDescriptions.length > 0) {
                    for (var i = 0; i < rec.rangeSet.axisDescriptions.length; i++) {
                        var constraint = rec.rangeSet.axisDescriptions[i];

                        //if this constraint has a defined set of values, lets add it
                        if (constraint.values && constraint.values.length > 0) {
                            //Only support singleValue constraints for the moment
                            if (constraint.values[0].type === 'singleValue') {
                                constraint.componentId = 'axis-constraint-' + i;
                                constraint.type = 'singleValue';
                                constraint.checkBoxName = 'axis-constraint-' + i;
                                constraint.checkBoxId = 'axis-constraint-' + i + '-chkboxgrp';

                                var checkBoxList = [];

                                for (var j = 0; j < constraint.values.length; j++) {
                                    checkBoxList.push({
                                        id          : constraint.checkBoxName + '-' + j,
                                        boxLabel    : constraint.values[j].value,
                                        name        : constraint.checkBoxName,
                                        inputValue  : constraint.values[j].value//,
                                        //submitValue : false // Can't use submitValue: false with radio's (it breaks them)
                                    });
                                }

                                fieldSetsToDisplay.push(new Ext.form.FieldSet({
                                    id              : constraint.componentId,
                                    title           : "Parameter '" + constraint.label + "' Constraints",
                                    checkboxToggle  : true,
                                    defaultType     : 'textfield',
                                    bodyStyle       : 'padding: 0 0 0 50px',
                                    allowBlank      : false,
                                    listeners: {
                                        expand      : {
                                            scope: this,
                                            fn : function(panel, anim) {
                                                setFieldSetDisabled(panel, false);
                                            }
                                        },
                                        collapse : {
                                            scope: this,
                                            fn : function(panel, anim) {
                                                setFieldSetDisabled(panel, true);
                                            }
                                        }
                                    },
                                    items: {
                                        // Use the default, automatic layout to distribute the controls evenly
                                        // across a single row
                                        id              : constraint.checkBoxId,
                                        xtype           : 'radiogroup',
                                        constraintName  : constraint.name,
                                        fieldLabel      : constraint.label,
                                        columns         : 3,
                                        items           : checkBoxList,
                                        submitValue     : false
                                    }
                                }));

                                axisConstraints.push(constraint);
                            }
                        }
                    }
                }

                fieldSetsToDisplay.push(new Ext.form.FieldSet({
                    id              : 'outputDimSpec',
                    title           : 'Output dimension specifications',
                    //defaultType     : 'radio', // each item will be a radio button
                    items           : [{
                        id              : 'radiogroup-outputDimensionType',
                        xtype            : 'radiogroup',
                        columns          : 2,
                        fieldLabel       : 'Type',
                        items           : [{
                            id           : 'radioHeightWidth',
                            boxLabel         : 'Width/Height',
                            name            : 'outputDimensionsType',
                            inputValue       : 'widthHeight',
                            checked         : true,
                            listeners       : {
                                check       : function (chkBox, checked) {
                                    var fldSet = Ext.getCmp('widthHeightFieldSet');
                                    fldSet.setVisible(checked);
                                    setFieldSetDisabled(fldSet, !checked);
                                }
                            }
                        },{
                            id              : 'radioResolution',
                            boxLabel     : 'Resolution',
                            name            : 'outputDimensionsType',
                            inputValue       : 'resolution',
                            checked         : false,
                            listeners       : {
                                check : function (chkBox, checked) {
                                    var fldSet = Ext.getCmp('resolutionFieldSet');
                                    fldSet.setVisible(checked);
                                    setFieldSetDisabled(fldSet, !checked);
                                }
                            }
                        }]
                    },{
                        id              : 'widthHeightFieldSet',
                        xtype           : 'fieldset',
                        hideLabel       : true,
                        hideBorders     : true,
                        items           : [{
                            id              : 'outputWidth',
                            xtype           : 'numberfield',
                            fieldLabel      : 'Width',
                            value           : '256',
                            name            : 'outputWidth',
                            anchor          : '-50',
                            allowBlank      : false,
                            allowDecimals   : false,
                            allowNegative   : false
                        },{
                            id              : 'outputHeight',
                            xtype           : 'numberfield',
                            fieldLabel      : 'Height',
                            value           : '256',
                            name            : 'outputHeight',
                            anchor          : '-50',
                            allowBlank      : false,
                            allowDecimals   : false,
                            allowNegative   : false
                        }]
                    },{
                        id              : 'resolutionFieldSet',
                        xtype           : 'fieldset',
                        hideLabel       : true,
                        hideBorders     : true,
                        disabled        : true,
                        hidden          : true,
                        items           : [{
                            id              : 'outputResX',
                            xtype           : 'numberfield',
                            fieldLabel      : 'X Resolution',
                            value           : '1',
                            name            : 'outputResX',
                            anchor          : '-50',
                            allowBlank      : false,
                            disabled        : true,
                            allowNegative   : false
                        },{
                            id              : 'outputResY',
                            xtype           : 'numberfield',
                            fieldLabel      : 'Y Resolution',
                            value           : '1',
                            name            : 'outputResY',
                            anchor          : '-50',
                            allowBlank      : false,
                            disabled        : true,
                            allowNegative   : false
                        }]
                    }]
                }));

                var downloadFormatStore = new Ext.data.ArrayStore({
                    fields : ['format'],
                    data   : rec.supportedFormats
                });
                var responseCRSStore = new Ext.data.ArrayStore({
                    fields : ['crs'],
                    data   : rec.supportedResponseCRSs
                });
                var requestCRSStore = new Ext.data.ArrayStore({
                    fields : ['crs'],
                    data   : rec.supportedRequestCRSs
                });

                var nativeCrsString = '';
                if (rec.nativeCRSs && rec.nativeCRSs.length > 0) {
                    for (var i = 0; i < rec.nativeCRSs.length; i++) {
                        if (nativeCrsString.length > 0) {
                            nativeCrsString += ',';
                        }
                        nativeCrsString += rec.nativeCRSs[i];
                    }
                }

                //Contains all "Global" download options
                fieldSetsToDisplay.push(new Ext.form.FieldSet({
                    id              : 'downloadOptsFldSet',
                    title           : 'Download options',
                    defaultType     : 'textfield',
                    bodyStyle       : 'padding: 0 0 0 50px',
                    items: [{
                        xtype           : 'textfield',
                        id              : 'nativeCrs',
                        name            : 'nativeCrs',
                        fieldLabel      : 'Native CRS',
                        emptyText       : 'Not specified',
                        value           : nativeCrsString,
                        disabled        : true,
                        anchor          : '-50',
                        submitValue     : false
                    },{
                        xtype           : 'combo',
                        id              : 'inputCrs',
                        name            : 'inputCrs',
                        fieldLabel      : 'Reference System',
                        labelAlign      : 'right',
                        emptyText       : '',
                        forceSelection  : true,
                        allowBlank      : false,
                        mode            : 'local',
                        store           : requestCRSStore,
                        typeAhead       : true,
                        triggerAction   : 'all',
                        displayField    : 'crs',
                        anchor          : '-50',
                        valueField      : 'crs',
                        listeners       : {
                            'render'    : function(cbx){
                                if(requestCRSStore.getTotalCount()>0){
                                    cbx.setValue(requestCRSStore.getAt(0).get('crs'));
                                }
                            }
                        }
                    },{
                        xtype           : 'combo',
                        id              : 'downloadFormat',
                        name            : 'downloadFormat',
                        fieldLabel      : 'Format',
                        labelAlign      : 'right',
                        forceSelection  : true,
                        mode            : 'local',
                        triggerAction   : 'all',
                        store           : downloadFormatStore,
                        typeAhead       : true,
                        allowBlank      : false,
                        displayField    : 'format',
                        anchor          : '-50',
                        valueField      : 'format',
                        listeners       : {
                            'render'    : function(cbx){
                                for(i = 0; i < downloadFormatStore.getTotalCount() ; i++) {
                                    if(downloadFormatStore.getAt(i).get('format')==='NetCDF3'){
                                        cbx.setValue(downloadFormatStore.getAt(i).get('format'));
                                    }
                                }
                            }
                        }
                    },{
                        xtype           : 'combo',
                        id              : 'outputCrs',
                        name            : 'outputCrs',
                        fieldLabel      : 'Output CRS',
                        labelAlign      : 'right',
                        emptyText       : '',
                        forceSelection  : true,
                        mode            : 'local',
                        triggerAction   : 'all',
                        store           : responseCRSStore,
                        typeAhead       : true,
                        displayField    : 'crs',
                        anchor          : '-50',
                        valueField      : 'crs'
                    }]
                }));

                // Dataset download window
                var win = new Ext.Window({
                    id              : 'wcsDownloadWindow',
                    border          : true,
                    layout          : 'fit',
                    resizable       : true,
                    modal           : true,
                    plain           : false,
                    buttonAlign     : 'right',
                    title           : 'Layer: '+ layerName,
                    height          : 600,
                    width           : 500,
                    items:[{
                        xtype   : 'panel',
                        layout  : 'fit',
                        autoScroll : true,
                        bodyStyle   : 'background-color: transparent;',
                        items : [{
                            // Bounding form
                            id      :'wcsDownloadFrm',
                            xtype   :'form',
                            layout  :'form',
                            frame   : false,
                            autoHeight : true,
                            autoWidth   : true,
                            axisConstraints : axisConstraints,  //This is stored here for later validation usage

                            // these are applied to columns
                            defaults:{
                                xtype: 'fieldset', layout: 'form'
                            },

                            // fieldsets
                            items   : fieldSetsToDisplay
                        }],
                        buttons:[{
                            xtype: 'button',
                            text: 'Download',
                            handler: function() {

                                if (!me.validateWCSInfoWindow()) {
                                    return;
                                }

                                var downloadUrl = './downloadWCSAsZip.do?' + me.getWCSInfoWindowDownloadParameters() +
                                    (ftpURL ? '&' + Ext.Object.toQueryString({ftpURL: ftpURL}) : '');
                                
                                portal.util.FileDownloader.downloadFile(downloadUrl);
                            }
                        }]
                    }]
                });

                win.show();
            }
        });
    },

    getWCSInfoWindowDownloadParameters : function () {
        var win = Ext.getCmp('wcsDownloadFrm');
        var params = win.getForm().getValues(true);
        var customParams = '';

        //Custom handling for time periods
        var dateFrom = Ext.getCmp('dateFrom');
        var dateTo = Ext.getCmp('dateTo');
        var timeFrom = Ext.getCmp('timeFrom');
        var timeTo = Ext.getCmp('timeTo');
        if (dateFrom && dateTo && timeFrom && timeTo) {
            if (!dateFrom.disabled && !dateTo.disabled && !timeFrom.disabled && !timeTo.disabled) {
                var dateTimeFrom = dateFrom.getValue().format('Y-m-d') + ' ' + timeFrom.getValue();
                var dateTimeTo = dateTo.getValue().format('Y-m-d') + ' ' + timeTo.getValue();

                customParams += '&timePeriodFrom=' + escape(dateTimeFrom);
                customParams += '&timePeriodTo' + escape(dateTimeTo);
            }
        }

        //Get the custom parameter constraints
        var axisConstraints = win.initialConfig.axisConstraints;
        if (axisConstraints && axisConstraints.length > 0) {
            for (var i = 0; i < axisConstraints.length; i++) {
                if (axisConstraints[i].type === 'singleValue') {
                    var checkBoxGrp = Ext.getCmp(axisConstraints[i].checkBoxId);

                    //This is for radio group selection
                    var selection = checkBoxGrp.getValue();
                    if (selection && !selection.disabled) {
                        var constraintName = checkBoxGrp.initialConfig.constraintName;
                        var constraintValue = selection.initialConfig.inputValue;

                        customParams += '&customParamValue=' + escape(constraintName + '=' + constraintValue);
                    }

                  /* This is for checkbox group selection
                  var selections = checkBoxGrp.getValue();

                  for (var j = 0; selections && j < selections.length; j++) {
                      if (!selections[j].disabled) {
                          var constraintName = checkBoxGrp.initialConfig.constraintName;
                          var constraintValue = selections[j].initialConfig.inputValue;

                          customParams += '&customParamValue=' + escape(constraintName + '=' + constraintValue);
                      }
                  }*/
                } else if (axisConstraints[i].type === 'interval') {
                    //TODO: Intervals
                }
            }
        }

        return params + customParams;
    },


    validateWCSInfoWindow : function () {
        var win = Ext.getCmp('wcsDownloadFrm');
        var form = win.getForm();
        var timePositionFieldSet = Ext.getCmp('timePositionFldSet');
        var timePeriodFieldSet = Ext.getCmp('timePeriodFldSet');
        var bboxFieldSet = Ext.getCmp('bboxFldSet');

        if (!form.isValid()) {
            Ext.Msg.alert('Invalid Fields','One or more fields are invalid');
            return false;
        }

        var usingTimePosition = timePositionFieldSet && !timePositionFieldSet.collapsed;
        var usingTimePeriod = timePeriodFieldSet && !timePeriodFieldSet.collapsed;
        var usingBbox = bboxFieldSet && !bboxFieldSet.collapsed;

        if (!usingBbox && !(usingTimePosition || usingTimePeriod)) {
            Ext.Msg.alert('No Constraints', 'You must specify at least one spatial or temporal constraint');
            return false;
        }

        if (usingTimePosition && usingTimePeriod) {
            Ext.Msg.alert('Too many temporal', 'You may only specify a single temporal constraint');
            return false;
        }

        return true;
    }
});