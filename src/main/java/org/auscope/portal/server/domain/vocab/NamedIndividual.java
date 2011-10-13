package org.auscope.portal.server.domain.vocab;

/**
 * A highly simplified representation of an OWL 'Named Individual'
 *
 * @author Josh Vote
 *
 */
public class NamedIndividual extends Concept {

    /**
     * Creates a new NamedIndividual, empty for all but it's URN
     * @param urn The unique ID for this named individual
     */
    public NamedIndividual(String urn) {
        super(urn);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NamedIndividual) {
            return this.equals((NamedIndividual) obj);
        } else {
            return super.equals(obj);
        }
    }

    /**
     * Compares 2 named individuals based on their URN
     * @param ni
     * @return
     */
    public boolean equals(NamedIndividual ni) {
        if (ni == null) {
            return false;
        }
        return this.equals(ni.getUrn());
    }
}
