function WebFeatureService(serviceUrl) {

    function doGetRequest(parameterList) {
        GDownloadUrl(serviceUrl, function(pData, pResponseCode) {
            if (pResponseCode == 200) {
                return GXml.parse(pData);
            }else if(responseCode == -1) {
                alert("Data request timed out. Please try later.");
            } else if ((responseCode >= 400) & (responseCode < 500)){
                alert('Request not found, bad request or similar problem. Error code is: ' + responseCode);
            } else if ((responseCode >= 500) & (responseCode <= 506)){
                alert('Requested service not available, not implemented or internal service error. Error code is: ' + responseCode);
            }else {
                alert('Remote server returned error code: ' + responseCode);
            }
        });
    }

    function doPostRequest(parameterList) {

    }
}