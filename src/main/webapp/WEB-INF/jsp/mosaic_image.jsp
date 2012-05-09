<html>
  <head>
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=${googleKey}" type="text/javascript"></script>
    <script src="../js/geoscimlwfs/global_variables.js" type="text/javascript"></script>
    <script type="text/javascript">
      function downloadImage() {
        if(location.search!='') {
          // paramList should contain coreid=xxx
          var sparamsList =  location.search.substring(1);
          var sMosaicUrl = ProxyURL + NVCL_WEB_SERVICE_IP + "/scalars.asmx/trayids?";
          sMosaicUrl += sparamsList;
          GDownloadUrl(sMosaicUrl, function(pData, pResponseCode) {
            if(pResponseCode == 200) {
              var aBoreholeTrays = new Array();
              var xmlDoc = GXml.parse(pData);
              // check for IE error.
              if (xmlDoc.documentElement != null) {
                aTrays = xmlDoc.documentElement.getElementsByTagName("trayids");
                var nTrayIndex = 0;
                var sTrayId = "";

                // Extract all tray ids from the XML document.
                for(var i=0; i < aTrays.length; i++) {


                  for(var j=0; j < aTrays[i].childNodes.length; j++) {
                    childTag = aTrays[i].childNodes[j].nodeName;

                    switch(childTag) {
                      case "Tray_ID" :
                        var sTrayId = GXml.value(aTrays[i].childNodes[j]);
                        break;
                    }
                    if (sTrayId != "") {
                      aBoreholeTrays[nTrayIndex] = sTrayId;
                      nTrayIndex++;
                      sTrayId = "";
                    }
                  }
                }

                var mosaicDiv = document.getElementById("div_mosaic_image");

                var mosaicHtml = "";

                var columnCounter = 0;
                // Each tray image has a thumbnail and an enlarged version.
                var trayImageThmbUrlCmn = NVCL_WEB_SERVICE_IP + "/Display_Tray_Thumb.aspx?" + sparamsList;
                var trayImageThmbUrl = "";
                var trayImageUrlCmn = NVCL_WEB_SERVICE_IP + "/Display_Tray_Full.aspx?" + sparamsList;
                var trayImageUrl = "";

                for(var i=0; i<aBoreholeTrays.length; i++) {
                  // Check if we should break line.
                  // We are displaying 3 trays per row.
                  columnCounter++;
                  if (columnCounter == 4) {
                    columnCounter = 1;
                    mosaicHtml += '<br/>';
                  }

                  trayImageUrl = trayImageUrlCmn + '&trayid=' + aBoreholeTrays[i];
                  trayImageThmbUrl = trayImageThmbUrlCmn + '&trayid=' + aBoreholeTrays[i];

                  mosaicHtml += '<a target="_blank" href="' + trayImageUrl + '">';
                  mosaicHtml += '<img src="'+ trayImageThmbUrl +'" style="border-width:0px;width:200px;"/>';
                  mosaicHtml += '</a>';
                }
                mosaicDiv.innerHTML = mosaicHtml;
              }
            }
          });
        }
      }
      </script>
    </head>
	<body onLoad="downloadImage();">
		<div id="div_mosaic_image" style="overflow:auto;">
		</div>
	</body>
</html>