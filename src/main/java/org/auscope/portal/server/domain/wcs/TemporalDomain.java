package org.auscope.portal.server.domain.wcs;

import java.io.Serializable;

/**
 * Represents a <wcs:TemporalDomain> element from a WCS DescribeCoverage response
 * @author vot002
 *
 */
public interface TemporalDomain extends Serializable {
    public String getType();
}
