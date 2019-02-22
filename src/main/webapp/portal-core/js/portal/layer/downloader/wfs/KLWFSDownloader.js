/**
 * A knownlayer downloader that creates an Ext.Window specialised into showing a
 * dialog for the user to download features from a WFS in a zip file.
 * This differ from WFSDownloader as it engage the use of downloader tracker which
 * forces the user enter a email address and allow the user to check back later with
 * the download progress
 */
Ext.define('portal.layer.downloader.wfs.KLWFSDownloader', {
    extend: 'portal.layer.downloader.Downloader',

    currentTooltip : null,
    featureCountUrl : null,
    enableFeatureCounts : false,

    /**
     * Adds the following config options
     * 
     * featureCountUrl : String - URL where feature counts will be looked up if proxy URL DNE. 
     * enableFeatureCounts : Boolean - Set to true to use feature counting in the popup.   
     */
    constructor : function(cfg) {
        this.featureCountUrl = cfg.featureCountUrl ? cfg.featureCountUrl : null;
        this.enableFeatureCounts = cfg.enableFeatureCounts ? true : false;
        this.callParent(arguments);
    },

    /**
     * An implementation of an abstract method, see parent method for details
     *
     * layer - portal.layer.Layer that owns resources
     * resources - an array of data sources that were used to render data
     * renderedFilterer - custom filter that was applied when rendering the specified data sources
     * currentFilterer - The value of the custom filter, this may differ from renderedFilterer if the
     *                   user has updated the form/map without causing a new render to occur
     * 
     */
    downloadData : function(layer, resources, renderedFilterer, currentFilterer) {
        var me = this;

        var currentlyVisibleBBox = null;

        currentlyVisibleBBox = currentFilterer.getSpatialParam();

        var downloadFilterer = Ext.create('portal.layer.filterer.Filterer', {});

        downloadFilterer.setSpatialParam(currentlyVisibleBBox, true);
        downloadFilterer.setParameters(renderedFilterer.getParameters(), true);

        var cswRecords = layer.get('cswRecords');
        //Create a popup showing our options
        Ext.create('Ext.Window', {
            title : 'Download Options',
            buttonAlign : 'right',
            modal : true,
            border : '1 1 0 1',
            width : 400,
            layout : {
                type : 'anchor'
                //align : 'stretch'
            },

            items : [{
                xtype : 'fieldset',
                anchor : '100%',
                layout : 'fit',
                padding : '10 10 0 10',
                border : false,
                items : [{
                    xtype : 'label',
                    style : 'font-size: 12px;',
                    itemId: 'klwfs-htmllabel',
                    html : this._parseNotifcationString(cswRecords)
                }]
            },{
                xtype : 'fieldset',
                anchor : '100%',
                layout : 'fit',
                border : false,
                items : [{
                    //Our radiogroup can see its item list vary according to the presence of bounding boxes
                    xtype : 'fieldcontainer',
                    defaultType: 'checkboxfield',

                    items : [{
                        boxLabel : 'Filter my download using the current visible map bounds.',
                        itemId: 'klwfs-checkbox',
                        checked : true,
                        listeners : {
                            change : Ext.bind(this._handleRadioChange, this, [currentlyVisibleBBox, resources, layer], true)
                        }

                    }]
                },{

                    xtype : 'fieldset',
                    anchor : '100%',
                    layout : 'fit',
                    border : false,
                    margin : '0 0 50 0',
                    items : [{
                        xtype           : 'textfield',
                        itemId          : 'downloadToken',
                        fieldLabel      : 'Email Address*',
                        emptyText       : 'Enter your email address',
                        name            : 'email',
                        selectOnFocus   : true,
                        allowBlank      : false,
                        blankText       : 'This field is required',
                        anchor          : '-50'
                    }]

                }]

            }],
            dockedItems: [{
                xtype : 'toolbar',
                dock: 'bottom',
                anchor : '100%',
                border : '0 1 1 1',
                margin : '-1 0 0 0',
                layout: {
                    type: 'hbox',
                    pack: 'end'
                },
                items : [{
                    xtype : 'button',
                    text: 'Check Status',
                    iconCls : 'info',
                    handler: function(btn) {
                        var popup = btn.up('window');
                        var sEmail = popup.down('#downloadToken').getValue();
                        if ( sEmail === '') {
                            Ext.MessageBox.alert('Unable to submit request...','Please enter a valid email address');
                            popup.down('#downloadToken').markInvalid();
                            return;
                        } else {
                           me._doCheckRequest(sEmail);
                        }
                    }

                },{
                    xtype : 'button',
                    text : 'Download',
                    iconCls : 'download',
                    handler : function(button) {
                        var popup = button.up('window');
                        var sEmail = popup.down('#downloadToken').getValue();
                        var outputFormat = "csv";
                        if ( sEmail === '' && sEmail.length < 4) {
                            Ext.MessageBox.alert('Unable to submit request...','Please enter a valid email address');
                            popup.down('#downloadToken').markInvalid();
                            return;
                        } else {
                            var bboxJson = '';
                            var popup = button.ownerCt.ownerCt;

                            var selected = popup.down('#klwfs-checkbox').getValue();
                            if (selected) {
                                me._doDownload(layer, downloadFilterer, resources, sEmail, outputFormat);
                            } else {
                                me._doDownload(layer, renderedFilterer, resources, sEmail, outputFormat);
                            }

                            //popup.close();
                        }
                    }//end of handler function

                }]
            }],
            listeners: {
                //After rendering - start the feature count loading
                afterrender: function(popup) {
                    if (me.enableFeatureCounts) {
                        var selected = popup.down('#klwfs-checkbox').getValue();
                        if (selected) {
                            me._updateFeatureCounts(layer, popup, resources, currentlyVisibleBBox);
                        } else {
                            me._updateFeatureCounts(layer, popup, resources, null);
                        }
                    }
                }
            }
        }).show();
    },

    _configureImageClickHandlers : function(c, eOpts, bbox) {
        var fireRender = function(bbox) {
            this.map.highlightBounds(bbox);
        };

        var fireScroll = function(bbox) {
            this.map.scrollToBounds(bbox);
        };

        c.getEl().on('click', Ext.bind(fireRender, this, [bbox], false), c);
        c.getEl().on('dblclick', Ext.bind(fireScroll, this, [bbox], false), c);
    },

    _handleRadioChange : function(radioGroup, newValue, oldValue, eOpts, currentBounds, resources, layer) {
        var popup = radioGroup.up('window');
        
        if (newValue) {
            this.map.scrollToBounds(currentBounds);
            this._updateFeatureCounts(layer, popup, resources, currentBounds);
        } else {
            this._updateFeatureCounts(layer, popup, resources, null);
        }
    },

    _doCheckRequest : function(email){

        var sUrl = '<iframe id="nav1" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="';
        sUrl += 'checkGMLDownloadStatus.do?';
        sUrl += "email=";
        sUrl += email;
        sUrl += '"></iframe>';

        var winDwld = new Ext.Window({
            autoScroll  : true,
            border      : true,
            html        : sUrl,
            id          : 'dwldWindow',
            layout      : 'fit',
            maximizable : true,
            modal       : true,
            plain       : false,
            title       : 'Check Status: ',
            height      : 120,
            width       : 500
          });

        winDwld.on('show',function(){
            winDwld.center();
        });
        winDwld.show();

    },
    
    _updateFeatureCount : function(layer, url, typeName, el, bbox) {
        //Override feature count callback if the known layer specifies it
        var countUrl = this.featureCountUrl;
        if (layer.get('sourceType') === portal.layer.Layer.KNOWN_LAYER) {
            var override = layer.get('source').get('proxyCountUrl');
            if (!Ext.isEmpty(override)) {
                countUrl = override;
            }
        }
        
        //Load filter details into our feature count request
        var params = layer.get('filterer').getParameters();
        params.serviceUrl = url;
        params.typeName = typeName;
        params.bbox = bbox ? Ext.JSON.encode(bbox) : '';
        
        el.setHtml('<img src="portal-core/img/dotdotdot.gif" width="16" height="16">'); //our loading placeholder
        portal.util.Ajax.request({
            url: countUrl,
            params: params,
            timeout: 5 * 60 * 1000, //5 minutes  
            callback: function(success, data) {
                if (!success) {
                    el.setHtml('Error');
                } else {
                    el.setHtml(data);
                }
            }
        });
    },
    
    _updateFeatureCounts : function(layer, popup, resources, bbox) {
        var els = popup.down('#klwfs-htmllabel').getEl().query('.klwfs-featurecount', false);
        var wfsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WFS);
        
        for (var i = 0; i < els.length; i++) {
            var url = wfsResources[i].get('url');
            var el = els[i];
            
            this._updateFeatureCount(layer, url, wfsResources[i].get('name'), el, bbox);
        }
    },

    _parseNotifcationString : function(cswRecords){

        var text = '<p>The portal will make a download request on your behalf and return the results as a zipped CSV file. ';
            text += 'To check the progress of your download and retrieve the file, click the "Check Status" button, using the email address that you used to start the download.</p>';
            text += '<p>We limit the results to 5000 features per access point. ';
            text += 'If you need more than 5000 features from any one data provider, you can download directly from the WFS service points below, or contact the data provider directly.</p>';
            text += "<p>Note: The links below are WFS service endpoints. Read <a href='http://docs.geoserver.org/latest/en/user/services/wfs/reference.html'>here</a> for more information </p>";




        text += '<div style="display:block;">';
        if (this.enableFeatureCounts) {
            text += '<div style="text-align:right;text-decoration:underline;">Total Features</div>';
        }

        for (var i = 0; i < cswRecords.length; i++) {
            var cswRecord = cswRecords[i];
            var resources = cswRecord.get('onlineResources');
            var wfsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WFS);
            for (var j = 0; j < wfsResources.length; j++) {
                var wfsResource = wfsResources[j];
                text += '<div style="display:block; height:25px;">';
                text += '<div style="display:inline-block;width:78%;">'
                text += this._generateWFSGetCapabilititesUrl(wfsResource.get('url'), cswRecord.get('adminArea'), cswRecord.get('contactOrg'));
                text += '</div>'
                if (this.enableFeatureCounts) {
                    text += '<div class="klwfs-featurecount" style="display:inline-block; text-align:center;width:80px;"></div>';
                }
                text += '</div>';
            }
            
        }
        text += '</div>';

        return text;


    },

    /**
     * Handles a download the specified set of online resources and filterer
     *
     * filterer - a portal.layer.filterer.Filterer
     * resources - an array portal.csw.OnlineResource
     */
    _doDownload : function(layer, filterer, resources, sEmail, outputFormat) {
        var renderer = layer.get('renderer');


        var email = sEmail;
        var downloadUrl=layer.get('source').get('proxyDownloadUrl');
        var proxyUrl = renderer.getProxyUrl();

        if(downloadUrl && downloadUrl.length > 0){
            proxyUrl = downloadUrl;
        } else {
            proxyUrl = (proxyUrl && proxyUrl.length > 0) ? proxyUrl : 'getAllFeatures.do';
        }

        var prefixUrl = portal.util.URL.base + proxyUrl + "?";


        var sUrl = '<iframe id="nav1" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="';

        sUrl += 'downloadGMLAsZip.do?';
        sUrl += "outputFormat=";
        sUrl += escape(outputFormat);
        sUrl += "&email=";
        sUrl += escape(email);


        //Iterate our WFS records and generate the array of PORTAL BACKEND requests that will be
        //used to proxy WFS requests. That array will be sent to a backend handler for making
        //multiple requests and zipping the responses into a single stream for the user to download.
        var wfsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WFS);
        for (var i = 0; i < wfsResources.length; i++) {
        	
        	//VT: if there is a service provider filter we only want to download from the service provider specified
        	if(filterer.parameters.serviceFilter && filterer.parameters.serviceFilter.length > 0
        			&& wfsResources[i].get('url')!= filterer.parameters.serviceFilter[0]){
        		continue;
        	}
            //Create a copy of the last set of filter parameters
            var url = wfsResources[i].get('url');
            var typeName = wfsResources[i].get('name');
            var filterParameters = filterer.getParameters();

            filterParameters.serviceUrl = url;
            filterParameters.typeName = typeName;
            filterParameters.maxFeatures = 5000;
            filterParameters.outputFormat = outputFormat;
            
            portal.util.GoogleAnalytic.trackevent('KLWFSDownloader', 'Url:' + url,'parameters:' + Ext.encode(filterer.getParameters()));


            sUrl += '&serviceUrls=' + escape(Ext.urlEncode(filterParameters, prefixUrl));
        }

        sUrl += '"></iframe>';

        var winDwld = new Ext.Window({
            autoScroll  : true,
            border      : true,
            html        : sUrl,
            id          : 'dwldWindow',
            layout      : 'fit',
            maximizable : true,
            modal       : true,
            plain       : false,
            title       : 'Download confirmation: ',
            height      : 200,
            width       : 840
          });

        winDwld.on('show',function(){
            winDwld.center();
        });
        winDwld.show();


    },

    _generateWFSGetCapabilititesUrl : function(url, adminArea, contactOrg) {
        var provider = adminArea;
        if (adminArea == 'ACT') {
            provider = contactOrg;
        }
        var params = {
            "request" : "GetCapabilities",
            "version" : "1.1.0",
            "service" : "WFS"
        };
        url = Ext.urlAppend(url, Ext.Object.toQueryString(params));
        return '<a href="' + url +'">' + provider + '</a>';
    }

});
