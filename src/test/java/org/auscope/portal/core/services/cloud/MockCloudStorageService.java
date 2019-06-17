/**
 * 
 */
package org.auscope.portal.core.services.cloud;

import org.jclouds.blobstore.BlobStoreContext;

/**
 * @author fri096
 *
 */
public class MockCloudStorageService extends CloudStorageServiceJClouds {

    private BlobStoreContext mockBlobStoreContext;

    public MockCloudStorageService(BlobStoreContext mockBlobStoreContext) {
        super();
        this.mockBlobStoreContext=mockBlobStoreContext;
    }

    @Override
    public BlobStoreContext getBlobStoreContext(String arn, String clientSecret) {
        return mockBlobStoreContext;
    }

}
