package org.auscope.portal.core.services.responses.wcs;

import java.io.Serializable;

/**
 * Represents a <wcs:TemporalDomain> element from a WCS DescribeCoverage response
 * 
 * @author vot002
 *
 */
public interface TemporalDomain extends Serializable {
    public String getType();
}
