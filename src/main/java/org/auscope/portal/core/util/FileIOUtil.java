package org.auscope.portal.core.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.download.DownloadResponse;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class FileIOUtil {

    protected final static Log log = LogFactory.getLog(FileIOUtil.class);

    public static String convertExceptionToString(Throwable ex, String debugQuery) {
        StringWriter sw = null;
        PrintWriter pw = null;
        String message = "";
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            message = String.format("An exception occured.\r\n%1$s\r\nMessage=%2$s\r\n%3$s", debugQuery,
                    ex.getMessage(), sw.toString());
        } finally {
            try {
                if (pw != null)
                    pw.close();
                if (sw != null)
                    sw.close();
            } catch (Exception exception) {
                //Not really a big deal if we fail to close the writer and shouldn't happen
                //log a warning if it does.
                log.warn(exception);
            }
        }
        return message;
    }

    public static String convertStreamtoString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
    }

    /**
     * Utility function for closing a stream quietly (with no exceptions being raised)
     *
     * @param s
     *            The stream to close
     */
    public static void closeQuietly(Closeable s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException ioe) {
                log.warn(String.format("Error closing stream - %1$s", ioe));
                log.debug("Exception: ", ioe);
            }
        }
    }

    /**
     * Copies a file from source to destination.
     *
     * @param source
     *            Source file to copy
     * @param destination
     *            File to copy to
     *
     * @return true if file was successfully copied, false otherwise
     */
    public static boolean copyFile(File source, File destination) {
        boolean success = false;
        log.debug(source.getPath() + " -> " + destination.getPath());
        byte[] buffer = new byte[8192];
        int bytesRead;

        try (FileInputStream input = new FileInputStream(source);
             FileOutputStream output = new FileOutputStream(destination)) {
            while ((bytesRead = input.read(buffer)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
            success = true;
        } catch (IOException e) {
            log.warn("Could not copy file: " + e.getMessage());
        } 

        return success;
    }

    /**
     * Moves a file from source to destination. Uses copyFile to create a copy then deletes the source.
     *
     * @param source
     *            Source file
     * @param destination
     *            Destination file
     *
     * @return true if file was successfully moved, false otherwise
     */
    public static boolean moveFile(File source, File destination) {
        boolean success = copyFile(source, destination);
        if (success) {
            source.delete();
        }
        return success;
    }

    /**
     * Recursively copies the contents of a directory into the destination directory. Source and destination must represent the same type (file or directory).
     * In the case of files this method does the same as copyFile. If they are directories then source is recursively copied into destination. Destination may
     * not exist in this case and will be created.
     *
     * @param source
     *            Source file or directory to be copied
     * @param destination
     *            Destination file or directory
     *
     * @return true if contents were successfully copied, false otherwise
     */
    public static boolean copyFilesRecursive(File source, File destination) {
        boolean success = false;

        if (source.isDirectory()) {
            if (!destination.exists()) {
                if (!destination.mkdirs()) {
                    success = false;
                    return false;
                }
            }
            String files[] = source.list();

            for (int i = 0; i < files.length; i++) {
                File newSrc = new File(source, files[i]);
                File newDest = new File(destination, files[i]);
                success = copyFilesRecursive(newSrc, newDest);
                if (!success) {
                    break;
                }
            }
        } else {
            success = copyFile(source, destination);
        }

        return success;
    }

    /**
     * Recursively deletes a directory. If path represents a file it is deleted. If it represents a directory then its contents are deleted before the directory
     * itself is removed.
     *
     * Be very careful calling this function, specifying a root directory could wipe the drive (assuming correct permissions)
     *
     * @param path
     *            File or directory to delete.
     *
     * @return true if deletion was successful, false otherwise
     */
    public static boolean deleteFilesRecursive(File path) {
        boolean success = true;
        if (path.isDirectory()) {
            String files[] = path.list();

            // delete contents first
            for (int i = 0; i < files.length; i++) {
                File newPath = new File(path, files[i]);
                success = deleteFilesRecursive(newPath);
                if (!success) {
                    break;
                }
            }
        }

        if (success) {
            // delete path (whether it is a file or directory)
            success = path.delete();
            if (!success) {
                log.warn("Unable to delete " + path.getPath());
            }
        }

        return success;
    }

    /**
     * This util will allow us to write JSON/XML responses to zip file
     *
     * @param gmlDownloads
     *            - a list of DownloadResponse
     * @param zout
     *            - the ZipOutputStream to write the response
     * @throws IOException
     */
    public static void writeResponseToZip(ArrayList<DownloadResponse> gmlDownloads, ZipOutputStream zout) throws IOException {
        writeResponseJSONToZip(gmlDownloads, zout, null);
    }

    /**
     * This util will allow us to write JSON/XML responses to zip file
     *
     * @param extension File extension to apply to files in the zip (defaults to .xml)
     * @param gmlDownloads
     *            - a list of DownloadResponse
     * @param zout
     *            - the ZipOutputStream to write the response
     * @throws IOException
     */
    public static void writeResponseToZip(ArrayList<DownloadResponse> gmlDownloads, ZipOutputStream zout, String extension)
            throws IOException {
        //VT: this assume all files will be of the sme extension. With paging, we have .zip in the mix.
        //JV: Let's make sure we use the first actual content type in case the first download fails
        String firstValidContentType = null;
        for (DownloadResponse dr : gmlDownloads) {
            String ct = dr.getContentType();
            if (!StringUtils.isEmpty(ct)) {
                firstValidContentType = ct;
                break;
            }
        }

        if (firstValidContentType != null &&
           (firstValidContentType.contains("text") || firstValidContentType.contains("zip"))) {
            writeResponseToZip(gmlDownloads, zout, true, extension);
        } else { //VT: TODO: the different response type should be handled differently.
                 //VT: eg. handle application/json and a final catch all.
            log.warn("No content type found, defaulting to handling JSON responses");
            writeResponseJSONToZip(gmlDownloads, zout, extension);
        }
    }


    /**
     * Writes a series of DownloadResponse objects to a zip stream, each download response will be put into a separate zip entry. This are the same code as
     * those in BasePortalController
     *
     * @param gmlDownloads
     *            The download responses
     * @param zout
     *            The stream to receive the zip entries
     * @param closeInput
     *            true to close all the input stream in the gmlDownloads
     */
    public static void writeResponseToZip(List<DownloadResponse> gmlDownloads, ZipOutputStream zout, boolean closeInputs) throws IOException {
        writeResponseToZip(gmlDownloads, zout, closeInputs, null);
    }

    /**
     * Writes a series of DownloadResponse objects to a zip stream, each download response will be put into a separate zip entry. This are the same code as
     * those in BasePortalController
     *
     * @param extensionOverride The file extension to apply to files in the zip (defaults to MimeUtil.mimeToFileExtension based on content type)
     * @param gmlDownloads
     *            The download responses
     * @param zout
     *            The stream to receive the zip entries
     * @param closeInput
     *            true to close all the input stream in the gmlDownloads
     */
    public static void writeResponseToZip(List<DownloadResponse> gmlDownloads, ZipOutputStream zout, boolean closeInputs, String extensionOverride)
            throws IOException {
        for (int i = 0; i < gmlDownloads.size(); i++) {
            DownloadResponse download = gmlDownloads.get(i);

            URI downloadURI = null;
            try {
                downloadURI = new URI(download.getRequestURL());
            } catch (URISyntaxException e1) {
                throw new IOException(e1.getMessage(), e1);
            }

            String downloadDomain = downloadURI.getHost().replace(".","_");
            String extension = extensionOverride == null ? MimeUtil.mimeToFileExtension(gmlDownloads.get(i).getContentType()) : extensionOverride;

            if (extension.equals(".csv"))
                extension = "csv";

            String entryName = new SimpleDateFormat((i + 1) + "_yyyyMMdd_HHmmss").format(new Date()) + "_" + downloadDomain + "." + extension;
            //TODO: VT - this method can be further improved if we thread this method as we are processing each stream one by one.
            // Check that attempt to request is successful
            if (!download.hasException()) {
                @SuppressWarnings("resource") // closed in writeInputToOutputStream or intentionally left open
                InputStream stream = download.getResponseAsStream();

                //Write stream into the zip entry
                zout.putNextEntry(new ZipEntry(entryName));
                writeInputToOutputStream(stream, zout, 8 * 1024, closeInputs);
                zout.closeEntry();
            } else {
                writeErrorToZip(zout, download.getRequestURL(), download.getException(), entryName + ".error");
            }
        }
    }

    /**
     * Writes an error to a zip stream. This are the same code as those in BasePortalController
     *
     * @param zout
     *            the zout
     * @param debugQuery
     *            the debug query
     * @param exceptionToPrint
     *            the exception to print
     * @param errorFileName
     *            The name of the error file in the zip (defaults to 'error.txt')
     */
    public static void writeErrorToZip(ZipOutputStream zout, String debugQuery, Throwable exceptionToPrint,
            String errorFileName) {
        String message = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            exceptionToPrint.printStackTrace(pw);
            message = String.format(
                    "An exception occured whilst requesting/parsing your download.\r\n%1$s\r\nMessage=%2$s\r\n%3$s",
                    debugQuery, exceptionToPrint.getMessage(), sw.toString());
        } finally {
            FileIOUtil.closeQuietly(pw);
            FileIOUtil.closeQuietly(sw);
        }

        try {
            zout.putNextEntry(new ZipEntry(errorFileName == null ? errorFileName : "error.txt"));
            zout.write(message.getBytes());
        } catch (IOException ex) {
            log.error("Couldnt create debug error.txt in output", ex);
        }
    }

    /**
     * Writes output to input via an in memory buffer of a certain size This are the same code as those in BasePortalController
     *
     * @param input
     *            The input stream
     * @param output
     *            The output stream (will receive input's bytes)
     * @param bufferSize
     *            The size (in bytes) of the in memory buffer
     * @param closeInput
     *            if true, the input will be closed prior to this method returning
     * @throws IOException
     */
    public static void writeInputToOutputStream(InputStream input, OutputStream output, int bufferSize,
            boolean closeInput) throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int dataRead;
            do {
                dataRead = input.read(buffer, 0, buffer.length);
                if (dataRead > 0) {
                    output.write(buffer, 0, dataRead);
                }
            } while (dataRead != -1);
        } catch (Exception e) {
            writeExceptionToXMLStream(e, output, false);
        } finally {
            if (closeInput) {
                FileIOUtil.closeQuietly(input);
            }
        }
    }

    /**
     * This util will allow us to write JSON responses to zip file
     *
     * @param gmlDownloads
     *            - a list of DownloadResponse
     * @param zout
     *            - the ZipOutputStream to write the response
     * @throws IOException
     */
    public static void writeResponseJSONToZip(ArrayList<DownloadResponse> gmlDownloads, ZipOutputStream zout)
            throws IOException {
        writeResponseJSONToZip(gmlDownloads, zout, ".xml");
    }

    /**
     * This util will allow us to write JSON responses to zip file
     *
     * @param extension File extension to apply to files inside the ZIP (defaults to .xml)
     * @param gmlDownloads
     *            - a list of DownloadResponse
     * @param zout
     *            - the ZipOutputStream to write the response
     * @throws IOException
     */
    public static void writeResponseJSONToZip(ArrayList<DownloadResponse> gmlDownloads, ZipOutputStream zout, String extension)
            throws IOException {
        StringBuilder errorMsg = new StringBuilder();

        for (int i = 0; i < gmlDownloads.size(); i++) {
            DownloadResponse download = gmlDownloads.get(i);
            //Check that attempt to request is successful
            if (!download.hasException()) {
                JSONObject jsonObject = JSONObject.fromObject(download.getResponseAsString());
                //check that JSON reply is successful
                if (jsonObject.get("success").toString().equals("false")) {
                    errorMsg.append("Unsuccessful JSON reply from: " + download.getRequestURL() + "\n");

                    Object messageObject = jsonObject.get("msg");
                    if (messageObject == null || messageObject.toString().length() == 0) {
                        errorMsg.append("No error message\n\n");
                    } else {
                        errorMsg.append(messageObject.toString() + "\n\n");
                    }
                } else {
                    byte[] gmlBytes = new byte[] {};
                    Object dataObject = jsonObject.get("data");
                    if (dataObject != null && !JSONNull.getInstance().equals(dataObject)) {
                        JSONObject dataObjectJson = JSONObject.fromObject(dataObject);
                        Iterator<?> children = dataObjectJson.keys();
                        if (children.hasNext()) {
                            String firstChild = children.next().toString();
                            gmlBytes = dataObjectJson.get(firstChild).toString().getBytes();
                        }
                    }

                    URI downloadURI = null;
                    try {
                        downloadURI = new URI(download.getRequestURL());
                    } catch (URISyntaxException e1) {
                        throw new IOException(e1.getMessage(), e1);
                    }

                    String downloadDomain = downloadURI.getHost().replace(".","_");
                    zout.putNextEntry(new ZipEntry(new SimpleDateFormat(
                            (i + 1) + "_yyyyMMdd_HHmmss").format(new Date())
                            + "_" + downloadDomain
                            + (extension == null ? ".xml" : "." + extension)));
                    zout.write(gmlBytes);
                    zout.closeEntry();
                }

            } else {
                errorMsg.append("Exception thrown while attempting to download from: " + download.getRequestURL()
                        + "\n");
                errorMsg.append(download.getExceptionAsString() + "\n\n");
            }
        }
        if (errorMsg.length() != 0) {
            zout.putNextEntry(new ZipEntry("downloadInfo.txt"));
            zout.write(errorMsg.toString().getBytes());
            zout.closeEntry();
        }
    }
    
    /**
     * Get the absolute url of a os specific temp directory 
     * @return absolute url of a temp directory
     */
	public static String getTempDirURL() {
		String tempdir = System.getProperty("java.io.tmpdir");

		if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
			tempdir = tempdir + System.getProperty("file.separator");
		}
		return tempdir;
	}

    /**
     * VT: Have to think of a better way to handle exception rather then just encapsulating the error in xml
     *
     * @param e
     * @param out
     * @param closeStream
     */
    public static void writeExceptionToXMLStream(Exception e, OutputStream out, boolean closeStream) throws IOException {
        writeExceptionToXMLStream(e, out, closeStream, "");
    }

    /**
     * VT: Have to think of a better way to handle exception rather then just encapsulating the error in xml
     *
     * @param e
     * @param out
     * @param closeStream
     */
    public static void writeExceptionToXMLStream(Exception e, OutputStream out, boolean closeStream,
            String additionalMsg) throws IOException {
        String stack = ExceptionUtils.getStackTrace(e);
        String error = "<StackTrace>" + additionalMsg + "\n" + stack + "</StackTrace>";
        try {
            out.write(error.getBytes());
        } catch (IOException e1) {
            log.error(e1.getCause());
            throw e1;
        } finally {
            if (closeStream) {
                out.close();
            }
        }
    }

    public static File writeStreamToFile(InputStream ins, File f, boolean closeIns) throws IOException {
        BufferedOutputStream out = null;

        try {
            out = new BufferedOutputStream(new FileOutputStream(f));
            FileIOUtil.writeInputToOutputStream(ins, out, 8 * 1024, false);
            out.flush();
            out.close();

            //VT: After we have finish writing the stream to file we want to return it.
            log.info(f.getName() + " : Complete writing of temporary file");
            return f;

        } catch (IOException e) {
            throw e;
        } finally {
            if(out != null)
                out.close();
            if (closeIns) {
                ins.close();
            }
        }
    }

    public static File writeStreamToFileTemporary(InputStream ins, String identifier, String fileSuffix,
            boolean closeIns) throws IOException {
        BufferedOutputStream out = null;
        File f = null;
        try {
            f = File.createTempFile(identifier, fileSuffix);
            out = new BufferedOutputStream(new FileOutputStream(f));
            FileIOUtil.writeInputToOutputStream(ins, out, 8 * 1024, false);
            out.flush();
            out.close();

            //VT: After we have finish writing the stream to file we want to return it.
            log.info(f.getName() + " : Complete writing of temporary file");
            return f;

        } catch (IOException e) {
            throw e;
        } finally {
            if(out !=null)
                out.close();
            if (closeIns) {
                ins.close();
            }
        }
    }

}
