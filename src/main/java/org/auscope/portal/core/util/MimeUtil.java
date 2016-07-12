package org.auscope.portal.core.util;

import java.util.HashMap;

/**
 * Utilities for MIME types
 *
 * @author Josh Vote
 */
public class MimeUtil {

    private static HashMap<String, String> table;

    static {

        table = new HashMap<>();
        table.put("image/png", "png");
        table.put("image/png8", "png");
        table.put("image/jpeg", "jpeg");
        table.put("image/gif", "gif");
        table.put("image/tiff", "tiff");
        table.put("image/tiff8", "tiff");
        table.put("image/geotiff", "tiff");
        table.put("image/geotiff8", "tiff");
        table.put("image/svg", "svg");
        table.put("application/pdf", "pdf");
        table.put("application/zip", "zip");
        table.put("text/xml", "xml");
        table.put("text/csv", "csv");
        table.put("rss", "rss");
        table.put("kml", "kml");
        table.put("kmz", "kmz");

    }

    /**
     * Converts a mime type to a 'well known' file extension. If the mime type is unknown then an empty string will be returned
     *
     * Returns only the file extension (ie "xml" not ".xml")
     *
     * @param mime
     *            The mime to examine
     */
    public static String mimeToFileExtension(String mime) {

        if (mime == null || mime.length() == 0) {
            return "";
        }

        String ext = table.get(mime);
        if (ext != null && ext.length() > 0) {
            return ext;
        }
        //below are fall back if there are no mime to file extension map
        if (mime.startsWith("image/")) {
            String suffix = mime.substring("image/".length());
            return suffix.split("\\+|;")[0];
        } else if (mime.startsWith("text/")) {
            String suffix = mime.substring("text/".length());
            return suffix.split("\\+|;")[0];
        } else if (mime.contains("kml")) {
            return "kml";
        } else if (mime.contains("kmz")) {
            return "kmz";
        } else if (mime.contains("xml")) {
            return "xml";
        } else if (mime.contains("csv")) {
            return "csv";
        }

        return "";
    }
}
