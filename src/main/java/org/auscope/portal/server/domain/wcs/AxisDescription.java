package org.auscope.portal.server.domain.wcs;

import java.io.Serializable;

/**
 * Represents an <wcs:AxisDescription> element from a WCS DescribeCoverage response
 * @author vot002
 *
 */
public interface AxisDescription extends Serializable {
    /**
     * Contains a simple text description
     */
    public String getDescription();
    /**
     * Identifier for the object, normally a descriptive name
     */
    public String getName();
    /**
     * Short human readable label
     */
    public String getLabel();
    /**
     * The type and value constraints for the values of this axis
     */
    public ValueEnumType[] getValues();
}
