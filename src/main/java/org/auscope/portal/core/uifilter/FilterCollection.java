package org.auscope.portal.core.uifilter;

import java.util.List;

import org.auscope.portal.core.uifilter.mandatory.AbstractMandatoryParamBinding;
import org.auscope.portal.core.uifilter.mandatory.UIHiddenParam;
import org.auscope.portal.core.uifilter.optional.xpath.AbstractXPathFilter;

public class FilterCollection {

    private List<AbstractBaseFilter> optionalFilters;
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

    /**
     * @return the optionalFilters
     */
    public List<AbstractBaseFilter> getOptionalFilters() {
        return optionalFilters;
    }

    /**
     * @param optionalFilters the optionalFilters to set
     */
    public void setOptionalFilters(List<AbstractBaseFilter> optionalFilters) {
        this.optionalFilters = optionalFilters;
    }









}
