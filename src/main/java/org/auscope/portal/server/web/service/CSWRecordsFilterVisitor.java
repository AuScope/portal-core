package org.auscope.portal.server.web.service;

import org.auscope.portal.csw.record.AbstractCSWOnlineResource;


/**
 * A Filtering visitor which can be used to perform filtering operations on  any AbstractCSWOnlineResource.
 * This follows the standard Visitor pattern  which is accepted by AbstractCSWOnlineResource.
 * @author Victor.Tey@csiro.au
 *
 */
public interface CSWRecordsFilterVisitor {

    public boolean visit(AbstractCSWOnlineResource resource);

}
