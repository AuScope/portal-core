package org.auscope.portal.core.services;

import com.hp.hpl.jena.rdf.model.*;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker;

import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker.View;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker.Format;

import org.auscope.portal.core.services.namespaces.VocabNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.FileIOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * A service class for interacting with a Linked Data API vocabulary service
 *
 * @author Josh Vote
 *
 */
public class VocabularyService {
    /** The class for making HTTP requests */
    protected HttpServiceCaller httpServiceCaller;

    /** The class for generating vocabulary requests */
    protected VocabularyMethodMaker vocabularyMethodMaker;

    /**
     * The service URL in the form - http://host.name/path/to/service
     *
     * The URL will be appended with repository/command names and file formats
     */
    private String serviceUrl;

    /**
     * This service will request concepts in batches of this size. Defaults to 1000
     */
    private int pageSize = 1000;

    public VocabularyService(HttpServiceCaller httpServiceCaller,
                             VocabularyMethodMaker vocabularyMethodMaker, String serviceUrl) {
        super();
        this.httpServiceCaller = httpServiceCaller;
        this.vocabularyMethodMaker = vocabularyMethodMaker;
        this.serviceUrl = serviceUrl;
    }

    /**
     * This service will request concepts in batches of this size. Defaults to 1000
     * 
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * This service will request concepts in batches of this size. Defaults to 1000
     * 
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * The service URL in the form - http://host.name/path/to/service
     *
     * The URL will be appended with repository/command names and file formats
     * 
     * @return
     */
    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * Gets all descriptions for a given page (as described by a HttpMethod), appends the parsed values to the specified JENA model.
     *
     * Returns true if there is more data (pages) to request. false otherwise. Exceptions will be rethrown as PortalServiceException objects
     *
     * @param method
     *            method to access to vocab service
     * @param model
     *            receives the response Descriptions
     */
    protected boolean requestPageOfConcepts(HttpRequestBase method, Model model) throws PortalServiceException {
        boolean moreData = false;

        try (InputStream inputStream  = httpServiceCaller.getMethodResponseAsStream(method)) {
            // Parse the response into an XML document
            Document document = null;
            document = DOMUtil.buildDomFromStream(inputStream);

            VocabNamespaceContext namespaceContext = new VocabNamespaceContext();
            XPathExpression getDescriptionsExpression = DOMUtil.compileXPathExpr("rdf:RDF/descendant::rdf:Description", namespaceContext);
            XPathExpression nextPageExpression = DOMUtil.compileXPathExpr("rdf:RDF/descendant::api:Page/xhv:next", namespaceContext);

            Node nextPageNode = (Node) nextPageExpression.evaluate(document, XPathConstants.NODE);
            if (nextPageNode != null) {
                moreData = true;
            }

            NodeList allDescriptions = (NodeList) getDescriptionsExpression.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < allDescriptions.getLength(); i++) {
                String rdf = DOMUtil.buildStringFromDom(allDescriptions.item(i), true);
                model.read(new StringReader(rdf), null);
            }

        } catch (Exception e) {
            throw new PortalServiceException(method, e);
        } finally {
            method.releaseConnection();
        }

        return moreData;
    }

    /**
     * Gets all RDF concepts at the specified repository as a single JENA Model.
     * The results will be requested page by page until the entire repository
     * has been traversed.
     *
     * @return
     * @throws PortalServiceException
     * @throws URISyntaxException
     */
    public Model getAllConcepts() throws PortalServiceException, URISyntaxException {
        Model model = ModelFactory.createDefaultModel();
        int pageNumber = 0;
        int ps = this.pageSize;

        // Request each page in turn - put the results into Model
        do {
            HttpRequestBase method = vocabularyMethodMaker.getAllConcepts(serviceUrl, Format.Rdf, ps, pageNumber);
            if (requestPageOfConcepts(method, model)) {
                pageNumber++;
            } else {
                break;
            }
        } while (true);

        return model;
    }

    /**
     * Gets all RDF concepts in the specified scheme as a single JENA Model. The
     * results will be requested page by page until the entire repository has
     * been traversed.
     *
     * @return
     * @throws PortalServiceException
     * @throws URISyntaxException
     */

    public Model getAllConceptsInScheme(String inScheme, View view) throws URISyntaxException, PortalServiceException {
        Model model = ModelFactory.createDefaultModel();
        int pageNumber = 0;
        int ps = this.pageSize;

        do {
            HttpRequestBase method = vocabularyMethodMaker.getAllConceptsInScheme(serviceUrl, inScheme,
                    Format.Rdf, view, ps, pageNumber);
            if (requestPageOfConcepts(method, model)) {
                pageNumber++;
            } else {
                break;
            }
        } while (true);

        return model;

    }

    /**
     * Returns all the relevant concepts for the given vocabulary
     *
     *
     * @return
     */
    public Map<String, String> getAllRelevantConcepts() throws URISyntaxException, PortalServiceException {
        Map<String, String> result = new HashMap<String, String>();

        Model model = getModel();

        Property prefLabelProperty = model.createProperty(VocabNamespaceContext.SKOS_NAMESPACE, "prefLabel");
        ResIterator iterator = model.listResourcesWithProperty(prefLabelProperty);
        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            StmtIterator prefLabelIterator = resource.listProperties(prefLabelProperty);
            while (prefLabelIterator.hasNext()) {
                Statement prefLabelStatement = prefLabelIterator.next();
                String prefLabel = prefLabelStatement.getString();

                String urn = resource.getURI();
                if (urn != null) {
                    result.put(urn, prefLabel);
                }
            }
        }
        return result;
    }

    /**
     * @return Returns full Jena model of the vocabulary
     *
     * @throws URISyntaxException
     * @throws PortalServiceException
     */
    public Model getModel() throws URISyntaxException, PortalServiceException {

        Model model = ModelFactory.createDefaultModel();
        int pageNumber = 0;
        int pageSize = this.getPageSize();

        do {
            HttpRequestBase method = vocabularyMethodMaker.getAllConcepts(getServiceUrl(), Format.Rdf, View.description, pageSize, pageNumber);
            if (requestPageOfConcepts(method, model)) {
                pageNumber++;
            } else {
                break;
            }
        } while (true);

        return model;
    }


    /**
     * Makes a request to the configured SISSVoc service for a concept to describe the specified URI
     * 
     * @param resourceUri
     *            The vocabulary resource to look for
     * @return
     * @throws PortalServiceException
     */
    @SuppressWarnings("resource")
    public Resource getResourceByUri(String resourceUri) throws PortalServiceException {
        InputStream inputStream = null;
        HttpRequestBase method = null;
        try {
            method = vocabularyMethodMaker.getResourceByUri(serviceUrl, resourceUri, Format.Rdf);
            inputStream = httpServiceCaller.getMethodResponseAsStream(method);
            Model model = ModelFactory.createDefaultModel();
            model.read(inputStream, null);

            return model.getResource(resourceUri);
        } catch (Exception e) {
            throw new PortalServiceException(method, e);
        } finally {
            FileIOUtil.closeQuietly(inputStream);
            if (method != null) {
                method.releaseConnection();                
            }
        }
    }

}
