package org.cdma.plugin.edf.array;

import org.cdma.exception.ShapeNotMatchException;
import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IDimension;
import org.cdma.plugin.edf.EdfFactory;

public class BasicDimension implements IDimension, Cloneable {

    private IArray array;
    private String longName;
    private boolean variableLength;
    private boolean unlimited;
    private boolean shared;

    public BasicDimension(String longName, IArray array, boolean variableLength, boolean unlimited,
            boolean shared) {
        super();
        this.longName = longName;
        this.array = array;
        this.variableLength = variableLength;
        this.unlimited = unlimited;
    }

    @Override
    public int compareTo(Object o) {
        IDimension dim = (IDimension) o;
        return longName.compareTo(dim.getName());
    }

    @Override
    public IArray getCoordinateVariable() {
        return array;
    }

    @Override
    public int getLength() {
        return Long.valueOf(array.getSize()).intValue();
    }

    @Override
    public String getName() {
        return longName;
    }

    @Override
    public boolean isShared() {
        return shared;
    }

    @Override
    public boolean isUnlimited() {
        return unlimited;
    }

    @Override
    public boolean isVariableLength() {
        return variableLength;
    }

    @Override
    public void setLength(int n) {
        try {
            array.getArrayUtils().reshape(new int[] { n });
        }
        catch (ShapeNotMatchException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setName(String name) {
        longName = name;
    }

    @Override
    public void setShared(boolean b) {
        shared = b;
    }

    @Override
    public void setUnlimited(boolean b) {
        unlimited = b;
    }

    @Override
    public void setVariableLength(boolean b) {
        variableLength = b;
    }

    @Override
    public void setCoordinateVariable(IArray array) throws ShapeNotMatchException {
        if (java.util.Arrays.equals(array.getShape(), array.getShape()))
            throw new ShapeNotMatchException("Arrays must have same shape!");
        this.array = array;
    }

    @Override
    public String writeCDL(boolean strict) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Object clone() {
        try {
            BasicDimension clone = (BasicDimension) super.clone();
            clone.array = array.copy();
            return clone;
        }
        catch (CloneNotSupportedException e) {
            // Should not happen because this class is Cloneable
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getFactoryName() {
        return EdfFactory.NAME;
    }

}
