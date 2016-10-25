package org.auscope.portal.core.uifilter;

import java.util.List;

public class FilterCollection {

    private List<IFilterObject> collection;

    private List<FixedFilterObject> fixedAttributes;

    public List<IFilterObject> getCollection() {
        return collection;
    }

    public void setCollection(List<IFilterObject> collection) {
        this.collection = collection;

    }

    public boolean isEmpty(){
        if(collection == null || collection.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    public List<FixedFilterObject> getFixedAttributes() {
        return fixedAttributes;
    }

    public void setFixedAttributes(List<FixedFilterObject> fixedAttributes) {
        this.fixedAttributes = fixedAttributes;
    }



}
