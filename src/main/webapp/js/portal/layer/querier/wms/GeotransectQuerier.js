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
     * Synchronously generates a download tab for the specified list of URLs
     */
    _getDownloadTab : function(urlList, tabTitle) {
        var sHtml = '<div class="niceDiv">' +
            '<div style="padding-bottom:10px;" >' +
            'Available data sets:' +
            '</div>' +
            '<div>' +
            'Note: Browsers may not be able to display the following data due to the '+
            'format or large size. It is advisable to download the data before viewing '+
            '(right click > Save Target / Link As).' +
            '</div>' +
            '<div>' +
            '<table border="1" cellspacing="1" cellpadding="4" class="auscopeTable">';

        for (var i = 0; i < urlList.length; i++) {
            sHtml += "<tr><td>";
            sHtml += "<a href='" + urlList[i] + "' target='_blank'>"+ urlList[i] +"</a><br/>";
            sHtml += "</tr></td>";
        }

        sHtml += "</table></div></div>";

        return Ext.create('portal.layer.querier.BaseComponent', {
            tabTitle : tabTitle,
            html : sHtml
        });
    },

    /**
     * Synchronously generates a BaseComponent for queryTarget + line ID
     */
    _getSummaryTab : function(queryTarget, iLineId){
        var knownLayer = queryTarget.get('layer').get('source');
        var cswRecords = knownLayer.getRelatedCSWRecordsByKeywords(iLineId);
        var cswRecord = cswRecords.length > 0 ? cswRecords[0] : null;

        //There just might not be any metadata for this record
        if (!cswRecord) {
            return this.generateErrorComponent(Ext.util.Format.format('There is no metadata registered for line "{0}"', iLineId), 'Summary');
        }

        var description = cswRecord.get('description');
        var url = cswRecord.get('onlineResources')[0].get('url');
        var name = cswRecord.get('onlineResources')[0].get('name');

        return Ext.create('portal.layer.querier.BaseComponent',{
            layout : 'fit',
            items : [{
                xtype : 'fieldset',
                border : false,
                autoScroll:true,
                labelWidth: 75,
                layout:'anchor',

                items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'ID',
                    value : '<a href="'+ queryTarget.get('cswRecord').get('recordInfoUrl') +'" TARGET=_blank>' + iLineId + '</a>'
                },{
                    xtype : 'textarea',
                    fieldLabel : 'Description',
                    readOnly : true,
                    anchor : '100%',
                    height : 200,
                    value :  description
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'HighRes Service URL',
                    value : url
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'HighRes Layer Name',
                    value : name
                }]
            }]
        });
    },

    /**
     * Utility function for performing a followup query for more information about the line with specified id
     *
     * This function will append to cmpList and eventually pass the contents of cmpList to callback
     */
    _queryForSegyDatasets : function(queryTarget, callback, gtServiceUrl, lineId, cmpList) {
        var queryString = Ext.Object.toQueryString({
            lineId : lineId
        });
        var segyDatasetsUrl = Ext.urlAppend(gtServiceUrl + this.GETSEGYDATASETS, queryString);

        Ext.Ajax.request({
            url: 'requestGeotransectsData.do',
            timeout : 180000,
            params  : {
                serviceUrl : segyDatasetsUrl
            },
            scope : this,
            callback: function(options, success, response) {
                if (!success) {
                    cmpList.push(this.generateErrorComponent('There was an error whilst communicating with the geotransects data server'));
                    callback(this, cmpList, queryTarget);
                    return;
                }

                try {
                    responseObj = Ext.JSON.decode(Ext.JSON.decode(response.responseText).json);
                } catch (err) {
                    cmpList.push(this.generateErrorComponent('There was an error whilst communicating with the geotransects data server'));
                    callback(this, cmpList, queryTarget);
                    return;
                }

                //Generate an error / success fragment to display to the user
                if (!responseObj.result.success) {
                    cmpList.push(this.generateErrorComponent('The service returned a failure result status: ' + seismicSectionsUrl));
                    callback(this, cmpList, queryTarget);
                    return;
                }

                var urlList = [];
                for (var i = 0; i < responseObj.items.length; i++) {
                    urlList.push(responseObj.items[i].url);
                }
                cmpList.push(this._getDownloadTab(urlList, 'SegY Data'));
                callback(this, cmpList, queryTarget);
            }
        });
    },

    /**
     * Utility function for performing a followup query for more information about the line with specified id
     *
     * This function will append to cmpList and eventually pass the contents of cmpList to callback
     */
    _queryForSeismicSections : function(queryTarget, callback, gtServiceUrl, lineId, cmpList) {
        var queryString = Ext.Object.toQueryString({
            lineId : lineId
        });
        var seismicSectionsUrl = Ext.urlAppend(gtServiceUrl + this.GETSEISMICSECTIONS, queryString);

        Ext.Ajax.request({
            url: 'requestGeotransectsData.do',
            timeout : 180000,
            params  : {
                serviceUrl : seismicSectionsUrl
            },
            scope : this,
            callback: function(options, success, response) {
                if (!success) {
                    cmpList.push(this.generateErrorComponent('There was an error whilst communicating with the geotransects data server'));
                    callback(this, cmpList, queryTarget);
                    return;
                }

                try {
                    responseObj = Ext.JSON.decode(Ext.JSON.decode(response.responseText).json);
                } catch (err) {
                    cmpList.push(this.generateErrorComponent('There was an error whilst communicating with the geotransects data server'));
                    callback(this, cmpList, queryTarget);
                    return;
                }

                //Generate an error / success fragment to display to the user
                if (!responseObj.result.success) {
                    cmpList.push(this.generateErrorComponent('The service returned a failure result status: ' + seismicSectionsUrl));
                    callback(this, cmpList, queryTarget);
                    return;
                }


                //Parse records and download the data
                var urlList = [];
                for (var i = 0; i < responseObj.items.length; i++) {
                    urlList.push(responseObj.items[i].url);
                }
                cmpList.push(this._getDownloadTab(urlList, 'Seismic Data'));

                //Lookup segy data
                this._queryForSegyDatasets(queryTarget, callback, gtServiceUrl, lineId, cmpList)
            }
      });
    },

    /**
     * See parent class for definition
     *
     * Makes a WMS request, waits for the response and then parses it passing the results to callback
     */
    query : function(queryTarget, callback) {
        var proxyUrl = this.generateWmsProxyQuery(queryTarget, 'application/vnd.ogc.gml');

        //Start off by making a request for the GML at the specified location
        //We need to extract the survey line ID of the place we clicked
        Ext.Ajax.request({
            url : proxyUrl,
            timeout : 180000,
            scope : this,
            callback : function(options, success, response) {
                if (!success) {
                    callback(this, [this.generateErrorComponent('There was an error when attempting to contact the remote WMS instance for information about this point.')], queryTarget);
                    return;
                }

                //TODO: There is a convergence here between this and the WFSQuerier (parsing a wfs:FeatureCollection)
                var domDoc = portal.util.xml.SimpleDOM.parseStringToDOM(response.responseText);
                var featureMemberNodes = portal.util.xml.SimpleDOM.getMatchingChildNodes(domDoc.documentElement, 'http://www.opengis.net/gml', 'featureMember');
                if (featureMemberNodes.length === 0) {
                    featureMemberNodes = portal.util.xml.SimpleDOM.getMatchingChildNodes(domDoc.documentElement, 'http://www.opengis.net/gml', 'featureMembers');
                }
                if (featureMemberNodes.length === 0 || featureMemberNodes[0].childNodes.length === 0) {
                    //we got an empty response - likely because the feature ID DNE.
                    callback(this, [], queryTarget);
                    return;
                }

                var featureTypeRoot = featureMemberNodes[0].childNodes[0];

                //Extract the line ID of what we clicked
                var lineId = portal.util.xml.SimpleXPath.evaluateXPathString(featureTypeRoot, 'gt:SURV_LINE');
                if (lineId.indexOf("cdp") === 0) {
                    lineId = lineId.substring(3, lineId.length);
                }

                //Build our first tab
                var cmpList = [];
                cmpList.push(this._getSummaryTab(queryTarget, lineId));

                //Figure out our geotransects URL
                //Assumption - The geotransect-dataservices is at the same host as the WMS
                var wmsUrl = queryTarget.get('onlineResource').get('url');
                var wmsUrlNoProtocol = wmsUrl.slice(("http://").length);
                var gtServiceUrl = 'http://' + wmsUrlNoProtocol.slice(0,wmsUrlNoProtocol.indexOf("/")) + this.GEOTRANSECTDATASERVICE;

                this._queryForSeismicSections(queryTarget, callback, gtServiceUrl, lineId, cmpList);
            }
        });
    }
});
