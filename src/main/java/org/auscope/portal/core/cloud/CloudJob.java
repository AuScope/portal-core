package org.auscope.portal.core.cloud;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class representing the base state of a job that is sent to the cloud for processing.
 *
 * @author Josh Vote
 */
public class CloudJob implements Serializable, StagedFileOwner, CloudFileOwner {

    /**
     * Generated 2012-06-07
     */
    private static final long serialVersionUID = -3796627138526394662L;

    /** The format used for representing cloud job dates as string */
    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";

    /** Unique ID identifying this job */
    protected Integer id;
    /** Descriptive name of this job */
    protected String name;
    /** Long description of this job */
    protected String description;
    /** Email address of job submitter */
    protected String emailAddress;
    /** user name of job submitter */
    protected String user;
    /** date/time when this job was submitted */
    protected Date submitDate;
    /** date/time when this job was processed */
    protected Date processDate;
    /** descriptive status of this job */
    protected String status;

    /** the ID of the VM that will be used to run this job */
    protected String computeVmId;
    /** the ID of the VM instance that is running this job (will be null if no job is currently running) */
    protected String computeInstanceId;
    /** The type of the compute instance to start (size of memory, number of CPUs etc) - eg m1.large. Can be null */
    protected String computeInstanceType;
    /** The name of the key to inject into the instance at startup for root access. Can be null */
    protected String computeInstanceKey;
    /** The unique ID of the storage service this job has been using */
    protected String computeServiceId;

    /** The key prefix for all files associated with this job in the specified storage bucket */
    protected String storageBaseKey;
    /** The unique ID of the storage service this job has been using */
    protected String storageServiceId;

    transient protected Map<String, String> properties = new HashMap<>();

    public final static String PROPERTY_STS_ARN = "sts_arn";
    public final static String PROPERTY_CLIENT_SECRET = "client_secret";
    public final static String PROPERTY_S3_ROLE = "s3_role";

    /**
     * Creates a new cloud job will null entries for every field
     */
    protected CloudJob() {
        super();
    }

    /**
     * Creates a new cloud job with the following fields
     *
     * @param id
     *            Unique ID identifying this job
     */
    public CloudJob(Integer id) {
        super();
        this.id = id;
    }

    public String setProperty(String key, String value) {
        if (value == null) {
            String oldValue = properties.get(key);
            properties.remove(key);
            return oldValue;
        }
        return properties.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Unique ID identifying this job
     *
     * @return
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Unique ID identifying this job
     *
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Descriptive name of this job
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Descriptive name of this job
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Long description of this job
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Long description of this job
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Email address of job submitter
     *
     * @return
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Email address of job submitter
     *
     * @param emailAddress
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * user name of job submitter
     *
     * @return
     */
    @Override
    public String getUser() {
        return user;
    }

    /**
     * user name of job submitter
     *
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * date/time when this job was submitted
     *
     * @return
     */
    public Date getSubmitDate() {
        return submitDate;
    }

    /**
     * date/time when this job was submitted
     *
     * @param submitDate
     */
    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    /**
     * date/time when this job was processed
     *
     * @return
     */
    public Date getProcessDate() {
        return processDate;
    }

    /**
     * date/time when this job was processed
     *
     * @param processDate
     */
    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    /**
     * date/time when this job was submitted (expects a date in the format CloudJob.DATE_FORMAT)
     *
     * @param submitDate
     * @throws ParseException
     */
    public void setSubmitDate(String submitDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        this.setSubmitDate(sdf.parse(submitDate));
    }

    /**
     * descriptive status of this job
     *
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * descriptive status of this job
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * the ID of the VM that will be used to run this job
     *
     * @return
     */
    public String getComputeVmId() {
        return computeVmId;
    }

    /**
     * the ID of the VM that will be used to run this job
     *
     * @param computeVmId
     */
    public void setComputeVmId(String computeVmId) {
        this.computeVmId = computeVmId;
    }

    /**
     * the ID of the VM instance that is running this job (will be null if no job is currently running)
     *
     * @return
     */
    public String getComputeInstanceId() {
        return computeInstanceId;
    }

    /**
     * the ID of the VM instance that is running this job (will be null if no job is currently running)
     *
     * @param computeInstanceId
     */
    public void setComputeInstanceId(String computeInstanceId) {
        this.computeInstanceId = computeInstanceId;
    }

    /**
     * The type of the compute instance to start (size of memory, number of CPUs etc) - eg m1.large. Can be null
     */
    public String getComputeInstanceType() {
        return computeInstanceType;
    }

    /**
     * The type of the compute instance to start (size of memory, number of CPUs etc) - eg m1.large. Can be null
     */
    public void setComputeInstanceType(String computeInstanceType) {
        this.computeInstanceType = computeInstanceType;
    }

    /**
     * The name of the key to inject into the instance at startup for root access. Can be null
     */
    public String getComputeInstanceKey() {
        return computeInstanceKey;
    }

    /**
     * The name of the key to inject into the instance at startup for root access. Can be null
     */
    public void setComputeInstanceKey(String computeInstanceKey) {
        this.computeInstanceKey = computeInstanceKey;
    }

    /**
     * The unique ID of the compute service this job has been using
     *
     * @return
     */
    public String getComputeServiceId() {
        return computeServiceId;
    }

    /**
     * The unique ID of the compute service this job has been using
     *
     * @param computeServiceId
     */
    public void setComputeServiceId(String computeServiceId) {
        this.computeServiceId = computeServiceId;
    }

    /**
     * The unique ID of the storage service this job has been using
     *
     * @return
     */
    public String getStorageServiceId() {
        return storageServiceId;
    }

    /**
     * The unique ID of the storage service this job has been using
     *
     * @param storageServiceId
     */
    public void setStorageServiceId(String storageServiceId) {
        this.storageServiceId = storageServiceId;
    }

    /**
     * The key prefix for all files associated with this job in the specified storage bucket
     *
     * @return
     */
    @Override
    public String getStorageBaseKey() {
        return storageBaseKey;
    }

    /**
     * The key prefix for all files associated with this job in the specified storage bucket
     *
     * @param storageBaseKey
     */
    @Override
    public void setStorageBaseKey(String storageBaseKey) {
        this.storageBaseKey = storageBaseKey;
    }

    /**
     * Default behaviour is to offload bucket requirements to the CloudStorageService.
     */
    @Override
    public String getStorageBucket() {
        return null;
    }
}
