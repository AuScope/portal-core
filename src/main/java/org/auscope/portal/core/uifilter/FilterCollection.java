package org.auscope.portal.core.uifilter;

import java.util.List;

import org.auscope.portal.core.uifilter.mandatory.AbstractMandatoryParamBinding;
import org.auscope.portal.core.uifilter.mandatory.UIHiddenParam;
import org.auscope.portal.core.uifilter.xpathfilter.AbstractXPathFilter;

public class FilterCollection {

    private List<AbstractXPathFilter> xpathFilters;
    private List<AbstractMandatoryParamBinding> hiddenParams;
    private List<AbstractMandatoryParamBinding> mandatoryFilters;



    /**
     * @return the mandatoryFilters
     */
    public List<AbstractMandatoryParamBinding> getMandatoryFilters() {
        return mandatoryFilters;
    }

    /**
     * @param mandatoryFilters the mandatoryFilters to set
     */
    public void setMandatoryFilters(List<AbstractMandatoryParamBinding> mandatoryFilters) {
        this.mandatoryFilters = mandatoryFilters;
    }


    /**
     * @return the xpathFilters
     */
    public List<AbstractXPathFilter> getXpathFilters() {
        return xpathFilters;
    }


    /**
     * @param xpathFilters the xpathFilters to set
     */
    public void setXpathFilters(List<AbstractXPathFilter> xpathFilters) {
        this.xpathFilters = xpathFilters;
    }

    /**
     * @return the hiddenParams
     */
    public List<AbstractMandatoryParamBinding> getHiddenParams() {
        return hiddenParams;
    }

    /**
     * @param hiddenParams the hiddenParams to set
     */
    public void setHiddenParams(List<AbstractMandatoryParamBinding> hiddenParams) {
        this.hiddenParams = hiddenParams;
    }









}
