package org.auscope.portal.core.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SLDLoader {

    private static final String RULE_XPATH = "/StyledLayerDescriptor/NamedLayer/UserStyle/FeatureTypeStyle/Rule";


    public static String loadSLD(String filename, Map<String,String> valueMap, boolean preserveformat) throws IOException{
        InputStream inputStream = loadStreamFromClass(filename);


        String newLine = System.getProperty("line.separator");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line; boolean flag = false;
        while ((line = reader.readLine()) != null) {
            if(!preserveformat){
                result.append(line.trim());
            }else{
                result.append(flag? newLine: "").append(line);
                flag = true;
            }
        }

        String input = result.toString();

        if(valueMap != null){
            Set<String> keys = valueMap.keySet();
            for(String key:keys){
                input = input.replace("[" + key + "]", valueMap.get(key));
            }
        }

        return input;


    }

    public static String loadSLDWithFilter(String filename, String filterString, String prefix, String namespace) throws IOException, ParserConfigurationException, XPathExpressionException, TransformerException, SAXException {


        InputStream inputStream = loadStreamFromClass(filename);

        Document doc = DOMUtil.buildDomFromStream(inputStream, false);

        if (prefix != null && namespace != null) {
          doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+prefix, namespace);
        }

        try {

            DocumentBuilderFactory documentBuilderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();


            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document filter = builder.parse(new ByteArrayInputStream(filterString.getBytes()));

            Node filterNode = doc.importNode(filter.getDocumentElement(), true);

            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr(RULE_XPATH).evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                node.insertBefore(filterNode.cloneNode(true), node.getFirstChild());
            }
        } catch (SAXException sxe) {

        }


        doc.normalizeDocument();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString();

        BufferedReader reader = new BufferedReader(new StringReader(output));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
                result.append(line.trim());

        }

        return result.toString();
    }


    private static InputStream loadStreamFromClass(String filename) throws IOException {
        InputStream inputStream = null;
        try{
            inputStream = SLDLoader.class.getResourceAsStream(filename);

        }catch(Exception e){
            inputStream = null;
        }

        if(inputStream == null){
            inputStream = ResourceUtil.loadResourceAsStream(filename);
        }

        return inputStream;
    }

    public static String loadSLDWithFilter(String filename, String filter) throws ParserConfigurationException, TransformerException, SAXException, XPathExpressionException, IOException {
        return loadSLDWithFilter(filename, filter, null, null);
    }
}
