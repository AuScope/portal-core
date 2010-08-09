<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html xmlns:v="urn:schemas-microsoft-com:vml">
   <head>
      <title>AuScope Discovery Portal</title>
      
      <meta name="description" content="Access geoscientific information from around Australia, via AuScopes national e-Research infrastructure.">
      <meta name="keywords" content="AuScope, Discovery, Resources, GeoSciML, Mineral Occurrence, Geologic Unit, Australia">
      <meta name="author" content="AuScope">
      
      <!-- Page Style -->
      <link rel="stylesheet" type="text/css" href="css/menu.css"> 
      <link rel="stylesheet" type="text/css" href="css/styles.css">
      <link rel="stylesheet" type="text/css" href="css/grid-examples.css">

      <STYLE type="text/css">
         #nav-example-02 a {
            background: url("/img/navigation.gif") -100px -38px no-repeat;
         }
         /* for IE */
         v\:* {
            behavior: url(#default#VML);
         }        
      </STYLE>
         
      <!--[if !mso]>
      <STYLE type="text/css">
         v\:* { behavior: url(#default#VML); }
      </STYLE>
      <![endif]-->
                  
      <!-- Google Maps imports -->
      <script src="http://maps.google.com/maps?file=api&amp;v=2.X&amp;key=${googleKey}" type="text/javascript"></script>
      <script src="http://gmaps-utility-library.googlecode.com/svn/trunk/markermanager/release/src/markermanager.js"
             type="text/javascript"></script>
             
     
      <jsp:include page="/jsimports.htm"/>
   </head>

   <body onunload="GUnload()">
      <!-- Include Navigation Header -->
      <%@ include file="page_header.jsp" %>
      
      <security:authorize ifAllGranted="ROLE_DOWNLOAD">
        <script type="text/javascript">
  		  //buttonsPanel.enable();
        </script>
      </security:authorize>
   </body>

</html>