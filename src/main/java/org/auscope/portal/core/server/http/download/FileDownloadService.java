package org.auscope.portal.core.server.http.download;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileDownloadService {

    HttpServiceCaller serviceCaller;

    @Autowired
    public FileDownloadService(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;

    }

    public DownloadResponse singleFileDownloadFromURL(String url) throws Exception{

        HttpGet method = new HttpGet(url);
        HttpResponse httpResponse = serviceCaller.getMethodResponseAsHttpResponse(method);

        DownloadResponse response = new DownloadResponse(url);
        response.setResponseStream(httpResponse.getEntity().getContent());


        Header contentType = httpResponse.getEntity().getContentType();

        //create a new entry in the zip file with a timestamped name
        String mime = null;
        if (contentType != null) {
            response.setContentType(contentType.getValue());
        }

        return response;


    }
}
