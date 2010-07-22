package org.auscope.portal.server.domain.wcs;

import java.io.Serializable;

/**
 * Represents any of the items that belong as children to  a <wcs:spatialDomain> element in a DescribeCoverage response.
 * @author vot002
 *
 */
public interface SpatialDomain extends Serializable {
    public String getSrsName();
    public String getType();
}
