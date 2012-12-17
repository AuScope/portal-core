package org.auscope.portal.core.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utilities for loading classpath resources
 */
public class ResourceUtil {

    /**
     * Loads a file resource into a String
     * @param resourceName The fully qualified resource name (must be relative to base namespace)
     * @return
     * @throws IOException if the file DNE or cannot be opened
     */
    public static String loadResourceAsString(String resourceName) throws IOException {
        InputStream is = loadResourceAsStream(resourceName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();

        String str;
        while ((str = reader.readLine()) != null) {
            sb.append(str);
        }
        reader.close();

        return sb.toString();
    }

    /**
     * Loads a file resource and returns it's contents as an InputStream
     * @param resourceName The fully qualified resource name (must be relative to base namespace)
     * @return
     * @throws IOException if the file DNE or cannot be opened
     */
    public static InputStream loadResourceAsStream(String resourceName) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException(String.format("resourceName '%1$s' cannot be opened", resourceName));
        }

        return is;
    }
}
