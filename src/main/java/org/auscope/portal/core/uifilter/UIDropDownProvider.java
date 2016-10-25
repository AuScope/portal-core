package org.auscope.portal.core.uifilter;

public class UIDropDownProvider extends IFilterObject{

    public final String TYPE ="PROVIDER";//VT what about sissvoc dropdown, or provider drop down

    public UIDropDownProvider(String label,String xpath,String value,Predicate predicate){
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
