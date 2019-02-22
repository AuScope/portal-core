package org.auscope.portal.core.uifilter.optional.xpath;

import org.auscope.portal.core.uifilter.Predicate;

public class UIDropDownRemote extends AbstractXPathFilter{

    private static final String TYPE ="OPTIONAL.DROPDOWNREMOTE";

    private String url;

    public UIDropDownRemote(String label,String xpath,String value,Predicate predicate,String url){
        this.setLabel(label);
        this.setXpath(xpath);
        this.setValue(value);
        this.setPredicate(predicate);
        this.setUrl(url);
    }


    @Override
    public String getType() {
        return TYPE;
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


}
