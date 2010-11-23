package org.auscope.portal.csw;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


import net.sf.saxon.xpath.XPathFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A number of utility methods related to the CSW 
 * @author vot002
 *
 */
public final class CSWXPathUtil {
    
    private static final XPath xPath;
    private static final Log logger = LogFactory.getLog(CSWXPathUtil.class);
    
    static {
        //Force the usage of the Saxon XPath library
        XPathFactory factory = new XPathFactoryImpl();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());
    }
    
    /**
     * Utility for compiling an XPathExpression for use with the CSWNamespace, 
     * 
     * throws a RuntimeException if the expression doesn't compile
     * @param xPath
     * @param expr
     * @return
     */
    public static XPathExpression attemptCompileXpathExpr(String expr) {
        try {
            return xPath.compile(expr);
        } catch (XPathExpressionException ex) {
            logger.error("Error compiling expression " + expr, ex);
            throw new RuntimeException();
        }
    }
}
