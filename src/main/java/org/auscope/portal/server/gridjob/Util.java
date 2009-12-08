/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Singleton class that provides utility methods like copying files.
 *
 * @author Cihan Altinay
 */
public class Util
{
    /** Logger for this class and subclasses */
    private static Log logger = LogFactory.getLog(Util.class.getName());

    /**
     * Private constructor to prevent instantiation.
     */
    private Util() { }

    /**
     * Copies a file from source to destination.
     *
     * @param source Source file to copy
     * @param destination File to copy to
     *
     * @return true if file was successfully copied, false otherwise
     */
    public static boolean copyFile(File source, File destination) {
        boolean success = false;
        logger.debug(source.getPath()+" -> "+destination.getPath());
        FileInputStream input = null;
        FileOutputStream output = null;
        byte[] buffer = new byte[8192];
        int bytesRead;

        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(destination);
            while ((bytesRead = input.read(buffer)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
            success = true;

        } catch (IOException e) {
            logger.warn("Could not copy file: "+e.getMessage());

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {}
            }
        }

        return success;
    }

    /**
     * Moves a file from source to destination. Uses copyFile to create a copy
     * then deletes the source.
     *
     * @param source Source file
     * @param destination Destination file
     *
     * @return true if file was successfully moved, false otherwise
     */
    public static boolean moveFile(File source, File destination) {
        boolean success = copyFile(source, destination);
        if (success) {
            source.delete();
        }
        return success;
    }

    /**
     * Recursively copies the contents of a directory into the destination
     * directory.
     * Source and destination must represent the same type (file or directory).
     * In the case of files this method does the same as copyFile. If they
     * are directories then source is recursively copied into destination.
     * Destination may not exist in this case and will be created.
     *
     * @param source Source file or directory to be copied
     * @param destination Destination file or directory
     *
     * @return true if contents were successfully copied, false otherwise
     */
    public static boolean copyFilesRecursive(File source, File destination) {
        boolean success = false;

        if (source.isDirectory()) {
            if (!destination.exists()) {
                if (!destination.mkdirs()) {
                    success = false;
                    return false;
                }
            }
            String files[] = source.list();

            for (int i=0; i<files.length; i++) {
                File newSrc = new File(source, files[i]);
                File newDest = new File(destination, files[i]);
                success = copyFilesRecursive(newSrc, newDest);
                if (!success) {
                    break;
                }
            }
        } else {
            success = copyFile(source, destination);
        }

        return success;
    }

    /**
     * Recursively deletes a directory.
     * If path represents a file it is deleted. If it represents a directory
     * then its contents are deleted before the directory itself is removed.
     *
     * @param path File or directory to delete.
     *
     * @return true if deletion was successful, false otherwise
     */
    public static boolean deleteFilesRecursive(File path) {
        boolean success = true;
        if (path.isDirectory()) {
            String files[] = path.list();

            // delete contents first
            for (int i=0; i<files.length; i++) {
                File newPath = new File(path, files[i]);
                success = deleteFilesRecursive(newPath);
                if (!success) {
                    break;
                }
            }
        }

        if (success) {
            // delete path (whether it is a file or directory)
            success = path.delete();
            if (!success) {
                logger.warn("Unable to delete "+path.getPath());
            }
        }

        return success;
    }
}

