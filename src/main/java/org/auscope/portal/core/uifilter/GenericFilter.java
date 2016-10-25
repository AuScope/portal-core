package org.auscope.portal.core.uifilter;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.methodmakers.filter.AbstractFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.services.sqs.model.UnsupportedOperationException;


/**
 * @author Victor Tey
 *
 * @version $Id$
 */

public abstract class GenericFilter extends AbstractFilter {



    // -------------------------------------------------------------- Constants

    /** Log object for this class. */
    protected final Log logger = LogFactory.getLog(getClass());
    protected String selectedFilters = "";

    // ----------------------------------------------------------- Constructors

    public GenericFilter(String selectedFilters) {
        this.selectedFilters = selectedFilters;
    }

    public GenericFilter() {
        super();
    }

    private String parseDateType(JSONObject obj){
        DateTimeFormatter outFormatter = DateTimeFormat
                .forPattern("yyyy-MM-dd");
        DateTime date = outFormatter
                .parseDateTime(obj.getString("value").split("T")[0]);
        String stringDate = outFormatter.print(date) + " 00:00:00";
        if(Predicate.valueOf(obj.getString("predicate")) == (Predicate.BIGGER_THAN)){
            return this.generateDatePropertyIsGreaterThan(
                    obj.getString("xpath"),false,
                    this.generateFunctionDateParse(stringDate));
        }else if(Predicate.valueOf(obj.getString("predicate")) == (Predicate.SMALLER_THAN)){
            return this.generateDatePropertyIsLessThan(
                    obj.getString("xpath"),false,
                    this.generateFunctionDateParse(stringDate));
        }else throw new UnsupportedOperationException("Unable to parse date string fragment.");

    }

    private String parseTextType(JSONObject obj){
        if(Predicate.valueOf(obj.getString("predicate")) == (Predicate.ISLIKE)){
            return this.generatePropertyIsLikeFragment(
                    obj.getString("xpath"), obj.getString("value"));
        }else if(Predicate.valueOf(obj.getString("predicate")) == (Predicate.EQUAL)){
            return this.generatePropertyIsEqualToFragment(
                    obj.getString("xpath"), obj.getString("value"));
        }else throw new UnsupportedOperationException("Unable to parse text string fragment.");

    }

    public List<String> generateParameterFragments(){
        List<String> results=new ArrayList<String>();
        final String unInitializedSelectedFilterMessage = "SelectedFilters has not been properly initialized. Make sure you have initialized via the constructor.";

        if(this.selectedFilters==null){
            throw new IllegalStateException(unInitializedSelectedFilterMessage);
        }

        String[] frags = this.selectedFilters.split("(?<=\\}),");

        if(frags.length==0){
            throw new IllegalStateException(unInitializedSelectedFilterMessage);
        }

        for(String frag:frags){
            JSONObject obj = JSONObject.fromObject(frag);
            if(obj.getString("type").equals("DATE")){
                results.add(parseDateType(obj));
            }else if(obj.getString("type").equals("TEXT")){
                results.add(parseTextType(obj));
            }
        }
        return results;
    }


    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateFilterFragment());
    }

    @Override
    public abstract String getFilterStringBoundingBox(FilterBoundingBox bbox);



    protected String generateFilterFragment() {
        List<String> parameterFragments = generateParameterFragments();
        return this.generateAndComparisonFragment(this
                .generateAndComparisonFragment(parameterFragments
                        .toArray(new String[parameterFragments.size()])));
    }
}
