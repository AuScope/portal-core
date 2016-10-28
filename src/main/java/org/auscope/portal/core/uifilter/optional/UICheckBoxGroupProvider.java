package org.auscope.portal.core.uifilter.optional;

import org.auscope.portal.core.uifilter.AbstractBaseFilter;



public class UICheckBoxGroupProvider extends AbstractBaseFilter{

    public final String TYPE ="OPTIONAL.PROVIDER";//VT what about sissvoc dropdown, or provider drop down

    public UICheckBoxGroupProvider(String label,String value){
        this.setLabel(label);
        this.setValue(value);
    }


    @Override
    public String getType() {
        return TYPE;
    }


}
