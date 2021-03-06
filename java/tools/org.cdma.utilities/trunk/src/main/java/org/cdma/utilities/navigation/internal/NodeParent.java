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
package org.cdma.utilities.navigation.internal;

import org.cdma.Factory;
import org.cdma.IFactory;
import org.cdma.exception.BackupException;
import org.cdma.interfaces.IDataset;
import org.cdma.interfaces.IGroup;

public abstract class NodeParent {
    private final IDataset mDataset; // Handler on the dataset
    private IGroup mParent; // Parent group of this
    private String mName; // Short name of this
    private final String mFactory; // Plug-in factory name to be use for this

    /**
     * Return the string representing a separator between two parent elements
     * in a hierarchical path.
     */
    abstract protected String getPathSeparator();

    public NodeParent(String factory, IDataset dataset, IGroup parent, String name) throws BackupException {
        // Members direct affectation
        mFactory = factory;
        mDataset = dataset;
        mParent = parent;
        mName = name;

        // Check factory name validity
        if (factory == null || factory.isEmpty()) {
            throw new BackupException("IFactory name must be provided!");
        } else if (getFactory() == null) {
            throw new BackupException("Specified factory name doesn't exist!");
        }
    }

    public NodeParent(NodeParent object) throws BackupException {
        if (object == null) {
            throw new BackupException("Specified object to construct is null!");
        }
        mFactory = object.mFactory;
        mDataset = object.mDataset;
        mParent = object.mParent;
        mName = object.mName;
    }

    public String getLocation() {
        String result = null;

        if (mDataset != null) {
            result = mDataset.getLocation();
        }
        return result;
    }

    public String getName() {
        StringBuffer name = new StringBuffer();

        IGroup group = getParentGroup();
        if (group != null) {
            name.append(group.getName());
            name.append(getPathSeparator());
        }
        if (mName != null) {
            name.append(mName);
        }
        return name.toString();
    }

    public String getShortName() {
        return mName;
    }

    public void setShortName(String name) {
        mName = name;
    }

    public void setParent(IGroup group) {
        mParent = group;
    }

    public String getFactoryName() {
        return mFactory;
    }

    public IDataset getDataset() {
        return mDataset;
    }

    public IGroup getParentGroup() {
        return mParent;
    }

    public IGroup getRootGroup() {
        IGroup result = null;
        if (mDataset != null) {
            result = mDataset.getRootGroup();
        }
        return result;
    }

    public void setName(String name) {
        String[] shortNames = name.split(getPathSeparator());
        if (shortNames.length > 0) {
            setShortName(shortNames[shortNames.length - 1]);
            IGroup parent = getParentGroup();
            for (int i = shortNames.length - 2; i >= 0 && parent != null; i--) {
                parent.setShortName(shortNames[i]);
                parent = parent.getParentGroup();
            }
        }
    }

    // ------------------------------------------------------------------------
    // private methods
    // ------------------------------------------------------------------------
    protected IFactory getFactory() {
        return Factory.getFactory(mFactory);
    }
}
