package org.auscope.portal.server.util;

import java.io.BufferedReader;
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
    public static void closeQuietly(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException ioe) {
                log.warn(String.format("Error closing stream - %1$s", ioe));
                log.debug("Exception: ", ioe);
            }
        }
    }
}
