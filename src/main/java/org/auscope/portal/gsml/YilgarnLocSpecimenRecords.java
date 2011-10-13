package org.auscope.portal.gsml;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.auscope.portal.gsml.YilgarnNamespaceContext;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;



public class YilgarnLocSpecimenRecords {
    private static final Log logger = LogFactory.getLog(YilgarnLocSpecimenRecords.class);
    String serviceName;
    String date;
    String observedMineralName;
    String preparationDetails;
    String labDetails;
    String analyticalMethod;
    String observedProperty;
    String analyteName;
    String analyteValue;
    String uom;


    public YilgarnLocSpecimenRecords(Node node, XPath xPath ) throws XPathExpressionException{
        Node tempNode = null;
        xPath.setNamespaceContext(new YilgarnNamespaceContext());


        tempNode = (Node)xPath.evaluate("om:Observation/@gml:id", node, XPathConstants.NODE);
        serviceName = tempNode != null ? tempNode.getTextContent() : "";

        String timeDateExpression = "om:Observation/om:samplingTime/gml:TimeInstant/gml:timePosition";
        tempNode = (Node)xPath.evaluate(timeDateExpression, node, XPathConstants.NODE);
        String dateAndTime = tempNode != null ? tempNode.getTextContent() : "";

        String[] dateTime = dateAndTime.split(" ");
        date = dateTime[0];

        String ObservationProcessExpression = "om:Observation/om:procedure/omx:ObservationProcess/@gml:id";
        tempNode = (Node)xPath.evaluate(ObservationProcessExpression, node, XPathConstants.NODE);
        observedMineralName = tempNode != null ? tempNode.getTextContent() : "";

        String obsProcessDesExpression = "om:Observation/om:procedure/omx:ObservationProcess/gml:description";
        tempNode = (Node)xPath.evaluate(obsProcessDesExpression, node, XPathConstants.NODE);
        preparationDetails = tempNode != null ? tempNode.getTextContent() : "";

        String obsProcessContExpression = "om:Observation/om:procedure/omx:ObservationProcess/sml:contact/@xlink:title";
        tempNode = (Node)xPath.evaluate(obsProcessContExpression, node, XPathConstants.NODE);
        labDetails = tempNode != null ? tempNode.getTextContent() : "";

        String obsProcessMethodExpression = "om:Observation/om:procedure/omx:ObservationProcess/omx:method";
        tempNode = (Node)xPath.evaluate(obsProcessMethodExpression, node, XPathConstants.NODE);
        analyticalMethod = tempNode != null ? tempNode.getTextContent() : "";

        String observedPropertyExpression = "om:Observation/om:observedProperty/@xlink:href";
        tempNode = (Node)xPath.evaluate(observedPropertyExpression, node, XPathConstants.NODE);
        observedProperty = tempNode != null ? tempNode.getTextContent() : "";

        String quantityNameExpression = "om:Observation/om:result/swe:Quantity/gml:name";
        tempNode = (Node)xPath.evaluate(quantityNameExpression, node, XPathConstants.NODE);
        analyteName = tempNode != null ? tempNode.getTextContent() : "";

        String quantityValueExpression = "om:Observation/om:result/swe:Quantity/swe:value";
        tempNode = (Node)xPath.evaluate(quantityValueExpression, node, XPathConstants.NODE);
        analyteValue = tempNode != null ? tempNode.getTextContent() : "";

        String uomExpression = "om:Observation/om:result/swe:Quantity/swe:uom/@xlink:href";
        tempNode = (Node)xPath.evaluate(uomExpression, node, XPathConstants.NODE);
        String urnUOM = tempNode != null ? tempNode.getTextContent() : "";
        urnUOM.trim();

        if(urnUOM.indexOf("ppm") != -1){
            uom = "ppm";
        }
        else if(urnUOM.indexOf("ppb") != -1){
            uom = "ppb";
        }
        else if(urnUOM.indexOf("%") != -1){
            uom = "%";
        }else
            uom = "null";


    }

    public String getServiceName()
    {
        return serviceName;
    }
    public String getDate()
    {
        return date;
    }
    public String getObservedMineralName()
    {
        return observedMineralName;
    }
    public String getPreparationDetails()
    {
        return preparationDetails;
    }
    public String getLabDetails()
    {
        return labDetails;
    }
    public String getAnalyticalMethod()
    {
        return analyticalMethod;
    }
    public String getObservedProperty()
    {
        return observedProperty;
    }
    public String getAnalyteName()
    {
        return analyteName;
    }
    public String getAnalyteValue()
    {
        return analyteValue;
    }
    public String getUom()
    {
        return uom;
    }

    public static YilgarnLocSpecimenRecords[] parseRecords(String gmlResponse)throws Exception{
        YilgarnLocSpecimenRecords[] records = null;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(gmlResponse));
            Document doc = builder.parse(inputSource);

            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new YilgarnNamespaceContext());
            NodeList exprResult = (NodeList)xPath.evaluate("/wfs:FeatureCollection/gml:featureMembers/sa:LocatedSpecimen/sa:relatedObservation | /wfs:FeatureCollection/gml:featureMember/sa:LocatedSpecimen/sa:relatedObservation", doc, XPathConstants.NODESET);

            records = new YilgarnLocSpecimenRecords[exprResult.getLength()];
            for (int i=0; i < exprResult.getLength(); i++) {

                records[i] = new YilgarnLocSpecimenRecords(exprResult.item(i), xPath);
            }

            return records;
    }

    public static String YilgarnLocSpecMaterialDesc(String gmlResponse) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(gmlResponse));
        Document doc = builder.parse(inputSource);

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new YilgarnNamespaceContext());

        Node materialDescriptionNode = (Node)xPath.evaluate("/wfs:FeatureCollection/gml:featureMembers/sa:LocatedSpecimen/sa:materialClass", doc, XPathConstants.NODE);
        String materialDescription = materialDescriptionNode != null ? materialDescriptionNode.getTextContent() : "";
        return materialDescription;
    }

}
