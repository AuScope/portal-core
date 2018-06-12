package org.auscope.portal.core.uifilter.optional.xpath;

import org.auscope.portal.core.uifilter.Predicate;


import net.sf.json.JSONObject;

public class UIDate extends AbstractXPathFilter{

    private static final String TYPE ="OPTIONAL.DATE";

    public UIDate(String label,String xpath,String value,Predicate predicate){
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
