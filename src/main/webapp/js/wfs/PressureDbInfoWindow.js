/**
 * @class PressureDbInfoWindow
 * <p>PressureDb marker info window pop-up</p>
 */

/**
 * @constructor
 * @param {GMap2}   iMap
 * @param {GMarker} iMarker
 * @param {wfsUrl}  wfsUrl
 */
function PressureDbInfoWindow(iMap, iMarker, wfsUrl) {

    this.map = iMap;
    this.marker = iMarker;
    this.boreholeId = iMarker.title || "";
    this.waitHtml =
        '<div>' +
            '<b>' + this.boreholeId + '</b>' +
            '<p style="text-align:center;">' +
                '<img src="img/wait.gif" style="padding-top:50px;" />' +
            '</p>' +
        '</div>';
    this.wfsUrl = wfsUrl;

    /**
     * iMarker.wfsUrl comes from GeoNetwork and represents GeoServer's service
     * Url. From this Url we remove pathname and return only protocol with
     * hostname eg.: http://server.com.au/nvcl/wfs --> http://server.com.au
     */
    var str = wfsUrl.slice( ("http://").length);
    this.baseUrl = 'http://' + str.slice(0,str.indexOf("/"));
    this.serviceUrl = this.baseUrl + '/pressuredb-dataservices'; //This is a hack - somehow this needs to make it to the registry
}

PressureDbInfoWindow.prototype = {

    //Private fields
    'CONTROLLER_GET_AVAILABLE_OM' : 'pressuredb-getAvailableOM.do',
    'CONTROLLER_DOWNLOAD' : 'pressuredb-download.do',
    'marker' : null,
    'map' : null,
    'waitHtml' : '',
    'boreholeId' : '',
    'wfsUrl' : '',
    'baseUrl' : '',
    'serviceUrl' : '',
    'tabList' : [],
    
    //Downloads the specified URl in a new window for the user to actually save to disk
    'downloadUrl' : function(url) {
        var body = Ext.getBody();
        var frame = body.createChild({
            tag:'iframe',
            //cls:'x-hidden',
            id:'iframe',
            name:'iframe'
        });
        var form = body.createChild({
            tag:'form',
            //cls:'x-hidden',
            id:'form',
            target:'iframe',
            method:'POST'
        });
        form.dom.action = url;
        form.dom.submit();
    },
    
    //Creates the extjs content of the OM tab 
    'renderAvailableOMTab' : function(divId, availableOmResponse) {
        var me = this;
        var panel = new Ext.form.FormPanel({
           renderTo : divId,
           width : 300,
           autoHeight : true,
           items : [{
               xtype : 'checkboxgroup',
               fieldLabel: 'Temperature',
               items : [{
                   boxLabel : 'T',
                   name : 't',
                   disabled : !availableOmResponse.temperatureT
               }]
           },{
               xtype : 'checkboxgroup',
               fieldLabel: 'Salinity',
               items : [{
                   boxLabel : 'TDS',
                   name : 'tds',
                   disabled : !availableOmResponse.salinityTds
               },{
                   boxLabel : 'NaCl',
                   name : 'nacl',
                   disabled : !availableOmResponse.salinityNacl
               },{
                   boxLabel : 'Cl',
                   name : 'cl',
                   disabled : !availableOmResponse.salinityCl
               }]
           },{
               xtype : 'checkboxgroup',
               fieldLabel: 'Pressure',
               items : [{
                   boxLabel : 'RFT',
                   name : 'rft',
                   disabled : !availableOmResponse.pressureRft
               },{
                   boxLabel : 'DST',
                   name : 'dst',
                   disabled : !availableOmResponse.pressureDst
               },{
                   boxLabel : 'FITP',
                   name : 'fitp',
                   disabled : !availableOmResponse.pressureFitp
               }]
           }],
           buttons : [{
               xtype : 'button',
               text : 'Refresh observations',
               handler : function() {
                   //The refresh is quite simple - kill the entire ExtJS panel and recreate it based on the response
                   var parentForm = this.findParentByType('form');
                   parentForm.destroy();
                   me.retrieveAvailableOM();
               }
           },{
               xtype : 'button',
               text : 'Download selected observations',
               handler : function() {
                   //We need to generate our download URL
                   var url = Ext.urlAppend(me.CONTROLLER_DOWNLOAD, Ext.urlEncode({wellID : me.boreholeId}));
                   url = Ext.urlAppend(url, Ext.urlEncode({serviceUrl : me.serviceUrl}));
                   
                   //Find the parent form - iterate the selected values
                   var parentForm = this.findParentByType('form');
                   var featuresAdded = 0;
                   for (feature in parentForm.getForm().getValues()) {
                       url = Ext.urlAppend(url, Ext.urlEncode({feature : feature}));
                       featuresAdded++;
                   }
                   
                   if (featuresAdded > 0) {
                       me.downloadUrl(url);
                   } else {
                       Ext.Msg.show({
                           title:'No selection',
                           msg: 'No observations have been selected! You must select at least one before proceeding with a download.',
                           icon: Ext.MessageBox.WARNING
                        });
                   }
               }
           }]
        });
    },
    
    //Makes a request to the pressure DB dataservice for the list of available datasets for this borehole
    'retrieveAvailableOM' : function() {
        Ext.Ajax.request({
            url : this.CONTROLLER_GET_AVAILABLE_OM,
            params : {
                wellID : this.boreholeId,
                serviceUrl : this.serviceUrl
            },
            scope : this,
            success : function(response) {
                var responseObj = Ext.util.JSON.decode(response.responseText);
                if (responseObj && responseObj.success) {
                    var availableOMResponse = responseObj.data[0];
                        
                    //This is our html page to show in the tab - our ext js panel will render directly to the div
                    var divId = 'pressuredb-om';
                    if (this.tabList.length == 1) {
                        var baseHtml = '<html><body><div id="' + divId + '"/></body></html>';
                        
                        //So update the page according to what we get from the data service
                        this.tabList.push(new GInfoWindowTab("Observations", baseHtml));
                        this.map.updateInfoWindow(this.tabList);
                    }
                    
                    this.renderAvailableOMTab(divId, availableOMResponse);
                }
            }
        });
    },
    
    //Shows this info window opened up on the marker used in the constructor
    'show': function() {
        //Extract the transformed HTML snippet into a proper HTML 'page' ready for display on the first tab
    	var indexOfDes = this.marker.description.indexOf('<');
    	var overlayDescription =this.marker.description.substring(indexOfDes);
    	var summaryHtml ='';
    	summaryHtml += '<html>';
    	summaryHtml += '<body>';
    	if (Ext.isIE) {
    		summaryHtml += '<div style="';
    		summaryHtml += 'width: expression(!document.body ? &quot;auto&quot; : (document.body.clientWidth > 299 ? &quot;300px&quot; : &quot;auto&quot;) );';
    		summaryHtml += 'height: expression( this.scrollHeight > 549 ? &quot;550px&quot; : &quot;auto&quot; );';
    		summaryHtml += 'overflow: auto;">';
    	} else {
    		summaryHtml += '<div style="max-width: 300px; max-height: 550px; overflow: hidden;">';
    	}
    	summaryHtml += overlayDescription;
    	summaryHtml += '</div>';
    	
    	this.tabList = [];
    	this.tabList.push(new GInfoWindowTab("Summary", summaryHtml));

        var me = this;
        this.marker.openInfoWindowTabs(this.tabList, {
            onOpenFn:function(){
                //And update it with the downloaded data as it arrives
                me.retrieveAvailableOM();
            }
        });
    }

}; // End of PressureDbInfoWindow.prototype

