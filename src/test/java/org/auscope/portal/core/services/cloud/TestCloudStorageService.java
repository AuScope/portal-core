package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;

import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.test.PortalTestClass;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder.PayloadBlobBuilder;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestCloudStorageService extends PortalTestClass {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private final String bucket = "bucket-name";

    private BlobStoreContext mockBlobStoreContext = context.mock(BlobStoreContext.class);
    private CloudJob job;
    private CloudStorageService service;

    private final String jobStorageBaseKey = "job/base/key";

    @Before
    public void initJobObject() {
        job = new CloudJob(13);
        job.setStorageBaseKey(jobStorageBaseKey);
        service = new CloudStorageService(mockBlobStoreContext);
        service.setBucket(bucket);
    }

    @Test
    public void testGetJobFileData() throws Exception {
        final String myKey = "my/key";
        final BlobStore mockBlobStore = context.mock(BlobStore.class);
        final Blob mockBlob = context.mock(Blob.class);
        final Payload mockPayload = context.mock(Payload.class);
        final InputStream mockReturnedInputStream = context.mock(InputStream.class);

        context.checking(new Expectations() {
            {
                oneOf(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));

                oneOf(mockBlobStore).getBlob(bucket, jobStorageBaseKey + "/" + myKey);
                will(returnValue(mockBlob));

                oneOf(mockBlob).getPayload();
                will(returnValue(mockPayload));

                oneOf(mockPayload).getInput();
                will(returnValue(mockReturnedInputStream));
            }
        });

        InputStream actualInputStream = service.getJobFile(job, myKey);
        Assert.assertSame(mockReturnedInputStream, actualInputStream);
    }

    /**
     * Tests that requests for listing files successfully call all dependencies
     * 
     * @throws Exception
     */
    @Test
    public void testListOutputJobFiles() throws Exception {
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        final BlobMetadataImpl mockStorageMetadata1 = context.mock(BlobMetadataImpl.class, "mockStorageMetadata1");
        final BlobMetadataImpl mockStorageMetadata2 = context.mock(BlobMetadataImpl.class, "mockStorageMetadata2");

        final MutableContentMetadata mockObj1ContentMetadata = context.mock(MutableContentMetadata.class, "mockObj1Md");
        final MutableContentMetadata mockObj2ContentMetadata = context.mock(MutableContentMetadata.class, "mockObj2Md");
        final PageSet<? extends StorageMetadata> mockPageSet = context.mock(PageSet.class);

        LinkedList<MutableBlobMetadataImpl> ls = new LinkedList<MutableBlobMetadataImpl>();
        ls.add(context.mock(MutableBlobMetadataImpl.class, "mockObj1"));
        ls.add(context.mock(MutableBlobMetadataImpl.class, "mockObj2"));

        final String obj1Key = "key/obj1";
        final String obj1Bucket = "bucket1";
        final long obj1Length = 1234L;
        final String obj2Key = "key/obj2";
        final String obj2Bucket = "bucket2";
        final long obj2Length = 4567L;

        context.checking(new Expectations() {
            {
                oneOf(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));

                oneOf(mockBlobStore).list(with(equal(bucket)), with(any(ListContainerOptions.class)));
                will(returnValue(mockPageSet));

                allowing(mockPageSet).getNextMarker();
                will(returnValue(null));

                allowing(mockPageSet).iterator();
                will(returnValue(Arrays.asList(mockStorageMetadata1, mockStorageMetadata2).iterator()));

                allowing(mockStorageMetadata1).getName();
                will(returnValue(obj1Key));
                allowing(mockStorageMetadata1).getUri();
                will(returnValue(new URI(obj1Bucket)));
                allowing(mockStorageMetadata1).getContentMetadata();
                will(returnValue(mockObj1ContentMetadata));
                allowing(mockObj1ContentMetadata).getContentLength();
                will(returnValue(obj1Length));

                allowing(mockStorageMetadata2).getName();
                will(returnValue(obj2Key));
                allowing(mockStorageMetadata2).getUri();
                will(returnValue(new URI(obj2Bucket)));
                allowing(mockStorageMetadata2).getContentMetadata();
                will(returnValue(mockObj2ContentMetadata));
                allowing(mockObj2ContentMetadata).getContentLength();
                will(returnValue(obj2Length));
            }
        });

        CloudFileInformation[] fileInfo = service.listJobFiles(job);
        Assert.assertNotNull(fileInfo);
        Assert.assertEquals(ls.size(), fileInfo.length);
        Assert.assertEquals(obj1Key, fileInfo[0].getCloudKey());
        Assert.assertEquals(obj1Length, fileInfo[0].getSize());
        Assert.assertEquals(obj2Key, fileInfo[1].getCloudKey());
        Assert.assertEquals(obj2Length, fileInfo[1].getSize());
    }

    /**
     * Tests that requests for uploading files successfully call all dependencies
     * 
     * @throws Exception
     */
    @Test
    public void testUploadJobFiles() throws Exception {
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        final Blob mockBlob1 = context.mock(Blob.class, "mockBlob1");
        final Blob mockBlob2 = context.mock(Blob.class, "mockBlob2");

        final PayloadBlobBuilder mockBuilder1 = context.mock(PayloadBlobBuilder.class, "mockBuilder1");
        final PayloadBlobBuilder mockBuilder2 = context.mock(PayloadBlobBuilder.class, "mockBuilder2");

        final File[] mockFiles = new File[] {
                context.mock(File.class, "mockFile1"),
                context.mock(File.class, "mockFile2"),
        };

        context.checking(new Expectations() {
            {
                oneOf(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));

                allowing(mockFiles[0]).getName();
                will(returnValue("file1Name"));
                allowing(mockFiles[1]).getName();
                will(returnValue("file2Name"));

                oneOf(mockBlobStore).blobBuilder(jobStorageBaseKey + "/file1Name");
                will(returnValue(mockBuilder1));
                oneOf(mockBlobStore).blobBuilder(jobStorageBaseKey + "/file2Name");
                will(returnValue(mockBuilder2));

                oneOf(mockBuilder1).payload(mockFiles[0]);
                will(returnValue(mockBuilder1));
                oneOf(mockBuilder1).build();
                will(returnValue(mockBlob1));

                oneOf(mockBuilder2).payload(mockFiles[1]);
                will(returnValue(mockBuilder2));
                oneOf(mockBuilder2).build();
                will(returnValue(mockBlob2));

                oneOf(mockBlobStore).putBlob(bucket, mockBlob1);
                oneOf(mockBlobStore).putBlob(bucket, mockBlob2);
            }
        });

        service.uploadJobFiles(job, mockFiles);
    }

    /**
     * Tests that requests for deleting files successfully call all dependencies
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteJobFiles() throws Exception {
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        context.checking(new Expectations() {
            {
                allowing(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));
                oneOf(mockBlobStore).deleteDirectory(bucket, jobStorageBaseKey);
            }
        });

        service.deleteJobFiles(job);
    }

    /**
     * Tests that no exceptions occur during base key generation edge cases
     * 
     * @throws Exception
     */
    @Test
    public void testBaseKeyGeneration() throws Exception {
        CloudJob emptyJob = new CloudJob(null);

        String emptyJobBaseKey = service.generateBaseKey(emptyJob);
        Assert.assertNotNull(emptyJobBaseKey);
        Assert.assertFalse(emptyJobBaseKey.isEmpty());

        CloudJob nonEmptyJob = new CloudJob(42);
        String nonEmptyJobBaseKey = service.generateBaseKey(nonEmptyJob);
        Assert.assertNotNull(nonEmptyJobBaseKey);
        Assert.assertFalse(nonEmptyJobBaseKey.isEmpty());

        //Base keys should differ based soley on ID
        Assert.assertFalse(nonEmptyJobBaseKey.equals(emptyJobBaseKey));
    }

    /**
     * Tests that generateBaseKey doesn't generate keys that are a substring of eachother.
     *
     * Cloud storage treats files as being in a 'directory' based solely on its prefix. If you have the following files:
     *
     * job5/aFile.txt job52/anotherFile.txt
     *
     * And searched for all files whose prefix begins 'job5' (i.e. to list all files in the job5 directory), you would get BOTH of the above files returned. We
     * need to manage this edge case by ensuring our prefixes don't overlap like that.
     * 
     * @throws Exception
     */
    @Test
    public void testBaseKeyNoSubstrings() throws Exception {
        CloudJob jobBase = new CloudJob(new Integer(5));
        CloudJob[] jobsToTest = new CloudJob[] {
                new CloudJob(new Integer(50)),
                new CloudJob(new Integer(52)),
                new CloudJob(new Integer(500)),
                new CloudJob(new Integer(500000000)),
        };

        for (int i = 0; i < jobsToTest.length; i++) {
            String base = service.generateBaseKey(jobBase);
            String test = service.generateBaseKey(jobsToTest[i]);

            Assert.assertFalse(base.startsWith(test));
            Assert.assertFalse(test.startsWith(base));
        }
    }
}
