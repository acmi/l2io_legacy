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
import acmi.l2.clientmod.io.UnrealPackageFile;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.core.Class;
import acmi.l2.clientmod.unreal.core.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class OldClassLoaderImpl implements UnrealClassLoader {
    private static final Logger log = Logger.getLogger(OldClassLoaderImpl.class.getName());

    private final File systemFolder;

    private final Map<String, UnrealPackageReadOnly> classPackages = new HashMap<>();
    private final Map<String, List<Field>> structCache = new HashMap<>();
    private final Map<String, Class> classCache = new HashMap<>();
    private final Map<String, Field> fieldCache = new HashMap<>();

    private final Map<Integer, Function> nativeFunctions = new HashMap<>();

    private final PropertiesUtil propertiesUtil;

    private final Charset charset;

    public OldClassLoaderImpl(String l2SystemFolder) {
        this.systemFolder = new File(l2SystemFolder);
        this.propertiesUtil = new PropertiesUtilImpl(this);
        this.charset = Charset.forName(System.getProperty(String.format("%s.charset", UnrealClassLoader.class.getName()), "EUC-KR"));
    }

    private UnrealPackageReadOnly getClassPackage(String name) throws IOException {
        if (!classPackages.containsKey(name))
            try (UnrealPackageFile up = new UnrealPackageFile(new File(systemFolder, name + ".u"), true, charset)) {
                classPackages.put(name, up);

//                for (UnrealPackageReadOnly.ExportEntry entry : up.getExportTable()){
//                    if (entry.getObjectClass() != null && entry.getObjectClass().getObjectFullName().equals("Core.Function"))
//                        loadStruct(entry.getObjectFullName());
//                }
            }

        return classPackages.get(name);
    }

    public PropertiesUtil getPropertiesUtil() {
        return propertiesUtil;
    }

    private UnrealPackageReadOnly.ExportEntry loadEntry(String name) throws IOException {
        String[] path = name.split("\\.", 2);
        UnrealPackageReadOnly up = getClassPackage(path[0]);
        return up.getExportTable().stream()
                .filter(e -> e.getObjectFullName().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new IOException(name + " not found"));
    }

    private Struct loadStruct(String className) throws IOException {
        UnrealPackageReadOnly.ExportEntry entry = loadEntry(className);
        DataInput buffer = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), entry.getOffset(), charset);
        Struct struct;
        switch (entry.getObjectClass() != null ? entry.getObjectClass().getObjectFullName() : "null") {
            case "Core.Function":
                struct = new Function(buffer, entry, propertiesUtil);
                if (Function.Flag.getFlags(((Function) struct).functionFlags).contains(Function.Flag.NATIVE))
                    nativeFunctions.put(((Function) struct).nativeIndex, (Function) struct);
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

    public List<Field> getStructFields(String structName) {
        if (!structCache.containsKey(structName)) {
            try {
                load(structName);
            } catch (IOException e) {
                log.log(Level.WARNING, "", e);
            }
        }

        return structCache.get(structName);
    }

    @Override
    public Struct getStruct(String structName) throws UnrealException {
        try {
            return loadStruct(structName);
        } catch (IOException e) {
            throw new UnrealException(e);
        }
    }

    public Optional<Class> getUClass(String className) {
        if (!classCache.containsKey(className)) {
            try {
                load(className);
            } catch (IOException e) {
                log.log(Level.WARNING, "", e);
            }
        }

        return Optional.ofNullable(classCache.get(className));
    }

    public List<L2Property> getDefaults(String className) {
        List<L2Property> properties = new ArrayList<>();

        Class uClass = getUClass(className).orElse(null);
        while (uClass != null) {
            properties.addAll(uClass.getProperties());

            if (uClass.getEntry().getObjectSuperClass() == null)
                uClass = null;
            else
                uClass = getUClass(uClass.getEntry().getObjectSuperClass().getObjectFullName()).orElse(null);
        }

        return properties;
    }

    public Field loadField(UnrealPackageReadOnly.ExportEntry entry) throws IOException {
        try {
            DataInput buffer = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), charset);

            String fieldClassName = Field.class.getPackage().getName() + "." + entry.getObjectClass().getObjectName().getName();
            java.lang.Class<? extends Field> fieldClass = java.lang.Class.forName(fieldClassName).asSubclass(Field.class);

            return fieldClass.getConstructor(DataInput.class, UnrealPackageReadOnly.ExportEntry.class, PropertiesUtil.class)
                    .newInstance(buffer, entry, propertiesUtil);
        } catch (ReflectiveOperationException roe) {
            log.log(Level.SEVERE, "Couldn't load field " + entry, roe);
            throw new RuntimeException(roe);
        }
    }

    public Field getField(UnrealPackageReadOnly.ExportEntry entry) {
        String name = entry.getObjectFullName();
        if (!fieldCache.containsKey(name))
            try {
                fieldCache.put(name, loadField(entry));
            } catch (IOException e) {
            }
        return fieldCache.get(name);
    }

    private void load(String structName) throws IOException {
        List<Struct> list = new ArrayList<>();

        Struct tmp = loadStruct(structName);
        while (tmp != null) {
            list.add(tmp);

            UnrealPackageReadOnly.Entry superStruct = tmp.getEntry().getObjectSuperClass();
            tmp = superStruct != null ? loadStruct(superStruct.getObjectFullName()) : null;
        }

        Collections.reverse(list);

        System.out.println(list);

        List<Field> fields = new ArrayList<>();
        for (Struct struct : list) {
            String name = struct.getEntry().getObjectFullName();

            UnrealPackageReadOnly.ExportEntry childEntry = (UnrealPackageReadOnly.ExportEntry) struct.getChild();
            while (childEntry != null) {
                Field field = getField(childEntry);

                fields.add(field);

                childEntry = field.getNext();
            }

            if (!structCache.containsKey(name))
                structCache.put(name, Collections.unmodifiableList(new ArrayList<>(fields)));

            if (struct instanceof Class && !classCache.containsKey(name)) {
                Class uClass = (Class) struct;
                uClass.readProperties();
                classCache.put(name, uClass);
            }
        }
    }

    public boolean isSubclass(String parent, String child) {
        if (parent.equalsIgnoreCase(child))
            return true;

        child = getSuperClass(child);

        return child != null && isSubclass(parent, child);
    }

    public String getSuperClass(String clazz) {
        Optional<acmi.l2.clientmod.unreal.core.Class> childClass = getUClass(clazz);
        if (childClass.isPresent()) {
            return childClass.get().getEntry().getObjectSuperClass().getObjectFullName();
        }
        return null;
    }

    public Function getNativeFunction(int nativeIndex) {
        return nativeFunctions.get(nativeIndex);
    }
}
