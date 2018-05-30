package org.auscope.portal.core.uifilter;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;

public class GenericFilterAdapter extends GenericFilter{
	
	 List<String> fragments;
	 String spatialXpath;
	
	 /**
     * Given a mine name, this object will build a filter to a wild card search for mine names
     *
     * @param mineName
     *            the main name
     */
    public GenericFilterAdapter(String optionalFilters, String spatialXpath) {
       super(optionalFilters);
       this.spatialXpath = spatialXpath;
       fragments = this.generateParameterFragments();
       
    }


    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {

        List<String> localFragment = new ArrayList<String>(fragments);
        localFragment.add(this.generateBboxFragment(bbox, this.spatialXpath));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment
                                                                                                       .size()])));
    }

}
