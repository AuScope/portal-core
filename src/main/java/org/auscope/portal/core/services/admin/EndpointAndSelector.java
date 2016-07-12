package org.auscope.portal.core.services.admin;

/**
 * Represents a tuple between a OGC web service and some form of dataset selector eg - WFS endpoint + feature type name WMS endpoint + layer name
 * 
 * @author Josh Vote
 */
public class EndpointAndSelector {
    /** the endpoint (URL) */
    private String endpoint;
    /** the selector (type name, layer name etc) */
    private String selector;

    /**
     * Creates a new instance
     * 
     * @param endpoint
     *            the endpoint (URL)
     * @param selector
     *            the selector (type name, layer name etc)
     */
    public EndpointAndSelector(String endpoint, String selector) {
        this.endpoint = endpoint;
        this.selector = selector;
    }

    /**
     * Gets the endpoint (URL)
     * 
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the selector (type name, layer name etc)
     * 
     * @return
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Prints the contents of this instance.
     */
    @Override
    public String toString() {
        return "EndpointAndSelector [endpoint=" + endpoint + ", selector="
                + selector + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
        result = prime * result + ((selector == null) ? 0 : selector.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EndpointAndSelector))
            return false;
        EndpointAndSelector other = (EndpointAndSelector) obj;
        if (endpoint == null) {
            if (other.endpoint != null)
                return false;
        } else if (!endpoint.equals(other.endpoint))
            return false;
        if (selector == null) {
            if (other.selector != null)
                return false;
        } else if (!selector.equals(other.selector))
            return false;
        return true;
    }

}
