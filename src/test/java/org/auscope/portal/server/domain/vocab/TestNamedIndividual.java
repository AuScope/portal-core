package org.auscope.portal.server.domain.vocab;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for NamedIndividual
 * @author Josh Vote
 *
 */
public class TestNamedIndividual {
    /**
     * Sanity checks for equals of the various object types
     */
    @Test
    public void testEquals() {
        final String expectedUrn = "my:urn";
        final String unexpectedUrn = expectedUrn + ":different";
        NamedIndividual ni = new NamedIndividual(expectedUrn);

        Assert.assertTrue(ni.equals(expectedUrn));
        Assert.assertTrue(ni.equals((Object) expectedUrn));
        Assert.assertTrue(ni.equals(new NamedIndividual(new String(expectedUrn))));

        Assert.assertFalse(ni.equals(unexpectedUrn));
        Assert.assertFalse(ni.equals((Object) unexpectedUrn));
        Assert.assertFalse(ni.equals(new NamedIndividual(new String(unexpectedUrn))));

    }

    /**
     * Sanity checks for equals of the various object types succeed when the comparison urn is null
     */
    @Test
    public void testEqualsNullComparison() {
        final String expectedUrn = "my:urn";
        NamedIndividual ni = new NamedIndividual(expectedUrn);

        Assert.assertFalse(ni.equals((String)null));
        Assert.assertFalse(ni.equals((Object) null));
        Assert.assertFalse(ni.equals(new NamedIndividual(null)));
    }

    /**
     * Sanity checks for equals of the various object types succeed when the NamedIndividual urn is null
     */
    @Test
    public void testEqualsNullUrn() {
        final String expectedUrn = null;
        final String unexpectedUrn = "different";
        NamedIndividual ni = new NamedIndividual(expectedUrn);

        Assert.assertTrue(ni.equals((String)null));
        Assert.assertFalse(ni.equals((Object) null));
        Assert.assertTrue(ni.equals(new NamedIndividual(null)));

        Assert.assertFalse(ni.equals(unexpectedUrn));
        Assert.assertFalse(ni.equals((Object) unexpectedUrn));
        Assert.assertFalse(ni.equals(new NamedIndividual(new String(unexpectedUrn))));
    }
}
