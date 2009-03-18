function WebFeatureService(serviceUrl) {

    function doGetRequest(parameterList) {
        GDownloadUrl(serviceUrl, function(pData, pResponseCode) {
            if (pResponseCode == 200) {
                return GXml.parse(pData);
            }
        });
    }

    function doPostRequest(parameterList) {

    }
}