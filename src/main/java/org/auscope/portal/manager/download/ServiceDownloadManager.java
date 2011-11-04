package org.auscope.portal.manager.download;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.manager.download.exception.InCompleteDownloadException;
import org.auscope.portal.server.web.service.HttpServiceCaller;
/**
 * A manager class that to control number of requests to a endpoint and also multithread a request
 * to provide more efficiency
 * @author Victor Tey
 *
 */
public class ServiceDownloadManager {
    protected final Log logger = LogFactory.getLog(getClass());
    private final int maxThreadPerEndpoint = 1;
    private final int maxThreadPerSession = 2;
    public static final int MAX_WAIT_TIME_MINUTE = 20;
    private static Hashtable<String, Semaphore> endpointSemaphores;
    private static int globalId;
    private int callerId;
    private ExecutorService pool;

    static{
        endpointSemaphores = new Hashtable<String, Semaphore>();
    }

    // VT do not directly access entryCount due to multi threading. Access it
    // via getCount()
    private int entryCount = 0;
    private String[] urls;
    private ZipOutputStream zout;
    private HttpServiceCaller serviceCaller;

    public ServiceDownloadManager(String[] urls, ZipOutputStream zout,
            HttpServiceCaller serviceCaller,ExecutorService executer) throws URISyntaxException {
        this.urls = urls;
        this.zout = zout;
        this.serviceCaller = serviceCaller;
        this.pool=executer;
        callerId=globalId++;
        for (int i = 0; i < urls.length; i++) {
            String host=this.getHost(urls[i]);
            if (!endpointSemaphores.containsKey(host)) {
                endpointSemaphores.put(host, new Semaphore(
                        maxThreadPerEndpoint, true));
            }
        }
    }

    public synchronized void downloadAll() throws URISyntaxException, InterruptedException {

        Semaphore processSemaphore = new Semaphore(this.maxThreadPerSession, true);

        for (int i = 0; i < urls.length; i++) {
            Semaphore sem = endpointSemaphores.get(this.getHost(urls[i]));
            pool.execute(new GMLZipDownload(urls[i], sem, i,
                    processSemaphore));
        }
       pool.shutdown();
       pool.awaitTermination(ServiceDownloadManager.MAX_WAIT_TIME_MINUTE, TimeUnit.MINUTES);
    }

    public String getHost(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String query = uri.getQuery();
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String[] s = param.split("=");
            if (s.length == 2) {
                String name = s[0];
                String value = s[1];
                map.put(name, value);
            }
        }
        return map.get("serviceUrl");
    }

    public synchronized int getCount() {
        return this.entryCount++;
    }

    public class GMLZipDownload implements Runnable {
        private String url;
        private JSONObject jsonObject = null;
        private boolean downloadComplete = false;
        private Semaphore endPointSem, processSem;
        private int id;

        public GMLZipDownload(String url, Semaphore sem, int id,
                Semaphore processSem) {
            this.endPointSem = sem;
            this.url = url;
            this.id = id;
            this.processSem = processSem;
        }

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
                logger.debug((callerId + "->Calling service: " + id +" " + url));
                jsonObject = this.download(url);
                this.downloadComplete = true;
                logger.info(callerId + "->Download Complete: " + id + " " + url);
                writeToZipFile(jsonObject);
            } catch (InterruptedException e) {
                logger.error("No reason for this thread to be interrupted", e);
            } catch (IOException e) {
                logger.error(e, e);
            } finally {
                endPointSem.release();
                processSem.release();
                logger.debug(callerId + "->semaphore release: " + id);
            }

        }

        public JSONObject getGMLDownload() throws InCompleteDownloadException {
            if (downloadComplete) {
                return jsonObject;
            } else {
                throw new InCompleteDownloadException(
                        "check that download is complete with isDownloadComplete() before calling this method");
            }
        }

        public boolean isDownloadComplete() {
            return downloadComplete;
        }

        public JSONObject download(String url) {

            GetMethod method = new GetMethod(url);
            HttpClient client = serviceCaller.getHttpClient();

            // Our request may fail (due to timeout or otherwise)
            String responseString = null;
            JSONObject jsonObject = null;
            try {
                responseString = serviceCaller.getMethodResponseAsString(
                        method, client);

                logger.trace("Response: " + responseString);

                jsonObject = JSONObject.fromObject(responseString);
            } catch (Exception ex) {
                // Replace a failure exception with a JSONObject representing
                // that exception
                logger.error(ex, ex);
                jsonObject = new JSONObject();
                jsonObject.put("msg", ex.getMessage());
                jsonObject.put("success", false);
                responseString = ex.toString();
            }
            return jsonObject;

        }

        public void writeToZipFile(JSONObject jsonObject) throws IOException {
            // Extract our data (if it exists)
            byte[] gmlBytes = new byte[] {}; // The error response is an empty
            // array
            Object dataObject = jsonObject.get("data");
            Object messageObject = jsonObject.get("msg"); // This will be used
            // as an
            // error string
            if (messageObject == null) {
                messageObject = "";
            }
            if (dataObject != null && !JSONNull.getInstance().equals(dataObject)) {
                Object gmlResponseObject = JSONObject.fromObject(dataObject)
                        .get("gml");

                if (gmlResponseObject != null) {
                    gmlBytes = gmlResponseObject.toString().getBytes();
                }
            }

            logger.trace(gmlBytes.length);

            if (jsonObject.get("success").toString().equals("false")) {
                // The server may have returned an error message, if so, lets
                // include it in the filename

                String messageString = messageObject.toString();
                if (messageString.length() == 0)
                    messageString = "operation-failed";

                // "Tidy" up the message
                messageString = messageString.replace(' ', '_')
                        .replace(".", "");

                zout.putNextEntry(new ZipEntry(new SimpleDateFormat(
                        (this.id + 1) + "_yyyyMMdd_HHmmss").format(new Date())
                        + "-" + messageString + ".xml"));
            } else {
                // create a new entry in the zip file with a timestamped name
                zout.putNextEntry(new ZipEntry(new SimpleDateFormat(
                        (this.id + 1) + "_yyyyMMdd_HHmmss").format(new Date())
                        + ".xml"));
            }

            zout.write(gmlBytes);
            zout.closeEntry();

        }
    }
}
