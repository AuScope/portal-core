package org.auscope.portal.server.domain.wcs;

import java.io.Serializable;

/**
 * Represents a child element of the AxisDescription values element from a WCS DescribeCoverage response
 * @author vot002
 *
 */
public interface ValueEnumType extends Serializable {
    /**
     *  Should return "interval" or "singleValue"
     */
    public String getType();
}
