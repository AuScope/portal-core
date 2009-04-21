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

    <!-- for developer's PC
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAZqsP68C-PfYEAobUmwen1xSei4j9m2Mi-rbOVQ0iCZaYodqdCRQbFXNtqGnMwriezq-u4iBCtlh5WQ" type="text/javascript"></script>
    -->
    <!-- for auscope-portal-dev
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAjNe9lSRMedgGg_SHNzEvuhSei4j9m2Mi-rbOVQ0iCZaYodqdCRQ_SvOVaQGcob4C8YKWj6B3LSvFpw" type="text/javascript"></script>
    -->
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=${googleKey}" type="text/javascript"></script>     
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

    <jsp:include page="/jsimports.htm" />

    <!-- for IE -->
    <style type="text/css">v\:* {behavior:url(#default#VML);}</style>

</head>

<body onunload="GUnload()">
   <!-- Include Navigation Header -->
   <%@ include file="page_header.jsp" %>
</body>
</html>