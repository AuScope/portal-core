/**
* @fileoverview This file declares the Class WmsLayer.
* Object of this class will be created for each WebMapService.
*/

/**
* @class
* This class defines information for each queryable layer belonging to a web map service.
*
* @constructor
* @param {String} name The Name of the layer.
* @param {String} title The Title of the layer.
* @param {String} abstract The Abstract of the layer.
* @return A new {@link WmsLayer}
*/
function WmsLayer(name, title, abstract) {
  this.msName = name;
  this.msTitle = title;
  this.msAbstract = abstract;
  this.mbIsVisible = 0;

}

/**
* Name of the layer
* @type String
*/
WmsLayer.prototype.msName = null;

/**
* Title of the layer
* @type String
*/
WmsLayer.prototype.msTitle = null;

/**
* Abstract of the layer
* @type String
*/
WmsLayer.prototype.msAbstract = null;

/**
* Flag indicating the visibility state for this layer.
* 0 - layer is hidden on the map
* 1 - layer is visible on the map
* @type Boolean
*/
WmsLayer.prototype.mbIsVisible = null;


 