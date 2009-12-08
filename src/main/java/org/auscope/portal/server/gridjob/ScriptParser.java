/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class that provides methods to extract information from an ESyS-Particle
 * python script file.
 *
 * @author Cihan Altinay
 */
public class ScriptParser
{
    /** Logger for this class and subclasses */
    private static Log logger = LogFactory.getLog(Util.class.getName());

    private String scriptText;
    private int numWorkerProcesses;
    private int numTimeSteps;
    private float timeStepSize;
    private String geometryFile = null;
    private String checkPointPrefix = null;
    private List<String> fieldSavers = new ArrayList<String>();

    /**
     * Default constructor
     */
    public ScriptParser() {
    }

    /**
     * Returns the extracted numWorkerProcesses value.
     */
    public int getNumWorkerProcesses() {
        return numWorkerProcesses;
    }

    /**
     * Returns the extracted numTimeSteps value.
     */
    public int getNumTimeSteps() {
        return numTimeSteps;
    }

    /**
     * Returns the extracted timeStepSize value.
     */
    public float getTimeStepSize() {
        return timeStepSize;
    }

    /**
     * Returns the extracted geometryFile name.
     */
    public String getGeometryFile() {
        return geometryFile;
    }

    /**
     * Returns the extracted checkPointPrefix.
     */
    public String getCheckPointPrefix() {
        return checkPointPrefix;
    }

    /**
     * Returns a list of the extracted fieldsaver filenames.
     */
    public List<String> getFieldSavers() {
        return fieldSavers;
    }

    /**
     * Parses given script file.
     *
     * @param scriptFile The File to parse
     */
    public void parse(File scriptFile) throws IOException {
        logger.debug("Parsing "+scriptFile.getPath());
        StringBuffer buffer = new StringBuffer();
        // pattern for line comments and empty lines
        Pattern p = Pattern.compile("^\\s*#.*$|^\\s*$");

        BufferedReader input = new BufferedReader(
                new FileReader(scriptFile));
        String line = null;
        while ((line = input.readLine()) != null) {
            Matcher matcher = p.matcher(line);
            // discard line comments and empty lines
            if (!matcher.matches()) {
                buffer.append(line).append('\n');
            }
        }
        input.close();

        scriptText = buffer.toString();

        // Now try to extract relevant data from the script
        numWorkerProcesses = extractInt(
                "LsmMpi\\s*\\(\\s*numWorkerProcesses\\s*=\\s*(\\d+)");
        numTimeSteps = extractInt("setNumTimeSteps\\s*\\(\\s*(\\d+)\\s*\\)");
        timeStepSize = extractFloat("setTimeStepSize\\s*\\(\\s*(\\S+)\\s*\\)");
        List<String> geoFiles =
            extractStrings("readGeometry\\s*\\(\\s*\"(.+)\"\\s*\\)");
        if (!geoFiles.isEmpty()) {
            geometryFile = geoFiles.get(0);
        }
        List<String> checkPointers =
            extractStrings("CheckPointPrms\\s*\\(\\s*fileNamePrefix\\s*=\\s*\"(.+)\"");
        if (!checkPointers.isEmpty()) {
            checkPointPrefix = checkPointers.get(0);
        }
        //fieldSavers = extractStrings("FieldSaverPrms\\s*\\("); //FIXME
    }

    private int extractInt(final String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(scriptText);
        int result = 0;
        if (m.find() && m.groupCount() >= 1) {
            try {
                result = Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Exception while parsing int: "+e.getMessage());
            }
        } else {
            logger.warn("No match for '"+regex+"'!");
        }
        return result;
    }

    private float extractFloat(final String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(scriptText);
        float result = 0.0f;
        if (m.find() && m.groupCount() >= 1) {
            try {
                result = Float.parseFloat(m.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Exception while parsing float: "+e.getMessage());
            }
        } else {
            logger.warn("No match for '"+regex+"'!");
        }
        return result;
    }

    private List<String> extractStrings(final String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(scriptText);
        List<String> result = new ArrayList<String>();
        while (m.find()) {
            if (m.groupCount() >= 1) {
                result.add(m.group(1));
            }
        }

        if (result.isEmpty()) {
            logger.debug("No match for '"+regex+"'.");
        }
        return result;
    }
}

