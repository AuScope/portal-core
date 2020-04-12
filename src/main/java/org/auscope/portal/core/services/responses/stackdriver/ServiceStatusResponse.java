package org.auscope.portal.core.services.responses.stackdriver;

import java.io.Serializable;

/**
 * Simple tuple of a StackDriver service name and response value
 *
 * @author Josh Vote (CSIRO)
 * @author Rini Angreani (CSIRO)
 *
 */
public class ServiceStatusResponse implements Serializable {

    private boolean uptimeCheck;
    private String serviceName;
	private String checkId;

    public ServiceStatusResponse(boolean passedCheck, String serviceName) {
        super();
        this.uptimeCheck = passedCheck;
        this.serviceName = serviceName;
    }

    /**
     * Checks if keyed service is up or down
     * @return
     */
    public boolean isUp() {
        return uptimeCheck;
    }

    /**
     * Gets the service name checked
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }

    public String getCheckId() {
    	return checkId;
    }

    public void setCheckId(String checkId) {
    	this.checkId = checkId;
    }
}
