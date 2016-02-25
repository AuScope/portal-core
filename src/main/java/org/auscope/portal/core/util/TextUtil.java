package org.auscope.portal.core.util;

import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Utility methods for text/string operations.
 * 
 * @author Richard Goh
 */
public class TextUtil {
	
	public static boolean isNullOrEmpty(String s) {
		return s==null || s.length()==0;
	}
	
	public static boolean isAnyNullOrEmpty(String ... strings) {
		for (String string : strings) {
			if(isNullOrEmpty(string)) return true;
		}
		return false;
	}
	
	public static boolean isAllNullOrEmpty(String ... strings) {
		for (String string : strings) {
			if( ! isNullOrEmpty(string)) return false;
		}
		return true;
	}

	/**
     * Get the last N lines of multi-line text. Each line will be separated by platform dependant line separator.
     * 
     * @param text
     *            The multi-line text.
     * @param maxLines
     *            The last N lines.
     * @return a string which contains the last N lines of a given multi-line text.
     */
    public static String tail(String text, int maxLines) {
        StringBuffer sb = new StringBuffer(maxLines);

        try {
            List<String> lines = IOUtils.readLines(new StringReader(text));
            int fromIndex = lines.size() - maxLines;
            int toIndex = lines.size();
            for (String line : lines.subList(fromIndex, toIndex)) {
                sb.append(line + System.getProperty("line.separator"));
            }
        } catch (Exception ex) {
            // if failed, return empty string.
        }

        return sb.toString();
    }
}