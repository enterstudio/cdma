package navigation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;
import ncsa.hdf.object.h5.H5ScalarDS;

import org.cdma.Factory;
import org.cdma.exception.DimensionNotSupportedException;
import org.cdma.exception.InvalidArrayTypeException;
import org.cdma.exception.InvalidRangeException;
import org.cdma.exception.NotImplementedException;
import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IAttribute;
import org.cdma.interfaces.IContainer;
import org.cdma.interfaces.IDataItem;
import org.cdma.interfaces.IDataset;
import org.cdma.interfaces.IDimension;
import org.cdma.interfaces.IGroup;
import org.cdma.interfaces.IIndex;
import org.cdma.interfaces.IRange;
import org.cdma.utils.DataType;
import org.cdma.utils.Utilities.ModelType;

import utils.HdfObjectUtils;
import array.HdfArray;
import array.HdfIndex;

public class HdfDataItem implements IDataItem, Cloneable {

    private final H5ScalarDS h5Item;
    private final H5File h5File;
    private final String factoryName;
    private IArray array;
    private final IGroup parent;

    public HdfDataItem(String factoryName, H5File file, IGroup parent, H5ScalarDS dataset) {
        this.factoryName = factoryName;
        this.h5File = file;
        this.h5Item = dataset;
        this.parent = parent;
        if (this.h5Item != null) {
            this.h5Item.init();
        }
    }

    public HdfDataItem(String factoryName) {
        this(factoryName, null, null, null);
    }

    private HdfDataItem(HdfDataItem dataItem) {
        this.parent = dataItem.parent;
        this.factoryName = dataItem.getFactoryName();
        this.h5File = dataItem.getH5File();
        this.h5Item = dataItem.getH5DataItem();
        if (this.h5Item != null) {
            this.h5Item.init();
        }
        try {
            this.array = dataItem.getData();
        }
        catch (IOException e) {
            Factory.getLogger().severe(e.getMessage());
        }
    }

    @Override
    public IDataItem clone() {
        return new HdfDataItem(this);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.DataItem;
    }

    public H5ScalarDS getH5DataItem() {
        return this.h5Item;
    }

    public H5File getH5File() {
        return this.h5File;
    }

    @Override
    public void addOneAttribute(IAttribute attribute) {
        HdfObjectUtils.addOneAttribute(h5Item, attribute);
    }

    @Override
    public void addStringAttribute(String name, String value) {
        HdfObjectUtils.addStringAttribute(h5Item, name, value);
    }

    public IAttribute getAttribute(String name, boolean ignoreCase) {
        IAttribute result = null;
        if (name != null && h5Item != null) {
            List<?> attributes;
            try {
                attributes = h5Item.getMetadata();
                for (Object attribute : attributes) {
                    IAttribute attr = new HdfAttribute(factoryName, (Attribute) attribute);
                    if (ignoreCase && name.equalsIgnoreCase(attr.getName())
                            || name.equals(attr.getName())) {
                        result = attr;
                        break;
                    }
                }
            }
            catch (HDF5Exception e) {
                Factory.getLogger().severe(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public IAttribute getAttribute(String name) {
        IAttribute result = null;
        result = getAttribute(name, false);
        return result;
    }

    @Override
    public List<IAttribute> getAttributeList() {
        List<IAttribute> result = HdfObjectUtils.getAttributeList(factoryName, h5Item);
        return result;
    }

    @Override
    public IDataset getDataset() {
        IDataset result = null;
        IContainer parentGroup = getParentGroup();
        if (parentGroup != null) {
            result = parentGroup.getDataset();
        }
        return result;
    }

    @Override
    public String getLocation() {
        String result = null;
        IContainer parentGroup = getParentGroup();
        if (parentGroup != null) {
            result = parentGroup.getLocation();
        }
        return result;
    }

    @Override
    public String getName() {
        IContainer parent = getParentGroup();
        return (parent == null ? "" : parent.getName() + "/") + getShortName();
    }

    @Override
    public IContainer getParentGroup() {
        return parent;
    }

    @Override
    public IContainer getRootGroup() {
        IContainer result = null;
        IContainer parent = getParentGroup();
        if (parent != null) {
            result = parent.getRootGroup();
        }
        return result;
    }

    @Override
    public String getShortName() {
        String result = null;
        if (h5Item != null) {
            result = h5Item.getName();
        }
        return result;
    }

    @Override
    public boolean hasAttribute(String name, String value) {
        boolean result = HdfObjectUtils.hasAttribute(h5Item, name, value);
        return result;
    }

    @Override
    public void setName(String name) {
        try {
            h5Item.setName(name);
        }
        catch (Exception e) {
            Factory.getLogger().log(Level.SEVERE, "Unable to setName", e);
        }
    }

    @Override
    public void setShortName(String name) {
        try {
            h5Item.setName(name);
        }
        catch (Exception e) {
            Factory.getLogger().log(Level.SEVERE, "Unable to setShortName", e);
        }
    }

    @Override
    public void setParent(IGroup group) {
        try {
            h5Item.setPath(group.getName());
        }
        catch (Exception e) {
            Factory.getLogger().log(Level.SEVERE, "Unable to setParent", e);
        }
    }

    @Override
    public long getLastModificationDate() {
        long result = 0;
        String fileName = h5Item.getFile();
        File currentFile = new File(fileName);
        if (currentFile != null && currentFile.exists()) {
            result = currentFile.lastModified();
        }
        return result;
    }

    @Override
    public String getFactoryName() {
        return this.factoryName;
    }

    @Override
    public IAttribute findAttributeIgnoreCase(String name) {
        IAttribute result = null;
        result = getAttribute(name, true);
        return result;
    }

    @Override
    public int findDimensionIndex(String name) {
        return 0;
    }

    @Override
    public IDataItem getASlice(int dimension, int value) throws InvalidRangeException {
        return getSlice(dimension, value);
    }

    @Override
    public IArray getData() throws IOException {
        if (array == null) {
            int[] shape = getShape();
            int[] origin = new int[getRank()];
            try {
                array = getData(origin, shape);
            }
            catch (Exception e) {
                Factory.getLogger().log(Level.SEVERE, "Unable to initialize data!", e);
            }
        }
        return array;
    }

    @Override
    public IArray getData(int[] origin, int[] shape) throws IOException, InvalidRangeException {
        IArray array = null;
        IIndex index = null;
        try {
            array = new HdfArray(factoryName, getType(), new int[0], this);
            index = new HdfIndex(this.factoryName, shape, origin, shape);
            array.setIndex(index);
        }
        catch (InvalidArrayTypeException e) {
            throw new InvalidRangeException(e);
        }

        return array;
    }

    public Object load(int[] origin, int[] shape) throws OutOfMemoryError, Exception {
        Object result = null;
        boolean viewHasChanged = false;
        synchronized (h5Item) {

            if (origin.length == h5Item.getDims().length || shape.length == origin.length) {
                // Set Selected dimensions
                long[] sDims = h5Item.getSelectedDims();

                if (!Arrays.equals(shape, HdfObjectUtils.convertLongToInt(sDims))) {
                    viewHasChanged = true;
                    for (int i = 0; i < sDims.length; i++) {
                        sDims[i] = shape[i];
                    }
                }

                // Set origin
                long[] startDims = h5Item.getStartDims();
                if (!Arrays.equals(origin, HdfObjectUtils.convertLongToInt(startDims))) {
                    viewHasChanged = true;
                    for (int i = 0; i < origin.length; i++) {
                        startDims[i] = origin[i];
                    }
                }

                if (viewHasChanged) {
                    // TODO DEBUG
                    System.out.println("Clearing dataset");
                    h5Item.clear();
                }
                result = h5Item.getData();
            }
        }
        return result;
    }

    @Override
    public String getDescription() {
        String result = null;
        IAttribute attribute = null;

        result = getAttribute("long_name").getStringValue();
        if (attribute == null) {
            result = getAttribute("description").getStringValue();
        }
        if (attribute == null) {
            result = getAttribute("title").getStringValue();
        }
        if (attribute == null) {
            result = getAttribute("standard_name").getStringValue();
        }
        if (attribute == null) {
            result = getAttribute("name").getStringValue();
        }
        return result;
    }

    @Override
    public List<IDimension> getDimensions(int index) {
        return new ArrayList<IDimension>();
    }

    @Override
    public List<IDimension> getDimensionList() {
        return new ArrayList<IDimension>();
    }

    @Override
    public String getDimensionsString() {
        return "";
    }

    @Override
    public int getElementSize() {
        int result = 0;
        if (h5Item != null) {
            Datatype type = h5Item.getDatatype();
            if (type != null) {
                result = type.getDatatypeSize();
            }
        }
        return result;
    }

    @Override
    public String getNameAndDimensions() {
        return null;
    }

    @Override
    public void getNameAndDimensions(StringBuffer buf, boolean longName, boolean length) {
    }

    @Override
    public List<IRange> getRangeList() {
        List<IRange> list = new ArrayList<IRange>();
        try {
            HdfIndex index = (HdfIndex) getData().getIndex();
            list.addAll(index.getRangeList());
        }
        catch (IOException e) {
            list = null;
        }
        return list;
    }

    @Override
    public int getRank() {
        return h5Item.getRank();
    }

    @Override
    public IDataItem getSection(List<IRange> section) throws InvalidRangeException {
        HdfDataItem item = null;
        try {
            item = new HdfDataItem(this);
            item.array = item.getData().getArrayUtils().sectionNoReduce(section).getArray();
        }
        catch (IOException e) {
        }
        return item;
    }

    @Override
    public List<IRange> getSectionRanges() {
        List<IRange> list = new ArrayList<IRange>();
        try {
            HdfIndex index = (HdfIndex) getData().getIndex();
            list.addAll(index.getRangeList());
        }
        catch (IOException e) {
            list = null;
        }
        return list;
    }

    @Override
    public int[] getShape() {
        long[] dims = h5Item.getDims();
        int[] result = new int[2];
        result = HdfObjectUtils.convertLongToInt(dims);
        return result;
    }

    @Override
    public long getSize() {
        long size = 1;
        long[] dims = h5Item.getDims();
        for (int i = 0; i < dims.length; i++) {
            if (dims[i] >= 0)
                size *= dims[i];
        }
        return size;
    }

    @Override
    public int getSizeToCache() {
        throw new NotImplementedException();
    }

    @Override
    public IDataItem getSlice(int dim, int value) throws InvalidRangeException {
        HdfDataItem item = new HdfDataItem(this);
        try {
            item.array = item.getData().getArrayUtils().slice(dim, value).getArray();
        }
        catch (Exception e) {
            item = null;
        }
        return item;
    }

    @Override
    public Class<?> getType() {
        Class<?> result = null;

        Datatype dType = h5Item.getDatatype();

        if (dType != null) {
            int datatype = dType.getDatatypeClass();

            if (DataType.BOOLEAN.equals(datatype)) {
                result = Boolean.TYPE;
            }
            else if (Datatype.CLASS_CHAR == datatype) {
                result = Character.TYPE;
            }
            else if (Datatype.CLASS_FLOAT == datatype) {
                switch (dType.getDatatypeSize()) {
                    case 4:
                        result = Float.TYPE;
                        break;
                    case 8:
                        result = Double.TYPE;
                        break;
                    default:
                        break;
                }
            }
            else if (Datatype.CLASS_INTEGER == datatype) {
                switch (dType.getDatatypeSize()) {
                    case 1:
                        result = Byte.TYPE;
                        break;
                    case 2:
                        result = Short.TYPE;
                        break;
                    case 4:
                        result = Integer.TYPE;
                        break;
                    case 8:
                        result = Long.TYPE;
                        break;
                    default:
                        break;
                }

            }
            else if (Datatype.CLASS_STRING == datatype) {
                result = String.class;
            }
        }
        return result;
    }

    @Override
    public String getUnitsString() {
        IAttribute attr = getAttribute("unit");
        String value = null;
        if (attr != null) {
            value = attr.getStringValue();
        }
        return value;
    }

    @Override
    public boolean hasCachedData() {
        throw new NotImplementedException();
    }

    @Override
    public void invalidateCache() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isCaching() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isMemberOfStructure() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isMetadata() {
        return (getAttribute("signal") == null);
    }

    @Override
    public boolean isScalar() {
        return (getRank() == 0);
    }

    @Override
    public boolean isUnlimited() {
        return false;
    }

    @Override
    public boolean isUnsigned() {
        return h5Item.getDatatype().isUnsigned();
    }

    @Override
    public byte readScalarByte() throws IOException {
        try {
            return java.lang.reflect.Array.getByte(h5Item.getData(), 0);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public double readScalarDouble() throws IOException {
        try {
            return java.lang.reflect.Array.getByte(h5Item.getData(), 0);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float readScalarFloat() throws IOException {
        try {
            return java.lang.reflect.Array.getFloat(h5Item.getData(), 0);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readScalarInt() throws IOException {
        try {
            return java.lang.reflect.Array.getInt(h5Item.getData(), 0);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readScalarLong() throws IOException {
        try {
            return java.lang.reflect.Array.getLong(h5Item.getData(), 0);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short readScalarShort() throws IOException {
        try {
            return java.lang.reflect.Array.getShort(h5Item.getData(), 0);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readScalarString() throws IOException {
        try {
            return new String((char[]) h5Item.getData());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeAttribute(IAttribute a) {
        return HdfObjectUtils.removeAttribute(h5Item, a);
    }

    @Override
    public void setCachedData(IArray cacheData, boolean isMetadata)
            throws InvalidArrayTypeException {
        array = cacheData;
    }

    @Override
    public void setCaching(boolean caching) {
        throw new NotImplementedException();
    }

    @Override
    public void setDataType(Class<?> dataType) {
        throw new NotImplementedException();
    }

    @Override
    public void setDimensions(String dimString) {

    }

    @Override
    public void setDimension(IDimension dim, int ind) throws DimensionNotSupportedException {

    }

    @Override
    public void setElementSize(int elementSize) {
        throw new NotImplementedException();
    }

    @Override
    public void setSizeToCache(int sizeToCache) {
        throw new NotImplementedException();
    }

    @Override
    public void setUnitsString(String units) {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DataItem = " + getName());
        return buffer.toString();
    }

    @Override
    public String toStringDebug() {
        StringBuffer strDebug = new StringBuffer();
        strDebug.append(getName());
        if (strDebug.length() > 0) {
            strDebug.append("\n");
        }
        try {
            strDebug.append("shape: ").append(getData().shapeToString()).append("\n");
            List<IDimension> dimensions = getDimensionList();
            for (IDimension dim : dimensions) {
                strDebug.append(dim.getCoordinateVariable().toString());
            }

            List<IAttribute> list = getAttributeList();
            if (list.size() > 0) {
                strDebug.append("\nAttributes:\n");
            }
            for (IAttribute a : list) {
                strDebug.append("- ").append(a.toString()).append("\n");
            }
        }
        catch (IOException e) {
        }
        return strDebug.toString();
    }

    public void save(FileFormat fileToWrite, Group parentInFile) throws Exception {
        try {
            Dataset ds = fileToWrite.createScalarDS(getShortName(), parentInFile,
                    h5Item.getDatatype(), h5Item.getDims(), h5Item.getMaxDims(), null, 0, null);
            if (ds != null) {
                ds.write(getData().getStorage());
            }
            List<IAttribute> attribute = getAttributeList();
            for (IAttribute iAttribute : attribute) {
                HdfAttribute attr = (HdfAttribute) iAttribute;
                attr.save(ds);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String writeCDL(String indent, boolean useFullName, boolean strict) {
        throw new NotImplementedException();
    }

    public static void main(String[] args) {

        System.setProperty(H5.H5PATH_PROPERTY_KEY,
                "/home/viguier/LocalSoftware/hdf-java/lib/linux/libjhdf5.so");

        String fileName = "/home/viguier/NeXusFiles/ANTARES/CKedge_2010-12-10_21-51-59.nxs";
        String datasetPath = "/Pattern_281_salsa.Microscopy.Scan2D_Scienta_Is_XIA_vs_PIXY_1/scan_data/actuator_1_1/";

        try {
            H5File file = new H5File(fileName);
            file.open();
            HObject hobject = file.get(datasetPath);
            H5ScalarDS ds = (H5ScalarDS) hobject;
            ds.read();

            long[] dims = ds.getDims();
            System.out.println("Dims =" + Arrays.toString(dims));

            List metaData = ds.getMetadata();
            System.out.println("MetaData =" + Arrays.toString(metaData.toArray()));

            String targetName = ds.getLinkTargetObjName();
            System.out.println("Target = " + targetName);

            String parent = ds.getPath();
            System.out.println("Path = " + parent);

            HObject parentObject = file.get(ds.getPath());
            H5Group group = (H5Group) parentObject;
            System.out.println("Parent = " + group);

            // ds.clearData();
            // int[] origin;
            // int[] shape;
            //
            // HdfDataItem item = new HdfDataItem("test", file, null, ds);
            // origin = new int[] { 0, 0 };
            // shape = new int[] { 101, 101 };
            // IArray a1 = item.getData(origin, shape);
            // Object a1Storage = a1.getStorage();
            //
            // origin = new int[] { 2, 2 };
            // shape = new int[] { 10, 10 };
            // IArray a2 = item.getData(origin, shape);
            // Object a2Storage = a2.getStorage();
            //
            // System.out.println("a1Storage");
            // for (int i = 0; i < 10; i++) {
            // System.out.println(Array.get(a1Storage, i));
            // }
            // System.out.println("a2Storage");
            // for (int i = 0; i < 10; i++) {
            // System.out.println(Array.get(a2Storage, i));
            // }


            // byte[] b = ds.readBytes();
            // for (int i = 0; i < 10; i++) {
            // System.out.print(Array.get(b, i));
            // System.out.print(",");
            // }
            // // TODO DEBUG
            // System.out.println();
            //
            // long[] sD = ds.getStartDims();
            // sD[1] = 1L;
            // ds.clear();
            // b = ds.readBytes();
            // for (int i = 0; i < 10; i++) {
            // System.out.print(Array.get(b, i));
            // System.out.print(",");
            // }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
