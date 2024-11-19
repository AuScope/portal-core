package org.auscope.portal.core.services;

import org.apache.jena.rdf.model.*;
import org.auscope.portal.core.services.namespaces.VocabNamespaceContext;
import org.auscope.portal.core.util.structure.RDFTriple;
import org.apache.jena.vocabulary.SKOS;

import java.util.*;



/**
 * Service class that handles filtering of JENA models that are cached by the {@link VocabularyCacheService}.
 * Filters are either selectors on resources or properties, and also find narrower relationships with specified resources
 */
public class VocabularyFilterService {

    private VocabularyCacheService vocabularyCacheService;

    public VocabularyFilterService(VocabularyCacheService vocabularyCacheService) {
        this.vocabularyCacheService = vocabularyCacheService;
    }

    /**
     * Returns a list of property value strings, given a vocab cache id, string value for the SKOS prefLabel, and a property type
     * 
     * @param vocabularyId  Cache ID of vocabulary
     * @param prefLabelVal String value of SKOS prefLabel to search for
     * @param property Property whose value will be returned in array
     * @return list of Strings
     */
    public ArrayList<String> getVocabularyById(String vocabularyId, String prefLabelVal, Property property) {
        Model model = this.vocabularyCacheService.getVocabularyCacheById(vocabularyId);
        ResIterator iterator = model.listResourcesWithProperty(SKOS.prefLabel);
        ArrayList<String> result = new ArrayList<String>();
        while (iterator.hasNext()) {
            Resource res = iterator.next();
            if (!res.getProperty(SKOS.prefLabel).getString().equals(prefLabelVal)) {
                continue;
            }
            String defn = res.getProperty(property).getString();
            if (defn != null) {
                result.add(defn);
            }
        }
        return result;
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
    public Map<String, String> getVocabularyById(String vocabularyId, RDFTriple... triples) {
        Model model = this.vocabularyCacheService.getVocabularyCacheById(vocabularyId);

        if (triples == null || triples.length == 0) {
            return  getLabeledVocabulary(model);
        }

        Model filteredModel = ModelFactory.createDefaultModel();
        for (RDFTriple  triple: triples) {
            StmtIterator stmtIterator;
            if (triple.language == null) {
                stmtIterator = filterModel(model, triple.subject, triple.predicate, (RDFNode) triple.object).listStatements();
            } else {
                stmtIterator = filterModel(model, triple.subject, triple.predicate, (String) triple.object, triple.language).listStatements();
            }
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                filteredModel.add(model.listStatements(statement.getSubject(), null, (RDFNode) null));
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
            ResIterator iterator = model.listResourcesWithProperty(SKOS.prefLabel);
            while (iterator.hasNext()) {
                Resource res = iterator.next();
                StmtIterator prefLabelIt = res.listProperties(SKOS.prefLabel);
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
        Model filteredModel = filterModel(model, resource, property, (RDFNode) null);
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
        Model filteredModel = filterModel(model, resource, property, (RDFNode) null);
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
     * Filters a model by the given (resource, property, object) triple
     *
     * @param model Model to filter
     * @param resource Resource to filter by
     * @param property Property to filter by
     * @param object RDFNode to filter by
     * @return New filtered model
     */
    private Model filterModel(Model model, Resource resource, Property property, RDFNode object) {
        Model filteredModel = ModelFactory.createDefaultModel();
        if (model != null) {
            StmtIterator stmtIterator = model.listStatements(resource, property, object);
            filteredModel.add(stmtIterator);
        }
        return filteredModel;
    }

    /**
     * Filters a model by the given (resource, property, object) triple
     *
     * @param model Model to filter
     * @param resource Resource to filter by
     * @param property Property to filter by
     * @param object RDFNode to filter by
     * @param language language
     * @return New filtered model
     */
    private Model filterModel(Model model, Resource resource, Property property, String object, String language) {
        Model filteredModel = ModelFactory.createDefaultModel();
        if (model != null) {
            StmtIterator stmtIterator = model.listStatements(resource, property, object, language);
            filteredModel.add(stmtIterator);
        }
        return filteredModel;
    }
}
