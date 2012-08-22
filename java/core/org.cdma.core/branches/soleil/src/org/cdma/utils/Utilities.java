// ****************************************************************************
// Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0 
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
// 
// Contributors: 
//    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
// ****************************************************************************
package org.cdma.utils;

/// @cond clientAPI

import java.io.IOException;
import java.net.URI;

import org.cdma.Factory;
import org.cdma.IFactory;
import org.cdma.dictionary.Path;
import org.cdma.exception.DimensionNotSupportedException;
import org.cdma.exception.FileAccessException;
import org.cdma.exception.InvalidArrayTypeException;
import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IArrayIterator;
import org.cdma.interfaces.IAttribute;
import org.cdma.interfaces.IContainer;
import org.cdma.interfaces.IDataItem;
import org.cdma.interfaces.IDataset;
import org.cdma.interfaces.IDictionary;
import org.cdma.interfaces.IDimension;
import org.cdma.interfaces.IGroup;
import org.cdma.interfaces.IKey;

/**
 * @brief This class defines main type used by the CDMA and some conversion methods. 
 * 
 * 
 */
public final class Utilities {

    /**
     * Hide default constructor.
     */
    private Utilities() {
    }

    // ---------------------------------------------------------
    /// Public enumeration
    // ---------------------------------------------------------
    /**
     * Data type names in GDM. The names are the same as the interfaces to
     * reference the objects.
     * 
     * @author nxi
     * 
     */
    public enum ModelType {
        Group, DataItem, Attribute, Dimension, Array, Dataset, LogicalGroup, Dictionary, Other
    }

    /**
     * Check the CDMA type of the object.
     * 
     * @param signal Object type
     * @return CDMA DataType 
     */
    public static ModelType checkModelType(final Object signal) {
        if (signal != null) {
            if (signal instanceof IGroup) {
                return ModelType.Group;
            }
            if (signal instanceof IDataItem) {
                return ModelType.DataItem;
            }
            if (signal instanceof IAttribute) {
                return ModelType.Attribute;
            }
            if (signal instanceof IDimension) {
                return ModelType.Dimension;
            }
            if (signal instanceof IArray) {
                return ModelType.Array;
            }
            if (signal instanceof IDataset) {
                return ModelType.Dataset;
            }
            if (signal instanceof IDictionary) {
                return ModelType.Dictionary;
            }
            return ModelType.Other;
        }
        return null;
    }

    /**
     * Retrieve the object that referenced by the URI.
     * 
     * @param uri URI object
     * @param dictionaryPath in String type
     * @return Object type
     * @throws FileAccessException
     *             file access error
     */
    public static Object findObject(URI uri, String dictionaryPath)
            throws FileAccessException {
        return findObject(uri, dictionaryPath, Factory.getFactory());
    }

    /**
     * Retrieve the object that referenced by the URI.
     * 
     * @param uri URI object
     * @param dictionaryPath in String type
     * @param factory data model factory
     * @return Object type
     * @throws FileAccessException
     *             file access error
     */
    public static Object findObject(URI uri, String dictionaryPath,
            IFactory factory) throws FileAccessException {
        if (uri.getScheme().equals("file")) {
            IGroup rootGroup = null;
            IDataset dataset = null;
            try {
                dataset = factory.createDatasetInstance(uri);
                //        dataset.open();
            } catch (IOException e1) {
                throw new FileAccessException(e1);
            } catch (Exception e2) {
                throw new FileAccessException(e2);
            }
            rootGroup = dataset.getRootGroup();
            if (dictionaryPath != null) {
                IDictionary dictionary = factory.createDictionary();
                dictionary.readEntries(dictionaryPath);
                rootGroup.setDictionary(dictionary);
            }
            String fragment = uri.getFragment();
            String query = uri.getQuery();
            Object signal = rootGroup;
            try {
                if (fragment != null) {
                    signal = rootGroup.findContainer(fragment);
                } else if (query != null) {
                    String itemPath = findPath(query);
                    if (itemPath != null) {
                        signal = rootGroup.findContainerByPath(itemPath);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            return signal;

        }
        return null;
    }

    /**
     * Check the data type in the location referenced by the URI.
     * 
     * @param uri URI object
     * @param dictionaryPath in String type
     * @return CDMA DataType object
     * @throws FileAccessException
     */
    public static ModelType checkDataType(final URI uri,
            final String dictionaryPath) throws FileAccessException {
        String path = null;
        if (uri.getScheme().equals("file")) {
            path = uri.getPath();
            IGroup rootGroup = null;
            IDataset dataset = null;
            IContainer container = null;
            try {
                IFactory factory = Factory.getFactory( dataset.getFactoryName() );
                dataset = factory.createDatasetInstance(uri);
                dataset.open();
            } catch (IOException e1) {
                throw new FileAccessException(e1);
            } catch (Exception e2) {
                throw new FileAccessException(e2);
            }
            rootGroup = dataset.getRootGroup();

            try {
                container = rootGroup.findContainerByPath(path);
            }
            catch (Exception e) {}
            if (rootGroup == null || !(container instanceof IGroup) ) {
                return null;
            }
            else {
                rootGroup = (IGroup) container;
            }
            String fragment = uri.getFragment();
            String query = uri.getQuery();
            Object signal = null;
            if (fragment != null) {
                signal = rootGroup.getContainer(fragment);
            } else if (query != null) {
                String itemPath = findPath(query);
                if (itemPath != null) {
                    signal = rootGroup.getContainer(itemPath);
                }
            } else {
                return ModelType.Dataset;
            }
            if (signal != null) {
                if (signal instanceof IGroup) {
                    return ModelType.Group;
                }
                if (signal instanceof IDataItem) {
                    return ModelType.DataItem;
                }
                if (signal instanceof IAttribute) {
                    return ModelType.Attribute;
                }
                if (signal instanceof IDimension) {
                    return ModelType.Dimension;
                }
            }
        }
        return ModelType.Other;
    }

    /**
     * Parse a string and find the value of a path. The string parameter is in a
     * format of 'path=pathValue'.
     * 
     * @param query in String type
     * @return String object 
     */
    private static String findPath(final String query) {
        String[] pairs = query.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String[] pair = pairs[i].split("=", 2);
            if (pair[0].equals("path")) {
                return pair[1];
            }
        }
        return null;
    }

    /**
     * Create an new IArray by reshaping an IArray to a new shape. The total size
     * of the new IArray must be same as the old IArray. The new IArray will use a
     * new storage space.
     * 
     * @param array CDMA IArray type
     * @param shape java array of integer
     * @return CDMA IArray
     * @throws InvalidArrayTypeException
     */
    public static IArray reshapeArray(final IArray array, final int[] shape)
            throws InvalidArrayTypeException {
        int newLength = 1;
        for (int i = 0; i < shape.length; i++) {
            newLength *= shape[i];
        }
        if (newLength != array.getSize()) {
            throw new InvalidArrayTypeException("the shape is invalid");
        }
        IFactory factory = Factory.getFactory( array.getFactoryName() );
        return factory.createArray(array.getElementType(), shape, array
                .getStorage());
    }

    /**
     * Copy the array to a new array with double type storage.
     * 
     * @param array new IArray with new storage
     * @return new IArray 
     */
    public static IArray copyToDoubleArray(final IArray array) {
        IFactory factory = Factory.getFactory( array.getFactoryName() );
        IArray doubleArray = factory.createArray(Double.TYPE, array.getShape());
        IArrayIterator oldIterator = array.getIterator();
        IArrayIterator newIterator = doubleArray.getIterator();
        while (oldIterator.hasNext()) {
            newIterator.next();
            newIterator.setDouble(oldIterator.getDoubleNext());
        }
        return doubleArray;
    }

    
    /**
     * Copy the array to a new array with double type storage.
     * 
     * @param array new IArray with new storage
     * @return new IArray 
     */
    public static IArray copyToPositiveDoubleArray(final IArray array) {
        IFactory factory = Factory.getFactory( array.getFactoryName() );
        IArray doubleArray = factory.createArray(Double.TYPE, array.getShape());
        IArrayIterator oldIterator = array.getIterator();
        IArrayIterator newIterator = doubleArray.getIterator();
        while (oldIterator.hasNext()) {
            newIterator.next();
            newIterator.setDouble(Math.abs(oldIterator.getDoubleNext()));
        }
        return doubleArray;
    }

    /**
     * Helper method for copying the given number of items from one array to another. 
     * If the length is not given (length<0), as many items in array1 will be copied to
     * array2 up to the smaller size of the two arrays.  
     * @param array1 IArray object
     * @param array2 IArray object
     * @param length an int value
     * @throws DimensionNotSupportedException
     */
    public static void copyTo(final IArray array1, final IArray array2, int length) 
            throws DimensionNotSupportedException {
        IArrayIterator iterator1 = array1.getIterator();
        IArrayIterator iterator2 = array2.getIterator();
        if (length < 0) {
            while (iterator1.hasNext() && iterator2.hasNext()) {
                iterator2.next();
                iterator2.setObject(iterator1.getObjectNext());
            }
        } else {
            int id = 0;
            while (iterator1.hasNext() && iterator2.hasNext()) {
                if (id < length) {
                    iterator2.next();
                    iterator2.setObject(iterator1.getObjectNext());
                    id ++;
                } else {
                    break;
                }
            }
        }
    }

    // [ANSTO][Tony] Copied from the original GDMUtils, but not sure if the dictionary mechanism
    // has already supported this or not.
    // [SOLEIL][clement] It should be working: it only concerns the 'normal dictionary' mechanism (due to the use of group.getRootGroup()...)
    /**
     * Reverse the dictionary. Return the key if given a value.
     * @param group @param shortName @return
     */
    public static String getKeyFromValue(IGroup group, String shortName){
        // [ANSTO][Tony] Please check if the following logic works as same as before
        IDictionary dictionary = group.getRootGroup().findDictionary();
        for (IKey key : dictionary.getAllKeys()) {
            for (Path path : dictionary.getAllPaths(key)) {
                String value = path.getValue();
                if (value.contains("@")){
                    if (value.substring(value.lastIndexOf("@") + 1).equals(shortName)) {
                        return key.getName();
                    }
                } else if (value.contains("/")){
                    if (value.substring(value.lastIndexOf("/") + 1).equals(shortName)) {
                        return key.getName();
                    }
                } else {
                    if (path.equals(shortName)) {
                        return key.getName();
                    }
                }
            }
        }
        return null;
        //    for (Entry<String, String> entry : group.getRootGroup().getDictionary().entrySet()){
        //      String path = entry.getValue();
        //      if (path.contains("@")){
        //        if (path.substring(path.lastIndexOf("@") + 1).equals(shortName))
        //          return entry.getKey();
        //      }else if (path.contains("/")){
        //        if (path.substring(path.lastIndexOf("/") + 1).equals(shortName))
        //          return entry.getKey();
        //      }else
        //        if (path.equals(shortName))
        //          return entry.getKey();
        //    }
        //    return null;
    }


}

/// @endcond clientAPI 