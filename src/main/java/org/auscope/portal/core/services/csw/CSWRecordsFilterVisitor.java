package org.auscope.portal.core.services.csw;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;

/**
 * A Filtering visitor which can be used to perform filtering operations on any AbstractCSWOnlineResource. This follows the standard Visitor pattern which is
 * accepted by AbstractCSWOnlineResource.
 * 
 * @author Victor.Tey@csiro.au
 *
 */
public interface CSWRecordsFilterVisitor {

    public boolean visit(AbstractCSWOnlineResource resource);

}
