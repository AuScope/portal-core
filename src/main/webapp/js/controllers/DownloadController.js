var downloadController = function(downloadUrls) {
    var url = "";
    var theUrls = downloadUrls.values();

    if (theUrls.length >= 1) {
        for (i = 0; i < theUrls.length; i++)
            url += "urls=" + theUrls[i] + "%26";

        //alert("downloadProxy?" + url);
        window.open("downloadProxy?" + url, name);
    }
};