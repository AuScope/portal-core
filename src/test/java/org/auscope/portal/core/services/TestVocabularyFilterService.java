package org.auscope.portal.core.services;

import com.hp.hpl.jena.rdf.model.*;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class TestVocabularyFilterService extends PortalTestClass {

    private VocabularyCacheService mockCacheService;
    private Model mockModel1;
    private Model mockModel2;
    private String mockVocabularyCacheId = "vocabularyCacheId";

    private Property mockDefaultProperty;


    private VocabularyFilterService vocabularyFilterService;

    @Before
    public void setUp() throws Exception {


        Resource mockResource = ResourceFactory.createResource("http://www.example.org/vocab/resource");
        Resource mockNarrowResource1 = ResourceFactory.createResource("http://www.example.org/vocab/narrow");
        Resource mockNarrowResource2 = ResourceFactory.createResource("http://www.example.org/vocab/narrow2");
        Resource mockNarrowTransitiveResource1 = ResourceFactory.createResource("http://www.example.org/vocab/narrowTransitive1");
        Resource mockNarrowTransitiveResource2 = ResourceFactory.createResource("http://www.example.org/vocab/narrowTransitive2");
        Resource mockNarrowTransitiveResource3 = ResourceFactory.createResource("http://www.example.org/vocab/narrowTransitive3");
        Resource mockNarrowTransitiveResource4 = ResourceFactory.createResource("http://www.example.org/vocab/narrowTransitive4");


        mockDefaultProperty = ResourceFactory.createProperty("http://example.org/property");
        Property mockPrefLabelProperty = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
        Property mockNarrowerProperty =ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#narrower");
        Property mockNarrowerTransitiveProperty = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#narrowerTransitive");

        // Model 1 has narrow and narrowerTransitive relationships

        mockModel1 = ModelFactory.createDefaultModel();

        Statement mockLanguageProperty = mockModel1.createStatement(mockResource, mockDefaultProperty, "example", "en");
        Statement mockPrefLabelStatement = mockModel1.createStatement(mockResource, mockPrefLabelProperty, "prefLabel");

        Statement mockNarrowerStatement1 = mockModel1.createStatement(mockResource, mockNarrowerProperty, mockNarrowResource1);
        Statement mockNarrowerStatement2 = mockModel1.createStatement(mockResource, mockNarrowerProperty, mockNarrowResource2);
        Statement mockNarrowerTransitiveStatement1 = mockModel1.createStatement(mockResource, mockNarrowerTransitiveProperty, mockNarrowTransitiveResource1);
        Statement mockNarrowerTransitiveStatement2 = mockModel1.createStatement(mockResource, mockNarrowerTransitiveProperty, mockNarrowTransitiveResource2);
        Statement mockNarrowerTransitiveStatement3 = mockModel1.createStatement(mockResource, mockNarrowerTransitiveProperty, mockNarrowTransitiveResource3);
        Statement mockNarrowerTransitiveStatement4 = mockModel1.createStatement(mockResource, mockNarrowerTransitiveProperty, mockNarrowTransitiveResource4);

        Statement[] statements1 = {mockLanguageProperty, mockPrefLabelStatement, mockNarrowerStatement1, mockNarrowerStatement2,
                mockNarrowerTransitiveStatement1, mockNarrowerTransitiveStatement2, mockNarrowerTransitiveStatement3, mockNarrowerTransitiveStatement4};

        mockModel1.add(statements1);

        // Model 2 has only narrower relationships which are defined recursively.

        mockModel2 = ModelFactory.createDefaultModel();

        mockLanguageProperty = mockModel2.createStatement(mockResource, mockDefaultProperty, "example", "en");

        mockPrefLabelStatement = mockModel2.createStatement(mockResource, mockPrefLabelProperty, "prefLabel");
        mockNarrowerStatement1 = mockModel2.createStatement(mockResource, mockNarrowerProperty, mockNarrowResource1);
        mockNarrowerStatement2 = mockModel2.createStatement(mockResource, mockNarrowerProperty, mockNarrowResource2);
        Statement mockNarrowerRecursiveStatement1 = mockModel2.createStatement(mockNarrowResource1, mockNarrowerProperty, mockNarrowTransitiveResource1);
        Statement mockNarrowerRecursiveStatement2 = mockModel2.createStatement(mockNarrowResource1, mockNarrowerProperty, mockNarrowTransitiveResource2);
        Statement mockNarrowerRecursiveStatement3 = mockModel2.createStatement(mockNarrowResource2, mockNarrowerProperty, mockNarrowTransitiveResource3);
        Statement mockNarrowerRecursiveStatement4 = mockModel2.createStatement(mockNarrowResource2, mockNarrowerProperty, mockNarrowTransitiveResource4);

        Statement[] statements2 = {mockLanguageProperty, mockPrefLabelStatement, mockNarrowerStatement1, mockNarrowerStatement2,
                mockNarrowerRecursiveStatement1, mockNarrowerRecursiveStatement2, mockNarrowerRecursiveStatement3, mockNarrowerRecursiveStatement4};

        mockModel2.add(statements2);


        mockCacheService = context.mock(VocabularyCacheService.class);


        vocabularyFilterService = new VocabularyFilterService(mockCacheService);
    }

    @Test
    public void testGetFilteredVocabularyById() {

        Selector mockSelector = new SimpleSelector(null, mockDefaultProperty, "example", "en");

        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getVocabularyCacheById(mockVocabularyCacheId);
                will(returnValue(mockModel1));
            }
        });

        Map<String, String> values = vocabularyFilterService.getVocabularyById(mockVocabularyCacheId, mockSelector);

        Assert.assertEquals(1, values.size());
    }

    @Test
    public void testGetVocabularyById() {


        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getVocabularyCacheById(mockVocabularyCacheId);
                will(returnValue(mockModel1));
            }
        });

        Map<String, String> values = vocabularyFilterService.getVocabularyById(mockVocabularyCacheId);

        Assert.assertEquals(1, values.size());
    }

    @Test
    public void testGetAllNarrowerWithTransitive() {
        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getVocabularyCacheById(mockVocabularyCacheId);
                will(returnValue(mockModel1));
            }
        });

        Set<String> values = vocabularyFilterService.getAllNarrower(mockVocabularyCacheId, "http://www.example.org/vocab/resource");

        Assert.assertEquals(7, values.size());
    }

    @Test
    public void testGetAllNarrowerWithRecursive() {
        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getVocabularyCacheById(mockVocabularyCacheId);
                will(returnValue(mockModel1));
            }
        });

        Set<String> values = vocabularyFilterService.getAllNarrower(mockVocabularyCacheId, "http://www.example.org/vocab/resource");

        Assert.assertEquals(7, values.size());
    }

    @Test
    public void testGetNarrowerRecursive() {

        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getVocabularyCacheById(mockVocabularyCacheId);
                will(returnValue(mockModel2));
            }
        });

        Set<String> values = vocabularyFilterService.getNarrowRecursive(mockVocabularyCacheId, "http://www.example.org/vocab/resource");

        Assert.assertEquals(6, values.size());
    }

    @Test
    public void testGetNarrower() {
        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getVocabularyCacheById(mockVocabularyCacheId);
                will(returnValue(mockModel1));
            }
        });

        Set<String> values = vocabularyFilterService.getNarrower(mockVocabularyCacheId, "http://www.example.org/vocab/resource");

        Assert.assertEquals(2, values.size());
    }

    @Test
    public void testGetNarrowerTransitive() {
        context.checking(new Expectations() {
            {
                oneOf(mockCacheService).getVocabularyCacheById(mockVocabularyCacheId);
                will(returnValue(mockModel1));
            }
        });

        Set<String> values = vocabularyFilterService.getNarrowerTransitive(mockVocabularyCacheId, "http://www.example.org/vocab/resource");

        Assert.assertEquals(4, values.size());
    }
}