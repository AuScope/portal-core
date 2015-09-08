/**
 * A Panel specialisation for allowing the user to browse
 * the online resource contents of a set of portal.csw.OnlineResource
 * objects.
 */
Ext.define('portal.widgets.panel.OnlineResourcePanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.onlineresourcepanel',

    //Array of portal.csw.CSWRecord objects
    cswRecords : null, 
    
    // the parent record (ie the layer)
    parentRecord : null,
    
    /**
     * Accepts all Ext.grid.Panel options as well as
     * {
     *  cswRecords : single instance of array of portal.csw.CSWRecord objects
     *  parentRecord : the parent layer record, for accessing configuration. 
     * }
     */
    constructor : function(cfg) {
        // Ensures this.cswRecords is an array:
        this.cswRecords = [].concat(cfg.cswRecords);

        // the Layer that includes the resources
        this.parentRecord = cfg.parentRecord;
        
        //Generate our flattened 'data items' list for rendering to the grid
        var dataItems = portal.widgets.panel.OnlineResourcePanelRow.parseCswRecords(this.cswRecords);

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({[values.rows.length]} {[values.rows.length > 1 ? "Items" : "Item"]})'
        });

        //The following two Configs variables can be set by the owner
        var sortable = true;
        var hideHeaders = true;
        if (typeof(cfg.hideHeaders) !== 'undefined' && cfg.hideHeaders != null) {
            hideHeaders = cfg.hideHeaders;
        }
        if (typeof(cfg.sortable) !== 'undefined' && cfg.sortable != null) {
            sortable = cfg.sortable;
        }

        //We allow the owner to specify additional columns
        var columns = [{
            //Title column
            dataIndex: 'onlineResource',
            menuDisabled: true,
            sortable: sortable,
            width: 480,
            cellWrap : true,
            renderer: Ext.bind(this._detailsRenderer, this)
        },
        {
            dataIndex: 'onlineResource',
            width: 170,
            renderer: Ext.bind(this._linksRenderer, this)
        },
        {
            dataIndex: 'onlineResource',
            width: 150,
            renderer: Ext.bind(this._previewRenderer, this)
        }];
        if (cfg.columns) {
            columns = columns.concat(cfg.columns);
        }
        
        //Build our configuration object
        Ext.apply(cfg, {
            selModel: cfg.selModel,
            features : [groupingFeature],
            store : Ext.create('Ext.data.Store', {
                groupField : 'group',
                model : 'portal.widgets.panel.OnlineResourcePanelRow',
                data : dataItems
            }),
            plugins : [{
                ptype : 'selectablegrid'
            }],
            hideHeaders : hideHeaders,
            columns: columns,
            viewConfig: {
                enableTextSelection: true,
                listeners: {
                   
                    // when the view is fully loaded we need to check for availability of some features and update the DOM                    
                    viewready: function(view) {
                        for (var i = 0; i < cfg.cswRecords.length; i++) {
                            var onlineResources = cfg.cswRecords[i].data.onlineResources;
                            for (var j = 0; j < onlineResources.length; j++) {
                                var onlineResource = onlineResources[j]; 
                                if (onlineResource.get('type') == portal.csw.OnlineResource.WFS
                                        || onlineResource.get('type') == portal.csw.OnlineResource.WMS) {
                                    var serviceUrl = onlineResource.get('url');
                                    var version = onlineResource.get('version');  
                                    var name = onlineResource.get('name');                                
                                    var id = onlineResource.get('id');                                    
                                    var description = onlineResource.get('description');   
                                    portal.widgets.panel.OnlineResourcePanel.prototype.
                                        _getLayerAbstractControlText(serviceUrl, version, name, id, description, onlineResource.get('type'));        
                                    portal.widgets.panel.OnlineResourcePanel.prototype.
                                        _getLayerMetadataURLControlText(serviceUrl, version, name, id, onlineResource.get('type'));    
                                }
                            }
                        }
                    }
                }
              }
        });

        this.callParent(arguments);
    },

    // renderer for the details of the resource (left hand column: name, url, etc)
    _detailsRenderer : function(value, metaData, record, parentRecord, row, col, store, gridView) {
        var onlineResource = record.get('onlineResource');
        var cswRecord = record.get('cswRecord');
        var styleURL = this.parentRecord.get("proxyStyleUrl");
        
        var name = onlineResource.get('name');
        var url = onlineResource.get('url');	
        var description = onlineResource.get('description');
        var version = onlineResource.get('version');  
        var rowLabelTitle = '<strong>Title:</strong>&nbsp;';
        
        // Probably could just use the type directly but I suspect it would be better to code it here
        var rowLabelURL = '';
        switch(onlineResource.get('type')) {
    		case portal.csw.OnlineResource.WFS:
    			rowLabelURL = '<strong>WFS URL:</strong>&nbsp';
    			break;
    		case portal.csw.OnlineResource.WMS:
    			rowLabelURL = '<strong>WMS URL:</strong>&nbsp';
    			break;
        }
        
        // WFS resources will have a feature type row
        var rowLabelFeatureType = "<strong>Feature type:</strong>&nbsp";
        
        //Ensure there is a title (even it is just '<Untitled>'
        if (!name || name.length === 0) {
            name = '&gt;Untitled&lt;';
        }

        //Truncate description
        var maxLength = 190;
        if (description.length > maxLength) {
            description = description.substring(0, maxLength) + '...';
        }

        //Render our HTML
        switch(onlineResource.get('type')) {        
        
        case portal.csw.OnlineResource.WWW:
        case portal.csw.OnlineResource.FTP:
        case portal.csw.OnlineResource.IRIS:
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{
                    tag : 'a',
                    target : '_blank',
                    href : url,
                    children : [{
                        tag : 'b',
                        html : name
                    }]
                },{
                    tag : 'br'
                },{
                    tag : 'span',
                    style : {
                        color : '#555'
                    },
                    html : description
                }]
            });

        // Add a separate entry for WMS even though it is similar to 'default'. Maybe they will diverge.
        case portal.csw.OnlineResource.WMS:
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{ 
                    	tag: 'span',
                    	html : rowLabelTitle +  description
                    },
                    {
                        tag : 'br'
                    },  
                    { 
                    	tag: 'span',
                    	html : rowLabelURL +  url
                    },
                    {
                        tag : 'br'
                    }                 
                ]
            });
            
        // WFS resources
        case portal.csw.OnlineResource.WFS:
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{ 
                    	tag: 'span',
                    	html : rowLabelTitle +  description 
                    },
                    {
                        tag : 'br'
                    },  
                    { 
                    	tag: 'span',
                    	html : rowLabelURL +  url
                    },
                    {
                        tag : 'br'
                    },
                    { 
                    	tag: 'span',
                    	html : rowLabelFeatureType + name
                    }                    
                ]
            });
        
		// any other not-specifically handled resource type	
        default:
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
                        html : url
                    },{
                        html : description
                    }]
                }]
            });
        }
    },
    
    _linksRenderer : function(value, metaData, record, row, col, store, gridView) {
        var onlineResource = record.get('onlineResource');
        var cswRecord = record.get('cswRecord');
        var url = onlineResource.get('url');
        var name = onlineResource.get('name');
        var id = onlineResource.get('id');
        var version = onlineResource.get('version');  
        var description = onlineResource.get('description');
        
        var layerAbstractSpanId = id.replace(/\W/g, '') + '_abstract';        
        var fullMetadataSpanId = id.replace(/\W/g, '') + '_metadata';     
        
        switch(onlineResource.get('type')) {
        case portal.csw.OnlineResource.WFS:
            var getFirst5FeaturesURL = url + this.internalURLSeperator(url) + 'SERVICE=WFS&REQUEST=GetFeature&VERSION=1.1.0&maxFeatures=5&typeName=' + name;
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{
                    tag: 'span', 
                    id : layerAbstractSpanId,
                    html : 'Loading Layer abstract...'
                },
                {tag : 'br'},
                {
                    tag : 'span',
                    id : fullMetadataSpanId,
                    html : 'Loading Full Metadata...'
                },
                {tag : 'br'},
                {
                    tag : 'a',
                    target : '_blank',
                    href : getFirst5FeaturesURL,
                    html : 'First 5 features'
                }
                ]
            });
            
        case portal.csw.OnlineResource.WCS:
            var describeCoverageUrl = url + this.internalURLSeperator(url) + 'SERVICE=WCS&REQUEST=DescribeCoverage&VERSION=1.0.0&coverage=' + name;
            return Ext.DomHelper.markup({
                tag : 'a',
                target : '_blank',
                href : describeCoverageUrl,
                html : 'DescribeCoverage response'
            });
        case portal.csw.OnlineResource.OPeNDAP:
            return Ext.DomHelper.markup({
                tag : 'a',
                target : '_blank',
                href : url + '.html',
                html : 'OPeNDAP Data access form'
            });
        case portal.csw.OnlineResource.SOS:
            var getObservations = url + this.internalURLSeperator(url) + 'SERVICE=SOS&REQUEST=GetObservation&VERSION=2.0.0&OFFERING=' + escape(name) + '&OBSERVEDPROPERTY=' + escape(description) + '&RESPONSEFORMAT=' + escape('http://www.opengis.net/om/2.0');
            return Ext.DomHelper.markup({
                tag : 'a',
                target : '_blank',
                href : getObservations,
                html : 'Observations for ' + description
            });  
        
        case portal.csw.OnlineResource.WMS:
            return Ext.DomHelper.markup({
                tag : 'div',
                children : [{
                    tag: 'span',
                    id : layerAbstractSpanId,
                    html : 'Loading Layer abstract...'
                },
                {tag : 'br'},
                {
                    tag : 'span',
                    id : fullMetadataSpanId,
                    html : 'Loading Full Metadata...'
                }
                ]
            });
              
        default :
            return '';
        }
    },

    _previewRenderer : function(value, metaData, record, row, col, store, gridView) {
        var onlineResource = record.get('onlineResource');
        var cswRecord = record.get('cswRecord');
        var url = onlineResource.get('url');
        var name = onlineResource.get('name');
        var description = onlineResource.get('description');
        
        switch(onlineResource.get('type')) {
        case portal.csw.OnlineResource.WMS:

            //To generate the url we will need to use the bounding box to make the request
            //To avoid distortion, we also scale the width height independently
            var geoEls = cswRecord.get('geographicElements');
            if (geoEls && geoEls.length > 0) {
                var superBbox = geoEls[0];
                for (var i = 1; i < geoEls.length; i++) {
                    superBbox = superBbox.combine(geoEls[i]);
                }

                //Set our width to a constant and scale the height appropriately
                var heightRatio = (superBbox.northBoundLatitude - superBbox.southBoundLatitude) /
                                  (superBbox.eastBoundLongitude - superBbox.westBoundLongitude);
                var width = 512;
                var height = Math.floor(width * heightRatio);

                var thumbWidth = width;
                var thumbHeight = height;

                //Scale our thumbnail appropriately
                if (thumbWidth > 128) {
                    thumbWidth = 128;
                    thumbHeight = thumbWidth * heightRatio;
                }

                var getMapUrl = '';
                if(cswRecord.get('version')=='1.3.0'){
                    getMapUrl = portal.map.primitives.BaseWMSPrimitive.getWms_130_Url(url, name, superBbox, width, height);
                    console.log("1.1.1:" + portal.map.primitives.BaseWMSPrimitive.getWmsUrl(url, name, superBbox, width, height));
                    console.log("1.3.0:"+portal.map.primitives.BaseWMSPrimitive.getWms_130_Url(url, name, superBbox, width, height));
                }else{
                    getMapUrl = portal.map.primitives.BaseWMSPrimitive.getWmsUrl(url, name, superBbox, width, height);
                }

                return Ext.DomHelper.markup({
                    tag : 'a',
                    target : '_blank',
                    href : getMapUrl,
                    children : [{
                        tag : 'img',
                        width : thumbWidth,
                        height : thumbHeight,
                        alt : 'Loading preview...',
                        src : getMapUrl
                    }]
                });
            }
            return 'Unable to preview WMS';
        default :
            return '';
        }
    },
    
    /**
     * Given a URL this will determine the correct character that can be appended
     * so that a number of URL parameters can also be appended
     *
     * See AUS-1931 for why this function should NOT exist
     */
    internalURLSeperator : function(url) {
        var lastChar = url[url.length - 1];
        if (lastChar == '?') {
            return '';
        } else if (lastChar === '&') {
            return '';
        } else if (url.indexOf('?') >= 0) {
            return '&';
        } else {
            return '?';
        }
    },
      
      /**
       * Looks for a Layer abstract from the getCapabilities document. If found, returns a 'link'
       * otherwise returns a text message stating that none was found
       */
      _getLayerAbstractControlText : function(serviceUrl, version, name, id, description, type) {
          var requestURL;
          switch (type) {
              case portal.csw.OnlineResource.WFS:
                  requestURL = 'getWFSFeatureAbstract.do';
                  break;
              case portal.csw.OnlineResource.WMS:
                  requestURL = 'getWMSLayerAbstract.do';
                  break; 
              default: 
                  // bad type passed in to this function
                  return; 
          }
          // callback to set the text of the "Layer abstract" control
          var getLayerAbstractCallback = function(options, success, response) {
              var layerAbstractElement = Ext.get(id.replace(/\W/g, '') + '_abstract');
              if (layerAbstractElement) {
                  var jsonResponse;
                  if (!success || !JSON.parse(response.responseText)["data"]) {                  
                      layerAbstractElement.update('No abstract provided.');
                  }  else {    
                      layerAbstractElement.update('Layer abstract');
                      layerAbstractElement.addListener('click', function(){
                          portal.widgets.panel.OnlineResourcePanel.prototype._layerAbstractPopupHandler(serviceUrl, version, name, description, type);
                      });
                      layerAbstractElement.set({
                        // pretend it to be a link.
                        style : 'color:blue; text-decoration:underline; cursor:pointer'
                    });
                  }
              }
        };
        
        Ext.Ajax.request({
            url : requestURL,
            params : { 
                serviceUrl : serviceUrl,
                version : version,
                name : name
            },            
            waitMsg: 'Fetching Layer abstract...',
            callback : getLayerAbstractCallback 
        });
        
      },
      
      /**
       * Handler for clicking on the Layer Abstract element.
       */
      _layerAbstractPopupHandler : function(serviceUrl, version, name, description, type) {          
          
          var layerAbstractURL;
          switch (type) {
              case portal.csw.OnlineResource.WFS:
                  layerAbstractURL = "getWFSFeatureAbstract.do";
                  break;
              case portal.csw.OnlineResource.WMS:
                  layerAbstractURL = "getWMSLayerAbstract.do";
                  break; 
              default: 
                  // bad type passed in to this function
                  return; 
          }
                             
          // callback from the ajax function that gets the abstract.
          var layerAbstractPopupCallback = function(response){           
                  
              var data = JSON.parse(response.responseText)["data"];
              
              Ext.create('Ext.window.Window', {
                  title : 'Abstract: '+ description,
                  layout : 'fit',
                  width : 600,
                  height : 300,
                  items : [ {                        
                      xtype: 'panel', 
                      layout : 'column',
                      maxHeight : 300,
                      autoScroll : true,
                      items : [{
                        html : data
                      }]   
                  }] 
              }).show();
          };
          
          Ext.Ajax.request({
              url : layerAbstractURL,
              params : { 
                  serviceUrl : serviceUrl,
                  version : version,
                  name : name
              }, 
              timeout : 180000,
              scope : this,
              success : layerAbstractPopupCallback,
              failure : function(response, opts) {
                  return;
              }
          });        
        },
        
        /**
         * Looks for a MetadataURL element from the getCapabilities document. If found, returns a 'link'
         * otherwise returns a text message stating that none was found
         */
        _getLayerMetadataURLControlText : function(serviceUrl, version, name, id, type) {
            var requestURL;
            switch (type) {
                case portal.csw.OnlineResource.WFS:
                    requestURL = 'getWFSFeatureMetadataURL.do';
                    break;
                case portal.csw.OnlineResource.WMS:
                    requestURL = 'getWMSLayerMetadataURL.do';
                    break; 
                default: 
                    // bad type passed in to this function
                    return; 
            }
            // callback to set the text of the "Layer abstract" control
            var getMetadataURLCallback = function(options, success, response) {
                var layerMetadataURLElement = Ext.get(id.replace(/\W/g, '') + '_metadata');
                if (layerMetadataURLElement) {
                    var jsonResponse;
                    if (!success || !JSON.parse(response.responseText)["data"]) {                  
                        layerMetadataURLElement.update('No MetadataURL provided.');
                    }  else {    
                        var url = JSON.parse(response.responseText)["data"];
                        layerMetadataURLElement.update('<a href=\'' + url + '\' target=\'_blank\'>Full Metadata</a>');
                        layerMetadataURLElement.set({
                          // pretend it to be a link.
                          style : 'color:blue; text-decoration:underline; cursor:pointer'
                      });
                    }
                }
          };
          
          Ext.Ajax.request({
              url : requestURL,
              params : { 
                  serviceUrl : serviceUrl,
                  version : version,
                  name : name
              },            
              waitMsg: 'Fetching Metadata URL...',
              callback : getMetadataURLCallback 
          });
          
        }        
        
  
});


/**
 * Convenience class for representing the rows in the OnlineResourcesPanel
 */
Ext.define('portal.widgets.panel.OnlineResourcePanelRow', {
    extend : 'Ext.data.Model',

    statics : {
        /**
         * Turns an array of portal.csw.CSWRecord objects into an equivalent array of
         * portal.widgets.panel.OnlineResourcePanelRow objects
         */
        parseCswRecords : function(cswRecords) {
            var dataItems = [];
            for (var i = 0; i < cswRecords.length; i++) {
                var onlineResources = cswRecords[i].getAllChildOnlineResources();
                for (var j = 0; j < onlineResources.length; j++) {

                    //ensure we have a type we want to describe
                    var group = portal.csw.OnlineResource.typeToString(onlineResources[j].get('type'),onlineResources[j].get('version'));
                    if (!group) {
                        continue; //don't include anything else
                    }

                    dataItems.push(Ext.create('portal.widgets.panel.OnlineResourcePanelRow',{
                        group : group,
                        onlineResource : onlineResources[j],
                        cswRecord : cswRecords[i]
                    }));
                }
            }

            return dataItems;
        }
    },

    fields: [
             {name : 'group', type: 'string'},
             {name : 'onlineResource', type: 'auto'},
             {name : 'cswRecord', type: 'auto'}
    ]
});