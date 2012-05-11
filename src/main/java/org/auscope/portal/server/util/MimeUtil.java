package org.auscope.portal.server.util;

/**
 * Utilities for MIME types
 * @author Josh Vote
 */
public class MimeUtil {
    /**
     * Converts a mime type to a 'well known' file extension. If the
     * mime type is unknown then an empty string will be returned
     *
     * Returns only the file extension (ie "xml" not ".xml")
     *
     * @param mime The mime to examine
     */
    public static String mimeToFileExtension(String mime) {
        if (mime == null) {
            return "";
        } else if (mime.startsWith("image/")) {
            String suffix = mime.substring("image/".length());
            return suffix.split("\\+")[0];
        } else if (mime.startsWith("text/")) {
            String suffix = mime.substring("text/".length());
            return suffix.split("\\+")[0];
        } else if (mime.contains("kml")) {
            return "kml";
        } else if (mime.contains("xml")) {
            return "xml";
        }

        return "";
    }
}
