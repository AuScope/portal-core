package org.auscope.portal.core.server.http.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.http.HttpClientResponse;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.util.DOMResponseUtil;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.util.MimeUtil;

/**
 * A manager class that to control number of requests to a endpoint and also multithread a request to provide more efficiency
 *
 * @author Victor Tey
 *
 */
public class ServiceDownloadManager {
    protected final Log logger = LogFactory.getLog(getClass());
    private final int maxThreadPerEndpoint = 1;
    private final int maxThreadPerSession = 2;
    // VT: the individual download controllers should decided on the length of time we allow each download
    // as they should be handled on a case by case basic rather then 1 length of time to set all.
    // 120 minutes is a huge time as a final catch all safety net.
    public static final int MAX_WAIT_TIME_MINUTE = 360;
    private static ConcurrentHashMap<String, Semaphore> endpointSemaphores;
    private static int globalId;
    private int callerId;
    private ExecutorService pool;

    static {
        endpointSemaphores = new ConcurrentHashMap<>();
    }

    // VT do not directly access entryCount due to multi threading. Access it
    // via getCount()

    private String[] urls;
    private HttpServiceCaller serviceCaller;
    private ServiceConfiguration serviceConfiguration;
    private String fileExtensionOverride;

    public ServiceDownloadManager(String[] urls,
            HttpServiceCaller serviceCaller, ExecutorService executer)
            throws URISyntaxException {
        this(urls, serviceCaller, executer, null);

    }

    public ServiceDownloadManager(String[] urls,
            HttpServiceCaller serviceCaller, ExecutorService executer, ServiceConfiguration serviceConfiguration)
            throws URISyntaxException {
        this(urls, serviceCaller, executer, serviceConfiguration, null);
    }

    public ServiceDownloadManager(String[] urls,
            HttpServiceCaller serviceCaller, ExecutorService executer, ServiceConfiguration serviceConfiguration, String fileExtensionOverride)
            throws URISyntaxException {
        this.serviceConfiguration = serviceConfiguration;
        this.urls = urls;
        this.serviceCaller = serviceCaller;
        this.pool = executer;
        this.fileExtensionOverride = fileExtensionOverride;
        callerId = globalId++;
        for (int i = 0; i < urls.length; i++) {
            String host = this.getHost(urls[i]);
            if (!endpointSemaphores.containsKey(host)) {
                endpointSemaphores.put(host, new Semaphore(
                        maxThreadPerEndpoint, true));
            }
        }
    }

    public synchronized ArrayList<DownloadResponse> downloadAll()
            throws URISyntaxException,
            InCompleteDownloadException {

        Semaphore processSemaphore = new Semaphore(this.maxThreadPerSession,
                true);
        ArrayList<GMLDownload> gmlDownloads = new ArrayList<>();

        for (int i = 0; i < urls.length; i++) {
            Semaphore sem = endpointSemaphores.get(this.getHost(urls[i]));
            GMLDownload gmlDownload = new GMLDownload(urls[i], sem, i,
                    processSemaphore, this.fileExtensionOverride);
            gmlDownloads.add(gmlDownload);
            pool.execute(gmlDownload);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(ServiceDownloadManager.MAX_WAIT_TIME_MINUTE,
                    TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.info("ServiceDownloadManager#downloadAll thread pool shutdown was interrupted.");
        }

        ArrayList<DownloadResponse> responses = new ArrayList<>();
        for (GMLDownload gmlDownload : gmlDownloads) {
            responses.add(gmlDownload.getGMLDownload());
        }

        return responses;
    }

    public String getHost(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String query = uri.getQuery();
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] s = param.split("=");
            if (s.length == 2) {
                String name = s[0];
                String value = s[1];
                map.put(name, value);
            }
        }

        //Some requests may use a hardcoded serviceUrl (not specifying the remote url)
        //We can't guess at underlying hardcoded URL so instead apply service fairness rule to the proxy
        String serviceUrl = map.get("serviceUrl");
        if (serviceUrl == null) {
            return url;
        }

        return serviceUrl;
    }

    public class GMLDownload implements Runnable {
        private String url;
        DownloadResponse response;
        private boolean downloadComplete = false;
        private Semaphore endPointSem, processSem;
        private int id;
        private String downloadFileExtensionOverride;

        public GMLDownload(String url, Semaphore sem, int id,
                Semaphore processSem, String fileExtensionOverride) throws URISyntaxException {
            this.endPointSem = sem;
            this.url = url;
            this.id = id;
            this.processSem = processSem;
            this.downloadFileExtensionOverride = fileExtensionOverride;
            response = new DownloadResponse(getHost(url));
        }

        @Override
        public void run() {

            try {

                boolean secondLockAquired = false;
                boolean firstLockAquired = false;
                // the while loop checks and waits until we acquire both
                // locks process and endpoints to ensure we don't generate
                // a) too much process thread which will create server load
                // b) too much endpoint request thread which will create
                // endpoint service load
                // I have given it 10 minutes to acquire lock for the process
                // and within 5 seconds if a endpoint lock is not acquired,
                // release the process lock so that other threads can attempt to
                // run and later we try again.
                // if the first lock never gets acquired, houston we have a
                // problem.
                while (true) {
                    firstLockAquired = processSem.tryAcquire(
                            ServiceDownloadManager.MAX_WAIT_TIME_MINUTE,
                            TimeUnit.MINUTES);

                    if (firstLockAquired) {
                        secondLockAquired = endPointSem.tryAcquire(5,
                                TimeUnit.SECONDS);
                    }
                    if (!secondLockAquired) {
                        processSem.release();
                        secondLockAquired = false;
                        firstLockAquired = false;
                        logger.warn(callerId + " Attempt to acquire both lock failed. Trying again");
                    } else {
                        break;
                    }
                }
                logger.info((callerId + "->Calling service: " + id + " " + url));
                this.download(response, url);
                this.downloadComplete = true;
                logger.info(callerId + "->Download Complete: " + id + " " + url);
            } catch (InterruptedException e) {
                logger.error("No reason for this thread to be interrupted", e);
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            } finally {
                endPointSem.release();
                processSem.release();
                logger.debug(callerId + "->semaphore release: " + id);
            }

        }

        public DownloadResponse getGMLDownload() throws InCompleteDownloadException {
            if (downloadComplete) {
                return response;
            } else {
                throw new InCompleteDownloadException(
                        "check that download is complete with isDownloadComplete() before calling this method");
            }
        }

        public boolean isDownloadComplete() {
            return downloadComplete;
        }

        public void download(DownloadResponse resp, String theUrl) {
            if (ServiceDownloadManager.this.serviceConfiguration != null) {
                ServiceConfigurationItem serviceConfigurationItem = ServiceDownloadManager.this.serviceConfiguration
                        .getServiceConfigurationItem(theUrl);

                if (serviceConfigurationItem != null && serviceConfigurationItem.doesPaging()) {
                    this.downloadPaging(resp, theUrl);
                } else {
                    this.downloadNormal(resp, theUrl);
                }
            } else {
                this.downloadNormal(resp, theUrl);
            }
        }

        public void downloadNormal(DownloadResponse resp, String theUrl) {
            HttpGet method = new HttpGet(theUrl);
            try {
                // Our request may fail (due to timeout or otherwise)
                // We need to ensure that this httpResponse is NOT closed. That is the responsibility of the
                // classes using this service
                @SuppressWarnings("resource")
                HttpClientResponse httpResponse = serviceCaller.getMethodResponseAsHttpResponse(method);

                resp.setResponseStream(httpResponse.getEntity().getContent());
                Header header = httpResponse.getEntity().getContentType();
                if (header != null && header.getValue().length() > 0) {
                    resp.setContentType(httpResponse.getEntity().getContentType().getValue());
                }

            } catch (Throwable ex) {
                logger.error(ex, ex);
                resp.setException(ex);
            }

        }

        public void downloadPaging(DownloadResponse resp, String theUrl) {
            //A typical request:http://localhost:8080/AuScope-Portal/doMineFilterDownload.do?&mineName=&serviceFilter=
            //http%3A%2F%2Fauscope-services-test.arrc.csiro.au%3A80%2Fgsq-earthresource%2Fwfs&bbox=%7B%22westBoundLongitude%22%3A%22144%22%2C%22
            //southBoundLatitude%22%3A%22-27%22%2C%22eastBoundLongitude%22%3A%22148%22%2C%22northBoundLatitude%22%3A%22-25%22%2C%22crs%22%3A%22EPSG%3A4326%22%7D&
            //serviceUrl=http%3A%2F%2Fauscope-services-test.arrc.csiro.au%3A80%2Fgsq-earthresource%2Fwfs&typeName=er%3AMiningFeatureOccurrence&maxFeatures=200
            File tempDir = null;
            try {
                tempDir = Files.createTempDirectory("APT_PAGING").toFile();
                tempDir.deleteOnExit();

                int index = 0;

                while (true) {
                    HttpGet method = new HttpGet(theUrl + "&startIndex=" + index);
                    @SuppressWarnings("resource")
                    HttpResponse httpResponse = serviceCaller.getMethodResponseAsHttpResponse(method);

                    Header header = httpResponse.getEntity().getContentType();
                    String fileExtension = ".xml";//VT: Default to xml as we will mostly be dealing with xml files
                    if (this.downloadFileExtensionOverride != null) {
                        fileExtension = this.downloadFileExtensionOverride;
                    } else if (header != null && header.getValue().length() > 0) {
                        fileExtension = "."
                                + MimeUtil.mimeToFileExtension(httpResponse.getEntity().getContentType().getValue());

                    }
                    File f = new File(tempDir, "ResultIndexed-" + index + fileExtension);
                    f.deleteOnExit();
                    FileIOUtil.writeStreamToFile(httpResponse.getEntity().getContent(), f, true);
                    int numberOfFeatures = this.getNumberOfFeature(f);
                    if (numberOfFeatures != 0) {
                        index += numberOfFeatures;
                    } else {
                        //VT: Delete file since it has 0 number of features;break out of this while loop
                        f.delete();
                        break;
                    }
                }
                @SuppressWarnings("resource")
                FileInputStream zipStream = new FileInputStream(this.zipDirectory(tempDir));
                //VT: Zip up tempDir and we are good to go.
                resp.setResponseStream(zipStream);
                resp.setContentType("application/zip");

            } catch (Throwable e) {
                logger.error(e, e);
                resp.setException(e);
            } finally {
                //Clean up
                if (tempDir != null) {
                    FileIOUtil.deleteFilesRecursive(tempDir);
                }
            }

        }

        private int getNumberOfFeature(File f) throws IOException, OWSException {
            try (InputStream br = new BufferedInputStream(new FileInputStream(f))) {
                NamespaceContext ns = new NumberOfFeatureNamespace();
                return DOMResponseUtil.getNumberOfFeatures(br, ns);
            }
        }

        private File zipDirectory(File zipDir) throws IOException {

            File tempZip = File.createTempFile("APT_", ".zip");
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip));

            try {

                // get a listing of the directory content
                String[] dirList = zipDir.list();

                // loop through dirList, and zip the files
                for (int i = 0; i < dirList.length; i++) {
                    File f = new File(zipDir, dirList[i]);

                    FileInputStream fis = new FileInputStream(f);
                    try {
                        ZipEntry anEntry = new ZipEntry(f.getName());
                        zos.putNextEntry(anEntry);
                        IOUtils.copy(fis, zos);
                        zos.closeEntry();
                    } finally {
                        fis.close();
                    }
                }
                return tempZip;
            } finally {
                zos.close();
            }
        }

    }
}
