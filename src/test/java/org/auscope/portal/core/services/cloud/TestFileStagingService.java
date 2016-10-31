package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.cloud.StagingInformation;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.jmock.ReadableServletOutputStream;
import org.auscope.portal.core.util.FileIOUtil;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Unit tests for JobFileService
 * 
 * @author Josh Vote
 *
 */
public class TestFileStagingService extends PortalTestClass {

    private static StagingInformation testStagingInfo;
    private static int testCounter = 0;

    private FileStagingService service;
    private CloudJob job;

    /**
     * This sets up a temporary directory in the target directory for the JobFileService to utilise as a staging area
     */
    @BeforeClass
    public static void setup() {
        testStagingInfo = new StagingInformation(String.format(
                "target%1$sTestJobFileService-%2$s%1$s",
                File.separator, new Date().getTime()));

        File dir = new File(testStagingInfo.getStageInDirectory());
        Assert.assertTrue("Failed setting up staging directory", dir.mkdirs());
    }

    /**
     * This tears down the staging area used by the tests
     */
    @AfterClass
    public static void tearDown() {
        File dir = new File(testStagingInfo.getStageInDirectory());
        FileIOUtil.deleteFilesRecursive(dir);

        //Ensure cleanup succeeded
        Assert.assertFalse(dir.exists());
    }

    /**
     * Creates a fresh job object for each unit test (with a unique fileStorageID).
     */
    @Before
    public void setupJobObj() {
        job = new CloudJob(testCounter++);

        service = new FileStagingService(testStagingInfo);
    }

    /**
     * Asserts that the shared staging directory (not the job staging directory) still exists after a unit test is run
     */
    @After
    public void ensureStagingDirectoryPreserved() {
        File dir = new File(testStagingInfo.getStageInDirectory());
        Assert.assertTrue(dir.exists());
        Assert.assertTrue(dir.isDirectory());
    }

    /**
     * Tests for the pathConcat utility method
     */
    @Test
    public void testPathConcat() {
        Assert.assertEquals("p1" + File.separator + "p2", FileStagingService.pathConcat("p1", "p2"));
        Assert.assertEquals("p1" + File.separator + "p2", FileStagingService.pathConcat("p1" + File.separator, "p2"));
        Assert.assertEquals("p1" + File.separator + "p2", FileStagingService.pathConcat("p1", File.separator + "p2"));
        Assert.assertEquals("p1" + File.separator + "p2",
                FileStagingService.pathConcat("p1" + File.separator, File.separator + "p2"));
    }

    /**
     * Tests the existence/nonexistence of job's stage in directory
     * 
     * @param job
     * @param exists
     */
    private static void assertStagedDirectory(CloudJob job, boolean exists) {
        File stageInDir = new File(FileStagingService.pathConcat(testStagingInfo.getStageInDirectory(),
                FileStagingService.getBaseFolderForJob(job)));
        Assert.assertEquals(exists, stageInDir.exists());
        if (exists) {
            Assert.assertEquals(true, stageInDir.isDirectory());
        }
    }

    /**
     * Tests the existence/nonexistence of job's stage in file
     * 
     * @param job
     * @param fileName
     * @param exists
     * @throws IOException 
     */
    private static void assertStagedFile(CloudJob job, String fileName, boolean exists) throws IOException  {
        assertStagedFile(job, fileName, exists, null);
    }

    /**
     * Tests the existence/nonexistence of job's stage in file as well as its contents
     * 
     * @param job
     * @param exists
     * @param expectedData
     *            if not null and file exists, file data will be tested against this value
     * @throws IOException 
     */
    private static void assertStagedFile(CloudJob job, String fileName, boolean exists, byte[] expectedData) throws IOException  {
        String stageInDir = FileStagingService.pathConcat(testStagingInfo.getStageInDirectory(),
                FileStagingService.getBaseFolderForJob(job));
        File stageInFile = new File(FileStagingService.pathConcat(stageInDir, fileName));

        Assert.assertEquals(exists, stageInFile.exists());
        if (exists) {
            Assert.assertEquals(true, stageInFile.isFile());

            //Test file contents
            if (expectedData != null) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(stageInFile);
                    byte[] actualData = IOUtils.toByteArray(fis);
                    Assert.assertArrayEquals(expectedData, actualData);
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }

        }
    }

    /**
     * Tests that creating/deleting an empty job staging area works
     * @throws PortalServiceException 
     */
    @Test
    public void testEmptyStartupTeardown() throws PortalServiceException  {
        service.generateStageInDirectory(job);

        assertStagedDirectory(job, true);

        service.deleteStageInDirectory(job);

        assertStagedDirectory(job, false);
    }

    /**
     * Tests that creating and listing files in a job staging area works
     * @throws PortalServiceException 
     * 
     * @throws IOException
     */
    @Test
    public void testFileCreationAndListing() throws PortalServiceException, IOException  {
        service.generateStageInDirectory(job);

        final byte[] file1Data = new byte[] {1, 2, 3};
        final byte[] file2Data = new byte[] {4, 3, 1};

        OutputStream file1 = service.writeFile(job, "testFile1");
        OutputStream file2 = service.writeFile(job, "testFile2");

        file1.write(file1Data);
        file2.write(file2Data);
        file1.close();
        file2.close();

        assertStagedDirectory(job, true);
        assertStagedFile(job, "testFile1", true, file1Data);
        assertStagedFile(job, "testFile2", true, file2Data);

        //Ensure that listing returns all the files (in no particular order)
        StagedFile[] expectedFiles = new StagedFile[] {new StagedFile(job, "testFile1", null),
                new StagedFile(job, "testFile2", null)};
        StagedFile[] listedFiles = service.listStageInDirectoryFiles(job);
        Assert.assertNotNull(listedFiles);
        Assert.assertEquals(expectedFiles.length, listedFiles.length);
        for (StagedFile expectedFile : expectedFiles) {
            boolean foundFile = false;
            for (StagedFile listedFile : listedFiles) {
                if (listedFile.equals(expectedFile)) {
                    Assert.assertNotNull("File reference in StagedFile not set!", listedFile.getFile());
                    Assert.assertEquals(job, listedFile.getOwner());
                    foundFile = true;
                    break;
                }
            }

            Assert.assertTrue(String.format("File '%1$s' not listed", expectedFile), foundFile);
        }

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "testFile2", false);
    }

    /**
     * Tests that using relative paths in a filename will generate exceptions
     * @throws PortalServiceException 
     * 
     * @throws IOException
     */
    @Test
    public void testBadFilenames() throws PortalServiceException  {
        service.generateStageInDirectory(job);

        //Should either return null or throw exception
        try (OutputStream file = service.writeFile(job, FileStagingService.pathConcat("..", "testFile1"))) {
            Assert.assertNull(file);
        } catch (Exception ex) {
            // empty
        }
        try (OutputStream file = service.writeFile(job, "testFile1" + File.pathSeparator + "testFile2")) {
            Assert.assertNull(file);
        } catch (Exception ex) {
            // empty
        }
        try (InputStream file = service.readFile(job, FileStagingService.pathConcat("..", "testFile1"))) {
            Assert.assertNull(file);
        } catch (Exception ex) {
            // empty
        }
        try (InputStream file = service.readFile(job, "testFile1" + File.pathSeparator + "testFile2")) {
            Assert.assertNull(file);
        } catch (Exception ex) {
            // empty
        }

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
    }

    /**
     * Tests that file uploads can be handled
     * @throws IOException 
     * @throws PortalServiceException 
     */
    @Test
    public void testFileUpload() throws IOException, PortalServiceException  {
        final MultipartHttpServletRequest request = context.mock(MultipartHttpServletRequest.class);
        final MultipartFile file = context.mock(MultipartFile.class);
        final String fileName = "myFileName";

        context.checking(new Expectations() {
            {
                oneOf(request).getFile("file");
                will(returnValue(file));
                oneOf(file).getOriginalFilename();
                will(returnValue(fileName));
                oneOf(file).transferTo(with(aFileWithName(fileName)));
            }
        });

        service.generateStageInDirectory(job);

        //"Upload" the file and check it gets created
        StagedFile newlyStagedFile = service.handleFileUpload(job, request);
        Assert.assertNotNull(newlyStagedFile);
        Assert.assertNotNull("File reference not set!", newlyStagedFile.getFile());
        Assert.assertEquals(job, newlyStagedFile.getOwner());
        Assert.assertEquals(fileName, newlyStagedFile.getName());

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
    }

    /**
     * Tests that multi-file uploads can be handled
     * @throws IOException 
     * @throws PortalServiceException 
     */
    @Test
    public void testMultiFileUpload() throws IOException, PortalServiceException  {
        final MultipartHttpServletRequest request = context.mock(MultipartHttpServletRequest.class);
        final MultipartFile file = context.mock(MultipartFile.class);
        final String fileName = "myFileName";
        final ArrayList<MultipartFile> files = new ArrayList<>();
        files.add(file);
        files.add(file);

        context.checking(new Expectations() {
            {
                oneOf(request).getFiles("file");
                will(returnValue(files));
                exactly(2).of(file).getOriginalFilename();
                will(returnValue(fileName));
                exactly(2).of(file).transferTo(with(aFileWithName(fileName)));
            }
        });

        service.generateStageInDirectory(job);

        //"Upload" the file and check it gets created
        List<StagedFile> newlyStagedFiles = service.handleMultiFileUpload(job, request);
        Assert.assertNotNull(newlyStagedFiles);
        Assert.assertEquals(files.size(), newlyStagedFiles.size());
        Assert.assertEquals(job, newlyStagedFiles.get(0).getOwner());

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
    }

    /**
     * Tests that file downloads are handled correctly
     * @throws IOException 
     * @throws PortalServiceException 
     */
    @Test
    public void testFileDownload() throws IOException, PortalServiceException  {
        try (final ReadableServletOutputStream outStream = new ReadableServletOutputStream()) {
            final byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 8, 5, 8, 9, 9, 9, 91, 1, 1 };
            final HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
            final String fileName = "myFileName";

            // Start by creating our file that we want to download
            service.generateStageInDirectory(job);
            OutputStream fos = service.writeFile(job, fileName);
            fos.write(data);
            fos.close();

            context.checking(new Expectations() {
                {
                    // This is so we can inject our own fake output stream so we
                    // can inspect the result
                    oneOf(mockResponse).getOutputStream();
                    will(returnValue(outStream));
                    oneOf(mockResponse).setContentType("application/octet-stream");
                    allowing(mockResponse).setHeader("Content-Disposition",
                            "attachment; filename=\"" + fileName + "\"");
                }
            });

            // 'Download' the file
            service.handleFileDownload(job, fileName, mockResponse);

            // Inspect the data we downloaded
            Assert.assertArrayEquals(data, outStream.getDataWritten());
        }
        
        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
    }

    /**
     * Tests that creating and renaming files in a job staging area works
     * @throws PortalServiceException 
     * 
     * @throws IOException
     */
    @Test
    public void testFileRenaming() throws PortalServiceException, IOException  {
        service.generateStageInDirectory(job);

        final byte[] file1Data = new byte[] {1, 2, 3};

        OutputStream file1 = service.writeFile(job, "testFile1");
        file1.write(file1Data);
        file1.close();

        service.renameStageInFile(job, "testFile1", "renamedTestFile1");

        assertStagedDirectory(job, true);
        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "renamedTestFile1", true, file1Data);

        //Ensure that listing returns all the files (in no particular order)
        StagedFile[] expectedFiles = new StagedFile[] {new StagedFile(job, "renamedTestFile1", null)};
        StagedFile[] listedFiles = service.listStageInDirectoryFiles(job);
        Assert.assertNotNull(listedFiles);
        Assert.assertEquals(expectedFiles.length, listedFiles.length);
        for (StagedFile expectedFile : expectedFiles) {
            boolean foundFile = false;
            for (StagedFile listedFile : listedFiles) {
                if (listedFile.equals(expectedFile)) {
                    Assert.assertNotNull("File reference in StagedFile not set!", listedFile.getFile());
                    Assert.assertEquals(job, listedFile.getOwner());
                    foundFile = true;
                    break;
                }
            }

            Assert.assertTrue(String.format("File '%1$s' not listed", expectedFile), foundFile);
        }

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "renamedTestFile1", false);
    }

    /**
     * Tests that creating and renaming files in a job staging area works when the target file already exists
     * 
     * @throws IOException
     * @throws PortalServiceException 
     */
    @Test
    public void testFileRenamingOverwrite() throws IOException, PortalServiceException  {
        service.generateStageInDirectory(job);

        final byte[] file1Data = new byte[] {1, 2, 3};
        final byte[] file2Data = new byte[] {4, 3, 1};

        OutputStream file1 = service.writeFile(job, "testFile1");
        OutputStream file2 = service.writeFile(job, "testFile2");

        file1.write(file1Data);
        file2.write(file2Data);
        file1.close();
        file2.close();

        assertStagedDirectory(job, true);
        assertStagedFile(job, "testFile1", true, file1Data);
        assertStagedFile(job, "testFile2", true, file2Data);

        Assert.assertTrue(service.renameStageInFile(job, "testFile1", "testFile2"));

        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "testFile2", true, file1Data);

        Assert.assertFalse(service.renameStageInFile(job, "testFile1", "testFile2"));

        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "testFile2", true, file1Data);

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "testFile2", false);
    }

    /**
     * Tests that renaming file to itself does nothing
     * 
     * @throws IOException
     * @throws PortalServiceException 
     */
    @Test
    public void testFileRenamingSameFile() throws IOException, PortalServiceException  {
        service.generateStageInDirectory(job);

        final byte[] file1Data = new byte[] {1, 2, 3};

        OutputStream file1 = service.writeFile(job, "testFile1");

        file1.write(file1Data);
        file1.close();

        assertStagedDirectory(job, true);
        assertStagedFile(job, "testFile1", true, file1Data);

        Assert.assertTrue(service.renameStageInFile(job, "testFile1", "testFile1"));

        assertStagedFile(job, "testFile1", true, file1Data);

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
        assertStagedFile(job, "testFile1", false);
    }

    /**
     * Tests that creating and renaming files in a job staging area works when the target file already exists
     * @throws PortalServiceException 
     * 
     * @throws IOException
     */
    @Test
    public void testFileExists() throws PortalServiceException, IOException  {
        service.generateStageInDirectory(job);

        final byte[] file1Data = new byte[] {1, 2, 3};
        final byte[] file2Data = new byte[] {4, 3, 1};

        OutputStream file1 = service.writeFile(job, "testFile1");
        OutputStream file2 = service.writeFile(job, "testFile2");

        file1.write(file1Data);
        file2.write(file2Data);
        file1.close();
        file2.close();

        assertStagedDirectory(job, true);
        assertStagedFile(job, "testFile1", true, file1Data);
        assertStagedFile(job, "testFile2", true, file2Data);

        Assert.assertTrue(service.stageInFileExists(job, "testFile1"));
        Assert.assertTrue(service.stageInFileExists(job, "testFile2"));
        Assert.assertFalse(service.stageInFileExists(job, "fileDNE"));

        service.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "testFile2", false);

        Assert.assertFalse(service.stageInFileExists(job, "testFile1"));
        Assert.assertFalse(service.stageInFileExists(job, "testFile2"));
        Assert.assertFalse(service.stageInFileExists(job, "fileDNE"));
    }

    /**
     * Tests that creating a job staging works and that it can be detected
     * @throws PortalServiceException 
     * 
     * @throws IOException
     */
    @Test
    public void testStagingDirectoryExists() throws PortalServiceException  {
        service.generateStageInDirectory(job);

        assertStagedDirectory(job, true);
        Assert.assertTrue(service.stageInDirectoryExists(job));
        service.deleteStageInDirectory(job);

        assertStagedDirectory(job, false);
        Assert.assertFalse(service.stageInDirectoryExists(job));
    }

}
