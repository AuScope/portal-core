package org.auscope.portal.core.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.sandbox.document.LatLonBoundingBox;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;

/**
 * A search service that uses Apache Lucene to create and query search indexes for known layers.
 *
 */
public class SearchService {
	// Limit search results
	static final int NUMBER_OF_RECORDS_TO_SEARCH = 1000;
	// Limit unique terms
	static final int NUMBER_OF_UNIQUE_TERMS = 10000;
	
	private final Log logger = LogFactory.getLog(getClass());
	private String localCacheDir;
	private StandardAnalyzer analyzer = new StandardAnalyzer();
	private AnalyzingSuggester analyzingSuggester;
	
	
	public SearchService(String localCacheDir) {
		this.localCacheDir = localCacheDir;
	}
	
	/**
	 * 
	 * @param autocompleteDirectory
	 * @param autocompleteAnalyzer
	 * @throws IOException
	 */
	public void buildAnalyzingSuggester(Directory autocompleteDirectory, Analyzer autocompleteAnalyzer)
	        throws IOException {
	    DirectoryReader sourceReader = DirectoryReader.open(autocompleteDirectory);
	    LuceneDictionary dict = new LuceneDictionary(sourceReader, "description");
	    analyzingSuggester = new AnalyzingSuggester(autocompleteDirectory, "autocomplete_temp",
	            autocompleteAnalyzer);
	    analyzingSuggester.build(dict);
	}
	
	/**
	 * Index known layers and CSW records for searching
	 * 
	 * @param knownLayersAndRecords list of known layers and their CSW records
	 */
	public void indexKnownLayersAndRecords(List<KnownLayerAndRecords> knownLayersAndRecords) {
		logger.info("Indexing collated layers and records for search");
		try {
			Path indexPath = new File(localCacheDir).toPath();
		    Directory directory = FSDirectory.open(indexPath);
		    IndexWriterConfig config = new IndexWriterConfig(analyzer);
		    IndexWriter iwriter = new IndexWriter(directory, config);

		    // Open index for reading if it exists
		    IndexReader indexReader = null;
		    IndexSearcher searcher = null;
		    if(DirectoryReader.indexExists(directory)) {
		    	indexReader = DirectoryReader.open(directory);
		    	searcher = new IndexSearcher(indexReader);
		    }
		    
		    // Layers
			for(KnownLayerAndRecords layerAndRecords: knownLayersAndRecords) {
				Document doc = createDocumentFromKnownLayer(layerAndRecords);
				if(DirectoryReader.indexExists(directory)) {
					// Only add if layer isn't already in index
					if(indexReader != null) {
						Query query = new QueryParser("id", analyzer).parse(layerAndRecords.getKnownLayer().getId());
						TopDocs topDocs = searcher.search(query, 1);
						boolean layerNotFound = true;
					    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
					    	Document searchedDoc = searcher.doc(scoreDoc.doc);
					        if(searchedDoc.get("id").equals(layerAndRecords.getKnownLayer().getId())) {
					        	layerNotFound = false;
					        	if(!this.documentsAreEqual(doc, searchedDoc)) {
							    	iwriter.updateDocument(new Term("id", layerAndRecords.getKnownLayer().getId()), doc);
					        		continue;
					        	}
					        }
					    }
					    // If the layer wasn't found it must be new
					    if(layerNotFound) {
					    	iwriter.updateDocument(new Term("id", layerAndRecords.getKnownLayer().getId()), doc);
					    }
					}
				} else {
					iwriter.updateDocument(new Term("id", layerAndRecords.getKnownLayer().getId()), doc);
				}
			}
			iwriter.close();
			
			// Build the analyzing suggester
			buildAnalyzingSuggester(directory, analyzer);
		} catch(ParseException pe) {
			logger.error("Parse error indexing known layers for search: " + pe.getLocalizedMessage());
		} catch(IOException ioe) {
			logger.error("Error indexing known layers for search: " + ioe.getLocalizedMessage());
		}
	}
	
	/**
	 * Create an indexable document from a known layer
	 * 
	 * @param layerAndRecords a known layer and its associated CSW records
	 * @return the known layer represented as a Lucene Document
	 */
	private Document createDocumentFromKnownLayer(KnownLayerAndRecords layerAndRecords) {
		Document document = new Document();
		
		// Layer info
		document.add(new Field("id", layerAndRecords.getKnownLayer().getId(), TextField.TYPE_STORED));
		document.add(new Field("group", layerAndRecords.getKnownLayer().getGroup(), TextField.TYPE_STORED));
		document.add(new Field("name", layerAndRecords.getKnownLayer().getName(), TextField.TYPE_STORED));
		document.add(new Field("description", layerAndRecords.getKnownLayer().getDescription(), TextField.TYPE_STORED));
		
		// CSW record info
		for(CSWRecord record: layerAndRecords.getBelongingRecords()) {	
			if(!StringUtils.isEmpty(record.getDataIdentificationAbstract())) {
				document.add(new Field("abstract", record.getDataIdentificationAbstract(), TextField.TYPE_STORED));
			}
			
			if(!StringUtils.isEmpty(record.getServiceName())) {
				document.add(new Field("serviceName", record.getServiceName(), TextField.TYPE_STORED));
			}
			
			if(!StringUtils.isEmpty(record.getFileIdentifier())) {
				document.add(new Field("fileIdentifier", record.getFileIdentifier(), TextField.TYPE_STORED));
			}
			
			if(record.getContact() != null) {
				if(!StringUtils.isEmpty(record.getContact().getOrganisationName())) {
					document.add(new Field("contact", record.getContact().getOrganisationName(), TextField.TYPE_STORED));
				}
			}
			
			if(record.getFunder() != null) {
				if(!StringUtils.isEmpty(record.getFunder().getOrganisationName())) {
					document.add(new Field("funder", record.getFunder().getOrganisationName(), TextField.TYPE_STORED));
				}
			}
			
			if(record.getDate() != null) {
				document.add(new Field("date", record.getDate().toString(), TextField.TYPE_STORED));
			}
			
			for(String keyword: record.getDescriptiveKeywords()) {
				document.add(new Field("keyword", keyword, TextField.TYPE_STORED));
			}
			
			List<OnlineResourceType> services = new ArrayList<OnlineResourceType>();
			for(AbstractCSWOnlineResource resource: record.getOnlineResources()) {
				if(!services.contains(resource.getType())) {
					services.add(resource.getType());
				}
			}
			for(OnlineResourceType ort: services) {
				document.add(new Field("service", ort.toString(), TextField.TYPE_STORED));
			}
			
			if(record.getCSWGeographicElements() != null && record.getCSWGeographicElements().length > 0) {
				for (CSWGeographicElement bbox: record.getCSWGeographicElements()) {
					// Sanitise data, some values were slightly beyond limits
					double southLat = bbox.getSouthBoundLatitude() >= -90.0 ? bbox.getSouthBoundLatitude() : -90.0;
					double westLong = bbox.getWestBoundLongitude() >= -180.0 ? bbox.getWestBoundLongitude() : -180.0;
					double northLat = bbox.getNorthBoundLatitude() <= 90.0 ? bbox.getNorthBoundLatitude() : 90.0;
					double eastLong = bbox.getEastBoundLongitude() <= 180.0 ? bbox.getEastBoundLongitude() : 180.0;
					document.add(new LatLonBoundingBox("bbox", southLat, westLong, northLat, eastLong));
				}
			}
		}
		return document;
	}
	
	/**
	 * Test equality of two indexable documents
	 * 
	 * @param docA first document
	 * @param docB second document
	 * @return true if documents are equal, false if not
	 */
	private boolean documentsAreEqual(Document docA, Document docB) {
		// Documents must have the same number of fields
		if (docA.getFields().size() != docB.getFields().size()) {
			return false;
		}
		// Iterate first document's fields and compare to second's
		List<IndexableField> fields = docA.getFields();
        for (IndexableField field : fields) {
        	if(docA.get(field.name()) != null && docB.get(field.name()) != null &&
        			!docA.get(field.name()).equals(docB.get(field.name()))) {
    			return false;
    		}
        }
		return true;
	}
	
	/**
	 * Search the index including spatial information
	 * 
	 * @param searchFields the fields of the index to search
	 * @param queryString the query string used to filter results
	 * @param spatialRelation "Intersects", "Contains" or "Within"
	 * @param southBoundLatitude south bounding box point
	 * @param westBoundLongitude west bounding box point
	 * @param northBoundLatitude north bounding box point
	 * @param eastBoundLongitude east bounding box point
	 * @return a List of Documents matching the supplied search criteria
	 * @throws ParseException error when parsing search
	 * @throws IOException error reading/writing files
	 */
	public List<Document> searchIndex(String[] searchFields, String queryString, String spatialRelation,
			Double southBoundLatitude, Double westBoundLongitude,
			Double northBoundLatitude, Double eastBoundLongitude) throws ParseException, IOException {
		
		Query query = null;
		List<Document> documents = new ArrayList<>();
		// Text field query
		Query textQuery = null;
		if(!StringUtils.isEmpty(queryString)) {
			textQuery = new MultiFieldQueryParser(searchFields, analyzer).parse(queryString);
		}
	    
	    // Spatial query (if requested)
	    Query spatialQuery = null;
	    if(!StringUtils.isEmpty(spatialRelation) && southBoundLatitude != null && westBoundLongitude != null && northBoundLatitude != null && eastBoundLongitude != null) {
		    switch (spatialRelation) {
		    	case "Within":
		    		spatialQuery = LatLonBoundingBox.newWithinQuery("bbox", southBoundLatitude.doubleValue(), westBoundLongitude.doubleValue(), northBoundLatitude.doubleValue(), eastBoundLongitude.doubleValue());
		    		break;
		    	case "Contains":
		    		spatialQuery = LatLonBoundingBox.newContainsQuery("bbox", southBoundLatitude.doubleValue(), westBoundLongitude.doubleValue(), northBoundLatitude.doubleValue(), eastBoundLongitude.doubleValue());
		    		break;
		    	case "Intersects":
		    	default:
		    		spatialQuery = LatLonBoundingBox.newIntersectsQuery("bbox", southBoundLatitude.doubleValue(), westBoundLongitude.doubleValue(), northBoundLatitude.doubleValue(), eastBoundLongitude.doubleValue());
		    		break;
		    }
	    }
	    
	    // Query will either be a TextQuery if no spatial component was specified, otherwise a combined BooleanQuery
	    if (textQuery != null && spatialQuery == null) {
	    	query = textQuery;
	    } else if(textQuery == null && spatialQuery != null) {
	    	query = spatialQuery;
	    } else {
	    	query = new BooleanQuery.Builder()
	    			.add(textQuery, BooleanClause.Occur.MUST)
					.add(spatialQuery, BooleanClause.Occur.MUST).build();
	    }
		
	    Path indexPath = new File(localCacheDir).toPath();
	    Directory directory = FSDirectory.open(indexPath);
	    IndexReader indexReader = DirectoryReader.open(directory);
	    IndexSearcher searcher = new IndexSearcher(indexReader);
	    TopDocs topDocs = searcher.search(query, NUMBER_OF_RECORDS_TO_SEARCH);
	    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        documents.add(searcher.doc(scoreDoc.doc));
	    }
	    return documents;
	}
	
	/**
	 * Return all unique terms for a field (such as keywords)
	 * 
	 * @param field field to query
	 * @return an array of unique terms for a given field
	 * @throws ParseException parsing error
	 * @throws IOException IO error
	 */
	public List<String> getUniqueTerms(String field) throws ParseException, IOException {
		List<String> terms = new ArrayList<>();
	    Path indexPath = new File(localCacheDir).toPath();
	    Directory directory = FSDirectory.open(indexPath);
	    IndexReader indexReader = DirectoryReader.open(directory);
	    Query query = new MatchAllDocsQuery();
	    IndexSearcher searcher = new IndexSearcher(indexReader);
	    TopDocs topDocs = searcher.search(query, NUMBER_OF_UNIQUE_TERMS);
	    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	    	Document doc = searcher.doc(scoreDoc.doc);
	    	if(!terms.contains(doc.get(field))) {
	    		terms.add(doc.get(field));
	    	}
	    }
	    return terms;
	}
	
	/**
	 * Suggest terms from the index
	 *
	 * @param term the term used to suggest further terms
	 * @param num the number of suggested terms to return
	 * @return a list of suggested terms
	 * @throws IOException
	 */
	public List<String> suggestTerms(String term, int num) throws IOException {
	    List<Lookup.LookupResult> lookup = analyzingSuggester.lookup(term, false, num);
	    List<String> suggestions = lookup.stream().map(a -> a.key.toString()).collect(Collectors.toList());
	    return suggestions;
	}
	
}
