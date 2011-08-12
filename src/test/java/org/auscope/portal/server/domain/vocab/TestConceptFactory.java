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
 * Unit tests for ConceptFactory
 * @author Josh Vote
 *
 */
public class TestConceptFactory {

    private void assertSameConcept(Concept[] expected, Concept[] actual, List<String> traversedUrns) {
        String errMsg = String.format("%1$s != %2$s", Arrays.toString(expected), Arrays.toString(actual));

        Assert.assertArrayEquals(errMsg, expected, actual);
        for (int i = 0; i < expected.length; i++) {
            assertSameConcept(expected[i], actual[i], traversedUrns);
        }
    }

    private void assertSameConcept(Concept expected, Concept actual, List<String> traversedUrns) {
        String errMsg = String.format("%1$s != %2$s", expected, actual);

        Assert.assertEquals(errMsg, expected, actual);
        Assert.assertEquals(errMsg, expected.getLabel(), actual.getLabel());
        Assert.assertEquals(errMsg, expected.getPreferredLabel(), actual.getPreferredLabel());
        Assert.assertEquals(errMsg, expected.isHref(), actual.isHref());

        //To deal with cycles in the hierarchy
        if (traversedUrns.contains(expected.getUrn())) {
            return;
        } else {
            traversedUrns.add(expected.getUrn());
        }

        assertSameConcept(expected.getBroader(), actual.getBroader(), traversedUrns);
        assertSameConcept(expected.getNarrower(), actual.getNarrower(), traversedUrns);
        assertSameConcept(expected.getRelated(), actual.getRelated(), traversedUrns);
    }

    /**
     * Runs the factory through a standard SISSVoc response XML
     */
    @Test
    public void testSISSVocRDF() throws Exception {
        //Build our expectation
        Concept concept1 = new Concept("urn:concept:1");
        Concept concept2 = new Concept("urn:concept:2");
        Concept concept3 = new Concept("urn:concept:3");
        NamedIndividual ni1 = new NamedIndividual("urn:ni:1");
        NamedIndividual ni2 = new NamedIndividual("urn:ni:2");

        concept1.setNarrower(new Concept[] {concept2, concept3, ni2});
        concept1.setLabel("LabelConcept1");
        concept1.setPreferredLabel("PrefLabelConcept1");

        concept2.setBroader(new Concept[] {concept1});
        concept2.setRelated(new Concept[] {concept3});
        concept2.setLabel("LabelConcept2");
        concept2.setPreferredLabel("PrefLabelConcept2");

        concept3.setBroader(new Concept[] {concept1});
        concept3.setRelated(new Concept[] {concept2});
        concept3.setNarrower(new Concept[] {ni1});
        concept3.setLabel("LabelConcept3");
        concept3.setPreferredLabel("PrefLabelConcept3");

        ni1.setBroader(new Concept[] {concept3});
        ni1.setLabel("LabelNamedIndividual1");
        ni1.setPreferredLabel("PrefLabelNamedIndividual1");

        ni2.setBroader(new Concept[] {concept1});
        ni2.setLabel("LabelNamedIndividual2");
        ni2.setPreferredLabel("PrefLabelNamedIndividual2");

        Concept[] expectation = new Concept[] {concept1};

        //Build our actual list
        String responseXml = Util.loadXML("src/test/resources/SISSVocResponse.xml");
        Document responseDoc = DOMUtil.buildDomFromString(responseXml);
        Node rdfNode = (Node) DOMUtil.compileXPathExpr("rdf:RDF", new VocabNamespaceContext()).evaluate(responseDoc, XPathConstants.NODE);
        ConceptFactory cf = new ConceptFactory();
        Concept[] actualConcepts = cf.parseFromRDF(rdfNode);

        Assert.assertNotNull(actualConcepts);
        assertSameConcept(expectation, actualConcepts, new ArrayList<String>());
    }
}
