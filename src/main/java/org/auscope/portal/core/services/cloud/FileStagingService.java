package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.cloud.StagedFileOwner;
import org.auscope.portal.core.cloud.StagingInformation;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.FileIOUtil;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * A service class for handling file uploads and storing them in a local staging directory
 * 
 * @author Josh Vote
 *
 */
public class FileStagingService {
    private final Log logger = LogFactory.getLog(getClass());
    protected StagingInformation stagingInformation;

    public FileStagingService(StagingInformation stagingInformation) {
        this.stagingInformation = stagingInformation;
    }

    /**
     * Utility for returning a File handle to the actual file on HDD for a given job + fileName
     * 
     * @param job
     * @param fileName
     * @return
     */
    private File getFile(StagedFileOwner job, String fileName) {
        if (fileName.contains(File.pathSeparator) || fileName.contains(File.separator)) {
            throw new IllegalArgumentException("fileName cannot include " + File.pathSeparator + " or "
                    + File.separator);
        }

        String directory = pathConcat(stagingInformation.getStageInDirectory(), getBaseFolderForJob(job));
        return new File(pathConcat(directory, fileName));
    }

    /**
     * Given 2 path components this function will concatenate them together.
     *
     * The result will ensure that you will not end up with 2 consecutive File.pathSeperator characters at the place of the concatenation.
     * 
     * @param p1
     * @param p2
     * @return
     */
    public static String pathConcat(String p1, String p2) {
        if (p1.endsWith(File.separator) && p2.startsWith(File.separator)) {
            return p1 + p2.substring(1);
        } else if ((!p1.endsWith(File.separator) && p2.startsWith(File.separator)) ||
                (p1.endsWith(File.separator) && !p2.startsWith(File.separator))) {
            return p1 + p2;
        } else {
            return p1 + File.separator + p2;
        }
    }

    /**
     * Generates a base folder name based on job ID. Basic attempt is made to sanitise input, No guarantees are made for security.
     * 
     * @param job
     * @return
     */
    public static String getBaseFolderForJob(StagedFileOwner job) {
        return String.format("job-%1$s", job.getId());
    }

    /**
     * Deletes the entire job stage in directory, returns true on success. It silently fails and log the failure message to error log if the operation failed.
     * 
     * @param job
     *            Must have its fileStorageId parameter set
     */
    public boolean deleteStageInDirectory(StagedFileOwner job) {
        boolean status = false;
        try {
            File jobInputDir = new File(pathConcat(stagingInformation.getStageInDirectory(), getBaseFolderForJob(job)));
            logger.debug("Recursively deleting " + jobInputDir.getPath());
            if (!jobInputDir.exists()) {
                status = true;
            }
            status = FileIOUtil.deleteFilesRecursive(jobInputDir);
        } catch (Exception ex) {
            logger.warn("There was a problem wiping the stage in directory for job: " + job, ex);
        }
        return status;
    }

    /**
     * Deletes a specific file from the job stage in directory.
     * 
     * @param job
     *            Must have its fileStorageId parameter set
     * @param fileName
     * @return
     */
    public boolean deleteStageInFile(StagedFileOwner job, String fileName) {
        File file = getFile(job, fileName);
        logger.debug("deleting " + file.getPath());
        if (!file.exists()) {
            return true;
        }

        return file.delete();
    }

    /**
     * Renames a specific file in the job stage in directory. If currentFileName DNE, this will return false. If newFileName exists, it will be deleted first.
     * if currentFileName equals newFileName this will return true and perform no operations.
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @param currentFileName
     *            The file to be renamed
     * @param newFileName
     *            The new file name (if it exists, it will be overwritten)
     * @return
     */
    public boolean renameStageInFile(StagedFileOwner job, String currentFileName, String newFileName) {
        File currentFile = getFile(job, currentFileName);
        File newFile = getFile(job, newFileName);

        //In case we rename something into itself
        if (newFile.getAbsolutePath().equals(currentFile.getAbsolutePath())) {
            return true;
        }

        if (!currentFile.exists()) {
            return false;
        }

        if (newFile.exists()) {
            newFile.delete();
        }

        logger.debug("renaming " + currentFile.getPath() + " to " + newFile.getPath());
        return currentFile.renameTo(newFile);
    }

    /**
     * Returns true if the specified file in the job staging area exists. False otherwise.
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @param currentFileName
     *            The file to be checked
     * @return
     */
    public boolean stageInFileExists(StagedFileOwner job, String fileName) {
        return getFile(job, fileName).exists();
    }

    /**
     * Given a job see if a folder for the internal staging area has been created.
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @return
     * @throws IOException
     *             If the directory creation fails
     */
    public boolean stageInDirectoryExists(StagedFileOwner job) {
        String jobInputDir = pathConcat(stagingInformation.getStageInDirectory(), getBaseFolderForJob(job));
        return new File(jobInputDir).exists();
    }

    /**
     * Given a job create a folder that is unique to that job in the internal staging area.
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @return
     * @throws IOException
     *             If the directory creation fails
     */
    public void generateStageInDirectory(StagedFileOwner job) throws PortalServiceException {
        String jobInputDir = pathConcat(stagingInformation.getStageInDirectory(), getBaseFolderForJob(job));

        logger.debug("Attempting to generate job input dir " + jobInputDir);

        boolean success = new File(jobInputDir).mkdirs();
        if (!success) {
            throw new PortalServiceException("Failed to create stage in directory: " + jobInputDir);
        }
    }

    /**
     * Lists every file in the specified job's stage in directory
     * 
     * @param job
     *            Must have its fileStorageId parameter set
     * @return
     * @throws IOException
     */
    public StagedFile[] listStageInDirectoryFiles(StagedFileOwner job) throws PortalServiceException {
        //List files in directory, add them to array
        File directory = new File(pathConcat(stagingInformation.getStageInDirectory(), getBaseFolderForJob(job)));
        logger.debug("Attempting to list files at " + directory.getPath());
        if (!directory.isDirectory()) {
            throw new PortalServiceException("Not a directory: " + directory.getPath());
        }
        File[] files = directory.listFiles();
        if (files == null) {
            throw new PortalServiceException("Unable to list files in: " + directory.getPath(), "");
        }

        StagedFile[] stagedFiles = new StagedFile[files.length];
        for (int i = 0; i < stagedFiles.length; i++) {
            stagedFiles[i] = new StagedFile(job, files[i].getName(), files[i]);
        }

        return stagedFiles;
    }

    /**
     * Opens the specified staging file for reading
     *
     * The returned stream must be closed when finished with
     * 
     * @param stagedFile
     * @return
     * @throws PortalServiceException
     */
    public InputStream readFile(StagedFile stagedFile) throws PortalServiceException {
        return this.readFile(stagedFile.getOwner(), stagedFile.getName());
    }

    /**
     * Opens the specified staging file for reading
     *
     * The returned stream must be closed when finished with
     * 
     * @param job
     *            Must have its fileStorageId parameter set
     * @param fileName
     * @return the staging file input stream if the file exists otherwise null
     */
    public InputStream readFile(StagedFileOwner job, String fileName) throws PortalServiceException {
        try {
            File f = getFile(job, fileName);
            FileInputStream fis = null;
            if (f.exists()) {
                fis = new FileInputStream(f);
            }
            return fis;
        } catch (Exception e) {
            throw new PortalServiceException(e.getMessage(), e);
        }
    }

    /**
     * Opens the specified staging file for writing. If it DNE, it will be created
     *
     * The returned stream must be closed when finished with
     * 
     * @param stagedFile
     * @return
     * @throws PortalServiceException
     */
    public OutputStream writeFile(StagedFile stagedFile) throws PortalServiceException {
        return writeFile(stagedFile.getOwner(), stagedFile.getName(), false);
    }

    /**
     * Opens the specified staging file for writing, If it DNE, it will be created
     *
     * The returned stream must be closed when finished with
     * 
     * @param job
     * @param fileName
     * @return
     */
    public OutputStream writeFile(StagedFileOwner job, String fileName) throws PortalServiceException {
        return writeFile(job, fileName, false);
    }

    /**
     * Opens the specified staging file for writing or appending. If it DNE, it will be created.
     *
     * The returned stream must be closed when finished with
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @param fileName
     * @param append
     *            Should the file be overwritten or appended to. true to append, false to overwrite
     * @return
     */
    public OutputStream writeFile(StagedFileOwner job, String fileName, boolean append) throws PortalServiceException {
        File f = getFile(job, fileName);
        try {
            return new FileOutputStream(f, append);
        } catch (Exception e) {
            throw new PortalServiceException(e.getMessage(), e);
        }
    }

    /**
     * Upload files to the staging directory of the specified job. Helper function for code deduplication
     *
     * @param job
     * @param f
     * @return
     * @throws PortalServiceException
     */
    private StagedFile fileUploadHelper(StagedFileOwner job, MultipartFile f)
            throws PortalServiceException {
        String originalFileName = f.getOriginalFilename();
        String directory = pathConcat(stagingInformation.getStageInDirectory(), getBaseFolderForJob(job));
        String destinationPath = pathConcat(directory, originalFileName);
        logger.debug("Saving uploaded file to " + destinationPath);

        File destination = new File(destinationPath);
        if (destination.exists()) {
            logger.debug("Will overwrite existing file.");
        }

        try {
            f.transferTo(destination);
        } catch (Exception ex) {
            logger.error("Failure during transfer:" + ex.getMessage());
            logger.debug("error:", ex);
            throw new PortalServiceException("Failure during transfer", ex);
        }

        return new StagedFile(job, originalFileName, destination);
    }

    /**
     * Given a MultipartHttpServletRequest with an internal file parameter, write that file to the staging directory of the specified job
     *
     * returns a FileInfo object describing the file on the file system
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @param request
     * @throws PortalServiceException
     */
    public StagedFile handleFileUpload(StagedFileOwner job, MultipartHttpServletRequest request)
            throws PortalServiceException {
        MultipartFile f = request.getFile("file");
        if (f == null) {
            throw new PortalServiceException("No file parameter provided.");
        }

        return fileUploadHelper(job, f);
    }

    /**
     * Given a MultipartHttpServletRequest with an internal file parameter for several files, write that files to the staging directory of the specified job
     *
     * returns a List of FileInfo objects describing the file on the file system
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @param request
     * @throws IOException
     */
    public List<StagedFile> handleMultiFileUpload(StagedFileOwner job, MultipartHttpServletRequest request)
            throws PortalServiceException {

        List<StagedFile> result = new ArrayList<>();

        List<MultipartFile> files = request.getFiles("file");
        if (files == null) {
            throw new PortalServiceException("No file parameter provided.");
        }

        if (files.size() > 0) {
            for (MultipartFile multipartFile : files) {
                result.add(fileUploadHelper(job, multipartFile));
            }
        } else {
            throw new PortalServiceException("No file parameter provided.");
        }

        return result;
    }

    /**
     * This function will attempt to download fileName from job's staging directory by writing directly to the output stream of response.
     *
     * response will have its internal outputStream directly accessed and written to (if the internal file request is successful).
     *
     * @param stagedFile
     *            Must have owner and name set
     * @throws IOException
     */
    public void handleFileDownload(StagedFile stagedFile, HttpServletResponse response) throws PortalServiceException {
        handleFileDownload(stagedFile.getOwner(), stagedFile.getName(), response);
    }

    /**
     * This function will attempt to download fileName from job's staging directory by writing directly to the output stream of response.
     *
     * response will have its internal outputStream directly accessed and written to (if the internal file request is successful).
     *
     * @param job
     *            Must have its fileStorageId parameter set
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public void handleFileDownload(StagedFileOwner job, String fileName, HttpServletResponse response)
            throws PortalServiceException {
        String directory = pathConcat(stagingInformation.getStageInDirectory(), getBaseFolderForJob(job));
        String filePath = pathConcat(directory, fileName);

        logger.debug("Downloading: " + filePath);

        //Simple sanity check
        File f = new File(filePath);
        if (!f.canRead()) {
            throw new PortalServiceException("File " + f.getPath() + " not readable!");
        }

        //Start configuring our response for a download stream
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"");

        //Then push all data down
        byte[] buffer = new byte[4096];
        int count = 0;
        OutputStream out = null;
        FileInputStream fin = null;
        try {
            out = response.getOutputStream();
            fin = new FileInputStream(f);
            while ((count = fin.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            out.flush();
        } catch (IOException ex) {
            logger.error("Failure during download:" + ex.getMessage());
            logger.debug("error:", ex);
            throw new PortalServiceException("Failure during transfer", ex);
        } finally {
            FileIOUtil.closeQuietly(fin);
        }
    }
}
