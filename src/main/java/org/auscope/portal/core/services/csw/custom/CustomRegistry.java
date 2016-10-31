package org.auscope.portal.core.services.csw.custom;

public class CustomRegistry implements CustomRegistryInt {
    private String id;
    private String title, serviceUrl;
    private String recordInformationUrl;

    public CustomRegistry(String id, String title, String serviceUrl, String recordInformationUrl) {
        this.id = id;
        this.title = title;
        this.serviceUrl = serviceUrl;
        this.recordInformationUrl = recordInformationUrl;
    }

    public CustomRegistry(String[] registryInfo) {
        this(registryInfo[0], registryInfo[1], registryInfo[2], registryInfo[3]);
    }

    @Override
    public boolean isEmpty() {
        //VT: All info are crucial therefore we don't recognize this registry if it is missing any information.
        if (id.isEmpty() || title.isEmpty() || serviceUrl.isEmpty() || recordInformationUrl.isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * @return the recordInformationUrl
     */
    @Override
    public String getRecordInformationUrl() {
        return recordInformationUrl;
    }

    /**
     * @param recordInformationUrl
     *            the recordInformationUrl to set
     */
    @Override
    public void setRecordInformationUrl(String recordInformationUrl) {
        this.recordInformationUrl = recordInformationUrl;
    }

    /**
     * @return the serviceUrl
     */
    @Override
    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * @param serviceUrl
     *            the serviceUrl to set
     */
    @Override
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }
}
