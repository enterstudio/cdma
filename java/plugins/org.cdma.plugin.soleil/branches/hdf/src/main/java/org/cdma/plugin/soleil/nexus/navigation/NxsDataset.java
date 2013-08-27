//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
//******************************************************************************
package org.cdma.plugin.soleil.nexus.navigation;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.cdma.Factory;
import org.cdma.dictionary.ExtendedDictionary;
import org.cdma.dictionary.LogicalGroup;
import org.cdma.engine.hdf.navigation.HdfDataset;
import org.cdma.engine.hdf.navigation.HdfGroup;
import org.cdma.exception.FileAccessException;
import org.cdma.exception.InvalidArrayTypeException;
import org.cdma.exception.NoResultException;
import org.cdma.exception.WriterException;
import org.cdma.interfaces.IAttribute;
import org.cdma.interfaces.IContainer;
import org.cdma.interfaces.IDataset;
import org.cdma.interfaces.IGroup;
import org.cdma.plugin.soleil.nexus.NxsFactory;
import org.cdma.plugin.soleil.nexus.array.NxsArray;
import org.cdma.plugin.soleil.nexus.dictionary.NxsLogicalGroup;
import org.cdma.plugin.soleil.nexus.internal.DetectedSource.NeXusFilter;
import org.cdma.plugin.soleil.nexus.internal.NexusDatasetImpl;
import org.cdma.utilities.configuration.ConfigDataset;
import org.cdma.utilities.configuration.ConfigManager;
import org.cdma.utils.Utilities.ModelType;

public final class NxsDataset implements IDataset {
    private boolean           mOpen;         // is the dataset open
    private URI                mPath;         // URI of this dataset
    private ConfigDataset      mConfig;       // Configuration associated to this dataset
    private final List<HdfDataset> mDatasets; // HdfDataset compounding this NxsDataset
    private IGroup             mRootPhysical; // Physical root of the document
    private NxsLogicalGroup    mRootLogical;  // Logical root of the document

    @Override
    public int hashCode() {
        int code = 0xDA7A;
        int mult = 0x60131;
        code = code * mult + new File(mPath.getPath()).hashCode();
        return code;
    }

    // SoftReference of dataset associated to their URI
    private static Map<String, SoftReference<NxsDataset>> datasets;

    // datasets' URIs associated to the last modification
    private static Map<String, Long> lastModifications;

    public static NxsDataset instanciate(URI destination) throws NoResultException {
        NxsDataset dataset = null;
        if (datasets == null) {
            synchronized (NxsDataset.class) {
                if (datasets == null) {
                    datasets = new HashMap<String, SoftReference<NxsDataset>>();
                    lastModifications = new HashMap<String, Long>();
                }
            }
        }

        synchronized (datasets) {
            String uri = destination.toString();
            SoftReference<NxsDataset> ref = datasets.get( uri );
            if (ref != null) {
                dataset = ref.get();
                long last = lastModifications.get( uri );
                if( dataset != null ) {
                    if( last < dataset.getLastModificationDate() ) {
                        dataset = null;
                    }
                }
            }

            if (dataset == null) {
                String filePath = destination.getPath();
                if( filePath != null ) {
                    try {
                        dataset = new NxsDataset(new File(filePath));
                        String fragment = destination.getFragment();

                        if (fragment != null && !fragment.isEmpty()) {
                            IGroup group = dataset.getRootGroup();
                            try {
                                String path = URLDecoder.decode(fragment, "UTF-8");
                                for (IContainer container : group.findAllContainerByPath(path)) {
                                    if (container.getModelType().equals(ModelType.Group)) {
                                        dataset.mRootPhysical = (IGroup) container;
                                        break;
                                    }
                                }
                            }
                            catch (UnsupportedEncodingException e) {
                                Factory.getLogger().log( Level.WARNING, e.getMessage());
                            }
                        }
                        datasets.put( uri, new SoftReference<NxsDataset>(dataset));
                        lastModifications.put( uri, dataset.getLastModificationDate());
                    }
                    catch ( FileAccessException e ) {
                        throw new NoResultException( e );
                    }
                }
            }
        }
        return dataset;
    }

    @Override
    public String getFactoryName() {
        return NxsFactory.NAME;
    }

    @Override
    public LogicalGroup getLogicalRoot() {
        if (mRootLogical == null) {
            String param;
            try {
                param = getConfiguration().getParameter(NxsFactory.DEBUG_INF);
                boolean debug = Boolean.parseBoolean(param);
                mRootLogical = new NxsLogicalGroup(null, null, this, debug);
            }
            catch (NoResultException e) {
                Factory.getLogger().log( Level.WARNING, e.getMessage());
            }
        }
        else {
            ExtendedDictionary dict = mRootLogical.getDictionary();
            if (dict != null && !Factory.getActiveView().equals(dict.getView())) {
                mRootLogical.setDictionary(mRootLogical.findAndReadDictionary());
            }
        }
        return mRootLogical;
    }

    @Override
    public IGroup getRootGroup() {
        if (mRootPhysical == null && mDatasets.size() > 0) {
            HdfGroup[] groups = new HdfGroup[mDatasets.size()];
            int i = 0;
            for (IDataset dataset : mDatasets) {
                groups[i++] = (HdfGroup) dataset.getRootGroup();
            }
            mRootPhysical = new NxsGroup(groups, null, this);
        }
        return mRootPhysical;
    }

    @Override
    public void saveTo(String location) throws WriterException {
        for (IDataset dataset : mDatasets) {
            dataset.saveTo(location);
        }
    }

    @Override
    public void save(IContainer container) throws WriterException {
        for (IDataset dataset : mDatasets) {
            dataset.save(container);
        }
    }

    @Override
    public void save(String parentPath, IAttribute attribute) throws WriterException {
        for (IDataset dataset : mDatasets) {
            dataset.save(parentPath, attribute);
        }
    }

    @Override
    public boolean sync() throws IOException {
        boolean result = true;
        for (IDataset dataset : mDatasets) {
            if (!dataset.sync()) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        mOpen = false;
    }

    @Override
    public String getLocation() {
        return mPath.toString();
    }

    @Override
    public String getTitle() {
        String title = getRootGroup().getShortName();
        if (title.isEmpty()) {
            try {
                title = mDatasets.get(0).getTitle();
            }
            catch (NoSuchElementException e) {
            }
        }

        return title;
    }

    @Override
    public void setLocation(String location) {
        if (location != null && !location.equals(mPath.toString())) {
            try {
                mPath = new URI(location);
            }
            catch (URISyntaxException e) {
                Factory.getLogger().log( Level.WARNING, e.getMessage());
            }
            mDatasets.clear();
        }
    }

    @Override
    public void setTitle(String title) {
        try {
            mDatasets.get(0).setTitle(title);
        }
        catch (NoSuchElementException e) {
        }
    }

    @Override
    public void open() throws IOException {
        mOpen = true;
        for (IDataset dataset : mDatasets) {
            dataset.open();
        }
    }

    @Override
    public void save() throws WriterException {
        for (IDataset dataset : mDatasets) {
            dataset.save();
        }
    }

    @Override
    public boolean isOpen() {
        return mOpen;
    }

    public ConfigDataset getConfiguration() throws NoResultException {
        if (mConfig == null) {
            if (mDatasets.size() > 0) {
                ConfigDataset conf;
                conf = ConfigManager.getInstance(NxsFactory.getInstance(), NxsFactory.CONFIG_FILE).getConfig(this);
                mConfig = conf;
            }
        }
        return mConfig;
    }

    @Override
    public long getLastModificationDate() {
        long last = 0;
        long temp = 0;

        File path = new File( mPath.getPath() );
        if( path.exists() && path.isDirectory() ) {
            last = path.lastModified();
        }

        for (HdfDataset dataset : mDatasets) {
            temp = dataset.getLastModificationDate();
            if (temp > last) {
                last = temp;
            }
        }

        return last;
    }



    // ---------------------------------------------------------
    // / Private methods
    // ---------------------------------------------------------
    private NxsDataset(File destination) throws FileAccessException {
        mPath = destination.toURI();
        mDatasets = new ArrayList<HdfDataset>();
        HdfDataset datafile;
        if (destination.exists() && destination.isDirectory()) {
            NeXusFilter filter = new NeXusFilter();
            File[] files = destination.listFiles(filter);
            if (files != null && files.length > 0) {
                for (File file : files) {
                    datafile = new NexusDatasetImpl(file);
                    mDatasets.add(datafile);
                }
            }
        }
        else {
            datafile = new NexusDatasetImpl(destination);
            mDatasets.add(datafile);
        }
        mOpen = false;
    }

    @Override
    public String toString() {
        String result = "Dataset with path = " + mPath.toString();
        return result;
    }

    public static void main(String... args) throws NoResultException, URISyntaxException, WriterException,
    InvalidArrayTypeException {
        String fileName = "/home/viguier/writeTests.nxs";
        String fileNameToWrite = "/home/viguier/writeTestsFinal.nxs";

        // Add group1
        NxsDataset dataset = NxsDataset.instanciate(new URI("file://" + fileName));
        NxsGroup group1 = new NxsGroup(dataset, "group1", fileName, "/group1", null);
        dataset.getRootGroup().addSubgroup(group1);

        NxsGroup group2 = new NxsGroup(dataset, "group2", fileName, "/group2", null);
        dataset.getRootGroup().addSubgroup(group2);

        // Add data1 to group1
        NxsDataItem dataItem1 = new NxsDataItem();
        dataItem1.setShortName("data1");
        int[] values1 = { 1, 2, 3, 4, 5, 6, 7 };
        int[] shape1 = { 1, 7 };
        NxsArray array1 = new NxsArray(values1, shape1);
        dataItem1.setCachedData(array1, false);

        NxsDataItem dataItem2 = new NxsDataItem();
        dataItem1.setShortName("data2");
        String[] values2 = { "sylvain", "mr bonbon" };
        int[] shape2 = { 1, 2 };
        NxsArray array2 = new NxsArray(values2, shape2);
        dataItem2.setCachedData(array2, false);

        group1.addDataItem(dataItem1);
        group1.addDataItem(dataItem2);
        group2.addDataItem(dataItem2);

        dataset.saveTo(fileNameToWrite);

    }
}




