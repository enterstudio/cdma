/*******************************************************************************
 * Copyright (c) 2008 - ANSTO/Synchrotron SOLEIL
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 * 	Tony Lam (nxi@Bragg Institute) - initial API and implementation
 *        Majid Ounsy (SOLEIL Synchrotron) - API v2 design and conception
 *        St�phane Poirier (SOLEIL Synchrotron) - API v2 design and conception
 * 	Clement Rodriguez (ALTEN for SOLEIL Synchrotron) - API evolution
 * 	Gregory VIGUIER (SOLEIL Synchrotron) - API evolution
 ******************************************************************************/
package org.cdma.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.cdma.Factory;

/**
 * A class used to access data by using CDMA. This class also gives some basic file methods
 * 
 * @author girardot
 */
public class CDMATool {

    private String resourceDirectory;

    public CDMATool(String resourceDirectory) {
        setResourceDirectory(resourceDirectory);
    }

    /**
     * Checks whether a dictionary file exist in the dictionary folder
     * 
     * @param dictionaryFileName The name of the file to check
     * @return A <code>boolean</code> value. <code>TRUE</code> if the file exists, <code>FALSE</code> otherwise
     */
    public static boolean hasDictionary() {
        String dictionaryFileName = Factory.getPathKeyDictionary();
        boolean exists;
        if ((dictionaryFileName == null) || (dictionaryFileName.trim().isEmpty())) {
            exists = false;
        } else {
            File file = new File(dictionaryFileName);
            exists = file.isFile();
        }
        return exists;
    }

    /**
     * Creates a dictionary file in the dictionary folder, based on some file content. The target
     * file is recovered by {@link Factory#getPathKeyDictionary()}
     * 
     * @param sourceStream The {@link InputStream} that reads the source file content
     * @see Factory#getPathKeyDictionary()
     */
    public static void createDictionary(InputStream sourceStream) {
        String dictionaryFileName = Factory.getPathKeyDictionary();
        if ((dictionaryFileName != null) && (sourceStream != null) && (!dictionaryFileName.trim().isEmpty())) {
            File destinationDictionaryFile = new File(dictionaryFileName);
            duplicateFile(sourceStream, destinationDictionaryFile, false);
        }
    }

    /**
     * Returns the directory used for application temporary files (CDM dictionaries, etc...)
     * 
     * @return A {@link File}
     */
    public File getDefaultResourceDirectory() {
        if ((resourceDirectory == null) || (resourceDirectory.trim().isEmpty())) {
            resourceDirectory = ".";
        }
        File resourceDirectoryFile = new File(resourceDirectory);
        if (!resourceDirectoryFile.exists()) {
            resourceDirectoryFile.mkdirs();
        }
        return resourceDirectoryFile;
    }

    /**
     * Sets the resource directory path
     * 
     * @param resourceDirectory the resource directory path
     */
    public void setResourceDirectory(String resourceDirectory) {
        if (resourceDirectory == null) {
            resourceDirectory = ".";
        }
        this.resourceDirectory = resourceDirectory;
    }

    /**
     * Prepares the CDMA {@link Factory} to use a particular dictionary
     * 
     * @param activeDictionary The experiment name of the dictionary (example: "FLYSCAN")
     */
    public void switchDictionary(String activeDictionary) {
        // In case someone was stupid enough to change this...
        Factory.setDictionariesFolder(getDefaultResourceDirectory().getAbsolutePath());
        // Now switch dictionary view
        Factory.setActiveView(activeDictionary);
    }

    /**
     * Prepares the CDMA {@link Factory} to use a particular dictionary
     * 
     * @param activeDictionary The experiment name of the dictionary (example: "FLYSCAN")
     * @param reference The {@link InputStream} that contains the dictionary xml code, if the
     *            dictionary view does not exist yet.
     */
    public void switchDictionary(String activeDictionary, InputStream reference) {
        switchDictionary(activeDictionary);
        if (reference != null) {
            if (!hasDictionary()) {
                createDictionary(reference);
            }
            try {
                reference.close();
            } catch (IOException e) {
                Factory.getLogger().log(Level.WARNING, e.getMessage());
            }
        }
    }

    /**
     * Gets access to parameter values of a parameter name for a given {@link IKey} in an {@link ILogicalGroup}. If the
     * corresponding parameter is not the 1st one in the list of
     * possible parameters, this methods sets the previous parameters with their first available
     * value.
     * 
     * @param parameterName The name of the desired parameter
     * @param group The {@link ILogicalGroup}
     * @param key The {@link IKey}
     * @return An {@link IPathParameter} {@link List}. May be <code>null</code> if parameter name is
     *         empty or <code>null</code>, or if <code>group</code> or <code>key</code> is <code>null</code>.
     */
    /*
        public static List<PathParameter> getFirstAccessParameterValues(String parameterName,
                LogicalGroup group, IKey key) {
            List<PathParameter> result = null;
            if ((parameterName != null) && (!parameterName.trim().isEmpty()) && (group != null)
                    && (key != null)) {
                result = group.getParameterValues(key);
                boolean found = result.isEmpty();
                while (!found) {
                    List<PathParameter> temp = result;
                    for (PathParameter parameter : temp) {
                        if (parameterName.equals(parameter.getName())) {
                            found = true;
                        }
                        else {
                            key.pushParameter(parameter);
                            result = group.getParameterValues(key);
                            found = result.isEmpty();
                        }
                        break;
                    }
                }
            }
            return result;
        }
    */
    /**
     * Duplicates a {@link File}.
     * 
     * @param source the source {@link File}, as an {@link InputStream}
     * @param dest the destination {@link File}
     * @param allowOverWrite a boolean to allow or not writing over an existing destination {@link File}.
     *            <code>true</code> to allow overwriting.
     * @return A boolean value: <code>true</code> if the destination File was successfully created
     *         and written, <code>false</code> otherwise
     */
    private static boolean duplicateFile(InputStream source, File dest, boolean allowOverWrite) {
        boolean result = true;
        if ((source == null) || (dest == null) || dest.isDirectory() || (dest.exists() && !allowOverWrite)) {
            result = false;
        } else if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                result = false;
            }
        }
        if (result) {
            byte[] buffer = new byte[10000]; // buffer size
            int length;
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(dest);
                while ((length = source.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
            } catch (IOException ex) {
                Factory.getLogger().log(Level.WARNING, ex.getMessage());
                result = false;
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        Factory.getLogger().log(Level.WARNING, e.getMessage());
                    }
                }
                try {
                    source.close();
                } catch (IOException e) {
                    Factory.getLogger().log(Level.WARNING, e.getMessage());
                }
            }
        }
        return result;
    }

}
