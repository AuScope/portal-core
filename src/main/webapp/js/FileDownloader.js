Ext.ns('FileDownloader');

/**
 * Given a URL this function will create a hidden form and post its (empty) contents
 * to the specified URL. If the response contains an appropriate header the user will
 * be prompted for a file download.
 */
FileDownloader.downloadFile = function(url) {
    var body = Ext.getBody();
    var frame = body.createChild({
        tag:'iframe',
        id:'iframe',
        name:'iframe'
    });
    var form = body.createChild({
        tag:'form',
        id:'form',
        target:'iframe',
        method:'POST'
    });
    form.dom.action = url;
    form.dom.submit();
};