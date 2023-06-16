package org.auscope.portal.core.server.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.List;
import java.util.stream.Stream;
import java.net.URL;
import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.OutputStream;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.OperationNotSupportedException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.io.Files;


import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.server.http.download.DownloadResponse;
import org.auscope.portal.core.server.http.download.DownloadTracker;
import org.auscope.portal.core.server.http.download.Progression;
import org.auscope.portal.core.server.http.download.ServiceDownloadManager;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.util.MimeUtil;
import org.auscope.portal.core.services.PortalServiceException;

/**
 * User: Mathew Wyatt Date: 02/09/2009 Time: 12:33:48 PM
 */

@Controller
public class DownloadController extends BasePortalController {
	
    private final Log logger = LogFactory.getLog(getClass());
    // Minimum number of lines we expect a download to be (header file plus at least one data row)
    private final static Integer MINIMUM_NUMBER_OF_LINES = 2;
    private HttpServiceCaller serviceCaller;
    private ServiceConfiguration serviceConfiguration;

    @Value("${access.whitelist}")
    private String whitelist;

    @Autowired
    public DownloadController(HttpServiceCaller serviceCaller, ServiceConfiguration serviceConfiguration) {
        this.serviceCaller = serviceCaller;
        this.serviceConfiguration = serviceConfiguration;
    }

    @RequestMapping("/getGmlDownload.do")
    public void getGmlDownload(
            @RequestParam("email") final String email,
            HttpServletResponse response) throws Exception {
        DownloadTracker downloadTracker = DownloadTracker.getTracker(email);
        Progression progress = downloadTracker.getProgress();
        if (progress == Progression.COMPLETED) {
            response.setContentType("application/zip");

            boolean csvSign = false;
            ZipFile downloadZip = null;
            try {
            	downloadZip = new ZipFile(downloadTracker.getFileHandle().getAbsolutePath());
            	Enumeration<? extends ZipEntry> zipEntries = downloadZip.entries();
                while (zipEntries.hasMoreElements()) {
                    String fileName = ((ZipEntry) zipEntries.nextElement()).getName();
                    if (fileName.contains("csv"))
                    {
                        csvSign = true;
                        break;
                    }
                }
            } finally {
                if (downloadZip != null) {
                    downloadZip.close();
                }
            }
            if (csvSign == false)
                response.setHeader("Content-Disposition",
                    "inline; filename=GMLDownload.zip;");
            else
                response.setHeader("Content-Disposition",
                    "inline; filename=CSVDownload.zip;");
            FileIOUtil.writeInputToOutputStream(downloadTracker.getFile(), response.getOutputStream(), 1024, true);
        }

    }

    @RequestMapping("/checkGMLDownloadStatus.do")
    public void checkGMLDownloadStatus(
            @RequestParam("email") final String email,
            HttpServletResponse response,
            HttpServletRequest request) throws Exception {

        DownloadTracker downloadTracker = DownloadTracker.getTracker(email);
        Progression progress = downloadTracker.getProgress();
        String htmlResponse = "";
        response.setContentType("text/html");

        if (progress == Progression.INPROGRESS) {
            htmlResponse = "<html><p>Download currently still in progress</p></html>";
        } else if (progress == Progression.NOT_STARTED) {
            htmlResponse = "<html><p>No download request found..</p></html>";
        } else if (progress == Progression.COMPLETED) {
            htmlResponse = "<html><p>Your download has successfully completed.</p><p><a href='getGmlDownload.do?email="
                    + email + "'>Click on this link to download</a></p></html>";
        } else {
            htmlResponse = "<html><p>A serious error has occurred, please contact our Administrator on cg-admin@csiro.au</p></html>";
        }

        response.getOutputStream().write(htmlResponse.getBytes());
    }

    /**
     * Given a list of URls, this function will collate the responses into a zip file and send the response back to the browser. if no email is provided, a zip
     * is written to the response output If email address is provided, a html response is returned to the user informing his request has been processed and to
     * check back again later.
     *
     * @param serviceUrls
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadGMLAsZip.do")
    public void downloadGMLAsZip(
            @RequestParam("serviceUrls") final String[] serviceUrls,
            @RequestParam(required = false, value = "email", defaultValue = "") final String email,
            @RequestParam(required = false, value = "outputFormat", defaultValue = "") final String outputFormat,
            HttpServletResponse response) throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        downloadGMLAsZip(serviceUrls, response, pool, email, outputFormat);
    }

    public void downloadGMLAsZip(String[] serviceUrls, HttpServletResponse response, ExecutorService threadpool,
            String email, String outputFormat) throws Exception {
        logger.trace("No. of serviceUrls: " + serviceUrls.length);

        String extension = null;
        if (outputFormat != null) {
            String ext = MimeUtil.mimeToFileExtension(outputFormat);
            if (ext != null && !ext.isEmpty()) {
                extension = "." + ext;
            }
        }

        ServiceDownloadManager downloadManager = new ServiceDownloadManager(serviceUrls, serviceCaller, threadpool,
                this.serviceConfiguration, extension);

        if (email != null && email.length() > 0) {

            DownloadTracker downloadTracker = DownloadTracker.getTracker(email);
            Progression progress = downloadTracker.getProgress();
            String htmlResponse = "";
            response.setContentType("text/html");
            if (progress == Progression.INPROGRESS) {
                htmlResponse = "<html><p>You are not allowed to start a new download when another download is in progress Please wait for your previous download to complete.</p>"
                        +
                        " <p>To check the progress of your download, enter your email address on the download popup and click on 'Check Status'</p>"
                        +
                        " <p>Please contact the administrator if you encounter any issues</p></html>";
                response.getOutputStream().write(htmlResponse.getBytes());
                return;
            }

            downloadTracker.startTrack(downloadManager, extension);

            htmlResponse = "<html><p>Your request has been submitted. The download process may take sometime depending on the size of the dataset</p>"
                    +
                    " <p>To check the progress of your download, enter your email address on the download popup and click on 'Check Status'</p>"
                    +
                    " <p>Please contact the administrator if you encounter any issues</p></html>";

            response.getOutputStream().write(htmlResponse.getBytes());

        } else if (outputFormat != null && outputFormat.equals("csv")) {
            // set the content type for zip files
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "inline; filename=CSVDownload.zip;");
            ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
            //VT: threadpool is closed within downloadAll();
            ArrayList<DownloadResponse> gmlDownloads = downloadManager.downloadAll();
            FileIOUtil.writeResponseToZip(gmlDownloads, zout,outputFormat, MINIMUM_NUMBER_OF_LINES);
            zout.finish();
            zout.flush();
            zout.close();
        } else {
        	// set the content type for zip files
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "inline; filename=GMLDownload.zip;");
            ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
            //VT: threadpool is closed within downloadAll();
            ArrayList<DownloadResponse> gmlDownloads = downloadManager.downloadAll();
            FileIOUtil.writeResponseToZip(gmlDownloads, zout, MINIMUM_NUMBER_OF_LINES);
            zout.finish();
            zout.flush();
            zout.close();
        }

    }

    /**
     * Searches for a filename in the download URL
     * 
     * @param uri
     * @return an empty string if not found else return the filename without the extension
     */
    private String getFileName(UriComponents uri) {
        String path = uri.getPath();
        Pattern pattern = Pattern.compile("/([^/ ]+)\\.\\w{3}$");
        Matcher matcher = pattern.matcher(path);
        boolean matchFound = matcher.find();
        if  (matchFound) {
            return matcher.group(1);
        } else {
           return "";
        }
    }

    /**
     * Given a list of WMS URL's, this function will collate the responses into a zip file and send the response back to the browser.
     *
     * @param serviceUrls URLs which will be called upon to create the collation
     * @param filename suggested filename for the zip file
     * @param response response parameter, used to set up the response
     * @throws Exception
     */
    @RequestMapping("/downloadDataAsZip.do")
    public void downloadDataAsZip(@RequestParam("serviceUrls") final String[] serviceUrls,
            @RequestParam("filename") final String filename,
            HttpServletResponse response) throws Exception {

        String filenameStr = filename == null || filename.length() < 0 ? "DataDownload" : filename;
        String ext = Files.getFileExtension(filename);
        if (ext.length() < 1) {
            ext = "zip";
        }

        // Set the content type for zip files
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "inline; filename=" + Files.getNameWithoutExtension(filenameStr)
                + "." + ext + ";");

        // Create the output stream
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        for (int i = 0; i < serviceUrls.length; i++) {
            // Some file names have spaces, they need to be encoded
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(serviceUrls[i]).build().encode();

            HttpGet method = new HttpGet(uri.toString());
            HttpResponse httpResponse = serviceCaller.getMethodResponseAsHttpResponse(method);

            Header contentType = httpResponse.getEntity().getContentType();

            byte[] responseBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());

            // Create a new entry in the zip file with a timestamped name
            String mime = null;
            if (contentType != null) {
                mime = contentType.getValue();
            }
            String fileExtension = MimeUtil.mimeToFileExtension(mime);
            if (fileExtension != null && !fileExtension.isEmpty()) {
                fileExtension = "." + fileExtension;
            }
            // Is there no filename in the download URL? If so, use a date format as the zip filename
            String zipFilename = getFileName(uri);
            if (zipFilename.equals("")) {
                zipFilename = new SimpleDateFormat((i + 1) + "_yyyyMMdd_HHmmss").format(new Date());
            }
            zout.putNextEntry(new ZipEntry(zipFilename + fileExtension));

            zout.write(responseBytes);
            zout.closeEntry();
        }

        zout.finish();
        zout.flush();
        zout.close();
    }

    /**
     * A proxy to make HTTP POST and GET requests to avoid CORS errors
     * If the incoming request is POST it will send out a POST, if the
     * incoming request is GET it will send out a GET request
     *
     * @param response response object
     * @param request incoming request object
     * @param url the URL to be proxied
     * @throws Exception
     */
    @RequestMapping(value = "/getViaProxy.do", method = {RequestMethod.GET, RequestMethod.POST})
    public void getViaProxy(
            HttpServletResponse response,
            HttpServletRequest request,
            @RequestParam("url") String url,
            @RequestParam(required = false, value = "usepostafterproxy", defaultValue = "false") boolean usePost,
            @RequestParam(required = false, value = "usewhitelist", defaultValue = "true") boolean useWhitelist
            ) throws PortalServiceException, OperationNotSupportedException, URISyntaxException, IOException {
        // Check if on whitelist (if usewhitelist = true)
    	if (useWhitelist) {
	        boolean isTrue = false;
	        URL aUrl = new URL(url);
	        String host = aUrl.getHost();
	        // get the URL whitelist from application.yaml
	        String[] urlList = whitelist.split(" ");
	        if (url != null) {
	            // Set a whitelist for the request URL only from the Commonwealth Government or the State Governments or Universities or Octopus will pass
	            Stream<String> whiteListStream = Stream.of(urlList);
	            isTrue = whiteListStream.anyMatch(parameter -> host.endsWith(parameter));
	        }
	        // Return if not on whitelist
	        if (!isTrue) return;
    	}

        // Assemble method depending on the incoming request's method
        HttpRequestBase method;
        if (request.getMethod().equals("POST") || usePost) {
            // Use old request parameters to assemble new request
            Map<String, String[]> pMap = request.getParameterMap();
            List<NameValuePair> nvpList = new ArrayList<>(pMap.size());
            for (Map.Entry<String, String[]> entry : pMap.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("url") && !entry.getKey().equalsIgnoreCase("usewhitelist")) {
                    for(String val: entry.getValue()) {
                        nvpList.add(new BasicNameValuePair(entry.getKey(), val));
                    }
                }
            }
            // Use an HTTP POST request
            method = new HttpPost(url);
            UrlEncodedFormEntity entity;
            try {
                entity = new UrlEncodedFormEntity(nvpList, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new URISyntaxException(e.getMessage(), "Error parsing UrlEncodedFormEntity");
            }
            ((HttpPost)method).setEntity(entity);
        } else {
            // Use an HTTP GET request
            method = new HttpGet(url);
        }
        HttpClientInputStream result = serviceCaller.getMethodResponseAsStream(method);
        response.addHeader("Cache-Control", "public, max-age=604800, must-revalidate, no-transform");
        try (OutputStream outputStream = response.getOutputStream();) {
            IOUtils.copy(result, outputStream);
        } catch (IOException e) {
            throw new PortalServiceException("Exception during getViaProxy.do "+e.getMessage(), e);
        }
    }

}
