package org.auscope.portal.core.services.vocabs;

import org.auscope.portal.core.services.VocabularyService;

/**
 *
 */
public class VocabularyServiceItem {
    private String id;
    private String title;

    private VocabularyService vocabularyService;

    public VocabularyServiceItem(String id, String title, VocabularyService vocabularyService) {
        this.id = id;
        this.title = title;
        this.vocabularyService = vocabularyService;
    }



    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public VocabularyService getVocabularyService() {
        return vocabularyService;
    }

    public void setVocabularyService(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }


}
