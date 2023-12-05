package org.auscope.portal.core.services.csw;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.data.elasticsearch.core.mapping.PropertyValueConverter;

/**
 * Converter to change URLs to Strings and vice versa in CSWOnlineResources to prevent crashes when indexing
 *
 */
public class URLToStringConverter implements PropertyValueConverter {
	
	@Override
    public Object write(Object value) {
        return value.toString();
    }

    @Override
    public Object read(Object value) {
    	URL url = null;
    	try {
    		url = new URL(value.toString());
    	} catch(MalformedURLException e) {
    		System.out.println("MalformedURL: " + e.getLocalizedMessage());
    	}
    	return url;
    }
	
	/* XXX
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    @Override
    public Object write(Object value) {
    	String dateString = "";
    	if (value != null) {
    		// XXX Make an arbitrary date in the past so we can detect nulls during unmarshalling? 
    		value = dateFormat.format(value);
    	}
        return dateString;
    }

    @Override
    public Object read(Object value) {
    	Date d = null;
		try {
			d = dateFormat.parse(value.toString());
		} catch(ParseException e) {}
		return d;
    }
    */
    
}