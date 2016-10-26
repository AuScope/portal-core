package org.auscope.portal.core.uifilter.mandatory;

public class UIDropDownSelectList extends AbstractMandatoryParamBinding{

    public final String TYPE ="DROPDOWNSELECTLIST";

    public UIDropDownSelectList(String parameter, String value){
        this.setParameter(parameter);
        this.setValue(value);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
