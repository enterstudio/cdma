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
// ****************************************************************************
// Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//    Clément Rodriguez - initial API and implementation
// ****************************************************************************
package org.cdma.internal.dictionary.solvers;

/// @cond internal

/**
 * @brief ItemSolver aims to return an IContainer that corresponds to the mapping.
 * 
 * The ItemSolver class as an unique method which solve. The goal is to return
 * the IContainer that is defined into the institute's mapping. To do so, it uses
 * a Context which permits to access the CDMA environment. The IContainer can be
 * searched in a specific path, or might need a named method execution a to construct it.
 * <p>
 * That class is used internally by the LogicalGroup to construct item.
 * 
 * @see org.cdma.dictionary.IPluginMethod
 * @see org.cdma.dictionary.Context
 * 
 * @author rodriguez
 *
 */

import java.util.ArrayList;
import java.util.List;

import org.cdma.IFactory;
import org.cdma.dictionary.Context;
import org.cdma.dictionary.IPluginMethod;
import org.cdma.dictionary.Path;
import org.cdma.dictionary.filter.IFilter;
import org.cdma.interfaces.IAttribute;
import org.cdma.interfaces.IContainer;
import org.cdma.interfaces.IKey;
import org.cdma.internal.dictionary.classloader.PluginMethodManager;
import org.cdma.utils.Utilities.ModelType;
import org.jdom2.Element;

public class ItemSolver {
    IFactory mFactory;                 // IFactory instance of the plug-in using this item solver
    List<Solver> mContent;             // List of solvers to process to get IContainer content
    List<AttributeSolver> mAttributes; // List of attribute solvers to process to get IContainer attributes
    ModelType mModelType;              // The model type of created item when solved

    public ItemSolver(IFactory factory, Path path) {
        mFactory = factory;

        // Prepare list of solver
        mContent = new ArrayList<Solver>();

        // Prepare list of attribute solvers
        mAttributes = new ArrayList<AttributeSolver>();

        // Set the model type
        mModelType = ModelType.DataItem;

        // Add the solver to this item solver
        Solver solver = new Solver(path);
        mContent.add(solver);
    }

    public ItemSolver(IFactory factory, PluginMethodManager manager, Element elem) {
        mFactory = factory;

        // Prepare list of solver
        mContent = new ArrayList<Solver>();

        // Prepare list of attribute solvers
        mAttributes = new ArrayList<AttributeSolver>();

        mModelType = ModelType.DataItem;

        // Initialize internal fields
        init(manager, elem);
    }

    public ItemSolver(IFactory factory, IKey key) {
        mFactory = factory;

        // Prepare list of solver
        mContent = new ArrayList<Solver>();

        // Prepare list of attribute solvers
        mAttributes = new ArrayList<AttributeSolver>();

        // Initialize internal fields
        mContent.add(new Solver(key));
        mModelType = ModelType.LogicalGroup;
    }

    public List<IContainer> solve(Context context) {
        List<IContainer> valid = new ArrayList<IContainer>();
        List<IContainer> found = new ArrayList<IContainer>();

        // Get the key from the context
        IKey key = context.getKey();

        // Execute sequentially each solver
        for (Solver solver : mContent) {
            found = solver.solve(context);

            // Update the context with the last found item
            if (found != null && !found.isEmpty()) {
                context.setContainers(found);
            }
        }

        found = context.getContainers();

        // Add attributes defined in the solver
        if (!mAttributes.isEmpty()) {
            context.setContainers(new ArrayList<IContainer>());
            IAttribute attrib;

            // For each valid container
            for (IContainer container : found) {
                // For each attributes to create
                for (AttributeSolver solver : mAttributes) {

                    // Update the context with container to process as a parameter
                    context.setParams( new IContainer[] {container});

                    // Resolve the attribute
                    attrib = solver.solve(context);

                    // Add the attribute to the IContainer
                    if (attrib != null) {
                        container.addOneAttribute(attrib);
                    }
                }
            }
        }

        // Check found items match what is requested by key (filters on key)
        for (IContainer container : found) {
            if (isValidContainer(key, container)) {
                valid.add(container);
            }
        }
        // Update the context with the last found item
        context.setContainers(valid);

        return valid;
    }

    // ---------------------------------------------------------
    // / Private methods
    // ---------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void init(PluginMethodManager manager, Element elem) {
        // Temporary variables
        IPluginMethod method;
        Solver current;

        // List DOM children
        List<?> nodes = elem.getChildren();
        List<?> attrs;

        // For each children of the mapping key item
        for (Element node : (List<Element>) nodes) {
            // If path open the path
            if (node.getName().equals("path")) {
                current = new Solver(mFactory.createPath(node.getText()));
                mContent.add(current);
            }
            // If call on a method
            else if (node.getName().equals("call")) {
                String param = node.getAttributeValue("param");
                String param_args = node.getAttributeValue("param_args");

                method = manager.getPluginMethod(mFactory.getName(), node.getText());
                current = new Solver(method, new String[] { param, param_args });
                mContent.add(current);
            }
            // If attribute
            else if (node.getName().equals("attribute")) {
                // For each children of the mapping attribute
                attrs = node.getChildren();
                String attrName  = node.getAttributeValue("name");
                String attrValue = node.getAttributeValue("value");

                AttributeSolver attr = null;
                if( attrValue != null ) {
                    attr = new AttributeSolver(attrName, attrValue);;
                }
                else {
                    // Create a list of solver for that particular attribute
                    List<Solver> attrSolv = new ArrayList<Solver>();
                    for (Element subNode : (List<Element>) attrs) {
                        // If path open the path
                        if (subNode.getName().equals("path")) {
                            current = new Solver(mFactory.createPath(subNode.getText()));
                            attrSolv.add(current);
                        }
                        // If call on a method
                        else if (subNode.getName().equals("call")) {
                            method = manager.getPluginMethod(mFactory.getName(), subNode.getText());
                            current = new Solver(method);
                            attrSolv.add(current);
                        }
                    }
                    attr = new AttributeSolver(attrName, attrSolv);
                }
                // Store the attribute into a list
                mAttributes.add(attr);
            }
        }
    }

    /**
     * Check if the IContainer is conform to the IKey's filters
     * 
     * @param key IKey with filters if any
     * @param container IContainer to check
     * @return true if the IContainer is compliant with the IKey
     */
    private boolean isValidContainer(IKey key, IContainer container) {
        boolean result = true;
        if (container != null) {
            for (IFilter filter : key.getFilterList()) {
                if ((filter!=null) && (!filter.matches(container))) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the model type of instantiated object when solve is called
     * @return the ModelType of item
     */
    public ModelType getModelType() {
        return mModelType;
    }
}

// / @endcond internal
