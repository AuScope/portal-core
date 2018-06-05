package org.auscope.portal.core.uifilter.optional.xpath;

import org.auscope.portal.core.uifilter.Predicate;

public class UITextBox extends AbstractXPathFilter {

    private static final String TYPE ="OPTIONAL.TEXT";

    public UITextBox(String label,String xpath,String value,Predicate predicate){
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
