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
     * Strip pathname from Url and return only protocol + hostname
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
        //y:    50,
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
                html: '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0"  src="' + iServerName + LOG_PATH + lDatasetId + '&startsampleno=' + startSampleNo + logId +'&endsampleno=' + endSampleNo + '"></iframe>' 
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
          
    tab = tp.add({
        title  : 'Scalars',
        id     : 'scalarstab',
        iconCls: 'icon-info',
        html   : 'Hello'
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