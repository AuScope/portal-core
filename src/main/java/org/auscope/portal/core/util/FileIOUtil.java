package org.auscope.portal.core.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
}
