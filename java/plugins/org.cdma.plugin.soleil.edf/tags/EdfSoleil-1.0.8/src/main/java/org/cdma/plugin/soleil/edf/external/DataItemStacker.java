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
//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
//******************************************************************************
package org.cdma.plugin.soleil.edf.external;

import java.util.ArrayList;
import java.util.List;

import org.cdma.dictionary.Context;
import org.cdma.dictionary.IPluginMethod;
import org.cdma.exception.CDMAException;
import org.cdma.interfaces.IContainer;
import org.cdma.interfaces.IDataItem;
import org.cdma.plugin.soleil.edf.EdfFactory;
import org.cdma.plugin.soleil.edf.navigation.EdfDataItem;
import org.cdma.utils.Utilities.ModelType;

/**
 * Stack all found data items to construct an aggregated NxsDataItem
 */
public final class DataItemStacker implements IPluginMethod {

    @Override
    public void execute(Context context) throws CDMAException {
        List<IContainer> result = new ArrayList<IContainer>();
        IDataItem item = stackDataItems(context);
        if (item != null) {
            result.add(item);
        }
        context.setContainers(result);
    }

    @Override
    public String getFactoryName() {
        return EdfFactory.NAME;
    }

    public IDataItem stackDataItems(Context context) {
        IDataItem item = null;

        // Get all previously found nodes
        List<IDataItem> items = new ArrayList<IDataItem>();
        List<IContainer> nodes = context.getContainers();
        for (IContainer node : nodes) {
            if (node.getModelType() == ModelType.DataItem) {
                items.add((IDataItem) node);
            }
        }

        if (!items.isEmpty()) {
            item = new EdfDataItem(items.toArray(new EdfDataItem[items.size()]));
        }

        return item;
    }
}
