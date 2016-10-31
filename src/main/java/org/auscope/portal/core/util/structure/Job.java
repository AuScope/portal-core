package org.auscope.portal.core.util.structure;

import org.auscope.portal.core.services.PortalServiceException;

/**
 * A job definition is a unit of work to be handled by AbstractJobQueue
 *
 * @author tey006
 *
 */
public interface Job {

    public boolean run() throws PortalServiceException;

    @Override
    public boolean equals(Object j);

}
