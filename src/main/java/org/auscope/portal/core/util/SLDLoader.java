package org.auscope.portal.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.test.ResourceUtil;

public class SLDLoader {

    public static String loadSLD(String filename, Map<String,String> valueMap, boolean preserveformat) throws IOException{
        InputStream inputStream = null;

        try{
            inputStream = SLDLoader.class.getResourceAsStream(filename);

        }catch(Exception e){
            inputStream = null;
        }
        if(inputStream == null){
            inputStream = ResourceUtil.loadResourceAsStream(filename);
        }


        String newLine = System.getProperty("line.separator");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line; boolean flag = false;
        while ((line = reader.readLine()) != null) {
            if(!preserveformat){
                result.append(line.trim());
            }else{
                result.append(flag? newLine: "").append(line);
                flag = true;
            }
        }

        String input = result.toString();

        if(valueMap != null){
            Set<String> keys = valueMap.keySet();
            for(String key:keys){
                input = input.replace("[" + key + "]", valueMap.get(key));
            }
        }

        return input;


    }

}
