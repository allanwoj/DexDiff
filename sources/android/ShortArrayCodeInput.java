package android;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package com.android.dx.io.instructions;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of {@code CodeInput} that reads from a {@code short[]}.
 */
public final class ShortArrayCodeInput extends BaseCodeCursor
        implements CodeInput {
    /** source array to read from */
    private final byte[] array;
    int count = 0;

    /**
     * Constructs an instance.
     */
    public ShortArrayCodeInput(byte[] array) {
        if (array == null) {
            throw new NullPointerException("array == null");
        }

        this.array = array;
    }

    /** @inheritDoc */
    public boolean hasMore() {
        return count < array.length;
    }

    /** @inheritDoc */
    public Collection<Byte> read() throws EOFException {
    	ArrayList<Byte> l = new ArrayList<Byte>();
    	l.add(array[count++]);
		l.add(array[count++]);
		return l;
    }

    /** @inheritDoc */
    public Collection<Byte> readInt() throws EOFException {
    	ArrayList<Byte> l = new ArrayList<Byte>();
    	l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		return l;
    }

    /** @inheritDoc */
    public Collection<Byte> readLong() throws EOFException {
    	ArrayList<Byte> l = new ArrayList<Byte>();
    	l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		l.add(array[count++]);
		return l;
    }

	@Override
	public int read16Bit() throws EOFException {
		int result = array[count++];
		result |= (array[count++] << 8);
		return result;
	}
}

