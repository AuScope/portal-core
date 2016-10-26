package org.auscope.portal.core.uifilter.xpathfilter;

import org.auscope.portal.core.uifilter.Predicate;

public class UITextBox extends AbstractXPathFilter {

    public final String TYPE ="TEXT";

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
