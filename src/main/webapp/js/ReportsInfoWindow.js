/**
 * @class ReportsInfoWindow
 * 
 * @author jac24m
 */

/**
 * @constructor
 */
function ReportsInfoWindow(iMap, iOverlay) {
    this.map = iMap; 
    this.overlay = iOverlay;
}

ReportsInfoWindow.prototype = {
	    
	'show': function() {
		var sHtml = "<div style=\"padding-bottom:10px;\" >" 
			+ "<table border=\"1\" cellspacing=\"1\" width=\"100%\" bgcolor=\"#EAF0F8\">" 
			+ "<pre style=\"white-space:pre-wrap;white-space:-moz-pre-wrap;" +
				"white-space:-pre-wrap;white-space:-o-pre-wrap;word-wrap:break-word;" +
				"width:99%;overflow:auto;\">"
			+ "<tr><td>" 
			+ this.overlay.title
			+ "</td></tr>"
			+ "<tr><td>" 
			+ this.overlay.description 
			+ "</td></tr>";
		
		if(this.overlay.serviceURLs != null && this.overlay.serviceURLs.length > 0) {
			for(var i=0; i<this.overlay.serviceURLs.length; i++) {
				sHtml += "<tr><td>" 
					+ "<a href='"	+ this.overlay.serviceURLs[i] + "' target='_blank'>" 
						+ this.overlay.serviceURLs[i] + "</a>";
					+ "</td></tr>";
			}
		}
			
		sHtml += "</pre>"
			+ "</table>"
			+ "</div>";

		this.map.openInfoWindowHtml(this.overlay.getBounds().getCenter(), sHtml, 
				{maxWidth:800, maxHeight:300, autoScroll:true});
	}
};