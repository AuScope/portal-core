package org.auscope.portal.core.uifilter;

/**
 * Class to deal with legacy stuff. e.g NVCL require a attribute call onlyHylogger for search NVCL collection.
 * If the filter is dynamically generate, there is no way of doing something like this.
 * @author tey006
 *
 */
public class FixedFilterObject {

    private String parameter;
    private String value;

    public FixedFilterObject(String parameter, String value){
        this.setParameter(parameter);
        this.setValue(value);
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
