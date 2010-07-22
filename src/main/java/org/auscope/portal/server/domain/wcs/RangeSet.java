package org.auscope.portal.server.domain.wcs;

import java.io.Serializable;

/**
 * Represents a <wcs:RangeSet> element from a DescribeCoverage request
 * @author vot002
 *
 */
public interface RangeSet extends Serializable {
    /**
     * Contains a simple text description of the object
     * (Can be null / empty)
     */
    public String getDescription();
    /**
     * Identifier for this object, normally a descriptive name
     */
    public String getName();
    /**
     * Show human readable label for this object
     */
    public String getLabel();
    /**
     * Defines a range provided by a coverage. Multiple occurences are used for compound observations, to 
     * describe an additional parameter (that is an independent variable besides space and time) plus the
     * valid values of this parameter (which GetCoverage requests can use to select subsets of the coverage
     * offering).
     * 
     */
    public AxisDescription[] getAxisDescriptions();
    /**
     * Values used when valid values are not available (The coverage encoding may specify a fixed value
     * for null (eg "-9999" or "n/a") but often the choice is up to the provider and must be communicated
     * to the client outside of the coverage itself). 
     * 
     * Can be null / empty
     */
    public ValueEnumType[] getNullValues();
}
