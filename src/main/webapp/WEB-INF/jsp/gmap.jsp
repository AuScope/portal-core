<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Auscope Portal Revision $Revision$</title>

    <!-- Page Style -->
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <STYLE type="text/css">
      #nav-example-02 a {
         background: url( "/img/navigation.gif" ) -100px -38px no-repeat;
      }
    </STYLE>

    <!-- Google Maps import -->

    <!-- for developer's PC
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAZqsP68C-PfYEAobUmwen1xSei4j9m2Mi-rbOVQ0iCZaYodqdCRQbFXNtqGnMwriezq-u4iBCtlh5WQ" type="text/javascript"></script>
    -->
    <!-- for auscope-portal-dev -->
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAjNe9lSRMedgGg_SHNzEvuhSei4j9m2Mi-rbOVQ0iCZaYodqdCRQ_SvOVaQGcob4C8YKWj6B3LSvFpw" type="text/javascript"></script>
 
    <!-- for auscope-portal-test
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAZqsP68C-PfYEAobUmwen1xQACWFotHIO8WvEtjV21SpXfQmX3xQtfg9sygne-SV-6ZapBiD3sx-QEg" type="text/javascript"></script>
    -->
    <!--  Map key for http://auscope-portal.arrc.csiro.au/
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAL8cDk1rYcykpyZKQpA6yZBSwRLY6bdISx9C2Tuxzg2ZWWoxkmxTUcmb_hCQhxL5BLrbSFbg_yWqlvg" type="text/javascript"></script>
    -->
    <!-- Map key For Production: http://portal.auscope.org/
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAjNe9lSRMedgGg_SHNzEvuhSBxvg3VkDH5pPvpEJEFQT7M94H8RSeylhM_lLyqpd3-4gfGtarcxkFpA" type="text/javascript"></script>
    -->
    
    <script src="http://gmaps-utility-library.googlecode.com/svn/trunk/markermanager/release/src/markermanager.js" type="text/javascript"></script>

    <!-- Bring in the ExtJs Libraries and CSS -->
    <link rel="stylesheet" type="text/css" href="js/external/ext-2.2/resources/css/ext-all.css">
    <script type="text/javascript" src="js/external/ext-2.2/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" src="js/external/ext-2.2/ext-all-debug.js"></script>

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
    <script src="js/geoscimlwfs/main.js" type="text/javascript"></script>
    <script src="js/geoscimlwfs/mineral_occurrences_ml/MineralOccurrencesML.js" type="text/javascript"></script>
    <script src="js/geoscimlwfs/mineral_occurrences_ml/Mine.js" type="text/javascript"></script>
    <script src="js/geoscimlwfs/WebFeatureService.js" type="text/javascript"></script>
    <script src="js/FormFactory.js" type="text/javascript"></script>

    <!-- Page specific javascript -->
    <script src="js/Main.js" type="text/javascript"></script>
    <script src="js/external/jhashtable/jshashtable.js" type="text/javascript"></script>
    <script src="js/external/dragzoom/dragzoom.js" type="text/javascript"></script>

    <!-- for IE -->
    <style type="text/css">v\:* {behavior:url(#default#VML);}</style>

</head>

<body onunload="GUnload()">
   <!-- Include Navigation Header -->
   <%@ include file="page_header.jsp" %>
</body>
</html>