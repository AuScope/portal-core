package org.auscope.portal.core.services.csw.custom;

public interface CustomRegistryInt {
    public String getRecordInformationUrl();

    /**
     * @param recordInformationUrl
     *            the recordInformationUrl to set
     */
    public void setRecordInformationUrl(String recordInformationUrl);

    /**
     * @return the serviceUrl
     */
    public String getServiceUrl();

    /**
     * @param serviceUrl
     *            the serviceUrl to set
     */
    public void setServiceUrl(String serviceUrl);

    /**
     * @return the title
     */
    public String getTitle();

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title);

    /**
     * @return the id
     */
    public String getId();

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id);

    /**
     * Check if registry is empty
     */
    public boolean isEmpty();
}
