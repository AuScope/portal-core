package org.auscope.portal.core.uifilter.mandatory;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class UIDropDownSelectList extends AbstractMandatoryParamBinding{

    public final String TYPE ="DROPDOWNSELECTLIST";
    private ImmutablePair<String,String> options;

    public UIDropDownSelectList(String parameter, String value, ImmutablePair<String,String> options){
        this.setParameter(parameter);
        this.setValue(value);
        this.setOptions(options);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * @return the options
     */
    public ImmutablePair<String,String> getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(ImmutablePair<String,String> options) {
        this.options = options;
    }
}
