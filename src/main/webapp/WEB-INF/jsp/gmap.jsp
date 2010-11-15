<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<!-- Credits for icons from http://www.fatcow.com/free-icons/ under http://creativecommons.org/licenses/by/3.0/us/-->
<html xmlns:v="urn:schemas-microsoft-com:vml">
   <head>
      <title>AuScope Discovery Portal</title>

      <meta name="description" content="Access geoscientific information from around Australia, via AuScopes national e-Research infrastructure.">
      <meta name="keywords" content="AuScope, Discovery, Resources, GeoSciML, Mineral Occurrence, Geologic Unit, Australia">
      <meta name="author" content="AuScope">

      <%-- Google Maps imports --%>
      <script src="http://maps.google.com/maps?file=api&amp;v=2.X&amp;key=${googleKey}" type="text/javascript"></script>
      <script src="http://gmaps-utility-library.googlecode.com/svn/trunk/markermanager/release/src/markermanager.js"
             type="text/javascript"></script>

      <script type="text/javascript">
         var VOCAB_SERVICE_URL = "${vocabServiceUrl}";
         var NVCL_WEB_SERVICE_IP = "${nvclWebServiceIP}";
         var MAX_FEATURES = "${maxFeatureValue}";
         var WEB_CONTEXT = '<%= request.getContextPath() %>';
         
      </script>

      <%-- CSS imports - relative paths back to the webapp directory--%>
      <jsp:include page="../../cssimports.htm"/>
      <%-- JS imports - relative paths back to the webapp directory --%>
      <jsp:include page="../../jsimports.htm"/>
      
      <!-- To enable text selection within a Ext JS GridPanel -->
	  <style type="text/css">
	    .x-selectable, .x-selectable * {
	        -moz-user-select: text!important;
	        -khtml-user-select: text!important;
	    }
  	  </style>
      
      <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
   </head>

   <body onunload="GUnload()">
      <!-- Include Navigation Header -->
      <%@ include file="page_header.jsp" %>
   </body>

</html>