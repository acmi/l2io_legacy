/*
 * Copyright (c) 2014 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class Field extends UObject {
    public final int superField;
    public final int next;

    public Field(ByteBuffer buffer, UnrealPackageFile.ExportEntry entry, PropertiesUtil propertiesUtil) {
        super(buffer, entry, propertiesUtil);

        superField = getCompactInt(buffer);
        next = getCompactInt(buffer);
    }

    public UnrealPackageFile.ExportEntry getSuperField() {
        return (UnrealPackageFile.ExportEntry) getEntry().getUnrealPackage().objectReference(superField);
    }

    public UnrealPackageFile.ExportEntry getNext() {
        return (UnrealPackageFile.ExportEntry) getEntry().getUnrealPackage().objectReference(next);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "superField=" + getSuperField() +
                ", next=" + getNext() +
                '}';
    }
}
