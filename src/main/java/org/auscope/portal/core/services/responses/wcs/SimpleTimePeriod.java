package org.auscope.portal.core.services.responses.wcs;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Represents a simplified version of the <wcs:timePeriod> element from a WCS DescribeCoverage response (The optional timeResolution is unsupported)
 * 
 * @author vot002
 *
 */
public class SimpleTimePeriod implements TemporalDomain {
    private Date beginPosition;
    private Date endPosition;
    private String type;

    public Date getBeginPosition() {
        return beginPosition;
    }

    public Date getEndPosition() {
        return endPosition;
    }

    public SimpleTimePeriod(Node node, XPath xPath) throws XPathExpressionException, DOMException, ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT")); // assumption - Make everything GMT

        Node tempNode = (Node) xPath.evaluate("beginPosition", node, XPathConstants.NODE);
        beginPosition = df.parse(tempNode.getTextContent());

        tempNode = (Node) xPath.evaluate("endPosition", node, XPathConstants.NODE);
        endPosition = df.parse(tempNode.getTextContent());

        type = node.getLocalName();
    }

    @Override
    public String getType() {
        return type;
    }
}
