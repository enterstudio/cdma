//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
//******************************************************************************
package org.cdma.utilities.memory;

import java.util.ArrayList;
import java.util.List;

import org.cdma.exception.InvalidRangeException;
import org.cdma.interfaces.IIndex;
import org.cdma.interfaces.IRange;

public class DefaultIndex implements IIndex, Cloneable {
	private int mRank;
	private int[] mICurPos; // Current position pointed by this index
	private DefaultRange[] mRanges; // Ranges that constitute the index global
									// view in each dimension
	private boolean mIsUpToDate; // Does the overall shape has changed
	private int mLastIndex;
	private int[] mProjShape; // shape without considering reduced range
	private int[] mProjOrigin; // origin without considering reduced range
	private long[] mProjStride; // stride without considering reduced range
	private String mFactory;

	// / Constructors
	public DefaultIndex(String factoryName, int[] shape) {
		this(factoryName, shape.clone(), new int[shape.length], shape.clone());
	}
	
	public DefaultIndex(String factoryName, final IIndex index) {
		this(factoryName, index.getShape().clone(), index.getOrigin(), index.getShape().clone());
	}

	public DefaultIndex(DefaultIndex index) {
		mRank = index.mRank;
		mRanges = new DefaultRange[index.mRanges.length];
		mICurPos = index.mICurPos.clone();
		mProjStride = index.mProjStride.clone();
		mProjShape = index.mProjShape.clone();
		mProjOrigin = index.mProjOrigin.clone();
		mLastIndex = index.mLastIndex;
		mIsUpToDate = index.mIsUpToDate;
		mFactory = index.mFactory;
		for (int i = 0; i < index.mRanges.length; i++) {
			mRanges[i] = (DefaultRange) index.mRanges[i].clone();
		}
	}

	/**
	 * Constructor
	 * @param factoryName of the plug-in
	 * @param shape of the storage this view describes (used to calculate stride)
	 * @param start position of the view
	 * @param length number of elements in each dimensions of this view
	 */
	public DefaultIndex(String factoryName, int[] shape, int[] start, int[] length) {
		long stride = 1;
		mRank = shape.length;
		mICurPos = new int[mRank];
		mRanges = new DefaultRange[mRank];
		mFactory = factoryName;
		mIsUpToDate = false;
		for (int i = mRank - 1; i >= 0; i--) {
			try {
				mRanges[i] = new DefaultRange( "",
												start[i] * stride,
												(start[i] + length[i] - 1) * stride,
												stride
											  );
				stride *= shape[i];
			} catch (InvalidRangeException e) {
				mRanges[i] = DefaultRange.EMPTY;
			}
		}
		updateProjection();
	}

	public DefaultIndex(String factoryName, List<IRange> ranges) {
		mFactory = factoryName;
		mIsUpToDate = false;
		mRank = ranges.size();
		mICurPos = new int[mRank];
		mRanges = new DefaultRange[mRank];
		int i = 0;
		for (IRange range : ranges) {
			mRanges[i] = new DefaultRange(range);
			i++;
		}
		updateProjection();
	}

	// ---------------------------------------------------------
	// / Public methods
	// ---------------------------------------------------------
	@Override
	public long currentElement() {
		long value = 0;
		try {
			for (int i = 0; i < mICurPos.length; i++) {
				value += (mRanges[i]).element(mICurPos[i]);
			}
		} catch (InvalidRangeException e) {
			value = -1;
		}
		return value;
	}

	@Override
	public int[] getCurrentCounter() {
		int[] curPos = new int[mRank];
		int i = 0;
		int j = 0;
		for (DefaultRange range : mRanges) {
			if (!range.reduced()) {
				curPos[i] = mICurPos[j];
				i++;
			}
			j++;
		}
		return curPos;
	}

	@Override
	public String getIndexName(int dim) {
		return (mRanges[dim]).getName();
	}

	@Override
	public int getRank() {
		return mRank;
	}

	@Override
	public int[] getShape() {
		int[] shape = new int[mRank];
		int i = 0;
		for (DefaultRange range : mRanges) {
			if (!range.reduced()) {
				shape[i] = range.length();
				i++;
			}
		}
		return shape;
	}

	@Override
	public long[] getStride() {
		long[] stride = new long[mRank];
		int i = 0;
		for (DefaultRange range : mRanges) {
			if (!range.reduced()) {
				stride[i] = range.stride();
				i++;
			}
		}
		return stride;
	}

	@Override
	public int[] getOrigin() {
		int[] origin = new int[mRank];
		int i = 0;
		for (DefaultRange range : mRanges) {
			if (!range.reduced()) {
				origin[i] = (int) (range.first() / range.stride());
				i++;
			}
		}
		return origin;
	}

	@Override
	public long getSize() {
		if (mRanges.length == 0) {
			return 0;
		}

		long size = 1;
		for (DefaultRange range : mRanges) {
			size *= range.length();
		}

		return size;
	}

	@Override
	public void setOrigin(int[] origins) {
		DefaultRange range;
		int i = 0;
		int j = 0;
		while (i < origins.length) {
			range = mRanges[j];
			if (!range.reduced()) {
				range.last(origins[i] * range.stride() + (range.length() - 1)
						* range.stride());
				range.first(origins[i] * range.stride());
				i++;
			}
			j++;
		}
		updateProjection();
	}

	@Override
	public void setShape(int[] value) {
		DefaultRange range;
		mIsUpToDate = false;
		int i = 0;
		int j = 0;
		while (i < value.length) {
			range = mRanges[j];
			if (!range.reduced()) {
				range.last(range.first() + (value[i] - 1) * range.stride());
				i++;
			}
			j++;
		}
		updateProjection();
	}

	@Override
	public void setStride(long[] stride) {
		DefaultRange range;
		if (stride == null) {
			return;
		}
		mIsUpToDate = false;
		int i = 0;
		int j = 0;
		while (i < stride.length) {
			range = mRanges[j];
			if (!range.reduced()) {
				range.stride(stride[i]);
				i++;
			}
			j++;
		}
		updateProjection();
	}

	@Override
	public IIndex set(int[] index) {
		if (index.length != mRank) {
			throw new IllegalArgumentException();
		}

		DefaultRange range;
		int i = 0;
		int j = 0;
		while (i < index.length) {
			range = mRanges[j];
			if (!range.reduced()) {
				mICurPos[j] = index[i];
				i++;
			}
			j++;
		}

		return this;
	}

	@Override
	public IIndex set(int v0) {
		int[] iCurPos = new int[mRank];
		iCurPos[DIM0] = v0;
		this.set(iCurPos);
		return this;
	}

	@Override
	public IIndex set(int v0, int v1) {
		int[] iCurPos = new int[mRank];
		iCurPos[DIM0] = v0;
		iCurPos[DIM1] = v1;
		this.set(iCurPos);
		return this;
	}

	@Override
	public IIndex set(int v0, int v1, int v2) {
		int[] iCurPos = new int[mRank];
		iCurPos[DIM0] = v0;
		iCurPos[DIM1] = v1;
		iCurPos[DIM2] = v2;
		this.set(iCurPos);
		return this;
	}

	@Override
	public IIndex set(int v0, int v1, int v2, int v3) {
		int[] iCurPos = new int[mRank];
		iCurPos[DIM0] = v0;
		iCurPos[DIM1] = v1;
		iCurPos[DIM2] = v2;
		iCurPos[DIM3] = v3;
		this.set(iCurPos);
		return this;
	}

	@Override
	public IIndex set(int v0, int v1, int v2, int v3, int v4) {
		int[] iCurPos = new int[mRank];
		iCurPos[DIM0] = v0;
		iCurPos[DIM1] = v1;
		iCurPos[DIM2] = v2;
		iCurPos[DIM3] = v3;
		iCurPos[DIM4] = v4;
		this.set(iCurPos);
		return this;
	}

	@Override
	public IIndex set(int v0, int v1, int v2, int v3, int v4, int v5) {
		int[] iCurPos = new int[mRank];
		iCurPos[DIM0] = v0;
		iCurPos[DIM1] = v1;
		iCurPos[DIM2] = v2;
		iCurPos[DIM3] = v3;
		iCurPos[DIM4] = v4;
		iCurPos[DIM5] = v5;
		this.set(iCurPos);
		return this;
	}

	@Override
	public IIndex set(int v0, int v1, int v2, int v3, int v4, int v5, int v6) {
		int[] iCurPos = new int[mRank];
		iCurPos[DIM0] = v0;
		iCurPos[DIM1] = v1;
		iCurPos[DIM2] = v2;
		iCurPos[DIM3] = v3;
		iCurPos[DIM4] = v4;
		iCurPos[DIM5] = v5;
		iCurPos[DIM6] = v6;
		this.set(iCurPos);
		return this;
	}

	@Override
	public IIndex set0(int v) {
		mICurPos[DIM0] = v;
		return this;
	}

	@Override
	public IIndex set1(int v) {
		mICurPos[DIM1] = v;
		return this;
	}

	@Override
	public IIndex set2(int v) {
		mICurPos[DIM2] = v;
		return this;
	}

	@Override
	public IIndex set3(int v) {
		mICurPos[DIM3] = v;
		return this;
	}

	@Override
	public IIndex set4(int v) {
		mICurPos[DIM4] = v;
		return this;
	}

	@Override
	public IIndex set5(int v) {
		mICurPos[DIM5] = v;
		return this;
	}

	@Override
	public IIndex set6(int v) {
		mICurPos[DIM6] = v;
		return this;
	}

	@Override
	public void setDim(int dim, int value) {
		if (dim >= mRank) {
			throw new IllegalArgumentException();
		}

		DefaultRange range;
		int i = 0;
		int j = 0;
		while (j < mRanges.length) {
			range = mRanges[j];
			if (!range.reduced()) {
				if (i == dim) {
					mICurPos[j] = value;
					return;
				}
				i++;
			}
			j++;
		}
	}

	@Override
	public void setIndexName(int dim, String indexName) {
		try {
			mIsUpToDate = false;
			int i = 0;
			int j = 0;
			DefaultRange range = null;
			while (i <= dim) {
				range = mRanges[j];
				if (!range.reduced()) {
					i++;
				}
				j++;
			}
			mRanges[i - 1] = new DefaultRange(indexName, range.first(),
					range.last(), range.stride());
		} catch (InvalidRangeException e) {
		}
	}

	@Override
	public IIndex reduce() {
		for (int ii = 0; ii < mICurPos.length; ii++) {
			// is there a dimension with length = 1
			if ((mRanges[ii]).length() == 1 && !mRanges[ii].reduced()) {
				// remove it
				this.reduce(0);

				// ensure there is not any more to do
				return this.reduce();
			}
		}
		mIsUpToDate = false;
		return this;
	}

	public IIndex unReduce() {
		for (DefaultRange range : mRanges) {
			// if range reduced
			if (range.reduced()) {
				// un-reduce it
				range.reduced(false);
				mRank++;
			}
		}
		mIsUpToDate = false;
		updateProjection();
		return this;
	}

	/**
	 * Create a new Index based on current one by eliminating the specified
	 * dimension;
	 * 
	 * @param dim
	 *            : dimension to eliminate: must be of length one, else
	 *            IllegalArgumentException
	 * @return the new Index
	 */
	@Override
	public IIndex reduce(int dim) {
		// search the correct range
		int i = 0;
		DefaultRange range = null;
		for (DefaultRange rng : mRanges) {
			if (!rng.reduced()) {
				if (i == dim) {
					range = rng;
					break;
				}
				i++;
			}
		}

		if ((dim < 0) || (dim >= mRank)) {
			throw new IllegalArgumentException("illegal reduce dim " + dim);
		}
		if (range.length() != 1) {
			throw new IllegalArgumentException("illegal reduce dim " + dim
					+ " : reduced dimension must be have length=1");
		}

		// Reduce proper range
		range.reduced(true);
		mRank--;
		updateProjection();
		return this;
	}

	@Override
	public String getFactoryName() {
		return mFactory;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		StringBuffer shp = new StringBuffer();
		str.append("Position: [");
		int i = 0;
		for (int pos : mICurPos) {
			if (i++ != 0) {
				str.append(", ");
			}
			str.append(pos);
		}
		i = 0;
		str.append("]  <=> Index: " + currentElement() + "\nRanges:\n");
		shp.append("Shape [");
		for (DefaultRange r : mRanges) {
			if (!r.reduced()) {
				if (i != 0) {
					shp.append(", ");
				}
				shp.append(r.length());
				str.append("- n�" + i + " " + (DefaultRange) r);
				if (i < mRanges.length) {
					str.append("\n");
				}
			}
			i++;
		}
		shp.append("]\n");
		shp.append(str);
		return shp.toString();
	}

	@Override
	public String toStringDebug() {
		StringBuilder sbuff = new StringBuilder();
		sbuff.setLength(0);
		int rank = mRanges.length;

		sbuff.append(" shape= ");
		for (int ii = 0; ii < rank; ii++) {
			sbuff.append((mRanges[ii]).length());
			sbuff.append(" ");
		}

		sbuff.append(" stride= ");
		for (int ii = 0; ii < rank; ii++) {
			sbuff.append((mRanges[ii]).stride());
			sbuff.append(" ");
		}

		long size = 1;
		for (int ii = 0; ii < rank; ii++) {
			size *= (mRanges[ii]).length();
		}
		sbuff.append(" size= ").append(size);
		sbuff.append(" rank= ").append(rank);

		sbuff.append(" current= ");
		for (int ii = 0; ii < rank; ii++) {
			sbuff.append(mICurPos[ii]);
			sbuff.append(" ");
		}

		return sbuff.toString();
	}

	@Override
	public long lastElement() {
		if (!mIsUpToDate) {
			int last = 0;
			for (IRange range : mRanges) {
				last += range.last();
			}
			mLastIndex = last;
			mIsUpToDate = true;
		}
		return mLastIndex;
	}

	@Override
	public IIndex clone() {
		return new DefaultIndex(this);
	}

	public List<IRange> getRangeList() {
		ArrayList<IRange> list = new ArrayList<IRange>();

		for (DefaultRange range : mRanges) {
			if (!range.reduced()) {
				list.add(range);
			}
		}
		return list;
	}

	/**
	 * Reduced ranges will also be considered
	 */
	public int[] getCurrentPos() {
		return mICurPos.clone();
	}

	/**
	 * Reduced ranges will also be considered
	 */
	public int[] getProjectionShape() {
		return mProjShape.clone();
	}

	/**
	 * Reduced ranges will also be considered
	 */
	public int[] getProjectionOrigin() {
		return mProjOrigin.clone();
	}

	/**
	 * Reduced ranges will also be considered
	 */
	public int currentProjectionElement() {
		int value = 0;

		for (int i = 0; i < mICurPos.length; i++) {
			value += mICurPos[i] * (mRanges[i]).stride();
		}
		return value;
	}

	// ---------------------------------------------------------
	// / Private methods
	// ---------------------------------------------------------
	private void updateProjection() {
		int realRank = mRanges.length;
		mProjStride = new long[realRank];
		mProjShape = new int[realRank];
		mProjOrigin = new int[realRank];
		long stride = 1;
		for (int i = realRank - 1; i >= 0; i--) {
			DefaultRange range = mRanges[i];
			mProjStride[i] = stride;
			mProjOrigin[i] = (int) (range.first() / range.stride());
			mProjShape[i] = range.length();
			stride *= range.reduced() ? 1 : range.length();
		}
	}

	private static final int DIM0 = 0;
	private static final int DIM1 = 1;
	private static final int DIM2 = 2;
	private static final int DIM3 = 3;
	private static final int DIM4 = 4;
	private static final int DIM5 = 5;
	private static final int DIM6 = 6;
}
