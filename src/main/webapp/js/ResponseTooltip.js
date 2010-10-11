/**
 * Contains response information from service calls, and formats them in some pretty html for th tooltip popup
 */
ResponseTooltip = function() {
    this.responses = [];
};

ResponseTooltip.prototype.addResponse = function(url, responseStatus) {
    this.responses[url] = responseStatus;
};

ResponseTooltip.prototype.getHtml = function() {
    /*if(this.responses.length == 0)
        return 'No status has been recorded.';*/

    var htmlString = '<table border="0">' ;

    for(i in this.responses) {
        if(!this.responses[i].toString().match('function')) {
        	if(i.length >= 1) {        	
        		htmlString += '<tr><td>'+ i + ' - ' + this.responses[i] +'</td></tr>';
        	} else {
                htmlString += '<tr><td>'+ this.responses[i] +'</td></tr>';	
        	}
        }
    }

    htmlString += '</table>' ;

    return htmlString;
};



