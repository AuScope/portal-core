package org.auscope.portal.core.uifilter;

public class UITextBox extends IFilterObject {

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
