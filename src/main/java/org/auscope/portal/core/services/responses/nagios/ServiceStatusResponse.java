package org.auscope.portal.core.services.responses.nagios;

import java.io.Serializable;

/**
 * Simple tuple of a Nagios service name and response value
 * @author Josh Vote (CSIRO)
 *
 */
public class ServiceStatusResponse implements Serializable {

    public enum Status {
        /**
         * Service was able to be checked and appears to be functioning correctly
         */
        ok,
        /**
         * Service was checked, but it appeared to be above some “warning” threshold or did not appear to be working properly
         */
        warning,
        /**
         * Service was checked and it appears to not be running or it was above some “critical” threshold
         */
        critical,
        /**
         * Service was checked but is currently awaiting on its first check
         */
        pending,
        /**
         * Some unknown status or malfunctioning NAGIOS plugin
         */
        unknown
    }

    private Status status;
    private String serviceName;

    public ServiceStatusResponse(Status status, String serviceName) {
        super();
        this.status = status;
        this.serviceName = serviceName;
    }

    /**
     * Gets the status value for the keyed service
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the service name checked
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }
}
