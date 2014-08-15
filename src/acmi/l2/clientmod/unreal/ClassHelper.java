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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class ClassHelper {
    private static final Logger log = Logger.getLogger(ClassHelper.class.getName());

    private final File systemFolder;

    private final Map<String, UnrealPackageFile> classPackages = new HashMap<>();
    private final Map<String, List<Field>> structCache = new HashMap<>();
    private final Map<String, UClass> classCache = new HashMap<>();

    private final PropertiesUtil propertiesUtil;

    public ClassHelper(String l2SystemFolder) {
        this.systemFolder = new File(l2SystemFolder);
        this.propertiesUtil = new PropertiesUtil(this);
    }

    private UnrealPackageFile getClassPackage(String name) throws IOException {
        if (!classPackages.containsKey(name))
            try (UnrealPackageFile up = new UnrealPackageFile(new File(systemFolder, name + ".u"), true)) {
                classPackages.put(name, up);
            }

        return classPackages.get(name);
    }

    public PropertiesUtil getPropertiesUtil() {
        return propertiesUtil;
    }

    private Struct loadStruct(String className) throws IOException {
        String[] path = className.split("\\.", 2);
        UnrealPackageFile up = getClassPackage(path[0]);
        UnrealPackageFile.ExportEntry entry = up.getExportTable().stream()
                .filter(e -> e.getObjectFullName().equalsIgnoreCase(className))
                .findAny()
                .orElseThrow(() -> new IOException(className + " not found"));
        ByteBuffer buffer = ByteBuffer.wrap(entry.getObjectRawDataExternally()).order(ByteOrder.LITTLE_ENDIAN);
        Struct struct;
        switch (entry.getObjectClass() != null ? entry.getObjectClass().getObjectFullName() : "null") {
            case "Core.Function":
                struct = new Function(buffer, entry, propertiesUtil);
                break;
            case "Core.Struct":
                struct = new Struct(buffer, entry, propertiesUtil);
                break;
            default:
                struct = new UClass(buffer, entry, propertiesUtil);
                break;
        }
        return struct;
    }

    public Optional<List<Field>> getStruct(String structName) {
        if (!structCache.containsKey(structName)) {
            try {
                load(structName);
            } catch (IOException e) {
                //log.log(Level.WARNING, "", e);
            }
        }

        return Optional.ofNullable(structCache.get(structName));
    }

    public Optional<UClass> getUClass(String className) {
        if (!classCache.containsKey(className)) {
            try {
                load(className);
            } catch (IOException e) {
                //log.log(Level.WARNING, "", e);
            }
        }

        return Optional.ofNullable(classCache.get(className));
    }

    public List<L2Property> getDefaults(String className) {
        List<L2Property> properties = new ArrayList<>();

        UClass uClass = getUClass(className).orElse(null);
        while (uClass != null) {
            properties.addAll(uClass.getProperties());

            if (uClass.getEntry().getObjectSuperClass() == null)
                uClass = null;
            else
                uClass = getUClass(uClass.getEntry().getObjectSuperClass().getObjectFullName()).orElse(null);
        }

        return properties;
    }

    public Field loadField(UnrealPackageFile.ExportEntry entry) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(entry.getObjectRawDataExternally()).order(ByteOrder.LITTLE_ENDIAN);

            String fieldClassName = getClass().getPackage().getName() + "." + entry.getObjectClass().getObjectName().getName();
            Class<? extends Field> fieldClass = (Class<? extends Field>) Class.forName(fieldClassName);

            return fieldClass.getConstructor(ByteBuffer.class, UnrealPackageFile.ExportEntry.class, PropertiesUtil.class)
                    .newInstance(buffer, entry, propertiesUtil);
        } catch (ReflectiveOperationException roe) {
            log.log(Level.SEVERE, "Couldn't load field " + entry, roe);
            throw new RuntimeException(roe);
        }
    }

    private void load(String structName) throws IOException {
        List<Struct> list = new ArrayList<>();

        Struct tmp = loadStruct(structName);
        while (tmp != null) {
            list.add(tmp);

            UnrealPackageFile.Entry superStruct = tmp.getEntry().getObjectSuperClass();
            tmp = superStruct != null ? loadStruct(superStruct.getObjectFullName()) : null;
        }

        Collections.reverse(list);

        List<Field> fields = new ArrayList<>();
        for (Struct struct : list) {
            String name = struct.getEntry().getObjectFullName();

            UnrealPackageFile.ExportEntry childEntry = (UnrealPackageFile.ExportEntry) struct.getChild();
            while (childEntry != null) {
                Field field = loadField(childEntry);

                fields.add(field);

                childEntry = field.getNext();
            }

            if (!structCache.containsKey(name))
                structCache.put(name, Collections.unmodifiableList(new ArrayList<>(fields)));

            if (struct instanceof UClass && !classCache.containsKey(name)) {
                UClass uClass = (UClass) struct;
                uClass.readProperties();
                classCache.put(name, uClass);
            }
        }
    }
}
