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
		var recordLink = this.cswRecord.getRecordInfoUrl();
		var recordLinkName = "Link back to registry";
		var sHtml = '<div class="niceDivOverFlow">' +
			'<table border="1" cellspacing="1" cellpadding="4" class="auscopeTable">' +
			"<tr><td>" +
		     "<a href='" + recordLink + "' target='_blank'>" +
		     recordLinkName +
		     "</a>" +
		     "</td></tr>"+
			'<tr><td id="reportsname">' +
			this.cswRecord.getServiceName() +
			'</td></tr>' +
			'<tr><td id="reportsabstract">' +
			this.cswRecord.getDataIdentificationAbstract() +
			'</td></tr>';

		var wwwLinks = this.cswRecord.getFilteredOnlineResources('WWW');
		for(var i=0; i< wwwLinks.length; i++) {
			var linkName = wwwLinks[i].name !== null && wwwLinks[i].name !== "" ? wwwLinks[i].name : wwwLinks[i].url;
			sHtml += "<tr><td>" +
				     "<a href='"	+ wwwLinks[i].url + "' target='_blank'>" +
				     linkName +
				     "</a>" +
				     "</td></tr>";
		}
		

		sHtml += "</table></div>";
		if (this.overlay instanceof GPolygon) {
			this.map.openInfoWindowHtml(this.overlay.getBounds().getCenter(), sHtml,
					{maxWidth:800, maxHeight:400, autoScroll:true});
		} else if (this.overlay instanceof GMarker) {
			this.map.openInfoWindowHtml(this.overlay.getPoint(), sHtml,
					{maxWidth:800, maxHeight:400, autoScroll:true});
		}
	}
};