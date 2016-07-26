package org.auscope.portal.core.services.responses.wms;

import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;

/**
 * This class represents WMS Layer node within GetCapabilites WMS response.
 *
 * @author VictorTey
 * @version
 *
 */
public interface GetCapabilitiesWMSLayerRecord {

    // ------------------------------------------ Attribute Setters and Getters

    /**
     * Gets the name.
     *
     * @return the name
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public String getName() throws XPathExpressionException;

    /**
     * Gets the title.
     *
     * @return the title
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public String getTitle() throws XPathExpressionException;

    /**
     * Gets the abstract.
     *
     * @return the abstract
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public String getAbstract() throws XPathExpressionException;

    /**
     * Gets the metadata URL.
     *
     * @return the metadataURL
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public String getMetadataURL() throws XPathExpressionException;
    
    /**
     * Gets the legendURL.
     *
     * @return the legendURL
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public String getLegendURL() throws XPathExpressionException;
    
    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     */
    public CSWGeographicBoundingBox getBoundingBox();

    /**
     * Gets the child layer srs.
     *
     * @return the child layer srs
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    public String[] getChildLayerSRS() throws XPathExpressionException;

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString();

}
