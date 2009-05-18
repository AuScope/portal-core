<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <title>AuScope Portal</title>

    <!-- Page Style -->
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <STYLE type="text/css">
      #nav-example-02 a {
        background: url( "/img/navigation.gif" ) -100px -38px no-repeat;
      }
    </STYLE>

    <!-- Google Maps imports -->
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=${googleKey}" type="text/javascript"></script>    
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