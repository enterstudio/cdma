//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
//******************************************************************************
package org.cdma.engine.sql.utils;

import org.cdma.Factory;
import org.cdma.IFactory;
import org.cdma.engine.sql.array.SqlArray;
import org.cdma.exception.ShapeNotMatchException;
import org.cdma.interfaces.IArray;
import org.cdma.math.ArrayMath;
import org.cdma.math.IArrayMath;

public class SqlArrayMath extends ArrayMath {

	public SqlArrayMath(SqlArray array) {
		super(array, Factory.getFactory( array.getFactoryName() ));
	}

	@Override
	public IArrayMath toAdd(IArray array) throws ShapeNotMatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IArrayMath add(IArray array) throws ShapeNotMatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IArrayMath matMultiply(IArray array) throws ShapeNotMatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IArrayMath matInverse() throws ShapeNotMatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IArrayMath sumForDimension(int dimension, boolean isVariance)
			throws ShapeNotMatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IArrayMath enclosedSumForDimension(int dimension, boolean isVariance)
			throws ShapeNotMatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getNorm() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IArrayMath normalise() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDeterminant() throws ShapeNotMatchException {
		// TODO Auto-generated method stub
		return 0;
	}

}
