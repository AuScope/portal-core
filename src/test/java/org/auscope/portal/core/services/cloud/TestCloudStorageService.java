package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder.PayloadBlobBuilder;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.BaseImmutableContentMetadata;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.ByteSource;

public class TestCloudStorageService extends PortalTestClass {
    private final String bucket = "bucket-name";

    private BlobStoreContext mockBlobStoreContext = context.mock(BlobStoreContext.class);
    private CloudJob job;
    private CloudStorageService service;

    private final String jobStorageBaseKey = "job/base/key";

    @Before
    public void initJobObject() {
        job = new CloudJob(13);
        job.setStorageBaseKey(jobStorageBaseKey);
        service = new MockCloudStorageService(mockBlobStoreContext);
        service.setBucket(bucket);
    }

    @Test
    public void testGetJobFileData() throws IOException, PortalServiceException  {
        final String myKey = "my.key";
        final BlobStore mockBlobStore = context.mock(BlobStore.class);
        final Blob mockBlob = context.mock(Blob.class);
        try (final Payload mockPayload = context.mock(Payload.class);
                final InputStream mockReturnedInputStream = context.mock(InputStream.class)) {
            context.checking(new Expectations() {
                {
                    oneOf(mockBlobStoreContext).close();
                    oneOf(mockBlobStoreContext).getBlobStore();
                    will(returnValue(mockBlobStore));

                    oneOf(mockBlobStore).getBlob(bucket, jobStorageBaseKey + "/" + myKey);
                    will(returnValue(mockBlob));

                    oneOf(mockBlob).getPayload();
                    will(returnValue(mockPayload));

                    oneOf(mockPayload).openStream();
                    will(returnValue(mockReturnedInputStream));

                    allowing(mockReturnedInputStream).close();
                    allowing(mockPayload).close();
                }
            });

            try (InputStream actualInputStream = service.getJobFile(job, myKey)) {
                Assert.assertSame(mockReturnedInputStream, actualInputStream);
            }
        }
    }

    @Test
    public void testGetJobFileMetaData() throws URISyntaxException, PortalServiceException  {
        final String myKey = "my.key";
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        final Map<String, String> userMetadata = new HashMap<>();
        final BaseImmutableContentMetadata contentMetadata = new BaseImmutableContentMetadata("mime/type", 24L, null, null, null, null, null);
        final StorageMetadata metadata = new BlobMetadataImpl("id", "name", null, new URI("http://example.cloud/file"), "asdsadsasd", new Date(), new Date(), userMetadata, new URI("http://example.cloud/publicfile"), null, contentMetadata);


        context.checking(new Expectations() {
            {
                oneOf(mockBlobStoreContext).close();
                oneOf(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));

                oneOf(mockBlobStore).blobMetadata(bucket, jobStorageBaseKey + "/" + myKey);
                will(returnValue(metadata));
            }
        });

        CloudFileInformation result = service.getJobFileMetadata(job, myKey);

        Assert.assertEquals("asdsadsasd", result.getFileHash());
        Assert.assertEquals("name", result.getName());
        Assert.assertEquals(24L, result.getSize());
    }

    @Test
    public void testGetJobFileSanitation() throws URISyntaxException, PortalServiceException  {
        final String myKey = "my.key"; //will have . preserved
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        final Map<String, String> userMetadata = new HashMap<>();
        final BaseImmutableContentMetadata contentMetadata = new BaseImmutableContentMetadata("mime/type", 24L, null, null, null, null, null);
        final StorageMetadata metadata = new BlobMetadataImpl("id", "name", null, new URI("http://example.cloud/file"), "asdsadsasd", new Date(), new Date(), userMetadata, new URI("http://example.cloud/publicfile"), null, contentMetadata);

        job.setStorageBaseKey(null);
        job.setUser("user");
        service.setJobPrefix("prefix.san-"); //will get the . removed

        context.checking(new Expectations() {
            {
                oneOf(mockBlobStoreContext).close();
                oneOf(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));

                oneOf(mockBlobStore).blobMetadata(bucket, "prefix_san-user-0000000013/my.key");
                will(returnValue(metadata));
            }
        });

        CloudFileInformation result = service.getJobFileMetadata(job, myKey);

        Assert.assertEquals("asdsadsasd", result.getFileHash());
        Assert.assertEquals("name", result.getName());
        Assert.assertEquals(24L, result.getSize());
    }

    /**
     * Tests that requests for listing files successfully call all dependencies
     * @throws URISyntaxException
     * @throws PortalServiceException
     */
    @Test
    public void testListOutputJobFiles() throws URISyntaxException, PortalServiceException  {
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        final BlobMetadataImpl mockStorageMetadata1 = context.mock(BlobMetadataImpl.class, "mockStorageMetadata1");
        final BlobMetadataImpl mockStorageMetadata2 = context.mock(BlobMetadataImpl.class, "mockStorageMetadata2");
        final BlobMetadataImpl mockStorageMetadata3 = context.mock(BlobMetadataImpl.class, "mockStorageMetadata3");

        final MutableContentMetadata mockObj1ContentMetadata = context.mock(MutableContentMetadata.class, "mockObj1Md");
        final MutableContentMetadata mockObj2ContentMetadata = context.mock(MutableContentMetadata.class, "mockObj2Md");
        final MutableContentMetadata mockObj3ContentMetadata = context.mock(MutableContentMetadata.class, "mockObj3Md");
        final PageSet<? extends StorageMetadata> mockPageSet = context.mock(PageSet.class);

        LinkedList<MutableBlobMetadataImpl> ls = new LinkedList<>();
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
                oneOf(mockBlobStoreContext).close();
                oneOf(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));

                oneOf(mockBlobStore).list(with(equal(bucket)), with(any(ListContainerOptions.class)));
                will(returnValue(mockPageSet));

                allowing(mockPageSet).getNextMarker();
                will(returnValue(null));

                allowing(mockPageSet).iterator();
                will(returnValue(Arrays.asList(mockStorageMetadata1, mockStorageMetadata2, mockStorageMetadata3).iterator()));

                allowing(mockStorageMetadata1).getName();
                will(returnValue(obj1Key));
                allowing(mockStorageMetadata1).getUri();
                will(returnValue(new URI(obj1Bucket)));
                allowing(mockStorageMetadata1).getContentMetadata();
                will(returnValue(mockObj1ContentMetadata));
                allowing(mockStorageMetadata1).getType();
                will(returnValue(StorageType.BLOB));
                allowing(mockObj1ContentMetadata).getContentLength();
                will(returnValue(obj1Length));
                allowing(mockStorageMetadata1).getETag();
                will(returnValue("sadgafsadfa"));


                allowing(mockStorageMetadata2).getName();
                will(returnValue(obj2Key));
                allowing(mockStorageMetadata2).getUri();
                will(returnValue(new URI(obj2Bucket)));
                allowing(mockStorageMetadata2).getContentMetadata();
                will(returnValue(mockObj2ContentMetadata));
                allowing(mockStorageMetadata2).getType();
                will(returnValue(StorageType.BLOB));
                allowing(mockObj2ContentMetadata).getContentLength();
                will(returnValue(obj2Length));
                allowing(mockStorageMetadata2).getETag();
                will(returnValue("mocoqqwiiluhqw"));

                allowing(mockStorageMetadata3).getContentMetadata();
                will(returnValue(mockObj3ContentMetadata));
                allowing(mockStorageMetadata3).getType();
                will(returnValue(StorageType.FOLDER));
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
     * @throws PortalServiceException
     */
    @Test
    public void testUploadJobFiles() throws PortalServiceException {
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

                oneOf(mockBlobStore).createContainerInLocation(null, bucket);
                will(returnValue(true));

                allowing(mockFiles[0]).length();
                will(returnValue(1L));
                allowing(mockFiles[0]).getName();
                will(returnValue("file1Name"));

                allowing(mockFiles[1]).length();
                will(returnValue(1L));
                allowing(mockFiles[1]).getName();
                will(returnValue("file2Name"));

                oneOf(mockBlobStore).blobBuilder(jobStorageBaseKey + "/file1Name");
                will(returnValue(mockBuilder1));
                oneOf(mockBlobStore).blobBuilder(jobStorageBaseKey + "/file2Name");
                will(returnValue(mockBuilder2));

                allowing(mockBuilder1).contentLength(1L);
                will(returnValue(mockBuilder1));
                allowing(mockBuilder1).payload(with(any(ByteSource.class)));
                will(returnValue(mockBuilder1));
                oneOf(mockBuilder1).build();
                will(returnValue(mockBlob1));

                allowing(mockBuilder2).contentLength(1L);
                will(returnValue(mockBuilder2));
                allowing(mockBuilder2).payload(with(any(ByteSource.class)));
                will(returnValue(mockBuilder2));
                oneOf(mockBuilder2).build();
                will(returnValue(mockBlob2));

                oneOf(mockBlobStore).putBlob(bucket, mockBlob1);
                oneOf(mockBlobStore).putBlob(bucket, mockBlob2);
                oneOf(mockBlobStoreContext).close();
            }
        });

        service.uploadJobFiles(job, mockFiles);
    }

    /**
     * Tests that requests for uploading single files successfully call all dependencies
     * @throws PortalServiceException
     */
    @Test
    public void testUploadSingleJobFile() throws PortalServiceException {
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        final Blob mockBlob1 = context.mock(Blob.class, "mockBlob1");

        final PayloadBlobBuilder mockBuilder1 = context.mock(PayloadBlobBuilder.class, "mockBuilder1");

        final InputStream mockInputStream = context.mock(InputStream.class);
        final String fileName = "file.name";

        context.checking(new Expectations() {
            {
                oneOf(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));

                oneOf(mockBlobStore).createContainerInLocation(null, bucket);
                will(returnValue(true));

                oneOf(mockBlobStore).blobBuilder(jobStorageBaseKey + "/" + fileName);
                will(returnValue(mockBuilder1));

                allowing(mockBuilder1).contentLength(1L);
                will(returnValue(mockBuilder1));
                allowing(mockBuilder1).payload(with(mockInputStream));
                will(returnValue(mockBuilder1));
                oneOf(mockBuilder1).build();
                will(returnValue(mockBlob1));

                oneOf(mockBlobStore).putBlob(bucket, mockBlob1);
                oneOf(mockBlobStoreContext).close();
            }
        });

        service.uploadJobFile(job, fileName, mockInputStream);
    }

    /**
     * Tests that requests for deleting files successfully call all dependencies
     * @throws PortalServiceException
     */
    @Test
    public void testDeleteJobFiles() throws PortalServiceException {
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        context.checking(new Expectations() {
            {
                allowing(mockBlobStoreContext).getBlobStore();
                will(returnValue(mockBlobStore));
                oneOf(mockBlobStore).deleteDirectory(bucket, jobStorageBaseKey);
                oneOf(mockBlobStoreContext).close();
            }
        });

        service.deleteJobFiles(job);
    }

    /**
     * Tests that no exceptions occur during base key generation edge cases
     */
    @Test
    public void testBaseKeyGeneration()  {
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
     */
    @Test
    public void testBaseKeyNoSubstrings() {
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

    @Test(expected=PortalServiceException.class)
    public void testStsRequired() throws PortalServiceException {
        CloudStorageServiceJClouds stsService = new CloudStorageServiceJClouds("dummy1", "dummy2", "dummy3");
        stsService.setStsRequirement(STSRequirement.Mandatory);
        stsService.getBlobStoreContext(null, null);
    }

}
