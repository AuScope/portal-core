package org.auscope.portal.core.server.http.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.util.FileIOUtil;

public class DownloadTracker {
    protected final Log logger = LogFactory.getLog(getClass());
    private String email;
    private static ConcurrentHashMap<String, DownloadTracker> downloadTracker;
    private Progression downloadProgress;
    private File file;

    static {
        downloadTracker = new ConcurrentHashMap<String, DownloadTracker>();
    }

    public DownloadTracker(String email) {
        this.email = email;
        downloadProgress = Progression.NOT_STARTED;
        try {
            this.file=File.createTempFile(email, ".zip");
        } catch (IOException e) {
            logger.error("Unable to write to file", e);
            e.printStackTrace();
        }
    }

    public static DownloadTracker getTracker(String email) {
        if (downloadTracker.containsKey(email)) {
            return downloadTracker.get(email);
        } else {
            DownloadTracker tracker = new DownloadTracker(email);

            downloadTracker.put(email, tracker);
            return tracker;
        }
    }

    public synchronized void startTrack(ServiceDownloadManager sdm) throws InCompleteDownloadException {

        if(this.downloadProgress==Progression.INPROGRESS){
            throw new InCompleteDownloadException("We do not allow the start of a new download when the old request has not complete");
        }else{
            synchronized(downloadProgress){
                this.downloadProgress=Progression.INPROGRESS;
            }
        }
        Process p = new Process(sdm);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.execute(p);
        pool.shutdown();

    }


    public synchronized InputStream getFile() throws InCompleteDownloadException, FileNotFoundException{
        if(getProgress()==Progression.COMPLETED){
            return new FileInputStream(this.file);
        }else{
            throw new InCompleteDownloadException("that that download has complete using getDownloadComplete() before requesting file");
        }
    }

    public synchronized File getFileHandle() throws InCompleteDownloadException, FileNotFoundException{
        if(getProgress()==Progression.COMPLETED){
            return this.file;
        }else{
            throw new InCompleteDownloadException("that that download has complete using getDownloadComplete() before requesting file");
        }
    }

    public synchronized void setDownloadComplete() {
        this.downloadProgress = Progression.COMPLETED;
    }

    public Progression getProgress() {
        return this.downloadProgress;
    }

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
                //in the event that a user makes another request we want to ensure the old file reference is deleted
                //and a file one is created in its place.
                if(file.exists()){
                    file.delete();
                    file=File.createTempFile(email, ".zip");
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
            }  catch (Exception e) {
                logger.error("Error with the serviceDownloadManager", e);
                e.printStackTrace();
            }


            DownloadTracker.this.setDownloadComplete();

        }

    }

}
