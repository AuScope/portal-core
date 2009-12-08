<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="include.jsp" %>

<html>

<head>
    <title>AuScope Geodesy Workflow  - Monitor Jobs</title>
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <link rel="stylesheet" type="text/css" href="css/grid-examples.css">
    <style type="text/css">
      #sitenav-01 a {
        background: url( "img/navigation.gif" ) 0px -38px no-repeat;
      }
    </style>
    <link rel="stylesheet" type="text/css" href="js/external/ext-2.2/resources/css/ext-all.css">
    <script type="text/javascript" src="js/external/ext-2.2/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" src="js/external/ext-2.2/ext-all.js"></script>
    <script type="text/javascript" src="js/GridJob/JobList.js"></script>
    <script src="js/geoscimlwfs/global_variables.js" type="text/javascript"></script>
    <c:if test='${error != null}'>
    <script type="text/javascript">
        JobList.error = "${error}";
    </script>
    </c:if>
    
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
</body>

</html>

