package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.pressuredb.AvailableOMResponse;
import org.auscope.portal.server.web.service.PressureDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class containing methods for supporting the Pressure DB layer and associated dataservice
 * @author Josh Vote
 *
 */
@Controller
public class PressureDBController extends BaseWFSToKMLController {

    protected final Log log = LogFactory.getLog(getClass());

    private PressureDBService pressureDBService;

    @Autowired
    public PressureDBController(PressureDBService pressureDBService) {
        this.pressureDBService = pressureDBService;
    }

    /**
     * Handles requests for the getAvailableOM method
     *
     * Will return a JSON encoded AvailableOMResponse
     * @param serviceUrl
     * @param wellID
     * @return
     */
    @RequestMapping("/pressuredb-getAvailableOM.do")
    public ModelAndView getAvailableOM(String serviceUrl, String wellID) {

        try {
           AvailableOMResponse response = pressureDBService.makeGetAvailableOMRequest(wellID, serviceUrl);

           return generateJSONResponseMAV(true, new AvailableOMResponse[] {response}, "");
        } catch (Exception e) {
            log.warn("Error making pressure-db service request", e);
            return generateJSONResponseMAV(false, null, "Failure communicating with Pressure DB data service");
        }
    }

    /**
     * Handles requests for the download method
     *
     * Will return a stream directly from the service
     * @param serviceUrl
     * @param wellID
     * @return
     * @throws IOException
     */
    @RequestMapping("/pressuredb-download.do")
    public void download(String serviceUrl, String wellID, String[] feature, HttpServletResponse response) throws IOException {

        //Make our request and get our inputstream
        InputStream inputStream = null;
        try {
           inputStream = pressureDBService.makeDownloadRequest(wellID, serviceUrl, feature);
        } catch (Exception e) {
            log.warn("Error making pressure-db service request", e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return;
        }

        //pipe our input into our output
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",String.format("inline; filename=PressureDB-%1$s.zip;", wellID));
        ServletOutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[1024 * 5];
        int numRead;
        while ( (numRead = inputStream.read(buffer)) >= 0) {
            outputStream.write(buffer, 0, numRead);
        }
        outputStream.flush();
        outputStream.close();
    }


}
