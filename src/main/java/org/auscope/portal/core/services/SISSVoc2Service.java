package org.auscope.portal.core.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc2MethodMaker;
import org.auscope.portal.core.services.namespaces.VocabNamespaceContext;
import org.auscope.portal.core.services.responses.vocab.Concept;
import org.auscope.portal.core.services.responses.vocab.ConceptFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Service class for interacting with a SISSVoc endpoint
 * 
 * @author Josh Vote
 */
public class SISSVoc2Service {
    private HttpServiceCaller httpServiceCaller;
    private ConceptFactory conceptFactory;
    private SISSVoc2MethodMaker sissVocMethodMaker;

    public SISSVoc2Service(HttpServiceCaller httpServiceCaller, ConceptFactory conceptFactory,
            SISSVoc2MethodMaker sissVocMethodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.conceptFactory = conceptFactory;
        this.sissVocMethodMaker = sissVocMethodMaker;
    }

    /**
     * Gets the Concept objects associated with the specified label
     * 
     * @param serviceUrl
     *            The SISSVoc endpoint to query
     * @param repository
     *            The SISSVoc repository to query
     * @param label
     *            The label query for
     * @return
     * @throws PortalServiceException
     */
    public Concept[] getConceptByLabel(String serviceUrl, String repository, String label)
            throws PortalServiceException {
        HttpRequestBase method = null;
        try {
            //Do the request
            method = sissVocMethodMaker.getConceptByLabelMethod(serviceUrl, repository, label);
            try (InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method)) {

                // Parse the response
                Document doc = DOMUtil.buildDomFromStream(responseStream);
                XPathExpression rdfExpression = DOMUtil.compileXPathExpr("rdf:RDF", new VocabNamespaceContext());
                Node response = (Node) rdfExpression.evaluate(doc, XPathConstants.NODE);
                return conceptFactory.parseFromRDF(response);
            }
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * Gets the Concept objects associated with a getCommodity request
     * 
     * @param serviceUrl
     *            The SISSVoc endpoint to query
     * @param repository
     *            The SISSVoc repository to query
     * @param commodityParent
     *            The commodity paretn
     * @return
     * @throws PortalServiceException
     */
    public Concept[] getCommodityConcepts(String serviceUrl, String repository, String commodityParent)
            throws PortalServiceException {
        HttpRequestBase method = null;
        try {
            //Do the request
            method = sissVocMethodMaker.getCommodityMethod(serviceUrl, repository, commodityParent);
            try (InputStream responseStream = httpServiceCaller.getMethodResponseAsStream(method)) {
                // Parse the response
                Document doc = DOMUtil.buildDomFromStream(responseStream);
                XPathExpression expression = DOMUtil.compileXPathExpr("/sparql:sparql/sparql:results/sparql:result",
                        new VocabNamespaceContext());
                NodeList exprResult = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

                List<Concept> concepts = new ArrayList<>();
                for (int i = 0; i < exprResult.getLength(); i++) {
                    Element node = (Element) exprResult.item(i);
                    String uri = node.getElementsByTagName("uri").item(0).getTextContent();
                    String literal = node.getElementsByTagName("literal").item(0).getTextContent();

                    Concept concept = new Concept(uri);
                    concept.setLabel(literal);

                    concepts.add(concept);
                }

                return concepts.toArray(new Concept[concepts.size()]);
            }
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
}
