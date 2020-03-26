package org.auscope.portal.core.services.responses.wms;

import javax.xml.xpath.XPathException;

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
     * @throws XPathException
     *             the x path expression exception
     */
    public String getName() throws XPathException;

    /**
     * Gets the title.
     *
     * @return the title
     * @throws XPathException
     *             the x path expression exception
     */
    public String getTitle() throws XPathException;

    /**
     * Gets the abstract.
     *
     * @return the abstract
     * @throws XPathException
     *             the x path expression exception
     */
    public String getAbstract() throws XPathException;

    /**
     * Gets the metadata URL.
     *
     * @return the metadataURL
     * @throws XPathException
     *             the x path expression exception
     */
    public String getMetadataURL() throws XPathException;
    
    /**
     * Gets the legendURL.
     *
     * @return the legendURL
     * @throws XPathException
     *             the x path expression exception
     */
    public String getLegendURL() throws XPathException;
    
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
     * @throws XPathException
     *             the x path expression exception
     */
    public String[] getChildLayerSRS() throws XPathException;

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString();

}
