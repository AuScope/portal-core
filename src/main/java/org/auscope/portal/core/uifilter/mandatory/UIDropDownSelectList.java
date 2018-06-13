package org.auscope.portal.core.uifilter.mandatory;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class UIDropDownSelectList extends AbstractMandatoryParamBinding{

    private static final String TYPE ="MANDATORY.DROPDOWNSELECTLIST";
    private List<ImmutablePair<String,String>> options;

    public UIDropDownSelectList(String label,String parameter, String value, List<ImmutablePair<String,String>> options){
        this.setLabel(label);
        this.setParameter(parameter);
        this.setValue(value);
        this.setOptions(options);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<ImmutablePair<String,String>> getOptions() {
        return options;
    }

    public void setOptions(List<ImmutablePair<String,String>> options) {
        this.options = options;
    }


}
