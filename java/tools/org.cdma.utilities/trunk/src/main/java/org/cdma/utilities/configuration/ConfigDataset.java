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
package org.cdma.utilities.configuration;

import java.util.List;

import org.cdma.interfaces.IDataset;
import org.cdma.utilities.configuration.internal.ConfigCriteria;
import org.cdma.utilities.configuration.internal.ConfigGeneric;
import org.cdma.utilities.configuration.internal.ConfigParameter;

/**
 * ConfigDataset defines some criteria that will permit to verify the given IDataset
 * matches it. A IDataset of the plug-in has one and only one configuration.
 * <p>
 * It permits to determines some parameters that can be statically or dynamically (according to some conditions or
 * values in dataset) fixed for a that specific data model. Those parameters (ConfigParameter) are resolved according
 * the given IDataset.
 * <p>
 * Each IDataset should match a specific ConfigDataset.
 * 
 * @see ConfigParameter ConfigParameter: interface a parameter must implement
 * @see ConfigCriteria ConfigCriteria: criteria that an IDataset must respect to match a ConfigDataset
 * @author rodriguez
 * 
 */

public class ConfigDataset {
    private final ConfigGeneric mConfig;
    private final IDataset mDataset;

    /**
     * Constructor of the dataset configuration
     * need a dom element named: "config_dataset"
     * 
     * @param dataset_model DOM element "dataset_model"
     * @param params some default parameters that can be override by this Config
     */
    public ConfigDataset(ConfigGeneric config, IDataset dataset) {
        mConfig = config;
        mDataset = dataset;
    }

    /**
     * Return the label of that ConfigDataset
     * 
     * @return
     */
    public String getLabel() {
        return mConfig.getLabel();
    }

    /**
     * Returns the list of all parameters that can be asked for that configuration
     * 
     * @return list of ConfigParameter
     */
    public List<ConfigParameter> getParameters() {
        return mConfig.getParameters();
    }

    /**
     * Returns the value of the named <b>ConfigParameter</b> for the given IDataset.
     * 
     * @param label of the parameter
     * @return the string value of the parameter
     */
    public String getParameter(String label) {
        return mConfig.getParameter(label, mDataset);
    }

    /**
     * Return true if the given parameter name is present in the configuration.
     * 
     * @param label
     */
    public boolean hasParameter(String label) {
        boolean result = false;

        for (ConfigParameter param : mConfig.getParameters()) {
            if (param.getName().equals(label)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the criteria a IDataset must respect to match that configuration.
     * 
     * @return ConfigCriteria object
     */
    public ConfigCriteria getCriteria() {
        return mConfig.getCriteria();
    }

    /**
     * Add a parameter to that configuration
     * 
     * @param param implementing ConfigParameter interface
     */
    public void addParameter(ConfigParameter param) {
        mConfig.addParameter(param);
    }

    @Override
    public String toString() {
        String result = mConfig + "\nFor dataset: " + mDataset.getLocation();

        return result;
    }
}
