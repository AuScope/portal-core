package org.auscope.portal.core.uifilter.mandatory;

/**
 * A fixed parameter binding and its value determined from a onlineresource property. e.g serviceUrl
 * @author tey006
 *
 */
public class UIHiddenResourceAttribute extends AbstractMandatoryParamBinding{

    private static final String TYPE ="MANDATORY.UIHiddenResourceAttribute";

    /**
     * The attribute of the resource
     */
    private String attribute;

    public UIHiddenResourceAttribute(String parameter, String attribute,String value){
        this.setParameter(parameter);
        this.setValue(value);
        this.setAttribute(attribute);
    }


    @Override
    public String getType() {
        return TYPE;
    }


    /**
     * @return the attribute
     */
    public String getAttribute() {
        return attribute;
    }


    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }





}
