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
package org.cdma.plugin.soleil.edf.utils;

import org.cdma.exception.NotImplementedException;
import org.cdma.exception.ShapeNotMatchException;
import org.cdma.interfaces.IArray;
import org.cdma.math.ArrayMath;
import org.cdma.math.IArrayMath;
import org.cdma.plugin.soleil.edf.EdfFactory;

public class EdfArrayMath extends ArrayMath {

    public EdfArrayMath(IArray array) {
        super(array, EdfFactory.getInstance());
    }

    @Override
    public IArrayMath toAdd(IArray array) throws ShapeNotMatchException {
        throw new NotImplementedException();
    }

    @Override
    public IArrayMath add(IArray array) throws ShapeNotMatchException {
        throw new NotImplementedException();
    }

    @Override
    public IArrayMath matMultiply(IArray array) throws ShapeNotMatchException {
        throw new NotImplementedException();
    }

    @Override
    public IArrayMath matInverse() throws ShapeNotMatchException {
        throw new NotImplementedException();
    }

    @Override
    public IArrayMath sumForDimension(int dimension, boolean isVariance)
            throws ShapeNotMatchException {
        throw new NotImplementedException();
    }

    @Override
    public IArrayMath enclosedSumForDimension(int dimension, boolean isVariance)
            throws ShapeNotMatchException {
        throw new NotImplementedException();
    }

    @Override
    public double getNorm() {
        throw new NotImplementedException();
    }

    @Override
    public IArrayMath normalise() {
        throw new NotImplementedException();
    }

    @Override
    public double getDeterminant() throws ShapeNotMatchException {
        throw new NotImplementedException();
    }

}
