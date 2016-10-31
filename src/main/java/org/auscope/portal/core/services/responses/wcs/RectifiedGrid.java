package org.auscope.portal.core.services.responses.wcs;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a simplified gml:RectifiedGrid element
 *
 * @author Josh Vote
 *
 */
public class RectifiedGrid implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * The spatial reference system for the origin/offsetVectors
     */
    private String srsName;
    /**
     * The number of dimensions in this rectified grid
     */
    private int dimension;
    /**
     * The low index values for this rectified grid (normally 0's)
     */
    private int[] envelopeLowValues;
    /**
     * The high index values for this rectified grid
     */
    private int[] envelopeHighValues;
    /**
     * The n dimensional ordinates for the origin of this grid (in srsName coordinate space)
     */
    private double[] origin;
    /**
     * double[] x will reference a n dimensional vector offset (in srsName space) that allows you to correspond an integer grid index to a point in real space
     */
    private double[][] offsetVectors;
    /**
     * The names of each axes
     */
    private String[] axisNames;

    /**
     *
     * @param srsName
     *            The spatial reference system for the origin/offsetVectors
     * @param dimension
     *            The number of dimensions in this rectified grid
     * @param envelopeLowValues
     *            The low index values for this rectified grid (normally 0's)
     * @param envelopeHighValues
     *            The high index values for this rectified grid
     * @param origin
     *            The n dimensional ordinates for the origin of this grid (in srsName coordinate space)
     * @param offsetVectors
     *            double[] x will reference a n dimensional vector offset (in srsName space) that allows you to correspond an integer grid index to a point in
     *            real space
     * @param axisNames
     *            The names of each axes
     */
    public RectifiedGrid(String srsName, int dimension,
            int[] envelopeLowValues, int[] envelopeHighValues, double[] origin,
            double[][] offsetVectors, String[] axisNames) {
        super();
        this.srsName = srsName;
        this.dimension = dimension;
        this.envelopeLowValues = envelopeLowValues;
        this.envelopeHighValues = envelopeHighValues;
        this.origin = origin;
        this.offsetVectors = offsetVectors;
        this.axisNames = axisNames;
    }

    /**
     * Creates a rectifiedGrid from a DOM node representing a gml:RectifiedGrid element
     * 
     * @param node
     * @throws XPathExpressionException
     */
    public RectifiedGrid(Node node) throws XPathExpressionException {
        WCSNamespaceContext nc = new WCSNamespaceContext();

        String nodeSrsName = (String) DOMUtil.compileXPathExpr("@srsName", nc).evaluate(node, XPathConstants.STRING);
        this.setSrsName(nodeSrsName);

        String dimensionStr = (String) DOMUtil.compileXPathExpr("@dimension", nc).evaluate(node, XPathConstants.STRING);
        try {
            this.setDimension(Integer.parseInt(dimensionStr));
        } catch (NumberFormatException ex) {
            log.debug(String.format("Unable to parse dimension '%1$s' to int: %2$s", dimensionStr, ex));
        }

        String envelopeLowValuesStr = (String) DOMUtil.compileXPathExpr("gml:limits/gml:GridEnvelope/gml:low", nc)
                .evaluate(node, XPathConstants.STRING);
        String envelopeHighValuesStr = (String) DOMUtil.compileXPathExpr("gml:limits/gml:GridEnvelope/gml:high", nc)
                .evaluate(node, XPathConstants.STRING);
        this.setEnvelopeLowValues(stringToIntVector(envelopeLowValuesStr));
        this.setEnvelopeHighValues(stringToIntVector(envelopeHighValuesStr));

        String originValues = (String) DOMUtil.compileXPathExpr("gml:origin/gml:pos", nc).evaluate(node,
                XPathConstants.STRING);
        this.setOrigin(stringToDoubleVector(originValues));

        NodeList offsetVectorNodes = (NodeList) DOMUtil.compileXPathExpr("gml:offsetVector", nc).evaluate(node,
                XPathConstants.NODESET);
        double[][] offsetVectorVals = new double[offsetVectorNodes.getLength()][];
        for (int i = 0; i < offsetVectorNodes.getLength(); i++) {
            Node offsetVectorNode = offsetVectorNodes.item(i);
            String values = offsetVectorNode.getTextContent();
            offsetVectorVals[i] = stringToDoubleVector(values);
        }
        this.setOffsetVectors(offsetVectorVals);

        NodeList axisNameNodes = (NodeList) DOMUtil.compileXPathExpr("gml:axisName", nc).evaluate(node,
                XPathConstants.NODESET);
        String[] axisNamesStr = new String[axisNameNodes.getLength()];
        for (int i = 0; i < axisNameNodes.getLength(); i++) {
            Node axisNameNode = axisNameNodes.item(i);
            axisNamesStr[i] = axisNameNode.getTextContent();
        }
        this.setAxisNames(axisNamesStr);
    }

    private double[] stringToDoubleVector(String s) {
        String[] vals = s.split(" ");
        double[] result = new double[vals.length];

        for (int i = 0; i < vals.length; i++) {
            try {
                result[i] = Double.parseDouble(vals[i]);
            } catch (NumberFormatException ex) {
                result[i] = 0.0;
                log.debug(String.format("Unable to parse value '%1$s' to double. Defaulting to 0: %2$s", vals[i], ex));
            }
        }

        return result;
    }

    private int[] stringToIntVector(String s) {
        String[] vals = s.split(" ");
        int[] result = new int[vals.length];

        for (int i = 0; i < vals.length; i++) {
            try {
                result[i] = Integer.parseInt(vals[i]);
            } catch (NumberFormatException ex) {
                result[i] = 0;
                log.debug(String.format("Unable to parse value '%1$s' to int. Defaulting to 0: %2$s", vals[i], ex));
            }
        }

        return result;
    }

    /**
     * The number of dimensions in this rectified grid
     * 
     * @return
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * The number of dimensions in this rectified grid
     * 
     * @param dimension
     */
    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    /**
     * The spatial reference system for the origin/offsetVectors
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * The spatial reference system for the origin/offsetVectors
     */
    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    /**
     * The low index values for this rectified grid (normally 0's)
     * 
     * @return
     */
    public int[] getEnvelopeLowValues() {
        return envelopeLowValues;
    }

    /**
     * The low index values for this rectified grid (normally 0's)
     * 
     * @param envelopeLowValues
     */
    public void setEnvelopeLowValues(int[] envelopeLowValues) {
        this.envelopeLowValues = envelopeLowValues;
    }

    /**
     * The high index values for this rectified grid
     * 
     * @return
     */
    public int[] getEnvelopeHighValues() {
        return envelopeHighValues;
    }

    /**
     * The high index values for this rectified grid
     * 
     * @param envelopeHighValues
     */
    public void setEnvelopeHighValues(int[] envelopeHighValues) {
        this.envelopeHighValues = envelopeHighValues;
    }

    /**
     * The n dimensional ordinates for the origin of this grid (in srsName coordinate space)
     * 
     * @return
     */
    public double[] getOrigin() {
        return origin;
    }

    /**
     * The n dimensional ordinates for the origin of this grid (in srsName coordinate space)
     * 
     * @param origin
     */
    public void setOrigin(double[] origin) {
        this.origin = origin;
    }

    /**
     * double[] x will reference a n dimensional vector offset (in srsName space) that allows you to correspond an integer grid index to a point in real space
     * 
     * @return
     */
    public double[][] getOffsetVectors() {
        return offsetVectors;
    }

    /**
     * double[] x will reference a n dimensional vector offset (in srsName space) that allows you to correspond an integer grid index to a point in real space
     * 
     * @param offsetVectors
     */
    public void setOffsetVectors(double[][] offsetVectors) {
        this.offsetVectors = offsetVectors;
    }

    /**
     * The names of each axes
     * 
     * @return
     */
    public String[] getAxisNames() {
        return axisNames;
    }

    /**
     * The names of each axes
     * 
     * @param axisNames
     */
    public void setAxisNames(String[] axisNames) {
        this.axisNames = axisNames;
    }
}
