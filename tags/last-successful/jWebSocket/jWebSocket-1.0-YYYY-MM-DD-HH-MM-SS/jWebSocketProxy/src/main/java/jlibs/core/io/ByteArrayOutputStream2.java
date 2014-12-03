/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.io;

import jlibs.core.lang.ByteSequence;

import java.io.ByteArrayOutputStream;

/**
 * This is an extension of {@link java.io.ByteArrayOutputStream}.
 * <p/>
 * You can get access to the internal char buffer using
 * {@link #toByteSequence()}
 *
 * @author Santhosh Kumar T
 */
public class ByteArrayOutputStream2 extends ByteArrayOutputStream{
    public ByteArrayOutputStream2(){}

    public ByteArrayOutputStream2(int size){
        super(size);
    }

    /**
     * Returns the input data as {@link ByteSequence}.<br>
     * Note that the internal buffer is not copied.
     */
    public ByteSequence toByteSequence(){
        return new ByteSequence(buf, 0, size());
    }
}
