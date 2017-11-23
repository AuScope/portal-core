package org.auscope.portal.core.uifilter.mandatory;

public class UITextBox extends AbstractMandatoryParamBinding {

    public final String TYPE ="MANDATORY.TEXTBOX";

    public UITextBox(String label,String parameter, String value){
        this.setLabel(label);
        this.setParameter(parameter);
        this.setValue(value);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
