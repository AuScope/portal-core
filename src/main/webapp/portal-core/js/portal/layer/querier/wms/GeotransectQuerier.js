/**
 *
 * Function class to handle Geotransect WMS. Method are exposed as public static method as they represent functions rather then Objects.
 */

Ext.define('portal.layer.querier.wms.GeotransectQuerier', {
    extend : 'portal.layer.querier.wms.WMSQuerier',

    GEOTRANSECTDATASERVICE : '/geotransect-dataservices',
    GETSEISMICSECTIONS : "/getSeismicSections.json?",
    GETSEGYDATASETS : "/getSEGYDatasets.json?",

    constructor : function(config) {
        this.callParent(arguments);
    },


   /**
    * Parse the response and retrieve the CSWRecord for that point.
    */
    _getCSWRecord : function(cswrecordurl,queryTarget,callback) {

        var cswRecordStore = Ext.create('Ext.data.Store', {
            id:'seismicCSWRecordStore',
            autoLoad: false,
            model : 'portal.csw.CSWRecord',
            proxy: {
                type: 'ajax',
                url: 'getSeismicCSWRecord.do',
                extraParams: {
                    service_URL : cswrecordurl
                },
                reader: {
                    type: 'json',
                    rootProperty: 'data',
                    successProperty: 'success',
                    totalProperty: 'totalResults'
                }

            }

        });

        cswRecordStore.load({
            scope: this,
            callback: function(records, operation, success) {
                if(success){
                    var panel = Ext.create('portal.layer.querier.BaseComponent', {
                        border : false,
                        autoScroll : true,
                        items : [{
                            xtype : 'cswmetadatapanel',
                            border : false,
                            cswRecord : records[0]
                        }]
                    });

                    callback(this, [panel], queryTarget);
                }else{
                    callback(this, [this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.')], queryTarget);
                }
            }
        });
    },

    /**
     * See parent class for definition
     *
     * Makes a WMS request, waits for the response and then parses it passing the results to callback
     */
    query : function(queryTarget, callback) {
        var proxyUrl = this.generateWmsProxyQuery(queryTarget, 'text/xml');
        Ext.Ajax.request({
            url : proxyUrl,
            timeout : 180000,
            scope : this,
            callback : function(options, success, response) {
                if (success) {
                    var xmlResponse = response.responseText;
                    var domDoc = portal.util.xml.SimpleDOM.parseStringToDOM(xmlResponse);
                    //VT: The default namespace is causing alot of grief in IE cause javeline xpath is unable to handle complex xpath.
                    if(domDoc.querySelector){
                        //VT: IE's version of selectSingleNode is querySelector.
                        if(domDoc.querySelector('FeatureInfoResponse').querySelector('FIELDS')){
                            var cswUrl = domDoc.querySelector('FeatureInfoResponse').querySelector('FIELDS').getAttribute('url');
                        }else{
                            callback(this,[],queryTarget);
                            return;
                        }
                    }else{
                        var cswUrl = portal.util.xml.SimpleXPath.evaluateXPathString(domDoc.childNodes[0], "//*[local-name()='FIELDS']/@url");
                    }

                    //VT: The response back from GA is a invalid url. Further it gets redirected to another URL
                    //VT: therefore concatenating /xml to the returned url won't work.
                    //VT: Rini advise to hardcode the URL.
                    //https://www.ga.gov.au/products/servlet/controller?event=GEOCAT_DETAILS&amp;catno=76436
                    var startIndex= cswUrl.lastIndexOf("catno=") + 6;
                    var catno = cswUrl.substring(startIndex,cswUrl.length);
                    cswUrl = "http://www.ga.gov.au/metadata-gateway/metadata/record/gcat_" + catno + "/xml";
                    this._getCSWRecord(cswUrl,queryTarget,callback);
                }else{
                    callback(this, [this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.')], queryTarget);
                }

            }
        });
    }

});
