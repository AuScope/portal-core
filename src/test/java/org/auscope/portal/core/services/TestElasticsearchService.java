package org.auscope.portal.core.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.repositories.CSWRecordRepository;
import org.auscope.portal.core.repositories.CSWSuggestionRepository;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.es.CSWRecordSearchResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.jmock.lib.action.CustomAction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;

public class TestElasticsearchService extends PortalTestClass {

    private CSWRecordRepository recordRepository;
    private CSWSuggestionRepository suggestionRepository;
    private ElasticsearchOperations elasticsearchOperations;
    private ElasticsearchTemplate elasticsearchTemplate;
    private HttpServiceCaller httpServiceCaller;
    private ElasticsearchService elasticsearchService;

    @Before
    public void setUp() {
        recordRepository = context.mock(CSWRecordRepository.class);
        suggestionRepository = context.mock(CSWSuggestionRepository.class);
        elasticsearchOperations = context.mock(ElasticsearchOperations.class);
        elasticsearchTemplate = context.mock(ElasticsearchTemplate.class);
        httpServiceCaller = context.mock(HttpServiceCaller.class);
        elasticsearchService = new ElasticsearchService(httpServiceCaller);
        elasticsearchService.setRecordRepository(recordRepository);
        elasticsearchService.setSuggestionRepository(suggestionRepository);
        elasticsearchService.setElasticsearchOperations(elasticsearchOperations);
        elasticsearchService.setElasticsearchTemplate(elasticsearchTemplate);
        try {
        	elasticsearchService.setElasticsearchSuggestionUrl(new URL("http://localhost:9200/suggestion-index/_search"));
        } catch(MalformedURLException e) {
        	System.out.println(e.getLocalizedMessage());
        }
    }

    @Test
    public void testIndexCSWRecords() {
    	CSWRecord record1 = new CSWRecord();
    	record1.setFileIdentifier("abc123");
        record1.setMinScale(1.7976931348623157E308);
        record1.setMaxScale(1.7976931348623157E308);
    	CSWRecord record2 = new CSWRecord();
    	record2.setFileIdentifier("def456");
        List<CSWRecord> cswRecords = Arrays.asList(record1, record2);
        context.checking(new Expectations() {{
            oneOf(recordRepository).saveAll(cswRecords);
        }});
        assertDoesNotThrow(() -> elasticsearchService.indexCSWRecords(cswRecords));
    }

    @Test
    public void testUpdateCSWRecord() {
        CSWRecord cswRecord = new CSWRecord();
        cswRecord.setFileIdentifier("abc123");
        context.checking(new Expectations() {{
            oneOf(recordRepository).save(with(same(cswRecord)));
            will(new CustomAction("save CSWRecord") {
                @Override
                public Object invoke(org.jmock.api.Invocation invocation) throws Throwable {
                    Object arg = invocation.getParameter(0);
                    if (!(arg instanceof CSWRecord)) {
                        throw new ClassCastException("Expected CSWRecord but got " + arg.getClass().getName());
                    }
                    return null;
                }
            });
        }});

        assertDoesNotThrow(() -> elasticsearchService.updateCSWRecord(cswRecord));
    }

    @SuppressWarnings("unchecked")
	@Test
	public void testGetAllCSWRecords() {
        SearchScrollHits<CSWRecord> searchScrollHits = context.mock(SearchScrollHits.class);
        CSWRecord cswRecord = new CSWRecord();

        SearchHit<CSWRecord> searchHit = context.mock(SearchHit.class);
        List<SearchHit<CSWRecord>> searchHitList = new ArrayList<SearchHit<CSWRecord>>();
        searchHitList.add(searchHit);
        
        context.checking(new Expectations() {{
            allowing(elasticsearchTemplate).searchScrollStart(with(any(Long.class)), with(any(Query.class)), with(any(Class.class)), with(any(IndexCoordinates.class)));
            will(returnValue(searchScrollHits));
            allowing(elasticsearchTemplate).searchScrollContinue(with(any(String.class)), with(any(Long.class)), with(any(Class.class)), with(any(IndexCoordinates.class)));
            allowing(elasticsearchTemplate).searchScrollClear(with(any(String.class)));
            allowing(searchScrollHits).getScrollId();
            will(returnValue("123"));
            allowing(searchScrollHits).hasSearchHits();
            will(returnValue(true));
            allowing(searchScrollHits).getSearchHits();
            will(returnValue(searchHitList));
            oneOf(searchHit).getContent();
            will(returnValue(cswRecord));
        }});

        List<CSWRecord> records = elasticsearchService.getAllCSWRecords();
        assertNotNull(records);
    }

    @SuppressWarnings("unchecked")
	@Test
	public void testSearchCSWRecords() {
        String matchPhraseText = "test";
        List<String> queryFields = Arrays.asList("field1", "field2");
        Integer page = 0;
        Integer pageSize = 10;
        List<String> ogcServices = Arrays.asList("OGC:WMS");
        String spatialRelation = "intersects";
        Double westBoundLongitude = -180.0;
        Double eastBoundLongitude = 180.0;
        Double southBoundLatitude = -90.0;
        Double northBoundLatitude = 90.0;

        SearchHits<CSWRecord> searchHits = context.mock(SearchHits.class);
        context.checking(new Expectations() {{
            allowing(elasticsearchOperations).search(with(any(Query.class)), with(CSWRecord.class), with(any(IndexCoordinates.class)));
            will(returnValue(searchHits));
            allowing(searchHits).iterator();
            will(returnValue(Collections.emptyIterator()));
            allowing(searchHits).getTotalHits();
            will(returnValue(0L));
        }});

        CSWRecordSearchResponse response = elasticsearchService.searchCSWRecords(matchPhraseText, queryFields, page, pageSize, ogcServices, spatialRelation, westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude, null);
        assertNotNull(response);
    }
    
    @Test
    public void testCreateSuggestTermMethod() throws URISyntaxException {
        String prefix = "test";
        HttpPost method = elasticsearchService.createSuggestTermMethod(prefix);

        assertNotNull(method);
        assertEquals(method.getURI(), new URI("http://localhost:9200/suggestion-index/_search"));

        HttpEntity entity = method.getEntity();
        assertNotNull(entity);
        assertTrue(entity.getContentType().getValue().contains(ContentType.APPLICATION_JSON.getMimeType()));

        String expectedJson = "{\"suggest\":{\"term-suggester\":{\"prefix\":\"test\",\"completion\":{\"skip_duplicates\":true,\"field\":\"suggestionCompletion\",\"size\":10}}}}";
        assertInstanceOf(StringEntity.class, entity);
        
        String actualJson = "";
        try {
			actualJson = getStringEntityContent((StringEntity)entity);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
        
        assertTrue(expectedJson.contains(actualJson));
    }

    @Test
    public void testSuggestTerms() {
        String prefix = "test";
        
        try {
			context.checking(new Expectations() {{
			    oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpPost.class)));
			    will(returnValue("{\"suggest\":{\"term-suggester\":[{\"options\":[{\"text\":\"test1\"},{\"text\":\"test2\"}]}]}}"));
			}});
		} catch (IOException e) {
			e.printStackTrace();
		}

        List<String> result = elasticsearchService.suggestTerms(prefix);
        assertEquals(Arrays.asList("test2", "test1"), result);
    }
    
    /**
     * Convenience method to return the content of a StringEntity 
     * @param stringEntity the StringEntity
     * @return the content of stringEntity as a String
     * @throws Exception
     */
    public static String getStringEntityContent(StringEntity stringEntity) throws Exception {
        InputStream inputStream = stringEntity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        return content.toString();
    }
    
}