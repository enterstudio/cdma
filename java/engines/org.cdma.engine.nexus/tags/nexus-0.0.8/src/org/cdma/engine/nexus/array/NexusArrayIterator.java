//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
//******************************************************************************
package org.cdma.engine.nexus.array;

import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IArrayIterator;
import org.cdma.interfaces.IIndex;
import org.cdma.utilities.memory.ArrayTools;

public final class NexusArrayIterator implements IArrayIterator {
	// / Members
	private IArray mArray;
	private IIndex mIndex;
	private Object mCurrent;
	private boolean mAccess; // Indicates that this can access the storage memory or not

	public NexusArrayIterator(NexusArray array) {
		mArray = array;
		// [ANSTO][Tony][2011-08-31] Should mAccess set to true for NxsArrayInterface?? If m_access is set to false, next() does not work.
		// [SOLEIL][Clement][2011-11-22] Yes it should. It indicates that the iterator shouldn't access memory. 
		// In case of hudge matrix the next() will update m_current (i.e. value), but the underlying NeXus engine
		// will automatically load the part corresponding to the view defined by this iterator, 
		// which can lead to java heap space memory exception (see NxsArray : private Object getData() )
		mAccess = true;
		try {
			mIndex = array.getIndex().clone();
			mIndex.set(new int[mIndex.getRank()]);
			mIndex.setDim(mIndex.getRank() - 1, -1);
		} catch (CloneNotSupportedException e) {
		}
	}

	public NexusArrayIterator(IArray array, IIndex index) {
		this(array, index, true);
	}

	public NexusArrayIterator(IArray array, IIndex index, boolean accessData) {
		int[] count = index.getCurrentCounter();
		mArray = array;
		mIndex = index;
		mAccess = accessData;
		count[mIndex.getRank() - 1]--;
		mIndex.set(count);
	}

	@Override
	public boolean getBooleanNext() {
		incrementIndex();
		return mArray.getBoolean(mIndex);
	}

	@Override
	public byte getByteNext() {
		incrementIndex();
		return mArray.getByte(mIndex);
	}

	@Override
	public char getCharNext() {
		return ((Character) next()).charValue();
	}

	@Override
	public int[] getCounter() {
		return mIndex.getCurrentCounter();
	}

	@Override
	public double getDoubleNext() {
		incrementIndex();
		return mArray.getDouble(mIndex);
	}

	@Override
	public float getFloatNext() {
		incrementIndex();
		return mArray.getFloat(mIndex);
	}

	@Override
	public int getIntNext() {
		incrementIndex();
		return mArray.getInt(mIndex);
	}

	@Override
	public long getLongNext() {
		incrementIndex();
		return mArray.getLong(mIndex);
	}

	@Override
	public Object getObjectNext() {
		return next();
	}

	@Override
	public short getShortNext() {
		incrementIndex();
		return mArray.getShort(mIndex);
	}

	@Override
	public boolean hasNext() {
		long index = mIndex.currentElement();
		long last = mIndex.lastElement();
		return (index < last && index >= -1);
	}

	@Override
	public Object next() {
		incrementIndex();
		if (mAccess) {
			long currentPos = mIndex.currentElement();
			if (currentPos <= mIndex.lastElement() && currentPos != -1) {
				mCurrent = mArray.getObject(mIndex);
			} else {
				mCurrent = null;
			}
		}
		return mCurrent;
	}

	@Override
	public void setBoolean(boolean val) {
		setObject(val);
	}

	@Override
	public void setByte(byte val) {
		setObject(val);
	}

	@Override
	public void setChar(char val) {
		setObject(val);
	}

	@Override
	public void setDouble(double val) {
		setObject(val);
	}

	@Override
	public void setFloat(float val) {
		setObject(val);
	}

	@Override
	public void setInt(int val) {
		setObject(val);
	}

	@Override
	public void setLong(long val) {
		setObject(val);
	}

	@Override
	public void setObject(Object val) {
		mCurrent = val;
		mArray.setObject(mIndex, val);
	}

	@Override
	public void setShort(short val) {
		setObject(val);
	}

	@Override
	public String getFactoryName() {
		return mArray.getFactoryName();
	}

	// / protected method
	protected void incrementIndex() {
		int[] current = mIndex.getCurrentCounter();
		int[] shape = mIndex.getShape();
		ArrayTools.incrementCounter(current, shape);
		mIndex.set(current);
	}
}