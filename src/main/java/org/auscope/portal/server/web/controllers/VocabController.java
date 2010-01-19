package org.auscope.portal.server.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.vocabs.VocabularyServiceResponseHandler;
import org.auscope.portal.vocabs.Concept;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import java.util.List;
import net.sf.json.JSONArray;

/**
 * User: Mathew Wyatt, Michael Stegherr
 * Date: 27/11/2009
 * Time: 11:55:10 AM
 */
@Controller
public class VocabController {

    private Logger logger = Logger.getLogger(getClass());
    private GetMethod method;
    private HttpServiceCaller httpServiceCaller;
    private VocabularyServiceResponseHandler vocabularyServiceResponseHandler;

    public static void main(String[] args) throws Exception {
        String rdfResponse = new HttpServiceCaller().getMethodResponseAsString(new GetMethod("http://auscope-services-test.arrc.csiro.au/vocab-service/query?repository=3DMM&label=*"), new HttpClient());

        List<Concept> concepts = new VocabularyServiceResponseHandler().getConcepts(rdfResponse);

        for(Concept concept : concepts)
            System.out.println(concept.getPreferredLabel());

    }

    /**
     * Construct
     * @param
     */
    @Autowired
    public VocabController(HttpServiceCaller httpServiceCaller,
                           VocabularyServiceResponseHandler vocabularyServiceResponseHandler,
                           PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer) {

        this.httpServiceCaller = httpServiceCaller;
        this.vocabularyServiceResponseHandler = vocabularyServiceResponseHandler;
        

        String vocabServiceUrl = portalPropertyPlaceholderConfigurer.resolvePlaceholder("HOST.vocabService.url");
        logger.debug("vocab service URL: " + vocabServiceUrl);

        this.method = new GetMethod(vocabServiceUrl);

        //set all of the parameters
        NameValuePair repo     = new NameValuePair("repository", "commodity_vocab");
        NameValuePair property = new NameValuePair("property1", "skos:inScheme");
        NameValuePair value    = new NameValuePair("property_value1", "<urn:cgi:classifierScheme:GA:commodity>");

        //attach them to the method
        this.method.setQueryString(new NameValuePair[]{repo, property, value});
    }

    /**
     * Get all GA commodity URNs with prefLabels
     * 
     * @param
     */
    @RequestMapping("/getCommodities.do")
    public ModelAndView getCommodities() throws Exception {

        logger.debug("vocab service query: " + this.method.getQueryString());

        //query the vocab service
        String vocabResponse = this.httpServiceCaller.getMethodResponseAsString(this.method, new HttpClient());

        //extract the concepts from the response
        List<Concept> concepts = this.vocabularyServiceResponseHandler.getConcepts(vocabResponse);

        //the main holder for the items
        JSONArray dataItems = new JSONArray();

        for(Concept concept : concepts) {

            JSONArray tableRow = new JSONArray();

            //URN
            tableRow.add(concept.getConceptUrn());

            //label
            tableRow.add(concept.getPreferredLabel());

            //add to the list
            dataItems.add(tableRow);
        }

        return new JSONModelAndView(dataItems);
    }
}
