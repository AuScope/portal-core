/**
 * A knownlayer downloader that creates an Ext.Window specialised into showing a
 * dialog for the user to download features from a WFS in a zip file.
 * This differ from WFSDownloader as it engage the use of downloader tracker which
 * forces the user enter a email address and allow the user to check back later with
 * the download progress
 */
Ext.define('portal.layer.downloader.wfs.KLWFSDownloader', {
    extend: 'portal.layer.downloader.Downloader',

    statics : {
        DOWNLOAD_CURRENTLY_VISIBLE : 1,
        DOWNLOAD_ORIGINALLY_VISIBLE : 2,
        DOWNLOAD_ALL : 3
    },

    currentTooltip : null,

    constructor : function(cfg) {
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
     */
    downloadData : function(layer, resources, renderedFilterer, currentFilterer) {
        var me = this;
        var isDifferentBBox = false;
        var originallyVisibleBBox = null;
        var currentlyVisibleBBox = null;

        originallyVisibleBBox = renderedFilterer.getSpatialParam();
        currentlyVisibleBBox = currentFilterer.getSpatialParam();
        isDifferentBBox = originallyVisibleBBox && currentlyVisibleBBox &&
                          !originallyVisibleBBox.equals(currentlyVisibleBBox);

        //Create a popup showing our options
        Ext.create('Ext.Window', {
            title : 'Download Options',
            buttonAlign : 'right',
            modal : true,
            width : 550,
            height : 200,
            layout : {
                type : 'anchor'
                //align : 'stretch'
            },

            items : [{
                xtype : 'label',
                anchor : '100%',
                style : 'font-size: 12px;',
                text : 'The portal will make a download request on your behalf and return the results in a ZIP archive. How would you like the portal to filter your download?'
            },{
                xtype : 'fieldset',
                anchor : '100%',
                layout : 'fit',
                border : 0,
                items : [{
                    //Our radiogroup can see its item list vary according to the presence of bounding boxes
                    xtype : 'radiogroup',
                    //Forced to use fixed width columns
                    //see: http://www.sencha.com/forum/showthread.php?187933-Ext-4.1-beta-3-Incorrect-layout-on-Radiogroup-with-columns
                    //columns : [0.99, 18],
                    columns : [500, 18],
                    listeners : {
                        change : Ext.bind(this._handleRadioChange, this, [currentlyVisibleBBox, originallyVisibleBBox], true)
                    },
                    items : [{
                        boxLabel : 'Filter my download using the current visible map bounds.',
                        name : 'wfs-download-radio',
                        inputValue : portal.layer.downloader.wfs.WFSDownloader.DOWNLOAD_CURRENTLY_VISIBLE,
                        hidden : !isDifferentBBox,
                        checked : isDifferentBBox

                    },{
                        xtype : 'box',
                        autoEl : {
                            tag : 'img',
                            src : 'img/magglass.gif',
                            qtip : 'Click to display the spatial bounds, double click to pan the map so they are visible.'
                        },
                        width : 18,
                        height : 21,
                        hidden : !isDifferentBBox,
                        style : 'padding:3px 0px 0px 0px;',
                        listeners : {
                            render : Ext.bind(this._configureImageClickHandlers, this, [currentlyVisibleBBox], true)
                        }
                    },{
                        boxLabel : 'Filter my download using the original bounds that were used to load the layer.',
                        name : 'wfs-download-radio',
                        inputValue : portal.layer.downloader.wfs.WFSDownloader.DOWNLOAD_ORIGINALLY_VISIBLE,
                        checked : !isDifferentBBox && originallyVisibleBBox !== null,
                        hidden : originallyVisibleBBox === null
                    },{
                        xtype : 'box',
                        autoEl : {
                            tag : 'img',
                            src : 'img/magglass.gif',
                            qtip : 'Click to display the spatial bounds, double click to pan the map so they are visible.'
                        },
                        width : 18,
                        height : 21,
                        style : 'padding:3px 0px 0px 0px;',
                        hidden : originallyVisibleBBox === null,
                        listeners : {
                            render : Ext.bind(this._configureImageClickHandlers, this, [originallyVisibleBBox], true)
                        }
                    },{
                        boxLabel : 'Don\'t filter my download. Return all available data.',
                        name : 'wfs-download-radio',
                        inputValue : portal.layer.downloader.wfs.WFSDownloader.DOWNLOAD_ALL,
                        checked : !isDifferentBBox && originallyVisibleBBox === null
                    }]
                },{

                    xtype : 'fieldset',
                    anchor : '100%',
                    layout : 'fit',
                    border : 0,
                    items : [{
                        xtype           : 'textfield',
                        id              : 'downloadToken',
                        fieldLabel      : 'Email Address*',
                        emptyText       : 'Enter your email address',
                        name            : 'email',
                        selectOnFocus   : true,
                        allowBlank      : false,
                        blankText       : 'This field is required',
                        anchor          : '-50'
                    }]

                },{

                    xtype : 'toolbar',
                    border : false,
                    layout : {
                        type : 'hbox',
                        pack : 'end'
                    },
                    items : [{
                        xtype : 'button',
                        text: 'Check Status',
                        iconCls : 'info',
                        handler: function() {
                            var sEmail = Ext.getCmp('downloadToken').getValue();
                            if ( sEmail === '') {
                                Ext.MessageBox.alert('Unable to submit request...','Please Enter valid Email Address');
                                Ext.getCmp('downloadToken').markInvalid();
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
                            var sEmail = Ext.getCmp('downloadToken').getValue();
                            if ( sEmail === '') {
                                Ext.MessageBox.alert('Unable to submit request...','Please Enter valid Email Address');
                                Ext.getCmp('downloadToken').markInvalid();
                                return;
                            } else {
                                var bboxJson = '';
                                var popup = button.ownerCt.ownerCt.ownerCt;
                                var fieldSet = popup.items.getAt(1); //our second item is the fieldset
                                var radioGroup = fieldSet.items.getAt(0);
                                var checkedRadio = radioGroup.getChecked()[0]; //there should always be a checked radio

                                switch(checkedRadio.inputValue) {
                                case portal.layer.downloader.wfs.WFSDownloader.DOWNLOAD_CURRENTLY_VISIBLE:
                                    me._doDownload(layer, currentFilterer, resources, sEmail);
                                    break;
                                case portal.layer.downloader.wfs.WFSDownloader.DOWNLOAD_ORIGINALLY_VISIBLE:
                                    me._doDownload(layer, renderedFilterer, resources, sEmail);
                                    break;
                                default:
                                    me._doDownload(layer, renderedFilterer, resources, sEmail);
                                    break;
                                }

                                //popup.close();
                            }
                        }//end of handler function

                    }]
                }]
            }]
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

    _handleRadioChange : function(radioGroup, newValue, oldValue, eOpts, currentBounds, originalBounds) {
        switch(newValue['wfs-download-radio']) {
        case portal.layer.downloader.wfs.WFSDownloader.DOWNLOAD_CURRENTLY_VISIBLE:
            this.map.scrollToBounds(currentBounds);
            break;
        case portal.layer.downloader.wfs.WFSDownloader.DOWNLOAD_ORIGINALLY_VISIBLE:
            this.map.scrollToBounds(originalBounds);
            break;
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
            height      : 200,
            width       : 840
          });

        winDwld.on('show',function(){
            winDwld.center();
        });
        winDwld.show();

    },

    /**
     * Handles a download the specified set of online resources and filterer
     *
     * filterer - a portal.layer.filterer.Filterer
     * resources - an array portal.csw.OnlineResource
     */
    _doDownload : function(layer, filterer, resources, sEmail) {
        var renderer = layer.get('renderer');


        var email = sEmail;

        var proxyUrl = renderer.getProxyUrl();
        proxyUrl = (proxyUrl && proxyUrl.length > 0) ? proxyUrl : 'getAllFeatures.do';
        var prefixUrl = portal.util.URL.base + proxyUrl + "?";


        var sUrl = '<iframe id="nav1" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="';
        sUrl += 'downloadGMLAsZip.do?';
        sUrl += "email=";
        sUrl += email;




        //Iterate our WFS records and generate the array of PORTAL BACKEND requests that will be
        //used to proxy WFS requests. That array will be sent to a backend handler for making
        //multiple requests and zipping the responses into a single stream for the user to download.
        var wfsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WFS);
        for (var i = 0; i < wfsResources.length; i++) {
            //Create a copy of the last set of filter parameters
            var url = wfsResources[i].get('url');
            var typeName = wfsResources[i].get('name');
            var filterParameters = filterer.getParameters();

            filterParameters.serviceUrl = url;
            filterParameters.typeName = typeName;
            filterParameters.maxFeatures = 0;


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


    }
});