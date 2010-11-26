/*
Copyright (c) 2005-2006, Andre Lewis, andre@earthcode.com
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided
that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of "Andre Lewis" nor the names of contributors to this software may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/*
GZoom custom map control. Version 0.2 Released 11/21/06

To use:
  oMap = new GMap2($id("large-google-map"));
  oMap.addControl(new GMapTypeControl());

Or with options:
  oMap.addControl(new GZoomControl({sColor:'#000',nOpacity:.3,sBorder:'1px solid yellow'}), new GControlPosition(G_ANCHOR_TOP_RIGHT,new GSize(10,10)));

More info at http://earthcode.com
*/

// base definition and inheritance
function GZoomControl(oBoxStyle,oOptions,oCallbacks) {
	//box style options
  GZoomControl.G.style = {
    nOpacity:0.2,
    sColor:"#000",
    sBorder:"2px solid blue"
  };
  var style=GZoomControl.G.style;
  for (var s in oBoxStyle) {style[s]=oBoxStyle[s];}
  var aStyle=style.sBorder.split(' ');
  style.nOutlineWidth=parseInt(aStyle[0].replace(/\D/g,''));
  style.sOutlineColor=aStyle[2];
  style.sIEAlpha='alpha(opacity='+(style.nOpacity*100)+')';

	// Other options
	GZoomControl.G.options={
		bForceCheckResize:false,
		sButtonHTML: 'Zoom Box',
		oButtonStartingStyle:{width:'68px',height:'11px',border:'1px dotted #3366CC',padding:'0px 2px 3px 5px',color:'#3366CC'},
		oButtonStyle:{background:'#FFF'},
		sButtonZoomingHTML:'Drag Box',
		//oButtonZoomingStyle:{background:'#FF0'},
		nOverlayRemoveMS:60000,
		bStickyZoom:false
	};

	for (var s in oOptions) {GZoomControl.G.options[s]=oOptions[s];}

	// callbacks: buttonClick, dragStart,dragging, dragEnd
	if (oCallbacks === null) {oCallbacks={};}
	GZoomControl.G.callbacks=oCallbacks;
}

GZoomControl.prototype = new GControl();
//class globals
GZoomControl.G={
  oZoomArea:null,
  bDragging:false,
  mct:null,
  mcr:null,
  mcb:null,
  mcl:null,
	oMapPos:null,
	oOutline:null,
	nMapWidth:0,
	nMapHeight:0,
	nMapRatio:0,
	nStartX:0,
	nStartY:0,
	nBorderCorrect:0
};

GZoomControl.prototype.initButton_=function(oMapContainer) {
	var G=GZoomControl.G;
	var oButton = document.createElement('div');
	oButton.innerHTML=G.options.sButtonHTML;
	oButton.id='gzoom-control';
	acl.style([oButton],{cursor:'pointer',zIndex:1400});
	acl.style([oButton],G.options.oButtonStartingStyle);
	acl.style([oButton],G.options.oButtonStyle);
	oMapContainer.appendChild(oButton);
	return oButton;
};

GZoomControl.prototype.setButtonMode_=function(sMode){
	var G=GZoomControl.G;
	if (sMode=='zooming') {
		G.oButton.innerHTML=G.options.sButtonZoomingHTML;
		acl.style([G.oButton],G.options.oButtonZoomingStyle);
	} else {
		G.oButton.innerHTML=G.options.sButtonHTML;
		acl.style([G.oButton],G.options.oButtonStyle);
	}
};

// ******************************************************************************************
// Methods required by Google maps -- initialize and getDefaultPosition
// ******************************************************************************************
GZoomControl.prototype.initialize = function(oMap) {
  var G=GZoomControl.G;
	var oMC=oMap.getContainer();
	var oMCC=document.getElementById('map');
  //DOM:button
	var oButton=this.initButton_(oMC);

	//DOM:map covers
	var o = document.createElement("div");
  o.id='gzoom-map-cover';
	o.innerHTML='<div id="gzoom-outline" style="position:absolute;display:none;"></div><div id="gzoom-mct" style="position:absolute;display:none;"></div><div id="gzoom-mcl" style="position:absolute;display:none;"></div><div id="gzoom-mcr" style="position:absolute;display:none;"></div><div id="gzoom-mcb" style="position:absolute;display:none;"></div>';
	acl.style([o],{position:'absolute',display:'none',overflow:'hidden',cursor:'crosshair',zIndex:101});
	oMC.appendChild(o);

  // add event listeners
	GEvent.addDomListener(oButton, 'click', GZoomControl.prototype.buttonClick_);
	GEvent.addDomListener(o, 'mousedown', GZoomControl.prototype.coverMousedown_);
	GEvent.addDomListener(document, 'mousemove', GZoomControl.prototype.drag_);
	GEvent.addDomListener(document, 'mouseup', GZoomControl.prototype.mouseup_);

  // get globals
	G.oMapPos=acl.getElementPosition(oMap.getContainer());
	G.oOutline=$id("gzoom-outline");
	G.oButton=$id("gzoom-control");
	G.mc=$id("gzoom-map-cover");
	G.mct=$id("gzoom-mct");
	G.mcr=$id("gzoom-mcr");
	G.mcb=$id("gzoom-mcb");
	G.mcl=$id("gzoom-mcl");
	G.oMap = oMap;

	G.nBorderCorrect = G.style.nOutlineWidth*2;
  this.setDimensions_();

  //styles
  this.initStyles_();

  debug("Finished Initializing gzoom control");
  return oButton;
};

// Default location for the control
GZoomControl.prototype.getDefaultPosition = function() {
  return new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(10, 10));
};

// ******************************************************************************************
// Private methods
// ******************************************************************************************
GZoomControl.prototype.coverMousedown_ = function(e){
  var G=GZoomControl.G;
  if (G.oZoomArea !== null) {G.oMap.removeOverlay(G.oZoomArea);}

  var oPos = GZoomControl.prototype.getRelPos_(e);
  debug("Mouse down at "+oPos.left+", "+oPos.top);
  G.nStartX=oPos.left;
  G.nStartY=oPos.top;

	acl.style([G.mc],{background:'transparent',opacity:1,filter:'alpha(opacity=100)'});
  acl.style([G.oOutline],{left:G.nStartX+'px',top:G.nStartY+'px',display:'block',width:'1px',height:'1px'});
  G.bDragging=true;

  G.mct.style.top=(G.nStartY-G.nMapHeight)+'px';
  G.mct.style.display='block';
  G.mcl.style.left=(G.nStartX-G.nMapWidth)+'px';
  G.mcl.style.top=(G.nStartY)+'px';
  G.mcl.style.display='block';

  G.mcr.style.left=(G.nStartX)+'px';
  G.mcr.style.top=(G.nStartY)+'px';
  G.mcr.style.display='block';
  G.mcb.style.left=(G.nStartX)+'px';
  G.mcb.style.top=(G.nStartY)+'px';
  G.mcb.style.width='0px';
  G.mcb.style.display='block';

	// invoke the callback if provided
	if (G.callbacks.dragStart !==null){G.callbacks.dragStart(G.nStartX,G.nStartY);}

  debug("mouse down done");
  return false;
};


GZoomControl.prototype.drag_=function(e){
  var G=GZoomControl.G;
  if(G.bDragging) {
    var oPos=GZoomControl.prototype.getRelPos_(e);
    oRec = GZoomControl.prototype.getRectangle_(G.nStartX,G.nStartY,oPos,G.nMapRatio);
    G.oOutline.style.width=oRec.nWidth+"px";
    G.oOutline.style.height=oRec.nHeight+"px";

    G.mcr.style.left=(oRec.nEndX+G.nBorderCorrect-4)+'px';
    G.mcb.style.top=(oRec.nEndY+G.nBorderCorrect-4)+'px';
    G.mcb.style.width=(oRec.nWidth+G.nBorderCorrect-4)+'px';

    var nepx = new GPoint(oRec.nEndX, oRec.nStartY);
	var swpx = new GPoint(oRec.nStartX, oRec.nEndY);
    var ne = G.oMap.fromContainerPixelToLatLng(nepx);
    var sw = G.oMap.fromContainerPixelToLatLng(swpx);

	// invoke callback if provided
	if (G.callbacks.dragging !==null){
	  G.callbacks.dragging(ne, sw);
	}

    return false;
  }
};



GZoomControl.prototype.mouseup_=function(e){
  var G=GZoomControl.G;
  if (G.bDragging) {
    var oPos = GZoomControl.prototype.getRelPos_(e);
    G.bDragging=false;

    var oRec = GZoomControl.prototype.getRectangle_(G.nStartX,G.nStartY,oPos,G.nMapRatio);
    debug("mouse up at "+oRec.nEndX+", "+oRec.nEndY+". Height/width="+oRec.nWidth+","+oRec.nHeight);

    GZoomControl.prototype.resetDragZoom_();

    var nwpx=new GPoint(oRec.nStartX,oRec.nStartY);
    var nepx=new GPoint(oRec.nEndX,oRec.nStartY);
    var sepx=new GPoint(oRec.nEndX,oRec.nEndY);
    var swpx=new GPoint(oRec.nStartX,oRec.nEndY);
    var nw = G.oMap.fromContainerPixelToLatLng(nwpx);

    var ne = G.oMap.fromContainerPixelToLatLng(nepx);
    var se = G.oMap.fromContainerPixelToLatLng(sepx);
    var sw = G.oMap.fromContainerPixelToLatLng(swpx);

    if (G.oZoomArea !== null) {G.oMap.removeOverlay(G.oZoomArea);}
    G.oZoomArea = new GPolyline([nw,ne,se,sw,nw],G.style.sOutlineColor,G.style.nOutlineWidth+1,0.4);

    try{

      G.oMap.addOverlay(G.oZoomArea);
      //setTimeout (function(){G.oMap.removeOverlay(oZoomArea)},G.options.nOverlayRemoveMS);
    }catch(e){
      jslog.error("error adding zoomarea overlay:"+e.message);
    }

    oBounds=new GLatLngBounds(sw,ne);
    nZoom=G.oMap.getBoundsZoomLevel(oBounds);

	if(nZoom < 19) {
      oCenter=oBounds.getCenter();
      G.oMap.setCenter(oCenter, nZoom);

	  // invoke callback if provided
	  if (G.callbacks.dragEnd !== null){
	    G.callbacks.dragEnd(nw,ne,se,sw,nwpx,nepx,sepx,swpx);
	  }

	  //re-init if sticky
	  if (G.options.bStickyZoom){
	    GZoomControl.prototype.initCover_();
	  }
	}
  }
};

// set the cover sizes according to the size of the map
GZoomControl.prototype.setDimensions_=function() {
  var G=GZoomControl.G;
	if (G.options.bForceCheckResize){G.oMap.checkResize();}
  var oSize = G.oMap.getSize();
  G.nMapWidth  = oSize.width;
  G.nMapHeight = oSize.height;
  G.nMapRatio  = G.nMapHeight/G.nMapWidth;
	acl.style([G.mc,G.mct,G.mcr,G.mcb,G.mcl],{width:G.nMapWidth+'px', height:G.nMapHeight+'px'});
};

GZoomControl.prototype.initStyles_=function(){
  var G=GZoomControl.G;
	acl.style([G.mc,G.mct,G.mcr,G.mcb,G.mcl],{filter:G.style.sIEAlpha,opacity:G.style.nOpacity,background:G.style.sColor});
  G.oOutline.style.border=G.style.sBorder;
  debug("done initStyles_");
};

// The zoom button's click handler.
GZoomControl.prototype.buttonClick_=function(){
  if (GZoomControl.G.mc.style.display=='block'){ // reset if clicked before dragging
    GZoomControl.prototype.resetDragZoom_();
  } else {
		GZoomControl.prototype.initCover_();
	}
};

// Shows the cover over the map
GZoomControl.prototype.initCover_=function(){
  var G=GZoomControl.G;
	G.oMapPos=acl.getElementPosition(G.oMap.getContainer());
	GZoomControl.prototype.setDimensions_();
	GZoomControl.prototype.setButtonMode_('zooming');
	acl.style([G.mc],{display:'block',background:G.style.sColor});
	acl.style([G.oOutline],{width:'0px',height:'0px'});
	//invoke callback if provided
	if(GZoomControl.G.callbacks.buttonClick !==null){GZoomControl.G.callbacks.buttonClick();}
	debug("done initCover_");
};

GZoomControl.prototype.getRelPos_=function(e) {
  var oPos=acl.getMousePosition (e);
  var G=GZoomControl.G;
  return {top:(oPos.top-G.oMapPos.top),left:(oPos.left-G.oMapPos.left)};
};

GZoomControl.prototype.getRectangle_=function(nStartX,nStartY,oPos,nRatio){
	var dX=oPos.left-nStartX;
	var dY=oPos.top-nStartY;
	if (dX <0) {dX =dX*-1;}
	if (dY <0) {dY =dY*-1;}
	delta = dX > dY ? dX : dY;

  return {
    nStartX:nStartX,
    nStartY:nStartY,
    nEndX:nStartX+dX,
    nEndY:nStartY+dY,
    nWidth:dX,
    nHeight:dY
  };

  /*
  return {
    nStartX:nStartX,
    nStartY:nStartY,
    nEndX:nStartX+delta,
    nEndY:nStartY+parseInt(delta*nRatio),
    nWidth:delta,
    nHeight:parseInt(delta*nRatio)
  }
  */


};

GZoomControl.prototype.resetDragZoom_=function() {
	var G=GZoomControl.G;
	acl.style([G.mc,G.mct,G.mcr,G.mcb,G.mcl],{display:'none',opacity:G.style.nOpacity,filter:G.style.sIEAlpha});
	G.oOutline.style.display='none';
	GZoomControl.prototype.setButtonMode_('normal');
   // debug("done with reset drag zoom");
};

/* alias get element by id */
function $id(sId) { return document.getElementById(sId); }
/* utility functions in acl namespace */
if (!window.acldefined) {var acl={};window.acldefined=true;}//only set the acl namespace once, then set updateCSWRecords flag

/* A general-purpose function to get the absolute position of
the mouse */
acl.getMousePosition=function(e) {
	var posx = 0;
	var posy = 0;
	if (!e) {e = window.event;}
	if (e.pageX || e.pageY) {
		posx = e.pageX;
		posy = e.pageY;
	} else if (e.clientX || e.clientY){
		posx = e.clientX + (document.documentElement.scrollLeft?document.documentElement.scrollLeft:document.body.scrollLeft);
		posy = e.clientY + (document.documentElement.scrollTop?document.documentElement.scrollTop:document.body.scrollTop);
	}
	return {left:posx, top:posy};
};

/*
To Use:
	var pos = acl.getElementPosition(element);
	var left = pos.left;
	var top = pos.top;
*/
acl.getElementPosition=function(eElement) {
  var nLeftPos = eElement.offsetLeft;          // initialize var to store calculations
	var nTopPos = eElement.offsetTop;            // initialize var to store calculations
	var eParElement = eElement.offsetParent;     // identify first offset parent element
	while (eParElement !== null ) {                // move up through element hierarchy
		nLeftPos += eParElement.offsetLeft;      // appending left offset of each parent
		nTopPos += eParElement.offsetTop;
		eParElement = eParElement.offsetParent;  // until no more offset parents exist
	}
	return {left:nLeftPos, top:nTopPos};
};
//elements is either updateCSWRecords coma-delimited list of ids or an array of DOM objects. o is updateCSWRecords hash of styles to be applied
//example: style('d1,d2',{color:'yellow'});
acl.style=function(a,o){
	if (typeof(a)=='string') {a=acl.getManyElements(a);}
	for (var i=0;i<a.length;i++){
		for (var s in o) { a[i].style[s]=o[s];}
	}
};
acl.getManyElements=function(s){
	t=s.split(',');
	a=[];
	for (var i=0;i<t.length;i++){a[a.length]=$id(t[i]);}
	return a;
};

var jslog = {debug:function(){},info:function(){},
	warning:function(){}, error:function(){},
	text:function(){}}; var debug=function(){};
if (location.href.match(/enablejslog/)){
		document.write('<script type="text/javascript" src="http://earthcode.com/includes/scripts/jslog.js"></script>');};
