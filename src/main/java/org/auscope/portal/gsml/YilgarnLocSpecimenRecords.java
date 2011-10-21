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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;



// TODO: Auto-generated Javadoc
/**
 * The Class YilgarnLocSpecimenRecords.
 */
public class YilgarnLocSpecimenRecords {

    /** The Constant logger. */
    private static final Log logger = LogFactory.getLog(YilgarnLocSpecimenRecords.class);

    /** The service name. */
    private String serviceName;

    /** The date. */
    private String date;

    /** The observed mineral name. */
    private String observedMineralName;

    /** The preparation details. */
    private String preparationDetails;

    /** The lab details. */
    private String labDetails;

    /** The analytical method. */
    private String analyticalMethod;

    /** The observed property. */
    private String observedProperty;

    /** The analyte name. */
    private String analyteName;

    /** The analyte value. */
    private String analyteValue;

    /** The uom. */
    private String uom;


    /** The Constant timeDateExpression. */
    private static final String timeDateExpression = "om:Observation/om:samplingTime/gml:TimeInstant/gml:timePosition";

    /** The Constant ObservationProcessExpression. */
    private static final String ObservationProcessExpression = "om:Observation/om:procedure/omx:ObservationProcess/@gml:id";

    /** The Constant obsProcessDesExpression. */
    private static final String obsProcessDesExpression = "om:Observation/om:procedure/omx:ObservationProcess/gml:description";

    /** The Constant obsProcessContExpression. */
    private static final String obsProcessContExpression = "om:Observation/om:procedure/omx:ObservationProcess/sml:contact/@xlink:title";

    /** The Constant obsProcessMethodExpression. */
    private static final String obsProcessMethodExpression = "om:Observation/om:procedure/omx:ObservationProcess/omx:method";

    /** The Constant observedPropertyExpression. */
    private static final String observedPropertyExpression = "om:Observation/om:observedProperty/@xlink:href";

    /** The Constant quantityNameExpression. */
    private static final String quantityNameExpression = "om:Observation/om:result/swe:Quantity/gml:name";

    /** The Constant quantityValueExpression. */
    private static final String quantityValueExpression = "om:Observation/om:result/swe:Quantity/swe:value";

    /** The Constant uomExpression. */
    private static final String uomExpression = "om:Observation/om:result/swe:Quantity/swe:uom/@xlink:href";

    /** The Constant omIdExpression. */
    private static final String omIdExpression = "om:Observation/@gml:id";



    /**
     * Instantiates a new yilgarn loc specimen records.
     *
     * @param node the node
     * @param xPath the x path
     * @throws XPathExpressionException the x path expression exception
     */
    public YilgarnLocSpecimenRecords(Node node, XPath xPath) throws XPathExpressionException {
        Node tempNode = null;
        xPath.setNamespaceContext(new YilgarnNamespaceContext());


        tempNode = (Node) xPath.evaluate(omIdExpression, node, XPathConstants.NODE);
        serviceName = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(timeDateExpression, node, XPathConstants.NODE);
        String dateAndTime = tempNode != null ? tempNode.getTextContent() : "";

        String[] dateTime = dateAndTime.split(" ");
        date = dateTime[0];

        tempNode = (Node) xPath.evaluate(ObservationProcessExpression, node, XPathConstants.NODE);
        observedMineralName = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(obsProcessDesExpression, node, XPathConstants.NODE);
        preparationDetails = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(obsProcessContExpression, node, XPathConstants.NODE);
        labDetails = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(obsProcessMethodExpression, node, XPathConstants.NODE);
        analyticalMethod = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(observedPropertyExpression, node, XPathConstants.NODE);
        observedProperty = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(quantityNameExpression, node, XPathConstants.NODE);
        analyteName = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(quantityValueExpression, node, XPathConstants.NODE);
        analyteValue = tempNode != null ? tempNode.getTextContent() : "";

        tempNode = (Node) xPath.evaluate(uomExpression, node, XPathConstants.NODE);
        String urnUOM = tempNode != null ? tempNode.getTextContent().trim() : "";

        if (urnUOM.contains("ppm")) {
            uom = "ppm";
        } else if (urnUOM.contains("ppb")) {
            uom = "ppb";
        } else if (urnUOM.contains("%")) {
            uom = "%";
        } else {
            uom = "null";
        }
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the observed mineral name.
     *
     * @return the observed mineral name
     */
    public String getObservedMineralName() {
        return observedMineralName;
    }

    /**
     * Gets the preparation details.
     *
     * @return the preparation details
     */
    public String getPreparationDetails() {
        return preparationDetails;
    }

    /**
     * Gets the lab details.
     *
     * @return the lab details
     */
    public String getLabDetails() {
        return labDetails;
    }

    /**
     * Gets the analytical method.
     *
     * @return the analytical method
     */
    public String getAnalyticalMethod() {
        return analyticalMethod;
    }

    /**
     * Gets the observed property.
     *
     * @return the observed property
     */
    public String getObservedProperty() {
        return observedProperty;
    }

    /**
     * Gets the analyte name.
     *
     * @return the analyte name
     */
    public String getAnalyteName() {
        return analyteName;
    }

    /**
     * Gets the analyte value.
     *
     * @return the analyte value
     */
    public String getAnalyteValue() {
        return analyteValue;
    }

    /**
     * Gets the uom.
     *
     * @return the uom
     */
    public String getUom() {
        return uom;
    }

    /**
     * Parses the records.
     *
     * @param gmlResponse the gml response
     * @return the yilgarn loc specimen records[]
     * @throws Exception the exception
     */
    public static YilgarnLocSpecimenRecords[] parseRecords(String gmlResponse)throws Exception{
        YilgarnLocSpecimenRecords[] records = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(gmlResponse));
        Document doc = builder.parse(inputSource);

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new YilgarnNamespaceContext());

        NodeList exprResult = (NodeList) xPath.evaluate("/wfs:FeatureCollection/gml:featureMembers/sa:LocatedSpecimen/sa:relatedObservation | /wfs:FeatureCollection/gml:featureMember/sa:LocatedSpecimen/sa:relatedObservation", doc, XPathConstants.NODESET);

        records = new YilgarnLocSpecimenRecords[exprResult.getLength()];
        for (int i = 0; i < exprResult.getLength(); i++) {
            records[i] = new YilgarnLocSpecimenRecords(exprResult.item(i), xPath);
        }
        return records;
    }

    /**
     * Yilgarn loc spec material desc.
     *
     * @param gmlResponse the gml response
     * @return the string
     * @throws Exception the exception
     */
    public static String yilgarnLocSpecMaterialDesc(String gmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(gmlResponse));
        Document doc = builder.parse(inputSource);

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new YilgarnNamespaceContext());

        Node materialDescriptionNode = (Node) xPath.evaluate("/wfs:FeatureCollection/gml:featureMembers/sa:LocatedSpecimen/sa:materialClass", doc, XPathConstants.NODE);
        String materialDescription = materialDescriptionNode != null ? materialDescriptionNode.getTextContent() : "";
        return materialDescription;
    }
}
