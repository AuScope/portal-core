package org.auscope.portal.core.uifilter;


public abstract class AbstractBaseFilter {


    private String value;

    private String label;

    private String toolTip;



    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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


    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }
}
