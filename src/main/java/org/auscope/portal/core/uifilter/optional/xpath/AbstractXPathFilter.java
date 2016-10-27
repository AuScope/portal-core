package org.auscope.portal.core.uifilter.optional.xpath;

import org.auscope.portal.core.uifilter.*;

public abstract class AbstractXPathFilter extends AbstractBaseFilter {

    private String xpath;

    private Predicate predicate;


    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }



    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }







}
