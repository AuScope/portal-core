package org.auscope.portal.core.uifilter;

public class UIDropDown extends IFilterObject{

    public final String TYPE ="DROPDOWN";//VT what about sissvoc dropdown, or provider drop down

    private SOURCE source;

    //VT: This can be an array or a simple String
    private Object sourceValue;

    //VT: The source for this data will dictate how this parameter will be handled.
    public enum SOURCE {
        VOCAB, LIST, PROVIDER
    }

    public UIDropDown(String label,String xpath,String value,Predicate predicate){
        this.setLabel(label);
        this.setXpath(xpath);
        this.setValue(value);
        this.setPredicate(predicate);
    }

    public SOURCE getSource() {
        return source;
    }

    public void setSource(SOURCE source) {
        this.source = source;
    }

    public Object getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(Object sourceValue) {
        this.sourceValue = sourceValue;
    }

    @Override
    public String getType() {
        return TYPE;
    }


}
