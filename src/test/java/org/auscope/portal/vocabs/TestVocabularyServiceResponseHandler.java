package org.auscope.portal.vocabs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;


/**
 * User: Michael Stegherr
 * Date: 07/09/2009
 * Time: 5:07:29 AM
 */
public class TestVocabularyServiceResponseHandler {
    VocabularyServiceResponseHandler vocabularyServiceResponseHandler =
        new VocabularyServiceResponseHandler();

    @Test
    public void testGetConcepts() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
        File vocabularyServiceResponse =
            new File("src/test/resources/vocabularyServiceResponse.xml");
        BufferedReader reader = new BufferedReader( new FileReader(vocabularyServiceResponse) );
        StringBuffer vocabularyServiceResponseXML = new StringBuffer();

        String str;
        while ((str = reader.readLine()) != null) {
            vocabularyServiceResponseXML.append(str);
        }
        reader.close();

        Collection<Concept> concepts =
            vocabularyServiceResponseHandler.getConcepts(vocabularyServiceResponseXML.toString());

        Assert.assertEquals("There are 27 concepts", 27, concepts.size());
        Assert.assertEquals(
                "The seventh is labelled: Siltstone - concrete aggregate",
                "Siltstone - concrete aggregate",
                ((Concept)concepts.toArray()[6]).getPreferredLabel());
        Assert.assertEquals(
                "The twentieth is labelled: Gneiss - crusher dust",
                "Gneiss - crusher dust",
                ((Concept)concepts.toArray()[19]).getPreferredLabel());
    }
}
