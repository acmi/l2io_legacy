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
import acmi.l2.clientmod.io.DataInputStream;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.core.Class;
import acmi.l2.clientmod.unreal.core.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnrealClassLoaderImpl implements UnrealClassLoader {
    private final PackageLoader packageLoader;
    private final PropertiesUtil propertiesUtil;

    private final Map<String, Struct> structCache = new HashMap<>();
    private final Map<String, List<Field>> structFieldsCache = new HashMap<>();

    private final Map<Integer, Function> nativeFunctions = new HashMap<>();

    UnrealClassLoaderImpl(PackageLoader packageLoader) {
        this.packageLoader = packageLoader;
        this.propertiesUtil = new PropertiesUtilImpl(this);
    }

    @Override
    public PropertiesUtil getPropertiesUtil() {
        return propertiesUtil;
    }

    private UnrealPackageReadOnly.ExportEntry getExportEntry(String name) throws UnrealException {
        String[] path = name.split("\\.", 2);
        return packageLoader.apply(path[0])
                .getExportTable()
                .stream()
                .filter(e -> e.getObjectFullName().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new UnrealException(String.format("Entry %s not found.", name)));
    }

    @Override
    public Struct getStruct(String structName) throws UnrealException {
        if (!structCache.containsKey(structName)) {
            Struct struct = null;

            if (struct == null)
                throw new UnrealException("Not implemented");

            structCache.put(structName, struct);
        }
        return structCache.get(structName);
    }

    @Override
    public List<Field> getStructFields(String structName) throws UnrealException {
        if (!structFieldsCache.containsKey(structName)) {
            List<Field> structFields = null;

            if (structFields == null)
                throw new UnrealException("Not implemented");

            structFieldsCache.put(structName, structFields);
        }
        return structFieldsCache.get(structName);
    }

    private Struct loadStruct(String name) throws IOException {
        UnrealPackageReadOnly.ExportEntry entry = getExportEntry(name);
        DataInput buffer = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), entry.getOffset(), entry.getUnrealPackage().getCharset());
        Struct struct;
        switch (entry.getObjectClass() != null ? entry.getObjectClass().getObjectFullName() : "null") {
            case "Core.Function":
                Function function = new Function(buffer, entry, propertiesUtil);
                if (Function.Flag.getFlags(function.functionFlags).contains(Function.Flag.NATIVE))
                    nativeFunctions.put(function.nativeIndex, function);
                struct = function;
                break;
            case "Core.Struct":
                struct = new Struct(buffer, entry, propertiesUtil);
                break;
            case "Core.State":
                struct = new State(buffer, entry, propertiesUtil);
                break;
            default:
                struct = new Class(buffer, entry, propertiesUtil);
                break;
        }
        return struct;
    }

    @Override
    public Function getNativeFunction(int index) throws UnrealException {
        return nativeFunctions.get(index);
    }
}
