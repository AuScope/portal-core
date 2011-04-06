/**
 * @class NvclInfoWindow
 * <p>Nvcl marker info window pop-up</p>
 *
 * @version $Id$
 */

/**
 * @constructor
 * @param {GMap2}   iMap
 * @param {GMarker} iMarker
 */
function NvclInfoWindow(iMap, iMarker, wfsUrl) {

    this.Map = iMap;
    this.Marker = iMarker;
    this.tabsArray = [];                            // Window Tabs
    this.boreholeId = iMarker.title || "";
    this.summaryHtml = iMarker.description || "";
    this.waitHtml =
        '<div>' +
            '<b>' + this.boreholeId + '</b>' +
            '<p style="text-align:center;">' +
                '<img src="img/wait.gif" style="padding-top:50px;" />' +
            '</p>' +
        '</div>';
    this.wfsServiceUrl = wfsUrl;
    /**
     * iMarker.wfsUrl comes from GeoNetwork and represents GeoServer's service
     * Url. From this Url we remove pathname and return only protocol with
     * hostname eg.: http://server.com.au/nvcl/wfs --> http://server.com.au
     */
    this.geoServerUrl = (function(url) {
        var str = url.slice( ("http://").length);
        return 'http://' + str.slice(0,str.indexOf("/"));
    })(wfsUrl);
}

NvclInfoWindow.prototype = {

    'TAB_1' : "Summary",

    'TAB_2' : "Details",

    'NVCL_SERVICE' : "/NVCLDataServices/getDatasetCollection.html?holeidentifier=",

    'show': function() {
		//Open our window with the basic info displayed
		this.tabsArray[0] = new GInfoWindowTab(this.TAB_1, this.summaryHtml);

		var me = this;

        this.Marker.openInfoWindowTabs(this.tabsArray, {
        	onOpenFn:function(){
                //And update it with the downloaded data as it arrives
                me.retrieveDatasets();
                }
        });

    },

    /*
     * Returns datasets for the selected borehole
     */
    'retrieveDatasets' : function() {

        var me = this;
        var serverAddr = this.geoServerUrl;
        var url = ProxyURL + serverAddr + this.NVCL_SERVICE + this.Marker.title;

        var myMask = new Ext.LoadMask(Ext.get('center_region'), {msg:"Please wait..."});
        myMask.show();


        GDownloadUrl(url, function(response, responseCode) {
            if (responseCode == 200) {
                var XmlDoc = GXml.parse(response);

                if (g_IsIE) {
                    XmlDoc.setProperty("SelectionLanguage", "XPath");
                }

                var rootNode = XmlDoc.documentElement;
                if (!rootNode) {
                    return;
                }

                // get the dataset tag (inside is the info we need)
                var aDataset = rootNode.getElementsByTagName("Dataset");

                // Loop over the Datatests and get DatasetId : DatasetName pairs
                if (aDataset.length > 0) {

                    // Dataset Collection of key : value pairs
                    // 6dd70215-fe38-457c-be42-3b165fd98c7 : {datasetName : WTB5, omUrl : http://something/wfs}
                    var datasetCol = new Ext.util.MixedCollection();
                    var aId, aName;

                    for (var i=0; i < aDataset.length; i++ ) {
                         aId = GXml.value(aDataset[i].selectSingleNode("*[local-name() = 'DatasetID']"));
                         aName = GXml.value(aDataset[i].selectSingleNode("*[local-name() = 'DatasetName']"));
                         omUrl = GXml.value(aDataset[i].selectSingleNode("*[local-name() = 'OmUrl']"));

                        datasetCol.add(aId, {
                            datasetName : aName,
                            omUrl : omUrl});
                    }

                    // Get tab content
                    var sHtml = me.createDetailsTabHtml(datasetCol);

                    // Create new tab
                    me.tabsArray[1] = new GInfoWindowTab(me.TAB_2, sHtml);

                    // Add new tab to pop-up window
                    me.Map.updateInfoWindow(me.tabsArray);
                }
                myMask.hide();

            } else if(responseCode == -1) {
                myMask.hide();
                alert("Data request timed out. Please try later.");
            } else if ((responseCode >= 400) & (responseCode < 500)){
                myMask.hide();
                alert('Request not found, bad request or similar problem. Error code is: ' + responseCode);
            } else if ((responseCode >= 500) & (responseCode <= 506)){
                myMask.hide();
            }else {
                myMask.hide();
                alert('Remote server returned error code: ' + responseCode);
            }
        });
    },

    /*
     * Creates 'Details' tab markup based on an array of datasets
     * @param {MixedCollection} Collection of DatasetId : DatasetName pairs
     * @return {String} Html markup
     */
    'createDetailsTabHtml' : function(datasetCol) {

        var lHtml = "";

        if (datasetCol.getCount() > 0) {
            lHtml += '<div style="padding:20px;" >' +
                        '<p> Available data sets: </p>' +
                     '</div>' +
                     '<div align="center">' +
                        '<form method="post">' +
                           '<select name="selDataset" id="selDataset" size="5" style="width:200px;">';

            datasetCol.eachKey( function(key,item) {
                var datasetDetails = {
                    datasetId : key,
                    datasetName : item.datasetName,
                    omUrl : item.omUrl
                };
                
                if (datasetCol.indexOfKey(key) === 0) {
                    lHtml += '<option value="'+ escape(Ext.util.JSON.encode(datasetDetails)) +'" selected="selected">'+ item.datasetName + ' (Latest)</option>';
                } else {
                    lHtml += '<option value="'+ escape(Ext.util.JSON.encode(datasetDetails)) +'">'+ item.datasetName +'</option>';
                }
            });

            lHtml +=       '</select>' +
                           '<div align="right">' +
                              '<br>' +
                              '<input type="button" id="displayDatasetBtn"  value="Display" name=butSelectDataset onclick="showBoreholeDetails(\''+ this.geoServerUrl +'\', Ext.util.JSON.decode(unescape(this.form.selDataset.value)));">' +
                              '&#160;' +
                              '&#160;' +
                              '<input type="button" id="downloadDatasetBtn" value="Download" name=butDownloadDataset onclick="showDownloadDetails(\''+ this.wfsServiceUrl +'\',\''+ this.geoServerUrl +'\', Ext.util.JSON.decode(unescape(this.form.selDataset.value)))">' +
                           '</div>' +
                        '</form>' +
                     '</div>';
        }
        return lHtml;
    }

}; // End of NvclInfoWindow.prototype


/**
 * Static method called from google map's info window to open a
 * new Ext JS window with borehole details
 *
 * @param {String} iServerUrl
 * @param {String} iDatasetDetails - an object with datasetName, datasetId and omUrl set.
 */
function showBoreholeDetails(iServerUrl, iDatasetDetails) {

    Ext.QuickTips.init();

    var lDatasetId   = iDatasetDetails.datasetId.replace(/'/g, '');
    var lDatasetName = iDatasetDetails.datasetName;
    var lOMUrl = iDatasetDetails.omUrl;
    
    var LOG_PATH    = '/NVCLDataServices/getLogCollection.html?datasetid=';
    var MOSAIC_PATH = '/NVCLDataServices/mosaic.html?logid=';
    var PLOT_PATH   = '/NVCLDataServices/plotscalar.html?logid=';

    var aLogMosaic  = getImageLog(iServerUrl, lDatasetId, "Mosaic");
    var aLog        = getImageLog(iServerUrl, lDatasetId, "Imagery");

    // Borehole detail window
    var win = new Ext.Window({
        //autoScroll:true,
        border      : true,
        //html      : iStr,
        id          : 'nvclDetailsWindow',
        layout      : 'fit',
        //maximizable:false,
        resizable   : false,
        modal       : true,
        plain       : false,
        title       : 'Borehole Id: '+ lDatasetName,
        height      : 600,
        width       : 820,
        items:[{
            xtype           : 'tabpanel',
            id              : 'tab-panel',
            //defaults      : {layout:'fit'},
            activeItem      : 0,
            enableTabScroll : true,
            buttonAlign     : 'center',
            items           : []
        }]
      });

    // Window Tab handle
    var tp = win.items.itemAt(0);

    var startSampleNo   = 0;
    var endSampleNo     = 100;
    var sampleIncrement = 100;
    var totalCount      = aLog.SampleCount;

    var cardNav = function(incr) {

        if ( startSampleNo >= 0 && endSampleNo >= sampleIncrement) {

            startSampleNo = 1 * startSampleNo + incr;
            endSampleNo = 1 * endSampleNo + incr;
            Ext.getCmp('card-prev').setDisabled(startSampleNo < 1);
            //Ext.get('nav').dom.src = "http://nvclwebservices.vm.csiro.au/NVCLDataServices/mosaic.html?logid=fae8f90d-2015-4200-908a-b30da787f01&startsampleno=" + startSampleNo + "&endsampleno=" + endSampleNo;
            Ext.get('imageryFrame').dom.src = iServerUrl + MOSAIC_PATH + aLog.LogId + '&startsampleno=' + startSampleNo + '&endsampleno=' + endSampleNo;

            // Ext.fly ... does not work in IE7
            //Ext.fly('display-count').update('Displayying Images: ' + startSampleNo + ' - ' + endSampleNo + ' of ' + totalCount);
            Ext.getCmp('display-count').setText('Displaying Imagess: ' + startSampleNo + ' - ' + endSampleNo + ' of ' + totalCount);
        }

        Ext.getCmp('card-prev').setDisabled(startSampleNo <= 0);
        Ext.getCmp('card-next').setDisabled(startSampleNo + sampleIncrement >= totalCount);
    };


    if (aLogMosaic.LogId != '') {      // Shall we add Mosaic tab?
        tp.add({
            title:  ' Mosaic ',
            id:     'mosaicTab',
            layout:'fit',
            html: '<iframe id="nav" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="' +
                  iServerUrl + MOSAIC_PATH + aLogMosaic.LogId +
                  '"></iframe>'
       });
    }


    if (aLog.LogId != '') {      // Shall we add Imagery tab?
        tp.add({
            title:  ' Imagery ',
            id:     'imageryTab',
            layout:'fit',
            html: '<iframe id="imageryFrame" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="' +
                  iServerUrl + MOSAIC_PATH + aLog.LogId +
                  '&startsampleno='+ startSampleNo +
                  '&endsampleno=' + sampleIncrement +
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

    // List of Scalars
    var scalarStore = new Ext.data.XmlStore({
        url     : ProxyURL + iServerUrl + LOG_PATH + lDatasetId,
        record  : 'Log',
        idPath  : 'LogID',
        fields  : ['LogID','logName']
    });

    var CheckBox = new Ext.grid.CheckboxSelectionModel();

    var scalarGrid = new Ext.grid.GridPanel({
        //title         : 'Available Scalars',
        store           : scalarStore,
        sm              : CheckBox,
        autoExpandColumn: 'log-name-col',
        id				: 'nvcl-scalar-grid',
        height          : 450,
        width           : 200,
        loadMask        : true,
        columns         : [
            CheckBox,
        {
            header: "ID", width: 50, dataIndex: 'LogID',
            sortable: true, hidden: true
        },{
            id: 'log-name-col',
            header: "Scalar", width: 100, dataIndex: 'logName',
            sortable: true
            //,renderer: renderCell.createDelegate(this)
        }]
    });

    //This is for loading our tool tips
    scalarGrid.on('render', function(grid) {

        //var store = grid.getStore();  // Capture the Store.
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
                    GDownloadUrl(vocabsQuery, function(pData, pResponseCode) {
                        if(pResponseCode != 200) {
                            tip.body.dom.innerHTML = "ERROR: " + pResponseCode;
                            return;
                        }

                        var response = eval('(' + pData + ')');
                        if (!response.success) {
                            tip.body.dom.innerHTML = "ERROR: server returned error";
                            return;
                        }

                        //Update tool tip
                        if (response.definition && response.definition.length > 0)
                        	tip.body.dom.innerHTML = response.definition;
                        else if (response.scopeNote && response.scopeNote.length > 0)
                        	tip.body.dom.innerHTML = response.scopeNote;
                        else
                        	tip.body.dom.innerHTML = 'N/A';
                      });
                }
                //TODO: Commenting this out for now as it causes js errors. Still need to
                // test whether there is a better solution.
                //hide : function(component) {
                //    component.destroy();
                //}
            }
        });
    });


    // How to load the store at the latest possible moment?
    scalarStore.load();
    /*
    scalarStore.load({
        // store loading is asynchronous, use a load listener or callback to handle results
        callback: function(){
            Ext.Msg.show({
                title: 'Store Load Callback',
                msg: 'store was loaded, data available for processing',
                modal: false,
                icon: Ext.Msg.INFO,
                buttons: Ext.Msg.OK
            });
        }
    }
    );

        this.on({
        afterlayout:{scope:this, single:true, fn:function() {
            //this.scalarStore.load({params:{start:0, limit:10}});
            this.scalarStore.load();
        }}
    }); */

    // Scalars Tab
    tp.add({
        title  : 'Scalars',
        id     : 'scalarsTab',
        layout : 'fit',
        border : false,
        items: {
            // Bounding form
            id      :'scalarsForm',
            xtype   :'form',
            layout  :'column',
            frame   : true,

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
                        name        : 'startdepth',
                        minValue    : 0,
                        value       : 0,
                        accelerate  : true
                    },{
                        xtype       : 'spinnerfield',
                        fieldLabel  : 'End Depth (m)',
                        name        : 'enddepth',
                        minValue    : 0,
                        value       : 99999,
                        accelerate  : true
                    },{
                        xtype                   : 'spinnerfield',
                        fieldLabel              : 'Interval (m)',
                        name                    : 'samplinginterval',
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
                            {boxLabel: 'Stacked Bar Chart', name: 'graphtype', inputValue: 1, checked: true},
                            {boxLabel: 'Scattered Chart', name: 'graphtype', inputValue: 2},
                            {boxLabel: 'Line Chart', name: 'graphtype', inputValue: 3}
                        ]
                    }]

                }],                
                buttons:[{
                    text: 'Plot',
                    handler: function() {
                        // var urlFull = 'http://nvclwebservices.vm.csiro.au/NVCLDataServices/plotscalar.html
                        // ?logid=77d0b966-d936-4b9d-bc5c-4b2b12bf1c5
                        // &startdepth=0
                        // &enddepth=99999
                        // &samplinginterval=1
                        // &width=300
                        // &height=600
                        // &graphtype=1';

                        var sHtml = '';
                        var item_count = scalarGrid.getSelectionModel().getCount();
                        //alert(Ext.getCmp('scalarsForm').getForm().findField('enddepth').getValue());
                        //alert(Ext.getCmp('scalarsForm').getForm().getValues(true));
                        var scalarForm = Ext.getCmp('scalarsForm').getForm();

                        if (item_count > 0) {
                            var s = scalarGrid.getSelectionModel().getSelections();
                            for(var i = 0, len = s.length; i < len; i++){
                                sHtml +='<img src="';
                                sHtml += ProxyURL + iServerUrl + PLOT_PATH;
                                sHtml += s[i].data.LogID;
                                sHtml += '&' + scalarForm.getValues(true);
                                sHtml += '&width=300';
                                sHtml += '&height=600';
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
                        }
                        else if (item_count === 0){
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

    // TO Do: Add check for Plot tab --> Don't display if neither Mosaic nor Plot exist
    if (aLog.LogId != '') {
        win.show();
        win.center();
    } else {
        Ext.MessageBox.alert('Info', 'Selected dataset is empty!');
    }
} // End of showBoreholeDetails()


/**
 * Static method called from google map's info window to open a
 * new Ext JS window with borehole details
 *
 * @param {String} iWfsServiceUrl - GeoServer Borehole service url
 * @param {String} iServerUrl
 * @param {String} iDatasetDetails - an object with datasetName, datasetId and omUrl set.
 */
function showDownloadDetails(iWfsServiceUrl, iServerUrl, iDatasetDetails) {

    var lDatasetId   = iDatasetDetails.datasetId.replace(/'/g, '');
    var lDatasetName = iDatasetDetails.datasetName;
    var lOMUrl = iDatasetDetails.omUrl;

    var CSV_PATH     = Ext.util.Format.htmlEncode("?request=GetFeature&typeName=om:GETPUBLISHEDSYSTEMTSA&CQL_FILTER=(DATASET_ID='");
    var CSV_PATH_END = Ext.util.Format.htmlEncode("')&outputformat=csv");

    //'/xsltRestProxy.do?serviceUrl=' + 'http://nvclwebservices.vm.csiro.au/NVCLTSGDownloadServices/checkstatus.html?email=Jarek.Sanders@csiro.au'
    //restproxy?http://nvclwebservices.vm.csiro.au/NVCLTSGDownloadServices/checkstatus.html?email=Jarek.Sanders@csiro.au'
    //var TSG_CHCK_PATH= ProxyURL + iServerUrl + '/NVCLTSGDownloadServices/checkstatus.html?email=';
    var TSG_PATH     = '/NVCLDownloadServices/downloadtsg.html?';
    var TSG_CHCK_PATH = '/NVCLDownloadServices/checktsgstatus.html?email=';

    // TODO: Get gsml:Borehole from request instead of hardcoding
    // http://apacsrv3.arrc.csiro.au/nvcldownload-services/downloadwfs.html?email=florence.tan@csiro.au&boreholeid=UDD1420&serviceurl=http://apacsrv3.arrc.csiro.au/geoserverOM/wfs&typename=gsml:Borehole
    // http://apacsrv3.arrc.csiro.au/nvcldownload-services/checkwfsstatus.html?email= Jarek.Sanders@csiro.au
    var O_M_PATH = '/NVCLDownloadServices/downloadwfs.html?';
    var O_M_CHCK_PATH = '/NVCLDownloadServices/checkwfsstatus.html?';

    // Dataset download window
    var win = new Ext.Window({
        id              : 'nvclDownloadWindow',
        //autoScroll    : true,
        border          : true,
        //html          : iStr,
        layout          : 'fit',
        //maximizable   : false,
        resizable       : false,
        modal           : true,
        plain           : false,
        title           : 'Borehole Id:  '+ lDatasetName,
        height          : 400,
        width           : 500,
        //defaultButton   :'emailAddress',
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
                        html : 'Download Hylogging dataset'
                    }
                }]
            },{
                xtype   :'hidden',
                name    :'datasetid', //name of the field sent to the server
                value   : lDatasetId  //value of the field
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
                    //href : iServerUrl + CSV_PATH + lDatasetId + CSV_PATH_END
                    href : iWfsServiceUrl + CSV_PATH + lDatasetId + CSV_PATH_END
                    //handler: function(){ console.log(iServerUrl + CSV_PATH + lDatasetId + CSV_PATH_END) }
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
                //html :"<div style='margin-left:60px;'><br>(*) denotes mandatory fields</div>",
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
                        {boxLabel: 'linescan', name: 'linescan', inputValue: 'yes', checked: true},
                        {boxLabel: 'spectra', name: 'spectra', inputValue: 'yes', checked: true},
                        {boxLabel: 'profilometer', name: 'profilometer', inputValue: 'yes', checked: true},
                        {boxLabel: 'traypics', name: 'traypics', inputValue: 'yes', checked: true},
                        {boxLabel: 'mospic', name: 'mospic', inputValue: 'yes', checked: true},
                        {boxLabel: 'mappics', name: 'mappics', inputValue: 'yes', checked: true}
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
                            var sUrl = '';
                            sUrl += '<iframe id="nav1" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="';
                            sUrl += ProxyURL + iServerUrl + TSG_CHCK_PATH + sEmail;
                            sUrl += '"></iframe>';
                            //alert(sUrl);

                            var winStat = new Ext.Window({
                                autoScroll  : true,
                                border      : true,
                                //html      : sUrl,
                                //url       : 'http://nvclwebservices.vm.csiro.au/NVCLTSGDownloadServices/checkstatus.html?email=Jarek.Sanders@csiro.au',
                                autoLoad    : ProxyURL + iServerUrl + TSG_CHCK_PATH + sEmail,
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
                            sUrl += ProxyURL + iServerUrl + TSG_PATH;
                            sUrl += downloadForm.getValues(true);
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
                            var sUrl = '';
                            sUrl += '<iframe id="nav1" style="overflow:auto;width:100%;height:100%;" frameborder="0" src="';
                            sUrl += ProxyURL + iServerUrl + O_M_CHCK_PATH;
                            sUrl += '&email=' + sEmail;
                            sUrl += '"></iframe>';
                            //alert(sUrl);

                            var winStat = new Ext.Window({
                                autoScroll  : true,
                                border      : true,
                                //html      : sUrl,
                                //url       : 'http://nvclwebservices.vm.csiro.au/NVCLTSGDownloadServices/checkstatus.html?email=Jarek.Sanders@csiro.au',
                                autoLoad    : ProxyURL + iServerUrl + O_M_CHCK_PATH + '&email=' + sEmail,
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
                            sUrl += ProxyURL + iServerUrl + O_M_PATH;
                            sUrl += 'boreholeid=sa.samplingfeaturecollection.' + lDatasetId;
                            sUrl += '&serviceurl=' + lOMUrl + 'wfs';
                            sUrl += '&typename=sa:SamplingFeatureCollection';
                            sUrl += '&email=' + sEmail;
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

} // End of showDownloadDetails()


/*
 * Queries NVCL service for a dataset LogCollection. From the result set
 * extracts LogId and total count of sub-images for a given image type.
 *
 * <LogCollection>
 *   <Log>
 *     <LogID>63d31981-bd0c-44dd-a118-6a3406c5d68</LogID>
 *     <LogName>Mosaic</LogName>
 *     <SampleCount>1</SampleCount>
 *   </Log>
 *   ..
 *   ..
 * </LogCollection>
 *
 * Note: This function uses synchronous HTTP request as we first need to
 * get image
 * @param {String} iServerUrl
 * @param {String} iDatasetId
 * @param {String} iLogName  Image type eg: Mosaic or Imagery
 */
function getImageLog(iServerUrl, iDatasetId, iLogName) {

    var LOG_PATH     = '/NVCLDataServices/getLogCollection.html?datasetid=';
    var aLogId       = '';
    var aSampleCount = 0;

    //http://nvclwebservices.vm.csiro.au:80/NVCLDataServices/getLogCollection.html?datasetid=6dd70215-fe38-457c-be42-3b165fd98c7&mosaicsvc=yes
    var logIdQuery   =
             ProxyURL + iServerUrl + LOG_PATH + iDatasetId + '&mosaicsvc=yes';

    try {
        // create an XMLHttpRequest
        var request = GXmlHttp.create();

        // Prepare synchronous HTTP request to the server (3rd param = false)
        request.open("GET", logIdQuery, false);

        // Processing will pause until response is available
        request.send(null);

        if (request.status == 200) {
            var XmlDoc = GXml.parse(request.responseText);

            if (g_IsIE)
                XmlDoc.setProperty("SelectionLanguage", "XPath");

            var rootNode = XmlDoc.documentElement;

            if (!rootNode)
                throw "EmptyXMLException";

            var aLog = rootNode.getElementsByTagName("Log");

            if (aLog.length == 0)
                throw "EmptyXMLException";

            for (var i=0; i < aLog.length; i++ ) {
                if (GXml.value(aLog[i].selectSingleNode
                        ("*[local-name() = 'LogName']")) == iLogName) {

                    aLogId = GXml.value(aLog[i].selectSingleNode
                                    ("*[local-name() = 'LogID']"));
                    aSampleCount = GXml.value(aLog[i].selectSingleNode
                                    ("*[local-name() = 'SampleCount']"));
                }
            }
        } else {
            alert('Remote server returned HTTP status: ' + request.status);
        }
    } catch (e) {
        if (e == "EmptyXMLException") {
            // Do nothing
        } else {
            alert("Network error");
        }
    }

    return {
        LogId       :aLogId,
        SampleCount :aSampleCount
    };
}   // End of getImageLog()
