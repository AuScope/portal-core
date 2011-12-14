/**
 * Contains response information from service calls, and formats them in some pretty html for debug window
 */
DebuggerData = function() {
    this.responses = {};
};

DebuggerData.prototype.addResponse = function(url, statusDetail) {
    this.responses[url] = statusDetail;
};

DebuggerData.prototype.getHtml = function() {
    var htmlString = '<br/>' ;

    for(i in this.responses) {
        var unescapedXml = new String(this.responses[i]);
        var escapedXml = unescapedXml.replace(/</g, '&lt;');

        htmlString += '<b>'+ i + '</b>' + '<br/> ' + escapedXml +'<br/><br/>';
    }

    htmlString += '<br/>' ;
    return htmlString;
};



