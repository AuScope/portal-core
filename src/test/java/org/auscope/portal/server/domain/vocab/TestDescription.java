package org.auscope.portal.server.domain.vocab;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for Description
 * @author Josh Vote
 *
 */
public class TestDescription {
    /**
     * Sanity checks for equals of the various object types
     */
    @Test
    public void testEquals() {
        final String expectedUrn = "my:urn";
        final String unexpectedUrn = expectedUrn + ":different";
        Description desc = new Description(expectedUrn);

        Assert.assertTrue(desc.equals(expectedUrn));
        Assert.assertTrue(desc.equals((Object) expectedUrn));
        Assert.assertTrue(desc.equals(new Description(new String(expectedUrn))));

        Assert.assertFalse(desc.equals(unexpectedUrn));
        Assert.assertFalse(desc.equals((Object) unexpectedUrn));
        Assert.assertFalse(desc.equals(new Description(new String(unexpectedUrn))));

    }

    /**
     * Sanity checks for equals of the various object types succeed when the comparison urn is null
     */
    @Test
    public void testEqualsNullComparison() {
        final String expectedUrn = "my:urn";
        Description desc = new Description(expectedUrn);

        Assert.assertFalse(desc.equals((String)null));
        Assert.assertFalse(desc.equals((Object) null));
        Assert.assertFalse(desc.equals(new Description(null)));
    }

    /**
     * Sanity checks for equals of the various object types succeed when the NamedIndividual urn is null
     */
    @Test
    public void testEqualsNullUrn() {
        final String expectedUrn = null;
        final String unexpectedUrn = "different";
        Description desc = new Description(expectedUrn);

        Assert.assertTrue(desc.equals((String)null));
        Assert.assertFalse(desc.equals((Object) null));
        Assert.assertTrue(desc.equals(new Description(null)));

        Assert.assertFalse(desc.equals(unexpectedUrn));
        Assert.assertFalse(desc.equals((Object) unexpectedUrn));
        Assert.assertFalse(desc.equals(new String(unexpectedUrn)));
    }
}
