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
package acmi.l2.clientmod.unreal.objectfactory;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataInputStream;
import acmi.l2.clientmod.io.UnrealPackageFile;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.Core.Object;
import acmi.l2.clientmod.unreal.classloader.UnrealClassLoader;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class DefalutObjectFactory implements ObjectFactory {
    private final UnrealClassLoader classLoader;

    public DefalutObjectFactory(UnrealClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public UnrealClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Object readObject(UnrealPackageReadOnly.ExportEntry entry) throws IOException {
        if (entry.getObjectClass() == null)
            return classLoader.getStructQuetly(entry.getObjectFullName())
                    .orElseThrow(() -> new IllegalArgumentException("Class can only be loaded from classpath, use " + UnrealClassLoader.class.getSimpleName()));

        java.lang.Class<? extends Object> clazz = getClass(entry.getObjectClass().getObjectFullName());
        try {
            Constructor<? extends Object> constructor = clazz.getConstructor(DataInput.class, UnrealPackageFile.ExportEntry.class, PropertiesUtil.class);
            ByteArrayInputStream bais = new ByteArrayInputStream(entry.getObjectRawDataExternally());
            DataInputStream dis = new DataInputStream(bais, entry.getOffset(), entry.getUnrealPackage().getCharset());
            return constructor.newInstance(dis, entry, classLoader.getPropertiesUtil());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private java.lang.Class<? extends Object> getClass(String clazz) {
        if (clazz.equals("Core.Object"))
            return AsIsObject.class;

        String name = getClass().getPackage().getName() + "." + clazz;
        try {
            return java.lang.Class.forName(name).asSubclass(Object.class);
        } catch (ClassNotFoundException e) {
            //System.err.println(name + " not found");
        }

        String parent = classLoader.getSuperClass(clazz);
        if (parent == null)
            parent = "Core.Object";

        return getClass(parent);
    }
}
