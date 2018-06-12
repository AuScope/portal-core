package org.auscope.portal.core.uifilter.optional.xpath;

import org.auscope.portal.core.uifilter.Predicate;

public class UIPolygonBBox extends AbstractXPathFilter {

    private static final String TYPE ="OPTIONAL.POLYGONBBOX";

    public UIPolygonBBox(String label,String xpath,String value,Predicate predicate){
        this.setLabel(label);
        this.setXpath(xpath);
        this.setValue(value);
        this.setPredicate(predicate);
    }

    @Override
    public String getType() {
        return TYPE;
    }




}
