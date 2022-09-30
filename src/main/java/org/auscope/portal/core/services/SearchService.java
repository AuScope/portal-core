package org.auscope.portal.core.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;

/**
 * A search service that uses Apache Lucene to create and query search indexes for known layers.
 *
 */
public class SearchService {
	
	private final Log logger = LogFactory.getLog(getClass());
	// Number of CSW records to search 
	private final static int NUMBER_OF_RECORDS = 50;
	
	private String localCacheDir;
	private StandardAnalyzer analyzer = new StandardAnalyzer();
	
	
	public SearchService(String localCacheDir) {
		this.localCacheDir = localCacheDir;
	}
	
	/**
	 * Index known layers and CSW records for searching
	 * 
	 * @param knownLayersAndRecords list of known layers and their CSW records
	 */
	public void indexKnownLayersAndRecords(List<KnownLayerAndRecords> knownLayersAndRecords) {
		logger.info("Indexing collated layers and records for search");
		Analyzer analyzer = new StandardAnalyzer();
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
						// XXX Do we need to get more and make sure they match exactly? TEST USING COMPARISON
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
				
				// CSW records
				for(CSWRecord cswRecord: layerAndRecords.getBelongingRecords()) {
					// Skip record if it doesn't have a URL or any associated online resources
					if(cswRecord.getOnlineResources() == null || cswRecord.getOnlineResources().length == 0 || StringUtils.isEmpty(cswRecord.getRecordInfoUrl())) {
						continue;
					}
					Document cswDoc = this.createDocumentFromCSWRecord(cswRecord, layerAndRecords.getKnownLayer().getId());
					
					// Only add if record isn't already in index
					if(DirectoryReader.indexExists(directory)) {
						if(indexReader != null) {
							final String[] recordFields = {"layerId", "recordInfoUrl"};
							Query query = new MultiFieldQueryParser(recordFields, analyzer)
								      .parse("layerId:\"" + layerAndRecords.getKnownLayer().getId() + "\" AND recordInfoUrl:\"" + cswRecord.getRecordInfoUrl() + "\"");
							boolean recordNotFound = true;
							TopDocs topDocs = searcher.search(query, NUMBER_OF_RECORDS);
						    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
						    	Document searchedDoc = searcher.doc(scoreDoc.doc);
						        if(searchedDoc.get("layerId").equals(layerAndRecords.getKnownLayer().getId()) && searchedDoc.get("recordInfoUrl").equals(cswRecord.getRecordInfoUrl())) {
						        	recordNotFound = false;
						        	if(!this.documentsAreEqual(cswDoc, searchedDoc)) {
										iwriter.updateDocument(new Term("layerId", cswDoc.get("layerId")), cswDoc);
										break;
						        	}
						        }
						    }
						    // If the record wasn't found it must be new
						    if(recordNotFound) {
						    	iwriter.updateDocument(new Term("layerId", cswDoc.get("layerId")), cswDoc);
						    }
						}
					} else {
						iwriter.updateDocument(new Term("layerId", cswDoc.get("layerId")), cswDoc);
					}
				}
			}
			iwriter.close();
		} catch(ParseException pe) {
			logger.error("Parse error indexing known layers for search: " + pe.getLocalizedMessage());
		} catch(IOException ioe) {
			logger.error("Error indexing known layers for search: " + ioe.getLocalizedMessage());
		}
	}

	/**
	 * Create an indexable document from a layer and CSW records
	 * 
	 * @param layerAndRecords layer and CSW records
	 * @return
	 */
	private Document createDocumentFromKnownLayer(KnownLayerAndRecords layerAndRecords) {
		Document document = new Document();
		
		document.add(new Field("type", "layer", TextField.TYPE_STORED));
		document.add(new Field("id", layerAndRecords.getKnownLayer().getId(), TextField.TYPE_STORED));
		document.add(new Field("group", layerAndRecords.getKnownLayer().getGroup(), TextField.TYPE_STORED));
		document.add(new Field("name", layerAndRecords.getKnownLayer().getName(), TextField.TYPE_STORED));
		document.add(new Field("description", layerAndRecords.getKnownLayer().getDescription(), TextField.TYPE_STORED));
		
		// TODO: Filters, or leave to original search?
		
		return document;
	}
	
	/**
	 * Create an indexable document from a CSW record
	 * 
	 * @param record CSW record
	 * @param layerId ID of layer
	 * @return
	 */
	private Document createDocumentFromCSWRecord(CSWRecord record, String layerId) {
		Document document = new Document();
		document.add(new Field("type", "csw", TextField.TYPE_STORED));
		document.add(new Field("layerId", layerId, TextField.TYPE_STORED));
		
		if(!StringUtils.isEmpty(record.getServiceName())) {
			document.add(new Field("serviceName", record.getServiceName(), TextField.TYPE_STORED));
		}
		
		if(!StringUtils.isEmpty(record.getRecordInfoUrl())) {
			document.add(new Field("recordInfoUrl", record.getRecordInfoUrl(), TextField.TYPE_STORED));
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
		
		if(record.getDescriptiveKeywords() != null && record.getDescriptiveKeywords().length > 0) {
			document.add(new Field("keywords", String.join(",", record.getDescriptiveKeywords()), TextField.TYPE_STORED));
		}
		
		// TODO: Look at LatLon types (see IndexableField)
		if(record.getCSWGeographicElements() != null && record.getCSWGeographicElements().length > 0) {
			final String bboxString = record.getCSWGeographicElements()[0].getWestBoundLongitude() + "," +
					record.getCSWGeographicElements()[0].getSouthBoundLatitude() + "," +
					record.getCSWGeographicElements()[0].getEastBoundLongitude() + "," +
					record.getCSWGeographicElements()[0].getNorthBoundLatitude();
			document.add(new Field("bbox", bboxString, TextField.TYPE_STORED));
		}
		
		return document;
	}
	
	/**
	 * Test equality of two indexable documents
	 * @param docA first document
	 * @param docB second document
	 * @return true if documents are equal, false if not
	 */
	private boolean documentsAreEqual(Document docA, Document docB) {
		// Iterate first document's fields
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
	 * Search the index with the specified search fields and query string.
	 * 
	 * @param searchFields the fields of the index to search
	 * @param queryString the query string used to filter results
	 * @return a List of Documents matching the supplied search criteria
	 * @throws ParseException error when parsing search
	 * @throws IOException error reading/writing files 
	 */
	public List<Document> searchIndex(String[] searchFields, String queryString) throws ParseException, IOException {
		List<Document> documents = new ArrayList<>();
	    Query query = new MultiFieldQueryParser(searchFields, analyzer)
	      .parse(queryString);
	    Path indexPath = new File(localCacheDir).toPath();
	    Directory directory = FSDirectory.open(indexPath);
	    IndexReader indexReader = DirectoryReader.open(directory);
	    IndexSearcher searcher = new IndexSearcher(indexReader);
	    TopDocs topDocs = searcher.search(query, 100);
	    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        documents.add(searcher.doc(scoreDoc.doc));
	    }
	    return documents;
	}
	
}
