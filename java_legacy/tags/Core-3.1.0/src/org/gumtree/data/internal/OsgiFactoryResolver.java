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
package org.gumtree.data.internal;

/// @cond internal

import org.gumtree.data.IFactory;
import org.gumtree.data.utils.IFactoryManager;
import org.gumtree.data.utils.IFactoryResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OsgiFactoryResolver implements IFactoryResolver {

    public void discoverFactories(IFactoryManager manager) {
        BundleContext context = Activator.getDefault().getContext();
        ServiceReference[] refs = null;
        try {
            refs = context.getServiceReferences(IFactory.class.getName(), null);
        } catch (InvalidSyntaxException e) {
        }
        if (refs != null) {
            for (ServiceReference ref : refs) {
                IFactory factory = (IFactory) context.getService(ref);
                manager.registerFactory(factory.getName(), factory);
            }
        }
    }

}

/// @endcond internal