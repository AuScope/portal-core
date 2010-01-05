/**
 * @class NvclInfoWindow
 * <p>Provides wrapper for Google Map's InfoWindow pop-up</p>
 * 
 * @version $Id$
 */
 
/**
 * @constructor
 * @param {GMap2}
 * @param {GMarker}
 */ 
 function NvclInfoWindow(iMap, iMarker) {
     
    this.Map = iMap;    
    this.Marker = iMarker;
    this.tabsArray = new Array();    
    this.boreholeId = this.Marker.title || "";
    this.summaryHtml = this.Marker.description || "";
    
    this.waitHtml 
        = "<div>" +
              "<b>" + this.boreholeId + "</b>" +
               "<p style=\"text-align:center;\">" +
                   "<img src=\"img/wait.gif\" style=\"padding-top:50px;\" />" +
               "</p>" +
          "</div>"
                    
    this.wfsUrl = this.Marker.wfsUrl;
    
    /**
     * From Url remove pathname and return only protocol with hostname
     * eg.: http://server.com.au:80/some_path --> http://server.com.au:80 
     */       
    this.wfsServerUrl = (function(url) {
        
        var str = url.slice( ("http://").length);   
        return 'http://' + str.slice(0,str.indexOf("/"));
        
    })(this.wfsUrl);
}

NvclInfoWindow.prototype = {

    'TAB_1' : "Summary",
    'TAB_2' : "Details",
    'NVCL_SERVICE' : "/NVCLDataServices/getDatasetCollection.html?holeidentifier=",
    
    'show': function() {

        this.retrieveDatasets();                
        this.tabsArray[0] = new GInfoWindowTab(this.TAB_1, this.summaryHtml);
        this.Marker.openInfoWindowTabs(this.tabsArray);        
        
    },
    
    /*
     * Returns datasets for the selected borehole
     */
    'retrieveDatasets' : function() {
        
        var that = this,
            serverAddr = this.wfsServerUrl,      
            url = ProxyURL + serverAddr + this.NVCL_SERVICE + this.Marker.title;

        GDownloadUrl(url, function(response, responseCode) {
            if (responseCode == 200) {
                var XmlDoc = GXml.parse(response);
                
                if (g_IsIE)
                    XmlDoc.setProperty("SelectionLanguage", "XPath");

                var rootNode = XmlDoc.documentElement;
                if (!rootNode)
                    return;
  
                // get the concept tag (inside is the additional info)
                var aDataset = rootNode.getElementsByTagName("Dataset");
                if (aDataset.length == 0)
                    return;           

                var datasetIds = new Array(),
                    datasetNames = new Array(),
                    dataset_idx = 0;   

                for (var i=0; i < aDataset.length; i++ ) {
                    var nvclDataset = aDataset[i];
                    datasetIds[dataset_idx] 
                        = GXml.value(aDataset[i].selectSingleNode
                                ("*[local-name() = 'DatasetID']"));
                    datasetNames[dataset_idx] 
                        = GXml.value(aDataset[i].selectSingleNode
                                ("*[local-name() = 'DatasetName']"));                    
                    dataset_idx++;
                }
                
                that.parseDatasetSearch(datasetIds, datasetNames);
                                                
            } else if(responseCode == -1) {
                alert("Data request timed out. Please try later.");
            } else {
                alert('Remote server returned error code: ' + responseCode);
            }
        });        
    },

    'parseDatasetSearch' : function(datasetIds, datasetNames) {        
        if (datasetIds.length > 0) {
            var lHtml = "" +
            '<div style="padding:20px;" >' + 
                '<p> Available data sets: </p>' +
            '</div>' +
            '<div align="center">' +
                '<form method="post" action="showNvclDetails(\''+ this.Marker.title +'\'); ">' +
                    '<select name="selAge" id="selAge" size="5" style="width:200px;">';

            for (var i=0; i < datasetIds.length; i++ ) {
                if (i==0)
                    lHtml += '<option value="\''+datasetIds[i] +'\'" selected="selected">'+ datasetNames[i] + ' (Latest)</option>';
                else 
                    lHtml += '<option value="\''+datasetIds[i] +'\'">'+ datasetNames[i] +'</option>';
            }
            lHtml += '</select>' +
                    '<div align="right">' +
                        '<br>' +
                        '<input type="button" value="Select DataSet" name=butSelectDataset onclick="showNvclDetails(\''+ this.wfsServerUrl +'\',this.form.selAge.value);">' +
                    '</div>' +
                '</form>' +
            '</div>';            

            this.tabsArray[1] = new GInfoWindowTab(this.TAB_2, lHtml);
            this.Map.updateInfoWindow(this.tabsArray);
        }
    },
    
    'showNvclDetails' : function (iServerName, iDatasetId) {
    
        alert(iDatasetId.replace(/'/g, ''));
    
        var lDatasetId = iDatasetId.replace(/'/g, '');
        var startSampleNo = 0;
        var endSampleNo = 100;
        var LOG_PATH = '/NVCLDataServices/getLogCollection.html?datasetid=';
        var MOSAIC_PATH = '/NVCLDataServices/mosaic.html?logid=';

        alert(iServerName + '\n' + LOG_PATH + '\n' + lDatasetId);
    }
}

/**
 * Static method called from within HTML/javascript markup to open a 
 * new Ext JS window
 * 
 * To DO: More elegant OO solution
 */
function showNvclDetails(iServerName, iDatasetId) {
    
    alert(iDatasetId.replace(/'/g, ''));
    
    var lDatasetId = iDatasetId.replace(/'/g, '');
    var startSampleNo = 0;
    var endSampleNo = 100;
    var LOG_PATH = '/NVCLDataServices/getLogCollection.html?datasetid=';
    var MOSAIC_PATH = '/NVCLDataServices/mosaic.html?logid=';
    var PLOT_PATH = '/NVCLDataServices/plotscalar.html?logid=';

    alert(iServerName + '\n' + LOG_PATH + '\n' + lDatasetId);

    var logId = (function (lDatasetId) {
        var aLogId = -1;
        var logIdQuery = ProxyURL + iServerName + LOG_PATH
                       + lDatasetId + '&mosaicsvc=yes';
        //alert('logIdQuery: ' + logIdQuery);
                                        
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
                    return -1;
                   
                var aLog = rootNode.getElementsByTagName("Log");
        
                if (aLog.length == 0) 
                    return -1;
                
                for (var i=0; i < aLog.length; i++ ) {
                    if (GXml.value(aLog[i].selectSingleNode("*[local-name() = 'LogName']")) == "Imagery");
                        aLogId = GXml.value(aLog[i].selectSingleNode("*[local-name() = 'LogID']")); 
                }
            } else {
                alert('Remote server returned HTTP status: ' + request.status);
            }
        } catch(e) {
            alert("Network error");
        }

        return aLogId;
    })(lDatasetId);
        
    var win = new Ext.Window({
        //autoScroll:true,
        border: true,        
        height: 600,
        //html: iStr,
        id    : 'nvclDetailsWindow',
        layout: 'fit',
        maximizable:true,
        modal:  true,
        plain:  false,
        title:  'Borehole Id: ',          
        width:  820,
        //x:    500,
        y:    50,
        items:[{
            xtype:'tabpanel',
            id   : 'tab-panel',
            //defaults:{layout:'fit'},
            activeItem:0,
            enableTabScroll:true,
            items: [{
                autoShow: true,
                title:   'Plot',
                id:      'plottab',
                iconCls: 'icon-info', 
                //html: '<div><p><br><b>' + iDatasetId +'<b></p></div>'
                //html: '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0"  src="' + iServerName + LOG_PATH + lDatasetId + '&startsampleno=' + startSampleNo + logId +'&endsampleno=' + endSampleNo + '"></iframe>'
                //html: '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0"  src="' + iServerName + LOG_PATH + lDatasetId + '"></iframe>'
                html: 'hello'  
            }]        
        }],             
        
      });
    var tp = win.items.itemAt(0);
    
    // Shall we add Mosaic tab?
    if (logId != -1) {      
        var tab = tp.add({
            title:  'Mosaic',
            id:     'mosaictab',
            iconCls: 'icon-info',
            html: '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0" src="' + iServerName + MOSAIC_PATH + logId + '&startsampleno=1&endsampleno=100"></iframe>'
       });
    }

    var scalarStore = new Ext.data.XmlStore({
        url: ProxyURL + iServerName + LOG_PATH + lDatasetId,
        record: 'Log',
        idPath: 'LogID',
        fields: ['LogID','logName']
    });


    
    var CheckBox = new Ext.grid.CheckboxSelectionModel();
    var scalarGrid = new Ext.grid.GridPanel({
        //title: 'Available Scalars',
        store: scalarStore,
        sm: CheckBox,
        columns : [
            CheckBox,
        {
            header: "ID", width: 50, dataIndex: 'LogID', 
            sortable: true, hidden: true
        },{
            id: 'log-name-col', header: "Scalar", width: 100, dataIndex: 'logName',
            sortable: true
        }],
        autoExpandColumn: 'log-name-col',
        width: 200,
        height: 450,
        loadMask: true
    });
    
    scalarStore.load();    
    // load the store at the latest possible moment
    /* this.on({
        afterlayout:{scope:this, single:true, fn:function() {
            //this.scalarStore.load({params:{start:0, limit:10}});
            this.scalarStore.load();
        }}
    }); */
          
    tab = tp.add({
        title  : 'Scalars',
        id     : 'scalarstab',
        iconCls: 'icon-info',
        layout : 'fit',
        border : false,
        items: {
            // Bounding form
            id     :'scalars-form'
            ,xtype :'form'
            ,layout:'column'
            ,frame :true
             
            // these are applied to columns
            ,defaults:{
                columnWidth:0.5
                ,layout:'form'
                ,hideLabels:true
                ,border:false
                ,bodyStyle:'padding:10px'
                ,labelWidth:100
            }
            
            // Columns
            ,items:[{ // column 1
                // these are applied to fieldsets
                defaults:{xtype:'fieldset', layout:'form', anchor:'100%', autoHeight:true}
                 
                // fieldsets
                ,items:[{
                    title:'List of Scalars'
                    ,defaultType:'textfield'
                     
                    // these are applied to fields
                    ,defaults:{anchor:'-5', allowBlank:false}
                     
                    // fields
                    ,items:[scalarGrid]
                }]
            },{ // column 2
                // these are applied to fieldsets
                defaults:{
                    xtype       :'fieldset',
                    layout      :'form',
                    anchor      :'100%',
                    autoHeight  :true,
                    paddingRight: '10px'
                }
                 
                // fieldsets
                ,items:[{
                    title       :'Hint',
                    defaultType:'textfield',
                    // fields
                    items:[{
                        xtype  : 'box',
                        id     : 'indicator',
                        autoEl : {
                            tag  : 'div',
                            html : 'Select a scalar(s) from the "Scalar List" table on the left and then click "Plot" button.<br><br>Leave the default depth values for the entire depth.'
                        }
                    }]
                },{                    
                    title:'Options',
                    defaultType:'textfield',
                 
                    // these are applied to fields
                    //,defaults:{anchor:'-20', allowBlank:false}
                    bodyStyle : 'padding:0 0 0 45px',
                 
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
                        xtype       : 'spinnerfield',
                        fieldLabel  : 'Interval (m)',
                        name        : 'samplinginterval',
                        minValue    : 0,
                        value       : 1.0,
                        allowDecimals: true,
                        decimalPrecision: 1,
                        incrementValue: 0.1,
                        alternateIncrementValue: 2.1,
                        accelerate: true                                                                   
                    }]
                },{
                    xtype: 'fieldset',
                    title       :'Graph Types',
                    autoHeight: true,
                    items:[{
                        xtype  : 'radiogroup',
                        id: 'ts1',
                        columns: 1,
                        items : [
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
                        
                        //alert(Ext.getCmp('scalars-form').getForm().findField('enddepth').getValue());
                        //alert(Ext.getCmp('scalars-form').getForm().getValues(true));
                        var scalarForm = Ext.getCmp('scalars-form').getForm();
                        
                        if (item_count > 0) {
                            var s = scalarGrid.getSelectionModel().getSelections();
                            for(var i = 0, len = s.length; i < len; i++){
                                sHtml +='<img src="'; 
                                sHtml += ProxyURL + iServerName + PLOT_PATH;
                                sHtml += s[i].data.LogID;
                                // startDepth=0&endDepth=99999&interval=1&rb-col=2
                                sHtml += '&' + scalarForm.getValues(true);
                                sHtml += '&width=300';
                                sHtml += '&height=600';
                                sHtml += '"/>';
                            }
                        }
                                                
                        var winPlot = new Ext.Window({
                            autoScroll:true,
                            border: true,        
                            html: sHtml,
                            id    : 'plWindow',
                            layout: 'fit',
                            maximizable:false,
                            modal:  true,
                            plain:  false,
                            title:  'Plot: ',
                            autoHeight: true,          
                            autoWidth: true,
                            x:10,
                            y:10,
                          });                        
                        winPlot.show();
                        
                    }
                }]
            }]
        }

    });
    //alert("Before windows show");
    //alert(logId);
    //alert("after");
        
    // TO Do: Add check for Plot tab --> Don't display if neither Mosaic nor Plot exist    
    if (logId != -1) {
        win.show();

    } else {
        Ext.MessageBox.alert('Info', 'Selected dataset is empty!');
    }
    
    
}