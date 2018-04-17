package org.auscope.portal.core.services;

import com.hp.hpl.jena.rdf.model.*;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker.Format;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker.View;

import org.auscope.portal.core.services.namespaces.VocabNamespaceContext;
import org.auscope.portal.core.services.vocabs.VocabularyServiceItem;
import org.auscope.portal.core.test.BasicThreadExecutor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestVocabularyCacheService extends PortalTestClass {

    private static final int CONCURRENT_THREADS_TO_RUN = 3;

    static final int VOCABULARY_COUNT_TOTAL = 461;
    static final int VOCABULARY_ERRORS_COUNT_TOTAL = 451;

    private BasicThreadExecutor threadExecutor;

    private VocabularyCacheService vocabularyCacheService;
    private VocabularyMethodMaker mockMethodMaker1 = context.mock(VocabularyMethodMaker.class,"mockMethodMaker1");
    private VocabularyMethodMaker mockMethodMaker2 = context.mock(VocabularyMethodMaker.class,"mockMethodMaker2");
    private VocabularyMethodMaker mockMethodMaker3 = context.mock(VocabularyMethodMaker.class,"mockMethodMaker3");

    private HttpRequestBase mockMethod1 = context.mock(HttpRequestBase.class,"method1");
    private HttpRequestBase mockMethod2 = context.mock(HttpRequestBase.class,"method2");

    private HttpRequestBase mockMethod3 = context.mock(HttpRequestBase.class,"method3");


    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private ArrayList<VocabularyServiceItem> serviceList;


    private static final String serviceUrlFormatString = "http://vocabservice.%1$s.url/";


    @Before
    public void setUp() throws Exception {

        this.threadExecutor = new BasicThreadExecutor();

        serviceList = new ArrayList<>(CONCURRENT_THREADS_TO_RUN);
        VocabularyService vocabularyService1 = new VocabularyService(mockServiceCaller,mockMethodMaker1,String.format(
                serviceUrlFormatString, 1));
        VocabularyService vocabularyService2 = new VocabularyService(mockServiceCaller,mockMethodMaker2,String.format(
                serviceUrlFormatString, 2));
        VocabularyService vocabularyService3 = new VocabularyService(mockServiceCaller,mockMethodMaker3,String.format(
                serviceUrlFormatString, 3));


        serviceList.add(new VocabularyServiceItem(String.format("id:%1$s", 1), String.format("title:%1$s", 1), vocabularyService1));
        serviceList.add(new VocabularyServiceItem(String.format("id:%1$s", 2), String.format("title:%1$s", 2), vocabularyService2));
        serviceList.add(new VocabularyServiceItem(String.format("id:%1$s", 3), String.format("title:%1$s", 3), vocabularyService3));


        this.vocabularyCacheService = new VocabularyCacheService(threadExecutor, serviceList);
    }

    @After
    public void tearDown() {
        this.threadExecutor = null;
        this.vocabularyCacheService = null;
    }

    @Test
    public void testMultiUpdate() throws IOException, URISyntaxException {
        final String commodityVocabulary= ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/commodityConcepts_MoreData.xml");
        final String noMorecommodityVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/commodityConcepts_NoMoreData.xml");
        final String noMoreMineStatusVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/mineStatusConcepts_NoMoreData.xml");
        final String timescaleVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/timescaleConcepts_MoreData.xml");
        final String noMoreTimescaleVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/timescaleConcepts_NoMoreData.xml");

        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");

        final int totalRequestsMade = CONCURRENT_THREADS_TO_RUN ;

        try (final HttpClientInputStream t1r1 = new HttpClientInputStream(new ByteArrayInputStream(commodityVocabulary.getBytes()), null);
             final HttpClientInputStream t1r2 = new HttpClientInputStream(new ByteArrayInputStream(noMorecommodityVocabulary.getBytes()), null);
             final HttpClientInputStream t2r1 = new HttpClientInputStream(new ByteArrayInputStream(noMoreMineStatusVocabulary.getBytes()), null);
             final HttpClientInputStream t3r1 = new HttpClientInputStream(new ByteArrayInputStream(timescaleVocabulary.getBytes()), null);
             final HttpClientInputStream t3r2 = new HttpClientInputStream(
                     new ByteArrayInputStream(noMoreTimescaleVocabulary.getBytes()), null)) {

            context.checking(new Expectations() {
                {

                    oneOf(mockMethodMaker1).getAllConcepts(serviceList.get(0).getVocabularyService().getServiceUrl(), Format.Rdf,View.description, 1000, 0);
                    inSequence(t1Sequence);
                    will(returnValue(mockMethod1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                    inSequence(t1Sequence);
                    will(returnValue(t1r1));

                    oneOf(mockMethod1).releaseConnection();
                    inSequence(t1Sequence);

                    oneOf(mockMethodMaker1).getAllConcepts(serviceList.get(0).getVocabularyService().getServiceUrl(), Format.Rdf,View.description, 1000, 1);
                    inSequence(t1Sequence);
                    will(returnValue(mockMethod1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                    inSequence(t1Sequence);
                    will(returnValue(t1r2));

                    oneOf(mockMethod1).releaseConnection();
                    inSequence(t1Sequence);

                    oneOf(mockMethodMaker2).getAllConcepts(serviceList.get(1).getVocabularyService().getServiceUrl(), Format.Rdf,View.description, 1000, 0);
                    inSequence(t2Sequence);
                    will(returnValue(mockMethod2));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                    inSequence(t2Sequence);
                    will(returnValue(t2r1));


                    oneOf(mockMethod2).releaseConnection();
                    inSequence(t2Sequence);

                    oneOf(mockMethodMaker3).getAllConcepts(serviceList.get(2).getVocabularyService().getServiceUrl(), Format.Rdf,View.description, 1000, 0);
                    inSequence(t3Sequence);
                    will(returnValue(mockMethod3));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod3);
                    inSequence(t3Sequence);
                    will(returnValue(t3r1));

                    oneOf(mockMethod3).releaseConnection();
                    inSequence(t3Sequence);

                    oneOf(mockMethodMaker3).getAllConcepts(serviceList.get(2).getVocabularyService().getServiceUrl(), Format.Rdf,View.description, 1000, 1);
                    inSequence(t3Sequence);
                    will(returnValue(mockMethod3));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod3);
                    inSequence(t3Sequence);
                    will(returnValue(t3r2));

                    oneOf(mockMethod3).releaseConnection();
                    inSequence(t3Sequence);

                }
            });

            Assert.assertTrue(this.vocabularyCacheService.updateCache());
            try {
                do {
                    Thread.sleep(300);
                } while (this.vocabularyCacheService.updateRunning);

            } catch (InterruptedException e) {
                Assert.fail("Test sleep interrupted. Test aborted.");
            }
            try {
                threadExecutor.getExecutorService().shutdown();
                threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                threadExecutor.getExecutorService().shutdownNow();
                Assert.fail("Exception whilst waiting for update to finish " + e.getMessage());
            }

            Map<String, Model> cache = this.vocabularyCacheService.getVocabularyCache();
            Selector selector = new SimpleSelector(null, ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"), (RDFNode) null);


            Assert.assertEquals(totalRequestsMade, cache.size());
            int numberOfTerms =0;
            for (Map.Entry<String, Model> entry: cache.entrySet() ) {
                StmtIterator statements = entry.getValue().listStatements(selector);
                numberOfTerms += statements.toList().size();
            }

            Assert.assertEquals(VOCABULARY_COUNT_TOTAL, numberOfTerms);
            Assert.assertFalse(this.vocabularyCacheService.updateRunning);
        }

    }

    @Test
    public void testMultiUpdateWithErrors() throws IOException, URISyntaxException {
        final String commodityVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/commodityConcepts_MoreData.xml");
        final String noMorecommodityVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/commodityConcepts_NoMoreData.xml");
        final String noMoreMineStatusVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/mineStatusConcepts_NoMoreData.xml");
        final String timescaleVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/timescaleConcepts_MoreData.xml");
        final String noMoreTimescaleVocabulary = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/vocabulary/timescaleConcepts_NoMoreData.xml");

        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");


        try (final HttpClientInputStream t1r1 = new HttpClientInputStream(new ByteArrayInputStream(commodityVocabulary.getBytes()), null);
             final HttpClientInputStream t1r2 = new HttpClientInputStream(new ByteArrayInputStream(noMorecommodityVocabulary.getBytes()), null);
             final HttpClientInputStream t3r1 = new HttpClientInputStream(new ByteArrayInputStream(timescaleVocabulary.getBytes()), null);
             final HttpClientInputStream t3r2 = new HttpClientInputStream(
                     new ByteArrayInputStream(noMoreTimescaleVocabulary.getBytes()), null)) {

            context.checking(new Expectations() {
                {

                    oneOf(mockMethodMaker1).getAllConcepts(serviceList.get(0).getVocabularyService().getServiceUrl(), Format.Rdf, View.description, 1000, 0);
                    inSequence(t1Sequence);
                    will(returnValue(mockMethod1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                    inSequence(t1Sequence);
                    will(returnValue(t1r1));

                    oneOf(mockMethod1).releaseConnection();
                    inSequence(t1Sequence);

                    oneOf(mockMethodMaker1).getAllConcepts(serviceList.get(0).getVocabularyService().getServiceUrl(), Format.Rdf, View.description, 1000, 1);
                    inSequence(t1Sequence);
                    will(returnValue(mockMethod1));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                    inSequence(t1Sequence);
                    will(returnValue(t1r2));

                    oneOf(mockMethod1).releaseConnection();
                    inSequence(t1Sequence);

                    oneOf(mockMethodMaker2).getAllConcepts(serviceList.get(1).getVocabularyService().getServiceUrl(), Format.Rdf, View.description, 1000, 0);
                    inSequence(t2Sequence);
                    will(returnValue(mockMethod2));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                    inSequence(t2Sequence);
                    will(throwException(new Exception()));


                    oneOf(mockMethod2).releaseConnection();
                    inSequence(t2Sequence);

                    oneOf(mockMethodMaker3).getAllConcepts(serviceList.get(2).getVocabularyService().getServiceUrl(), Format.Rdf, View.description, 1000, 0);
                    inSequence(t3Sequence);
                    will(returnValue(mockMethod3));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod3);
                    inSequence(t3Sequence);
                    will(returnValue(t3r1));

                    oneOf(mockMethod3).releaseConnection();
                    inSequence(t3Sequence);

                    oneOf(mockMethodMaker3).getAllConcepts(serviceList.get(2).getVocabularyService().getServiceUrl(), Format.Rdf, View.description, 1000, 1);
                    inSequence(t3Sequence);
                    will(returnValue(mockMethod3));
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod3);
                    inSequence(t3Sequence);
                    will(returnValue(t3r2));

                    oneOf(mockMethod3).releaseConnection();
                    inSequence(t3Sequence);

                }
            });

            Assert.assertTrue(this.vocabularyCacheService.updateCache());
            try {
                do {
                    Thread.sleep(300);
                } while (this.vocabularyCacheService.updateRunning);

            } catch (InterruptedException e) {
                Assert.fail("Test sleep interrupted. Test aborted.");
            }
            try {
                threadExecutor.getExecutorService().shutdown();
                threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                threadExecutor.getExecutorService().shutdownNow();
                Assert.fail("Exception whilst waiting for update to finish " + e.getMessage());
            }

            Map<String, Model> cache = this.vocabularyCacheService.getVocabularyCache();
            Selector selector = new SimpleSelector(null, ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"), (RDFNode) null);


            Assert.assertEquals(CONCURRENT_THREADS_TO_RUN - 1, cache.size());
            int numberOfTerms =0;
            for (Map.Entry<String, Model> entry: cache.entrySet() ) {
                StmtIterator statements = entry.getValue().listStatements(selector);
                numberOfTerms += statements.toList().size();
            }
            Assert.assertEquals(VOCABULARY_ERRORS_COUNT_TOTAL, numberOfTerms);
            Assert.assertFalse(this.vocabularyCacheService.updateRunning);
        }
    }
}