package org.auscope.portal.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class CSVUtil {

    private BufferedReader csvReader;

    private String[] headers;

    public CSVUtil(InputStream csvStream) throws IOException {
        this.csvReader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8));
        this.setHeaders();
    }

    public String[] getHeaders() {
        return this.headers;
    }

    private void setHeaders() throws IOException {
        String line = "";
        line = csvReader.readLine();
        if (line != null) {
            headers = line.split(",");
        } else {
            throw new IOException("CSV Headers not found");
        }

    }

    public HashMap<String, ArrayList<String>> getColumnOfInterest(String[] columns) throws IOException {
        int[] columnIndex = getColumnIndex(columns);
        if (columnIndex.length != columns.length) {
            throw new IOException("Not all columns are found");
        }
        HashMap<String, ArrayList<String>> result = new HashMap<>();

        // Adds in an empty ArrayList for each column name
        for (String column : columns) {
            result.put(column, new ArrayList<String>());
        }

        String line = "";

        // 'doneCols' is used to make sure that duplicate columns are not processed twice
        Set<String> doneCols = new HashSet<String>();

        // Loop over each line of CSV, setting the desired columns' values
        while ((line = csvReader.readLine()) != null) {
            if (line.isEmpty())
                continue;

            String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            for (int i = 0; i < columnIndex.length; i++) {
                if (!doneCols.contains(columns[i])) {
                    if (columnIndex[i] < tokens.length) {
                        result.get(columns[i]).add(tokens[columnIndex[i]]);
                    } else {
                        result.get(columns[i]).add("");
                    }
                    doneCols.add(columns[i]);
                }
            }
            doneCols.clear();
        }

        return result;
    }

    private int[] getColumnIndex(String[] columns) {
        int[] columnIndex = new int[columns.length];

        for (int i = 0; i < columns.length; i++) {
            columnIndex[i] = Arrays.asList(this.headers).indexOf(columns[i]);
        }

        return columnIndex;
    }

}
