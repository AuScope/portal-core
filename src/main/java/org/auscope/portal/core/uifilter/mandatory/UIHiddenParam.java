package org.auscope.portal.core.uifilter.mandatory;

/**
 * Class to deal with legacy stuff. e.g NVCL require a attribute call onlyHylogger for searching NVCL collection.
 * If the filter is dynamically generate, there is no way of doing something like this.
 * @author tey006
 *
 */
public class UIHiddenParam extends AbstractMandatoryParamBinding{

    private static final String TYPE ="MANDATORY.HIDDEN";

    public UIHiddenParam(String parameter, String value){
        this.setParameter(parameter);
        this.setValue(value);
    }


    @Override
    public String getType() {
        return TYPE;
    }


}
