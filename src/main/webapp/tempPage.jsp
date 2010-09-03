<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
   <head>
      <title>AuScope Discovery Portal</title>

      <meta name="description" content="Access geoscientific information from around Australia, via AuScopes national e-Research infrastructure."/>
      <meta name="keywords" content="AuScope, Discovery, Resources, GeoSciML, Mineral Occurrence, Geologic Unit, Australia"/>
      <meta name="author" content="AuScope"/>

      <!-- Page Style -->
      <link rel="stylesheet" type="text/css" href="css/styles.css">
      <link rel="stylesheet" type="text/css" href="css/grid-examples.css">

      <STYLE type="text/css">
        #nav-example-02 a {
            background: url("img/navigation.gif") -100px -38px no-repeat;
        }
      </STYLE>

      <!-- Google Maps imports -->
      <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=${googleKey}" type="text/javascript"></script>
      <script src="http://gmaps-utility-library.googlecode.com/svn/trunk/markermanager/release/src/markermanager.js"
             type="text/javascript"></script>

      <!-- Bring in the ExtJs Libraries and CSS -->
        <link rel="stylesheet" type="text/css" href="js/external/extjs/resources/css/ext-all.css">
        <script type="text/javascript" src="js/FirefoxXPathFix.js"></script>

        <script type="text/javascript" src="js/external/extjs/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="js/external/extjs/ext-all-debug.js"></script>
        <!--<script type="text/javascript" src="js/external/extjs/ext-basex.js"></script>-->
        <script type="text/javascript" src="js/external/extjs/RowExpander.js"></script>
        <script type="text/javascript" src="js/external/extjs/CheckColumn.js"></script>
        <script type="text/javascript" src="js/external/extjs/adapter/jquery/jquery.js"></script>


        <script src="js/external/geoxml/geoxml.js" type="text/javascript"></script>

        <!-- Scripts for interperating the geosciml -->
        <script src="js/geoscimlwfs/utility_functions.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/feature_types/location.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/feature_types/coordinates.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/feature_types/sampling_curve.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/feature_types/borehole.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/feature_types/geodesy_stations.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/feature_types/sampling_point.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/feature_types/bushfire.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/global_variables.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/station_group.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/nvcl/nvcl_marker.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/geodesy/geodesy_calendar.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/geodesy/geodesy_marker.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/gnss/gnss_marker.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/ga_sentinel/ga_sentinel_marker.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/gzoom.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/map.js" type="text/javascript"></script>
        <!--<script src="js/geoscimlwfs/main.js" type="text/javascript"></script>-->
        <script src="js/geoscimlwfs/mineral_occurrences_ml/MineralOccurrencesML.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/mineral_occurrences_ml/Mine.js" type="text/javascript"></script>
        <script src="js/geoscimlwfs/WebFeatureService.js" type="text/javascript"></script>

        <!-- Page specific javascript -->
        <script src="js/KMLParser.js" type="text/javascript"></script>
        <script src="js/FormFactory.js" type="text/javascript"></script>
        <script src="js/GMapClickController.js" type="text/javascript"></script>
        <script src="js/Main-UI.js" type="text/javascript"></script>

        <script src="js/external/jhashtable/jshashtable.js" type="text/javascript"></script>
        <script src="js/external/dragzoom/dragzoom.js" type="text/javascript"></script>

        <!-- WMS import -->
        <script src="js/wms/gmap-wms.js" type="text/javascript"></script>

      <!-- for IE -->
      <!--<style type="text/css">v\:* {
        behavior: url(#default#VML);
      }</style>-->
   </head>

   <body onunload="GUnload()">
      <!-- Include Navigation Header -->
   </body>

</html>