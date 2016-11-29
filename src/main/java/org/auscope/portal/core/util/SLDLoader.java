package org.auscope.portal.core.util;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import org.auscope.portal.core.test.ResourceUtil;

public class SLDLoader {

    public static String loadSLD(String filename, Hashtable<String,String> valueMap) throws IOException{
        String input = ResourceUtil.loadResourceAsString(filename);

        if(valueMap != null){
            Set<String> keys = valueMap.keySet();
            for(String key:keys){
                input = input.replace("[" + key + "]", valueMap.get(key));
            }
        }

        return input;
    }

}
