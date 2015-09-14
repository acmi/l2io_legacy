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
package acmi.l2.clientmod.io;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static acmi.l2.clientmod.io.ByteUtil.*;

public class UnrealPackageFile implements UnrealPackageReadOnly, Closeable {
    public static final int UNREAL_PACKAGE_MAGIC = 0x9E2A83C1;

    public static final int VERSION_OFFSET = 4;
    public static final int LICENSEE_OFFSET = 6;
    public static final int PACKAGE_FLAGS_OFFSET = 8;
    public static final int NAME_COUNT_OFFSET = 12;
    public static final int NAME_OFFSET_OFFSET = 16;
    public static final int EXPORT_COUNT_OFFSET = 20;
    public static final int EXPORT_OFFSET_OFFSET = 24;
    public static final int IMPORT_COUNT_OFFSET = 28;
    public static final int IMPORT_OFFSET_OFFSET = 32;
    public static final int GUID_OFFSET = 36;
    public static final int GENERATIONS_OFFSET = 52;

    private RandomAccessFile file;
    private final String packageName;

    private int version;
    private int licensee;
    private int flags;

    private List<NameEntry> names;
    private List<ExportEntry> exports;
    private List<ImportEntry> imports;

    private UUID uuid;

    private List<Generation> generations;

    public UnrealPackageFile(String path, boolean readOnly, Charset charset) throws IOException {
        this(new RandomAccessFile(path, readOnly, charset));
    }

    public UnrealPackageFile(String path, boolean readOnly) throws IOException {
        this(new RandomAccessFile(path, readOnly));
    }

    public UnrealPackageFile(File file, boolean readOnly, Charset charset) throws IOException {
        this(new RandomAccessFile(file, readOnly, charset));
    }

    public UnrealPackageFile(File file, boolean readOnly) throws IOException {
        this(new RandomAccessFile(file, readOnly));
    }

    public UnrealPackageFile(RandomAccessFile file) throws IOException {
        this.file = Objects.requireNonNull(file);
        String fileName = new File(file.getPath()).getName();
        this.packageName = fileName.substring(0, fileName.indexOf('.'));

        file.setPosition(0);

        if (file.readInt() != UNREAL_PACKAGE_MAGIC)
            throw new IOException("Not a L2 package file.");

        version = file.readUnsignedShort();
        licensee = file.readUnsignedShort();
        flags = file.readInt();

        readNameTable();
        readImportTable();
        readExportTable();

        file.setPosition(GUID_OFFSET);
        byte[] uuidBytes = new byte[16];
        file.readFully(uuidBytes);
        uuid = uuidFromBytes(uuidBytes);

        file.setPosition(GENERATIONS_OFFSET);
        int count = file.readInt();
        List<Generation> tmp = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            tmp.add(new Generation(this, i, file.readInt(), file.readInt()));
        generations = Collections.unmodifiableList(tmp);
    }

    public RandomAccessFile getFile() {
        return file;
    }

    @Override
    public Charset getCharset() {
        return getFile().getCharset();
    }

    public String getPackageName() {
        return packageName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) throws IOException {
        file.setPosition(VERSION_OFFSET);
        file.writeShort(version);

        this.version = version;
    }

    public int getLicensee() {
        return licensee;
    }

    public void setLicensee(int licensee) throws IOException {
        file.setPosition(LICENSEE_OFFSET);
        file.writeShort(licensee);

        this.licensee = licensee;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) throws IOException {
        file.setPosition(PACKAGE_FLAGS_OFFSET);
        file.writeInt(flags);

        this.flags = flags;
    }

    public List<NameEntry> getNameTable() {
        return names;
    }

    private void readNameTable() throws IOException {
        file.setPosition(NAME_COUNT_OFFSET);
        int count = file.readInt();
        file.setPosition(getNameTableOffset());

        List<NameEntry> tmp = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            tmp.add(new NameEntry(this, i, file.readLine(), file.readInt()));

        names = Collections.unmodifiableList(tmp);
    }

    public List<ExportEntry> getExportTable() {
        return exports;
    }

    private void readExportTable() throws IOException {
        file.setPosition(EXPORT_COUNT_OFFSET);
        int count = file.readInt();
        file.setPosition(getExportTableOffset());

        List<ExportEntry> tmp = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            tmp.add(new ExportEntry(this, i,
                    file.readCompactInt(),
                    file.readCompactInt(),
                    file.readInt(),
                    file.readCompactInt(),
                    file.readInt(),
                    file.readCompactInt(),
                    file.readCompactInt()));

        exports = Collections.unmodifiableList(tmp);
    }

    public List<ImportEntry> getImportTable() {
        return imports;
    }

    private void readImportTable() throws IOException {
        file.setPosition(IMPORT_COUNT_OFFSET);
        int count = file.readInt();
        file.setPosition(getImportTableOffset());

        List<ImportEntry> tmp = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            tmp.add(new ImportEntry(this, i,
                    file.readCompactInt(),
                    file.readCompactInt(),
                    file.readInt(),
                    file.readCompactInt()));

        imports = Collections.unmodifiableList(tmp);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) throws IOException {
        file.setPosition(GUID_OFFSET);
        file.write(uuidToBytes(uuid));

        this.uuid = uuid;
    }

    public List<Generation> getGenerations() {
        return generations;
    }

    public String getFilePath() {
        return file.getPath();
    }

    public String toString() {
        return getPackageName();
    }

    public String nameReference(int index) {
        return getNameTable().get(index).getName();
    }

    public Entry objectReference(int ref) {
        if (ref > 0)
            return getExportTable().get(ref - 1);
        else if (ref < 0)
            return getImportTable().get(-ref - 1);
        else
            return null;
    }

    public Entry getAt(int index) {
        return objectReference(index);
    }

    public int nameReference(String name) {
        try {
            return getNameTable().parallelStream()
                    .filter(e -> e.getName().equalsIgnoreCase(name))
                    .findAny()
                    .get()
                    .getIndex();
        } catch (NoSuchElementException e) {
            return -1;
        }
    }

    @Deprecated
    public int objectReference(String name) {
        int ref;

        if ((ref = importReference(name)) != 0)
            return ref;

        if ((ref = exportReference(name)) != 0)
            return ref;

        return 0;
    }

    @Deprecated
    public int importReference(String name) {
        try {
            return getImportTable().stream()
                    .filter(entry -> entry.getObjectFullName().equalsIgnoreCase(name))
                    .findAny()
                    .get()
                    .getObjectReference();
        } catch (NoSuchElementException ignore) {
            return 0;
        }
    }

    @Deprecated
    public int exportReference(String name) {
        try {
            return getExportTable().stream()
                    .filter(entry -> entry.getObjectInnerFullName().equalsIgnoreCase(name))
                    .findAny()
                    .get()
                    .getObjectReference();
        } catch (NoSuchElementException ignore) {
            return 0;
        }
    }

    public void updateNameTable(Consumer<List<NameEntry>> transformation) throws IOException {
        List<NameEntry> nameTable = new ArrayList<>(getNameTable());

        transformation.accept(nameTable);

        int newNameTablePos = getDataEndOffset();
        file.setPosition(newNameTablePos);
        writeNameTable(nameTable);
        int newImportTablePos = file.getPosition();
        writeImportTable(getImportTable());
        int newExportTablePos = file.getPosition();
        writeExportTable(getExportTable());

        file.setLength(file.getPosition());

        file.setPosition(NAME_COUNT_OFFSET);
        file.writeInt(nameTable.size());
        file.setPosition(NAME_OFFSET_OFFSET);
        file.writeInt(newNameTablePos);

        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(newExportTablePos);

        file.setPosition(IMPORT_OFFSET_OFFSET);
        file.writeInt(newImportTablePos);

        readNameTable();
    }

    public void updateImportTable(Consumer<List<ImportEntry>> transformation) throws IOException {
        List<ImportEntry> importTable = new ArrayList<>(getImportTable());

        transformation.accept(importTable);

        file.setPosition(getImportTableOffset());
        writeImportTable(importTable);
        int newExportTablePos = file.getPosition();
        writeExportTable(getExportTable());
        file.setLength(file.getPosition());

        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(newExportTablePos);
        file.setPosition(IMPORT_COUNT_OFFSET);
        file.writeInt(importTable.size());

        readImportTable();
    }

    public void addNameEntries(Map<String, Integer> names) throws IOException {
        updateNameTable(nameTable -> names.forEach((k, v) -> {
            NameEntry entry = new NameEntry(null, 0, k, v);
            if (!nameTable.contains(entry))
                nameTable.add(entry);
        }));
    }

    public void updateNameEntry(int index, String newName, int newFlags) throws IOException {
        updateNameTable(nameTable -> {
            nameTable.remove(index);
            nameTable.add(index, new NameEntry(this, index, newName, newFlags));
        });
    }

    public void addImportEntries(Map<String, String> imports, boolean force) throws IOException {
        Map<String, Integer> namesToAdd = new HashMap<>();
        imports.forEach((k, v) -> {
            String[] namePath = k.split("\\.");
            String[] classPath = v.split("\\.");

            Arrays.stream(namePath)
                    .filter(s -> nameReference(s) == -1)
                    .forEach(s -> namesToAdd.put(s, PACKAGE_FLAGS));
            Arrays.stream(classPath)
                    .filter(s -> nameReference(s) == -1)
                    .forEach(s -> namesToAdd.put(s, PACKAGE_FLAGS));
        });
        addNameEntries(namesToAdd);

        updateImportTable(importTable -> {
            for (Map.Entry<String, String> entry : imports.entrySet()) {
                String[] namePath = entry.getKey().split("\\.");
                String[] classPath = entry.getValue().split("\\.");

                int pckg = 0;
                ImportEntry importEntry;
                for (int i = 0; i < namePath.length - 1; i++) {
                    importEntry = new ImportEntry(this, 0,
                            nameReference("Core"),
                            nameReference("Package"),
                            pckg,
                            nameReference(namePath[i]));
                    if ((pckg = importTable.indexOf(importEntry)) == -1) {
                        importTable.add(importEntry);
                        pckg = importTable.size() - 1;
                    }
                    pckg = -(pckg + 1);
                }

                importEntry = new ImportEntry(this, 0,
                        nameReference(classPath[0]),
                        nameReference(classPath[1]),
                        pckg,
                        nameReference(namePath[namePath.length - 1]));
                if (force || importTable.indexOf(importEntry) == -1)
                    importTable.add(importEntry);
            }
        });
    }

    public void addExportEntry(String objectName, String objectClass, String objectSuperClass, byte[] data, int flags) throws IOException {
        Map<String, String> classes = new HashMap<>();
        if (objectClass != null && objectReference(objectClass) == 0)
            classes.put(objectClass, "Core.Class");
        if (objectSuperClass != null && objectReference(objectSuperClass) == 0)
            classes.put(objectClass, "Core.Class");
        if (!classes.isEmpty())
            addImportEntries(classes, false);

        Map<String, Integer> namesToAdd = new HashMap<>();
        String[] namePath = objectName.split("\\.");
        Arrays.stream(namePath)
                .filter(s -> nameReference(s) == -1)
                .forEach(s -> namesToAdd.put(s, PACKAGE_FLAGS));
        addNameEntries(namesToAdd);

        file.setPosition(getDataEndOffset());
        List<ExportEntry> exportTable = new ArrayList<>(getExportTable());
        int pckgInd = importReference("Core.Package");
        byte[] pckgData = compactIntToByteArray(nameReference("None"));

        int pckg = 0;
        ExportEntry exportEntry;
        for (int i = 0; i < namePath.length - 1; i++) {
            exportEntry = new ExportEntry(this, 0,
                    pckgInd,
                    0,
                    pckg,
                    nameReference(namePath[i]),
                    PACKAGE_FLAGS,
                    pckgData.length, file.getPosition());
            if ((pckg = exportTable.indexOf(exportEntry)) == -1) {
                exportTable.add(exportEntry);
                pckg = exportTable.size() - 1;
                file.write(pckgData);
            }
            pckg++;
        }

        exportEntry = new ExportEntry(this,
                0,
                objectReference(objectClass),
                objectReference(objectSuperClass),
                pckg,
                nameReference(namePath[namePath.length - 1]),
                flags,
                data.length, file.getPosition());
        if (exportTable.indexOf(exportEntry) == -1) {
            exportTable.add(exportEntry);
            file.write(data);
        }

        int nameTablePosition = file.getPosition();
        writeNameTable(getNameTable());
        int importTablePosition = file.getPosition();
        writeImportTable(getImportTable());
        int exportTablePosition = file.getPosition();
        writeExportTable(exportTable);

        file.setPosition(NAME_OFFSET_OFFSET);
        file.writeInt(nameTablePosition);
        file.setPosition(EXPORT_COUNT_OFFSET);
        file.writeInt(exportTable.size());
        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(exportTablePosition);
        file.setPosition(IMPORT_OFFSET_OFFSET);
        file.writeInt(importTablePosition);

        readNameTable();
        readImportTable();
        readExportTable();
    }

//    public void renameExport(String nameSrc, String nameDst) throws IOException {
//        if (objectReference(nameSrc) <= 0){
//            log.log(Level.WARNING, "ExportEntry not found: "+nameSrc);
//            return;
//        }
//        if (objectReference(nameDst) != 0){
//            log.log(Level.WARNING, "Entry already exist: "+nameDst);
//            return;
//        }
//
//        List<String> namesToAdd = new ArrayList<>();
//        String[] namePath = nameDst.split("\\.");
//        if (namePath.length > 1 && objectReference("Core.Package") == 0)
//            addImportEntries(Collections.singletonMap("Core.Package", "Core.Class"));
//        for (String s : namePath)
//            if (nameReference(s) == -1)
//                namesToAdd.add(s);
//        String[] newNames = namesToAdd.toArray(new String[namesToAdd.size()]);
//        addNameEntries(newNames);
//
//        List<NameEntry> nameTable = getNameTable();
//        List<ImportEntry> importTable = getImportTable();
//
//        file.setPosition(getDataEndOffset());
//        List<ExportEntry> exportTable = new ArrayList<>(getExportTable());
//        int pckgInd = objectReference("Core.Package");
//        byte[] pckgData = L2RandomAccessFile.compactIntToByteArray(getNameTable().indexOf(new NameEntry("None")));
//        int pckg = 0;
//        ExportEntry exportEntry;
//        for (int i = 0; i < namePath.length - 1; i++) {
//            exportEntry = new ExportEntry(
//                    pckgInd,
//                    0,
//                    pckg,
//                    nameReference(namePath[i]),
//                    RF_Public | RF_LoadForServer | RF_LoadForEdit,
//                    pckgData.length, file.getPosition());
//            if ((pckg = exportTable.indexOf(exportEntry)) == -1) {
//                exportTable.add(exportEntry);
//                pckg = exportTable.size() - 1;
//                file.write(pckgData);
//                log.log(Level.INFO, "ExportEntry added: "+exportEntry);
//            }
//            pckg++;
//        }
//        for (ExportEntry entry : exportTable) {
//            if (entry.getAbsoluteName().equalsIgnoreCase(nameSrc)) {
//                log.log(Level.INFO, "ExportEntry renamed: "+entry);
//                entry.objectPackage = pckg;
//                entry.objectName = nameReference(namePath[namePath.length - 1]);
//                //log.log(Level.INFO, "New ExportEntry name: "+entry);
//                break;
//            }
//        }
//
//        int nameTablePosition = file.getPosition();
//        writeNameTable(nameTable);
//        int importTablePosition = file.getPosition();
//        writeImportTable(importTable);
//        int exportTablePosition = file.getPosition();
//        writeExportTable(exportTable);
//
//        setNameTableOffset(nameTablePosition);
//        setExportTableOffset(exportTablePosition);
//        setExportTableCount(exportTable.size());
//        setImportTableOffset(importTablePosition);
//
//        names = null;
//        imports = null;
//        exports = null;
//    }

//    public void removeExport(String className) throws IOException{
//        if (objectReference("Core.Package") == 0)
//            addImportEntries(Collections.singletonMap("Core.Package", "Core.Class"), false);
//
//        int pckgInd = objectReference("Core.Package");
//        byte[] pckgData = compactIntToByteArray(nameReference("None"));
//
//        List<ExportEntry> exportTable = getExportTable();
//
//        for (ExportEntry entry : exportTable){
//            if (entry.getObjectClass().getObjectFullName().equalsIgnoreCase(className)){
//                entry.objectClass = pckgInd;
//                entry.objectSuperClass = 0;
//                entry.objectFlags =  ObjectFlag.getFlags(ObjectFlag.Public, ObjectFlag.LoadForServer, ObjectFlag.LoadForEdit);
//                entry.setObjectRawData(pckgData, false);
//            }
//        }
//
//        file.setPosition(getExportTableOffset());
//        writeExportTable(exportTable);
//        exports = null;
//    }

    public void renameImport(int index, String importDst) throws IOException {
        addImportEntries(
                Collections.singletonMap(importDst, getImportTable().get(index).getFullClassName()),
                true
        );

        updateImportTable(importTable -> importTable.set(index, importTable.remove(importTable.size() - 1)));
    }

    public void changeImportClass(int index, String importDst) throws IOException {
        String[] clazz = importDst.split("\\.");
        if (clazz.length != 2)
            throw new IllegalArgumentException("Format: Package.Class");

        addNameEntries(Arrays.stream(clazz)
                .filter(s -> nameReference(s) == -1)
                .collect(Collectors.toMap(v -> v, v -> PACKAGE_FLAGS)));

        updateImportTable(importTable -> {
            ImportEntry entry = importTable.get(index);
            entry.classPackage = nameReference(clazz[0]);
            entry.className = nameReference(clazz[1]);
        });
    }

    private OutputStream fileOutputStream = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            file.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            file.write(b, off, len);
        }
    };

    private void writeNameTable(List<NameEntry> nameTable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput buffer = new DataOutputStream(baos, file.getCharset());
        for (NameEntry entry : nameTable) {
            buffer.writeLine(entry.getName());
            buffer.writeInt(entry.getFlags());
        }
        baos.writeTo(fileOutputStream);
    }

    private void writeImportTable(List<ImportEntry> importTable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput buffer = new DataOutputStream(baos, file.getCharset());
        for (ImportEntry entry : importTable) {
            buffer.writeCompactInt(entry.classPackage);
            buffer.writeCompactInt(entry.className);
            buffer.writeInt(entry.objectPackage);
            buffer.writeCompactInt(entry.objectName);
        }
        baos.writeTo(fileOutputStream);
    }

    private void writeExportTable(List<ExportEntry> exportTable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput buffer = new DataOutputStream(baos, file.getCharset());
        for (ExportEntry entry : exportTable) {
            buffer.writeCompactInt(entry.objectClass);
            buffer.writeCompactInt(entry.objectSuperClass);
            buffer.writeInt(entry.objectPackage);
            buffer.writeCompactInt(entry.objectName);
            buffer.writeInt(entry.objectFlags);
            buffer.writeCompactInt(entry.size);
            buffer.writeCompactInt(entry.offset);
        }
        baos.writeTo(fileOutputStream);
    }

    public int getNameTableOffset() throws IOException {
        file.setPosition(NAME_OFFSET_OFFSET);
        return file.readInt();
    }

    public int getExportTableOffset() throws IOException {
        file.setPosition(EXPORT_OFFSET_OFFSET);
        return file.readInt();
    }

    public int getImportTableOffset() throws IOException {
        file.setPosition(IMPORT_OFFSET_OFFSET);
        return file.readInt();
    }

    public int getDataStartOffset() {
        return getExportTable().stream()
                .filter(entry -> entry.getSize() > 0)
                .mapToInt(ExportEntry::getOffset)
                .min()
                .orElseThrow(() -> new IllegalStateException("Data block is empty"));
    }

    public int getDataEndOffset() {
        return getExportTable().stream()
                .filter(entry -> entry.getSize() > 0)
                .mapToInt(entry -> entry.getOffset() + entry.getSize())
                .max()
                .orElseThrow(() -> new IllegalStateException("Data block is empty"));
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private static abstract class PackageEntry {
        private final UnrealPackageFile unrealPackage;
        private final int index;

        protected PackageEntry(UnrealPackageFile unrealPackage, int index) {
            this.unrealPackage = unrealPackage;
            this.index = index;
        }

        public UnrealPackageFile getUnrealPackage() {
            return unrealPackage;
        }

        public int getIndex() {
            return index;
        }

        public abstract PackageEntry previous();

        public abstract PackageEntry next();
    }

    public static final class Generation extends PackageEntry {
        private final int exportCount;
        private final int importCount;

        public Generation(UnrealPackageFile unrealPackage, int index, int exportCount, int importCount) {
            super(unrealPackage, index);
            this.exportCount = exportCount;
            this.importCount = importCount;
        }

        public int getExportCount() {
            return exportCount;
        }

        public int getNameCount() {
            return importCount;
        }

        @Override
        public String toString() {
            return "Generation[" +
                    "exportCount=" + exportCount +
                    ", importCount=" + importCount +
                    ']';
        }

        @Override
        public PackageEntry previous() {
            return getUnrealPackage().getGenerations().get(getIndex() - 1);
        }

        @Override
        public PackageEntry next() {
            return getUnrealPackage().getGenerations().get(getIndex() + 1);
        }
    }

    public static final class NameEntry extends PackageEntry implements UnrealPackageReadOnly.NameEntry {
        private final String name;
        private int flags;

        private NameEntry(UnrealPackageFile unrealPackage, int index, String name, int flags) {
            super(unrealPackage, index);
            this.name = Objects.requireNonNull(name);
            this.flags = flags;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            NameEntry nameEntry = (NameEntry) o;

            return name.equalsIgnoreCase(nameEntry.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public NameEntry previous() {
            return getUnrealPackage().getNameTable().get(getIndex() - 1);
        }

        @Override
        public NameEntry next() {
            return getUnrealPackage().getNameTable().get(getIndex() + 1);
        }
    }

    public static abstract class Entry extends PackageEntry implements UnrealPackageReadOnly.Entry {
        protected final int objectPackage;
        protected final int objectName;

        private Reference<String> innerName = new SoftReference<>(null);

        protected Entry(UnrealPackageFile unrealPackage, int index, int objectPackage, int objectName) {
            super(unrealPackage, index);
            this.objectPackage = objectPackage;
            this.objectName = objectName;
        }

        @Override
        public Entry getObjectPackage() {
            return getUnrealPackage().objectReference(objectPackage);
        }

        @Override
        public NameEntry getObjectName() {
            return getUnrealPackage().getNameTable().get(objectName);
        }

        @Override
        public String getObjectInnerFullName() {
            String str = innerName.get();
            if (str == null) {
                Entry pckg = getObjectPackage();
                str = pckg == null ? getObjectName().getName() : pckg.getObjectInnerFullName() + '.' + getObjectName().getName();
                innerName = new SoftReference<>(str);
            }
            return str;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry entry = (Entry) o;

            return (objectName == entry.objectName) && (objectPackage == entry.objectPackage);
        }

        @Override
        public int hashCode() {
            return (objectPackage << 16) + objectName;
        }

        @Override
        public String toString() {
            return getObjectFullName();
        }
    }

    public static final class ExportEntry extends Entry implements UnrealPackageReadOnly.ExportEntry {
        private int objectClass;
        private int objectSuperClass;
        private int objectFlags;
        private int size;
        private int offset;

        private Reference<String> fullName = new SoftReference<>(null);

        private ExportEntry(UnrealPackageFile unrealPackage, int index, int objectClass, int objectSuperClass, int objectPackage, int objectName, int objectFlags, int size, int offset) {
            super(unrealPackage, index, objectPackage, objectName);
            this.objectClass = objectClass;
            this.objectSuperClass = objectSuperClass;
            this.objectFlags = objectFlags;
            this.size = size;
            this.offset = offset;
        }

        @Override
        public int getObjectReference() {
            return getIndex() + 1;
        }

        @Override
        public Entry getObjectClass() {
            return getUnrealPackage().objectReference(objectClass);
        }

        @Override
        public Entry getObjectSuperClass() {
            return getUnrealPackage().objectReference(objectSuperClass);
        }

        @Override
        public int getObjectFlags() {
            return objectFlags;
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        public byte[] getObjectRawData() throws IOException {
            if (getSize() == 0)
                return new byte[0];

            byte[] raw = new byte[size];
            getUnrealPackage().file.setPosition(offset);
            getUnrealPackage().file.readFully(raw);
            return raw;
        }

        @Override
        public byte[] getObjectRawDataExternally() throws IOException {
            if (getSize() == 0)
                return new byte[0];

            try (RandomAccessFile raf = new RandomAccessFile(getUnrealPackage().file.getPath(), true, getUnrealPackage().file.getCharset())) {
                byte[] data = new byte[getSize()];
                raf.setPosition(getOffset());
                raf.readFully(data);
                return data;
            }
        }

        public void setObjectRawData(byte[] data) throws IOException {
            setObjectRawData(data, true);
        }

        public void setObjectRawData(byte[] data, boolean writeExportTable) throws IOException {
            if (data.length <= getSize()) {
                getUnrealPackage().file.setPosition(getOffset());
                getUnrealPackage().file.write(data);
                if (data.length != getSize()) {
                    size = data.length;

                    if (writeExportTable) {
                        getUnrealPackage().file.setPosition(getUnrealPackage().getExportTableOffset());
                        getUnrealPackage().writeExportTable(getUnrealPackage().getExportTable());
                    }
                }
            } else {
//                //clear
//                getUnrealPackage().file.setPosition(getOffset());
//                getUnrealPackage().file.write(new byte[getSize()]);

                getUnrealPackage().file.setPosition(getUnrealPackage().getDataEndOffset());
                offset = getUnrealPackage().file.getPosition();
                size = data.length;
                getUnrealPackage().file.write(data);

                int nameTablePosition = getUnrealPackage().file.getPosition();
                getUnrealPackage().writeNameTable(getUnrealPackage().getNameTable());
                int importTablePosition = getUnrealPackage().file.getPosition();
                getUnrealPackage().writeImportTable(getUnrealPackage().getImportTable());
                int exportTablePosition = getUnrealPackage().file.getPosition();
                getUnrealPackage().writeExportTable(getUnrealPackage().getExportTable());

                getUnrealPackage().file.setPosition(NAME_OFFSET_OFFSET);
                getUnrealPackage().file.writeInt(nameTablePosition);
                getUnrealPackage().file.setPosition(EXPORT_OFFSET_OFFSET);
                getUnrealPackage().file.writeInt(exportTablePosition);
                getUnrealPackage().file.setPosition(IMPORT_OFFSET_OFFSET);
                getUnrealPackage().file.writeInt(importTablePosition);
            }
        }

        @Override
        public String getObjectFullName() {
            String str = fullName.get();
            if (str == null) {
                str = getUnrealPackage().getPackageName() + "." + getObjectInnerFullName();
                fullName = new SoftReference<>(str);
            }
            return str;
        }

        @Override
        public String toString() {
            return getObjectInnerFullName();
        }

        @Override
        public ExportEntry previous() {
            return getUnrealPackage().getExportTable().get(getIndex() - 1);
        }

        @Override
        public ExportEntry next() {
            return getUnrealPackage().getExportTable().get(getIndex() + 1);
        }
    }

    public static final class ImportEntry extends Entry implements UnrealPackageReadOnly.ImportEntry {
        private int classPackage;
        private int className;

        private Reference<String> fullClassName = new SoftReference<>(null);

        private ImportEntry(UnrealPackageFile unrealPackage, int index, int classPackage, int className, int objectPackage, int objectName) {
            super(unrealPackage, index, objectPackage, objectName);
            this.classPackage = classPackage;
            this.className = className;
        }

        @Override
        public int getObjectReference() {
            return -(getIndex() + 1);
        }

        @Override
        public NameEntry getClassPackage() {
            return getUnrealPackage().getNameTable().get(classPackage);
        }

        @Override
        public NameEntry getClassName() {
            return getUnrealPackage().getNameTable().get(className);
        }

        @Override
        public String getFullClassName() {
            String str = fullClassName.get();
            if (str == null) {
                NameEntry pckg = getClassPackage();
                str = pckg == null ? getClassName().getName() : pckg.getName() + '.' + getClassName().getName();
                fullClassName = new SoftReference<>(str);
            }
            return str;
        }

        @Override
        public ImportEntry previous() {
            return getUnrealPackage().getImportTable().get(getIndex() - 1);
        }

        @Override
        public ImportEntry next() {
            return getUnrealPackage().getImportTable().get(getIndex() + 1);
        }
    }
}
