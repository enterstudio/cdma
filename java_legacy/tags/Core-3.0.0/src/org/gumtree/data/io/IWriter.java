/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Wrapper for UCAR NetCDF/Java Array class
 * 
 * Contributors: 
 *    Norman Xiong - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.io;

import org.gumtree.data.exception.GDMWriterException;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

/**
 * The interface of GDM writer. It provides methods of outputting GDM object to
 * a storage that has a tree structure. The default implementation is
 * {@link NcHdfWriter}. Use
 * 
 * <pre>
 * IWriter writer = new NcHdfWriter(String filename);
 * </pre>
 * 
 * to create a default implementation object.
 * 
 * @author nxi
 * @version 2.0
 */
public interface IWriter {

	/**
	 * Open the storage, for example, the file handler. Any output action will
	 * require the storage to be open.
	 * 
	 * @throws GDMWriterException
	 *             failed to open the storage
	 */
	void open() throws GDMWriterException;

	/**
	 * Check if the storage is open for output.
	 * 
	 * @return true or false
	 */
	boolean isOpen();

	/**
	 * Add a group to the root of the storage. The root has an X-path of '/'.
	 * This has the same performance as
	 * {@link #writeToRoot(IGroup group, boolean force)}, where force is set to
	 * be false.
	 * 
	 * @param group
	 *            Gumtree group object
	 * @throws GDMWriterException
	 *             failed to write the group
	 */
	void writeToRoot(IGroup group) throws GDMWriterException;

	/**
	 * Write a group to the root of the storage. The root has an X-path of '/'.
	 * When a group with a same name already exists under the root node, write
	 * the contents of the GDM group under the target group node. For conflict
	 * data item, check the force switch. If it is set to be true, overwrite the
	 * contents under the group. Otherwise raise an exception. <br>
	 * See {@link #writeGroup(String, IGroup, boolean)} for more information.
	 * 
	 * @param group
	 *            GDM group object
	 * @param force
	 *            if allow overwriting
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeToRoot(IGroup group, boolean force) throws GDMWriterException;

	/**
	 * Write a data item to the root of the storage. If a data node with the
	 * same name already exists, raise an exception.
	 * 
	 * @param dataItem
	 *            GDM data item
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeToRoot(IDataItem dataItem) throws GDMWriterException;

	/**
	 * Write a data item to the root of the storage. If force is true, overwrite
	 * the conflicting node.
	 * 
	 * @param dataItem
	 *            GDM data item
	 * @param force
	 *            if allow overwriting
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeToRoot(IDataItem dataItem, boolean force)
			throws GDMWriterException;

	/**
	 * Write an attribute to the root of the storage. If an attribute node
	 * already exists in the root of the storage, raise an exception.
	 * 
	 * @param attribute
	 *            GDM attribute
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeToRoot(IAttribute attribute) throws GDMWriterException;

	/**
	 * Write an attribute to the root of the storage. If an attribute node
	 * already exists, check the force switch. If it is true, overwrite the
	 * node. Otherwise raise an exception.
	 * 
	 * @param attribute
	 *            GDM attribute
	 * @param force
	 *            if allow overwriting
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeToRoot(IAttribute attribute, boolean force)
			throws GDMWriterException;

	/**
	 * Write a group under the node with a given X-path. If a group node with
	 * the same name already exists, this will not overwrite any existing
	 * contents under the node. When conflicting happens, raise an exception.
	 * 
	 * @param parentPath
	 *            x-path as a String object
	 * @param group
	 *            GDM group
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeGroup(String parentPath, IGroup group) throws GDMWriterException;

	/**
	 * Write a group under the node of a given X-path. If a group node with the
	 * same name already exists, check the 'force' switch. If it is true,
	 * overwrite any conflicting contents under the node. Otherwise raise an
	 * exception for conflicting.
	 * 
	 * @param parentPath
	 *            x-path as a String object
	 * @param group
	 *            GDM group
	 * @param force
	 *            if allow overwriting
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeGroup(String parentPath, IGroup group, boolean force)
			throws GDMWriterException;

	/**
	 * Write a data item under a group node with a given X-path. If a data item
	 * node already exists there, raise an exception.
	 * 
	 * @param parentPath
	 *            x-path as a String object
	 * @param dataItem
	 *            GDM data item
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeDataItem(String parentPath, IDataItem dataItem)
			throws GDMWriterException;

	/**
	 * Write a data item under a group node with a given X-path. If a data item
	 * node already exists there, check the 'force' switch. If it is true,
	 * overwrite the node. Otherwise raise an exception.
	 * 
	 * @param parentPath
	 *            String value
	 * @param dataItem
	 *            IDataItem object
	 * @param force
	 *            true or false
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeDataItem(String parentPath, IDataItem dataItem, boolean force)
			throws GDMWriterException;

	/**
	 * Write an attribute under a node with a given X-path. The parent node can
	 * be either a group node or a data item node. If an attribute node with the
	 * same name already exists, raise an exception.
	 * 
	 * @param parentPath
	 *            x-path as a String object
	 * @param attribute
	 *            GDM attribute
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeAttribute(String parentPath, IAttribute attribute)
			throws GDMWriterException;

	/**
	 * Write an attribute to the node with a given X-path. The node can be
	 * either a group node or a data item node. If an attribute with an existing
	 * name already exists, raise an exception.
	 * 
	 * @param parentPath
	 *            x-path as a String object
	 * @param attribute
	 *            GDM attribute
	 * @param force
	 *            if allowing overwriting
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeAttribute(String parentPath, IAttribute attribute, boolean force)
			throws GDMWriterException;

	/**
	 * Write an empty group under a group node with a given X-path. If a group
	 * node with the same name already exists, check the 'force' switch. If it
	 * is true, remove all the contents of the group node.
	 * 
	 * @param xPath
	 *            as a String object
	 * @param groupName
	 *            short name as a String object
	 * @param force
	 *            if allow overwriting
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void writeEmptyGroup(String xPath, String groupName, boolean force)
			throws GDMWriterException;

	/**
	 * Remove a group with a given X-path from the storage.
	 * 
	 * @param groupPath
	 *            as a String object
	 */
	void removeGroup(String groupPath);

	/**
	 * Remove a data item with a given X-path from the storage.
	 * 
	 * @param dataItemPath
	 *            as a String object
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void removeDataItem(String dataItemPath) throws GDMWriterException;

	/**
	 * Remove an attribute with a given X-path from the storage.
	 * 
	 * @param attributePath
	 *            x-path as a String object
	 * @throws GDMWriterException
	 *             failed to write to hdf file
	 */
	void removeAttribute(String attributePath) throws GDMWriterException;

	/**
	 * Check if a group exists in a given X-path.
	 * 
	 * @param xPath
	 *            as a String object
	 * @return true or false
	 */
	boolean isGroupExist(String xPath);

	/**
	 * Check if a group exists under certain group node with a given X-path.
	 * 
	 * @param parentPath
	 *            the X-path of the parent group
	 * @param groupName
	 *            the name of the target group
	 * @return true or false
	 */
	boolean isGroupExist(String parentPath, String groupName);

	/**
	 * Check if a data item exists with a given X-path.
	 * 
	 * @param xPath
	 *            as a String object
	 * @return true or false
	 */
	boolean isDataItemExist(String xPath);

	/**
	 * Check if a data item exists under a parent group with a given X-path.
	 * 
	 * @param parentPath
	 *            x-path of the parent group as a String object
	 * @param dataItemName
	 *            name of the target data item
	 * @return true or false
	 */
	boolean isDataItemExist(String parentPath, String dataItemName);

	/**
	 * Check if an attribute exist with a given xpath.
	 * 
	 * @param xPath
	 *            x-path as a String object
	 * @return true or false
	 */
	boolean isAttributeExist(String xPath);

	/**
	 * Check if the attribute with a given name already exists.
	 * 
	 * @param parentPath
	 *            String object
	 * @param attributeName
	 *            String object
	 * @return true or false
	 */
	boolean isAttributeExist(String parentPath, String attributeName);

	/**
	 * Close the file handler. Unlock the file.
	 */
	void close();
}