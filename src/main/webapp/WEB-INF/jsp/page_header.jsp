<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>
   <div id="header-container">
      <div id="logo">
         <h1>
            <a href="#" onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false"><img alt="" src="/img/img-auscope-banner.gif"></a>
            <!-- <a href="login.html"><img alt="" src="/img/img-auscope-banner.gif" /></a> -->
         </h1>
      </div>
                                  
     
      <div id="menu">
         <ul >
         	<security:authorize ifAllGranted="ROLE_ADMINISTRATOR">
				<li ><a href="admin.html">Administration<span></span></a></li>
      		</security:authorize>
            <li ><a href="http://www.auscope.org">AuScope.org<span></span></a></li>
            <li <%if (request.getRequestURL().toString().contains("/gmap.jsp")) {%>class="current" <%} %>><a href="gmap.html">AuScope Discovery Portal<span></span></a></li>
            <li ><a href="login.html">Login<span></span></a></li>
         </ul>
      </div>
   </div>
