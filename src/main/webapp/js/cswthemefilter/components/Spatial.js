Ext.namespace("CSWThemeFilter");

/**
 * An extension of CSWThemeFilter.BaseComponent to allow the selection of a spatial bounding box
 */
CSWThemeFilter.Spatial = Ext.extend(CSWThemeFilter.BaseComponent, {
    /**
     * An instance of MPolyDragControl
     */
    bboxSelection : null,

    /**
     * BBOX coordinate - Will be populated after the component is rendered
     */
    numberFieldNELat : null,
    /**
     * BBOX coordinate - Will be populated after the component is rendered
     */
    numberFieldNELon : null,
    /**
     * BBOX coordinate - Will be populated after the component is rendered
     */
    numberFieldSWLat : null,
    /**
     * BBOX coordinate - Will be populated after the component is rendered
     */
    numberFieldSWLon : null,

    /**
     * Shown when the user clicks "Draw Bounds"
     */
    labelBBoxHelp : null,

    constructor : function(cfg) {
        var spatialComponent = this;

        //This is our control for drawing a bounding box on the map
        this.bboxSelection = new MPolyDragControl({
            map: cfg.map,
            type: 'rectangle',
            labelText: 'CSW Bounds',
            ondragend : function() {
                spatialComponent.labelBBoxHelp.hide();
                spatialComponent.numberFieldNELat.setRawValue(spatialComponent.bboxSelection.getNorthEastLat());
                spatialComponent.numberFieldNELon.setRawValue(spatialComponent.bboxSelection.getNorthEastLng());
                spatialComponent.numberFieldSWLat.setRawValue(spatialComponent.bboxSelection.getSouthWestLat());
                spatialComponent.numberFieldSWLon.setRawValue(spatialComponent.bboxSelection.getSouthWestLng());
            }
        });

        //Build our configuration
        Ext.apply(cfg, {
            title : 'Spatial Bounds',
            collapsible : true,
            border : false,
            items : [{
                xtype : 'livenumberfield',
                fieldLabel : 'Lat (NE)',
                name : 'neLat',
                anchor : '100%',
                decimalPrecision : 6,
                endTypingDelay : 500,
                listeners : {
                    change : this._numberFieldChange.createDelegate(this, [])
                }
            },{
                xtype : 'livenumberfield',
                fieldLabel : 'Lon (NE)',
                name : 'neLon',
                anchor : '100%',
                decimalPrecision : 6,
                endTypingDelay : 500,
                listeners : {
                    change : this._numberFieldChange.createDelegate(this, [])
                }
            },{
                xtype : 'livenumberfield',
                fieldLabel : 'Lat (SW)',
                name : 'swLat',
                anchor : '100%',
                decimalPrecision : 6,
                endTypingDelay : 500,
                listeners : {
                    change : this._numberFieldChange.createDelegate(this, [])
                }
            },{
                xtype : 'livenumberfield',
                fieldLabel : 'Lon (SW)',
                name : 'swLon',
                anchor : '100%',
                decimalPrecision : 6,
                endTypingDelay : 500,
                listeners : {
                    change : this._numberFieldChange.createDelegate(this, [])
                }
            },{
                xtype : 'button',
                text : 'Draw Bounds',
                handler : this._drawBoundsHandler.createDelegate(this, [])
            },{
                xtype : 'label',
                cls : 'x-form-item',
                text : 'Use your mouse to drag a filter bounding box on the map.',
                hidden : true
            }],
            listeners : {
                //Get references to our fields
                afterrender : function(cmp) {
                    var coordFields = cmp.findByType('livenumberfield');
                    for (var i = 0; i < coordFields.length; i++) {
                        if (coordFields[i].getName() === 'neLat') {
                            spatialComponent.numberFieldNELat = coordFields[i];
                        } else if (coordFields[i].getName() === 'neLon') {
                            spatialComponent.numberFieldNELon = coordFields[i];
                        } else if (coordFields[i].getName() === 'swLat') {
                            spatialComponent.numberFieldSWLat = coordFields[i];
                        } else if (coordFields[i].getName() === 'swLon') {
                            spatialComponent.numberFieldSWLon = coordFields[i];
                        }
                    }

                    var labelFields = cmp.findByType('label');
                    spatialComponent.labelBBoxHelp = labelFields[0];
                }
            }
        });

        //Construct our instance
        CSWThemeFilter.Spatial.superclass.constructor.call(this, cfg);
    },

    _numberFieldChange : function() {
        var north = this.numberFieldNELat.getValue();
        var east = this.numberFieldNELon.getValue();
        var south = this.numberFieldSWLat.getValue();
        var west = this.numberFieldSWLon.getValue();

        //If we have entered in specific values, draw that bounds on the map
        if (!isNaN(north) && !isNaN(south) &&
            !isNaN(east) && !isNaN(west)) {
            this.bboxSelection.drawRectangle(north,east,south,west);
        } else {
            this.bboxSelection.reset();
        }
    },

    /**
     * Handler for the draw bounds function
     */
    _drawBoundsHandler : function() {
        this.numberFieldNELat.setValue(Number.NaN);
        this.numberFieldNELon.setValue(Number.NaN);
        this.numberFieldSWLat.setValue(Number.NaN);
        this.numberFieldSWLon.setValue(Number.NaN);


        if (!this.bboxSelection.transMarkerEnabled) {
            this.labelBBoxHelp.show();
            this.bboxSelection.enableTransMarker();
        }
    },

    /**
     * Returns the selected spatial bounding box
     */
    getFilterValues : function() {
        var north = this.numberFieldNELat.getValue();
        var east = this.numberFieldNELon.getValue();
        var south = this.numberFieldSWLat.getValue();
        var west = this.numberFieldSWLon.getValue();

        if (isNaN(north) || isNaN(south) ||
            isNaN(east) || isNaN(west)) {
            return {};
        } else {
            return {
                westBoundLongitude : west,
                eastBoundLongitude : east,
                northBoundLatitude : north,
                southBoundLatitude : south
            };
        }
    },

    /**
     * The Spatial component supports all URN's
     */
    supportsTheme : function(urn) {
        return true;
    }
});
