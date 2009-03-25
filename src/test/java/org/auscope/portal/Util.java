package org.auscope.portal;

import java.io.*;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 8:44:18 AM
 */
public class Util {
    public static String loadXML(String fileName) throws IOException {
        File mineContents = new File(fileName);
        BufferedReader reader = new BufferedReader( new FileReader(mineContents) );
        StringBuffer mineXML = new StringBuffer();

        String str;
        while ((str = reader.readLine()) != null) {
            mineXML.append(str);
        }
        reader.close();

        return mineXML.toString();
    }
}
