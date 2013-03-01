package org.auscope.portal.core.util;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.download.DownloadResponse;

public class FileIOUtil {

    protected final static Log log = LogFactory.getLog(FileIOUtil.class);

    public static String convertExceptionToString(Exception ex,String debugQuery) {
        StringWriter sw = null;
        PrintWriter pw = null;
        String message="";
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            message = String.format("An exception occured.\r\n%1$s\r\nMessage=%2$s\r\n%3$s",debugQuery, ex.getMessage(), sw.toString());
        } finally {
            try {
                if (pw != null)  pw.close();
                if (sw != null)  sw.close();
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
     * @param s The stream to close
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
     * @param source Source file to copy
     * @param destination File to copy to
     *
     * @return true if file was successfully copied, false otherwise
     */
    public static boolean copyFile(File source, File destination) {
        boolean success = false;
        log.debug(source.getPath()+" -> "+destination.getPath());
        FileInputStream input = null;
        FileOutputStream output = null;
        byte[] buffer = new byte[8192];
        int bytesRead;

        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(destination);
            while ((bytesRead = input.read(buffer)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
            success = true;

        } catch (IOException e) {
            log.warn("Could not copy file: "+e.getMessage());

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {}
            }
        }

        return success;
    }

    /**
     * Moves a file from source to destination. Uses copyFile to create a copy
     * then deletes the source.
     *
     * @param source Source file
     * @param destination Destination file
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
     * Recursively copies the contents of a directory into the destination
     * directory.
     * Source and destination must represent the same type (file or directory).
     * In the case of files this method does the same as copyFile. If they
     * are directories then source is recursively copied into destination.
     * Destination may not exist in this case and will be created.
     *
     * @param source Source file or directory to be copied
     * @param destination Destination file or directory
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

            for (int i=0; i<files.length; i++) {
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
     * Recursively deletes a directory.
     * If path represents a file it is deleted. If it represents a directory
     * then its contents are deleted before the directory itself is removed.
     *
     * Be very careful calling this function, specifying a root directory could wipe the drive (assuming correct permissions)
     *
     * @param path File or directory to delete.
     *
     * @return true if deletion was successful, false otherwise
     */
    public static boolean deleteFilesRecursive(File path) {
        boolean success = true;
        if (path.isDirectory()) {
            String files[] = path.list();

            // delete contents first
            for (int i=0; i<files.length; i++) {
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
                log.warn("Unable to delete "+path.getPath());
            }
        }

        return success;
    }

    /**
     * This util will allow us to write JSON/XML responses to zip file
     *
     * @param gmlDownloads - a list of DownloadResponse
     * @param zout - the ZipOutputStream to write the response
     * @throws IOException
     */
    public static void writeResponseToZip(ArrayList<DownloadResponse> gmlDownloads,ZipOutputStream zout) throws IOException{
      if(gmlDownloads.get(0).getContentType().equals("text/xml")){
          writeResponseToZip(gmlDownloads,zout,false);
      }else{ //VT: TODO: the different response type should be handled differently.
             //VT: eg. handle application/json and a final catch all.
          log.warn("No content type found, defaulting to handling JSON responses");
          writeResponseJSONToZip(gmlDownloads,zout);
      }
    }


    /**
     * Writes a series of DownloadResponse objects to a zip stream, each
     * download response will be put into a separate zip entry.
     * This are the same code as those in BasePortalController
     *
     * @param gmlDownloads The download responses
     * @param zout The stream to receive the zip entries
     */
    public static void writeResponseToZip(List<DownloadResponse> gmlDownloads, ZipOutputStream zout, boolean closeInputs) throws IOException {
        for (int i = 0; i < gmlDownloads.size(); i++) {
            DownloadResponse download = gmlDownloads.get(i);
            String entryName = new SimpleDateFormat((i + 1) + "_yyyyMMdd_HHmmss").format(new Date()) + ".xml";

            // Check that attempt to request is successful
            if (!download.hasException()) {
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
     * Writes an error to a zip stream.
     * This are the same code as those in BasePortalController
     * @param zout the zout
     * @param debugQuery the debug query
     * @param exceptionToPrint the exception to print
     * @param errorFileName The name of the error file in the zip (defaults to 'error.txt')
     */
    public static void writeErrorToZip(ZipOutputStream zout, String debugQuery, Exception exceptionToPrint, String errorFileName) {
        String message = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            exceptionToPrint.printStackTrace(pw);
            message = String.format("An exception occured whilst requesting/parsing your WCS download.\r\n%1$s\r\nMessage=%2$s\r\n%3$s", debugQuery, exceptionToPrint.getMessage(), sw.toString());
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
     * Writes output to input via an in memory buffer of a certain size
     * This are the same code as those in BasePortalController
     * @param input The input stream
     * @param output The output stream (will receive input's bytes)
     * @param bufferSize The size (in bytes) of the in memory buffer
     * @param closeInput if true, the input will be closed prior to this method returning
     * @throws IOException
     */
    public static void writeInputToOutputStream(InputStream input, OutputStream output, int bufferSize, boolean closeInput) throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int dataRead;
            do {
                dataRead = input.read(buffer, 0, buffer.length);
                if (dataRead > 0) {
                    output.write(buffer, 0, dataRead);
                }
            } while (dataRead != -1);
        } finally {
            if (closeInput) {
                FileIOUtil.closeQuietly(input);
            }
        }
    }


    /**
     * This util will allow us to write JSON responses to zip file
     * @param gmlDownloads - a list of DownloadResponse
     * @param zout - the ZipOutputStream to write the response
     * @throws IOException
     */
    public static void writeResponseJSONToZip(ArrayList<DownloadResponse> gmlDownloads,ZipOutputStream zout) throws IOException{
        StringBuilder errorMsg = new StringBuilder();

        for (int i = 0; i<gmlDownloads.size(); i++) {
            DownloadResponse download=gmlDownloads.get(i);
            //Check that attempt to request is successful
            if (!download.hasException()) {
                JSONObject jsonObject = JSONObject.fromObject(download.getResponseAsString());
                //check that JSON reply is successful
                if (jsonObject.get("success").toString().equals("false")) {
                    errorMsg.append("Unsuccessful JSON reply from: " + download.getRequestURL() + "\n");

                    Object messageObject = jsonObject.get("msg");
                    if (messageObject==null || messageObject.toString().length()==0) {
                        errorMsg.append("No error message\n\n");
                    } else {
                        errorMsg.append(messageObject.toString() + "\n\n");
                    }
                } else {
                    byte[] gmlBytes = new byte[] {};
                    Object dataObject = jsonObject.get("data");
                    if (dataObject != null && !JSONNull.getInstance().equals(dataObject)) {
                        Object gmlResponseObject = JSONObject.fromObject(dataObject)
                                .get("gml");

                        if (gmlResponseObject != null) {
                            gmlBytes = gmlResponseObject.toString().getBytes();
                        }
                    }

                    zout.putNextEntry(new ZipEntry(new SimpleDateFormat(
                            (i + 1) + "_yyyyMMdd_HHmmss").format(new Date())
                            + ".xml"));
                    zout.write(gmlBytes);
                    zout.closeEntry();
                }

            } else {
                errorMsg.append("Exception thrown while attempting to download from: " + download.getRequestURL() + "\n");
                errorMsg.append(download.getExceptionAsString() + "\n\n");
            }
        }
        if (errorMsg.length()!=0) {
            zout.putNextEntry(new ZipEntry("downloadInfo.txt"));
            zout.write(errorMsg.toString().getBytes());
            zout.closeEntry();
        }
    }

    /**
     * VT: Have to think of a better way to handle exception rather then just encapsulating the error in xml
     * @param e
     * @param out
     * @param closeStream
     * @throws Exception
     */
    public static void writeExceptionToXMLStream(Exception e,OutputStream out,boolean closeStream) throws Exception{
        String stack=ExceptionUtils.getStackTrace(e);
        String error="<error>" + stack + "</error>";
        try {
            out.write(error.getBytes());
        } catch (IOException e1) {
            log.error(e1.getCause());
           throw e1;
        }finally{
            if(closeStream){
                out.close();
            }
        }
    }
}
