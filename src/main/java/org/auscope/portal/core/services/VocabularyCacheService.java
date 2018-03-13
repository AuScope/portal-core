package org.auscope.portal.core.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.vocabs.VocabularyServiceItem;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

public class VocabularyCacheService {

    private final Log log = LogFactory.getLog(getClass());

    protected List<VocabularyServiceItem> serviceList;
    protected Executor executor;

    protected Map<String, Map<String, String>> vocabularyCache;
    protected boolean updateRunning;

    public VocabularyCacheService(Executor executor,
                                  ArrayList serviceList) {
        this.executor = executor;
        this.serviceList = serviceList;

        this.vocabularyCache = new HashMap<String, Map<String, String>>();
    }

    /**
     * @return
     */
    public boolean updateCache() {
        if (!okToUpdate()) {
            return false;
        }

        VocabularyCacheUpdateThread[] updateThreads = new VocabularyCacheUpdateThread[serviceList.size()];

        for (int i = 0; i < updateThreads.length; i++) {
            updateThreads[i] = new VocabularyCacheUpdateThread(this,
                    updateThreads,
                    serviceList.get(i),
                    this.vocabularyCache);
        }

        for (VocabularyCacheUpdateThread thread : updateThreads) {
            this.executor.execute(thread);
        }

        return true;
    }

    private synchronized boolean okToUpdate() {
        if (this.updateRunning) {
            return false;
        }

        this.updateRunning = true;
        return true;
    }

    private synchronized void updateFinished(Map<String, Map<String, String>> vocabularyCache) {
        this.updateRunning = false;

        if (vocabularyCache != null) {
            this.vocabularyCache = vocabularyCache;
        }

        int numberOfTerms = 0;
        int numberOfVocabularies = this.vocabularyCache.size();

        for (Entry<String, Map<String, String>> entry : this.vocabularyCache.entrySet()) {
            numberOfTerms +=  entry.getValue().size();
        }
        log.info(String.format("Vocabulary cache updated! Cache now has '%1$d' unique vocabulary terms, from '%2$d' vocabulary services",
                numberOfTerms, numberOfVocabularies));



    }


    private class VocabularyCacheUpdateThread extends Thread {
        private final Log threadLog = LogFactory.getLog(getClass());

        private VocabularyCacheService parent;
        private VocabularyCacheUpdateThread[] siblings;
        private VocabularyServiceItem serviceItem;

        private Map<String, Map<String, String>> vocabularyCache;
        private boolean finishedExecution;


        public VocabularyCacheUpdateThread(VocabularyCacheService parent,
                                           VocabularyCacheUpdateThread[] siblings,
                                           VocabularyServiceItem serviceItem,
                                           Map<String, Map<String, String>> vocabularyCache){
            this.parent = parent;
            this.siblings = siblings;
            this.serviceItem = serviceItem;
            this.vocabularyCache = vocabularyCache;
        }

        private boolean isFinishedExecution() {
            synchronized (siblings) {
                return finishedExecution;
            }
        }

        private void setFinishedExecution(boolean finishedExecution) {
            synchronized (siblings) {
                this.finishedExecution = finishedExecution;
            }
        }

        /**
         *
         */
        @Override
        public void run() {
            try {
                VocabularyService service = serviceItem.getVocabularyService();
                Map<String,String> map = service.getAllRelevantConcepts();
                synchronized (this.vocabularyCache){
                    this.vocabularyCache.put(this.serviceItem.getId(), map);
                }
            }
            catch (PortalServiceException | URISyntaxException e) {
                threadLog.error(e.getStackTrace());

            } finally {
                attemptCleanup();
            }
        }

        private void attemptCleanup() {
            synchronized(siblings) {
                this.setFinishedExecution(true);

                boolean cleanupRequired = true;
                for (VocabularyCacheUpdateThread sibling : siblings) {
                    if (!sibling.isFinishedExecution()) {
                        cleanupRequired = false;
                        break;
                    }
                }

                //Last thread to finish tells our parent we've terminated
                if (cleanupRequired) {
                    parent.updateFinished(vocabularyCache);
                }
            }
        }

    }

    public synchronized Map<String, Map<String, String>> getVocabularyCache() {
        return vocabularyCache;
    }

}
