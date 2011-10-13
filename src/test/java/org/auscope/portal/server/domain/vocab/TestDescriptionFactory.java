package org.auscope.portal.server.domain.vocab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import org.auscope.portal.Util;
import org.auscope.portal.server.util.DOMUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Unit tests for DescriptionFactory
 * @author Josh Vote
 *
 */
public class TestDescriptionFactory {

    private void assertSameDescription(Description[] expected, Description[] actual, List<String> traversedUrns) {
        String errMsg = String.format("%1$s != %2$s", Arrays.toString(expected), Arrays.toString(actual));

        Assert.assertArrayEquals(errMsg, expected, actual);
        for (int i = 0; i < expected.length; i++) {
            assertSameDescription(expected[i], actual[i], traversedUrns);
        }
    }

    private void assertSameDescription(Description expected, Description actual, List<String> traversedUrns) {
        String errMsg = String.format("%1$s != %2$s", expected, actual);

        Assert.assertEquals(errMsg, expected, actual);
        Assert.assertEquals(errMsg, expected.isHref(), actual.isHref());

        //To deal with cycles in the hierarchy
        if (traversedUrns.contains(expected.getUrn())) {
            return;
        } else {
            traversedUrns.add(expected.getUrn());
        }

        assertSameDescription(expected.getBroader(), actual.getBroader(), traversedUrns);
        assertSameDescription(expected.getNarrower(), actual.getNarrower(), traversedUrns);
        assertSameDescription(expected.getRelated(), actual.getRelated(), traversedUrns);
        assertSameDescription(expected.getTopConcepts(), actual.getTopConcepts(), traversedUrns);
    }

    /**
     * Runs the factory through a standard SISSVoc response XML
     */
    @Test
    public void testSISSVocRDF() throws Exception {
        //Build our expectation
        Description concept1 = new Description("urn:concept:1");
        Description concept2 = new Description("urn:concept:2");
        Description concept3 = new Description("urn:concept:3");
        Description concept4 = new Description("urn:concept:4");
        Description ni1 = new Description("urn:ni:1");
        Description ni2 = new Description("urn:ni:2");
        Description ni3 = new Description("urn:ni:3");

        concept1.setNarrower(new Description[] {concept2, concept3, ni2});

        concept2.setBroader(new Description[] {concept1});
        concept2.setRelated(new Description[] {concept3});

        concept3.setBroader(new Description[] {concept1});
        concept3.setRelated(new Description[] {concept2});
        concept3.setNarrower(new Description[] {ni1});

        concept4.setNarrower(new Description[] {ni3});

        ni1.setBroader(new Description[] {concept3});

        ni2.setBroader(new Description[] {concept1});

        ni3.setBroader(new Description[] {concept4});

        Description[] expectation = new Description[] {concept1, concept4};

        //Build our actual list
        String responseXml = Util.loadXML("src/test/resources/SISSVocResponse.xml");
        Document responseDoc = DOMUtil.buildDomFromString(responseXml);
        Node rdfNode = (Node) DOMUtil.compileXPathExpr("rdf:RDF", new VocabNamespaceContext()).evaluate(responseDoc, XPathConstants.NODE);
        DescriptionFactory df = new DescriptionFactory();
        Description[] actualDescriptions = df.parseFromRDF(rdfNode);

        Assert.assertNotNull(actualDescriptions);
        assertSameDescription(expectation, actualDescriptions, new ArrayList<String>());
    }
}
