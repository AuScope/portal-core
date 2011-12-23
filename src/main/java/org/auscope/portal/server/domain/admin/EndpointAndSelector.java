package org.auscope.portal.server.domain.admin;

/**
 * Represents a tuple between a OGC web service and some form of dataset selector
 * eg - WFS endpoint + feature type name
 *      WMS endpoint + layer name
 * @author Josh Vote
 *
 */
public class EndpointAndSelector {
    /** the endpoint (URL) */
    private String endpoint;
    /** the selector (type name, layer name etc)*/
    private String selector;

    /**
     * Creates a new instance
     * @param endpoint the endpoint (URL)
     * @param selector the selector (type name, layer name etc)
     */
    public EndpointAndSelector(String endpoint, String selector) {
        this.endpoint = endpoint;
        this.selector = selector;
    }
    /**
     * Gets the endpoint (URL)
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the selector (type name, layer name etc)
     * @return
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Compares two instances of this class for equality. Only equal if endpoint AND selector match
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof EndpointAndSelector) {
            return this.equals((EndpointAndSelector) o);
        }

        return this == o;
    }

    /**
     * Compares two instances of this class for equality. Only equal if endpoint AND selector match
     * @param comparison the comparison object
     * @return
     */
    public boolean equals(EndpointAndSelector comparison) {
        if (this == null || comparison == null) {
            return this == comparison;
        }

        return this.endpoint.equals(comparison.endpoint) &&
                this.selector.equals(comparison.selector);
    }

    /**
     * Prints the contents of this instance.
     */
    @Override
    public String toString() {
        return "EndpointAndSelector [endpoint=" + endpoint + ", selector="
                + selector + "]";
    }


}
