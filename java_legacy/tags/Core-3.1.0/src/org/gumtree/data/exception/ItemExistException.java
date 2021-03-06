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
package org.gumtree.data.exception;

/**
 * @author nxi
 * 
 */
public class ItemExistException extends WriterException {

    /**
     * 
     */
    private static final long serialVersionUID = 3550694606967945269L;

    /**
     * 
     */
    public ItemExistException() {
    }

    /**
     * @param arg0 String value
     */
    public ItemExistException(final String arg0) {
        super(arg0);
    }

    /**
     * @param arg0 Throwable object
     */
    public ItemExistException(final Throwable arg0) {
        super(arg0);
    }

    /**
     * @param arg0 String value
     * @param arg1 Throwable object
     */
    public ItemExistException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

}
