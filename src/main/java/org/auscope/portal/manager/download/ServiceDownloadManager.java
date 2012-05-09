package org.auscope.portal.manager.download;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    private String[] urls;
    private HttpServiceCaller serviceCaller;

    public ServiceDownloadManager(String[] urls,
            HttpServiceCaller serviceCaller, ExecutorService executer)
            throws URISyntaxException {
        this.urls = urls;
        this.serviceCaller = serviceCaller;
        this.pool = executer;
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
            throws URISyntaxException, InterruptedException,
            InCompleteDownloadException {

        Semaphore processSemaphore = new Semaphore(this.maxThreadPerSession,
                true);
        ArrayList<GMLDownload> gmlDownloads = new ArrayList<GMLDownload>();

        for (int i = 0; i < urls.length; i++) {
            Semaphore sem = endpointSemaphores.get(this.getHost(urls[i]));
            GMLDownload gmlDownload = new GMLDownload(urls[i], sem, i,
                    processSemaphore);
            gmlDownloads.add(gmlDownload);
            pool.execute(gmlDownload);
        }
        pool.shutdown();
        pool.awaitTermination(ServiceDownloadManager.MAX_WAIT_TIME_MINUTE,
                TimeUnit.MINUTES);

        ArrayList<DownloadResponse> responses = new ArrayList<DownloadResponse>();
        for (GMLDownload gmlDownload : gmlDownloads) {
            responses.add(gmlDownload.getGMLDownload());
        }

        return responses;
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

    public class GMLDownload implements Runnable {
        private String url;
        DownloadResponse response;
        private boolean downloadComplete = false;
        private Semaphore endPointSem, processSem;
        private int id;

        public GMLDownload(String url, Semaphore sem, int id,
                Semaphore processSem) throws URISyntaxException {
            this.endPointSem = sem;
            this.url = url;
            this.id = id;
            this.processSem = processSem;
            response=new DownloadResponse(getHost(url));
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
                this.download(response,url);
                this.downloadComplete = true;
                logger.info(callerId + "->Download Complete: " + id + " " + url);
            } catch (InterruptedException e) {
                logger.error("No reason for this thread to be interrupted", e);
            } catch(Exception e){
                e.printStackTrace();
            }finally {
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

        public void download(DownloadResponse response, String url) {

            GetMethod method = new GetMethod(url);
            HttpClient client = serviceCaller.getHttpClient();

            try {
                // Our request may fail (due to timeout or otherwise)
                response.setResponseStream(serviceCaller.getMethodResponseAsStream(
                        method, client));
            } catch (Exception ex) {
                logger.error(ex, ex);
                response.setException(ex);
            }

        }
    }
}
