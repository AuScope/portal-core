package org.auscope.portal.core.uifilter.mandatory;

public class UICheckbox extends AbstractMandatoryParamBinding {

    private static final String TYPE ="MANDATORY.CHECKBOX";

    public UICheckbox(String label,String parameter, String value){
        this.setLabel(label);
        this.setParameter(parameter);
        this.setValue(value);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
