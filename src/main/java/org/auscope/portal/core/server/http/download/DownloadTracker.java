package org.auscope.portal.core.server.http.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.util.FileIOUtil;

/**
 * DownloadTracker provides a way for downloads to be made in the background via a seperate track and tracks its download progress. A basic usage is to
 * getTracker(email), startTrack(), then getFile or getFileHandle. Refer to each of the method Java doc for more info
 *
 *
 * @author tey006
 *
 */
public class DownloadTracker {
    protected final Log logger = LogFactory.getLog(getClass());
    private String email;
    private static ConcurrentHashMap<String, DownloadTracker> downloadTracker;
    private Progression downloadProgress;
    private File file;
    private long lastCompletedTime = System.currentTimeMillis();
    public static final long timeAllowForCache = 6 * 60 * 60 * 1000; //VT we give the user 6 hours to download before we clear up memory

    static {
        downloadTracker = new ConcurrentHashMap<String, DownloadTracker>();
    }

    public DownloadTracker(String email) {
        this.email = email;
        downloadProgress = Progression.NOT_STARTED;
        try {
            this.file = File.createTempFile("APT_TRACKER", ".zip");
            this.file.deleteOnExit();
        } catch (IOException e) {
            logger.error("Unable to write to file", e);
            e.printStackTrace();
        }
    }

    /**
     * To get a reference to an instance of the tracker. Each email address acts as a token and is only allowed 1 instance of a tracker
     * 
     * @param email
     *            : unique token to identify the tracker and its user
     * @return a reference to a DownloadTracker instance
     */
    public static DownloadTracker getTracker(String email) {

        //VT: always perform some clean up in case of memory leak.
        DownloadTracker.cleanUp(timeAllowForCache);

        if (downloadTracker.containsKey(email)) {
            return downloadTracker.get(email);
        } else {
            DownloadTracker tracker = new DownloadTracker(email);

            downloadTracker.put(email, tracker);
            return tracker;
        }
    }

    /**
     * This method cleans up the downloadTracker map object and frees up memory
     * 
     * @param timeAllowance
     *            : how much time do we allow the object to sit in memory.
     */
    public synchronized static void cleanUp(long timeAllowance) {
        //Everytime someone attempts to get a Tracker we do some cleaning up
        Set<String> keys = downloadTracker.keySet();
        long currentTime = System.currentTimeMillis();

        for (String key : keys) {
            long lastComplete = downloadTracker.get(key).getLastCompletedTime();
            if (currentTime - lastComplete > timeAllowance) {
                try {
                    File f = downloadTracker.get(key).getFileHandle();
                    f.delete();
                } catch (Exception e) {
                    LogFactory.getLog(DownloadTracker.class).warn(e);
                }
                downloadTracker.remove(key);
            }
        }
    }

    /**
     * Creates a background thread to commence the download
     * 
     * @param smd
     *            - ServiceDownloadManager {@link ServiceDownloadManager}
     * @throws InCompleteDownloadException
     *             {@link InCompleteDownloadException}
     */
    public synchronized void startTrack(ServiceDownloadManager sdm) throws InCompleteDownloadException {

        if (this.downloadProgress == Progression.INPROGRESS) {
            throw new InCompleteDownloadException(
                    "We do not allow the start of a new download when the old request has not complete");
        } else {
            synchronized (downloadProgress) {
                this.downloadProgress = Progression.INPROGRESS;
            }
        }
        Process p = new Process(sdm);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.execute(p);
        pool.shutdown();

    }

    /**
     * Retrieve the file after download as stream
     * 
     * @return
     * @throws InCompleteDownloadException
     *             {@link InCompleteDownloadException}
     * @throws FileNotFoundException
     */
    public synchronized InputStream getFile() throws InCompleteDownloadException, FileNotFoundException {
        if (getProgress() == Progression.COMPLETED) {
            return new FileInputStream(this.file);
        } else {
            throw new InCompleteDownloadException(
                    "that that download has complete using getDownloadComplete() before requesting file");
        }
    }

    /**
     * Retrieve the file after download as a file handle
     * 
     * @return
     * @throws InCompleteDownloadException
     * @throws FileNotFoundException
     */
    public synchronized File getFileHandle() throws InCompleteDownloadException, FileNotFoundException {
        if (getProgress() == Progression.COMPLETED) {
            return this.file;
        } else {
            throw new InCompleteDownloadException(
                    "that that download has complete using getDownloadComplete() before requesting file");
        }
    }

    /**
     * return the time of last completion
     * 
     * @return time of last completion
     */
    public long getLastCompletedTime() {
        return this.lastCompletedTime;
    }

    /**
     * set download completion flag
     */
    public synchronized void setDownloadComplete() {
        this.lastCompletedTime = System.currentTimeMillis();
        this.downloadProgress = Progression.COMPLETED;
    }

    /**
     * get current progress of download
     * 
     * @return download progress
     */
    public Progression getProgress() {
        return this.downloadProgress;
    }

    /**
     * A runnable thread to executed in the background to perform download
     * 
     * @author tey006
     *
     */
    public class Process implements Runnable {
        ServiceDownloadManager sdm;

        // File file;

        public Process(ServiceDownloadManager sdm) {
            this.sdm = sdm;
            // this.file=file;
        }

        @Override
        public void run() {

            FileOutputStream fos;
            ZipOutputStream zout;

            try {
                // in the event that a user makes another request we want to
                // ensure the old file reference is deleted
                // and a file one is created in its place.
                if (file.exists()) {
                    file.delete();
                    file = File.createTempFile("APT_TRACKER", ".zip");
                    file.deleteOnExit();
                }
                fos = new FileOutputStream(file);
                zout = new ZipOutputStream(fos);
                ArrayList<DownloadResponse> gmlDownloads = sdm.downloadAll();
                FileIOUtil.writeResponseToZip(gmlDownloads, zout);
                zout.finish();
                zout.flush();
                zout.close();
                fos.flush();
                fos.close();

            } catch (FileNotFoundException e) {
                logger.error("Unable to write to file", e);
                e.printStackTrace();
            } catch (Exception e) {
                logger.error("Error with the serviceDownloadManager", e);
                e.printStackTrace();
            } finally {
                // VT : No matter what happens we have to give it a completion.
                DownloadTracker.this.setDownloadComplete();
            }

        }

    }

}
