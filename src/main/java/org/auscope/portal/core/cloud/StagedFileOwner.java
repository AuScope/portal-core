/**
 *
 */
package org.auscope.portal.core.cloud;

/**
 * To be implemented by classes that want to own files from the FileStagingService
 *
 * @author fri096
 *
 */
public interface StagedFileOwner {

    /**
     * Return unique identifier
     * 
     * @return unique identifier
     */
    Integer getId();

}
