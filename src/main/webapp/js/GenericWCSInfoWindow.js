/** 
* @fileoverview This file declares the Class GenericWCSInfoWindow.
* It will represent a popup information window for a single WCS layer 
*/

/**
 * Creates a new instance of GenericWCSInfoWindow
 * 
 * @param overlay The overlay that you want to open the window on
 * @param serviceUrl The URL of the remote service that will serve up WCS data
 * @param layerName The name of the layer (which will be used in all requests to serviceUrl). 
 */
function GenericWCSInfoWindow (map, overlay, serviceUrl, layerName, defaultBboxList) {
    this.map = map;
    this.overlay = overlay;
    this.serviceUrl = serviceUrl;
    this.layerName = layerName;
    this.defaultBboxList = defaultBboxList;
}

//Instance variables
GenericWCSInfoWindow.prototype.map = null;
GenericWCSInfoWindow.prototype.overlay = null;
GenericWCSInfoWindow.prototype.serviceUrl = null;
GenericWCSInfoWindow.prototype.layerName = null;
GenericWCSInfoWindow.prototype.defaultBboxList = null;



//Functions that can be accessed globally
//This function will validate each of the field sets individually (as some of them are optional in certain situations).
//If they are valid, true will be returned, if they are invalid false will be returned
//A modal error message box will be shown if the fields are invalid.
function validateWCSInfoWindow() {
	var form = Ext.getCmp('wcsDownloadFrm').getForm();
	var timeFieldSet = Ext.getCmp('timeFldSet');
	var bboxFieldSet = Ext.getCmp('bboxFldSet');

	// Time field constraints are optional
	if (!timeFieldSet.collapsed) {
		var date = Ext.getCmp('date');
		var time = Ext.getCmp('time');

		if (!date.isValid() || !time.isValid()) {
			Ext.Msg
					.alert('Invalid Date/Time',
							'You must enter a valid date \'YYYY-MM-DD\' and time \'HH:MM:SS\'');
			return false;
		}
	}

	// bbox field constraints are optional
	if (!bboxFieldSet.collapsed) {
		var north = Ext.getCmp('northBoundLatitude');
		var south = Ext.getCmp('southBoundLatitude');
		var east = Ext.getCmp('eastBoundLongitude');
		var west = Ext.getCmp('westBoundLongitude');

		if (!north.isValid() || !south.isValid() || !east.isValid()
				|| !west.isValid()) {
			Ext.Msg.alert('Invalid Bounding Box',
					'You must enter valid bounding box constraints');
			return false;
		}
	}

	// Validate our output dimensions
	var outputResX = Ext.getCmp('outputResX');
	var outputResY = Ext.getCmp('outputResY');
	var outputWidth = Ext.getCmp('outputWidth');
	var outputHeight = Ext.getCmp('outputHeight');

	if (outputResX && outputResY) {
		if (!outputResX.isValid() || !outputResY.isValid()) {
			Ext.Msg.alert('Invalid Dimensions',
					'You must enter a valid numerical output resolution');
			return false;
		}

	} else if (outputWidth && outputHeight) {
		if (!outputWidth.isValid() || !outputHeight.isValid()) {
			Ext.Msg.alert('Invalid Dimensions',
					'You must enter a valid numerical output resolution');
			return false;
		}
	} else {
		Ext.Msg.alert('Invalid Fields', 'Missing output dimension fields');
		return false;
	}

	return true;
};

function showWCSDownload(serviceUrl, layerName, defaultWestBoundLongitude, defaultEastBoundLongitude, defaultSouthBoundLatitude, defaultNorthBoundLatitude) {
    
    if (!defaultWestBoundLongitude)
        defaultWestBoundLongitude = -180;
    if (!defaultEastBoundLongitude)
        defaultEastBoundLongitude = 180;
    if (!defaultSouthBoundLatitude)
        defaultSouthBoundLatitude = -90;
    if (!defaultNorthBoundLatitude)
        defaultNorthBoundLatitude = 90;
    
    //Completely disables a field set and stops its values from being selected by the "getValues" function
    //This function is recursive over fieldset objects
    var setFieldSetDisabled = function (fieldSet, disabled) {
    	fieldSet.setDisabled(disabled);
    	
    	for (var i = 0; i < fieldSet.items.length; i++) {
    		var item = fieldSet.items.get(i);
    		
    		if (item.getXType() == 'fieldset') {
    			setFieldSetDisabled(item, disabled);
    		} else {
    			item.setDisabled(disabled);
    		}
    	}
    };
    
    
    //Contains the fields for bbox selection
    var bboxFieldSet = new Ext.form.FieldSet({ 
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
                    Ext.getCmp('timeFldSet').collapse();
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
            fieldLabel      : 'North bound latitude',
            value           : defaultNorthBoundLatitude.toString(),
            name            : 'northBoundLatitude',
            allowBlank      : false,
            anchor          : '-50'                                       
        },{
            id              : 'southBoundLatitude',                        
            xtype           : 'numberfield',
            fieldLabel      : 'South bound latitude',
            value           : defaultSouthBoundLatitude.toString(),
            name            : 'southBoundLatitude',
            allowBlank      : false,
            anchor          : '-50'                                       
        },{
            id              : 'eastBoundLongitude',                        
            xtype           : 'numberfield',
            fieldLabel      : 'East bound longitude',
            value           : defaultEastBoundLongitude.toString(),
            name            : 'eastBoundLongitude',
            allowBlank      : false,
            anchor          : '-50'                                       
        },{
            id              : 'westBoundLongitude',                        
            xtype           : 'numberfield',
            fieldLabel      : 'West bound longitude',
            value           : defaultWestBoundLongitude.toString(),
            name            : 'westBoundLongitude',
            allowBlank      : false,
            anchor          : '-50'                                       
        }]
    });
    
    //Contains the fields for temporal range selection
    var timeFieldSet = new Ext.form.FieldSet({ 
        id              : 'timeFldSet',
        title           : 'Time constraint',
        checkboxToggle  : true,
        checkboxName    : 'usingTimeConstraint',
        defaultType     : 'textfield',
        bodyStyle       : 'padding: 0 0 0 50px',
        collapsed       : true,
        listeners: {
            expand 		: {
                scope: this,
                fn : function(panel, anim) {
    				Ext.getCmp('bboxFldSet').collapse();
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
            id              : 'date',                        
            xtype           : 'datefield',
            fieldLabel      : 'Date',
            name            : 'date',
            format          : 'Y-m-d',
            allowBlank      : false,
            disabled      	: true,
            anchor          : '-50'                                       
        },{
            id              : 'time',                        
            xtype           : 'timefield',
            fieldLabel      : 'Time',
            name            : 'time',
            format          : 'H:i:s',
            allowBlank      : false,
            disabled      	: true,
            anchor          : '-50'                                       
        }]
    });
     
    var outputDimensionSpecs = new Ext.form.FieldSet({
        id				: 'outputDimSpec',
        title           : 'Output dimension specifications',
        //defaultType     : 'radio', // each item will be a radio button
        items			: [{
        	id				: 'radiogroup-outputDimensionType',
       		xtype			: 'radiogroup',
       		columns			: 2,
       		fieldLabel		: 'Type',
   			items           : [{
   	            id          	: 'radioHeightWidth',
   	            boxLabel      	: 'Width/Height',
   	            name            : 'outputDimensionsType',
   	            inputValue		: 'widthHeight',
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
   	            boxLabel	    : 'Resolution',
   	            name            : 'outputDimensionsType',
   	            inputValue		: 'resolution',
   	            checked         : false,
   	            listeners       : {
   	                check             : function (chkBox, checked) {
		   	        	var fldSet = Ext.getCmp('resolutionFieldSet');
						fldSet.setVisible(checked);
						setFieldSetDisabled(fldSet, !checked);
   	            	}
   	        	}
   	        }]
        },{
        	id				: 'widthHeightFieldSet',
        	xtype			: 'fieldset',
        	hideLabel		: true,
        	hideBorders		: true,
        	items 			: [{
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
        	id				: 'resolutionFieldSet',
        	xtype			: 'fieldset',
        	hideLabel		: true,
        	hideBorders		: true,
        	disabled		: true,
        	hidden			: true,
        	items 			: [{
            	id              : 'outputResX',                        
                xtype           : 'numberfield',
                fieldLabel      : 'X Resolution',
                value           : '1',
                name            : 'outputResX',
                anchor          : '-50',
                allowBlank      : false,
                disabled      	: true,
                allowNegative   : false
            },{
                id              : 'outputResY',                        
                xtype           : 'numberfield',
                fieldLabel      : 'Y Resolution',
                value           : '1',
                name            : 'outputResY',
                anchor          : '-50',
                allowBlank      : false,
                disabled      	: true,
                allowNegative   : false
            }]
        }]
    });
     
    //This should really be fetched from a DescribeCoverageRequest
    var downloadFormatStore = new Ext.data.SimpleStore({
    	fields : ['formatLabel', 'format'],
        data   : [['GeoTIFF', 'geotiff'],
                   ['NetCDF', 'netcdf'],
                   ['GeoTIFF_Float', 'geotiff_float']]             
    });
     
     //Contains all "Global" download options
    var downloadOptsFieldSet = new Ext.form.FieldSet({
        id              : 'downloadOptsFldSet',
        title           : 'Download options',
        defaultType     : 'textfield',
        bodyStyle       : 'padding: 0 0 0 50px',
        items: [{
            id              : 'inputCrs',                        
            xtype           : 'textfield',
            fieldLabel      : 'Reference System',
            value           : 'EPSG:4326',
            name            : 'inputCrs',
            anchor          : '-50',
            allowBlank      : false
        },{
            xtype			: 'combo',
            tpl             : '<tpl for="."><div ext:qtip="{formatLabel}" class="x-combo-list-item">{formatLabel}</div></tpl>',
            id              : 'downloadFormat',
            name            : 'downloadFormat',
            fieldLabel      : 'Format',
            labelAlign      : 'right',
            emptyText       : 'GeoTIFF',
            forceSelection  : true,
            mode            : 'local',
            store           : downloadFormatStore,
            typeAhead       : true,
            displayField    : 'formatLabel',
            anchor          : '-50',
            valueField      : 'format'        
        },{
            xtype           : 'textfield',
            id              : 'outputCrs',
            name            : 'outputCrs',
            fieldLabel      : 'Output CRS',
            anchor          : '-50'
        }]
    });
    
    // Dataset download window  
    var win = new Ext.Window({
        id              : 'wcsDownloadWindow',        
        autoScroll      : true,
        border          : true,        
        //html          : iStr,
        layout          : 'fit',
        resizable       : false,
        modal           : true,
        plain           : false,
        buttonAlign     : 'right',
        title           : 'Layer:  '+ layerName,
        height          : 600,
        width           : 500,
        items:[{
            // Bounding form
            id      :'wcsDownloadFrm',
            xtype   :'form',
            layout  :'form',
            frame   : true,
             
            // these are applied to columns
            defaults:{
                xtype: 'fieldset', layout: 'form'
            },
            
            // fieldsets
            items   :[{
                xtype   :'hidden',
                name    :'layerName', //name of the field sent to the server
                value   : layerName  //value of the field
            },{
                xtype   :'hidden',
                name    :'serviceUrl', //name of the field sent to the server
                value   : serviceUrl  //value of the field
            }, 
            bboxFieldSet,
            timeFieldSet,
            outputDimensionSpecs,
            downloadOptsFieldSet]
        }],
        buttons:[{
                xtype: 'button',
                text: 'Download',
                handler: function() {
                    
        			if (!validateWCSInfoWindow()) {
        				return;
        			}
        	
                    location.href = './downloadWCSAsZip.do?' + Ext.getCmp('wcsDownloadFrm').getForm().getValues(true); 
                }
        }]
    });
    
    win.show();
};

//Instance methods
GenericWCSInfoWindow.prototype.showInfoWindow = function() {
    
    var bbox = {};
    if (this.defaultBboxList && this.defaultBboxList.length > 0)
        bbox = this.defaultBboxList[0];
    
    var htmlFragment = '';
    
    htmlFragment += '<div style="padding:20px;" >' + 
                        '<p> TODO: Display some basic data here</p>' +
                    '</div>';

    //defaultWestBoundLongitude, defaultEastBoundLongitude, defaultSouthBoundLatitude, defaultNorthBoundLatitude
    htmlFragment += '<div align="right">' + 
                        '<br>' +
                        '<input type="button" id="downloadWCSBtn"  value="Download" onclick="showWCSDownload('+ 
                        '\'' + this.serviceUrl +'\',' + 
                        '\''+ this.layerName + '\',' +
                        bbox.westBoundLongitude + ',' +
                        bbox.eastBoundLongitude + ',' +
                        bbox.southBoundLatitude + ',' +
                        bbox.northBoundLatitude +
                        ');">' +
                    '</div>';
    
    
    
    if (this.overlay instanceof GMarker) {
        this.overlay.openInfoWindowHtml(htmlFragment, {maxWidth:800, maxHeight:600, autoScroll:true});
    } else if (this.overlay instanceof GPolygon) {
        this.map.openInfoWindowHtml(this.overlay.getBounds().getCenter(),htmlFragment);
    }
};