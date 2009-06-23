<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>
   <div id="header-container">
      <div id="logo">
         <h1>
            <a href="#" onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false"><img alt="" src="/img/img-auscope-banner.gif" /></a>
            <!-- <a href="login.html"><img alt="" src="/img/img-auscope-banner.gif" /></a> -->
         </h1>
      </div>                            
      <security:authorize ifAllGranted="ROLE_ADMINISTRATOR">
         <a href="admin.html"><span>Administration</span></a>
      </security:authorize>
      <div id="menu">
         <ul id="nav-example">
            <li id="nav-example-01"><a href="http://www.auscope.org"><span>AuScope.org</span></a></li>
            <li id="nav-example-02"><a href="gmap.html"><span>AuScope Discovery Portal</span></a></li>
            <li id="nav-example-03"><a href="login.html"><span>Login</span></a></li>
         </ul>
      </div>
   </div>
