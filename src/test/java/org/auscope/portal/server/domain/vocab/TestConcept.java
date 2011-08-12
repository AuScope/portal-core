package org.auscope.portal.server.domain.vocab;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for Concept
 * @author Josh Vote
 *
 */
public class TestConcept {
    /**
     * Sanity checks for equals of the various object types
     */
    @Test
    public void testEquals() {
        final String expectedUrn = "my:urn";
        final String unexpectedUrn = expectedUrn + ":different";
        Concept concept = new Concept(expectedUrn);

        Assert.assertTrue(concept.equals(expectedUrn));
        Assert.assertTrue(concept.equals((Object) expectedUrn));
        Assert.assertTrue(concept.equals(new Concept(new String(expectedUrn))));

        Assert.assertFalse(concept.equals(unexpectedUrn));
        Assert.assertFalse(concept.equals((Object) unexpectedUrn));
        Assert.assertFalse(concept.equals(new Concept(new String(unexpectedUrn))));

    }

    /**
     * Sanity checks for equals of the various object types succeed when the comparison urn is null
     */
    @Test
    public void testEqualsNullComparison() {
        final String expectedUrn = "my:urn";
        Concept concept = new Concept(expectedUrn);

        Assert.assertFalse(concept.equals((String)null));
        Assert.assertFalse(concept.equals((Object) null));
        Assert.assertFalse(concept.equals(new Concept(null)));
    }

    /**
     * Sanity checks for equals of the various object types succeed when the NamedIndividual urn is null
     */
    @Test
    public void testEqualsNullUrn() {
        final String expectedUrn = null;
        final String unexpectedUrn = "different";
        Concept concept = new Concept(expectedUrn);

        Assert.assertTrue(concept.equals((String)null));
        Assert.assertFalse(concept.equals((Object) null));
        Assert.assertTrue(concept.equals(new Concept(null)));

        Assert.assertFalse(concept.equals(unexpectedUrn));
        Assert.assertFalse(concept.equals((Object) unexpectedUrn));
        Assert.assertFalse(concept.equals(new Concept(new String(unexpectedUrn))));
    }
}
