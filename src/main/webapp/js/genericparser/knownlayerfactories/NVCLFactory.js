/**
 * A factory for parsing WFS features from the National Virtual Core Library known layer.
 */
Ext.ns('GenericParser.KnownLayerFactory');
GenericParser.KnownLayerFactory.NVCLFactory = Ext.extend(GenericParser.KnownLayerFactory.BaseFactory, {

    constructor : function(cfg) {
        GenericParser.KnownLayerFactory.NVCLFactory.superclass.constructor.call(this, cfg);
    },

    /**
     * Overrides abstract supportsKnownLayer. Supports only the NVCL known layer
     */
    supportsKnownLayer : function(knownLayer) {
        return knownLayer.getId() === 'nvcl-borehole';
    },


    /**
     * Shows the NVCL dataset display window for the given dataset (belonging to the selected known layer feature)
     *
     * Note - AUS-2055 brought about the removal of the requirement for an open proxy - work still needs to be done
     *        to break this into more manageable pieces because the code is still very much a copy from the original source.
     */
    showDetailsWindow : function(datasetId, datasetName, omUrl, nvclDataServiceUrl, featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource) {

        //We create an instance of our popup window but don't show it immediately
        //We need to dynamically add to its contents
        var win = new Ext.Window({
            border      : true,
            layout      : 'fit',
            resizable   : false,
            modal       : true,
            plain       : false,
            title       : 'Borehole Id: '+ datasetName,
            height      : 600,
            width       : 820,
            items:[{
                xtype           : 'tabpanel',
                activeItem      : 0,
                enableTabScroll : true,
                buttonAlign     : 'center',
                items           : []
            }]
        });
        var tp = win.items.itemAt(0); // store a reference to our tab panel for easy access

        //This store is for holding log info
        var logStore = new Ext.data.Store({
            proxy : new Ext.data.HttpProxy(new Ext.data.Connection({
                url : 'getNVCLLogs.do',
                extraParams : {
                    serviceUrl : nvclDataServiceUrl,
                    datasetId : datasetId,
                    mosaicService : true
                }
            })),
            reader : new Ext.data.JsonReader({
                idProperty : 'logId',
                root : 'data',
                successProperty : 'success',
                messageProperty : 'msg',
                fields : [
                    {name : 'logId'},
                    {name : 'logName'},
                    {name : 'sampleCount'}
                ]
            })
        });

        //Load our log store, populate our window when it finishes loading
        logStore.load({
            callback : function(recs, options, success) {
                //Find our 'mosaic' and 'imagery' record
                var mosaicRecord = null;
                var imageryRecord = null;
                for (var i = 0; i < recs.length; i++) {
                    switch(recs[i].get('logName')) {
                    case 'Mosaic':
                        mosaicRecord = recs[i];
                        break;
                    case 'Imagery':
                        imageryRecord = recs[i];
                        break;
                    }
                }

                //Add our mosaic tab (if available)
                if (mosaicRecord != null) {
                    tp.add({
                        title : ' Mosaic ',
                        layout : 'fit',
                        html: '<iframe id="nav" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="' +
                              'getNVCLMosaic.do?serviceUrl=' + escape(nvclDataServiceUrl) + '&logId=' + mosaicRecord.get('logId') +
                              '"></iframe>'
                    });
                }

                //Add our imagery tab (if available)
                if (imageryRecord != null) {
                    var startSampleNo   = 0;
                    var endSampleNo     = 100;
                    var sampleIncrement = 100;
                    var totalCount      = imageryRecord.get('sampleCount');

                    //Navigation function for Imagery tab
                    var cardNav = function(incr) {

                        if ( startSampleNo >= 0 && endSampleNo >= sampleIncrement) {
                            startSampleNo = 1 * startSampleNo + incr;
                            endSampleNo = 1 * endSampleNo + incr;
                            Ext.getCmp('card-prev').setDisabled(startSampleNo < 1);
                            Ext.get('imageryFrame').dom.src = 'getNVCLMosaic.do?serviceUrl=' + escape(nvclDataServiceUrl) + '&logId=' + imageryRecord.get('logId') + '&startSampleNo=' + startSampleNo + '&endSampleNo=' + endSampleNo;

                            // Ext.fly ... does not work in IE7
                            //Ext.fly('display-count').update('Displayying Images: ' + startSampleNo + ' - ' + endSampleNo + ' of ' + totalCount);
                            Ext.getCmp('display-count').setText('Displaying Imagess: ' + startSampleNo + ' - ' + endSampleNo + ' of ' + totalCount);
                        }

                        Ext.getCmp('card-prev').setDisabled(startSampleNo <= 0);
                        Ext.getCmp('card-next').setDisabled(startSampleNo + sampleIncrement >= totalCount);
                    };

                    //Add the actual tab
                    tp.add({
                        title : ' Imagery ',
                        layout : 'fit',
                        html : '<iframe id="imageryFrame" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="' +
                              'getNVCLMosaic.do?serviceUrl=' + escape(nvclDataServiceUrl) + '&logId=' + imageryRecord.get('logId') +
                              '&startSampleNo='+ startSampleNo +
                              '&endSampleNo=' + sampleIncrement +
                              '"></iframe>',
                        bbar: [{
                            id   : 'display-count',
                            text : 'Displaying Images: ' + startSampleNo + ' - ' + endSampleNo + ' of ' + totalCount
                        },
                        '->',
                        {
                            id  : 'card-prev',
                            text: '< Previous',
                            disabled: true,
                            handler: cardNav.createDelegate(this, [-100])
                        },{
                            id  : 'card-next',
                            text: 'Next >',
                            handler: cardNav.createDelegate(this, [100])
                        }]
                   });
                }

                //Add our scalars tab (this always exists
                var CheckBox = new Ext.grid.CheckboxSelectionModel();
                var scalarGrid = new Ext.grid.GridPanel({
                    //This store will be loaded when this component renders
                    store           : new Ext.data.Store({
                        proxy : new Ext.data.HttpProxy(new Ext.data.Connection({
                            url : 'getNVCLLogs.do',
                            extraParams : {
                                serviceUrl : nvclDataServiceUrl,
                                datasetId : datasetId
                            }
                        })),
                        reader : new Ext.data.JsonReader({
                            idProperty : 'logId',
                            root : 'data',
                            successProperty : 'success',
                            messageProperty : 'msg',
                            fields : [
                                {name : 'logId'},
                                {name : 'logName'}
                            ]
                        })
                    }),
                    sm : CheckBox,
                    autoExpandColumn : 'log-name-col',
                    id : 'nvcl-scalar-grid',
                    height : 450,
                    width : 200,
                    loadMask : true,
                    columns : [
                        CheckBox,
                    {
                        header: "ID", width: 50, dataIndex: 'LogID',
                        sortable: true, hidden: true
                    },{
                        id: 'log-name-col',
                        header: "Scalar", width: 100, dataIndex: 'logName',
                        sortable: true
                    }],
                    listeners : {
                        //Load our store on render
                        afterrender : function(grid) {
                            grid.getStore().load();
                        },
                        //Setup tool tips
                        render : function(grid) {
                            var view = grid.getView();    // Capture the GridView.

                            var autoWidth = !Ext.isIE6 && !Ext.isIE7;
                            var width = autoWidth ? undefined : 250;

                            scalarGrid.tip = new Ext.ToolTip({
                                target: view.mainBody,    // The overall target element.
                                delegate: '.x-grid3-row', // Each grid row causes its own seperate show and hide.
                                trackMouse: true,         // Moving within the row should not hide the tip.
                                renderTo: document.body,  // Render immediately so that tip.body can be referenced prior to the first show.
                                autoWidth: autoWidth,
                                width: width,
                                listeners: {              // Change content dynamically depending on which element triggered the show.
                                    beforeshow: function updateTipBody(tip) {
                                        var grid = Ext.getCmp('nvcl-scalar-grid');
                                        var store = grid.getStore();  // Capture the Store.
                                        var view = grid.getView();    // Capture the GridView.
                                        var rowIndex = view.findRowIndex(tip.triggerElement);
                                        var record = store.getAt(rowIndex);

                                        tip.body.dom.innerHTML = "Loading...";

                                        //Load our vocab string asynchronously
                                        var vocabsQuery = 'getScalar.do?repository=nvcl-scalars&label=' + escape(record.get('logName').replace(' ', '_'));
                                        Ext.Ajax.request({
                                            url:vocabsQuery,
                                            success: function(pData, options) {
                                            var pResponseCode=pData.status;
                                            if(pResponseCode != 200) {
                                                tip.body.dom.innerHTML = "ERROR: " + pResponseCode;
                                                return;
                                            }

                                            var response = eval('(' + pData.responseText + ')');
                                            if (!response.success) {
                                                tip.body.dom.innerHTML = "ERROR: server returned error";
                                                return;
                                            }

                                            //Update tool tip
                                            if (response.data.definition && response.data.definition.length > 0)
                                                tip.body.dom.innerHTML = response.data.definition;
                                            else if (response.data.scopeNote && response.data.scopeNote.length > 0)
                                                tip.body.dom.innerHTML = response.data.scopeNote;
                                            else
                                                tip.body.dom.innerHTML = 'N/A';
                                          }});
                                    }
                                    //TODO: Commenting this out for now as it causes js errors. Still need to
                                    // test whether there is a better solution.
                                    //hide : function(component) {
                                    //    component.destroy();
                                    //}
                                }
                            });
                        }
                    }
                });

                // Scalars Tab
                tp.add({
                    title : 'Scalars',
                    layout : 'fit',
                    border : false,
                    items : {
                        // Bounding form
                        id :'scalarsForm',
                        xtype :'form',
                        layout :'column',
                        frame : true,

                        // these are applied to columns
                        defaults:{
                            columnWidth : 0.5,
                            layout      : 'form',
                            hideLabels  : true,
                            border      : false,
                            bodyStyle   : 'padding:10px',
                            labelWidth  : 100
                        },

                        // Columns
                        items:[{ // column 1
                            // these are applied to fieldsets
                            defaults:{xtype:'fieldset', layout:'form', anchor:'100%', autoHeight:true},

                            // fieldsets
                            items:[{
                                title       : 'List of Scalars',
                                defaultType : 'textfield',

                                // these are applied to fields
                                defaults    : {anchor:'-5', allowBlank:false},

                                // fields
                                items       : [scalarGrid]
                            }]
                        },{ // column 2
                            // these are applied to fieldsets
                            defaults:{
                                xtype: 'fieldset',
                                layout: 'form',
                                anchor: '100%',
                                autoHeight: true,
                                paddingRight: '10px'
                            },

                            // fieldsets
                            items:[{
                                title       : 'Hint',
                                defaultType : 'textfield',
                                // fields
                                items:[{
                                    xtype  : 'box',
                                    id     : 'scalarFormHint',
                                    autoEl : {
                                        tag  : 'div',
                                        html : 'Select a scalar(s) from the "Scalar List" table on the left and then click "Plot" button.<br><br>Leave the default depth values for the entire depth.'
                                    }
                                }]
                            },{
                                title       : 'Options',
                                defaultType : 'textfield',

                                // these are applied to fields
                                //,defaults:{anchor:'-20', allowBlank:false}
                                bodyStyle   : 'padding:0 0 0 45px',

                                // fields
                                items:[{
                                    xtype       : 'spinnerfield',
                                    fieldLabel  : 'Start Depth (m)',
                                    name        : 'startDepth',
                                    minValue    : 0,
                                    value       : 0,
                                    accelerate  : true
                                },{
                                    xtype       : 'spinnerfield',
                                    fieldLabel  : 'End Depth (m)',
                                    name        : 'endDepth',
                                    minValue    : 0,
                                    value       : 99999,
                                    accelerate  : true
                                },{
                                    xtype                   : 'spinnerfield',
                                    fieldLabel              : 'Interval (m)',
                                    name                    : 'samplingInterval',
                                    minValue                : 0,
                                    value                   : 1.0,
                                    allowDecimals           : true,
                                    decimalPrecision        : 1,
                                    incrementValue          : 0.1,
                                    alternateIncrementValue : 2.1,
                                    accelerate              : true
                                }]
                            },{
                                xtype       : 'fieldset',
                                title       : 'Graph Types',
                                autoHeight  : true,
                                items       :[{
                                    xtype   : 'radiogroup',
                                    id      : 'ts1',
                                    columns : 1,
                                    items   : [
                                        {boxLabel: 'Stacked Bar Chart', name: 'graphType', inputValue: 1, checked: true},
                                        {boxLabel: 'Scattered Chart', name: 'graphType', inputValue: 2},
                                        {boxLabel: 'Line Chart', name: 'graphType', inputValue: 3}
                                    ]
                                }]

                            }],
                            buttons:[{
                                text: 'Plot',
                                handler: function() {
                                    var sHtml = '';
                                    var item_count = scalarGrid.getSelectionModel().getCount();
                                    var scalarForm = Ext.getCmp('scalarsForm').getForm();

                                    if (item_count > 0) {
                                        var s = scalarGrid.getSelectionModel().getSelections();
                                        for(var i = 0, len = s.length; i < len; i++){
                                            sHtml +='<img src="';
                                            sHtml += 'getNVCLPlotScalar.do?logId=';
                                            sHtml += s[i].data.logId;
                                            sHtml += '&' + scalarForm.getValues(true);
                                            sHtml += '&width=300';
                                            sHtml += '&height=600';
                                            sHtml += '&serviceUrl=';
                                            sHtml += escape(nvclDataServiceUrl)
                                            sHtml += '"/>';
                                        }

                                        var winPlot = new Ext.Window({
                                            autoScroll  : true,
                                            border      : true,
                                            html        : sHtml,
                                            id          : 'plWindow',
                                            layout      : 'fit',
                                            maximizable : true,
                                            modal       : true,
                                            plain       : false,
                                            title       : 'Plot: ',
                                            autoHeight  : true,
                                            autoWidth   : true,
                                            x           : 10,
                                            y           : 10
                                          });

                                        winPlot.show();
                                    } else if (item_count === 0){
                                        Ext.Msg.show({
                                            title:'Hint',
                                            msg: 'You need to select a scalar(s) from the "List of Scalars" table to plot.',
                                            buttons: Ext.Msg.OK
                                        });
                                    }
                                }
                            }]
                        }]
                    }
                });

                if (mosaicRecord != null || imageryRecord != null) {
                    win.show();
                    win.center();
                } else {
                    Ext.MessageBox.alert('Info', 'Selected dataset is empty!');
                }
            }
        });
    },

    /**
     * Shows the NVCL dataset download window for the given dataset (belonging to the selected known layer feature)
     *
     * Note - AUS-2055 brought about the removal of the requirement for an open proxy - work still needs to be done
     *        to break this into more manageable pieces because the code is still very much a copy from the original source.
     */
    showDownloadWindow : function(datasetId, datasetName, omUrl, nvclDownloadServiceUrl, featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource) {
        // Dataset download window
        var win = new Ext.Window({
            border          : true,
            layout          : 'fit',
            resizable       : false,
            modal           : true,
            plain           : false,
            title           : 'Borehole Id:  '+ datasetName,
            height          : 400,
            width           : 500,
            items:[{
                // Bounding form
                id      :'nvclDownloadFrm',
                xtype   :'form',
                layout  :'form',
                frame   : true,

                // these are applied to columns
                defaults:{
                    xtype: 'fieldset', layout: 'form'
                },

                // fieldsets
                items   :[{
                    title       :'Hint',
                    defaultType :'textfield',

                    // fields
                    items:[{
                        xtype  : 'box',
                        autoEl : {
                            tag  : 'div',
                            html : 'This data is best viewed in TSG Viewer. Freely available from <a href="http://www.thespectralgeologist.com/tsg_viewer.htm">here</a>.'
                        }
                    }]
                },{
                    xtype   :'hidden',
                    name    :'datasetId', //name of the field sent to the server
                    value   : datasetId  //value of the field
                },{
                    id              : 'csvFldSet',
                    title           : 'Comma-separated values (CSV) file',
                    checkboxName    : 'csv',
                    checkboxToggle  : true,
                    collapsed       : true,
                    buttonAlign     : 'right',
                    buttons:[{
                        text : 'Download',
                        xtype: 'linkbutton',
                        href : String.format('getNVCLCSVDownload.do?serviceUrl={0}&datasetId={1}', escape(omUrl), datasetId)
                    }],
                    listeners: {
                        'expand' : {
                            scope: this,
                            fn : function(panel) {
                                Ext.getCmp('tsgFldSet').collapse();
                                Ext.getCmp('omFldSet').collapse();
                            }
                        }
                    }
                },{
                    id              : 'tsgFldSet',
                    title           : 'Spectral Geologist (TSG) data file',
                    checkboxName    : 'tsg',
                    checkboxToggle  : true,
                    defaultType     : 'textfield',
                    bodyStyle       : 'padding: 0 0 0 50px',
                    items:[{
                        id              : 'tsgEmailAddress',
                        xtype           : 'textfield',
                        fieldLabel      : 'Email Address*',
                        value           : 'Your.Name@csiro.au',
                        name            : 'email',
                        selectOnFocus   : true,
                        allowBlank      : false,
                        anchor          : '-50'
                    },{
                        xtype: 'checkboxgroup',
                        fieldLabel: 'Options',
                        columns: [100,100],
                        vertical: true,
                        items: [
                            {boxLabel: 'linescan', name: 'lineScan', inputValue: 'true', checked: true},
                            {boxLabel: 'spectra', name: 'spectra', inputValue: 'true', checked: true},
                            {boxLabel: 'profilometer', name: 'profilometer', inputValue: 'true', checked: true},
                            {boxLabel: 'traypics', name: 'trayPics', inputValue: 'true', checked: true},
                            {boxLabel: 'mospic', name: 'mosaicPics', inputValue: 'true', checked: true},
                            {boxLabel: 'mappics', name: 'mapPics', inputValue: 'true', checked: true}
                        ]
                    }],
                    buttonAlign:'right',
                    buttons:[{
                        text: 'Check Status',
                        handler: function() {
                            var sEmail = Ext.getCmp('tsgEmailAddress').getValue();
                            if (sEmail == 'Your.Name@csiro.au' || sEmail == '') {
                                Ext.MessageBox.alert('Unable to submit request...','Please Enter valid Email Address');
                                Ext.getCmp('tsgEmailAddress').markInvalid();
                                return;
                            } else {
                                Ext.getCmp('omEmailAddress').setValue(sEmail);

                                var winStat = new Ext.Window({
                                    autoScroll  : true,
                                    border      : true,
                                    autoLoad    : String.format('getNVCLTSGDownloadStatus.do?email={0}&serviceUrl={1}', escape(sEmail), escape(nvclDownloadServiceUrl)),
                                    id          : 'dwldStatusWindow',
                                    layout      : 'fit',
                                    modal       : true,
                                    plain       : false,
                                    title       : 'Download status: ',
                                    height      : 400,
                                    width       : 1200
                                  });

                                winStat.on('show',function(){
                                    winStat.center();
                                });
                                winStat.show();
                            }
                        }
                    },{
                        text: 'Download',
                        handler: function(button) {
                            var sUrl = '';
                            var sEmail = Ext.getCmp('tsgEmailAddress').getValue();
                            if (sEmail == 'Your.Name@csiro.au' || sEmail == '') {
                                Ext.MessageBox.alert('Unable to submit request...','Please Enter valid Email Address');
                                Ext.getCmp('tsgEmailAddress').markInvalid();
                                return;
                            } else {
                                Ext.getCmp('omEmailAddress').setValue(sEmail);
                                var downloadForm = Ext.getCmp('nvclDownloadFrm').getForm();
                                sUrl += '<iframe id="nav1" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="';
                                sUrl += 'getNVCLTSGDownload.do?';
                                sUrl += downloadForm.getValues(true);
                                sUrl += '&serviceUrl=' + escape(nvclDownloadServiceUrl);
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
                        }
                    }],
                    listeners: {
                        'expand' : {
                            scope: this,
                            fn : function(panel, anim) {
                                Ext.getCmp('csvFldSet').collapse();
                                Ext.getCmp('omFldSet').collapse();
                            }
                        }
                    }
                },{
                    id              : 'omFldSet',
                    title           : 'Observations and Measurements',
                    checkboxName    : 'om',
                    checkboxToggle  : true,
                    collapsed       : true,
                    bodyStyle       : 'padding: 0 0 0 50px',
                    items:[ {
                        id            : 'omEmailAddress',
                        xtype         : 'textfield',
                        fieldLabel    : 'Email Address*',
                        value         : 'Your.Name@csiro.au',
                        name          : 'email',
                        selectOnFocus : true,
                        allowBlank    : false,
                        anchor        : '-50'
                    }],
                    buttonAlign     : 'right',
                    buttons:[{
                        text    : 'Check Status',
                        handler : function() {
                            var sEmail = Ext.getCmp('omEmailAddress').getValue();
                            if (sEmail == 'Your.Name@csiro.au' || sEmail == '') {
                                Ext.MessageBox.alert('Unable to submit request...','Please Enter valid Email Address');
                                Ext.getCmp('omEmailAddress').markInvalid();
                                return;
                            } else {
                                Ext.getCmp('tsgEmailAddress').setValue(sEmail);
                                var winStat = new Ext.Window({
                                    autoScroll  : true,
                                    border      : true,
                                    autoLoad    : String.format('getNVCLWFSDownloadStatus.do?email={0}&serviceUrl={1}', escape(sEmail), escape(nvclDownloadServiceUrl)),
                                    id          : 'omDwldStatusWindow',
                                    layout      : 'fit',
                                    modal       : true,
                                    plain       : false,
                                    title       : 'Download status: ',
                                    height      : 400,
                                    width       : 1200
                                  });

                                winStat.on('show',function(){
                                    winStat.center();
                                });
                                winStat.show();
                            }
                        }
                    },{
                        text : 'Download',
                        handler: function(button) {
                            var sUrl = '';
                            var sEmail = Ext.getCmp('omEmailAddress').getValue();
                            if (sEmail == 'Your.Name@csiro.au' || sEmail == '') {
                                Ext.MessageBox.alert('Unable to submit request...','Please Enter valid Email Address');
                                Ext.getCmp('omEmailAddress').markInvalid();
                                return;
                            } else {
                                Ext.getCmp('tsgEmailAddress').setValue(sEmail);
                                var downloadForm = Ext.getCmp('nvclDownloadFrm').getForm();
                                sUrl += '<iframe id="nav1" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="';
                                sUrl += 'getNVCLWFSDownload.do?';
                                sUrl += 'boreholeId=sa.samplingfeaturecollection.' + datasetId;
                                sUrl += '&omUrl=' + escape(omUrl);
                                sUrl += '&serviceUrl=' + escape(nvclDownloadServiceUrl);
                                sUrl += '&typeName=sa:SamplingFeatureCollection';
                                sUrl += '&email=' + escape(sEmail);
                                sUrl += '"></iframe>';
                                //alert(sUrl);

                                var winDwld = new Ext.Window({
                                    autoScroll  : true,
                                    border      : true,
                                    //autoLoad  : sUrl,
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
                        }
                    }],
                    listeners: {
                        'expand' : {
                            scope: this,
                            fn : function(panel) {
                                Ext.getCmp('csvFldSet').collapse();
                                Ext.getCmp('tsgFldSet').collapse();
                            }
                        }
                    }
                }]
            }]
        });

        win.show();
    },

    /**
     * Overrides abstract parseKnownLayerFeature
     */
    parseKnownLayerFeature : function(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource, rootCfg) {
        var me = this;

        //NVCL URL's are discovered by doing some 'tricky' URL rewriting
        var baseUrl = this.getBaseUrl(parentOnlineResource.url);
        if (baseUrl.indexOf('pir.sa.gov.au') >= 0) {
            baseUrl += '/nvcl'; //AUS-2144 - PIRSA specific fix
        }

        var nvclDataServiceUrl = baseUrl + '/NVCLDataServices/';
        var nvclDownloadServiceUrl = baseUrl + '/NVCLDownloadServices/';

        Ext.apply(rootCfg, {
            plain : true,
            //We only have a single child which is our grid
            items : [{
                xtype : 'grid',
                title : 'Available Datasets',
                border : false,
                //This is for holding our dataset information
                store : new Ext.data.Store({
                    proxy : new Ext.data.HttpProxy(new Ext.data.Connection({
                        url : 'getNVCLDatasets.do',
                        extraParams : {
                            serviceUrl : nvclDataServiceUrl,
                            holeIdentifier : featureId.replace('gsml.borehole.', '')
                        }
                    })),
                    reader : new Ext.data.JsonReader({
                        idProperty : 'datasetId',
                        root : 'data',
                        successProperty : 'success',
                        messageProperty : 'msg',
                        fields : [
                            {name : 'datasetId'},
                            {name : 'datasetName'},
                            {name : 'omUrl'}
                        ]
                    })
                }),
                loadMask : true,
                hideHeaders : true,
                //Show just the dataset names
                columns : [{
                    id : 'name',
                    header : 'Name',
                    dataIndex : 'datasetName',
                    width : 600,
                    renderer : function(value, p, record) {
                        return String.format('<div><b>{0}</b></div>', value);
                    }
                }],
                //We only want single row selection
                sm: new Ext.grid.RowSelectionModel({
                    singleSelect:true
                }),
                listeners : {
                    //When our grid is rendered - load the datastore and select the first row
                    afterrender : function(grid) {
                        grid.getStore().load({
                            callback : function() {
                                var sm = grid.getSelectionModel();
                                if (sm) {
                                    sm.selectFirstRow();
                                }
                            }
                        });
                    }
                },
                viewConfig : {
                    forceFit : true,
                    enableRowBody:true,
                    showPreview:true,
                    getRowClass : function(record, rowIndex, p, ds){
                        if(this.showPreview){
                            var description = record.data.datasetId;
                            var maxLength = 190;
                            if (description.length > maxLength) {
                                description = description.substring(0, maxLength) + '...';
                            }
                            p.body = '<p style="margin:5px 10px 10px 25px;color:#555;">'+description+'</p>';
                            return 'x-grid3-row-expanded';
                        }
                        return 'x-grid3-row-collapsed';
                    }
                },
                buttonAlign : 'right',
                buttons : [{
                    iconCls : 'info',
                    text : 'Display',
                    handler : function(button, e) {
                        var grid = button.ownerCt.ownerCt;
                        var selectedRec = grid.getSelectionModel().getSelected();
                        if (selectedRec) {
                            me.showDetailsWindow(selectedRec.get('datasetId'),
                                    selectedRec.get('datasetName'),
                                    selectedRec.get('omUrl'),
                                    nvclDataServiceUrl,
                                    featureId,
                                    parentKnownLayer,
                                    parentCSWRecord,
                                    parentOnlineResource);
                        }
                    }
                },{
                    iconCls : 'download',
                    text : 'Download',
                    handler : function(button, e) {
                        var grid = button.ownerCt.ownerCt;
                        var selectedRec = grid.getSelectionModel().getSelected();
                        if (selectedRec) {
                            me.showDownloadWindow(selectedRec.get('datasetId'),
                                    selectedRec.get('datasetName'),
                                    selectedRec.get('omUrl'),
                                    nvclDownloadServiceUrl,
                                    featureId,
                                    parentKnownLayer,
                                    parentCSWRecord,
                                    parentOnlineResource);
                        }
                    }
                }]
            }]
        });

        return new GenericParser.BaseComponent(rootCfg);
    }
});