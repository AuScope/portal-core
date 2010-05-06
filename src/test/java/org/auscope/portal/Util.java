package org.auscope.portal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

//import java.io.*;

/**
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * @version $Id$
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
