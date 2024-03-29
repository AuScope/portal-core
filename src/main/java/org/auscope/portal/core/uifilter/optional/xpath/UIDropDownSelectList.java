package org.auscope.portal.core.uifilter.optional.xpath;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.auscope.portal.core.uifilter.Predicate;


public class UIDropDownSelectList extends AbstractXPathFilter{

    private static final String TYPE ="OPTIONAL.DROPDOWNSELECTLIST";

    private List<ImmutablePair<String,String>> options;
    private boolean multiSelect;

    public UIDropDownSelectList(String label,String xpath, String value,Predicate predicate, List<ImmutablePair<String,String>> options,
                                boolean multiSelect) {
        this.setLabel(label);
        this.setValue(value);
        this.setOptions(options);
        this.setXpath(xpath);
        this.setPredicate(predicate);
        this.setMultiSelect(multiSelect);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * @return the options
     */
    public List<ImmutablePair<String,String>> getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(List<ImmutablePair<String,String>> options) {
        this.options = options;
    }

    /**
     * 
     * @return if this a single select or multi-select dropdown
     */
    public boolean getMultiSelect() {
        return multiSelect;
    }

    /**
     * @param multiSelect is this a single select or multi-select dropdown?
     */
    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }
}
