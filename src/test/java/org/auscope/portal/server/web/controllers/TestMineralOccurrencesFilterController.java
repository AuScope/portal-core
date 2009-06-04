package org.auscope.portal.server.web.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.xml.sax.SAXException;
import org.auscope.portal.server.web.HttpServiceCaller;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.web.mineraloccurrence.MineralOccurrenceServiceClient;
import org.auscope.portal.server.web.mineraloccurrence.Mine;
import org.auscope.portal.server.util.GmlToKml;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * User: Mathew Wyyatt
 * Date: 23/03/2009
 * Time: 12:50:56 PM
 */
public class TestMineralOccurrencesFilterController {
    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrencesFilterController minerOccurrenceFilterController;
    private MineralOccurrenceServiceClient mineralOccurrenceServiceClient;
    private HttpServletRequest mockHttpRequest;
    private HttpServletResponse mockHttpResponse;
    private GmlToKml mockGmlToKml;

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setup() {
        this.httpServiceCaller =  context.mock(HttpServiceCaller.class);
        this.mineralOccurrencesResponseHandler = context.mock(MineralOccurrencesResponseHandler.class);
        this.mineralOccurrenceServiceClient = context.mock(MineralOccurrenceServiceClient.class);
        this.mockGmlToKml = context.mock(GmlToKml.class);
        this.minerOccurrenceFilterController = new MineralOccurrencesFilterController(this.httpServiceCaller, this.mineralOccurrencesResponseHandler, this.mineralOccurrenceServiceClient, this.mockGmlToKml);
        this.mockHttpRequest = context.mock(HttpServletRequest.class);
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
    }

    /**
     * Test that the getAlLMines controller returns the correct JSON response
     * 
     * @throws Exception
     */
    @Test
    public void testGetMineNames() throws Exception {
        final String serviceURL = "http://localhost?";
        final String expectedJSONResponse = "{\"data\":[{\"mineDisplayName\":\"AllMines..\"},{\"mineDisplayName\":\"Balh1\"},{\"mineDisplayName\":\"Balh2\"}],\"success\":true}";
        final Mine mockMine1 = context.mock(Mine.class);
        final Mine mockMine2 = context.mock(Mine.class, "mockMine2");
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            //return a list of mock mines for the controller to build up json from
            oneOf (mineralOccurrenceServiceClient).getAllMines(serviceURL);will(returnValue(Arrays.asList(mockMine1, mockMine2)));

            //return the names which are in our expectedJSONResponse
            oneOf (mockMine1).getMineNamePreffered(); will(returnValue("Balh1"));
            oneOf (mockMine2).getMineNamePreffered(); will(returnValue("Balh2"));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
            //oneOf (mockPrintWriter).write(expectedJSONResponse);
        }});

        //call getMineNames with a dud service url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.getMineNames(serviceURL, new ModelMap());

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        Assert.assertEquals(expectedJSONResponse, actualJSONResponse.getBuffer().toString());
    }

    /**
     * Test the event of an exception being thrown when trying to get the mines
     */
    @Test
    public void testGetMinesError() throws Exception {
        final String serviceURL = "http://localhost?";
        final String expectedJSONResponse = "{\"msg\":\""+ ErrorMessages.OPERATION_FAILED +"\",\"success\":false}".replaceAll("\n", "").replaceAll("\\s+", "");
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            //get the call the throw an exception
            oneOf (mineralOccurrenceServiceClient).getAllMines(serviceURL);will(throwException(new Exception()));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call getMineNames with a dud service url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.getMineNames(serviceURL, new ModelMap());

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        Assert.assertEquals(expectedJSONResponse, actualJSONResponse.getBuffer().toString());
    }

    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterAllMines() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "All Mines.."; //to get all mines
        final String expectedKML = "<kml/>";
        final String expectedJSONResponse = "{\"data\":{\"kml\":\""+expectedKML+"\"},\"success\":true}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceServiceClient).getAllMinesGML(serviceURL);
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(HttpServletRequest.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with a dud url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMineFilter(serviceURL, mineName,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        Assert.assertEquals(expectedJSONResponse, actualJSONResponse.getBuffer().toString());
    }

    /**
     * Test a mine query with a specified name
     */
    @Test
    public void testDoMineFilterSpecifiedName() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "SomeName"; //random name
        final String expectedKML = "<kml/>";
        final String expectedJSONResponse = "{\"data\":{\"kml\":\""+expectedKML+"\"},\"success\":true}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceServiceClient).getMineWithSpecifiedNameGML(serviceURL, mineName);
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(HttpServletRequest.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with a dud url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMineFilter(serviceURL, mineName,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        Assert.assertEquals(expectedJSONResponse, actualJSONResponse.getBuffer().toString());
    }

    /**
     * Test doing a minefilter, and there being an error
     */
    @Test
    public void testDoMineFilterError() {

    }

    @Test
    public void testGetAllForMine() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        MineralOccurrencesFilterController minOccController = new MineralOccurrencesFilterController();
        minOccController.doMiningActivityFilter("http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services?", "Good Hope", "18/Mar/2009", "26/Mar/2009", "", "", "", "", null);

    }
    @Test 
    public void testConvertToKML() {
       MineralOccurrencesFilterController minOccController = new MineralOccurrencesFilterController();
       
       // TODO write test case
    /*   
       //C:\Projects\AuScope-Portal
       String basePath = new File("").getAbsolutePath();
       //C:\Projects\AuScope-Portal\src\main\webapp\WEB-INF\xsl\ma.xml    mine.xml
       //String testRunner = "file:///" + basePath + "/src/test/js/testRunner.html";
       //String testSuite  = new File(new URI("file:///" + basePath.replace("\\", "/") + "/src/main/webapp/WEB-INF/xsl/ma.xml")).getAbsolutePath();
       //String testSuite1  = new File(new URI("file:///" + basePath.replace("\\", "/") + "/src/main/webapp/WEB-INF/xsl/mine.xml")).getAbsolutePath();
       String mineFilePath = basePath + "/src/main/webapp/WEB-INF/xsl/mine.xml";
       String maFilePath = basePath + "/src/main/webapp/WEB-INF/xsl/ma.xml";
       String s = new String();
       
       try {
          BufferedReader inMine = new BufferedReader (new FileReader(mineFilePath));
          BufferedReader inMA   = new BufferedReader (new FileReader(maFilePath));
          try {
             while ((s = inMine.readLine()) != null )
                System.out.println(s);

                System.out.println("---------------------");
                
             while ((s = inMA.readLine()) != null )
                System.out.println(s);
             
                System.out.println("========---------------------");                
          } catch(IOException iox) {
             System.out.println("File read error...");
             iox.printStackTrace();
          }
       
          System.out.println("....Calling ...minOccController.convertToKML");
          minOccController.convertToKML(new FileInputStream(mineFilePath), 
                                        new FileInputStream(maFilePath),
                                        null);
          
       } catch (FileNotFoundException e) {
          System.out.println("File not found...");
       }
    */   
    }

    public static void main(String[] args) {
        System.out.println(new String("09/Mar/9008").toUpperCase());
    }
}
