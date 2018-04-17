package org.auscope.portal.core.services;

import com.hp.hpl.jena.rdf.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.VocabularyCacheService;
import org.auscope.portal.core.services.namespaces.VocabNamespaceContext;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class that handles filtering of JENA models that are cached by the {@link VocabularyCacheService}.
 * Filters are either selectors on resources or properties, and also find narrower relationships with specified resources
 */
public class VocabularyFilterService {

    private final Log log = LogFactory.getLog(getClass());

    private VocabularyCacheService vocabularyCacheService;


    public VocabularyFilterService(VocabularyCacheService vocabularyCacheService) {
        this.vocabularyCacheService = vocabularyCacheService;
    }


    /**
     * Returns key-value pairs of vocabulary terms for the specified cache ID, filtered by
     * the queries listed in the selectors. If there are no selectors in the call then it returns
     * the vocabulary for the unfiltered model.
     *
     * @param vocabularyId Cache ID of vocabulary model
     * @param selectors List of selectors used to query the model
     * @return
     */
    public Map<String, String> getVocabularyById(String vocabularyId, Selector... selectors) {
        Model model = this.vocabularyCacheService.getVocabularyCacheById(vocabularyId);

        if (selectors == null || selectors.length == 0) {
            return  getLabeledVocabulary(model);
        }

        Model filteredModel = ModelFactory.createDefaultModel();
        for (Selector selector : selectors) {
            StmtIterator stmtIterator = filterModel(model, selector).listStatements();
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                Selector resourceSelector = new SimpleSelector(statement.getSubject(), null, (RDFNode) null);
                filteredModel.add(model.listStatements(resourceSelector));
            }

        }

        return getLabeledVocabulary(filteredModel);
    }

    /**
     * Returns key-value pairs of vocabulary terms for model. Pairs are prefLabel and URI.
     *
     * @param model The model to transform into URI and prefLabels.
     * @return
     */
    private Map<String, String> getLabeledVocabulary(Model model) {
        Map<String, String> result = new HashMap<>();

        if (model != null) {
            Property prefLabelProperty = model.createProperty(VocabNamespaceContext.SKOS_NAMESPACE, "prefLabel");

            ResIterator iterator = model.listResourcesWithProperty(prefLabelProperty);
            while (iterator.hasNext()) {
                Resource res = iterator.next();
                StmtIterator prefLabelIt = res.listProperties(prefLabelProperty);
                while (prefLabelIt.hasNext()) {
                    Statement prefLabelStatement = prefLabelIt.next();
                    String prefLabel = prefLabelStatement.getString();

                    String urn = res.getURI();
                    if (urn != null) {
                        result.put(urn, prefLabel);
                    }

                }
            }
        }
        return result;
    }

    /**
     * Returns set of all narrower URIs, narrower and narrowerTransitive, for given cache ID and URI.
     * If no narrowerTransitive found, it recursively follows the narrower relationships through the model.
     *
     * @param vocabularyId  Cache ID of vocabulary model
     * @param uri Vocabulary URI to find narrower terms
     * @return
     */
    public Set<String> getAllNarrower(String vocabularyId, String uri) {
        Model model = this.vocabularyCacheService.getVocabularyCacheById(vocabularyId);
        Set<String> result = new HashSet<>();
        result.add(uri);
        Set<String> narrowerTransitive = getNarrowerTransitive(model, uri);
        if (result.addAll(narrowerTransitive)) {
            result.addAll(getNarrower(model, uri));
        } else {
            result.addAll(getNarrowRecursive(model, uri));
        }
        return result;
    }

    public Set<String> getNarrowRecursive(String vocabularyId, String uri) {
        Model model = this.vocabularyCacheService.getVocabularyCacheById(vocabularyId);
        return getNarrowRecursive(model, uri);
    }

    /**
     * Recursive narrower query through the model for the given cache ID and URI
     *
     * @param model Cache ID of vocabulary model
     * @param uri Vocabulary URI to find narrower terms
     * @return Set of URIs
     */
    private Set<String> getNarrowRecursive(Model model, String uri) {
        Set<String> result = new HashSet<>();

        Set<String> narrower = getNarrower(model, uri);

        result.addAll(narrower);
        for (String narrowUri : narrower) {
            result.addAll(getNarrowRecursive(model, narrowUri));
        }
        return result;
    }


    /**
     * Narrower concepts for the given URI and vocabualry ID.
     *
     * @param vocabularyId The vocabulary ID.
     * @param uri Vocabulary URI to find narrower terms
     * @return
     */
    public Set<String> getNarrower(String vocabularyId, String uri) {
        Model model = this.vocabularyCacheService.getVocabularyCacheById(vocabularyId);
        return getNarrower(model, uri);
    }

    /**
     * Narrower concepts for the given URI and model.
     *
     * @param model The vocabulary model
     * @param uri Vocabulary URI to find narrower terms
     * @return Set of URIs
     */
    private Set<String> getNarrower(Model model, String uri) {
        Property property = model.createProperty(VocabNamespaceContext.SKOS_NAMESPACE, "narrower");
        Resource resource = model.getResource(uri);
        Selector selector = new SimpleSelector(resource, property, (RDFNode) null);
        Model filteredModel = filterModel(model, selector);
        return getSubjectUris(filteredModel);
    }


    /**
     * Narrower concepts for the given URI and vocabulary ID.
     *
     * @param vocabularyId The vocabulary ID.
     * @param uri Vocabulary URI to find narrower terms
     * @return
     */
    public Set<String> getNarrowerTransitive(String vocabularyId, String uri) {
        Model model = this.vocabularyCacheService.getVocabularyCacheById(vocabularyId);
        return getNarrowerTransitive(model, uri);
    }

    /**
     * Narrower Transitive concepts for the given URI and voabualry model
     *
     * @param model The vocabulary model
     * @param uri Vocabulary URI to find narrower terms
     * @return Set of URIs
     */
    private Set<String> getNarrowerTransitive(Model model, String uri) {
        Property property = model.createProperty(VocabNamespaceContext.SKOS_NAMESPACE, "narrowerTransitive");
        Resource resource = model.getResource(uri);
        Selector selector = new SimpleSelector(resource, property, (RDFNode) null);
        Model filteredModel = filterModel(model, selector);
        return getSubjectUris(filteredModel);
    }

    /** Returns all subject resource URIs for the given model
     *
     * @param model
     * @return Set of URIs
     */
    private Set<String> getSubjectUris(Model model) {
        Set<String> result = new HashSet<>();
        if (model != null) {
            StmtIterator stmtIterator = model.listStatements();
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                String uri = statement.getResource().getURI();
                result.add(uri);
            }
        }
        return result;
    }


    /**
     * Filters a model by the given selector
     *
     * @param model Model to filter
     * @param selector Query for the model to filter on
     * @return New filtered model
     */
    private Model filterModel(Model model, Selector selector) {
        Model filteredModel = ModelFactory.createDefaultModel();
        if (model != null) {
            StmtIterator stmtIterator = model.listStatements(selector);
            filteredModel.add(stmtIterator);
        }
        return filteredModel;
    }
}
