package org.auscope.portal.core.services.cloud;

/**
 * The various levels of STS support that an instance of CloudStorageService/CloudComputeService can implement
 * @author Josh Vote (CSIRO)
 *
 */
public enum STSRequirement {
    /**
     * Only STS enabled jobs will be allowed to read/write data using this instance. Non STS
     * enabled jobs will error when using this class
     */
    Mandatory,
    /**
     * STS jobs will use STS as per normal, non STS jobs will use the inbuilt credentials
     */
    Permissable,
    /**
     * ALL jobs (STS or otherwise) will be forced to use the inbuilt credentials
     */
    ForceNone
}
