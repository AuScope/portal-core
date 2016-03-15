<!-- Page Style -->
<%-- VT: I removed <link rel="stylesheet" type="text/css" href="portal-core/css/menu.css"> from core as I wanted the banner to look different
At the same time, I do not want to force the other portals to follow the same path therefore in gmap.jsp and links.jsp modify the cssimport
      
      <jsp:include page="../../portal-core/cssimports."/>      
      <jsp:include page="../../cssimports.htm"/>
            
 copy menu.css to webapp/css/menu.css and update webapp/cssimport.htm to just
 
 <link rel="stylesheet" type="text/css" href="css/menu.css">
 
 We will be following this architecture for portal specific css.
--%>

<link rel="stylesheet" type="text/css" href="portal-core/css/styles.css?v=${buildTimestamp}">
<link rel="stylesheet" type="text/css" href="portal-core/css/auscope-portal-core.css?v=${buildTimestamp}">
<link rel="stylesheet" type="text/css" href="portal-core/css/portal-ux.css?v=${buildTimestamp}">
<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">

<STYLE type="text/css">
#nav-example-02 a {
	background: url("img/navigation.gif") -100px -38px no-repeat;
}
/* for IE */
v\:* {
	behavior: url(#default#VML);
}

.x-spotlight {
	background-color: #ccc;
	z-index: 8999;
	position: absolute;
	top: 0;
	left: 0;
	-moz-opacity: 0.5;
	opacity: .50;
	filter: alpha(opacity = 50);
	width: 0;
	height: 0;
	zoom: 1;
	font-size: 0;
}

</STYLE>


