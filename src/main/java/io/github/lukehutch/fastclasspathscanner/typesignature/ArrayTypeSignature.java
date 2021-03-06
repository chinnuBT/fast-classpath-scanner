/*
 * This file is part of FastClasspathScanner.
 *
 * Author: Luke Hutchison
 *
 * Hosted at: https://github.com/lukehutch/fast-classpath-scanner
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Luke Hutchison
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.lukehutch.fastclasspathscanner.typesignature;

import java.lang.reflect.Array;
import java.util.Set;

import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import io.github.lukehutch.fastclasspathscanner.typesignature.TypeUtils.ParseException;
import io.github.lukehutch.fastclasspathscanner.typesignature.TypeUtils.ParseState;

/** An array type. */
public class ArrayTypeSignature extends ReferenceTypeSignature {
    /** The array element type signature. */
    final TypeSignature elementTypeSignature;

    /** The number of array dimensions. */
    final int numArrayDims;

    public ArrayTypeSignature(final TypeSignature elementTypeSignature, final int numArrayDims) {
        this.elementTypeSignature = elementTypeSignature;
        this.numArrayDims = numArrayDims;
    }

    /** Get the type signature of the array elements. */
    public TypeSignature getElementTypeSignature() {
        return elementTypeSignature;
    }

    /** Get the number of dimensions of the array. */
    public int getNumArrayDims() {
        return numArrayDims;
    }

    @Override
    public void getAllReferencedClassNames(final Set<String> classNameListOut) {
        elementTypeSignature.getAllReferencedClassNames(classNameListOut);
    }

    private static Class<?> arrayify(final Class<?> cls, final int arrayDims) {
        if (arrayDims == 0) {
            return cls;
        } else {
            final int[] zeroes = (int[]) Array.newInstance(int.class, arrayDims);
            return Array.newInstance(cls, zeroes).getClass();
        }
    }

    @Override
    public Class<?> instantiate(final ScanResult scanResult) {
        final Class<?> elementClassRef = elementTypeSignature.instantiate(scanResult);
        return arrayify(elementClassRef, numArrayDims);
    }

    @Override
    public int hashCode() {
        return elementTypeSignature.hashCode() + numArrayDims * 15;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ArrayTypeSignature)) {
            return false;
        }
        final ArrayTypeSignature o = (ArrayTypeSignature) obj;
        return o.elementTypeSignature.equals(this.elementTypeSignature) && o.numArrayDims == this.numArrayDims;
    }

    @Override
    public boolean equalsIgnoringTypeParams(final TypeSignature other) {
        if (!(other instanceof ArrayTypeSignature)) {
            return false;
        }
        final ArrayTypeSignature o = (ArrayTypeSignature) other;
        return o.elementTypeSignature.equalsIgnoringTypeParams(this.elementTypeSignature)
                && o.numArrayDims == this.numArrayDims;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(elementTypeSignature.toString());
        for (int i = 0; i < numArrayDims; i++) {
            buf.append("[]");
        }
        return buf.toString();
    }

    static ArrayTypeSignature parse(final ParseState parseState) throws ParseException {
        int numArrayDims = 0;
        while (parseState.peek() == '[') {
            numArrayDims++;
            parseState.next();
        }
        if (numArrayDims > 0) {
            final TypeSignature elementTypeSignature = TypeSignature.parse(parseState);
            if (elementTypeSignature == null) {
                throw new ParseException();
            }
            return new ArrayTypeSignature(elementTypeSignature, numArrayDims);
        } else {
            return null;
        }
    }
}