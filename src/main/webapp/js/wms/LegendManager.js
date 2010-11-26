
/**
 * The LegendManager class is a utility class that helps in the generation
 * of WMS GetLegendGraphic Requests
 *
 * url - The URL of the WMS service to query
 * layerName - the name of the layer whose legend you wish to fetch
 * styles - [Optional] value for the styles parameter
 */
function LegendManager(url, layerName, styles) {
	this.url = url;
	this.layerName = layerName;
	this.styles = styles;
}

LegendManager.prototype.url = null;
LegendManager.prototype.layerName = null;
LegendManager.prototype.styles = null;

/**
 * Generates and returns the URL that can be used to request a PNG Legend from the
 * WMS
 * @return
 */
LegendManager.prototype.generateImageUrl = function() {
	var url = this.url;

	var last_char = url.charAt(url.length - 1);
	if ((last_char !== "?") && (last_char !== "&")) {
      if (url.indexOf('?') == -1) {
         url += "?";
      } else {
         url += "&";
      }
	}

	url += 'REQUEST=GetLegendGraphic';
	url += '&SERVICE=WMS';
	url += '&VERSION=1.1.1';
	url += '&FORMAT=image/png';
	url += '&BGCOLOR=0xFFFFFF';
	url += '&LAYER=' + escape(this.layerName);
	url += '&LAYERS=' + escape(this.layerName);
	if (this.styles) {
		url += '&STYLES=' + escape(this.styles);
	}

	return url;
};
