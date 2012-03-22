   <div id="header-container">
      <div id="logo">
         <h1>
            <a href="#" onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false"><img alt="" src="img/img-auscope-banner.gif"></a>
         </h1>
      </div>
      <div id="menu">
         <ul >
            <li ><a href="http://www.auscope.org">AuScope.org<span></span></a></li>
            <li <%if (request.getRequestURL().toString().contains("/gmap.")) {%>class="current" <%} %>><a href="gmap.html">AuScope Discovery Portal<span></span></a></li>
            <li <%if (request.getRequestURL().toString().contains("/links.")) {%>class="current" <%} %>><a href="links.html">Links<span></span></a></li>
         </ul>
      </div>
      <span id="latlng" class="input-text"></span>
      <div id="permalinkicon"><a href="javascript:void(0)"><img src="img/link.png" width="16" height="16"/></a></div>
      <div id="permalink"><a href="javascript:void(0)">Permanent Link</a></div>


   </div>
