/**
 * An abstract base class to be extended.
 *
 * Represents a pseudo grid panel (see AUS-2685) for containing layers
 * that haven't yet been added to the map. Each row
 * will be grouped under a heading, contain links
 * to underlying data sources and have a spatial location
 * that can be viewed by the end user.
 *
 * This class is expected to be extended for usage within
 * the 'Registered Layers', 'Known Layers' and 'Custom Layers'
 * panels in the portal. Support for KnownLayers/CSWRecords and
 * other row types will be injected by implementing the abstract
 * functions of this class.
 * 
 * This is a super-classs; the sub-classes should define the columns of the Grid Panel
 *
 */
Ext.define('portal.widgets.panel.CommonBaseRecordPanel', {
    extend : 'portal.widgets.panel.recordpanel.RecordPanel',
    alias: 'widget.commonbaserecordpanel',
    browseCatalogueDNSMessage : false, //VT: Flags the do not show message when browse catalogue is clicked.
    map : null,
    menuFactory : null,
    onlineResourcePanelType : null,
    serviceInformationIcon : null,
    nagiosErrorIcon: null,
    mapExtentIcon : null,
    
    
    /**
     * Define listeners to combine with any passed in with the Options
     */
    listenersHere : {},

    constructor : function(cfg) {
        var me = this;
        me.map = cfg.map;
        me.menuFactory = cfg.menuFactory;
        me.onlineResourcePanelType = cfg.onlineResourcePanelType;
        me.serviceInformationIcon = cfg.serviceInformationIcon;
        me.nagiosErrorIcon = Ext.isEmpty(cfg.nagiosErrorIcon) ? 'portal-core/img/warning.png' : cfg.nagiosErrorIcon;
        me.mapExtentIcon = cfg.mapExtentIcon;
        me.listeners = Object.extend(me.listenersHere, cfg.listeners);

        me.callParent(arguments);
    },
    
    onDestroy : function() {
        me.callParent();
    },
    
    //-------- Abstract methods requiring implementation ---------

    /**
     * Abstract function - Should return a string based title
     * for a given record
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose title should be extracted
     */
    getTitleForRecord : portal.util.UnimplementedFunction,

    /**
     * Abstract function - Should return an Array of portal.csw.OnlineResource
     * objects that make up the specified record. If no online resources exist
     * then an empty array can be returned
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose underlying online resources should be extracted.
     */
    getOnlineResourcesForRecord : portal.util.UnimplementedFunction,

    /**
     * Abstract function - Should return an Array of portal.util.BBox
     * objects that represent the total spatial bounds of the record. If no
     * bounds exist then an empty array can be returned
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose spatial bounds should be extracted.
     */
    getSpatialBoundsForRecord : portal.util.UnimplementedFunction,

    /**
     * Abstract function - Should return an Array of portal.csw.CSWRecord
     * objects that make up the specified record.
     *
     * function(Ext.data.Model record)
     *
     * record - The record whose underlying CSWRecords should be extracted.
     */
    getCSWRecordsForRecord : portal.util.UnimplementedFunction,

    //--------- Class Methods ---------

    /**
     * Generates an Ext.DomHelper.markup for the specified imageUrl
     * for usage as an image icon within this grid.
     */
    _generateHTMLIconMarkup : function(imageUrl) {
        return Ext.DomHelper.markup({
            tag : 'div',
            style : 'text-align:center;',
            children : [{
                tag : 'img',
                width : 16,
                height : 16,
                align: 'CENTER',
                src: imageUrl
            }]
        });
    },

    /**
     * Renderer used in Column definitions that will be done on subclasses.  Useful to define here.
     * 
     * Internal method, acts as a renderer function for rendering
     * the title of the record.
     *
     * http://docs.sencha.com/ext-js/4-0/#!/api/Ext.grid.column.Column-cfg-renderer
     */
    _titleRenderer : function(value, metaData, record, row, col, store, gridView) {
        return this.getTitleForRecord(record);
    },

    /**
     * Renderer used in Column definitions that will be done on subclasses.  Useful to define here.
     *
     * Internal method, acts as a renderer function for rendering
     * the service information of the record.
     *
     */
    _serviceInformationRenderer : function(value, record) {
        
        if(record.get('resourceProvider')=="kml"){
            return 'portal-core/img/kml.png';
        }
        
        var onlineResources = this.getOnlineResourcesForRecord(record);

        var serviceType = this._getServiceType(onlineResources);
        
        var containsDataService = serviceType.containsDataService;
        var containsImageService = serviceType.containsImageService;

        // default iconPath where there is no service info available
        var iconPath = 'portal-core/img/exclamation.png';

        if ((containsDataService || containsImageService) && 
            (record instanceof portal.knownlayer.KnownLayer) &&
            record.containsNagiosFailures()) {
            iconPath = this.nagiosErrorIcon;
        } else if (this.serviceInformationIcon && (containsDataService || containsImageService)) {
            // check whether the portal has overridden the icons
            iconPath = this.serviceInformationIcon;
        } else {        
            if (containsDataService) {            
                iconPath = 'portal-core/img/binary.png'; //a single data service will label the entire layer as a data layer
            } else if (containsImageService) {
                iconPath = 'portal-core/img/picture.png';
            }
        }
        
        return iconPath;
    },
    
    /**
     * Helper function.  Useful to define here for other methods and subclasses.
     */
    _getServiceType : function(onlineResources){
        var containsDataService = false;
        var containsImageService = false;
        
      //We classify resources as being data or image sources.
        for (var i = 0; i < onlineResources.length; i++) {
            switch(onlineResources[i].get('type')) {
            case portal.csw.OnlineResource.WFS:
            case portal.csw.OnlineResource.WCS:
            case portal.csw.OnlineResource.SOS:
            case portal.csw.OnlineResource.OPeNDAP:
            case portal.csw.OnlineResource.CSWService:
            case portal.csw.OnlineResource.IRIS:
            case portal.csw.OnlineResource.NCSS:
                containsDataService = true;
                break;
            case portal.csw.OnlineResource.WMS:
            case portal.csw.OnlineResource.WWW:
            case portal.csw.OnlineResource.FTP:
            case portal.csw.OnlineResource.CSW:
            case portal.csw.OnlineResource.UNSUPPORTED:
                containsImageService = true;
                break;
            }
        }
       
        return result = {
            containsDataService : containsDataService,
            containsImageService : containsImageService
        };
    },

    /**
     * Renderer used in Column definitions that will be done on subclasses.  Useful to define here.
     *
     * Internal method, acts as an renderer function for rendering
     * the spatial bounds column of the record.
     */
    _spatialBoundsRenderer : function(value, record) {
        var spatialBounds = this.getSpatialBoundsForRecord(record);

        if (spatialBounds.length > 0 || record.internalId == 'portal-InSar-reports') {                                   
            var icon = null;
            if (this.mapExtentIcon) {
                icon = this.mapExtentIcon;
            } else {
                icon = 'portal-core/img/magglass.gif';
            }
            return icon;
        }

        return '';
    },

    /**
     * Helper function.  Useful to define here for subclasses.
     *
     * Show a popup containing info about the services that 'power' this layer
     */
    _serviceInformationClickHandler : function(value, record) {
        var cswRecords = this.getCSWRecordsForRecord(record);
        if (!cswRecords || cswRecords.length === 0) {
            return;
        }

        var popup = Ext.create('portal.widgets.window.CSWRecordDescriptionWindow', {
            cswRecords : cswRecords,
            parentRecord : record,
            onlineResourcePanelType : this.onlineResourcePanelType
        });

        popup.show();
    },


    /**
     * Helper function.  Useful to define here for subclasses.
     *
     * On single click, show a highlight of all BBoxes
     */
    _spatialBoundsClickHandler : function(value, record) {
        var spatialBoundsArray;
        if (record.internalId == 'portal-InSar-reports') {
            spatialBoundsArray = this.getWholeGlobeBounds();
        } else {
            spatialBoundsArray = this.getSpatialBoundsForRecord(record);
        }
        var nonPointBounds = [];

        //No point showing a highlight for bboxes that are points
        for (var i = 0; i < spatialBoundsArray.length; i++) {
            var bbox = spatialBoundsArray[i];
            if (bbox.southBoundLatitude !== bbox.northBoundLatitude ||
                bbox.eastBoundLongitude !== bbox.westBoundLongitude) {

                //VT: Google map uses EPSG:3857 and its maximum latitude is only 85 degrees
                // anything more will stretch the transformation
                if(bbox.northBoundLatitude>85){
                    bbox.northBoundLatitude=85;
                }
                if(bbox.southBoundLatitude<-85){
                    bbox.southBoundLatitude=-85;
                }
                nonPointBounds.push(bbox);
            }
        }

        this.map.highlightBounds(nonPointBounds);
    },

    /**
     * Helper function.  Useful to define here for subclasses.
     *
     * Return the max bbox for insar layer as it is a dummy CSW.
     */
    getWholeGlobeBounds : function() {
        var bbox = new Array();
        bbox[0] = Ext.create('portal.util.BBox', {
            northBoundLatitude : 85,
            southBoundLatitude : -85,
            eastBoundLongitude : 180,
            westBoundLongitude : -180
        });
        return bbox;
    },

    /**
     * Helper function.  Useful to define here for subclasses.
     *
     * On double click, move the map so that specified bounds are visible
     */
    _spatialBoundsDoubleClickHandler : function(value, record) {
        var spatialBoundsArray;
        if (record.internalId == 'portal-InSar-reports') {
            spatialBoundsArray = this.getWholeGlobeBounds();
        } else {
            spatialBoundsArray = this.getSpatialBoundsForRecord(record);
        }

        if (spatialBoundsArray.length > 0) {
            var superBBox = spatialBoundsArray[0];

            for (var i = 1; i < spatialBoundsArray.length; i++) {
                superBBox = superBBox.combine(spatialBoundsArray[i]);
            }

            this.map.scrollToBounds(superBBox);
        }
    },

    /**
     * A renderer for generating the contents of the tooltip that shows when the
     * layer is loading
     */
    _loadingTipRenderer : function(value, record, tip) {
        var layer = record.get('layer');
        if(!layer){//VT:The layer has yet to be created.
            return 'No status has been recorded';
        }
        var renderer = layer.get('renderer');
        var update = function(renderStatus, keys) {
            tip.update(renderStatus.renderHtml());
        };

        //Update our tooltip as the underlying status changes
        renderer.renderStatus.on('change', update, this);
        tip.on('hide', function() {
            renderer.renderStatus.un('change', update); //ensure we remove the handler when the tip closes
        });

        return renderer.renderStatus.renderHtml();
    },
    
    _loadingClickHandler : function(value, record) {
        
        var layer = record.get('layer');
        
        var html = '<p>No Service recorded, Click on Add layer to map</p>';    
        
        if(layer){
            var renderer = layer.get('renderer');
            html =  renderer.renderStatus.renderHtml();
        }        
        var win = Ext.create('Ext.window.Window', {
            title: 'Service Loading Status',
            height: 200,
            width: 500,
            layout: 'fit',
            modal: true,
            items: {  // Let's put an empty grid in just to illustrate fit layout
                xtype: 'panel',
                autoScroll : true,                
                html : html
            }
        });
        
        win.show();
    }
});
