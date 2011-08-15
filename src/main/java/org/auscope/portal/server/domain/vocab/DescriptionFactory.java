package org.auscope.portal.server.domain.vocab;

import java.util.ArrayList;
import java.util.Arrays;
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
    protected void attemptResolveHrefs(Description[] descs, Map<String, Description> descriptionsMap) {
        for (int i = 0; i < descs.length; i++) {
            if (descs[i].isHref()) {
                Description linkedObj = descriptionsMap.get(descs[i].getUrn());
                if (linkedObj != null) {
                    descs[i] = linkedObj;
                }
            }
        }
    }

    /**
     * Given a description and a map of descriptions keyed by URN. Attempt to replace each 'href' relation with
     * an object from descriptions.
     *
     * This function will NOT traverse any relations
     * @param desc
     * @param descriptionsMap
     */
    protected void attemptResolveHrefs(Description desc, Map<String, Description> descriptionsMap) {

        Description[] broader = desc.getBroader();
        attemptResolveHrefs(broader, descriptionsMap);
        desc.setBroader(broader);

        Description[] narrower = desc.getNarrower();
        attemptResolveHrefs(narrower, descriptionsMap);
        desc.setNarrower(narrower);

        Description[] related = desc.getRelated();
        attemptResolveHrefs(related, descriptionsMap);
        desc.setRelated(related);

        Description[] topConcepts = desc.getTopConcepts();
        attemptResolveHrefs(topConcepts, descriptionsMap);
        desc.setTopConcepts(topConcepts);
    }

    /**
     * Merge the entirety of 2 sets of descriptions into a single array. Any duplicates will be removed
     * with precedence being given to the non href duplicate.
     */
    private Description[] mergeDescriptionArrays(Description[] array1, Description[] array2) {
        List<Description> source = new ArrayList<Description>(Arrays.asList(array2));
        List<Description> destination = new ArrayList<Description>(Arrays.asList(array1));

        //Iterate our source list looking for duplicates
        for (Description candidate : source) {
            int destinationIndex = destination.indexOf(candidate);

            //With a duplicate we aim to keep a reference to a non href description (if available)
            if (destinationIndex >= 0) {
                Description destinationDuplicate = destination.get(destinationIndex);

                //In the case where our candidate is a non href AND our duplicate is a href we perform a replace
                //otherwise there is no point in replacing
                if (destinationDuplicate.isHref() && !candidate.isHref()) {
                    destination.remove(destinationIndex);
                    destination.add(candidate); //we don't care about ordering
                }
            } else {
                destination.add(candidate);
            }
        }

        return destination.toArray(new Description[destination.size()]);
    }

    /**
     * Appends desc and all of desc's (non href) children to parsedDescriptions
     *
     * If desc already exists in parsedDescriptions then the two descriptions will be merged
     *
     * @param desc
     * @param parsedDescriptions
     */
    private void addDescriptionToMap(Description desc, Map<String, Description> parsedDescriptions) {
        if (desc == null) {
            return;
        }

        //Either merge or insert our description
        Description existingDesc = parsedDescriptions.get(desc.getUrn());
        if (existingDesc != null) {
            existingDesc.setBroader(mergeDescriptionArrays(existingDesc.getBroader(), desc.getBroader()));
            existingDesc.setNarrower(mergeDescriptionArrays(existingDesc.getNarrower(), desc.getNarrower()));
            existingDesc.setRelated(mergeDescriptionArrays(existingDesc.getRelated(), desc.getRelated()));
            existingDesc.setTopConcepts(mergeDescriptionArrays(existingDesc.getTopConcepts(), desc.getTopConcepts()));
        } else {
            parsedDescriptions.put(desc.getUrn(), desc);
        }

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
     * If skos:hasTopConcept relations are defined only the top level descriptions
     * will be returned (the remaining will be linked via the top level concepts)
     *
     * If no skos:hasTopConcept every description element will be returned
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
            attemptResolveHrefs(parsedDescriptions.get(urn), parsedDescriptions);
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

        //This can occur if our RDF doesn't define 'Top Level Concepts'
        //In this case we just return every description that is an immediate child of our RDF
        if (topConcepts.isEmpty()) {
            topConcepts.addAll(parsedDescriptions.values());
        }

        return topConcepts.toArray(new Description[topConcepts.size()]);
    }
}
