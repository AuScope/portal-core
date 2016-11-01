package org.auscope.portal.core.uifilter.mandatory;

import org.auscope.portal.core.uifilter.AbstractBaseFilter;

public abstract class AbstractMandatoryParamBinding extends AbstractBaseFilter {

    private String parameter;

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }



}
