package org.auscope.portal.core.uifilter;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.junit.Assert;
import org.junit.Test;

public class TestGenericFilter extends GenericFilter{



    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        throw new NotImplementedException();
    }

    @Test
    public void testFragementGeneration(){
        //VT: should ignore provider filter
        this.setxPathFilters("{\"label\":\"Name\",\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.TEXT\",\"value\":\"er\",\"xpath\":\"mt:name\"},{\"label\":\"Tenement Type\",\"options\":[],\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.DROPDOWNSELECTLIST\",\"value\":\"prospecting\",\"xpath\":\"mt:tenementType\"},{\"label\":\"Provider\",\"type\":\"OPTIONAL.PROVIDER\",\"value\":{\"auscope.dpi.nsw.gov.au\":true,\"geology.data.nt.gov.au\":true}}");
        List<String> result = generateParameterFragments();

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("<ogc:PropertyIsLike escapeChar=\"!\" singleChar=\"#\" matchCase=\"false\" wildCard=\"*\" ><ogc:PropertyName>mt:name</ogc:PropertyName><ogc:Literal>*er*</ogc:Literal></ogc:PropertyIsLike>", result.get(0));
        Assert.assertEquals("<ogc:PropertyIsLike escapeChar=\"!\" singleChar=\"#\" matchCase=\"false\" wildCard=\"*\" ><ogc:PropertyName>mt:tenementType</ogc:PropertyName><ogc:Literal>*prospecting*</ogc:Literal></ogc:PropertyIsLike>", result.get(1));
    }

    @Test
    public void testEmptyResult(){
        List<String> result = generateParameterFragments();
        Assert.assertEquals(0, result.size());
    }

}
