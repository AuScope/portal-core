package org.auscope.portal.server.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p> This class merges xml files </p>
 * @author jsanders
 */
public class XmlMerge {
   private static final Log log = LogFactory.getLog(XmlMerge.class);
   
   /**
    * Utility method to merge xml files.
    * <p>
    * CAVEAT:
    *    It assumes that both files have the same structure of the top two elements 
    *    and copies all children of the root element from one file to the other.
    * <p>
    * <b> Example </b>
    * 
    * <p>
    * Consider the two following two xml files:
    * <pre>
    *    a
    *       b
    *    a
    * </pre>
    * and file to be merged with:
    * <pre>
    *    a
    *       b1
    *    a
    * </pre>
    * The merged output will be:
    * <pre>
    *    a
    *       b
    *       b1
    *    a
    * </pre> 
    *  
    * @param is1  Merged file
    * @param is2  File to be merged with
    * @return To Do
    * @throws ParserConfigurationException
    * Thrown if ...
    * @throws SAXException
    * Thrown if ...
    * @throws IOException
    * Thrown if ...
    * 
    */
   public static String merge(InputStream is1, InputStream is2) 
         throws ParserConfigurationException, SAXException, IOException {
      
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      //log.debug("Using DocumentBuilderFactory " + dfactory.getClass());
      
      dfactory.setNamespaceAware(true);
      
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      //log.debug("Using DocumentBuilder " + docBuilder.getClass());
            
      Document doc = docBuilder.parse(is1);
      Document docImport = docBuilder.parse(is2);
      //log.debug("doc: " + doc.getNodeName());
      //log.debug("docImport: " + docImport.getNodeName());

      // This is the root wfs:FeatureCollection element
      Element root = doc.getDocumentElement();
      Element rootImport = docImport.getDocumentElement();
      //log.debug("root: " + root.getNodeName());
      //log.debug("rootImport: " + rootImport.getNodeName());      
      
      Node child;
      NodeList children = root.getChildNodes();
      for (int i=0; i < children.getLength(); i++) {
         child = children.item(i);
         //log.debug(i + "child before: " + child.getNodeName());
      }
      
      //Get Node to move
      NodeList kids = rootImport.getElementsByTagName("gml:featureMember");
      //log.debug("kids: " + kids.getLength());
      Element oneToMove;
      for (int i=0; i < kids.getLength(); i++) {
         oneToMove = (Element)kids.item(i);
         Node newOneToMove = doc.importNode(oneToMove, true);
         root.appendChild(newOneToMove);
      }

      children = root.getChildNodes();
      for (int i=0; i < children.getLength(); i++) {
         child = children.item(i);
         //log.debug(i + "child: " + child.getNodeName());
      }
                  
      StringWriter sw = new StringWriter();
      StreamResult result  = null;
      try {
         // Now create updateCSWRecords TransformerFactory and use it to create updateCSWRecords Transformer
         // object to transform our DOM document into updateCSWRecords stream of XML text.
         // No arguments to newTransformer() means no XSLT stylesheet.
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer output = tf.newTransformer();
         
         // Result objects for the transformation
         result = new StreamResult (sw);   // to XML text
         
         // do the transformation
         output.transform( new DOMSource(doc), result);
         //log.debug(sw.toString());
      
      } catch (TransformerException e) {
         e.printStackTrace();
      } finally {
         if (sw != null)
            sw.close();
      }
      
      return sw.toString();
   }
}
