<!-- CSS Specific to portal-core's Open Layers wrapper-->
<style type="text/css">
    .olControlPanel {
      right: 2px;
      top: 150px;
    }

    .olControlPanel button {
      position: relative;
      display: block;
      margin-top:2px;
      margin-left:100px;
      border: 1px solid;
      padding: 0 5px;
      border-radius: 4px;
      height: 35px;
      float: left;
      overflow: visible; /* needed to remove padding from buttons in IE */
    }
    .olControlPanel button span {
      padding-left: 25px;
    }
    .olControlPanel button span:first-child {
      padding-left: 0;
      display: block;
      position: absolute;
      left: 2px;
    }

    .olControlPanel button.olControlDrawFeatureItemActive {
      background-color: #EFBE2A;
    }

    .olControlPanel button.olControlDrawFeatureItemInactive {
      background-color: white;
    }




    .olControlPanel .olControlDrawFeatureItemActive span:first-child {
      background-image: url("portal-core/js/OpenLayers-2.13.1/theme/default/img/draw_polygon_off.png");
      height: 22px;
      width: 24px;
      top: 5px;
    }

    .olControlPanel .olControlDrawFeatureItemActive span.inactive-text {
      display:none;
    }

    .olControlPanel .olControlDrawFeatureItemInactive span:first-child {
      background-image: url("portal-core/js/OpenLayers-2.13.1/theme/default/img/draw_polygon_off.png");
      height: 22px;
      width: 24px;
      top: 5px;
    }

    .olControlPanel .olControlDrawFeatureItemInactive span.active-text {
      display:none;
    }

</style>

<!-- Javascript imports for portal core Open Layers v2 Support -->
<script src="portal-core/js/portal/map/openlayers/FeatureWithLocationHandler.js" type="text/javascript"></script>
<script src="portal-core/js/portal/map/openlayers/ClickControl.js" type="text/javascript"></script>
<script src="portal-core/js/portal/map/openlayers/OpenLayersMap.js" type="text/javascript"></script>
<script src="portal-core/js/portal/map/openlayers/PrimitiveManager.js" type="text/javascript"></script>
<script src="portal-core/js/portal/map/openlayers/primitives/Marker.js" type="text/javascript"></script>
<script src="portal-core/js/portal/map/openlayers/primitives/Polygon.js" type="text/javascript"></script>
<script src="portal-core/js/portal/map/openlayers/primitives/Polyline.js" type="text/javascript"></script>
<script src="portal-core/js/portal/map/openlayers/primitives/WMSOverlay.js" type="text/javascript"></script>
<script src="https://maps.google.com/maps/api/js?v=3.8&sensor=false&key=${googleKey}"></script>