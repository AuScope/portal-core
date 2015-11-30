/**
 * Control for allow a user to draw a bounding box representing an area to be selected for subdivision
 *
 * Adapted from: GmapSubsetControl Class v1.3 - original copyright 2005-2007, Andre Lewis, andre@earthcode.com
 *
 */
function GmapSubsetControl(dragEndCallback) {
  this.globals = {
      buttonStyle: {
        width: '80px',
        border: '1px solid black',
        padding: '1px',
        borderWidth: '1px',
        fontFamily: 'Arial,sans-serif',
        fontSize: '12px',
        textAlign: 'center',
        background: '#A0D4FF'
      },

      buttonSubsetStyle : {
        background: '#FF0'
      },

      buttonHtml : 'Select Data',
      buttonId : 'gmap-subset-control',
      buttonSubsetHtml : 'Click and drag a region of interest',

      boxStyle : {
        outlineWidth : 2,
        opacity: .2,
          fillColor: "#000",
          border: "2px solid blue",
          outlineColor : 'blue',
          alphaIE : 'alpha(opacity=20)'
      },

      minDragSize: 0,
      overlayRemoveTime: 6000,

      dragEndCallback : dragEndCallback
  };
};

GmapSubsetControl.prototype = new GControl();

/**
 * Sets button mode to zooming or otherwise, changes CSS & HTML.
 * @param {String} mode Either "zooming" or not.
 */
GmapSubsetControl.prototype.setButtonMode_ = function(mode){
  var G = this.globals;
  if (mode == 'subset') {
    G.buttonDiv.innerHTML = G.buttonSubsetHtml;
    GmapSubsetUtil.style([G.buttonDiv], G.buttonStyle);
    GmapSubsetUtil.style([G.buttonDiv], G.buttonSubsetStyle);
  } else {
    G.buttonDiv.innerHTML = G.buttonHtml;
    GmapSubsetUtil.style([G.buttonDiv], G.buttonStyle);
  }
};


GmapSubsetControl.prototype.initButton_ = function(buttonContainerDiv) {
  var G = this.globals;
  var buttonDiv = document.createElement('div');
  buttonDiv.innerHTML = G.buttonHtml;
  buttonDiv.id = G.buttonId;
  GmapSubsetUtil.style([buttonDiv], {cursor: 'pointer', zIndex:200});
  GmapSubsetUtil.style([buttonDiv], G.buttonStyle);
  buttonContainerDiv.appendChild(buttonDiv);
  return buttonDiv;
};

GmapSubsetControl.prototype.initialize = function(map) {
  var G = this.globals;
  var me = this;
  var mapDiv = map.getContainer();

  // Create div for selection box button
  var buttonContainerDiv = document.createElement("div");
  GmapSubsetUtil.style([buttonContainerDiv], {cursor: 'pointer', zIndex: 150});

  // create and init the zoom button
  //DOM:button
  var buttonDiv = this.initButton_(buttonContainerDiv);

  // Add the two buttons to the map
  mapDiv.appendChild(buttonContainerDiv);

  //DOM:map covers
  var zoomDiv = document.createElement("div");
  var DIVS_TO_CREATE = ['outlineDiv', 'cornerTopDiv', 'cornerLeftDiv', 'cornerRightDiv', 'cornerBottomDiv'];
  for (var i=0; i<DIVS_TO_CREATE.length; i++) {
    var id = DIVS_TO_CREATE[i];
    var div = document.createElement("div");
    GmapSubsetUtil.style([div], {position: 'absolute', display: 'none'});
    zoomDiv.appendChild(div);
    G[id] = div;
  }
  GmapSubsetUtil.style([zoomDiv], {position: 'absolute', display: 'none', overflow: 'hidden', cursor: 'crosshair', zIndex: 101});
  mapDiv.appendChild(zoomDiv);

  // add event listeners
  GEvent.addDomListener(buttonDiv, 'click', function(e) {
    me.buttonclick_(e);
  });
  GEvent.addDomListener(zoomDiv, 'mousedown', function(e) {
    me.coverMousedown_(e);
  });
  GEvent.addDomListener(document, 'mousemove', function(e) {
    me.drag_(e);
  });
  GEvent.addDomListener(document, 'mouseup', function(e) {
    me.mouseup_(e);
  });

  //get globals
  G.mapPosition = GmapSubsetUtil.getElementPosition(mapDiv);
  G.buttonDiv = buttonDiv;
  G.mapCover = zoomDiv;
  G.map = map;

  G.borderCorrection = G.boxStyle.outlineWidth * 2;
  this.setDimensions_();

  //styles
  this.initStyles_();

  // disable text selection on map cover
  G.mapCover.onselectstart = function() {return false;};

  return buttonContainerDiv;
};

/**
 * Required by GMaps API for controls.
 * @return {GControlPosition} Default location for control
 */
GmapSubsetControl.prototype.getDefaultPosition = function() {
  return new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(3, 120));
};

/**
 * Function called when mousedown event is captured.
 * @param {Object} e
 */
GmapSubsetControl.prototype.coverMousedown_ = function(e){
  var G = this.globals;
  var pos = this.getRelPos_(e);
  G.startX = pos.left;
  G.startY = pos.top;

  GmapSubsetUtil.style([G.mapCover], {background: 'transparent', opacity: 1, filter: 'alpha(opacity=100)'});
  GmapSubsetUtil.style([G.outlineDiv], {left: G.startX + 'px', top: G.startY + 'px', display: 'block', width: '1px', height: '1px'});
  G.draggingOn = true;

  G.cornerTopDiv.style.top = (G.startY - G.mapHeight) + 'px';
  G.cornerTopDiv.style.display ='block';
  G.cornerLeftDiv.style.left = (G.startX - G.mapWidth) +'px';
  G.cornerLeftDiv.style.top = G.startY + 'px';
  G.cornerLeftDiv.style.display = 'block';

  G.cornerRightDiv.style.left = G.startX + 'px';
  G.cornerRightDiv.style.top = G.startY + 'px';
  G.cornerRightDiv.style.display = 'block';
  G.cornerBottomDiv.style.left = G.startX + 'px';
  G.cornerBottomDiv.style.top = G.startY + 'px';
  G.cornerBottomDiv.style.width = '0px';
  G.cornerBottomDiv.style.display = 'block';

  return false;
};

/**
 * Function called when drag event is captured
 * @param {Object} e
 */
GmapSubsetControl.prototype.drag_ = function(e){
  var G = this.globals;
  if(G.draggingOn) {
    var pos = this.getRelPos_(e);
    var rect = this.getRectangle_(G.startX, G.startY, pos, G.mapRatio);

    if (rect.left) {
      addX = -rect.width;
    } else {
      addX = 0;
    }

    if (rect.top) {
      addY = -rect.height;
    } else {
      addY = 0;
    }

    GmapSubsetUtil.style([G.outlineDiv], {left: G.startX + addX + 'px', top: G.startY + addY + 'px', display: 'block', width: '1px', height: '1px'});

    G.outlineDiv.style.width = rect.width + "px";
    G.outlineDiv.style.height = rect.height + "px";

    G.cornerTopDiv.style.height = ((G.startY + addY) - (G.startY - G.mapHeight)) + 'px';
    G.cornerLeftDiv.style.top = (G.startY + addY) + 'px';
    G.cornerLeftDiv.style.width = ((G.startX + addX) - (G.startX - G.mapWidth)) + 'px';
    G.cornerRightDiv.style.top = G.cornerLeftDiv.style.top;
    G.cornerRightDiv.style.left = (G.startX + addX + rect.width + G.borderCorrection) + 'px';
    G.cornerBottomDiv.style.top = (G.startY + addY + rect.height + G.borderCorrection) + 'px';
    G.cornerBottomDiv.style.left = (G.startX - G.mapWidth + ((G.startX + addX) - (G.startX - G.mapWidth))) + 'px';
    G.cornerBottomDiv.style.width = (rect.width + G.borderCorrection) + 'px';

    return false;
  }
};

/**
 * Function called when mouseup event is captured
 * @param {Event} e
 */
GmapSubsetControl.prototype.mouseup_ = function(e){
  var G = this.globals;
  if (G.draggingOn) {
    var pos = this.getRelPos_(e);
    G.draggingOn = false;

    var rect = this.getRectangle_(G.startX, G.startY, pos, G.mapRatio);

    if (rect.left) rect.endX = rect.startX - rect.width;
    if (rect.top) rect.endY = rect.startY - rect.height;

    this.resetDragZoom_();

    if (rect.width >= G.minDragSize && rect.height >= G.minDragSize) {
      var nwpx = new GPoint(rect.startX, rect.startY);
      var nepx = new GPoint(rect.endX, rect.startY);
      var sepx = new GPoint(rect.endX, rect.endY);
      var swpx = new GPoint(rect.startX, rect.endY);
      var nw = G.map.fromContainerPixelToLatLng(nwpx);
      var ne = G.map.fromContainerPixelToLatLng(nepx);
      var se = G.map.fromContainerPixelToLatLng(sepx);
      var sw = G.map.fromContainerPixelToLatLng(swpx);

      var zoomAreaPoly = new GPolyline([nw, ne, se, sw, nw], G.boxStyle.outlineColor, G.boxStyle.outlineWidth + 1,.4);

      try{
        G.map.addOverlay(zoomAreaPoly);
        setTimeout (function() {G.map.removeOverlay(zoomAreaPoly);}, G.overlayRemoveTime);
      }catch(e) {}

      var polyBounds = zoomAreaPoly.getBounds();
      var ne = polyBounds.getNorthEast();
      var sw = polyBounds.getSouthWest();
      var se = new GLatLng(sw.lat(), ne.lng());
      var nw = new GLatLng(ne.lat(), sw.lng());

      // invoke callback if provided
      if (G.dragEndCallback) {
        G.dragEndCallback(nw, ne, se, sw, nwpx, nepx, sepx, swpx);
      }
    }
  }
};

/**
 * Set the cover sizes according to the size of the map
 */
GmapSubsetControl.prototype.setDimensions_ = function() {
  var G = this.globals;
  var mapSize = G.map.getSize();
  G.mapWidth  = mapSize.width;
  G.mapHeight = mapSize.height;
  G.mapRatio  = G.mapHeight / G.mapWidth;
  // set left:0px in next <div>s in case we inherit text-align:center from map <div> in IE.
  GmapSubsetUtil.style([G.mapCover, G.cornerTopDiv, G.cornerRightDiv, G.cornerBottomDiv, G.cornerLeftDiv],
      {top: '0px', left: '0px', width: G.mapWidth + 'px', height: G.mapHeight +'px'});
};

/**
 * Initializes styles based on global parameters
 */
GmapSubsetControl.prototype.initStyles_ = function(){
  var G = this.globals;
  GmapSubsetUtil.style([G.mapCover, G.cornerTopDiv, G.cornerRightDiv, G.cornerBottomDiv, G.cornerLeftDiv],
    {filter: G.boxStyle.alphaIE, opacity: G.boxStyle.opacity, background:G.boxStyle.fillColor});
  G.outlineDiv.style.border = G.boxStyle.border;
};

/**
 * Function called when the zoom button's click event is captured.
 */
GmapSubsetControl.prototype.buttonclick_ = function(){
  var G = this.globals;
  if (G.mapCover.style.display == 'block') { // reset if clicked before dragging
    this.resetDragZoom_();
  } else {
    this.initCover_();
  }
};

/**
 * Shows the cover over the map
 */
GmapSubsetControl.prototype.initCover_ = function(){
  var G = this.globals;
  G.mapPosition = GmapSubsetUtil.getElementPosition(G.map.getContainer());
  this.setDimensions_();
  this.setButtonMode_('subset');
  GmapSubsetUtil.style([G.mapCover], {display: 'block', background: G.boxStyle.fillColor});
  GmapSubsetUtil.style([G.outlineDiv], {width: '0px', height: '0px'});
};

/**
 * Gets position of the mouse relative to the map
 * @param {Object} e
 */
GmapSubsetControl.prototype.getRelPos_ = function(e) {
  var pos = GmapSubsetUtil.getMousePosition(e);
  var G = this.globals;
  return {top: (pos.top - G.mapPosition.top),
          left: (pos.left - G.mapPosition.left)};
};

/**
 * Figures out the rectangle the user's trying to draw
 * @param {Number} startX
 * @param {Number} startY
 * @param {Object} pos
 * @param {Number} ratio
 * @return {Object} Describes the rectangle
 */
GmapSubsetControl.prototype.getRectangle_ = function(startX, startY, pos, ratio) {
  var left = false;
  var top = false;
  var dX = pos.left - startX;
  var dY = pos.top - startY;

  var rect = {
    startX: Math.min(startX, pos.left),
    startY: Math.min(startY, pos.top),
    endX: Math.max(startX, pos.left),
    endY: Math.max(startY, pos.top),
    left:left,
    top:top
  };

  rect.width = rect.endX - rect.startX;
  rect.height = rect.endY - rect.startY;
  return rect;
};

/**
 * Resets CSS and button display when drag zoom done
 */
GmapSubsetControl.prototype.resetDragZoom_ = function() {
  var G = this.globals;
  GmapSubsetUtil.style([G.mapCover, G.cornerTopDiv, G.cornerRightDiv, G.cornerBottomDiv, G.cornerLeftDiv],
    {display: 'none', opacity: G.boxStyle.opacity, filter: G.boxStyle.alphaIE});
  G.outlineDiv.style.display = 'none';
  this.setButtonMode_('normal');
};

var GmapSubsetUtil={};

/**
 * Alias function for getting element by id
 * @param {String} sId
 * @return {Object} DOM object with sId id
 */
GmapSubsetUtil.gE = function(sId) {
  return document.getElementById(sId);
};

/**
 * A general-purpose function to get the absolute position
 * of the mouse.
 * @param {Object} e  Mouse event
 * @return {Object} Describes position
 */
GmapSubsetUtil.getMousePosition = function(e) {
  var posX = 0;
  var posY = 0;
  if (!e) var e = window.event;
  if (e.pageX || e.pageY) {
    posX = e.pageX;
    posY = e.pageY;
  } else if (e.clientX || e.clientY){
    posX = e.clientX +
      (document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft);
    posY = e.clientY +
      (document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop);
  }
  return {left: posX, top: posY};
};

/**
 * Gets position of element
 * @param {Object} element
 * @return {Object} Describes position
 */
GmapSubsetUtil.getElementPosition = function(element) {
  var leftPos = element.offsetLeft;          // initialize var to store calculations
  var topPos = element.offsetTop;            // initialize var to store calculations
  var parElement = element.offsetParent;     // identify first offset parent element
  while (parElement != null ) {                // move up through element hierarchy
    leftPos += parElement.offsetLeft;      // appending left offset of each parent
    topPos += parElement.offsetTop;
    parElement = parElement.offsetParent;  // until no more offset parents exist
  }
  return {left: leftPos, top: topPos};
};

/**
 * Applies styles to DOM objects
 * @param {String/Object} elements Either comma-delimited list of ids
 *   or an array of DOM objects
 * @param {Object} styles Hash of styles to be applied
 */
GmapSubsetUtil.style = function(elements, styles){
  if (typeof(elements) == 'string') {
    elements = GmapSubsetUtil.getManyElements(elements);
  }
  for (var i = 0; i < elements.length; i++){
    for (var s in styles) {
      elements[i].style[s] = styles[s];
    }
  }
};

/**
 * Gets DOM elements array according to list of IDs
 * @param {String} elementsString Comma-delimited list of IDs
 * @return {Array} Array of DOM elements corresponding to s
 */
GmapSubsetUtil.getManyElements = function(idsString){
  var idsArray = idsString.split(',');
  var elements = [];
  for (var i = 0; i < idsArray.length; i++){
    elements[elements.length] = GmapSubsetUtil.gE(idsArray[i]);
  };
  return elements;
};