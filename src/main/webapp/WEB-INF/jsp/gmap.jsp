<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>My Google Map</title>
    <link rel="stylesheet" type="text/css" href="/css/styles.css"/>

    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAjNe9lSRMedgGg_SHNzEvuhTwM0brOpm-All5BF6PoaKBxRWWERQRcbtFgCIEBbfjrXQpTiWtdGwZFg"
      type="text/javascript"></script>
    
    <script type="text/javascript">

    //<![CDATA[
        
    function load() {
      //alert(window.location.host);
      //alert(${2+2});
      
      // Is user's browser suppported by Google Maps?
      if (GBrowserIsCompatible()) {
        var map = new GMap2(document.getElementById("content"));
        // Large pan and zoom control
        map.addControl(new GLargeMapControl());
        // Toggle between Map, Satellite, and Hybrid types 
        map.addControl(new GMapTypeControl());

        var startZoom = 4; 
        map.setCenter(new GLatLng(${centerLat},${centerLon}), 4);
        //map.setCenter(new GLatLng(-31.9554, 115.85859), 7);
        map.setMapType(G_SATELLITE_MAP); 

		//Thumbnail map
		var Tsize = new GSize(150, 150);
        map.addControl(new GOverviewMapControl(Tsize));
                
      }
    }

    // Create a base icon for all of our markers that specifies the
    // shadow, icon dimensions, etc.
    var baseIcon = new GIcon();
    baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
    baseIcon.iconSize = new GSize(20, 34);
    baseIcon.shadowSize = new GSize(37, 34);
    baseIcon.iconAnchor = new GPoint(9, 34);
    baseIcon.infoWindowAnchor = new GPoint(9, 2);
    baseIcon.infoShadowAnchor = new GPoint(18, 25);

    // To Do: Checkboxes
    var CAT_ICONS = [];
    CAT_ICONS["DEFAULT_ICON"] = tinyIcon("green");
    CAT_ICONS["Hyperspectral"] = tinyIcon("red");
    CAT_ICONS["Mineral Occurences"] = tinyIcon("green");
    CAT_ICONS["Geological Units"] = tinyIcon("gray");
    CAT_ICONS["Geochemistry"] = tinyIcon("blue");
    CAT_ICONS["Bore holes"] = tinyIcon("yellow");
    CAT_ICONS["GNNS / GPS"] = tinyIcon("purple");
    CAT_ICONS["Seismic Imaging"] = tinyIcon("purple");

    //]]>
    </script>	
  </head>
  
  <body onload="load()" onunload="GUnload()">
    <div id="head">&nbsp;AuScope Portal</div>
    <div id="foot">&nbsp;&copy;2008 AuScope</div>
    <div id="left">
      <dl>
        <dt><a class="nav" href="#">Hyperspectral</a></dt>
        <dd>Hyperspectral data</dd>
        <dt><a class="nav" href="#">Mineral Occurences</a></dt>
        <dd>Mineral Occurences data</dd>
        <dt><a class="nav" href="#">Geological Units</a></dt>
        <dt><a class="nav" href="#">Geochemistry</a></dt>
        <dt><a class="nav" href="#">Bore holes</a></dt>
        <dt><a class="nav" href="#">GNNS / GPS</a></dt>
        <dt><a class="nav" href="#">Seismic Imaging</a></dt>
      </dl>
    </div>
    <!-- 
    <div id="map" style="width: 800px; height: 600px"></div>
    -->
    <div id="content"></div>
  </body>
</html>