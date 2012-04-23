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
import java.util.Collection;

/**
 * Input stream of code units, for reading in Dalvik bytecode.
 */
public interface CodeInput extends CodeCursor {
    /**
     * Returns whether there are any more code units to read. This
     * is analogous to {@code hasNext()} on an interator.
     */
    public boolean hasMore();

    /**
     * Reads a code unit.
     */
    public Collection<Byte> read() throws EOFException;
    
    /**
     * Reads a code unit.
     */
    public long read16Bit() throws EOFException;

    /**
     * Reads two code units, treating them as a little-endian {@code int}.
     */
    public Collection<Byte> readInt() throws EOFException;

    /**
     * Reads four code units, treating them as a little-endian {@code long}.
     */
    public Collection<Byte> readLong() throws EOFException;
}