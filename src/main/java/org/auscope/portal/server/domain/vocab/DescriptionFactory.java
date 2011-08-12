package org.auscope.portal.server.domain.vocab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory class for instantiating Description
 * @author Josh Vote
 *
 */
public class DescriptionFactory {

    protected final Log log = LogFactory.getLog(getClass());
    private static final VocabNamespaceContext nc = new VocabNamespaceContext();

    /**
     * Attempts to parse any related Descriptions from a descriptionNode.
     *
     * The parsing will proceed recusively through any Descriptions that are defined 'inline'
     *
     * @param descriptionNode An rdf:Description node
     * @param relationXPath The type of relation to parse
     * @return
     * @throws XPathExpressionException
     */
    protected Description[] attemptParseRelations(Node descriptionNode, String relationXPath) throws XPathExpressionException {
        XPathExpression getRelationsExpr = DOMUtil.compileXPathExpr(relationXPath, nc);
        XPathExpression getInlineDescExpr = DOMUtil.compileXPathExpr("rdf:Description", nc);

        NodeList relationNodes = (NodeList) getRelationsExpr.evaluate(descriptionNode, XPathConstants.NODESET);
        List<Description> descriptions = new ArrayList<Description>();

        //Parsing our relations is complicated by the fact that a Concept/NamedIndividual can be defined inline
        //or 'linked' via a string in the rdf:about
        XPathExpression getUrnExpr = DOMUtil.compileXPathExpr("@rdf:resource", nc);
        for (int i = 0; i < relationNodes.getLength(); i++) {
            String urn = (String) getUrnExpr.evaluate(relationNodes.item(i), XPathConstants.STRING);

            //We have a reference to another description
            if (urn != null && !urn.isEmpty()) {
                descriptions.add(new Description(urn, true));
                continue;
            }

            //We have an inline description
            Node inlineDescNode = (Node) getInlineDescExpr.evaluate(relationNodes.item(i), XPathConstants.NODE);
            if (inlineDescNode != null) {
                Description inlineDesc = attemptParseDescription(inlineDescNode);
                if (inlineDesc != null) {
                    descriptions.add(inlineDesc);
                }
                continue;
            }
        }

        return descriptions.toArray(new Description[descriptions.size()]);
    }

    /**
     * Given a rdf:Description node, parse it into a Description object. Unless the node defines all related
     * Descriptions inline the resulting Description node will be populated with related Descriptions that have the
     * 'href' flag set.
     * @throws XPathExpressionException
     */
    protected Description attemptParseDescription(Node node) throws XPathExpressionException {
        String urn = (String) DOMUtil.compileXPathExpr("@rdf:about", nc).evaluate(node, XPathConstants.STRING);
        if (urn == null || urn.isEmpty()) {
            return null;
        }

        Description desc = new Description(urn);
        desc.setBroader(attemptParseRelations(node, "skos:broader"));
        desc.setNarrower(attemptParseRelations(node, "skos:narrower"));
        desc.setRelated(attemptParseRelations(node, "skos:related"));
        desc.setTopConcepts(attemptParseRelations(node, "skos:hasTopConcept"));

        return desc;
    }

    /**
     * Given a description and a map of descriptions keyed by URN. Attempt to replace each 'href' relation with
     * an object from descriptions.
     *
     * The replacements will occur directly into descs
     * @param descs
     * @param descriptionsMap
     */
    protected void attemptResolveHrefs(Description[] descs, Map<String, Description> descriptionsMap, List<String> traversedUrns) {
        for (int i = 0; i < descs.length; i++) {
            if (descs[i].isHref()) {
                Description linkedObj = descriptionsMap.get(descs[i].getUrn());
                if (linkedObj != null) {
                    descs[i] = linkedObj;
                }
            } else {
                attemptResolveHrefs(descs[i], descriptionsMap, traversedUrns);
            }
        }
    }

    /**
     * Given a description and a map of descriptions keyed by URN. Attempt to replace each 'href' relation with
     * an object from descriptions
     * @param desc
     * @param descriptionsMap
     */
    protected void attemptResolveHrefs(Description desc, Map<String, Description> descriptionsMap, List<String> traversedUrns) {

        //To deal with cycles in the hierarchy
        if (traversedUrns.contains(desc.getUrn())) {
            return;
        } else {
            traversedUrns.add(desc.getUrn());
        }

        Description[] broader = desc.getBroader();
        attemptResolveHrefs(broader, descriptionsMap, traversedUrns);
        desc.setBroader(broader);

        Description[] narrower = desc.getNarrower();
        attemptResolveHrefs(narrower, descriptionsMap, traversedUrns);
        desc.setNarrower(narrower);

        Description[] related = desc.getRelated();
        attemptResolveHrefs(related, descriptionsMap, traversedUrns);
        desc.setRelated(related);

        Description[] topConcepts = desc.getTopConcepts();
        attemptResolveHrefs(topConcepts, descriptionsMap, traversedUrns);
        desc.setTopConcepts(topConcepts);
    }

    /**
     * Appends desc and all of desc's (non href) children to parsedDescriptions
     * @param desc
     * @param parsedDescriptions
     */
    private void addDescriptionToMap(Description desc, Map<String, Description> parsedDescriptions) {
        if (desc == null) {
            return;
        }

        parsedDescriptions.put(desc.getUrn(), desc);
        for (Description broader : desc.getBroader()) {
            if (!broader.isHref()) {
                addDescriptionToMap(broader, parsedDescriptions);
            }
        }
        for (Description narrower : desc.getNarrower()) {
            if (!narrower.isHref()) {
                addDescriptionToMap(narrower, parsedDescriptions);
            }
        }
        for (Description topConcept : desc.getTopConcepts()) {
            if (!topConcept.isHref()) {
                addDescriptionToMap(topConcept, parsedDescriptions);
            }
        }
        for (Description related : desc.getRelated()) {
            if (!related.isHref()) {
                addDescriptionToMap(related, parsedDescriptions);
            }
        }
    }

    /**
     * Parses every rdf:Description element that is a child of the specified node
     *
     * @param rdfNode The node to search for rdf:Description elements from
     * @return
     */
    public Description[] parseFromRDF(Node rdfNode) {
        //Firstly parse all of our descriptions into a map keyed by their urn
        Map<String, Description> parsedDescriptions = new HashMap<String, Description>();
        try {
            XPathExpression getDescriptionExpr = DOMUtil.compileXPathExpr("rdf:Description", nc);
            NodeList descriptionNodes = (NodeList) getDescriptionExpr.evaluate(rdfNode, XPathConstants.NODESET);

            for (int i = 0; i < descriptionNodes.getLength(); i++) {
                Description desc = attemptParseDescription(descriptionNodes.item(i));
                addDescriptionToMap(desc, parsedDescriptions);
            }
        } catch (XPathExpressionException e) {
            log.error("Unable to evaluate inbuilt XPath - requesting descriptions", e);
            throw new RuntimeException();
        }

        //Next we take our parsed descriptions and attempt to link them together by replacing
        //'href' descriptions with links to the actual objects (if they exist)
        for (String urn : parsedDescriptions.keySet()) {
            attemptResolveHrefs(parsedDescriptions.get(urn), parsedDescriptions, new ArrayList<String>());
        }

        //Finally we return an array of our "top concepts"
        List<Description> topConcepts = new ArrayList<Description>();
        for (String urn : parsedDescriptions.keySet()) {
            for (Description topConcept : parsedDescriptions.get(urn).getTopConcepts()) {
                if (!topConcept.isHref()) {
                    topConcepts.add(topConcept);
                }
            }
        }

        return topConcepts.toArray(new Description[topConcepts.size()]);
    }
}
