package org.auscope.portal.core.uifilter;

public abstract class IFilterObject {

    private String xpath;
    private String value;
    private Predicate predicate;
    private String label;




    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean canHandle(String type){
        return type.equalsIgnoreCase(getType());
    }

    public abstract String getType();



}
