package org.auscope.portal.server.domain.filter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents a partial implementation of the IFilter interface with protected
 * helper methods to aid in building some common filters
 *
 * TODO: This whole concept of String concatenation filters has been inherited from
 *       the original portal (it's not very correct and subject to errors).
 *       Use the geotools Library ogc:Filter module to generate the strings for us
 *
 * @author VOT002
 *
 */
public abstract class AbstractFilter implements IFilter {
    protected final Log log = LogFactory.getLog(getClass());

    protected enum MatchActionType {
        All,
        Any,
        One
    }

    /**
     * Utility method for converting a MatchActionType into a string that is defined by the filter specification
     * @param type
     * @return
     */
    protected String matchActionToString(MatchActionType type) {
        switch(type) {
        case All:
            return "All";
        case Any:
            return "Any";
        case One:
            return "One";
        default:
            throw new IllegalArgumentException();
        }
    }

    protected String generateGmlObjectIdFragment(String gmlId) {
        return String.format("<ogc:GmlObjectId gml:id=\"%1$s\"/>", gmlId);
    }

    /**
     * returns a ogc:Filter string fragment that can be embedded in
     * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
     *
     * The filter will return true if the specified propertyName geometry lies within the specified bounding box bounds
     * @param bboxSrs
     * @param lowerCornerPoints
     * @param upperCornerPoints
     * @param propertyXpath
     * @return
     */
   protected String generateBboxFragment(FilterBoundingBox bbox, String propertyXpath) {
       //Curse the lack of String.Join
       StringBuilder lowerCorner = new StringBuilder();
       StringBuilder upperCorner = new StringBuilder();

       //To deal with axis order issues http://geoserver.org/display/GEOSDOC/2.+WFS+-+Web+Feature+Service
       //Basically crs with urn:x-ogc:def:crs:EPSG:XXXX will need to lat/long, otherwise it will need to be long/lat
       if (bbox.getBboxSrs().startsWith("urn:x-ogc:def:crs:EPSG:")) {
           for (int i = bbox.getLowerCornerPoints().length - 1; i >= 0; i--) {
               lowerCorner.append(Double.toString(bbox.getLowerCornerPoints()[i]));
               lowerCorner.append(" ");
           }
           for (int i = bbox.getUpperCornerPoints().length - 1; i >= 0; i--) {
               upperCorner.append(Double.toString(bbox.getUpperCornerPoints()[i]));
               upperCorner.append(" ");
           }
       } else {
           for (double d : bbox.getLowerCornerPoints()) {
               lowerCorner.append(Double.toString(d));
               lowerCorner.append(" ");
           }
           for (double d : bbox.getUpperCornerPoints()) {
               upperCorner.append(Double.toString(d));
               upperCorner.append(" ");
           }
       }


       StringBuilder sb = new StringBuilder();

       /*sb.append("<ogc:Not>");
       sb.append("<ogc:Disjoint>");
       sb.append("<ogc:PropertyName>" + propertyXpath + "</ogc:PropertyName>");
       sb.append("<gml:Envelope srsName=\"" + bbox.getBboxSrs() + "\">");
       sb.append("<gml:lowerCorner>" + lowerCorner + "</gml:lowerCorner>");
       sb.append("<gml:upperCorner>" + upperCorner +"</gml:upperCorner>");
       sb.append("</gml:Envelope>");
       sb.append("</ogc:Disjoint>");
       sb.append("</ogc:Not>");*/

       sb.append("<ogc:BBOX>");
       if (propertyXpath != null && !propertyXpath.isEmpty()) {
           sb.append("<ogc:PropertyName>" + propertyXpath + "</ogc:PropertyName>");
       }
       sb.append("<gml:Envelope srsName=\"" + bbox.getBboxSrs() + "\">");
       sb.append("<gml:lowerCorner>" + lowerCorner + "</gml:lowerCorner>");
       sb.append("<gml:upperCorner>" + upperCorner +"</gml:upperCorner>");
       sb.append("</gml:Envelope>");
       sb.append("</ogc:BBOX>");


       return sb.toString();
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a string property against a literal (using * as wild, # for single and ! for escape).
    *
    * By default the comparison will be CASE INSENSITIVE
    *
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against (can include wildcards)
    * @return
    */
   protected String generatePropertyIsLikeFragment(String propertyName, String literal) {
       return generatePropertyIsLikeFragment(propertyName, literal, '*', '#', '!', false, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a string property against a literal (using * as wild, # for single and ! for escape).
    *
    * @param matchCase whether the comparison should made with a case sensitive match
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against (can include wildcards)
    * @return
    */
   protected String generatePropertyIsLikeFragment(String propertyName, String literal, boolean matchCase) {
       return generatePropertyIsLikeFragment(propertyName, literal, '*', '#', '!', matchCase, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a string property against a literal
    *
    * @param matchCase whether the comparison should made with a case sensitive match
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against (can include wildcards)
    * @param wildCard The wildcard character
    * @param singleChar The wildcard (single match) character
    * @param escapeChar The escape character for wildcard characters
    * @return
    */
   protected String generatePropertyIsLikeFragment(String propertyName, String literal, char wildCard, char singleChar, char escapeChar, Boolean matchCase, MatchActionType matchAction) {
       HashMap<String, String> attributes = new HashMap<String, String>();
       attributes.put("wildCard", Character.toString(wildCard));
       attributes.put("singleChar", Character.toString(singleChar));
       attributes.put("escapeChar", Character.toString(escapeChar));

       if (matchCase != null) {
           attributes.put("matchCase", Boolean.toString(matchCase));
       }

       if (matchAction != null) {
           attributes.put("matchAction", matchActionToString(matchAction));
       }

       return generatePropertyComparisonFragment("ogc:PropertyIsLike", attributes, propertyName, literal);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * The comparison will default to case insensitive matching
    *
    * Will compare whether a property equals literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsEqualToFragment(String propertyName, String literal) {
       return generatePropertyIsEqualToFragment(propertyName, literal, false, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare whether a property equals literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @param matchCase Whether the comparison should be case sensitive
    * @return
    */
   protected String generatePropertyIsEqualToFragment(String propertyName, String literal, Boolean matchCase) {
       return generatePropertyIsEqualToFragment(propertyName, literal, matchCase, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare whether a property equals literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @param matchCase Whether the comparison should be case sensitive
    * @param matchAction How to resolve propertyName matching multiple elements
    * @return
    */
   protected String generatePropertyIsEqualToFragment(String propertyName, String literal, Boolean matchCase, MatchActionType matchAction) {
       HashMap<String, String> attributes = new HashMap<String, String>();
       if (matchCase != null) {
           attributes.put("matchCase", Boolean.toString(matchCase));
       }

       if (matchAction != null) {
           attributes.put("matchAction", matchActionToString(matchAction));
       }

       return generatePropertyComparisonFragment("ogc:PropertyIsEqualTo", attributes, propertyName, literal);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will default to a 'case insensitive' match
    *
    * Will test if a property is greater than or equal to the literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsGreaterThanOrEqualTo(String propertyName, String literal) {
       return generatePropertyIsGreaterThanOrEqualTo(propertyName, literal, false, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will test if a property is greater than or equal to the literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsGreaterThanOrEqualTo(String propertyName, String literal, Boolean matchCase, MatchActionType matchAction) {
       HashMap<String, String> attributes = new HashMap<String, String>();
       if (matchCase != null) {
           attributes.put("matchCase", Boolean.toString(matchCase));
       }

       if (matchAction != null) {
           attributes.put("matchAction", matchActionToString(matchAction));
       }
       return generatePropertyComparisonFragment("ogc:PropertyIsGreaterThanOrEqualTo", attributes, propertyName, literal);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will default to a 'case insensitive' match
    *
    * Will compare a property against a literal to see if they mismatch
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsNotEqualTo(String propertyName, String literal) {
       return generatePropertyIsNotEqualTo(propertyName, literal, false, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a property against a literal to see if they mismatch
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsNotEqualTo(String propertyName, String literal, Boolean matchCase, MatchActionType matchAction) {
       HashMap<String, String> attributes = new HashMap<String, String>();
       if (matchCase != null) {
           attributes.put("matchCase", Boolean.toString(matchCase));
       }

       if (matchAction != null) {
           attributes.put("matchAction", matchActionToString(matchAction));
       }
       return generatePropertyComparisonFragment("ogc:PropertyIsNotEqualTo", attributes, propertyName, literal);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will default to a 'case insensitive' match
    *
    * Will compare a property to see if it is less than a literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsLessThan(String propertyName, String literal) {
       return generatePropertyIsLessThan(propertyName, literal, false, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a property to see if it is less than a literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsLessThan(String propertyName, String literal, Boolean matchCase, MatchActionType matchAction) {
       HashMap<String, String> attributes = new HashMap<String, String>();
       if (matchCase != null) {
           attributes.put("matchCase", Boolean.toString(matchCase));
       }

       if (matchAction != null) {
           attributes.put("matchAction", matchActionToString(matchAction));
       }
       return generatePropertyComparisonFragment("ogc:PropertyIsLessThan", attributes, propertyName, literal);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will default to a 'case insensitive' match
    *
    * Will compare a property to see if it is greater than a literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsGreaterThan(String propertyName, String literal) {
       return generatePropertyIsGreaterThan(propertyName, literal, false ,null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a property to see if it is greater than a literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsGreaterThan(String propertyName, String literal, Boolean matchCase, MatchActionType matchAction) {
       HashMap<String, String> attributes = new HashMap<String, String>();
       if (matchCase != null) {
           attributes.put("matchCase", Boolean.toString(matchCase));
       }

       if (matchAction != null) {
           attributes.put("matchAction", matchActionToString(matchAction));
       }
       return generatePropertyComparisonFragment("ogc:PropertyIsGreaterThan", attributes, propertyName, literal);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will default to a 'case insensitive' match
    *
    * Will compare a property to see if it is greater than or equal to a literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsLessThanOrEqualTo(String propertyName, String literal) {
       return generatePropertyIsLessThanOrEqualTo(propertyName, literal, false, null);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a property to see if it is greater than or equal to a literal
    * @param propertyName The XPath to the property to compare
    * @param literal The literal to compare against
    * @return
    */
   protected String generatePropertyIsLessThanOrEqualTo(String propertyName, String literal, Boolean matchCase, MatchActionType matchAction) {
       HashMap<String, String> attributes = new HashMap<String, String>();
       if (matchCase != null) {
           attributes.put("matchCase", Boolean.toString(matchCase));
       }

       if (matchAction != null) {
           attributes.put("matchAction", matchActionToString(matchAction));
       }
       return generatePropertyComparisonFragment("ogc:PropertyIsLessThanOrEqualTo", attributes, propertyName, literal);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a property to see if it is null
    * @param propertyName The XPath to the property to compare
    * @return
    */
   protected String generatePropertyIsNull(String propertyName) {
       return generatePropertyUnaryComparisonFragment("ogc:PropertyIsNull", propertyName);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a variable number of filters using a logical AND comparison
    * @param fragment1 a filter fragment
    * @param fragment2 a filter fragment
    * @return
    */
   protected String generateAndComparisonFragment(String... fragments) {
       return generateLogicalFragment("ogc:And",2, null, fragments);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will compare a variable number of filters using a logical OR comparison
    * @param fragment1 a filter fragment
    * @param fragment2 a filter fragment
    * @return
    */
   protected String generateOrComparisonFragment(String... fragments) {
       return generateLogicalFragment("ogc:Or",2, null, fragments);
   }

   /**
    * Generates an ogc:Filter string fragment that can be embedded in
    * <ogc:And> <ogc:Or> <ogc:Not> <ogc:Filter> parent elements.
    *
    * Will logical NOT the fragments result
    * @param fragment a filter fragment
    * @return
    */
   protected String generateNotComparisonFragment(String fragment) {
       return generateLogicalFragment("ogc:Not",1, null, fragment);
   }

   /**
    *
    * @param filterContents A single filter fragment or an And/Or/Not element
    * @return
    */
   protected String generateFilter(String filterContents, Map<String, String> attributes) {

       return generateLogicalFragment("ogc:Filter",1, attributes,filterContents);
   }

   /**
    *
    * @param filterContents A single filter fragment or an And/Or/Not element
    * @return
    */
   protected String generateFilter(String filterContents) {

       return generateLogicalFragment("ogc:Filter",1, null,filterContents);
   }

   private String generateLogicalFragment(String logicalComparison,int minParams, Map<String, String> attributes, String... fragments) {
       StringBuilder sb = new StringBuilder();

       int nonEmptyFragmentCount = 0;
       for (String fragment : fragments) {
           if (fragment != null && !fragment.isEmpty()) {
               nonEmptyFragmentCount++;
           }
       }

       if (nonEmptyFragmentCount >= minParams) {
           if (attributes == null)
               sb.append(String.format("<%1$s>", logicalComparison));
           else {
               sb.append(String.format("<%1$s ", logicalComparison));
               for (String attName : attributes.keySet()) {
                   sb.append(String.format("%1$s=\"%2$s\" ", attName, attributes.get(attName)));
               }
               sb.append(">");
           }
       }

       for (String fragment : fragments) {
           sb.append(fragment);
       }

       if (nonEmptyFragmentCount >= minParams)
           sb.append(String.format("</%1$s>", logicalComparison));
       return sb.toString();
   }

   private String generatePropertyUnaryComparisonFragment(String comparison, String propertyName) {
       return generatePropertyComparisonFragment(comparison, null, propertyName, null);
   }

   private String generatePropertyBinaryComparisonFragment(String comparison, String propertyName, String literal) {
       return generatePropertyComparisonFragment(comparison, null, propertyName, literal);
   }

   private String generatePropertyComparisonFragment(String comparison, Map<String, String> attributes, String propertyName, String literal) {
       StringBuilder sb = new StringBuilder();

       if (attributes == null)
           sb.append(String.format("<%1$s>", comparison));
       else {
           sb.append(String.format("<%1$s ", comparison));
           for (String attName : attributes.keySet()) {
               sb.append(String.format("%1$s=\"%2$s\" ", attName, attributes.get(attName)));
           }
           sb.append(">");
       }
       sb.append(String.format("<ogc:PropertyName>%1$s</ogc:PropertyName>", propertyName));
       if (literal != null)
           sb.append(String.format("<ogc:Literal>%1$s</ogc:Literal>", literal));
       sb.append(String.format("</%1$s>", comparison));

       return sb.toString();
   }
}
