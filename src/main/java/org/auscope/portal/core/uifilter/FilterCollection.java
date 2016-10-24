package org.auscope.portal.core.uifilter;

import java.util.List;

public class FilterCollection {

    private List<IFilterObject> collection;

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

}
