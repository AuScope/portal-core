package org.auscope.portal.server.web.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.auscope.portal.csw.record.AbstractCSWOnlineResource;
import org.auscope.portal.csw.record.CSWRecord;

public class CSWRecordsHostFilter implements CSWRecordsFilterVisitor {

    URL filterUrl;

    public CSWRecordsHostFilter(URL url){
        this.filterUrl=url;
    }

    public CSWRecordsHostFilter(String url){
        try{
            this.filterUrl=((url.equals("")|| url==null)?null:new URL(url));
        }catch(MalformedURLException mue){
            this.filterUrl=null;
        }
    }

    @Override
    public boolean visit(AbstractCSWOnlineResource resource) {
       if(filterUrl==null){
           return true;
       }else{
           return resource.getLinkage().getHost().equalsIgnoreCase(filterUrl.getHost());
       }
    }

}
