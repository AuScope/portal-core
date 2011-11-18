package org.auscope.portal.server.web.service;

import org.auscope.portal.csw.record.AbstractCSWOnlineResource;


public interface CSWRecordsFilterVisitor {

    public boolean visit(AbstractCSWOnlineResource resource);

}
