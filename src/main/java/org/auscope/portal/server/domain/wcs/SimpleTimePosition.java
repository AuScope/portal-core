package org.auscope.portal.server.domain.wcs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

/**
 * Represents a simplified instance of the <gml:timePosition> element from a WCS DescribeCoverage response
 * @author vot002
 *
 */
public class SimpleTimePosition implements TemporalDomain {


	private static final long serialVersionUID = 1L;
	private Date timePosition;
    private String type;

    public String getType() {
        return type;
    }

    public Date getTimePosition() {
        return timePosition;
    }

    public SimpleTimePosition(Node node, XPath xPath) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT")); // assumption - Make everything GMT

        timePosition = df.parse(node.getTextContent());

        type = node.getLocalName();
    }
}
