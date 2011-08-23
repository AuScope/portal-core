/**
 * @class ReportsInfoWindow
 *
 * @author jac24m
 */

/**
 * @constructor
 */
function ReportsInfoWindow(iMap, iOverlay, iCSWRecord) {
    this.map = iMap;
    this.overlay = iOverlay;
    this.cswRecord = iCSWRecord;
}

ReportsInfoWindow.prototype = {
//'<div style="padding-bottom:30px;white-space:pre-wrap;white-space:-moz-pre-wrap;' +
//		'white-space:-pre-wrap;white-space:-o-pre-wrap;word-wrap:break-word;' +
//		'width:99%;max-height:300px;overflow:auto;">' +

    'show': function() {
        var divId = 'reports-binding-id';
        var sHtml = '';
        var maxWidth = 600;
        var maxHeight = 300;

        sHtml += '<div id="' + divId + '" style="width: ' + maxWidth + 'px; height: ' + maxHeight  +'px;"/>';

        var renderMetadataPanel = function() {
            this.reportsPanel = new Ext.form.FormPanel({
                width : maxWidth - 20,
                height : maxHeight - 20,
                renderTo : divId,
                hideBorders : true,
                layout : 'auto',
                autoScroll : true,
                items : [{
                    xtype : 'cswmetadatapanel',
                    hideBorder : true,
                    cswRecord : this.cswRecord,
                    bodyStyle : {
                        'background-color' : '#ffffff'
                    }
                }]
            });
        }

        var destroyReportsPanel = function() {
            this.reportsPanel.destroy();
        }

        if (this.overlay instanceof GPolygon) {
            this.map.openInfoWindowHtml(this.overlay.getBounds().getCenter(), sHtml, {
                maxWidth:maxWidth,
                //maxHeight:maxHeight,
                autoScroll:true,
                onOpenFn : renderMetadataPanel.createDelegate(this),
                onCloseFn : destroyReportsPanel.createDelegate(this)
            });
        } else if (this.overlay instanceof GMarker) {
            this.map.openInfoWindowHtml(this.overlay.getPoint(), sHtml,{
                maxWidth:maxWidth,
                //maxHeight:maxHeight,
                autoScroll:true,
                onOpenFn : renderMetadataPanel.createDelegate(this),
                onCloseFn : destroyReportsPanel.createDelegate(this)
            });
        }


    }
};