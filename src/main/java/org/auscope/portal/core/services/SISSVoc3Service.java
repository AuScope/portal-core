package org.auscope.portal.core.services;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc3MethodMaker.Format;
import org.auscope.portal.core.services.namespaces.VocabNamespaceContext;
import org.auscope.portal.core.services.responses.vocab.Concept;
import org.auscope.portal.core.services.responses.vocab.ConceptFactory;
import org.auscope.portal.core.services.responses.vocab.Description;
import org.auscope.portal.core.services.responses.vocab.DescriptionFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.FileIOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A service class for interacting with a SISSVoc version 3 deployment.
 *
 * @author Josh Vote
 *
 */
public class SISSVoc3Service {
    /** The class for making HTTP requests*/
    protected HttpServiceCaller httpServiceCaller;

    /** The class for generating SISSVoc requests*/
    protected SISSVoc3MethodMaker sissVocMethodMaker;

    /**
     * The service URL in the form - http://host.name/path/to/service
     *
     * The URL will be appended with repository/command names and file formats
     */
    private String baseUrl;

    /**
     * The repository at baseUrl that will be queried
     */
    private String repository;

    /**
     * This service will request concepts in batches of this size. Defaults to 1000
     */
    private int pageSize = 1000;

    public SISSVoc3Service(HttpServiceCaller httpServiceCaller,
            SISSVoc3MethodMaker sissVocMethodMaker, String baseUrl, String repository) {
        super();
        this.httpServiceCaller = httpServiceCaller;
        this.sissVocMethodMaker = sissVocMethodMaker;
        this.baseUrl = baseUrl;
        this.repository = repository;
    }

    /**
     * This service will request concepts in batches of this size. Defaults to 1000
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * This service will request concepts in batches of this size. Defaults to 1000
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * The service URL in the form - http://host.name/path/to/service
     *
     * The URL will be appended with repository/command names and file formats
     * @return
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     *  The name of the repository to query
     * @return
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Gets all descriptions for a given page, appends the parsed values to the specified JENA model.
     *
     * Returns the next page number or -1 if end of data
     * @param repository
     * @param pageNumber The page number to request
     * @param pageSize The number of descriptions per request
     * @param model receives the response Descriptions
     */
    private int requestPageOfConcepts(int pageNumber, int pageSize, Model model) throws PortalServiceException {
        //Make our request
        HttpMethodBase method = sissVocMethodMaker.getAllConcepts(baseUrl, repository, Format.Rdf, this.pageSize, pageNumber);
        InputStream is;
        try {
            is = httpServiceCaller.getMethodResponseAsStream(method);
        } catch (Exception e) {
            method.releaseConnection();
            throw new PortalServiceException(method, e);
        }

        //Parse the response into an XML document
        Document doc = null;
        int nextPage = -1;
        try {
            doc = DOMUtil.buildDomFromStream(is);

            VocabNamespaceContext nc = new VocabNamespaceContext();
            XPathExpression getDescriptionsExpr = DOMUtil.compileXPathExpr("rdf:RDF/descendant::api:Page/api:items/rdf:Description", nc);
            XPathExpression nextPageExpr = DOMUtil.compileXPathExpr("rdf:RDF/descendant::api:Page/xhv:next", nc);

            Node nextPageNode = (Node) nextPageExpr.evaluate(doc, XPathConstants.NODE);
            if (nextPageNode != null) {
                nextPage = pageNumber + 1;
            }

            NodeList allDescriptions = (NodeList) getDescriptionsExpr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < allDescriptions.getLength(); i++) {
                String rdf = DOMUtil.buildStringFromDom(allDescriptions.item(i), true);
                model.read(new StringReader(rdf), null);
            }

        } catch (Exception e) {
            throw new PortalServiceException(method, e);
        } finally {
            method.releaseConnection();
            FileIOUtil.closeQuietly(is);
        }

        return nextPage;
    }

    /**
     * Gets all RDF concepts at the specified repository as a single JENA Model. The results
     * will be requested page by page until the entire repository has been traversed.
     *
     * @return
     * @throws PortalServiceException
     */
    public Model getAllConcepts() throws PortalServiceException {
        Model model = ModelFactory.createDefaultModel();
        int pageNumber = 0;
        int pageSize = this.pageSize;

        //Request each page in turn - put the results into Model
        do {
            pageNumber = requestPageOfConcepts(pageNumber, pageSize, model);
        } while (pageNumber >= 0);

        return model;
    }

    /**
     * Makes a request to the configured SISSVoc service for a concept to describe the specified URI
     * @param resourceUri The vocabulary resource to look for
     * @return
     * @throws PortalServiceException
     */
    public Resource getResourceByUri(String resourceUri) throws PortalServiceException {
        HttpMethodBase method = sissVocMethodMaker.getResourceByUri(baseUrl, repository, resourceUri, Format.Rdf);
        InputStream is = null;
        try {
            is = httpServiceCaller.getMethodResponseAsStream(method);

            Model model = ModelFactory.createDefaultModel();
            model.read(is, null);

            return model.getResource(resourceUri);
        } catch (Exception e) {
            throw new PortalServiceException(method, e);
        } finally {
            FileIOUtil.closeQuietly(is);
            method.releaseConnection();
        }
    }


}
