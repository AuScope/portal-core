<html>
	<head>
        <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=${googleKey}" type="text/javascript"></script>
	    <script src="../js/geoscimlwfs/global_variables.js" type="text/javascript"></script>
		<script type="text/javascript">
   		function downloadImage() {
      		if(location.search!='')
      		{
         		var sparamsList =  location.search.substring(1);
                var imageSrc = ProxyURL + NVCL_WEB_SERVICE_IP + "/plotscalar.aspx?" + location.search.substring(1);

         		var img = document.getElementById("plotted_images");
				img.src = imageSrc;
      		}
   		}
		</script>
 	</head>
	<body onLoad="downloadImage();">
		<div id="div_plotted_images" style="overflow:auto;">
			<img id="plotted_images" src="img/nvcl/loading_plots.gif">
		</div>
	</body>
</html>