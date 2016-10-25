package org.auscope.portal.core.uifilter;

public class UIDropDownRemote extends IFilterObject{

    public final String TYPE ="REMOTE";

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
