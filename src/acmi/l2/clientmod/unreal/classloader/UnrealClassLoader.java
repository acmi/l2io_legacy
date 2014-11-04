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

import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.core.Field;
import acmi.l2.clientmod.unreal.core.Function;
import acmi.l2.clientmod.unreal.core.Struct;

import java.util.List;
import java.util.Optional;

public interface UnrealClassLoader {
    static UnrealClassLoader getInstance(PackageLoader packageLoader) {
        return new UnrealClassLoaderImpl(packageLoader);
    }

    PropertiesUtil getPropertiesUtil();

    Struct getStruct(String structName) throws UnrealException;

    default Optional<Struct> getStructQuetly(String structName) {
        try {
            return Optional.of(getStruct(structName));
        } catch (UnrealException e) {
            return Optional.ofNullable(null);
        }
    }

    List<Field> getStructFields(String structName) throws UnrealException;

    default Optional<List<Field>> getStructFieldsQuetly(String structName) {
        try {
            return Optional.of(getStructFields(structName));
        } catch (UnrealException e) {
            return Optional.ofNullable(null);
        }
    }

    Field getField(String field) throws UnrealException;

    default Optional<Field> getFieldQuetly(String field) {
        try {
            return Optional.of(getField(field));
        } catch (UnrealException e) {
            return Optional.ofNullable(null);
        }
    }

    Function getNativeFunction(int index) throws UnrealException;

    default Optional<Function> getNativeFunctionQuetly(int index) {
        try {
            return Optional.of(getNativeFunction(index));
        } catch (UnrealException e) {
            return Optional.ofNullable(null);
        }
    }

    default boolean isSubclass(String parent, String child) {
        if (parent.equalsIgnoreCase(child))
            return true;

        child = getSuperClass(child);

        return child != null && isSubclass(parent, child);
    }

    default String getSuperClass(String clazz) {
        Optional<Struct> childClass = getStructQuetly(clazz);
        if (childClass.isPresent()) {
            return childClass.get().getEntry().getObjectSuperClass().getObjectFullName();
        }
        return null;
    }
}
