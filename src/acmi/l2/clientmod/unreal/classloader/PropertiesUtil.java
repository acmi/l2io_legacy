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
package acmi.l2.clientmod.unreal.classloader;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.UnrealException;

import java.util.List;

/**
 * Entry properties:
 * <p>
 * if (entry.getObjectClass() == null) {
 * UClass uClass = new UClass(buffer, entry, this);
 * uClass.readProperties();
 * return uClass.getProperties();
 * }
 * if (getFlags(entry.getObjectFlags()).contains(HasStack)) {
 * getCompactInt(buffer);
 * getCompactInt(buffer);
 * buffer.position(buffer.position() + 12);
 * getCompactInt(buffer);
 * }
 */
public interface PropertiesUtil {
    UnrealClassLoader getUnrealClassLoader();

    List<L2Property> readProperties(DataInput dataInput, String objClass, UnrealPackageReadOnly up) throws UnrealException;

    List<L2Property> readStructBin(DataInput objBuffer, String structName, UnrealPackageReadOnly up) throws UnrealException;

    void writeProperties(DataOutput buffer, List<L2Property> list, UnrealPackageReadOnly up) throws UnrealException;

    void writeStructBin(DataOutput objBuffer, List<L2Property> struct, String structName, UnrealPackageReadOnly up) throws UnrealException;

    static L2Property getAt(List<L2Property> properties, String name) {
        return properties.stream()
                .filter(p -> p.getName().equalsIgnoreCase((name)))
                .findFirst()
                .orElse(null);
    }
}
