<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Auscope Portal</title>

    <!-- Page Style -->
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <STYLE type="text/css">
      #nav-example-02 a {
         background: url( "/img/navigation.gif" ) -100px -38px no-repeat;
      }
    </STYLE>

    <!-- Google Maps import -->

    <!-- for auscope-portal-dev -->
    <!-- <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAZqsP68C-PfYEAobUmwen1xSei4j9m2Mi-rbOVQ0iCZaYodqdCRQbFXNtqGnMwriezq-u4iBCtlh5WQ" type="text/javascript"></script>-->
    
    <!-- for auscope-portal-test -- >
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAZqsP68C-PfYEAobUmwen1xQACWFotHIO8WvEtjV21SpXfQmX3xQtfg9sygne-SV-6ZapBiD3sx-QEg" type="text/javascript"></script>


    <script src="http://gmaps-utility-library.googlecode.com/svn/trunk/markermanager/release/src/markermanager.js" type="text/javascript"></script>

    <!-- Bring in the ExtJs Libraries and CSS -->
    <link rel="stylesheet" type="text/css" href="js/ext-2.2/resources/css/ext-all.css">
    <script type="text/javascript" src="js/ext-2.2/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" src="js/ext-2.2/ext-all.js"></script>

    <script src="js/egeoxml/csgeoxml.js" type="text/javascript"></script>
    <script src="js/egeoxml/egeoxml.js" type="text/javascript"></script>
    <script src="js/egeoxml/geoxml.js" type="text/javascript"></script>
    <script src="js/egeoxml/clustermarker.js" type="text/javascript"></script>

    <!-- Scripts for the Web Map Service layering -->
    <script src="js/wms/wms-gs-1_1_1.js" type="text/javascript"></script>
    <script src="js/wms/wms_layer.js" type="text/javascript"></script>
    <script src="js/wms/web_map_service.js" type="text/javascript"></script>

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
    <script src="js/geoscimlwfs/main.js" type="text/javascript"></script>

    <!-- Page specific javascript -->
    <script src="js/Main.js" type="text/javascript"></script>
    <script src="js/dragzoom.js" type="text/javascript"></script>
    <script src="js/jshashtable.js" type="text/javascript"></script>

    <!-- for IE -->
    <style type="text/css">v\:* {behavior:url(#default#VML);}</style>

</head>

<body onunload="GUnload()">
   <!-- Include Navigation Header -->
   <%@ include file="page_header.jsp" %>
</body>
</html>