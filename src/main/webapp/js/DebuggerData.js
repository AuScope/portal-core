/**
 * Contains response information from service calls, and formats them in some pretty html for response data
 */
DebuggerData = function() {
    this.responses = [];
};

DebuggerData.prototype.addResponse = function(url, statusDetail) {
    this.responses[url] = statusDetail;
};

DebuggerData.prototype.getHtml = function() {
    /*if(this.responses.length == 0)
        return 'No status has been recorded.';*/	
    var htmlString = '<br/>' ;

    for(i in this.responses) {
    	

        if(!this.responses[i].toString().match('function')){
        	
        	var unescapedXml = this.responses[i];
        	var escapedXml = unescapedXml.replace(/</g, '&lt;');

        	htmlString += '<b>'+ i + '</b>' + '<br/> ' + escapedXml +'<br/><br/>';
        }	
        
    } 

    htmlString += '<br/>' ;

    return htmlString;
};



	